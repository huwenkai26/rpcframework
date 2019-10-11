package com.fenghuaxz.rpcframework.pojo;

public class XResponse {

    private int id;
    private Throwable cause;
    private Object result;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        cause.getStackTrace();
        this.cause = cause;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
