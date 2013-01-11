/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
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
package org.switchyard.component.jca.composer;

import org.switchyard.component.jca.processor.cci.StreamableRecord;

/**
 * Streamable record binding data.
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; &copy; 2012 Red Hat Inc.
 * @author Antti Laisi
 */
public class StreamableRecordBindingData implements RecordBindingData<StreamableRecord> {

    private final StreamableRecord _record;

    /**
     * Constructs a new streamable record binding data with the specified record.
     * @param record the specified streamable record
     */
    public StreamableRecordBindingData(StreamableRecord record) {
        _record = record;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamableRecord getRecord() {
        return _record;
    }

}
