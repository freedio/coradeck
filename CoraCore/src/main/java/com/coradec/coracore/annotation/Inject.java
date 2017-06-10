/*
 * Copyright ⓒ 2017 by Coradec GmbH.
 *
 * This file is part of the Coradeck.
 *
 * Coradeck is free software: you can redistribute it under the the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License,
 * or any later version.
 *
 * Coradeck is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR ANY PARTICULAR PURPOSE.  See the
 * GNU General Public License for further details.
 *
 * The GNU General Public License is available from <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 * @author Dominik Wezel <dom@coradec.com>
 *
 */

package com.coradec.coracore.annotation;

import static com.coradec.coracore.model.InjectionMode.*;
import static java.lang.annotation.ElementType.*;

import com.coradec.coracore.model.InjectionMode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ​Defines an injection point (if applied to a field or parameter) or marks a class as needing
 * special treatment because it has injection points.
 * <p>
 * If a class has injection points, it should be annotated with @Inject.  The CAR class loader and
 * its injector will automatically detect fields annotated with @Inject in all loaded classes, but
 * injections need a constructor modification, which will not take place if the class itself is not
 * annotated with @Inject and the first constructor appears BEFORE the first injection point.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, PARAMETER, TYPE})
public @interface Inject {

    InjectionMode value() default DIRECT;
}
