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
