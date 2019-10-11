package com.fenghuaxz.rpcframework.concurrent;

public interface IFutureListener<F extends IFuture> {

    void completed(F future);
}
