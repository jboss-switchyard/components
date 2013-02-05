# Switchyard Script Component
This project provides an implementation.script enabling the usage of the any JVM scripting languge with JSR-223 support in a SwitchYard service implementation.

_ _ _

## Using script "inlined" in SwitchYard
    <sd:switchyard 
        xmlns="urn:switchyard-component-script:config:1.0" 
        xmlns:sca="http://docs.oasis-open.org/ns/opencsa/sca/200912" 
        xmlns:sd="urn:switchyard-config:switchyard:1.0"
        xmlns:bean="urn:switchyard-component-bean:config:1.0">
    
        <sca:composite>
        
            <sca:component name="ScriptComponent">
                <sca:service name="OrderService" >
                    <sca:interface.java interface="org.switchyard.component.script.deploy.support.OrderService"/>
                </sca:service>
                
                <implementation.script language="JavaScript">
                    <code>
			'Hello ' + exchange.message.content
                    </code>
                </implementation.script>
            </sca:component>
            
        </sca:composite>

    </sd:switchyard>

The script languge is defined by the attribute *language* of *implementation.script*

Calling this service is done in the same way as calling any other SwitchYard service, for example:

    String title = (String) newInvoker("OrderService").operation("getTitleForItem").sendInOut(10).getContent(String.class);

_ _ _

## Using external script 
    <sd:switchyard 
        xmlns="urn:switchyard-component-script:config:1.0" 
        xmlns:sca="http://docs.oasis-open.org/ns/opencsa/sca/200912" 
        xmlns:sd="urn:switchyard-config:switchyard:1.0"
        xmlns:bean="urn:switchyard-component-bean:config:1.0">
    
        <sca:composite>
        
            <sca:component name="scriptComponent">
                <sca:service name="OrderService" >
                    <sca:interface.java interface="org.switchyard.component.script.deploy.support.OrderService"/>
                </sca:service>
                
                <implementation.script scriptFile="script.js"/>
            </sca:component>
            
        </sca:composite>

    </sd:switchyard>
    
The script can be located on the classpath or in an external file.
The scripting language can be defined by the *language* attribute or is devised from an extension of a scripting file
_ _ _

## Message content/Exchange injection
The invoked script has binded one of two variables

* exchange - representing SwitchYard exchange and available if *injectExchange* attribute of *implementation.script* set to true
* content - content of the incoming message otherwise
    <implementation.script injectExchange="true" scriptFile="file://sample.js"/>
    
_ _ _

