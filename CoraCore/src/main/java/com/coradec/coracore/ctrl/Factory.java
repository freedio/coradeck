package com.coradec.coracore.ctrl;

/**
 * API of an object factory.
 *
 * @param <G> the generated object type.
 */
public interface Factory<G> {

    /**
     * Returns an instance of the requested type, supplying the specified arguments for
     * construction or selection.
     *
     * If CoreJet picks a singleton to satisfy the request, an already existing instance will be
     * returned, otherwise a new object will be created.
     *
     * @param args the initialization arguments.
     * @return an instance of G.
     */
    G get(Object... args);

    /**
     * Returns a new instance of the requested type, supplying the specified arguments for
     * construction or selection.
     *
     * This method is the same as {@link #get(Object...)}, but it will always create a new instance,
     * i.e. it will never pick a singleton instance.
     *
     * @param args the initialization arguments.
     * @return an instance of G.
     */
    G create(Object... args);

}
