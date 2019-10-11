package com.fenghuaxz.rpcframework.annotations;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Timeout {

    long value();

    TimeUnit unit() default TimeUnit.SECONDS;
}
