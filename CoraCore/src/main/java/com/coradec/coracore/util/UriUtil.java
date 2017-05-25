package com.coradec.coracore.util;

import java.net.URI;

/**
 * ​​Static library of URI related utilities.
 */
public final class UriUtil {

    private UriUtil() {
    }

    /**
     * Composes an identifier URI for the specified scheme and ID.
     *
     * @param scheme  the URI scheme.
     * @param id      the ID.
     * @return an identifier based URI.
     */
    public static URI composeIdURI(final String scheme, final Object id) {
        return URI.create(scheme + ":#" + id); // TODO: fake, make real
    }

}
