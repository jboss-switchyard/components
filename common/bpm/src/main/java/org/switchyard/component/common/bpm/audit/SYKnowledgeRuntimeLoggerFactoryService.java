package org.switchyard.component.common.bpm.audit;

import org.drools.event.KnowledgeRuntimeEventManager;
import org.drools.logger.KnowledgeRuntimeLogger;

public interface SYKnowledgeRuntimeLoggerFactoryService {
    
    KnowledgeRuntimeLogger newJPALogger(KnowledgeRuntimeEventManager session);
}
