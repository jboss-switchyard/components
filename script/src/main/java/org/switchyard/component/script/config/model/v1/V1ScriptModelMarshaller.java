package org.switchyard.component.script.config.model.v1;

import org.switchyard.component.script.config.model.ScriptComponentImplementationModel;
import org.switchyard.config.Configuration;
import org.switchyard.config.model.BaseMarshaller;
import org.switchyard.config.model.Descriptor;
import org.switchyard.config.model.Model;
import org.switchyard.config.model.composite.ComponentImplementationModel;

/**
 * Version 1 marshaller for a Script model.
 * 
 * @author Jiri Pechanec
 * @author Daniel Bevenius
 *
 */
public class V1ScriptModelMarshaller extends BaseMarshaller {

    /**
     * Sole constructor.
     * 
     * @param desc The descriptor for this model.
     */
    public V1ScriptModelMarshaller(Descriptor desc) {
        super(desc);
    }

    @Override
    public Model read(final Configuration config) {
        final String name = config.getName();
        if (name.startsWith(ComponentImplementationModel.IMPLEMENTATION)) {
            return new V1ScriptComponentImplementationModel(config, getDescriptor());
        }
        
        if (name.startsWith(ScriptComponentImplementationModel.CODE)) {
            return new V1CodeModel(config, getDescriptor());
        }
        
        return null;
    }

}
