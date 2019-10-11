package com.fenghuaxz.rpcframework;

import com.fenghuaxz.rpcframework.channels.Channel;
import com.fenghuaxz.rpcframework.channels.NioSocketChannel;
import com.fenghuaxz.rpcframework.codec.ByteToMessageCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class RPCClient extends Context implements Remote {

    private volatile Channel mChannel;
    private final EventExecutorGroup eventExecutors;
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final Bootstrap b = new Bootstrap();

    public RPCClient() {
        this(new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2));
    }

    public RPCClient(EventExecutorGroup eventExecutors) {
        if (eventExecutors == null) {
            throw new NullPointerException("eventExecutors");
        }
        this.eventExecutors = eventExecutors;
        this.b.group(workerGroup);
        this.b.channelFactory(() -> new NioSocketChannel(this));
        option(ChannelOption.TCP_NODELAY, true);
        option(ChannelOption.ALLOCATOR, ByteBufAllocator.DEFAULT);
        this.b.handler(new ChannelInitializer<io.netty.channel.Channel>() {
            @Override
            protected void initChannel(io.netty.channel.Channel ch) {
                final ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new ByteToMessageCodec());
                pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                pipeline.addLast(eventExecutors, Events.instance);
            }
        });
    }

    public <T> RPCClient option(ChannelOption<T> option, T value) {
        this.b.option(option, value);
        return this;
    }

    public RPCClient setAddress(InetSocketAddress address) {
        this.b.remoteAddress(address);
        return this;
    }

    public RPCClient connect() throws Exception {
        if (isConnected()) this.mChannel.close();
        final Thread thread = Thread.currentThread();
        final AtomicReference<Exception> err = new AtomicReference<>();
        new Thread(getClass().getSimpleName() + " - " + b.config().remoteAddress()) {
            @Override
            public void run() {
                try {
                    final ChannelFuture future = b.connect().sync();
                    mChannel = (Channel) future.channel();
                    LockSupport.unpark(thread);
                    future.channel().closeFuture().sync();
                } catch (Exception e) {
                    err.set(e);
                    LockSupport.unpark(thread);
                }
            }
        }.start();
        LockSupport.park();
        Exception cause;
        if ((cause = err.get()) != null) throw cause;
        return this;
    }

    public boolean isConnected() {
        return mChannel != null && mChannel.isActive();
    }

    @Override
    public void dispose() {
        this.workerGroup.shutdownGracefully();
        this.eventExecutors.shutdownGracefully();
    }

    @Override
    public <T> T oneway(Class<T> cls) {
        if (!isConnected()) {
            throw new IllegalStateException("closed");
        }
        return this.mChannel.oneway(cls);
    }

    @Override
    public <T> T call(Class<T> cls) {
        if (!isConnected()) {
            throw new IllegalStateException("closed");
        }
        return this.mChannel.call(cls);
    }

    @Override
    public <T> T async(Class<T> cls, AsynchronousHandler<?> handler) {
        if (!isConnected()) {
            throw new IllegalStateException("closed");
        }
        return this.mChannel.async(cls, handler);
    }
}
