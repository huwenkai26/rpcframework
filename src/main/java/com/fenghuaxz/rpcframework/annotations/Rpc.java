package com.fenghuaxz.rpcframework.annotations;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Rpc {

    String value() default "";
}
