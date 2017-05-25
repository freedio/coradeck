package com.coradec.corecore.util;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.trouble.ResourceFileNotFoundException;
import com.coradec.coracore.util.ClassUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class ClassUtilTest {

    @Test public void testToStringWithoutAttributes() {
        Object testee = new EmptyObject();
        final String s = testee.getClass().getName().replace('$', '.');

        final String result = ClassUtil.toString(testee);

        assertThat(result, is("(" + s + ")"));
    }

    @Test public void testToStringWithoutAnnotatedAttributes() {
        Object testee = new UnannotatedObject();
        final String s = testee.getClass().getName().replace('$', '.');

        final String result = ClassUtil.toString(testee);

        assertThat(result, is("(" + s + ")"));
    }

    @Test public void testToStringWithAnnotatedAttributes() {
        final Object testee = new AnnotatedObject();
        final String s = testee.getClass().getName().replace('$', '.');

        final String result = ClassUtil.toString(testee);

        assertThat(result, is("(" + s + " (TheAnswer: Integer 42))"));
    }

    @Test public void testToStringWithForeignClasses() {
        Map<String, Date> rmap = new HashMap<>();
        rmap.put("died", new Date(1234567890123L));
        rmap.put("born", new Date(234567890123L));
        rmap.put("operated", new Date(345678901234L));

        assertThat(ClassUtil.toString("abc"), is("(String \"abc\")"));
        assertThat(ClassUtil.toString(42), is("(Integer 42)"));
        assertThat(ClassUtil.toString(new Date(1234567890123L)),
                is("(Date 2009-02-14T00:31:30.123000000)"));
        assertThat(ClassUtil.toString(123.456), is("(Double 123.456)"));
        assertThat(ClassUtil.toString(new StringBuffer()), is("(StringBuffer \"\")"));
        assertThat(ClassUtil.toString(
                new RuntimeException("Something went bad", new NoSuchElementException())),
                is("(RuntimeException java.lang.RuntimeException: Something went bad)"));
        assertThat(ClassUtil.toString(new ResourceFileNotFoundException("a.context")),
                is("(com.coradec.coracore.trouble.ResourceFileNotFoundException com.coradec" +
                   ".coracore.trouble.ResourceFileNotFoundException: (FileName: \"a.context\"))" +
                   ""));
        assertThat(ClassUtil.toString(Boolean.TRUE), is("(Boolean true)"));
        assertThat(ClassUtil.toString('ö'), is("(Character '\\u00f6')"));
        assertThat(ClassUtil.toString("abc.def".split("\\.")), is("(String[\"abc\", \"def\"])"));
        assertThat(ClassUtil.toString(new byte[] {0, 1, 2}), is("(byte[0, 1, 2])"));
        assertThat(ClassUtil.toString(new char[] {'a', '\n', '\20'}),
                is("(char['a', '\\n', '\\20'])"));
        assertThat(ClassUtil.toString(Arrays.asList("x", "y", "z")),
                is("(com.google.common.collect.RegularImmutableList [\"x\", \"y\", \"z\"])"));
        assertThat(ClassUtil.toString(rmap),
                is("(com.google.common.collect.RegularImmutableMap {\"died\": " +
                   "2009-02-14T00:31:30.123000000, \"born\": 1977-06-07T22:44:50.123000000, " +
                   "\"operated\": 1980-12-14T22:55:01.234000000})"));
    }

    private class EmptyObject {

        @Override public String toString() {
            return ClassUtil.toString(this);
        }

    }

    private class UnannotatedObject {

        public String getInvalid() {
            return "This should not be shown";
        }

        @Override public String toString() {
            return ClassUtil.toString(this);
        }

    }

    private class AnnotatedObject {

        public String getInvalid() {
            return "This should not be shown";
        }

        @ToString public int getTheAnswer() {
            return 42;
        }

        @Override public String toString() {
            return ClassUtil.toString(this);
        }

    }

}
