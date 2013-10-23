package org.switchyard.component.bean.tests;
import org.junit.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.Message;

import org.switchyard.test.Invoker;
import org.switchyard.test.ServiceOperation;
import org.switchyard.test.SwitchYardRunner;
import org.switchyard.test.SwitchYardTestCaseConfig;
import org.switchyard.component.test.mixins.cdi.CDIMixIn;
/*
 * Assorted methods for testing a CDI bean consuming a service in SwitchYard.
 */
@RunWith(SwitchYardRunner.class)
@SwitchYardTestCaseConfig(mixins = CDIMixIn.class)
public class UnboxTest {
    @ServiceOperation("UnboxService.unboxBoolean")
    private Invoker unboxBoolean;
    
    @ServiceOperation("UnboxService.unboxByte")
    private Invoker unboxByte;
    
    @ServiceOperation("UnboxService.unboxChar")
    private Invoker unboxChar;
        
    @ServiceOperation("UnboxService.unboxDouble")
    private Invoker unboxDouble;

    @ServiceOperation("UnboxService.unboxFloat")
    private Invoker unboxFloat;

    @ServiceOperation("UnboxService.unboxInt")
    private Invoker unboxInt;

    @ServiceOperation("UnboxService.unboxLong")
    private Invoker unboxLong;

    @ServiceOperation("UnboxService.unboxShort")
    private Invoker unboxShort;
    
    @Test
    public void unboxBooleanTest() {
    	 Message response = unboxBoolean.sendInOut(true);
    	Assert.assertEquals(true, response.getContent());
    }
    
    @Test
    public void testUnboxByte() {
    	byte c = 100;
    	Message response = unboxByte.sendInOut(c);
    	Assert.assertEquals(c, response.getContent());
    }
    
    @Test
    public void testUnboxChar() {
    	char b = 'c';
    	Message response = unboxChar.sendInOut(b);
    	Assert.assertEquals(b, response.getContent());
    }
    
    @Test
    public void testUnboxDouble() {
    	double b = 1;
    	Message response = unboxDouble.sendInOut(b);
    	Assert.assertEquals(b, response.getContent());
    }
    
    @Test
    public void testUnboxFloat() {
    	float b = 100;
    	Message response = unboxFloat.sendInOut(b);
    	Assert.assertEquals(b, response.getContent());
    }
    
    @Test
    public void testUnboxInt() {
    	int b = 100;
    	 Message response = unboxInt.sendInOut(b);
    	Assert.assertEquals(b, response.getContent());
    }
    
    @Test
    public void unboxLongTest() {
    	long b = 100;
    	 Message response = unboxLong.sendInOut(b);
    	Assert.assertEquals(b, response.getContent());
    }
    
    @Test
    public void testUnboxShort() {
    	short b = 100;
    	 Message response = unboxShort.sendInOut(b);
    	Assert.assertEquals(b, response.getContent());
    }

}
