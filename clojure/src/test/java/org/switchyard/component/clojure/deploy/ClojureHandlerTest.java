/*
 * JBoss, Home of Professional Open Source Copyright 2009, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.switchyard.component.clojure.deploy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.switchyard.Exchange;
import org.switchyard.Message;
import org.switchyard.component.clojure.config.model.ClojureComponentImplementationModel;

/**
 * 
 * @author Daniel Bevenius
 *
 */
public class ClojureHandlerTest
{
    @Test
    public void handleMessage() throws Exception {
        final ClojureComponentImplementationModel implModel = mock(ClojureComponentImplementationModel.class);
        final String clj = "(ns org.switchyard (:import org.switchyard.Exchange))(defn process [ex] (println (.getContent (.getMessage ex))))";
        when(implModel.getScript()).thenReturn(clj);
        final Exchange exchange = mock(Exchange.class);
        final Message msg = mock(Message.class);
        when(msg.getContent()).thenReturn("payload");
        when(exchange.getMessage()).thenReturn(msg);
        
        final ClojureHandler clojureHandler = new ClojureHandler(implModel);
        clojureHandler.start(null);
        clojureHandler.handleMessage(exchange);
        
    }

}
