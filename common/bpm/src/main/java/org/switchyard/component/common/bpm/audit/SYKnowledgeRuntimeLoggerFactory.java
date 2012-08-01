/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.switchyard.component.common.bpm.audit;

import org.drools.event.KnowledgeRuntimeEventManager;
import org.drools.logger.KnowledgeRuntimeLogger;


public class SYKnowledgeRuntimeLoggerFactory {
    
    private static SYKnowledgeRuntimeLoggerFactoryService knowledgeRuntimeLoggerFactoryService;
    
    public static KnowledgeRuntimeLogger newJPALogger(KnowledgeRuntimeEventManager session) {
          
        return getKnowledgeRuntimeLoggerProvider().newJPALogger( session);
    }
    
    private static synchronized void setKnowledgeRuntimeLoggerProvider(SYKnowledgeRuntimeLoggerFactoryService provider) {
        SYKnowledgeRuntimeLoggerFactory.knowledgeRuntimeLoggerFactoryService = provider;
    }

    private static synchronized SYKnowledgeRuntimeLoggerFactoryService getKnowledgeRuntimeLoggerProvider() {
        if ( knowledgeRuntimeLoggerFactoryService == null ) {
            loadProvider();
        }
        return knowledgeRuntimeLoggerFactoryService;
    }

    @SuppressWarnings("unchecked")
    private static void loadProvider() {
        try {
            Class<SYKnowledgeRuntimeLoggerFactoryService> cls = (Class<SYKnowledgeRuntimeLoggerFactoryService>) Class.forName( "org.switchyard.component.common.bpm.audit.KnowledgeRuntimeLoggerProviderImpl" );
            setKnowledgeRuntimeLoggerProvider( cls.newInstance() );
        } catch ( Exception e ) {
            throw new RuntimeException( "Provider org.switchyard.component.common.bpm.audit.KnowledgeRuntimeLoggerProviderImpl could not be set.", e);
        }
    }
    
    
}
