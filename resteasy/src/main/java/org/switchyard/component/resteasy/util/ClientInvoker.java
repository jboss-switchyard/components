/*
 * 2012 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.switchyard.component.resteasy.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Providers;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.BaseClientResponse;
import org.jboss.resteasy.client.core.ClientInterceptorRepositoryImpl;
import org.jboss.resteasy.client.core.ClientInvokerInterceptorFactory;
import org.jboss.resteasy.client.core.extractors.ClientErrorHandler;
import org.jboss.resteasy.client.core.extractors.ClientRequestContext;
import org.jboss.resteasy.client.core.extractors.DefaultEntityExtractorFactory;
import org.jboss.resteasy.client.core.extractors.EntityExtractor;
import org.jboss.resteasy.client.core.extractors.EntityExtractorFactory;
import org.jboss.resteasy.client.core.marshallers.ClientMarshallerFactory;
import org.jboss.resteasy.client.core.marshallers.Marshaller;
import org.jboss.resteasy.specimpl.UriBuilderImpl;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.MediaTypeHelper;
import org.jboss.resteasy.util.IsHttpMethod;
import org.switchyard.component.resteasy.composer.RESTEasyBindingData;

/**
 * Client Invoker for RESTEasy gateway. Code lifted from RESTEasy.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2013 Red Hat Inc.
 */
public class ClientInvoker extends ClientInterceptorRepositoryImpl implements MethodInvoker {

    private static final Logger LOGGER = Logger.getLogger(ClientInvoker.class);

    private URI _baseUri;
    private String _subResourcePath;
    private Class<?> _resourceClass;
    private Method _method;
    private String _httpMethod;
    private UriBuilderImpl _uri;
    private MediaType _accepts;
    private Marshaller[] _marshallers;
    private ClientExecutor _executor;
    private EntityExtractor _extractor;
    private EntityExtractorFactory _extractorFactory;
    private ResteasyProviderFactory _providerFactory;

    private static URI createUri(String base) {
        try {
            return new URI(base);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a RESTEasy invoker client.
     *
     * @param basePath The base path for the class
     * @param resourceClass The JAX-RS Resource Class
     * @param method The JAX-RS Resource Class's method
     */
    public ClientInvoker(String basePath, Class<?> resourceClass, Method method) {
        Set<String> httpMethods = IsHttpMethod.getHttpMethods(method);
        _baseUri = createUri(basePath);
        if ((httpMethods == null || httpMethods.size() == 0) && method.isAnnotationPresent(Path.class) && method.getReturnType().isInterface()) {
            _subResourcePath = createSubResourcePath(basePath, method);
        } else if (httpMethods == null || httpMethods.size() != 1) {
            throw new RuntimeException("You must use at least one, but no more than one http method annotation on: " + method.toString());
        }
        _httpMethod = httpMethods.iterator().next();
        _resourceClass = resourceClass;
        _method = method;
        _uri = new UriBuilderImpl();
        _uri.uri(_baseUri);
        if (_resourceClass.isAnnotationPresent(Path.class)) {
            _uri.path(_resourceClass);
        }
        if (_method.isAnnotationPresent(Path.class)) {
            _uri.path(_method);
        }

        _providerFactory = ResteasyProviderFactory.getInstance();
        _executor = ClientRequest.getDefaultExecutor();
        _extractorFactory = new DefaultEntityExtractorFactory();
        _extractor = _extractorFactory.createExtractor(_method);
        _marshallers = ClientMarshallerFactory.createMarshallers(_resourceClass, _method, _providerFactory, null);
        _accepts = MediaTypeHelper.getProduces(_resourceClass, method, null);
        ClientInvokerInterceptorFactory.applyDefaultInterceptors(this, _providerFactory, _resourceClass, _method);
    }

    private static String createSubResourcePath(String base, Method method) {
        if (!base.endsWith("/")) {
            base = base + "/";
        }
        String path = method.getAnnotation(Path.class).value();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        Annotation[][] params = method.getParameterAnnotations();
        int index = 1;
        for (Annotation[] param : params) {
            for (Annotation a : param) {
                if (a instanceof PathParam) {
                    String name = ((PathParam) a).value();
                    path = path.replace("{" + name + "}", "%" + index + "$s");
                    break;
                }
            }
            index++;
        }
        return base + path;
    }

    @Override
    public RESTEasyBindingData invoke(Object[] args, MultivaluedMap<String, String> headers) {
        boolean isProvidersSet = ResteasyProviderFactory.getContextData(Providers.class) != null;
        if (!isProvidersSet) {
            ResteasyProviderFactory.pushContext(Providers.class, _providerFactory);
        }

        try {
            if (_uri == null) {
                throw new RuntimeException("You have not set a base URI for the client proxy");
            }

            ClientRequest request = null;

            BaseClientResponse clientResponse = null;
            try {
                request = createRequest(args, headers);
                clientResponse = (BaseClientResponse) request.httpMethod(_httpMethod);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ClientErrorHandler errorHandler = new ClientErrorHandler(_providerFactory.getClientErrorInterceptors());
            clientResponse.setAttributeExceptionsTo(_method.toString());
            clientResponse.setAnnotations(_method.getAnnotations());
            ClientRequestContext clientRequestContext = new ClientRequestContext(request, clientResponse, errorHandler, _extractorFactory, _baseUri);
            Object response = _extractor.extractEntity(clientRequestContext);
            RESTEasyBindingData restResponse = new RESTEasyBindingData();
            if (response != null) {
                restResponse.setParameters(new Object[]{response});
                // Propagate outgoing headers
                restResponse.setHeaders(clientResponse.getHeaders());
                restResponse.setStatusCode(clientResponse.getStatus());
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Incoming Headers to SwitchYard through OutboundHandler [");
                    for (Object key : clientResponse.getHeaders().keySet()) {
                        LOGGER.trace(key + " = " + clientResponse.getHeaders().get(key));
                    }
                    LOGGER.trace("]");
                }
            }
            return restResponse;
        } finally {
            if (!isProvidersSet) {
                ResteasyProviderFactory.popContextData(Providers.class);
            }
        }
    }

    private ClientRequest createRequest(Object[] args, MultivaluedMap<String, String> headers) {
        UriBuilder uriBuilder = _uri;
        if (_subResourcePath != null) {
            uriBuilder = UriBuilder.fromUri(String.format(_subResourcePath, args));
        }
        ClientRequest request = new ClientRequest(uriBuilder, _executor, _providerFactory);
        if (_accepts != null) {
            request.header(HttpHeaders.ACCEPT, _accepts.toString());
        }
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Outgoing Headers from SwitchYard through OutboundHandler [");
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                LOGGER.trace(entry.getKey() + " = " + entry.getValue());
            }
            LOGGER.trace("]");
        }
        this.copyClientInterceptorsTo(request);

        boolean isClientResponseResult = ClientResponse.class.isAssignableFrom(_method.getReturnType());
        request.followRedirects(!isClientResponseResult);

        for (int i = 0; i < _marshallers.length; i++) {
            _marshallers[i].build(request, args[i]);
        }
        return request;
    }
}
