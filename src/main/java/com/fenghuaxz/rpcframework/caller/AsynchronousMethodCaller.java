package com.fenghuaxz.rpcframework.caller;

import com.fenghuaxz.rpcframework.*;
import com.fenghuaxz.rpcframework.annotations.Timeout;
import com.fenghuaxz.rpcframework.channels.Channel;
import com.fenghuaxz.rpcframework.channels.ChannelFuture;
import io.netty.util.TimerTask;

import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

public class AsynchronousMethodCaller extends AbstractMethodCaller {

    private final AsyncHandler handler;

    @SuppressWarnings("unchecked")
    public AsynchronousMethodCaller(Channel channel, AsyncHandler<?> handler) {
        super(channel);
        if (handler == null) {
            throw new NullPointerException("handler");
        }
        this.handler = new AsyncHandlerWrapper(handler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        WriteTask writeTask = new WriteTask<>(this.mChannel, method, args);
        TimeoutTask timeoutTask = new TimeoutTask(writeTask, handler);
        final Timeout timeout = getTimeout(method);
        timeoutTask.setHold(TimerHolder.newTimeout(timeoutTask, timeout.value(), timeout.unit()));
        writeTask.addListener(timeoutTask);
        writeTask.write(Remote.Type.ASYNC);
        return Context.takeTypeDefaultValue(method.getReturnType());
    }

    static class AsyncHandlerWrapper<V> implements AsyncHandler<V> {

        private final AsyncHandler<V> handler;

        AsyncHandlerWrapper(AsyncHandler<V> handler) {
            if (handler == null) {
                throw new NullPointerException("handler");
            }
            this.handler = handler;
        }

        @Override
        public void completed(ChannelFuture<V> future) {
            future.channel().runTaskWithContext(() -> handler.completed(future));
        }
    }

    static class TimeoutTask implements TimerTask, AsyncHandler<Object> {

        private final WriteTask task;
        private final AsyncHandler handler;
        private volatile io.netty.util.Timeout hold;

        TimeoutTask(WriteTask task, AsyncHandler handler) {
            this.task = task;
            this.handler = handler;
        }

        void setHold(io.netty.util.Timeout hold) {
            this.hold = hold;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void completed(ChannelFuture<Object> future) {
            this.hold.cancel();
            this.handler.completed(future);
        }

        @Override
        public void run(io.netty.util.Timeout timeout) {
            this.task.setFailure(new TimeoutException());
        }
    }
}
