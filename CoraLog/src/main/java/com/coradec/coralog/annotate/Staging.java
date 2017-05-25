package com.coradec.coralog.annotate;

import com.coradec.coralog.model.LogLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ​Marks a class as staging level.
 *
 * This will cause the log level of this class to be {@link LogLevel#INFORMATION}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface Staging {

}
