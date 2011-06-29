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
package org.switchyard.component.bpm.config.model.v1;

import static org.switchyard.component.bpm.process.ProcessConstants.MESSAGE_CONTENT_NAME;
import static org.switchyard.component.bpm.process.ProcessConstants.PROCESS_DEFINITION;
import static org.switchyard.component.bpm.process.ProcessConstants.PROCESS_DEFINITION_TYPE;
import static org.switchyard.component.bpm.process.ProcessConstants.PROCESS_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.switchyard.component.bpm.config.model.BpmComponentImplementationModel;
import org.switchyard.component.bpm.config.model.ProcessResourceModel;
import org.switchyard.component.bpm.config.model.TaskHandlerModel;
import org.switchyard.component.bpm.process.ProcessResourceType;
import org.switchyard.config.Configuration;
import org.switchyard.config.model.Descriptor;
import org.switchyard.config.model.composite.v1.V1ComponentImplementationModel;

/**
 * A "bpm" implementation of a ComponentImplementationModel.
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; (C) 2011 Red Hat Inc.
 */
public class V1BpmComponentImplementationModel extends V1ComponentImplementationModel implements BpmComponentImplementationModel {

    private List<ProcessResourceModel> _processResources = new ArrayList<ProcessResourceModel>();
    private List<TaskHandlerModel> _taskHandlers = new ArrayList<TaskHandlerModel>();

    /**
     * Default constructor for application use.
     */
    public V1BpmComponentImplementationModel() {
        super(BPM, DEFAULT_NAMESPACE);
        setModelChildrenOrder(ProcessResourceModel.PROCESS_RESOURCE, TaskHandlerModel.TASK_HANDLER);
    }

    /**
     * Constructor for Marshaller use (ie: V1BpmMarshaller).
     *
     * @param config the Configuration
     * @param desc the Descriptor
     */
    public V1BpmComponentImplementationModel(Configuration config, Descriptor desc) {
        super(config, desc);
        for (Configuration processResource_config : config.getChildren(ProcessResourceModel.PROCESS_RESOURCE)) {
            ProcessResourceModel processResource = (ProcessResourceModel)readModel(processResource_config);
            if (processResource != null) {
                _processResources.add(processResource);
            }
        }
        for (Configuration taskHandler_config : config.getChildren(TaskHandlerModel.TASK_HANDLER)) {
            TaskHandlerModel taskHandler = (TaskHandlerModel)readModel(taskHandler_config);
            if (taskHandler != null) {
                _taskHandlers.add(taskHandler);
            }
        }
        setModelChildrenOrder(ProcessResourceModel.PROCESS_RESOURCE, TaskHandlerModel.TASK_HANDLER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProcessDefinition() {
        return getModelAttribute(PROCESS_DEFINITION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BpmComponentImplementationModel setProcessDefinition(String processDefinition) {
        setModelAttribute(PROCESS_DEFINITION, processDefinition);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessResourceType getProcessDefinitionType() {
        String pdt = getModelAttribute(PROCESS_DEFINITION_TYPE);
        return pdt != null ? ProcessResourceType.valueOf(pdt) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BpmComponentImplementationModel setProcessDefinitionType(ProcessResourceType processDefinitionType) {
        String pdt = processDefinitionType != null ? processDefinitionType.name() : null;
        setModelAttribute(PROCESS_DEFINITION_TYPE, pdt);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProcessId() {
        return getModelAttribute(PROCESS_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BpmComponentImplementationModel setProcessId(String processId) {
        setModelAttribute(PROCESS_ID, processId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessageContentName() {
        return getModelAttribute(MESSAGE_CONTENT_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BpmComponentImplementationModel setMessageContentName(String messageContentName) {
        setModelAttribute(MESSAGE_CONTENT_NAME, messageContentName);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProcessResourceModel> getProcessResources() {
        return Collections.unmodifiableList(_processResources);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BpmComponentImplementationModel addProcessResource(ProcessResourceModel processResource) {
        addChildModel(processResource);
        _processResources.add(processResource);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TaskHandlerModel> getTaskHandlers() {
        return Collections.unmodifiableList(_taskHandlers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BpmComponentImplementationModel addTaskHandler(TaskHandlerModel taskHandler) {
        addChildModel(taskHandler);
        _taskHandlers.add(taskHandler);
        return this;
    }

}
