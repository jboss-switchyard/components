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
package org.switchyard.component.camel.netty.model;

/**
 * Configuration binding for tcp gateway.
 * 
 * @author Lukasz Dywicki
 */
public interface CamelNettyUdpBindingModel extends CamelNettyBindingModel {

    /**
     * Get type of UDP transmission - broadcast or multicast.
     * 
     * @return True if broadcast should be used.
     */
    Boolean isBroadcast();

    /**
     * Setting to choose broadcast over UDP.
     * 
     * @param broadcast Use broadcast instead of multicast
     * @return a reference to this binding model
     */
    CamelNettyBindingModel setBroadcast(Boolean broadcast);

}
