package com.coradec.coracore.model;

import java.io.Serializable;
import java.net.URI;

/**
 * ​A location.
 */
public interface Origin extends Serializable, Representable {

    /**
     * Returns the URI representation of the origin.
     *
     * @return the URI representation of the origin.
     */
    URI toURI();

}
