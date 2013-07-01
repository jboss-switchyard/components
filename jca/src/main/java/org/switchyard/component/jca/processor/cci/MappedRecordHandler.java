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
package org.switchyard.component.jca.processor.cci;

import javax.resource.cci.Connection;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;

import org.switchyard.Exchange;
import org.switchyard.Message;
import org.switchyard.component.jca.composer.MappedRecordBindingData;

/**
 * MappedRecord handler.
 * 
 * @author <a href="mailto:tm.igarashi@gmail.com">Tomohisa Igarashi</a>
 *
 */
public class MappedRecordHandler extends RecordHandler<MappedRecordBindingData> {

    @Override
    public Message handle(Exchange exchange, Connection conn, Interaction interact) throws Exception {
        MappedRecord record = getRecordFactory().createMappedRecord(MappedRecordHandler.class.getName());
        MappedRecord outRecord = (MappedRecord) interact.execute(getInteractionSpec(), getMessageComposer(MappedRecordBindingData.class).decompose(exchange, new MappedRecordBindingData(record)).getRecord());
        return getMessageComposer(MappedRecordBindingData.class).compose(new MappedRecordBindingData(outRecord), exchange);
    }
}
