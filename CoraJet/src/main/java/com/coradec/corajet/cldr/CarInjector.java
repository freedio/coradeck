package com.coradec.corajet.cldr;

import static com.coradec.coracore.model.Scope.*;
import static org.objectweb.asm.Opcodes.*;

import com.coradec.coracore.annotation.Component;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.model.Scope;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coracore.util.ExecUtil;
import com.coradec.corajet.trouble.ImplementationNotFoundException;
import com.coradec.corajet.trouble.UnknownScopeException;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * ​​The injector of the CarClassLoader.
 */
public class CarInjector {

    private static final String INJECT_DESC = ClassUtil.signatureOf(Inject.class);
    private static Map<String, Scope> implementations = new HashMap<>();
    private static Map<Class<?>, Object> singletons = new HashMap<>();
    private Set<String> components = new HashSet<>();

    public CarInjector() {

    }

    /**
     * Embeds the class file in the specified buffer by resolving its static injection points and
     * prepares it to resolve its instance injection points.
     *
     * @param name   the name of the class.
     * @param buffer the buffer containing the class file.
     * @param off    the offset in the buffer at which the class file starts.
     * @param len    the length of the class file in the buffer.
     * @return a byte array confaining the embedded class.
     */
    public byte[] embedClass(final String name, final byte[] buffer, final int off, final int len) {
        ClassReader reader = new ClassReader(buffer, off, len);
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new ClassModeler(writer), 0);
        return writer.toByteArray();
    }

    public static void finish(Object target) {
//        Syslog.info("Known implementations: %s", implementations);
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Object>)() -> {
                final Class<?> targetClass = target.getClass();
                for (Field field : targetClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Inject.class)) {
                        final Class<?> fieldType = field.getType();
                        Class<?> implClass = null;
                        Scope scope = null;
                        for (final Entry<String, Scope> entry : implementations.entrySet()) {
                            final String implClassName = entry.getKey();
//                            Syslog.debug("Looking up class \"%s\"", implClassName);
                            implClass = Class.forName(implClassName);
                            if (fieldType.isAssignableFrom(implClass)) {
                                scope = entry.getValue();
                                break;
                            }
                        }
                        if (implClass == null || scope == null)
                            throw new ImplementationNotFoundException(targetClass.getName(),
                                    field.getName(), fieldType);
                        final Object value = instantiate(implClass, scope);
//                        Syslog.debug("Setting field %s.%s to %s", targetClass.getName(),
//                                field.getName(), value);
                        field.setAccessible(true);
                        field.set(target, value);
                    }
                }
                return null;
            });
        }
        catch (PrivilegedActionException e) {
            Syslog.error(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T instantiate(final Class<T> implClass, final Scope scope) throws Exception {
        final Exception[] problem = {null};
        switch (scope) {
            case SINGLETON:
                final T result = (T)singletons.computeIfAbsent(implClass, k -> {
                    try {
                        return k.newInstance();
                    }
                    catch (Exception e) {
                        problem[0] = e;
                        return null;
                    }
                });
                if (problem[0] == null) return result;
                else throw problem[0];
            case TEMPLATE:
                return (T)implClass.newInstance();
            default:
                throw new UnknownScopeException(scope);
        }
    }

    public <T> T implement(Class<? super T> interfaceType)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> implClass = null;
        Scope scope = null;
        for (final Entry<String, Scope> entry : implementations.entrySet()) {
            final String implClassName = entry.getKey();
            implClass = Class.forName(implClassName);
            if (interfaceType.isAssignableFrom(implClass)) {
                scope = entry.getValue();
                break;
            }
        }
        if (implClass == null || scope == null)
            throw new ImplementationNotFoundException(ExecUtil.getCallerStackFrame().getClassName(),
                    null, interfaceType);
        return (T)implClass.newInstance();
    }

    private class ClassModeler extends ClassVisitor {

        private String currentClassName;
        private final Map<String, String> fieldInjections = new HashMap<>();

        ClassModeler(final ClassWriter writer) {
            super(Opcodes.ASM5, writer);
        }

        @Override public void visit(final int version, final int access, final String name,
                                    final String signature, final String superName,
                                    final String[] interfaces) {
            currentClassName = name.replace('/', '.');
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            final String annClassName = ClassUtil.toExternal(desc);
            if (annClassName.equals(Component.class.getName())) {
                components.add(currentClassName);
            } else if (annClassName.equals(Implementation.class.getName())) {
                implementations.put(currentClassName, TEMPLATE); // default
                return new ClassAnnotationVisitor(currentClassName);
            } else {
                // others are uninteresting --- until at least one field is annotated with @Inject
            }
            return null;
        }

        @Override
        public FieldVisitor visitField(final int access, final String name, final String desc,
                                       final String signature, final Object value) {
            if (value == null && (Modifier.isStatic(access))) {
                Syslog.debug("Detected static field (%s)%s", desc, name);
                return new ClassModeler.FieldPatcher(
                        cv.visitField(access, name, desc, signature, value), name,
                        signature != null ? signature : desc);
            }
            return super.visitField(access, name, desc, signature, value);
        }

        @Override public MethodVisitor visitMethod(final int access, String name, final String desc,
                                                   final String signature,
                                                   final String[] exceptions) {
            if (name.equals("<init>")) {
                Syslog.debug("Found a constructor.");
                return new ConstructorPatcher(currentClassName,
                        signature != null ? signature : desc,
                        cv.visitMethod(access, name, desc, signature, exceptions));
            }
            if (name.equals("<clinit>") && !fieldInjections.isEmpty()) {
                return new ClassModeler.ClassConstructorPatcher(
                        cv.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        private void processStaticFieldInjections(MethodVisitor mv) {
            fieldInjections.forEach((field, type) -> {
                Syslog.debug("Inserting patch code for (%s)%s.%s.", type, currentClassName, field);
//  LDC Lcom/coradec/corajet/test/Interface;.class
//  INVOKESTATIC com/coradec/corajet/cldr/CarInjector.implement (Ljava/lang/Class;)
// Ljava/lang/Object;
//  CHECKCAST com/coradec/corajet/test/Interface
//  PUTSTATIC com/coradec/corajet/test/ToRemodel.staticInjectUnassigned :
//            Lcom/coradec/corajet/test/Interface;
//
//  where:
//    Lcom/coradec/corajet/test/Interface; stands for the interface,
//    com/coradec/corajet/test/ToRemodel   stands for the target class name
//    staticInjectUnassigned               stands for the field name.
                mv.visitLdcInsn(Type.getType(type));
                mv.visitMethodInsn(INVOKESTATIC, INJECT_DESC, "implement",
                        "(Ljava/lang/Class;)Ljava/lang/Object;", false);
                mv.visitTypeInsn(CHECKCAST, type);
                mv.visitFieldInsn(PUTSTATIC, currentClassName, field, type);
            });
            fieldInjections.clear();
        }

        @Override public void visitEnd() {
            if (!fieldInjections.isEmpty()) {
                final ClassModeler.ClassConstructorPatcher cp =
                        new ClassModeler.ClassConstructorPatcher(
                                cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null));
                cp.visitCode();
                cp.visitMaxs(1, 0);
            }
            super.visitEnd();
        }

        private class FieldPatcher extends FieldVisitor {

            private final String name;
            private final String type;

            FieldPatcher(final FieldVisitor fv, final String name, final String type) {
                super(Opcodes.ASM5, fv);
                this.name = name;
                this.type = type;
            }

            @Override
            public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                if (desc.equals(INJECT_DESC) && visible) fieldInjections.put(name, this.type);
                return super.visitAnnotation(desc, visible);
            }

        }

        private class ClassConstructorPatcher extends MethodVisitor {

            ClassConstructorPatcher(final MethodVisitor mv) {
                super(Opcodes.ASM5, mv);
            }

            @Override public void visitCode() {
                mv.visitCode();
                processStaticFieldInjections(mv);
            }

            @Override public void visitMaxs(final int maxStack, final int maxLocals) {
                super.visitMaxs(Integer.max(maxStack, 1), maxLocals);
            }
        }

    }

    private class ClassAnnotationVisitor extends AnnotationVisitor {

        private final String className;

        public ClassAnnotationVisitor(final String className) {
            super(Opcodes.ASM5);
            this.className = className;
        }

        @Override public void visitEnum(final String name, final String desc, final String value) {
            try {
                Class<?> annotationValueType = ClassUtil.classForDescriptor(desc);
                if ("value".equals(name) && annotationValueType == Scope.class) {
                    Syslog.info("Found implementation class %s with scope %s", className,
                            annotationValueType);
                    if (Scope.SINGLETON.name().equals(value)) {
                        implementations.put(className, Scope.SINGLETON);
                    } else if (TEMPLATE.name().equals(value)) {
                        implementations.put(className, Scope.TEMPLATE);
                    } else Syslog.warn("Unknown scope: %s -> ignored", value);
                } else
                    Syslog.error("Unrecognized combination: name=%s, desc=%s, value=\"%s\"", name,
                            desc, value);
            }
            catch (ClassNotFoundException e) {
                Syslog.error(e);
            }
            super.visitEnum(name, desc, value);
        }
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class ConstructorPatcher extends MethodVisitor {

        private final String className;
        private final String signature;

        ConstructorPatcher(final String className, final String signature, final MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
            this.className = className;
            this.signature = signature;
        }

        @Override
        public void visitMethodInsn(final int opcode, final String owner, final String name,
                                    final String desc, final boolean intf) {
            super.visitMethodInsn(opcode, owner, name, desc, intf);
            if (opcode == INVOKESPECIAL && "<init>".equals(name) && !className.equals(owner)) {
//                Syslog.debug("Patching constructor invocation %s.%s in %s%n", owner, name,
//                        className);
                mv.visitIntInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESTATIC, "com/coradec/corajet/cldr/CarInjector", "finish",
                        "(Ljava/lang/Object;)V", intf);
                Syslog.debug("Patched constructor %s(%s).", className.replace('/', '.'),
                        ClassUtil.fromSignature(signature)[1]);
            }
        }

    }

}
