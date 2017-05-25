package com.coradec.corajet.test;

import static com.coradec.coracore.model.Scope.*;

import com.coradec.coracore.annotation.Inject;

/**
 * ​​Class to be remodeled
 */
@com.coradec.coracore.annotation.Implementation(SINGLETON)
public class ToRemodel extends Exception {

    @Inject private static int staticInjectAssigned = 4711;
    @Inject private static Interface staticInjectUnassigned;
    private static int staticAssigned = 42;
    private static int staticUnAssigned;

    @Inject private String injectAssigned = "xxx";
    @Inject private String injectUnassigned;
    private int assigned = 42;
    private int unAssigned;

    public ToRemodel(final int x, final byte[] y, boolean z) {
        super("boo!");
        System.out.printf("Injected %s.%n", staticInjectUnassigned);
    }

    protected ToRemodel(final int i) {
        this(i, new byte[0], false);
    }

}
