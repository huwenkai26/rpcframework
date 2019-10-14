package com.fenghuaxz.rpcframework.channels;

import com.fenghuaxz.rpcframework.AsyncHandler;
import com.fenghuaxz.rpcframework.concurrent.IFuture;

public interface ChannelFuture<V> extends IFuture<V, AsyncHandler<V>> {

    Channel channel();
}
