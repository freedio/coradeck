package com.coradec.coracore.trouble;

import com.coradec.coracore.annotation.ToString;

/**
 * ​​Base class of all resource file problems.
 */
public class ResourceFileException extends BasicException {

    private final String fileName;

    /**
     * Initializzes a new instance of ResourceFileException with the specified resource file name.
     *
     * @param fileName the resource file name.
     */
    public ResourceFileException(final String fileName) {
        this.fileName = fileName;
    }

    @ToString public String getFileName() {
        return this.fileName;
    }

}
