package io.github.shashank022.linkagelens.analysis;

import io.github.shashank022.linkagelens.bytecode.MemberRef;

import java.lang.reflect.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class PlatformTypes {
    private final Map<String, Boolean> cache = new ConcurrentHashMap<>();

    boolean isPlatform(String internalName) {
        if (internalName == null || internalName.isBlank()) return false;
        return cache.computeIfAbsent(internalName, this::lookup);
    }

    boolean memberExists(String internalName, MemberRef ref) {
        Class<?> type = load(internalName);
        if (type == null) return false;
        if (ref.kind() == MemberRef.Kind.FIELD) {
            for (Field f : type.getFields()) if (f.getName().equals(ref.name()) && descriptor(f.getType()).equals(ref.descriptor())) return true;
            for (Field f : type.getDeclaredFields()) if (f.getName().equals(ref.name()) && descriptor(f.getType()).equals(ref.descriptor())) return true;
            return false;
        }
        if (ref.name().equals("<init>")) {
            for (Constructor<?> c : type.getDeclaredConstructors()) if (methodDescriptor(void.class, c.getParameterTypes()).equals(ref.descriptor())) return true;
            return false;
        }
        for (Method m : type.getMethods()) if (matches(m, ref)) return true;
        for (Method m : type.getDeclaredMethods()) if (matches(m, ref)) return true;
        return false;
    }

    private boolean matches(Method m, MemberRef ref) {
        return m.getName().equals(ref.name()) && methodDescriptor(m.getReturnType(), m.getParameterTypes()).equals(ref.descriptor());
    }

    private boolean lookup(String internalName) { return load(internalName) != null; }

    private Class<?> load(String internalName) {
        String binary = internalName.replace('/', '.');
        try { return ClassLoader.getPlatformClassLoader().loadClass(binary); }
        catch (ClassNotFoundException | LinkageError ignored) { return null; }
    }

    private String methodDescriptor(Class<?> returnType, Class<?>[] params) {
        StringBuilder sb = new StringBuilder("(");
        for (Class<?> p : params) sb.append(descriptor(p));
        return sb.append(')').append(descriptor(returnType)).toString();
    }

    private String descriptor(Class<?> c) {
        if (c.isArray()) return c.getName().replace('.', '/');
        if (!c.isPrimitive()) return "L" + c.getName().replace('.', '/') + ";";
        if (c == void.class) return "V";
        if (c == boolean.class) return "Z";
        if (c == byte.class) return "B";
        if (c == char.class) return "C";
        if (c == short.class) return "S";
        if (c == int.class) return "I";
        if (c == long.class) return "J";
        if (c == float.class) return "F";
        if (c == double.class) return "D";
        throw new IllegalArgumentException("Unknown primitive " + c);
    }
}
