package com.erbaijiu.common.proxy;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author pengpan
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Proxy {

    /**
     * 是否启用代理
     */
    boolean enable() default true;
}
