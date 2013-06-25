package org.switchyard.component.bean.internal.beanbag;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataSource;

import org.switchyard.Context;
import org.switchyard.Message;
import org.switchyard.Scope;
import org.switchyard.component.bean.BeanBag;
import org.switchyard.internal.DefaultContext;

/**
 * BeanBag provides access to the Context object for passed in service reference,
 * as well as request Context and request Message.
 */
public class BeanBagImpl implements BeanBag {
    
    private Message _outMessage;
    
    private Map<String, Context> _refContext = new HashMap<String, Context>();
    
    /**
     * Constructor.
     */
    public BeanBagImpl() {
    }
    
    @Override
    public Message getOutMessage() {
        if (_outMessage == null) {
            _outMessage = new BeanBagMessage();
        }
        return _outMessage;
    }

    @Override
    public Context getInContext(String reference) {
        
        if (_refContext.get(reference) == null) {
            _refContext.put(reference, new DefaultContext(Scope.EXCHANGE));
        }
        return _refContext.get(reference);
    }
    
    private class BeanBagMessage implements Message {
        private Context _context = new DefaultContext(Scope.MESSAGE);
        private Map<String,DataSource> _attachments = new HashMap<String,DataSource>();
        
        @Override
        public Context getContext() {
            return _context;
        }
        @Override
        public Message setContent(Object content) {
            return this;
        }
        @Override
        public Object getContent() {
            return null;
        }
        @Override
        public <T> T getContent(Class<T> type) {
            return null;
        }
        @Override
        public Message addAttachment(String name, DataSource attachment) {
            _attachments.put(name, attachment);
            return this;
        }
        @Override
        public DataSource getAttachment(String name) {
            return _attachments.get(name);
        }
        @Override
        public void removeAttachment(String name) {
            _attachments.remove(name);
        }
        @Override
        public Map<String, DataSource> getAttachmentMap() {
            return Collections.unmodifiableMap(_attachments);
        }
        @Override
        public Message copy() {
            BeanBagMessage bbm = new BeanBagMessage();
            bbm.getContext().setProperties(_context.getProperties());
            for (String key : _attachments.keySet()) {
                bbm.addAttachment(key, _attachments.get(key));
            }
            return bbm;
        }
    }
}
