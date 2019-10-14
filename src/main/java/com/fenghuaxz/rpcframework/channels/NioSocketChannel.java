package com.fenghuaxz.rpcframework.channels;

import com.fenghuaxz.rpcframework.Context;
import com.fenghuaxz.rpcframework.Template;
import com.fenghuaxz.rpcframework.AsyncHandler;
import com.fenghuaxz.rpcframework.caller.AsynchronousMethodCaller;
import com.fenghuaxz.rpcframework.caller.OnewayMethodCaller;
import com.fenghuaxz.rpcframework.caller.SynchronousMethodCaller;
import com.fenghuaxz.rpcframework.codec.ByteToMessageCodec;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;

import java.lang.reflect.Proxy;
import java.nio.channels.SocketChannel;

public class NioSocketChannel extends io.netty.channel.socket.nio.NioSocketChannel
        implements Channel, Channel.CloseIntercept {

    private final Context mContext;

    public NioSocketChannel(Context context) {
        this.mContext = context;
    }

    NioSocketChannel(io.netty.channel.Channel parent, SocketChannel socket, Context context) {
        super(parent, socket);
        this.mContext = context;
    }

    @Override
    public Context context() {
        return this.mContext;
    }

    @Override
    public ChannelFuture send(Object msg) {
        return writeAndFlush(msg);
    }

    @Override
    public <T extends ByteToMessageCodec> void setCodec(T codec) {
        if (codec == null) {
            throw new NullPointerException("codec");
        }
        pipeline().replace(ByteToMessageCodec.class, "MESSAGE_CODEC", codec);
    }

    @Override
    public void runTaskWithContext(Runnable task) {
        this.mContext.runTask(task);
    }

    @Override
    public <T extends Template> T template(Class<T> cls) {
        return this.mContext.getTemplate(cls);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T oneway(Class<T> cls) {
        return (T) Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}
                , new OnewayMethodCaller(this));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Class<T> cls) {
        return (T) Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}
                , new SynchronousMethodCaller(this));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T async(Class<T> cls, AsyncHandler<?> handler) {
        return (T) Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}
                , new AsynchronousMethodCaller(this, handler));
    }

    private volatile boolean intercept;
    private volatile ChannelPromise closePromise;

    @Override
    public void setIntercept(boolean intercept) {
        this.intercept = intercept;
    }

    @Override
    public ChannelPromise closePromise() {
        return this.closePromise;
    }

    @Override
    public ChannelFuture close() {
        if (intercept) {
            this.closePromise = newProgressivePromise();
            return closePromise;
        }
        return super.close();
    }
}