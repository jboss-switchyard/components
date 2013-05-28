/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009-11, Red Hat Middleware LLC, and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.switchyard.component.bpm.exchange;

import org.kie.event.process.ProcessCompletedEvent;
import org.kie.event.process.ProcessEventListener;
import org.kie.event.process.ProcessNodeLeftEvent;
import org.kie.event.process.ProcessNodeTriggeredEvent;
import org.kie.event.process.ProcessStartedEvent;
import org.kie.event.process.ProcessVariableChangedEvent;
import org.switchyard.event.EventPublisher;

/**
 * This process event listener routes select events to the switchyard
 * domain's event manager.
 *
 */
public class ProcessListener implements ProcessEventListener {

    private final EventPublisher _publisher;
    
    /**
     * The constructor.
     * 
     * @param ep The event publisher
     */
    public ProcessListener(EventPublisher ep) {
        _publisher = ep;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        _publisher.publish((java.util.EventObject)event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent event) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        _publisher.publish((java.util.EventObject)event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
     }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent event) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterVariableChanged(ProcessVariableChangedEvent event) {
        _publisher.publish((java.util.EventObject)event);
    }

}
