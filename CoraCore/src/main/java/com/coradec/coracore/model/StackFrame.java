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

package com.coradec.coracore.model;

/**
 * An origin based on a stack trace element.
 */
public interface StackFrame extends Origin {

    /**
     * Returns the class name.
     *
     * @return the class name.
     */
    String getClassName();

    /**
     * Returns the file name of the class relative to the class path (i.e. the class file resource
     * name).  This is basically the class name with dots replaced by the path separator character,
     *
     * @return the class file name.
     */
    String getClassFileName();

    /**
     * Returns the method name.
     *
     * @return the method name.
     */
    String getMethodName();

    /**
     * Returns the name of the file containing the code location.
     *
     * @return the name of the source file.
     */
    String getFileName();

    /**
     * Returns the line number of the code.
     *
     * @return the source line number.
     */
    int getLineNumber();
}
