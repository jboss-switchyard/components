/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
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

package org.switchyard.component.bean.config.model;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.switchyard.common.type.Classes;
import org.switchyard.component.bean.Service;
import org.switchyard.component.bean.tests.OneWay;
import org.switchyard.component.bean.tests.ServiceWithReferenceBean;
import org.switchyard.config.model.ScannerInput;
import org.switchyard.config.model.composite.ComponentImplementationModel;
import org.switchyard.config.model.composite.ComponentModel;
import org.switchyard.config.model.composite.ComponentReferenceModel;
import org.switchyard.config.model.switchyard.SwitchYardModel;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BeanSwitchYardScannerTest {
    
    private SwitchYardModel _scannedModel;
    private BeanSwitchYardScanner _scanner;
    private List<URL> _scannedURLs;
    
    @Before
    public void setUp() throws Exception {
        _scanner = new BeanSwitchYardScanner();

        // If running this test inside your IDE... you need to set the cwd to be the
        // root of the bean module !!
        _scannedURLs = new ArrayList<URL>();
        _scannedURLs.add(new File("./target/test-classes").toURI().toURL());

    }

    @Test
    public void test() throws Exception {
        scan(new File("./target/test-classes").toURI().toURL());
        List<ComponentModel> components = _scannedModel.getComposite().getComponents();
        for(ComponentModel component : components) {
            ComponentImplementationModel implementation = component.getImplementation();
            Assert.assertTrue(implementation instanceof BeanComponentImplementationModel);
            checkBeanModel((BeanComponentImplementationModel)implementation);
        }
    }
    
    // Verify that the ConsumerBean reference is picked up by the scanner
    @Test
    public void checkReference() throws Exception {
        scan(new File("./target/test-classes").toURI().toURL());
        ComponentModel consumerBeanModel = null;
        ComponentReferenceModel oneWayReference = null;
        
        for (ComponentModel component : _scannedModel.getComposite().getComponents()) {
            BeanComponentImplementationModel beanImp = 
                (BeanComponentImplementationModel)component.getImplementation();
            if (ServiceWithReferenceBean.class.getName().equals(beanImp.getClazz())) {
                consumerBeanModel = component;
                break;
            }
        }
        // If the bean wasn't found, then something is screwed up
        Assert.assertNotNull(consumerBeanModel);
        for (ComponentReferenceModel reference : consumerBeanModel.getReferences()) {
            if (reference.getName().equals(OneWay.class.getSimpleName())) {
                oneWayReference = reference;
            }
        }
        // OneWay reference should have been picked up by scanner
        Assert.assertNotNull(oneWayReference);
    }
    
    // verify an empty model is created
    @Test
    public void testEmptyScan() throws Exception {
        scan();
        Assert.assertNull("Composite element should not be created if no components were found.",
                _scannedModel.getComposite());
    }

    private void checkBeanModel(BeanComponentImplementationModel model) throws ClassNotFoundException {
        Class<?> serviceClass = Classes.forName(model.getClazz(), getClass());

        Assert.assertFalse(serviceClass.isInterface());
        Assert.assertFalse(Modifier.isAbstract(serviceClass.getModifiers()));
        Assert.assertTrue(serviceClass.isAnnotationPresent(Service.class));
    }
    
    // Takes a list of URLs to scan *instead* of what's defined in @Before.
    private void scan(URL ... urls) throws Exception {
        _scannedURLs.clear();
        if (urls != null && urls.length > 0) {
            _scannedURLs.addAll(Arrays.asList(urls));
        }
        ScannerInput<SwitchYardModel> input = new ScannerInput<SwitchYardModel>().setURLs(_scannedURLs);
        _scannedModel = _scanner.scan(input).getModel();
    }
}
