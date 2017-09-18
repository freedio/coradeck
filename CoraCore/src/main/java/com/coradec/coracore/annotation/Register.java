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

import static java.lang.annotation.ElementType.*;

import com.coradec.coracore.model.Registry;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers the annotated class with the registry specified in the annotation parameter.  The
 * annotated class will be added to the specified registry upon loading ONLY IF it is also annotated
 * as {@link Implementation}.
 * <p>
 * The typical use case for this annotation are components of a hierarchy acting as implementations
 * for a hierarchy of interfaces in a parallel universe.  These implementation classes will be found
 * and initialized by the CarClassLoader due to their @Implementation annotation, but would be very
 * hard to locate and address unless being registered in a common registry.
 * <p>
 * The annotation has been introduced to create GUI implementations implementing a particular type
 * of gadget.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface Register {

    Class<? extends Registry> value();

}
