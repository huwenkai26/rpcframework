package com.fenghuaxz.rpcframework.channels;

import com.fenghuaxz.rpcframework.Context;
import io.netty.channel.ChannelException;
import io.netty.util.internal.SocketUtils;

import java.nio.channels.SocketChannel;
import java.util.List;

public class NioServerSocketChannel extends io.netty.channel.socket.nio.NioServerSocketChannel {

    private final Context mContext;

    public NioServerSocketChannel(Context context) {
        this.mContext = context;
    }

    @Override
    protected int doReadMessages(List<Object> buf) throws Exception {
        SocketChannel ch = SocketUtils.accept(javaChannel());

        try {
            if (ch != null) {
                buf.add(new NioSocketChannel(this, ch, this.mContext));
                return 1;
            }
        } catch (Throwable t) {
            ch.close();
            throw new ChannelException("Failed to create a new channel from an accepted socket.", t);
        }
        return 0;
    }
}
