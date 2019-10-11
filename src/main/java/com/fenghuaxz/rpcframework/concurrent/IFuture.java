package com.fenghuaxz.rpcframework.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface IFuture<V> extends Future<V> {

    V getNow();

    Throwable cause();

    boolean isCancellable();

    boolean isSuccess();

    IFuture<V> await() throws InterruptedException;

    boolean await(long timeoutMillis) throws InterruptedException;

    boolean await(long timeout, TimeUnit timeunit) throws InterruptedException;

    IFuture<V> awaitUninterruptibly();

    boolean awaitUninterruptibly(long timeoutMillis);

    boolean awaitUninterruptibly(long timeout, TimeUnit timeunit);
}  