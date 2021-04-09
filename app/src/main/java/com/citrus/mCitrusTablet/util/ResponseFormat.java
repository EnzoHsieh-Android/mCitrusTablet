package com.citrus.mCitrusTablet.util;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(METHOD)
@Retention(RUNTIME)
public @interface ResponseFormat {

    String JSON = "json";

    String XML = "xml";

    String value() default "";
}
