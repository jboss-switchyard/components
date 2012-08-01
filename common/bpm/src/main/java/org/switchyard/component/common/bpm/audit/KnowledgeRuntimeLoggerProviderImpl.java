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
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;

public class KnowledgeRuntimeLoggerProviderImpl implements SYKnowledgeRuntimeLoggerFactoryService {

    
    public KnowledgeRuntimeLogger newJPALogger(KnowledgeRuntimeEventManager session) {
        JPAWorkingMemoryDbLogger logger = new JPAWorkingMemoryDbLogger( session );
        return new KnowledgeRuntimeJPALoggerWrapper( logger );
    }

     private class KnowledgeRuntimeJPALoggerWrapper implements KnowledgeRuntimeLogger {

        private JPAWorkingMemoryDbLogger logger;

        public KnowledgeRuntimeJPALoggerWrapper(JPAWorkingMemoryDbLogger logger) {
            this.logger = logger;
        }
        
        public void close() {
            logger.dispose();
        }
    }
}
