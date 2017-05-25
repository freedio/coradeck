package com.coradec.coracore.annotation;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Target;

/**
 * ​Tags a potentially null object
 */
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, TYPE_USE})
public @interface Nullable {

}
