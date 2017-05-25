package com.coradec.coraconf.trouble;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;

import java.net.URL;

/**
 * ​​Base class of all exceptions related to resources.
 */
public class ResourceFailure extends ConfigurationException {

    private final @Nullable String resourceName;
    private final @Nullable URL resource;

    /**
     * Initializes a new instance of ResourceFailure for the resource with the specified name.
     *
     * @param resourceName the resource name.
     */
    public ResourceFailure(final String resourceName) {
        this.resource = null;
        this.resourceName = resourceName;
    }

    /**
     * Initializes a new instance of ResourceFailure for the specified resource with the specified
     * underlying problem and explanation.
     *
     * @param resource    the resource.
     * @param explanation the explanation.
     * @param problem     the underlying problem.
     */
    public ResourceFailure(final URL resource, final String explanation, final Throwable problem) {
        super(explanation, problem);
        this.resource = resource;
        this.resourceName = resource.toExternalForm();
    }

    @ToString public @Nullable String getResourceName() {
        String result = this.resourceName;
        if (resource != null) result = resource.toExternalForm();
        return result;
    }

}
