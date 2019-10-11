package com.fenghuaxz.rpcframework.pojo;

import com.fenghuaxz.rpcframework.annotations.Rpc;

public class XRequest {

    private int id;
    private String name;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
    private boolean oneway;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(Rpc rpc, String def) {
        final String value = rpc.value();
        this.name = value.isEmpty() ? def : value;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public boolean isOneway() {
        return this.oneway;
    }

    public void setOneway(boolean oneway) {
        this.oneway = oneway;
    }
}
