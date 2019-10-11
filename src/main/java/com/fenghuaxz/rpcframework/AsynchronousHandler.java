package com.fenghuaxz.rpcframework;

import com.fenghuaxz.rpcframework.channels.ChannelFuture;
import com.fenghuaxz.rpcframework.concurrent.IFutureListener;

public interface AsynchronousHandler<V> extends IFutureListener<ChannelFuture<V>> {

    AsynchronousHandler<?> CLOSE_ON_FAILURE = (AsynchronousHandler<Object>) future -> {
        if (!future.isSuccess()) {
            future.channel().close();
        }
    };

    AsynchronousHandler<?> DO_NOTHING = (AsynchronousHandler<Object>) future -> {
    };
}
