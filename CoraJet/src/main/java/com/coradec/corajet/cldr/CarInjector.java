/*
 * Copyright ⓒ 2017 by Coradec GmbH.
 *
 * This file is part of the Coradeck.
 *
 * Coradeck is free software: you can redistribute it under the the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License,
 * or any later version.
 *
 * Coradeck is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR ANY PARTICULAR PURPOSE.  See the
 * GNU General Public License for further details.
 *
 * The GNU General Public License is available from <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 * @author Dominik Wezel <dom@coradec.com>
 *
 */

package com.coradec.corajet.cldr;

import static com.coradec.coracore.model.InjectionMode.*;
import static com.coradec.coracore.model.Scope.*;
import static org.objectweb.asm.Opcodes.*;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.ctrl.Factory;
import com.coradec.coracore.model.InjectionMode;
import com.coradec.coracore.model.Scope;
import com.coradec.coracore.trouble.ObjectInstantiationFailure;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coracore.util.StringUtil;
import com.coradec.corajet.trouble.ImplementationNotFoundException;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ​​The injector of the CarClassLoader.
 */
@SuppressWarnings({"unchecked", "WeakerAccess"})
public class CarInjector {

    static final String INJECT_DESC = ClassUtil.descriptorOf(Inject.class);
    static final Map<String, Scope> implementations = new HashMap<>();
    private static final Map<Class<?>, Object> singletons = new HashMap<>();
    static final Set<String> components = new HashSet<>();
    private static Set<ImplementationClass<?>> implementationClasses;

    @SuppressWarnings("unchecked")
    private static Set<ImplementationClass<?>> getImplementationClasses() {
        if (implementationClasses == null ||
            implementationClasses.size() != implementations.size()) {
            implementationClasses = new HashSet<>();
            for (final Entry<String, Scope> entry : implementations.entrySet()) {
                try {
                    final Class<?> klass = Class.forName(entry.getKey());
                    implementationClasses.add(new ImplementationClass(klass, entry.getValue()));
                }
                catch (ClassNotFoundException e) {
                    // Log this incident and don't add the class
                    Syslog.error(e);
                }
            }
        }
        return implementationClasses;
    }

    /**
     * Embeds the class file in the specified buffer by resolving its static injection points and
     * prepares it to resolve its instance injection points.
     *
     * @param name   the name of the class.
     * @param buffer the buffer containing the class file.
     * @param off    the offset in the buffer at which the class file starts.
     * @param len    the length of the class file in the buffer.
     * @return a byte array containing the embedded class.
     */
    public byte[] embedClass(final String name, final byte[] buffer, final int off, final int len) {
        ClassReader reader = new ClassReader(buffer, off, len);
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new ClassModeler(writer), 0);
        return writer.toByteArray();
    }

    /**
     * Embeds the class file in the specified buffer by resolving its static injection points and
     * prepares it to resolve its instance injection points.
     *
     * @param name   the name of the class.
     * @param buffer the buffer containing the class file.
     * @param off    the offset in the buffer at which the class file starts.
     * @param len    the length of the class file in the buffer.
     * @return bit 0 set if the class is a component, bit 1 set if the class is an implementation,
     * all other bits are 0.
     */
    public int analyzeClass(final String name, final byte[] buffer, final int off, final int len) {
        ClassReader reader = new ClassReader(buffer, off, len);
        ClassWriter writer = new ClassWriter(reader, 0);
        final ClassAnalyzer classAnalyzer = new ClassAnalyzer();
        reader.accept(classAnalyzer, 0);
        return classAnalyzer.getState();
    }

    public static void finishG(final Object instance, final Class<?>... types) {
        final TypeVariable<? extends Class<?>>[] typeVars = instance.getClass().getTypeParameters();
        Syslog.debug("Resolving instance %s with %s for type parameters %s", instance,
                StringUtil.toString(types), StringUtil.toString(typeVars));
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Object>)() -> {
                final Class<?> targetClass = instance.getClass();
                for (Field field : targetClass.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers()) &&
                        field.isAnnotationPresent(Inject.class)) {
                        final Class<?> fieldType = field.getType();
                        final Type genericType = field.getGenericType();
                        final String name = field.getName();
                        Syslog.trace("Field %s.%s has generic type parameters %s",
                                instance.getClass().getName(), name, genericType);
                        List<Type> typeArgs = Collections.EMPTY_LIST;
                        if (genericType instanceof ParameterizedType) typeArgs = Arrays.asList(
                                ((ParameterizedType)genericType).getActualTypeArguments());
                        typeArgs = typeArgs.stream().map(type -> {
                            if (type instanceof TypeVariable) {
                                for (int i = 0, is = typeVars.length; i < is; ++i) {
                                    if (((TypeVariable)type).getName()
                                                            .equals(typeVars[i].getName())) {
                                        return i < types.length ? types[i] : null;
                                    }
                                }
                            }
                            return type;
                        }).collect(Collectors.toList());
                        field.setAccessible(true);
                        field.set(instance, implementationFor(fieldType, typeArgs));
                        Syslog.debug("Field %s of %s set to %s", name, instance,
                                StringUtil.toString(field.get(instance)));
                    }
                }
                return null;
            });
        }
        catch (PrivilegedActionException e) {
            Syslog.error(e.getException());
        }
    }

    /**
     * Finishes the construction of the specified instance by resolving all injection points.  The
     * method is invoked from the patched constructor &lt;init&gt; immediately after the call to the
     * superclass constructor.
     *
     * @param instance the instance to finish.
     */
    public static void finish(Object instance) {
        Syslog.debug(">>> %s.finish()", instance);
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Object>)() -> {
                for (Class<?> targetClass = instance.getClass();
                     targetClass != null;
                     targetClass = targetClass.getSuperclass()) {
                    for (Field field : targetClass.getDeclaredFields()) {
                        if (!Modifier.isStatic(field.getModifiers()) &&
                            field.isAnnotationPresent(Inject.class)) {
                            try {
                                final String name = field.getName();
                                final Class<?> fieldType = field.getType();
                                final Type genericType = field.getGenericType();
                                Syslog.debug(">>> inject: %s.%s", instance.getClass().getName(),
                                        name);
                                Syslog.trace("Field %s.%s has generic type parameters %s",
                                        instance.getClass().getName(), name, genericType);
                                List<Type> typeArgs = Collections.EMPTY_LIST;
                                if (genericType instanceof ParameterizedType) typeArgs =
                                        Arrays.asList(
                                                ((ParameterizedType)genericType)
                                                        .getActualTypeArguments());
                                field.setAccessible(true);
                                field.set(instance, implementationFor(fieldType, typeArgs));
                                Syslog.debug("Field %s of %s set to %s", name, instance,
                                        StringUtil.toString(field.get(instance)));
                            }
                            catch (Exception e) {
                                Syslog.error(e);
                            }
                        }
                    }
                }
                return null;
            });
        }
        catch (PrivilegedActionException e) {
            Syslog.error(e.getException());
        }
    }

    /**
     * Returns an implementation of the specified interface type, if possible.  This method is
     * invoked in the patched class constructor &lt;clinit&gt; for each static field tagged as
     * injected.
     *
     * @param interfaceType the interface type to implement, including type parameters.
     * @return an implementation of the interface, or {@code null}.
     * @throws ClassNotFoundException          if either the interface itself or any of its type
     *                                         parameters were missing on the classpath.
     * @throws ImplementationNotFoundException if not implementation for the specified interface was
     *                                         found.
     */
    public static Object implement(String interfaceType)
            throws ClassNotFoundException, ImplementationNotFoundException {
        Syslog.debug("Looking for an implementation of %s", interfaceType);
        Class<?> interfaceClass = ClassUtil.classForDescriptor(interfaceType);
        final List<Type> typeArgs = extractTypeParameters(interfaceType);
        Syslog.debug("i.e. %s%s", interfaceClass.getName(), StringUtil.toString(typeArgs));
        return implementationFor(interfaceClass, typeArgs);
    }

    static Object implementationFor(final Class<?> interfaceClass, final List<Type> types,
                                    Object... args) throws ObjectInstantiationFailure {
        if (Factory.class.isAssignableFrom(interfaceClass)) {
            return new ObjectFactory(types);
        }
        Exception[] failed = new Exception[1];
        try {
            return getImplementationClasses().stream()
                                             .filter(ic -> ic.matches(interfaceClass, types))
                                             .findAny()
                                             .map(ic -> {
                                                 try {
                                                     return ic.instantiate(types, args);
                                                 }
                                                 catch (ObjectInstantiationFailure e) {
                                                     failed[0] = e;
                                                     return null;
                                                 }
                                             })
                                             .filter(Objects::nonNull)
                                             .orElseThrow(() -> failed[0] != null //
                                                                ? failed[0]
                                                                : new ImplementationNotFoundException(
                                                                        interfaceClass.getName(),
                                                                        types, args));
        }
        catch (ObjectInstantiationFailure e) {
            throw e;
        }
        catch (Exception e) {
            throw new ObjectInstantiationFailure(interfaceClass, e);
        }
    }

    /**
     * Extracts the generic type parameters from the specified generic type name.
     *
     * @param genericTypeName the generic type name.
     * @return the generic type parameters, if any.
     */
    private static List<Type> extractTypeParameters(final String genericTypeName)
            throws ClassNotFoundException {
        List<Type> ptypes = new ArrayList<>();
        int start = genericTypeName.indexOf('<');
        int end = genericTypeName.lastIndexOf('>');
        if (start == -1 || end == -1) return ptypes;
        String paraTypes = genericTypeName.substring(start + 1, end);
        start = 0;
        for (int i = 0, level = 0, is = paraTypes.length(); i < is; ++i) {
            char c = paraTypes.charAt(i);
            if (c == '<') ++level;
            else if (c == '>') --level;
            else if (c == ';' && level == 0) {
                ptypes.add(ClassUtil.typeForDescriptor(paraTypes.substring(start, start = i + 1)));
            }
        }
        return ptypes;
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private static class ImplementationClass<T> {

        private static final Object UNASSIGNED = new Object();

        private final Class<? super T> klass;
        private final Scope scope;
        private final TypeVariable<? extends Class<? super T>>[] typeParameters;
        private final Map<List<ParameterizedType>, T> parametrizedInstances;
        private T singleton;

        ImplementationClass(final Class<? super T> klass, final Scope scope) {
            this.klass = klass;
            this.scope = scope;
            this.typeParameters = klass.getTypeParameters();
            parametrizedInstances = new HashMap<>();
        }

        private Class<? super T> getEmbeddedClass() {
            return this.klass;
        }

        private Scope getScope() {
            return this.scope;
        }

        @Override public boolean equals(final Object obj) {
            return obj instanceof ImplementationClass &&
                   getEmbeddedClass() == ((ImplementationClass)obj).getEmbeddedClass() &&
                   getScope() == ((ImplementationClass)obj).getScope();
        }

        @Override public int hashCode() {
            return 13 * getEmbeddedClass().hashCode() + 7 * getScope().hashCode();
        }

        /**
         * Checks if the this implementation class is assignable to the specified interface class,
         * including a match of type arguments vs. parameters.
         *
         * @param interfaceClass the interface class.
         * @param types          the type arguments.
         * @return {@code true} if this class matches the interface class.
         */
        boolean matches(final Class<?> interfaceClass, final List<Type> types) {
            return interfaceClass.isAssignableFrom(klass) && types.size() == typeParameters.length;
        }

        /**
         * Returns an instance of this implementation class using the specified type arguments for
         * the type parameters of the class.
         *
         * @param types  the type arguments.
         * @param extras additional arguments.
         * @return an instance of the class.
         * @throws ObjectInstantiationFailure if the class could not be instantiated due to one or
         *                                    more of several reasons..
         */
        @SuppressWarnings("unchecked") T instantiate(final List<Type> types, final Object[] extras)
                throws ObjectInstantiationFailure {
            Syslog.debug("Instantiating %s%s", //
                    klass.getName(), StringUtil.toString(typeParameters)
                                               .replaceFirst("^\\[", "<")
                                               .replaceFirst("]$", ">"));
            Scope scope, conScope = null;
            Constructor<?> constructor = null;
            Object[] args = null;
            List<ParameterizedType> parametrized, conPara = null;
            outer:
            for (final Constructor<?> constr : klass.getConstructors()) {
                Syslog.debug("Examining constructor %s", constr);
                scope = this.scope;
                parametrized = new ArrayList<>();
                final Type[] paras = constr.getGenericParameterTypes();
                final Object[] arguments = new Object[paras.length];
                for (int i = 0, is = paras.length; i < is; i++) {
                    final Type para = paras[i];
                    if (para instanceof ParameterizedType) {
                        ParameterizedType paraType = (ParameterizedType)para;
                        Syslog.debug("Parameter %d is parametrized type (%s)", i,
                                StringUtil.toString(para));
                        if (scope == SINGLETON) scope = PARAMETRIZED;
                        parametrized.add(paraType);
                        final Type[] typeArgs = paraType.getActualTypeArguments();
                        if (paraType.getRawType() == Class.class && typeArgs.length == 1) {
                            Syslog.debug("Found class argument with type args %s and types %s",
                                    StringUtil.toString(typeArgs), StringUtil.toString(types));
                            for (final TypeVariable<? extends Class<? super T>> typeParameter :
                                    typeParameters) {
                                if (typeArgs[0].getTypeName().equals(typeParameter.getTypeName())) {
                                    arguments[i] = types.get(i);
                                }
                            }
                            if (arguments[i] == null) {
                                Syslog.debug("Found class argument %s",
                                        StringUtil.toString(typeArgs), StringUtil.toString(extras));
                                if (extras.length > i && extras[i] instanceof Class)
                                    arguments[i] = extras[i];
                                else continue outer;
                            }
                        } else {
                            Syslog.warn("Found parametrized type %s with type args %s → skipping " +
                                        "constructor", para, StringUtil.toString(typeArgs));
                            continue outer;
                        }
                    } else if (para instanceof Class<?>) {
                        Syslog.debug("Parameter %d is %s", i, para);
                        Class<?> klass = (Class<?>)para;
                        Object value;
                        if (extras.length > i) value = extras[i];
                        else try {
                            value = implementationFor((Class<?>)para, Collections.EMPTY_LIST);
                        }
                        catch (ImplementationNotFoundException | ObjectInstantiationFailure e) {
                            continue outer;
                        }
                        arguments[i] = value;
                    } else {
                        Syslog.warn("Found unrecognizable parameter type %s",
                                ClassUtil.toString(para, para));
                        continue outer;
                    }
                }
                if (args == null || arguments.length > args.length) {
                    // override best choice so far (if any) only if new one has more arguments.
                    constructor = constr;
                    conPara = parametrized;
                    args = arguments;
                    conScope = scope;
                }
            }
            if (constructor == null)
                throw new ObjectInstantiationFailure(klass, "No suitable public constructor found");
            try {
                Syslog.debug("Using %s with %s in scope %s", constructor, StringUtil.toString(args),
                        conScope);
                switch (conScope) {
                    case SINGLETON:
                        if (singleton == null) {
                            Syslog.debug("No singleton so far in %s → instantiating it.", this);
                            singleton = (T)constructor.newInstance(args);
                        }
                        return singleton;
                    case TEMPLATE:
                        return (T)constructor.newInstance(args);
                    case PARAMETRIZED:
                        T instance = parametrizedInstances.get(conPara);
                        if (instance == null) parametrizedInstances.put(conPara,
                                instance = (T)constructor.newInstance(args));
                        return instance;
                    case IDEMPOTENT:
                        return (T)constructor.newInstance(args);
                    default:
                        throw new IllegalArgumentException(
                                String.format("Unknown scope: %s", conScope.name()));
                }
            }
            catch (Exception e) {
                throw new ObjectInstantiationFailure(klass, e);
            }
        }
    }

    /**
     * Implementation of a generic object factory.
     */
    private static class ObjectFactory<T> implements Factory<T> {

        private final Class<? super T> rawType;
        private final List<Type> typeArgs;

        ObjectFactory(final List<Type> types) {
            if (types.isEmpty())
                throw new IllegalArgumentException("Factory without type parameters!");
            Syslog.debug("Creating object factory for %s", StringUtil.toString(types));
            final Type primaryType = types.get(0);
            if (primaryType instanceof Class<?>) {
                rawType = (Class<? super T>)primaryType;
                typeArgs = types.subList(1, types.size());
            } else if (primaryType instanceof ParameterizedType) {
                rawType = (Class<? super T>)((ParameterizedType)primaryType).getRawType();
                typeArgs = Arrays.asList(((ParameterizedType)primaryType).getActualTypeArguments());
            } else throw new IllegalArgumentException(
                    String.format("First type parameter of factory must be a class, but was <%s>!",
                            ClassUtil.toString(primaryType, primaryType)));
        }

        @ToString public Class<? super T> getRawType() {
            return this.rawType;
        }

        @ToString public List<Type> getTypeArgs() {
            return this.typeArgs;
        }

        @Override public T get(final Object... args) {
            T result;
            try {
                result = (T)implementationFor(rawType, typeArgs, args);
                Syslog.debug("ObjectFactory(%s%s).get(%s) → %s", ClassUtil.nameOf(getRawType()),
                        typeArgs.isEmpty() ? "" : typeArgs, ClassUtil.toString(args, args),
                        StringUtil.toString(result));
                return result;
            }
            catch (ImplementationNotFoundException | ObjectInstantiationFailure e) {
                Syslog.error(e);
                throw e;
            }
        }

        @Override public T create(final Object... args) {
            T result;
            try {
                result = (T)implementationFor(rawType, typeArgs, args);
                Syslog.debug("ObjectFactory(%s%s).create(%s) → %s", ClassUtil.nameOf(getRawType()),
                        typeArgs.isEmpty() ? "" : typeArgs, ClassUtil.toString(args, args),
                        StringUtil.toString(result));
                return result;
            }
            catch (ImplementationNotFoundException | ObjectInstantiationFailure e) {
                Syslog.error(e);
                throw e;
            }
        }

        @Override public String toString() {
            return ClassUtil.toString(this);
        }
    }

    @SuppressWarnings({"ClassHasNoToStringMethod", "PackageVisibleField"})
    private class ClassModeler extends ClassVisitor {

        private String currentClassName;
        final Map<String, String> fieldInjections = new HashMap<>();
        boolean patch = false;
        InjectionMode injectionMode = DIRECT; // default

        ClassModeler(final ClassWriter writer) {
            super(Opcodes.ASM5, writer);
        }

        @Override public void visit(final int version, final int access, final String name,
                                    final String signature, final String superName,
                                    final String[] interfaces) {
            currentClassName = name.replace('/', '.');
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Nullable @Override
        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            final String annClassName = ClassUtil.toExternal(desc);
            if (annClassName.equals(Inject.class.getName())) {
                components.add(currentClassName);
                patch = true;
            } else if (annClassName.equals(Implementation.class.getName())) {
                implementations.put(currentClassName, TEMPLATE); // default
                return new ClassAnnotationVisitor(currentClassName);
            }
            return null;
        }

        @Override
        public FieldVisitor visitField(final int access, final String name, final String desc,
                                       final String signature, final Object value) {
            return new ClassModeler.FieldPatcher(
                    cv.visitField(access, name, desc, signature, value), name,
                    signature != null ? signature : desc, Modifier.isStatic(access));
        }

        @Override public MethodVisitor visitMethod(final int access, String name, final String desc,
                                                   final String signature,
                                                   final String[] exceptions) {
            if (patch) {
                if (name.equals("<init>")) {
                    final String descriptor = signature != null ? signature : desc;
                    Syslog.debug("Found constructor %s for class %s.", descriptor,
                            currentClassName);
                    return new ConstructorPatcher(currentClassName, descriptor,
                            cv.visitMethod(access, name, desc, signature, exceptions),
                            injectionMode);
                }
                if (name.equals("<clinit>") && !fieldInjections.isEmpty()) {
                    return new ClassModeler.ClassConstructorPatcher(
                            cv.visitMethod(access, name, desc, signature, exceptions));
                }
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        void processStaticFieldInjections(MethodVisitor mv) {
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
                mv.visitLdcInsn(type);
                mv.visitMethodInsn(INVOKESTATIC, ClassUtil.internalNameOf(CarInjector.class),
                        "implement", "(Ljava/lang/String;)Ljava/lang/Object;", false);
                final String typeInternal = internalNameOf(type);
                mv.visitTypeInsn(CHECKCAST, typeInternal);
                mv.visitFieldInsn(PUTSTATIC, currentClassName.replace('.', '/'), field,
                        "L" + typeInternal + ";");
            });
            fieldInjections.clear();
        }

        @Override public void visitEnd() {
            if (!fieldInjections.isEmpty()) {
                final ClassModeler.ClassConstructorPatcher cp =
                        new ClassModeler.ClassConstructorPatcher(
                                cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null));
                cp.visitCode();
                cp.visitInsn(RETURN);
                cp.visitMaxs(1, 0);
            }
            super.visitEnd();
        }

        private class FieldPatcher extends FieldVisitor {

            private final String name;
            private final String type;
            private final boolean isStatic;

            FieldPatcher(final FieldVisitor fv, final String name, final String type,
                         final boolean isStatic) {
                super(Opcodes.ASM5, fv);
                this.name = name;
                this.type = type;
                this.isStatic = isStatic;
            }

            @Override
            public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                if (desc.equals(INJECT_DESC) && visible) {
                    if (isStatic) fieldInjections.put(name, this.type);
                    patch = true;
                    //noinspection ConstantConditions
                    return new InjectionVisitor(fv.visitAnnotation(desc, visible));
                }
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

        private class InjectionVisitor extends AnnotationVisitor {

            InjectionVisitor(final AnnotationVisitor annotationVisitor) {
                super(Opcodes.ASM5, annotationVisitor);
            }

            @Override
            public void visitEnum(final String name, final String desc, final String value) {
                try {
                    Class<?> annotationValueType = ClassUtil.classForDescriptor(desc);
                    if ("value".equals(name) && annotationValueType == InjectionMode.class) {
                        Syslog.debug("Found injection with type %s", value);
                        injectionMode = InjectionMode.valueOf(value);
                    } else Syslog.error("Unrecognized combination: name=%s, desc=%s, scope=\"%s\"",
                            name, desc, value);
                }
                catch (ClassNotFoundException e) {
                    Syslog.error(e);
                }
                super.visitEnum(name, desc, value);
            }

        }

    }

    String internalNameOf(final String type) {
        String result = type.replaceFirst("<.*>", "");
        Syslog.trace("Internal name of \"%s\" → \"%s\"", type, result);
        try {
            switch (result) {
                case "Z":
                    return "Boolean";
                case "B":
                    return "Byte";
                case "C":
                    return "Character";
            }
            return result.substring(1, result.length() - 1);
        }
        catch (StringIndexOutOfBoundsException e) {
            Syslog.warn("Fishy class name: \"%s\"", type);
            return result;
        }
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class ClassAnnotationVisitor extends AnnotationVisitor {

        private final String className;

        ClassAnnotationVisitor(final String className) {
            super(Opcodes.ASM5);
            this.className = className;
        }

        @Override public void visitEnum(final String name, final String desc, final String value) {
            try {
                Class<?> annotationValueType = ClassUtil.classForDescriptor(desc);
                if ("value".equals(name) && annotationValueType == Scope.class) {
                    Syslog.debug("Found implementation class %s with scope %s", className, value);
                    if (Scope.SINGLETON.name().equals(value)) {
                        implementations.put(className, Scope.SINGLETON);
                    } else if (TEMPLATE.name().equals(value)) {
                        implementations.put(className, Scope.TEMPLATE);
                    } else Syslog.warn("Unknown scope: %s -> ignored", value);
                } else
                    Syslog.error("Unrecognized combination: name=%s, desc=%s, scope=\"%s\"", name,
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
        private final InjectionMode injectionMode;
        private boolean patched;
        private int minStack;

        ConstructorPatcher(final String className, final String signature, final MethodVisitor mv,
                           final InjectionMode injectionMode) {
            super(Opcodes.ASM5, mv);
            this.className = className;
            this.signature = signature;
            this.injectionMode = injectionMode;
            patched = false;
        }

        @Override public void visitMaxs(final int maxStack, final int maxLocals) {
            super.visitMaxs(Math.max(maxStack, minStack), maxLocals);
        }

        @Override
        public void visitMethodInsn(final int opcode, final String owner, final String name,
                                    final String desc, final boolean intf) {
            super.visitMethodInsn(opcode, owner, name, desc, intf);
            if (!patched &&
                opcode == INVOKESPECIAL &&
                "<init>".equals(name) &&
                !className.equals(owner)) {
                Syslog.debug("Patching constructor invocation %s.%s in %s (mode: %s)", owner, name,
                        className, injectionMode.name());
                switch (injectionMode) {
                    case DIRECT:
                        mv.visitIntInsn(ALOAD, 0);
                        mv.visitMethodInsn(INVOKESTATIC, "com/coradec/corajet/cldr/CarInjector",
                                "finish", "(Ljava/lang/Object;)V", intf);
                        minStack = 1;
                        break;
                    case TYPE_ARG:
                        Syslog.debug("Constructor signature: %s", signature);
                        /*
    CarInjector.finishG(this, type1, type2, type1, type2);

    ALOAD 0

    ICONST_4
    ANEWARRAY java/lang/Class

    DUP
    ICONST_0
    ALOAD 1
    AASTORE

    DUP
    ICONST_1
    ALOAD 2
    AASTORE

    DUP
    ICONST_2
    ALOAD 1
    AASTORE

    DUP
    ICONST_3
    ALOAD 2
    AASTORE

    INVOKESTATIC com/coradec/corajet/cldr/CarInjector.finishG (Ljava/lang/Object;
    [Ljava/lang/Class;)V

    MAXSTACK = 5

                         */
                        int nargs = ClassUtil.fromSignature(signature)[1].split(", ").length;
                        mv.visitIntInsn(ALOAD, 0);
                        mv.visitInsn(ICONST_0 + nargs);
                        mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
                        for (int i = 0; i < nargs; ++i) {
                            mv.visitInsn(DUP);
                            mv.visitInsn(ICONST_0 + i);
                            mv.visitIntInsn(ALOAD, i + 1);
                            mv.visitInsn(AASTORE);
                        }
                        mv.visitMethodInsn(INVOKESTATIC, "com/coradec/corajet/cldr/CarInjector",
                                "finishG", "(Ljava/lang/Object;[Ljava/lang/Class;)V", intf);
                        minStack = 5;
                        break;
                    default:
                        Syslog.error("Unknown injection mode: " + injectionMode.name());
                        throw new IllegalArgumentException(
                                "Unknown injection mode: " + injectionMode.name());
                }
                final String parameters = ClassUtil.fromSignature(signature)[1];
                Syslog.debug("Patched constructor %s(%s).", className.replace('/', '.'),
                        parameters == null ? "" : parameters);
                patched = true;
            }

        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class ClassAnalyzer extends ClassVisitor {

        private String currentClassName;
        private int state;
        private FieldAnalyzer fieldAnalyzer;

        ClassAnalyzer() {
            super(Opcodes.ASM5);
        }

        private FieldAnalyzer getFieldAnalyzer() {
            if (fieldAnalyzer == null) fieldAnalyzer = new FieldAnalyzer();
            return this.fieldAnalyzer;
        }

        @Override public void visit(final int version, final int access, final String name,
                                    final String signature, final String superName,
                                    final String[] interfaces) {
            currentClassName = name.replace('/', '.');
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Nullable @Override
        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            final String annClassName = ClassUtil.toExternal(desc);
            if (annClassName.equals(Inject.class.getName())) {
                setComponent();
            } else if (annClassName.equals(Implementation.class.getName())) {
                setImplementation();
            }
            return null;
        }

        void setComponent() {
            state |= 1;
        }

        private void setImplementation() {
            state |= 2;
        }

        @Override
        public FieldVisitor visitField(final int access, final String name, final String desc,
                                       final String signature, final Object value) {
            return getFieldAnalyzer();
        }

        /**
         * Returns the state determined by the analysis.
         *
         * @return the state (bit 0: whether it's a component, bit 1: whether it's an
         * implementation).
         */
        int getState() {
            return state;
        }

        private class FieldAnalyzer extends FieldVisitor {

            FieldAnalyzer() {
                super(Opcodes.ASM5);
            }

            @Override
            public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                if (desc.equals(INJECT_DESC) && visible) {
                    setComponent();
                }
                return super.visitAnnotation(desc, visible);
            }

        }

    }

}
