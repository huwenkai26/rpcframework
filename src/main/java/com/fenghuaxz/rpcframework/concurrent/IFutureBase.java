package com.fenghuaxz.rpcframework.concurrent;

import java.util.Collection;
import java.util.concurrent.*;

public abstract class IFutureBase<V, L extends IFutureListener> implements IFuture<V> {

    private volatile Object result;
    private Collection<L> listeners;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (isDone()) {
            return false;
        }

        synchronized (this) {
            if (isDone()) {
                return false;
            }
            result = new CauseHolder(new CancellationException());
            notifyAll();
        }
        notifyListeners();
        return true;
    }

    @Override
    public boolean isCancellable() {
        return result == null;
    }

    @Override
    public boolean isCancelled() {
        return result != null && result instanceof CauseHolder && ((CauseHolder) result).cause instanceof CancellationException;
    }

    @Override
    public boolean isDone() {
        return result != null;
    }

    private V get0() throws ExecutionException {
        Throwable cause = cause();
        if (cause == null) {
            return getNow();
        }
        if (cause instanceof CancellationException) {
            throw (CancellationException) cause;
        }
        throw new ExecutionException(cause);
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        await();
        return get0();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (await(timeout, unit)) {
            return get0();
        }
        throw new TimeoutException();
    }

    @Override
    public boolean isSuccess() {
        return result != null && !(result instanceof CauseHolder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getNow() {
        return result == SuccessSignal.instance ? null : (V) result;
    }

    @Override
    public Throwable cause() {
        if (result != null && result instanceof CauseHolder) {
            return ((CauseHolder) result).cause;
        }
        return null;
    }


    public IFuture<V> addListener(L listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        if (isDone()) {
            notifyListener(listener);
            return this;
        }
        synchronized (this) {
            if (!isDone()) {
                if (listeners == null) {
                    listeners = new CopyOnWriteArrayList<>();
                }
                listeners.add(listener);
                return this;
            }
        }
        notifyListener(listener);
        return this;
    }


    public IFuture<V> removeListener(L listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }

        if (!isDone() && listeners != null) {
            listeners.remove(listener);
        }
        return this;
    }

    @Override
    public IFuture<V> await() throws InterruptedException {
        return await0(true);
    }


    private IFuture<V> await0(boolean interruptable) throws InterruptedException {
        if (!isDone()) {

            if (interruptable && Thread.interrupted()) {
                throw new InterruptedException("thread has been interrupted.");
            }

            boolean interrupted = false;
            synchronized (this) {
                while (!isDone()) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        if (interruptable) {
                            throw e;
                        } else {
                            interrupted = true;
                        }
                    }
                }
            }
            if (interrupted) {
                final Thread thread = Thread.currentThread();
                thread.interrupt();
            }
        }
        return this;
    }

    @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
        return await0(TimeUnit.MILLISECONDS.toNanos(timeoutMillis), true);
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return await0(unit.toNanos(timeout), true);
    }

    private boolean await0(long timeoutNanos, boolean interruptible) throws InterruptedException {
        if (isDone()) {
            return true;
        }

        if (timeoutNanos <= 0) {
            return false;
        }

        if (interruptible && Thread.interrupted()) {
            throw new InterruptedException(toString());
        }

        long startTime = System.nanoTime();
        long waitTime = timeoutNanos;
        boolean interrupted = false;

        try {
            synchronized (this) {
                if (isDone()) {
                    return true;
                }

                for (; ; ) {
                    try {
                        wait(waitTime / 1000000, (int) (waitTime % 1000000));
                    } catch (InterruptedException e) {
                        if (interruptible) {
                            throw e;
                        } else {
                            interrupted = true;
                        }
                    }

                    if (isDone()) {
                        return true;
                    } else {
                        waitTime = timeoutNanos - (System.nanoTime() - startTime);
                        if (waitTime <= 0) {
                            return isDone();
                        }
                    }
                }
            }
        } finally {
            if (interrupted) {
                final Thread thread = Thread.currentThread();
                thread.interrupt();
            }
        }
    }

    @Override
    public IFuture<V> awaitUninterruptibly() {
        try {
            return await0(false);
        } catch (InterruptedException e) {
            throw new InternalError();
        }
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        try {
            return await0(TimeUnit.MILLISECONDS.toNanos(timeoutMillis), false);
        } catch (InterruptedException e) {
            throw new InternalError();
        }
    }

    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        try {
            return await0(unit.toNanos(timeout), false);
        } catch (InterruptedException e) {
            throw new InternalError();
        }
    }

    protected void setFailure(Throwable cause) {
        if (setFailure0(cause)) {
            notifyListeners();
        }
    }

    private boolean setFailure0(Throwable cause) {
        if (isDone()) {
            return false;
        }

        synchronized (this) {
            if (isDone()) {
                return false;
            }
            result = new CauseHolder(cause);
            notifyAll();
        }

        return true;
    }

    protected void setSuccess(Object result) {
        if (setSuccess0(result)) {
            notifyListeners();
        }
    }

    private boolean setSuccess0(Object result) {
        if (isDone()) {
            return false;
        }

        synchronized (this) {
            if (isDone()) {
                return false;
            }
            if (result == null) {
                this.result = SuccessSignal.instance;
            } else {
                this.result = result;
            }
            notifyAll();
        }
        return true;
    }

    private void notifyListeners() {
        if (listeners != null) {
            for (L listener : listeners) {
                notifyListener(listener);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void notifyListener(L listener) {
        try {
            listener.completed(this);
        } catch (Exception ignored) {
        }
    }

    private static class SuccessSignal {

        static final SuccessSignal instance = new SuccessSignal();
    }

    private static final class CauseHolder {
        final Throwable cause;

        CauseHolder(Throwable cause) {
            this.cause = cause;
        }
    }
}  