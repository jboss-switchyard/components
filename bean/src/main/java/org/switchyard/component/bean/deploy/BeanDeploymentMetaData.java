/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved. 
 * See the copyright.txt in the distribution for a 
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use, 
 * modify, copy, or redistribute it subject to the terms and conditions 
 * of the GNU Lesser General Public License, v. 2.1. 
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details. 
 * You should have received a copy of the GNU Lesser General Public License, 
 * v.2.1 along with this distribution; if not, write to the Free Software 
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */

package org.switchyard.component.bean.deploy;

import org.switchyard.common.cdi.CDIUtil;
import org.switchyard.component.bean.ClientProxyBean;
import org.switchyard.exception.SwitchYardException;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Bean Deployment Meta Data.
 * <p/>
 * All the CDI bean info for a specific deployment.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BeanDeploymentMetaData {

    private BeanManager _beanManager;
    private ClassLoader _deploymentClassLoader;
    private List<ServiceDescriptor> _serviceDescriptors = new ArrayList<ServiceDescriptor>();
    private List<ClientProxyBean> _clientProxies = new ArrayList<ClientProxyBean>();
    private List<CDIBean> _deploymentBeans = new ArrayList<CDIBean>();

    /**
     * Default no-arg constructor.
     */
    public BeanDeploymentMetaData() {}

    /**
     * Set the deployment CDI BeanManager.
     * @param beanManager The bean manager.
     * @return this instance.
     */
    public BeanDeploymentMetaData setBeanManager(BeanManager beanManager) {
        _beanManager = beanManager;
        return this;
    }

    /**
     * Get the deployment CDI BeanManager.
     * @return The bean manager.
     */
    public BeanManager getBeanManager() {
        return _beanManager;
    }

    /**
     * Set the deployment ClassLoader.
     * @param deploymentClassLoader The deployment ClassLoader.
     * @return this instance.
     */
    public BeanDeploymentMetaData setDeploymentClassLoader(ClassLoader deploymentClassLoader) {
        _deploymentClassLoader = deploymentClassLoader;
        return this;
    }

    /**
     * Get the deployment ClassLoader.
     * @return The deployment ClassLoader.
     */
    public ClassLoader getDeploymentClassLoader() {
        return _deploymentClassLoader;
    }

    /**
     * Add a {@link ServiceDescriptor}.
     * @param serviceDescriptor The descriptor instance.
     */
    public void addServiceDescriptor(ServiceDescriptor serviceDescriptor) {
        _serviceDescriptors.add(serviceDescriptor);
    }

    /**
     * Add a {@link ClientProxyBean}.
     * @param proxy The proxy instance.
     */
    public void addClientProxy(ClientProxyBean proxy) {
        _clientProxies.add(proxy);
    }

    /**
     * Add a deployment CDI bean.
     * @param bean The CDI bean instance.
     */
    public void addDeploymentBean(CDIBean bean) {
        _deploymentBeans.add(bean);
    }

    /**
     * Add a list of all the {@link ServiceDescriptor ServiceDescriptors}.
     * @return The list of all the {@link ServiceDescriptor ServiceDescriptors}.
     */
    public List<ServiceDescriptor> getServiceDescriptors() {
        return Collections.unmodifiableList(_serviceDescriptors);
    }

    /**
     * Add a list of all the {@link ClientProxyBean ClientProxyBeans}.
     * @return The list of all the {@link ClientProxyBean ClientProxyBeans}.
     */
    public List<ClientProxyBean> getClientProxies() {
        return Collections.unmodifiableList(_clientProxies);
    }

    /**
     * Get a list of all beans in the deployment.
     * @return The list of all beans in the deployment.
     */
    public List<CDIBean> getDeploymentBeans() {
        return Collections.unmodifiableList(_deploymentBeans);
    }

    /**
     * Lookup the BeanDeploymentMetaData for the current deployment.
     * @return The BeanDeploymentMetaData.
     */
    public static BeanDeploymentMetaData lookupBeanDeploymentMetaData() {
        BeanManager beanManager = CDIUtil.lookupBeanManager();
        if (beanManager == null) {
            throw new SwitchYardException("Failed to lookup BeanManager.  Must be bound into java:comp as per CDI specification.");
        }

        Set<Bean<?>> beans = beanManager.getBeans(BeanDeploymentMetaData.class);
        if (beans.isEmpty()) {
            throw new SwitchYardException("Failed to lookup BeanDeploymentMetaData from BeanManager.  Must be bound into BeanManager.  Perhaps SwitchYard CDI Extensions not properly installed in container.");
        }
        if (beans.size() > 1) {
            throw new SwitchYardException("Failed to lookup BeanDeploymentMetaData from BeanManager.  Multiple beans resolved for type '" + BeanDeploymentMetaData.class.getName() + "'.");
        }

        BeanDeploymentMetaDataCDIBean bean = (BeanDeploymentMetaDataCDIBean) beans.iterator().next();

        return bean.getBeanMetaData();
    }

}
