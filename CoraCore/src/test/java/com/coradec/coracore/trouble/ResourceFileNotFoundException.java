package com.coradec.coracore.trouble;

import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.trouble.BasicException;

/**
 * ​​Indicates an attempt to access a resource file that does not exist.
 */
public class ResourceFileNotFoundException extends BasicException {

    private final String fileName;

    /**
     * Initializes a new instance of ResourceFileNotFoundException regarding the specified resource
     * file.
     *
     * @param fileName the resource file name in question.
     */
    public ResourceFileNotFoundException(final String fileName) {
        this.fileName = fileName;
    }

    @ToString public String getFileName() {
        return this.fileName;
    }
}
