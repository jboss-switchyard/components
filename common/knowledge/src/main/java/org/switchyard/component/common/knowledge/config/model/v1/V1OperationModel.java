/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
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
package org.switchyard.component.common.knowledge.config.model.v1;

import static org.switchyard.component.common.knowledge.config.model.FaultsModel.FAULTS;
import static org.switchyard.component.common.knowledge.config.model.GlobalsModel.GLOBALS;
import static org.switchyard.component.common.knowledge.config.model.InputsModel.INPUTS;
import static org.switchyard.component.common.knowledge.config.model.OutputsModel.OUTPUTS;

import org.switchyard.common.xml.XMLHelper;
import org.switchyard.component.common.knowledge.OperationType;
import org.switchyard.component.common.knowledge.config.model.FaultsModel;
import org.switchyard.component.common.knowledge.config.model.GlobalsModel;
import org.switchyard.component.common.knowledge.config.model.InputsModel;
import org.switchyard.component.common.knowledge.config.model.OperationModel;
import org.switchyard.component.common.knowledge.config.model.OutputsModel;
import org.switchyard.config.Configuration;
import org.switchyard.config.model.BaseNamedModel;
import org.switchyard.config.model.Descriptor;

/**
 * The 1st version OperationModel.
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; &copy; 2012 Red Hat Inc.
 */
public abstract class V1OperationModel extends BaseNamedModel implements OperationModel {

    private GlobalsModel _globals;
    private InputsModel _inputs;
    private OutputsModel _outputs;
    private FaultsModel _faults;

    /**
     * Creates a new V1OperationModel in the specified namespace.
     * @param namespace the namespace
     */
    public V1OperationModel(String namespace) {
        super(XMLHelper.createQName(namespace, OPERATION));
        setModelChildrenOrder(GLOBALS, INPUTS, OUTPUTS, FAULTS);
    }

    /**
     * Creates a new V1OperationModel with the specified configuration and descriptor.
     * @param config the configuration
     * @param desc the descriptor
     */
    public V1OperationModel(Configuration config, Descriptor desc) {
        super(config, desc);
        setModelChildrenOrder(GLOBALS, INPUTS, OUTPUTS, FAULTS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEventId() {
        return getModelAttribute("eventId");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperationModel setEventId(String eventId) {
        setModelAttribute("eventId", eventId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperationModel setType(OperationType type) {
        setModelAttribute("type", type != null ? type.name() : null);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GlobalsModel getGlobals() {
        if (_globals == null) {
            _globals = (GlobalsModel)getFirstChildModel(GLOBALS);
        }
        return _globals;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperationModel setGlobals(GlobalsModel globals) {
        setChildModel(globals);
        _globals = globals;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputsModel getInputs() {
        if (_inputs == null) {
            _inputs = (InputsModel)getFirstChildModel(INPUTS);
        }
        return _inputs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperationModel setInputs(InputsModel inputs) {
        setChildModel(inputs);
        _inputs = inputs;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputsModel getOutputs() {
        if (_outputs == null) {
            _outputs = (OutputsModel)getFirstChildModel(OUTPUTS);
        }
        return _outputs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperationModel setOutputs(OutputsModel outputs) {
        setChildModel(outputs);
        _outputs = outputs;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FaultsModel getFaults() {
        if (_faults == null) {
            _faults = (FaultsModel)getFirstChildModel(FAULTS);
        }
        return _faults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperationModel setFaults(FaultsModel faults) {
        setChildModel(faults);
        _faults = faults;
        return this;
    }

}