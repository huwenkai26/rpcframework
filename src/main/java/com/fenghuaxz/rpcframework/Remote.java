package com.fenghuaxz.rpcframework;

public interface Remote {

    <T> T oneway(Class<T> cls);

    <T> T call(Class<T> cls);

    default <T> T async(Class<T> cls) {
        return async(cls, AsyncHandler.CLOSE_ON_FAILURE);
    }

    <T> T async(Class<T> cls, AsyncHandler<?> handler);

    enum Type {
        ONEWAY, SYNC, ASYNC
    }
}
