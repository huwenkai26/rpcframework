package com.fenghuaxz.rpcframework;

import com.fenghuaxz.rpcframework.annotations.Nullable;
import com.fenghuaxz.rpcframework.annotations.Rpc;
import io.netty.util.internal.ConcurrentSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public abstract class Context {

    private final Map<String, Service> mServices = new ConcurrentHashMap<>();
    private final Map<Class<? extends Template>, Template> mTemplates = new ConcurrentHashMap<>();
    private final Set<Hook> mHooks = new ConcurrentSet<>();
    private volatile Executor mExecutor = Runnable::run;


    Context() {
        register(new Nullable.EmptyParameterValidator());
    }

    public abstract void dispose();

    Service getService(String name) {
        Service service;
        if ((service = this.mServices.get(name)) == null) {
            throw new IllegalStateException("No service added: " + name);
        }
        return service;
    }

    public <T extends Service> void register(T... services) {
        if (services == null) {
            throw new NullPointerException("services");
        }

        for (Service service : services) {
            for (Template template : service.newTemplates()) {
                this.mTemplates.put(template.getClass(), template);
            }

            for (Class<?> cls : service.getClass().getInterfaces()) {
                Rpc rpc;
                if ((rpc = cls.getAnnotation(Rpc.class)) == null) {
                    throw new IllegalStateException("Must @Rpc in: " + cls.getName());
                }
                String name = rpc.value();
                this.mServices.put(name.isEmpty() ? cls.getName() : name, service);
            }
        }
    }

    public <T extends Hook> void register(T... hooks) {
        if (hooks == null) {
            throw new NullPointerException("hooks");
        }
        this.mHooks.addAll(Arrays.asList(hooks));
    }

    @SuppressWarnings("unchecked")
    public <T extends Template> T getTemplate(Class<T> cls) {
        T handler;
        if ((handler = (T) this.mTemplates.get(cls)) == null) {
            throw new IllegalStateException("No template added: " + cls.getName());
        }
        return handler;
    }

    Collection<Template> templates() {
        return this.mTemplates.values();
    }

    Collection<Hook> hooks() {
        return this.mHooks;
    }

    public <T extends Executor> void setExecutor(T executor) {
        if (executor == null) {
            throw new NullPointerException("executor");
        }
        this.mExecutor = executor;
    }

    public void runTask(Runnable task) {
        this.mExecutor.execute(task);
    }

    public static Object takeTypeDefaultValue(Class<?> type) {
        if (type == null || !type.isPrimitive()) {
            return null;
        }

        switch (type.getName()) {
            case "long":
                return (long) 0;
            case "double":
                return (double) 0;
            case "int":
                return 0;
            case "float":
                return (float) 0;
            case "boolean":
                return false;
            case "short":
                return (short) 0;
            case "char":
                return (char) 0;
            case "byte":
                return (byte) 0;
            default:
                return null;
        }
    }
}
