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

package com.coradec.coracore.ctrl;

import com.coradec.coracore.model.Origin;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coracore.util.ExecUtil;

/**
 * ​​A class template providing origins.
 */
public class AutoOrigin {

    /**
     * Returns the current code location as an origin.
     *
     * @return the current code location.
     */
    protected Origin here() {
        return ExecUtil.getStackFrame(1, ClassUtil.nameOf(getClass()));
    }

    /**
     * Returns the caller's code location as an origin.
     *
     * @return the caller's code location.
     */
    protected Origin there() {
        return ExecUtil.getStackFrame(2, ClassUtil.nameOf(getClass()));
    }

    /**
     * Returns the caller's code location as an origin.
     *
     * @param alt the actual class name.
     * @return the caller's code location.
     */
    protected Origin there(Class<?> alt) {
        return ExecUtil.getStackFrame(2, ClassUtil.nameOf(alt));
    }

    /**
     * Returns the caller's caller's code location as an origin.
     *
     * @return the caller's caller's code location.
     */
    protected Origin tthere() {
        return ExecUtil.getStackFrame(3, ClassUtil.nameOf(getClass()));
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

}
