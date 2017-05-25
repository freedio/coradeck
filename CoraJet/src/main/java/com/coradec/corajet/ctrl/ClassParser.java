package com.coradec.corajet.ctrl;

import com.coradec.coracore.annotation.Component;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Representable;
import com.coradec.coracore.util.ClassUtil;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ​​Implementation of a class visitor.
 */
public class ClassParser extends ClassVisitor {

    private static final String IMPLEMENTATION_SIGNATURE =
            ClassUtil.signatureOf(Implementation.class);
    private static final String COMPONENT_SIGNATURE = ClassUtil.signatureOf(Component.class);
    private static final String INJECT_SIGNATURE = ClassUtil.signatureOf(Inject.class);

    private String className;
    private FieldDef currentField;
    private boolean implementation;
    private boolean component;
    private Set<FieldDef> injected = new HashSet<>();
    private Set<String> implementations = new HashSet<>();
    private Set<String> components = new HashSet<>();
    private Map<String, Set<FieldDef>> injections = new HashMap<>();

    public ClassParser() {
        super(Opcodes.ASM5);
    }

    @Override public void visit(final int version, final int access, final String className,
                                final String signature, final String superClassName,
                                final String[] interfaces) {
//        System.out.printf("%nClass %s (%s) v. %d {%n", className, signature, version);
        this.className = className;
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
//        System.out.printf("- Annotation %s is %svisible%n", descriptor, visible ? "" : "not ");
        if (IMPLEMENTATION_SIGNATURE.equals(descriptor)) implementation = true;
        if (COMPONENT_SIGNATURE.equals(descriptor)) component = true;
        return null;
    }

    @Override public void visitAttribute(final Attribute attribute) {
        System.out.printf("- Attribute %s%n", attribute);
    }

    @Override
    public void visitInnerClass(final String name, final String outerName, final String innerName,
                                final int access) {
//        System.out.printf("- Inner class %s (inner %s, outer %s)%n", name, innerName, outerName);
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String descriptor,
                                   final String signature, final Object value) {
//        System.out.printf("- Field %s (%s): %s=%s%n", name, signature, descriptor, value);
        this.currentField = new FieldDef(name, descriptor, signature);
        return new FieldParser();
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
                                     final String signature, final String[] exceptions) {
//        System.out.printf("- Method %s (%s): %s throws %s%n", name, signature, descriptor,
//                StringUtil.toString(exceptions));
        return null;
    }

    @Override public void visitEnd() {
//        System.out.println("}");
        final String className = this.className.replaceFirst("\\.class$", ""). replace('/', '.');
        if (implementation) addImplementation(className);
        if (component) addComponent(className);
        if (!injected.isEmpty()) addInjections(className, injected);
        implementation = false;
        component = false;
        injected.clear();
    }

    private void addImplementation(final String className) {
        implementations.add(className);
    }

    private void addComponent(final String className) {
        components.add(className);
    }

    private void addInjections(final String className, final Set<FieldDef> injected) {
        components.add(className);
        injections.computeIfAbsent(className, key -> new HashSet<>()).addAll(injected);
    }

    public void list() {
        System.out.println("Components");
        components.forEach(c -> System.out.printf("• %s%n", c));
        System.out.println("Injections");
        injections.forEach((c, fs) -> System.out.printf("• %s: %s%n", c, fs));
        System.out.println("Implementations");
        implementations.forEach(i -> System.out.printf("• %s%n", i));
    }

    private class FieldParser extends FieldVisitor {

        public FieldParser() {
            super(Opcodes.ASM5);
        }

    }

    private class FieldDef implements Representable {

        private final String name;
        private final String descriptor;
        private final String signature;

        public FieldDef(final String name, final String descriptor, final String signature) {
            this.name = name;
            this.descriptor = descriptor;
            this.signature = signature;
        }

        @ToString public String getName() {
            return this.name;
        }

        @ToString public String getDescriptor() {
            return this.descriptor;
        }

        @ToString public String getSignature() {
            return this.signature;
        }

        @Override public String toString() {
            return ClassUtil.toString(this);
        }

        @Override public String represent() {
            return String.format("%s %s(%s)", ClassUtil.toExternal(getDescriptor()), getName(),
                    getSignature());
        }

    }
}
