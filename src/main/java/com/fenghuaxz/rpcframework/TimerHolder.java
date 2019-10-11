package com.fenghuaxz.rpcframework;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

public final class TimerHolder {

    private static volatile HashedWheelTimer defaultTimer;

    public static void setTimer(HashedWheelTimer timer) {
        if (TimerHolder.defaultTimer != null) TimerHolder.defaultTimer.stop();
        TimerHolder.defaultTimer = timer;
    }

    public static Timeout newTimeout(TimerTask task, long delay, TimeUnit unit) {
        if (defaultTimer == null) {
            defaultTimer = new HashedWheelTimer();
        }
        return TimerHolder.defaultTimer.newTimeout(task, delay, unit);
    }

}
