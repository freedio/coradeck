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
}
