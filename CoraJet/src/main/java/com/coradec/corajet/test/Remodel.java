package com.coradec.corajet.test;

import static org.objectweb.asm.Opcodes.*;

import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.trouble.ResourceFileNotFoundException;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.corajet.cldr.Syslog;
import com.coradec.corajet.ctrl.Injector;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * ​​Application that changes the constructors of a class.
 */
public class Remodel implements Origin {

    private static final String INJECT_DESC = ClassUtil.signatureOf(Inject.class);

    public static void main(String[] args) {
        try {
            new Remodel().launch();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launch() throws URISyntaxException, IOException {
        final String inClassName = "com/coradec/corajet/test/ToRemodel.class";
        final String outClassName = "com/coradec/corajet/test/Remodeled.class";
        URL inClassURL = getClass().getClassLoader().getResource(inClassName);
        if (inClassURL == null) throw new ResourceFileNotFoundException(inClassName);
        final Path inPath = Paths.get(inClassURL.toURI());
        final byte[] inClassData = Files.readAllBytes(inPath);
        ClassReader reader = new ClassReader(inClassData);
        ClassWriter writer = new ClassWriter(reader, 0);
        ClassModeler modeler = new ClassModeler(writer);
        reader.accept(modeler, 0);
        final byte[] outClassData = writer.toByteArray();
        Path outPath = inPath.getParent().resolve("Remodeled.class");
        Files.write(outPath, outClassData, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override public String represent() {
        return "Remodeler";
    }

    @Override public URI toURI() {
        return URI.create(represent());
    }

    private class ClassModeler extends ClassVisitor {

        private String currentClassName;
        private Map<String, String> fieldInjections = new HashMap<String, String>();

        public ClassModeler(final ClassWriter writer) {
            super(Opcodes.ASM5, writer);
        }

        @Override public void visit(final int version, final int access, final String name,
                                    final String signature, final String superName,
                                    final String[] interfaces) {
            currentClassName = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            return new ClassAnnotationVisitor(desc, visible);
        }

        @Override
        public FieldVisitor visitField(final int access, final String name, final String desc,
                                       final String signature, final Object value) {
            if (value == null && (Modifier.isStatic(access))) {
                System.out.printf("Detected static field (%s)%s%n", desc, name);
                return new FieldPatcher(cv.visitField(access, name, desc, signature, value), name,
                        signature != null ? signature : desc);
            }
            return super.visitField(access, name, desc, signature, value);
        }

        @Override public MethodVisitor visitMethod(final int access, String name, final String desc,
                                                   final String signature,
                                                   final String[] exceptions) {
            if (name.equals("<init>")) {
                System.out.printf("Found a constructor.%n");
                return new ConstructorPatcher(currentClassName,
                        signature != null ? signature : desc,
                        cv.visitMethod(access, name, desc, signature, exceptions));
            }
            if (name.equals("<clinit>") && !fieldInjections.isEmpty()) {
                return new ClassConstructorPatcher(
                        cv.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        private void processStaticFieldInjections(MethodVisitor mv) {
            fieldInjections.forEach((field, type) -> {
                System.out.printf("Inserting patch code for (%s)%s.%s.%n", type, currentClassName,
                        field);
//  LDC Lcom/coradec/corajet/test/Interface;.class
//  INVOKESTATIC com/coradec/corajet/ctrl/Injector.implement (Ljava/lang/Class;)Ljava/lang/Object;
//  CHECKCAST com/coradec/corajet/test/Interface
//  PUTSTATIC com/coradec/corajet/test/ToRemodel.staticInjectUnassigned :
//            Lcom/coradec/corajet/test/Interface;
//
//  where:
//    Lcom/coradec/corajet/test/Interface; stands for the interface,
//    com/coradec/corajet/test/ToRemodel   stands for the target class name
//    staticInjectUnassigned               stands for the field name.
                mv.visitLdcInsn(Type.getType(type));
                mv.visitMethodInsn(INVOKESTATIC, ClassUtil.internalNameOf(Injector.class),
                        "implement", "(Ljava/lang/Class;)Ljava/lang/Object;", false);
                mv.visitTypeInsn(CHECKCAST, type);
                mv.visitFieldInsn(PUTSTATIC, currentClassName, field, type);
            });
            fieldInjections.clear();
        }

        @Override public void visitEnd() {
            if (!fieldInjections.isEmpty()) {
                final ClassConstructorPatcher cp = new ClassConstructorPatcher(
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
                if (desc.equals(INJECT_DESC) && visible) {
                    try {
                        final Class<?> impl = ClassUtil.classForDescriptor(type);
                        final Class<?> implType = Injector.findImplementationFor(impl).orElse(null);
                        if (implType != null) fieldInjections.put(name, this.type);
                    }
                    catch (ClassNotFoundException e) {
                        Syslog.error(e);
                    }
                }
                return super.visitAnnotation(desc, visible);
            }

        }

        private class ClassConstructorPatcher extends MethodVisitor {

            public ClassConstructorPatcher(final MethodVisitor mv) {
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

        ClassAnnotationVisitor(final String desc, final boolean visible) {
            super(Opcodes.ASM5);
        }

        @Override
        public void visitEnum(final String name, final String desc, final String value) {
            System.out.printf("Enum type %s descr %s value %s%n", name, desc, value);
        }
    }

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
//                System.out.printf("Patching constructor invocation %s.%s in %s%n", owner, name,
//                        className);
                mv.visitIntInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESTATIC, "com/coradec/corajet/ctrl/Injector", "finish",
                        "(Ljava/lang/Object;)V", intf);
                System.out.printf("Patched constructor %s(%s).%n", className.replace('/', '.'),
                        ClassUtil.fromSignature(signature)[1]);
            }
        }

    }

}
