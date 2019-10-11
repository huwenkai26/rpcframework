package com.fenghuaxz.rpcframework;

import com.fenghuaxz.rpcframework.channels.Channel;

public abstract class Template {

    protected void doActive(Channel channel) {
    }

    protected void doInactive(Channel channel) {
    }

    protected void doCaught(Channel channel, Throwable cause) {
    }
}
