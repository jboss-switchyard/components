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
package org.switchyard.component.camel.common.selector;

import org.switchyard.component.camel.common.composer.CamelBindingData;
import org.switchyard.component.common.selector.BaseOperationSelector;
import org.switchyard.config.model.selector.OperationSelectorModel;
import org.w3c.dom.Document;

/**
 * Camel OperationSelector implementation.
 */
public class CamelOperationSelector extends BaseOperationSelector<CamelBindingData> {

    /**
     * Constructor.
     * @param model OperationSelectorModel
     */
    public CamelOperationSelector(OperationSelectorModel model) {
        super(model);
    }

    @Override
    protected Document extractDomDocument(CamelBindingData content) {
        return content.getMessage().getBody(Document.class);
    }

    @Override
    protected String extractString(CamelBindingData content) {
        return content.getMessage().getBody(String.class);
    }

}
