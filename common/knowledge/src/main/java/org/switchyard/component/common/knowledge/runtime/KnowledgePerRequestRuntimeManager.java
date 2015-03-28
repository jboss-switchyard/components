package org.switchyard.component.common.knowledge.runtime;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.runtime.manager.impl.PerRequestRuntimeManager;
import org.jbpm.runtime.manager.impl.RuntimeEngineImpl;
import org.jbpm.runtime.manager.impl.RuntimeEngineInitlializer;
import org.jbpm.runtime.manager.impl.tx.DestroySessionTransactionSynchronization;
import org.jbpm.runtime.manager.impl.tx.DisposeSessionTransactionSynchronization;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.task.TaskService;
import org.kie.internal.runtime.manager.Disposable;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.SessionFactory;
import org.kie.internal.runtime.manager.TaskServiceFactory;
import org.kie.internal.task.api.InternalTaskService;

public class KnowledgePerRequestRuntimeManager extends PerRequestRuntimeManager {

    private static Map<String, ThreadLocal<RuntimeEngine>> local = new HashMap<String, ThreadLocal<RuntimeEngine>>();

    public KnowledgePerRequestRuntimeManager(RuntimeEnvironment environment, SessionFactory factory, TaskServiceFactory taskServiceFactory, String identifier) {
        super(environment, factory, taskServiceFactory, identifier);
    }

    @Override
    public RuntimeEngine getRuntimeEngine(Context<?> context) {
        if (isClosed()) {
            throw new IllegalStateException("Runtime manager " + identifier + " is already closed");
        }
        checkPermission();
        RuntimeEngine runtime = null;
        if (local.get(getIdentifier()) != null) {
            return local.get(getIdentifier()).get();
        }
        if (engineInitEager) {
            InternalTaskService internalTaskService = (InternalTaskService) getTaskServiceFactory().newTaskService();
            runtime = new RuntimeEngineImpl(getFactory().newKieSession(), internalTaskService);
            ((RuntimeEngineImpl) runtime).setManager(this);

            configureRuntimeOnTaskService(internalTaskService, runtime);
            registerDisposeCallback(runtime, new DisposeSessionTransactionSynchronization(this, runtime));
            registerDisposeCallback(runtime, new DestroySessionTransactionSynchronization(runtime.getKieSession()));
            registerItems(runtime);
            attachManager(runtime);
        } else {
            runtime = new RuntimeEngineImpl(context, new PerRequestInitializer());
            ((RuntimeEngineImpl) runtime).setManager(this);
        }
        ThreadLocal<RuntimeEngine> loc = new ThreadLocal<RuntimeEngine>();
        loc.set(runtime);
        local.put(getIdentifier(), loc);
        return runtime;
    }

    private class PerRequestInitializer implements RuntimeEngineInitlializer {

        @Override
        public KieSession initKieSession(Context<?> context, InternalRuntimeManager manager, RuntimeEngine engine) {
            RuntimeEngine inUse = local.get(getIdentifier()).get();
            if (inUse != null && ((RuntimeEngineImpl) inUse).internalGetKieSession() != null) {
                return inUse.getKieSession();
            }
            KieSession ksession = getFactory().newKieSession();
            ((RuntimeEngineImpl) engine).internalSetKieSession(ksession);
            registerDisposeCallback(engine, new DisposeSessionTransactionSynchronization(manager, engine));
            registerDisposeCallback(engine, new DestroySessionTransactionSynchronization(ksession));
            registerItems(engine);
            attachManager(engine);
            return ksession;
        }

        @Override
        public TaskService initTaskService(Context<?> context, InternalRuntimeManager manager, RuntimeEngine engine) {
            InternalTaskService internalTaskService = (InternalTaskService) getTaskServiceFactory().newTaskService();
            configureRuntimeOnTaskService(internalTaskService, engine);

            return internalTaskService;
        }

    }

    @Override
    public void validate(KieSession ksession, Context<?> context) throws IllegalStateException {
        if (isClosed()) {
            throw new IllegalStateException("Runtime manager " + identifier + " is already closed");
        }
        RuntimeEngine runtimeInUse = local.get(getIdentifier()).get();
        if (runtimeInUse == null || runtimeInUse.getKieSession().getIdentifier() != ksession.getIdentifier()) {
            throw new IllegalStateException("Invalid session was used for this context " + context);
        }
    }

    @Override
    public void disposeRuntimeEngine(RuntimeEngine runtime) {
        if (isClosed()) {
            throw new IllegalStateException("Runtime manager " + identifier + " is already closed");
        }
        local.remove(getIdentifier());
        try {
            if (canDestroy(runtime)) {
                runtime.getKieSession().destroy();
            } else {
                if (runtime instanceof Disposable) {
                    ((Disposable) runtime).dispose();
                }
            }
        } catch (Exception e) {
            // do nothing
            if (runtime instanceof Disposable) {
                ((Disposable) runtime).dispose();
            }
        }
    }

}
