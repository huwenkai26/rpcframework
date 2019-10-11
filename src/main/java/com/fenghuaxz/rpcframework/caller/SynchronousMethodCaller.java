package com.fenghuaxz.rpcframework.caller;

import com.fenghuaxz.rpcframework.WriteTask;
import com.fenghuaxz.rpcframework.Remote;
import com.fenghuaxz.rpcframework.annotations.Timeout;
import com.fenghuaxz.rpcframework.channels.Channel;

import java.lang.reflect.Method;

public class SynchronousMethodCaller extends AbstractMethodCaller {

    public SynchronousMethodCaller(Channel channel) {
        super(channel);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        WriteTask writeTask = new WriteTask(this.mChannel, method, args);
        writeTask.write(Remote.Type.SYNC);
        Timeout timeout = getTimeout(method);
        return writeTask.get(timeout.value(), timeout.unit());
    }
}
