package com.fenghuaxz.rpcframework;

import com.fenghuaxz.rpcframework.annotations.Rpc;
import com.fenghuaxz.rpcframework.channels.Channel;
import com.fenghuaxz.rpcframework.channels.ChannelFuture;
import com.fenghuaxz.rpcframework.concurrent.IFutureBase;
import com.fenghuaxz.rpcframework.pojo.XRequest;
import com.fenghuaxz.rpcframework.pojo.XResponse;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.fenghuaxz.rpcframework.Hook.AbstractMethodCallHook.doCall;
import static com.fenghuaxz.rpcframework.Hook.AbstractMethodCallHook.doCompleted;

public final class WriteTask<V> extends IFutureBase<V, AsynchronousHandler<V>> implements ChannelFuture<V> {

    private static final AtomicInteger idCounter = new AtomicInteger();
    private static final Map<Channel, Map<Integer, WriteTask>> infos = new ConcurrentHashMap<>();

    private final io.netty.channel.ChannelFutureListener SEND_FAILURE_HANDLER = future -> {
        if (!future.isSuccess()) {
            setFailure(future.cause());
        }
    };

    private final Channel mChannel;
    private final Method mMethod;
    private final Object[] mParameters;

    public WriteTask(Channel channel, Method method, Object[] parameters) {
        this.mChannel = channel;
        this.mMethod = method;
        this.mParameters = parameters;
    }

    @Override
    public Channel channel() {
        return this.mChannel;
    }

    public void write(Remote.Type type) {
        final Rpc rpc;
        final Class<?> cls = this.mMethod.getDeclaringClass();
        if ((rpc = cls.getAnnotation(Rpc.class)) == null) {
            throw new IllegalStateException("Must @Rpc in " + cls.getName());
        }
        final XRequest req = new XRequest();
        req.setId(idCounter.incrementAndGet());
        req.setName(rpc, cls.getName());
        req.setMethodName(this.mMethod.getName());
        req.setParameterTypes(this.mMethod.getParameterTypes());
        req.setParameters(this.mParameters);
        req.setOneway(type == Remote.Type.ONEWAY);

        doCall(this.mMethod, this.mParameters, this.mChannel, type);

        if (!req.isOneway()) {
            Map<Integer, WriteTask> infoMap;
            if ((infoMap = infos.get(mChannel)) == null) {
                infos.put(mChannel, infoMap = new ConcurrentHashMap<>());
            }
            infoMap.put(req.getId(), this);
        }
        this.mChannel.send(req).addListener(SEND_FAILURE_HANDLER);
    }

    @Override
    public void setFailure(Throwable cause) {
        super.setFailure(cause);
    }

    static void doInactive(Channel channel) {
        infos.remove(channel);
    }

    static void doResponse(Channel channel, XResponse response) {
        final Map<Integer, WriteTask> map;
        if ((map = infos.get(channel)) != null) {
            final WriteTask task;
            if ((task = map.remove(response.getId())) != null) {
                doCompleted(task.mMethod, channel, response.getResult(), response.getCause());
                final Throwable cause = response.getCause();
                if (cause != null)
                    task.setFailure(cause);
                else {
                    task.setSuccess(response.getResult());
                }
            }
        }
    }
}
