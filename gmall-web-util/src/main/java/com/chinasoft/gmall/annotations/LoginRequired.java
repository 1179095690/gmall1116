package com.chinasoft.gmall.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


//使用范围（只在方法内使用）
@Target(ElementType.METHOD)
//生效范围（）
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
    boolean loginSuccess() default true;
}
