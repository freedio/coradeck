package com.coradec.coracore.model;

/**
 * ​Representation of a state.
 */
public interface State {

    /**
     * Returns the name of the state.
     *
     * @return the name of the state.
     */
    String name();

    /**
     * Returns the ordinal number of the state.
     *
     * @return the ordinal number.
     */
    int ordinal();

    /**
     * Checks if this state precedes the specified state.
     *
     * @param state the state to check against.
     * @return {@code true} if this state precedes the specified state.
     */
    default boolean precedes(final State state) {
        return ordinal() < state.ordinal();
    }

}
