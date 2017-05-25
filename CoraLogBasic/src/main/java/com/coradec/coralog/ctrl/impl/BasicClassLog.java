package com.coradec.coralog.ctrl.impl;

import static com.coradec.coralog.model.LogLevel.*;

import com.coradec.coracore.annotation.Component;
import com.coradec.coralog.annotate.Production;
import com.coradec.coralog.annotate.Staging;
import com.coradec.coralog.ctrl.ClassLog;
import com.coradec.coralog.model.LogLevel;

/**
 * ​​Basic implementation of a class log.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Component
public class BasicClassLog extends BasicLog implements ClassLog {

    private static LogLevel getInitialLevel(Class<?> klass) {
        if (klass.isAnnotationPresent(Production.class)) return ALERT;
        if (klass.isAnnotationPresent(Staging.class)) return INFORMATION;
        return ALL;
    }

    @SuppressWarnings("FieldCanBeLocal") private final Class<?> klass;

    public BasicClassLog(Class<?> klass) {
        super(getInitialLevel(klass));
        this.klass = klass;
    }

}
