package com.hnu.ict.ids.utils;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) // 表示注解在运行时依然存在
@Target(ElementType.METHOD)
@Documented
public @interface ParamsNotNull {
    public String str() default "";
}
