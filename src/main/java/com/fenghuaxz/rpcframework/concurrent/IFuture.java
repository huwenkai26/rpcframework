package com.fenghuaxz.rpcframework.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface IFuture<V, L extends IFutureListener<? extends IFuture<V, ?>>> extends Future<V> {

    V getNow();

    Throwable cause();

    boolean isCancellable();

    boolean isSuccess();

    IFuture<V, L> await() throws InterruptedException;

    boolean await(long timeoutMillis) throws InterruptedException;

    boolean await(long timeout, TimeUnit timeunit) throws InterruptedException;

    IFuture<V, L> awaitUninterruptibly();

    boolean awaitUninterruptibly(long timeoutMillis);

    boolean awaitUninterruptibly(long timeout, TimeUnit timeunit);

    IFuture<V, L> addListener(L listener);

    IFuture<V, L> removeListener(L listener);
}  