package com.coradec.coraconf.trouble;

import java.net.URL;

/**
 * ​​Indicates a failure to instantiate a resource.
 */
public class ResourceInstantiationFailure extends ResourceFailure {

    public ResourceInstantiationFailure(final String resourceName) {
        super(resourceName);
    }

    public ResourceInstantiationFailure(final URL resource, final String explanation,
                                        final Throwable problem) {
        super(resource, explanation, problem);
    }
}
