package org.switchyard.component.sca;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.switchyard.Exchange;
import org.switchyard.HandlerException;
import org.switchyard.Message;
import org.switchyard.MockDomain;
import org.switchyard.MockExchange;
import org.switchyard.Service;
import org.switchyard.ServiceReference;
import org.switchyard.SwitchYardException;
import org.switchyard.config.model.composite.CompositeReferenceModel;
import org.switchyard.config.model.composite.v1.V1CompositeReferenceModel;
import org.switchyard.config.model.composite.v1.V1SCABindingModel;
import org.switchyard.config.model.switchyard.SwitchYardNamespace;
import org.switchyard.metadata.InOnlyOperation;
import org.switchyard.metadata.InOnlyService;
import org.switchyard.remote.RemoteEndpoint;
import org.switchyard.remote.RemoteRegistry;
import org.switchyard.remote.cluster.LoadBalanceStrategy;
import org.switchyard.remote.cluster.RandomStrategy;
import org.switchyard.remote.cluster.RoundRobinStrategy;
import org.switchyard.test.MockHandler;

public class SCAInvokerTest {
    
    private MockDomain _domain;
    private MockHandler _provider;
    
    @Before
    public void setUp() throws Exception {
        _domain = new MockDomain();
        _provider = new MockHandler();
    }
    
    @Test
    public void localInvocationSameName() throws Exception {
        V1SCABindingModel config = new V1SCABindingModel(SwitchYardNamespace.DEFAULT.uri()) {
            @Override
            public String getName() {
                return "testBinding";
            }
            public CompositeReferenceModel getReference() {
                return new V1CompositeReferenceModel();
            };
        };
        SCAInvoker invoker = new SCAInvoker(config);
        invoker.start();
        
        final QName TEST_SERVICE = new QName("urn:test", "SCAInvokerTest");
        ServiceReference reference = _domain.createInOnlyService(TEST_SERVICE, _provider);
        MockExchange ex = new MockExchange();
        Message msg = ex.createMessage().setContent("TEST");
        ex.setMessage(msg);
        ex.consumer(reference, new InOnlyOperation("Test"));
        ex.provider(_domain.getServices().get(0), new InOnlyOperation("Test"));
        invoker.handleMessage(ex);
        
        Assert.assertTrue(_provider.getMessages().size() == 1);
        Assert.assertEquals(msg.getContent(), _provider.getMessages().poll().getMessage().getContent());
    }
    
    @Test
    public void localInvocationTarget() throws Exception {
        V1SCABindingModel config = new V1SCABindingModel(SwitchYardNamespace.DEFAULT.uri()) {
            @Override
            public String getName() {
                return "testBinding";
            }
            public CompositeReferenceModel getReference() {
                return new V1CompositeReferenceModel();
            };
        };
        config.setTarget("ServiceB");
        SCAInvoker invoker = new SCAInvoker(config);
        invoker.start();

        // Serivce A is for app 1, Service B is for app 2
        final QName SERVICE_A = new QName("urn:test", "ServiceA");
        final QName SERVICE_B = new QName("urn:test", "ServiceB");
        ServiceReference referenceA = _domain.registerServiceReference(SERVICE_A, new InOnlyService());
        _domain.registerServiceReference(SERVICE_B, new InOnlyService());
        Service serviceA = _domain.registerService(SERVICE_A, new InOnlyService(), new MockHandler());
        _domain.registerService(SERVICE_B, new InOnlyService(), _provider);
        
        MockExchange ex = new MockExchange();
        Message msg = ex.createMessage().setContent("TEST");
        ex.setMessage(msg);
        ex.consumer(referenceA, new InOnlyOperation(null));
        ex.provider(serviceA, new InOnlyOperation(null));
        invoker.handleMessage(ex);
        
        Assert.assertTrue(_provider.getMessages().size() == 1);
        Assert.assertEquals(msg.getContent(), _provider.getMessages().poll().getMessage().getContent());
    }
    
    @Test
    public void crossDomain() throws Exception {
        V1SCABindingModel config = new V1SCABindingModel(SwitchYardNamespace.DEFAULT.uri()) {
            @Override
            public String getName() {
                return "testBinding";
            }
            public CompositeReferenceModel getReference() {
                return new V1CompositeReferenceModel();
            };
        };
        config.setTargetNamespace("urn:testb");
        SCAInvoker invoker = new SCAInvoker(config);
        invoker.start();

        // service names with different namespace
        final QName SERVICE_A = new QName("urn:testa", "Service");
        final QName SERVICE_B = new QName("urn:testb", "Service");
        
        // two domains
        MockDomain domain1 = _domain;
        MockDomain domain2 = new MockDomain(domain1.getServiceRegistry());

        ServiceReference referenceA = domain1.registerServiceReference(SERVICE_A, new InOnlyService());
        Service serviceA = domain1.registerService(SERVICE_A, new InOnlyService(), new MockHandler());
        domain2.registerServiceReference(SERVICE_B, new InOnlyService());
        domain2.registerService(SERVICE_B, new InOnlyService(), _provider);
        
        MockExchange ex = new MockExchange();
        Message msg = ex.createMessage().setContent("TEST");
        ex.setMessage(msg);
        ex.consumer(referenceA, new InOnlyOperation(null));
        ex.provider(serviceA, new InOnlyOperation(null));
        invoker.handleMessage(ex);
        
        Assert.assertTrue(_provider.getMessages().size() == 1);
        Assert.assertEquals(msg.getContent(), _provider.getMessages().poll().getMessage().getContent());
    }
    
    @Test
    public void localInvocationWithInvalidServiceReference() throws Exception {
        boolean fail = false;
        
        V1SCABindingModel config = new V1SCABindingModel(SwitchYardNamespace.DEFAULT.uri()) {
            @Override
            public String getName() {
                return "testBinding";
            }
            public CompositeReferenceModel getReference() {
                return new V1CompositeReferenceModel();
            };
        };
        config.setTarget("Blah");
        SCAInvoker invoker = new SCAInvoker(config);
        invoker.start();

        // Create one valid but unregistered reference
        final QName SERVICE_A = new QName("urn:test", "ServiceA");
        ServiceReference referenceA = _domain.registerServiceReference(SERVICE_A, new InOnlyService());       

        Service serviceA = _domain.registerService(SERVICE_A, new InOnlyService(), null);
        _domain.registerService(SERVICE_A, new InOnlyService(), _provider);
        
        MockExchange ex = new MockExchange();
        Message msg = ex.createMessage().setContent("TEST");
        ex.setMessage(msg);
        ex.consumer(referenceA, new InOnlyOperation(null));
        ex.provider(serviceA, new InOnlyOperation(null));
        
        // Should throw a Handler exception
        try {
            invoker.handleMessage(ex);
        } catch (HandlerException e) {
            String message = e.getMessage();
            Assert.assertTrue(message.contains("SWITCHYARD039604"));
            fail = true;
        }
        
        Assert.assertTrue(fail);
    }
    
    @Test
    public void localInvocationTargetNamespace() throws Exception {
        V1SCABindingModel config = new V1SCABindingModel(SwitchYardNamespace.DEFAULT.uri()) {
            @Override
            public String getName() {
                return "testBinding";
            }
            public CompositeReferenceModel getReference() {
                return new V1CompositeReferenceModel();
            };
        };
        config.setTargetNamespace("urn:app2");
        SCAInvoker invoker = new SCAInvoker(config);
        invoker.start();

        // urn:app1 is for app 1, urn:app2 is for app 2
        final QName SERVICE_A = new QName("urn:app1", "ServiceA");
        final QName SERVICE_B = new QName("urn:app2", "ServiceA");
        ServiceReference referenceA = _domain.registerServiceReference(SERVICE_A, new InOnlyService());
        _domain.registerServiceReference(SERVICE_B, new InOnlyService());
        Service serviceA = _domain.registerService(SERVICE_A, new InOnlyService(), new MockHandler());
        _domain.registerService(SERVICE_B, new InOnlyService(), _provider);
        
        MockExchange ex = new MockExchange();
        Message msg = ex.createMessage().setContent("TEST");
        ex.setMessage(msg);
        ex.consumer(referenceA, new InOnlyOperation(null));
        ex.provider(serviceA, new InOnlyOperation(null));
        invoker.handleMessage(ex);
        
        Assert.assertTrue(_provider.getMessages().size() == 1);
        Assert.assertEquals(msg.getContent(), _provider.getMessages().poll().getMessage().getContent());
    }
    
    @Test
    public void createOOTBLoadBalancers() throws Exception {
        V1SCABindingModel config = new V1SCABindingModel(SwitchYardNamespace.DEFAULT.uri()) {
            @Override
            public String getName() {
                return "testBinding";
            }
            public CompositeReferenceModel getReference() {
                return new V1CompositeReferenceModel();
            };
        };
        SCAInvoker invoker = new SCAInvoker(config);
        invoker.start();
        
        LoadBalanceStrategy roundRobin = invoker.createLoadBalancer("RoundRobinStrategy");
        Assert.assertTrue(roundRobin instanceof RoundRobinStrategy);
        LoadBalanceStrategy random = invoker.createLoadBalancer("RandomStrategy");
        Assert.assertTrue(random instanceof RandomStrategy);
    }
    
    @Test
    public void contextPropertiesCopied() throws Exception {
        V1SCABindingModel config = new V1SCABindingModel(SwitchYardNamespace.DEFAULT.uri()) {
            @Override
            public String getName() {
                return "testBinding";
            }
            public CompositeReferenceModel getReference() {
                return new V1CompositeReferenceModel();
            };
        };
        SCAInvoker invoker = new SCAInvoker(config);
        invoker.start();
        
        final QName TEST_SERVICE = new QName("urn:test", "SCAInvokerTest");
        ServiceReference reference = _domain.createInOnlyService(TEST_SERVICE, _provider);
        MockExchange ex = new MockExchange();
        Message msg = ex.createMessage().setContent("TEST");
        ex.setMessage(msg);
        ex.getMessage().getContext().setProperty("message-prop", "abc");
        ex.consumer(reference, new InOnlyOperation("Test"));
        ex.provider(_domain.getServices().get(0), new InOnlyOperation("Test"));
        invoker.handleMessage(ex);
        
        Assert.assertTrue(_provider.getMessages().size() == 1);
        Exchange receivedEx = _provider.getMessages().poll();
        Assert.assertEquals("abc", receivedEx.getMessage().getContext().getPropertyValue("message-prop"));
    }
    
    @Test
    public void createCustomLoadBalancers() throws Exception {
        V1SCABindingModel config = new V1SCABindingModel(SwitchYardNamespace.DEFAULT.uri()) {
            @Override
            public String getName() {
                return "testBinding";
            }
            public CompositeReferenceModel getReference() {
                return new V1CompositeReferenceModel();
            };
        };
        SCAInvoker invoker = new SCAInvoker(config);
        invoker.start();
        
        LoadBalanceStrategy lb = invoker.createLoadBalancer("org.switchyard.component.sca.MyLoadBalancer");
        Assert.assertTrue(lb instanceof MyLoadBalancer);
        
        // try to create a bogus load balancer
        try {
            invoker.createLoadBalancer("org.foo.Bar");
            Assert.fail("Should not be able to create a bogus load balance strategy");
        } catch (SwitchYardException ex) {
            // expected
        }
    }
}

class MyLoadBalancer implements LoadBalanceStrategy {

    @Override
    public RemoteEndpoint selectEndpoint(QName serviceName) {
        return null;
    }

    @Override
    public RemoteRegistry getRegistry() {
        return null;
    }

    @Override
    public void setRegistry(RemoteRegistry registry) {
    }
}
