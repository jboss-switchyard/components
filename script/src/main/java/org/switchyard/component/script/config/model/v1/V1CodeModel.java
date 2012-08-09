package org.switchyard.component.script.config.model.v1;

import javax.xml.namespace.QName;

import org.switchyard.component.script.config.model.ScriptComponentImplementationModel;
import org.switchyard.component.script.config.model.CodeModel;
import org.switchyard.config.Configuration;
import org.switchyard.config.model.BaseNamedModel;
import org.switchyard.config.model.Descriptor;

/**
 * A version 1 implementation of a CodeModel.
 * 
 * @author Jiri Pechanec
 * @author Daniel Bevenius
 *
 */
public class V1CodeModel extends BaseNamedModel implements CodeModel {
    
    private String _code;
    
    /**
     * No-args constructor.
     */
    public V1CodeModel() {
        super(new QName(ScriptComponentImplementationModel.DEFAULT_NAMESPACE, CODE));
    }
    
    /**
     * Constructor.
     * 
     * @param config The configuration model.
     * @param desc The descriptor for this model.
     */
    public V1CodeModel(Configuration config, Descriptor desc) {
        super(config, desc);
    }
    
    @Override
    public String getCode() {
        if (_code != null) {
            return _code;
        }
        
        _code = getModelValue();
        return _code;
    }
    
    @Override
    public CodeModel setCode(final String code) {
        setModelValue(code);
        _code = code;
        return this;
    }

}
