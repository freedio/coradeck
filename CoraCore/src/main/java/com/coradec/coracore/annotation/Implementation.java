package com.coradec.coracore.annotation;

import static com.coradec.coracore.model.Scope.*;

import com.coradec.coracore.model.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ​Marks a class as an implementation of all its superclasses and interfaces it implements.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Implementation {
    Scope value() default TEMPLATE;
}
