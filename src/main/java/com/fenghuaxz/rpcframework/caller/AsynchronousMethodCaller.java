package com.fenghuaxz.rpcframework.caller;

import com.fenghuaxz.rpcframework.WriteTask;
import com.fenghuaxz.rpcframework.AsynchronousHandler;
import com.fenghuaxz.rpcframework.Remote;
import com.fenghuaxz.rpcframework.TimerHolder;
import com.fenghuaxz.rpcframework.annotations.Timeout;
import com.fenghuaxz.rpcframework.channels.Channel;
import com.fenghuaxz.rpcframework.channels.ChannelFuture;
import io.netty.util.TimerTask;

import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

public class AsynchronousMethodCaller extends AbstractMethodCaller {

    private final AsynchronousHandler handler;

    @SuppressWarnings("unchecked")
    public AsynchronousMethodCaller(Channel channel, AsynchronousHandler<?> handler) {
        super(channel);
        if (handler == null) {
            throw new NullPointerException("handler");
        }
        this.handler = new AsynchronousHandlerWrapper(handler);
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
        return super.invoke(proxy, method, args);
    }

    static class AsynchronousHandlerWrapper<V> implements AsynchronousHandler<V> {

        private final AsynchronousHandler<V> listener;

        AsynchronousHandlerWrapper(AsynchronousHandler<V> listener) {
            if (listener == null) {
                throw new NullPointerException("handler");
            }
            this.listener = listener;
        }

        @Override
        public void completed(ChannelFuture<V> future) {
            future.channel().runTaskWithContext(() -> listener.completed(future));
        }
    }

    static class TimeoutTask implements TimerTask, AsynchronousHandler<Object> {

        private final WriteTask mMethod;
        private final AsynchronousHandler handler;
        private volatile io.netty.util.Timeout hold;

        TimeoutTask(WriteTask method, AsynchronousHandler handler) {
            this.mMethod = method;
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
            this.mMethod.setFailure(new TimeoutException());
        }
    }
}
