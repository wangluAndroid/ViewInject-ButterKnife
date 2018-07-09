package com.example;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by serenitynanian on 2018/7/9.
 * 绑定监听事件
 */

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface OnClick {
    int value();
}
