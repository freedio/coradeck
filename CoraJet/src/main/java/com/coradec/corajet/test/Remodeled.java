package com.coradec.corajet.test;

import com.coradec.coracore.annotation.Inject;
import com.coradec.corajet.ctrl.Injector;

/**
 * ​​Class to be remodeled
 */
public class Remodeled extends Exception {

    @Inject
    private static Interface staticInjectUnassigned = (Interface)Injector.implement(Interface.class);

    public Remodeled(int x, byte[] y, boolean z) {
        super("boo!");
        com.coradec.corajet.ctrl.Injector.finish(this);
        System.out.printf("Injected %s.%n", new Object[]{staticInjectUnassigned});
    }

    protected Remodeled(int i) {
        this(i, new byte[0], false);
    }

}
