package io.github.shashank022.linkagelens.bytecode;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ClassFileParser {
    private static final int MAGIC = 0xCAFEBABE;
    private static final Pattern DESC_CLASS = Pattern.compile("L([^;]+);");

    private ClassFileParser() {}

    public static ClassInfo parse(byte[] bytes) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (in.readInt() != MAGIC) throw new IOException("Not a Java class file");
            in.readUnsignedShort();
            int major = in.readUnsignedShort();
            int cpCount = in.readUnsignedShort();
            CpEntry[] cp = new CpEntry[cpCount];
            for (int i = 1; i < cpCount; i++) {
                int tag = in.readUnsignedByte();
                switch (tag) {
                    case 1 -> cp[i] = new CpEntry(tag, in.readUTF(), 0, 0);
                    case 3, 4 -> { in.readInt(); cp[i] = new CpEntry(tag, null, 0, 0); }
                    case 5, 6 -> { in.readLong(); cp[i] = new CpEntry(tag, null, 0, 0); i++; }
                    case 7, 8, 16, 19, 20 -> cp[i] = new CpEntry(tag, null, in.readUnsignedShort(), 0);
                    case 9, 10, 11, 12, 17, 18 -> cp[i] = new CpEntry(tag, null, in.readUnsignedShort(), in.readUnsignedShort());
                    case 15 -> cp[i] = new CpEntry(tag, null, in.readUnsignedByte(), in.readUnsignedShort());
                    default -> throw new IOException("Unsupported constant-pool tag: " + tag);
                }
            }
            in.readUnsignedShort();
            int thisClass = in.readUnsignedShort();
            int superClass = in.readUnsignedShort();
            String name = className(cp, thisClass);
            String superName = superClass == 0 ? null : className(cp, superClass);
            int interfaceCount = in.readUnsignedShort();
            List<String> interfaces = new ArrayList<>(interfaceCount);
            for (int i = 0; i < interfaceCount; i++) interfaces.add(className(cp, in.readUnsignedShort()));
            Set<MemberKey> fields = readMembers(in, cp);
            Set<MemberKey> methods = readMembers(in, cp);
            skipAttributes(in);
            Set<String> refs = new LinkedHashSet<>();
            List<MemberRef> memberRefs = new ArrayList<>();
            for (int i = 1; i < cp.length; i++) {
                CpEntry e = cp[i];
                if (e == null) continue;
                if (e.tag == 7) addType(refs, utf8(cp, e.a));
                if (e.tag == 9 || e.tag == 10 || e.tag == 11) {
                    String owner = className(cp, e.a);
                    CpEntry nat = cp[e.b];
                    String memberName = utf8(cp, nat.a);
                    String descriptor = utf8(cp, nat.b);
                    MemberRef.Kind kind = e.tag == 9 ? MemberRef.Kind.FIELD : (e.tag == 11 ? MemberRef.Kind.INTERFACE_METHOD : MemberRef.Kind.METHOD);
                    memberRefs.add(new MemberRef(kind, owner, memberName, descriptor));
                    addType(refs, owner);
                    addDescriptorTypes(refs, descriptor);
                }
                if (e.tag == 12) addDescriptorTypes(refs, utf8(cp, e.b));
            }
            for (MemberKey key : fields) addDescriptorTypes(refs, key.descriptor());
            for (MemberKey key : methods) addDescriptorTypes(refs, key.descriptor());
            refs.remove(name);
            return new ClassInfo(name, superName, List.copyOf(interfaces), Set.copyOf(fields), Set.copyOf(methods), Set.copyOf(refs), List.copyOf(memberRefs), major);
        }
    }

    private static Set<MemberKey> readMembers(DataInputStream in, CpEntry[] cp) throws IOException {
        int count = in.readUnsignedShort();
        Set<MemberKey> result = new LinkedHashSet<>();
        for (int i = 0; i < count; i++) {
            in.readUnsignedShort();
            String name = utf8(cp, in.readUnsignedShort());
            String desc = utf8(cp, in.readUnsignedShort());
            result.add(new MemberKey(name, desc));
            skipAttributes(in);
        }
        return result;
    }

    private static void skipAttributes(DataInputStream in) throws IOException {
        int count = in.readUnsignedShort();
        for (int i = 0; i < count; i++) {
            in.readUnsignedShort();
            long len = Integer.toUnsignedLong(in.readInt());
            long skipped = 0;
            while (skipped < len) {
                long n = in.skip(len - skipped);
                if (n <= 0) { in.readByte(); n = 1; }
                skipped += n;
            }
        }
    }

    private static String className(CpEntry[] cp, int classIndex) throws IOException {
        CpEntry e = cp[classIndex];
        if (e == null || e.tag != 7) throw new IOException("Invalid class constant: " + classIndex);
        return utf8(cp, e.a);
    }

    private static String utf8(CpEntry[] cp, int index) throws IOException {
        CpEntry e = cp[index];
        if (e == null || e.tag != 1) throw new IOException("Invalid UTF8 constant: " + index);
        return e.text;
    }

    private static void addType(Set<String> refs, String value) {
        if (value == null || value.isBlank()) return;
        if (value.charAt(0) == '[') addDescriptorTypes(refs, value);
        else refs.add(value);
    }

    private static void addDescriptorTypes(Set<String> refs, String descriptor) {
        if (descriptor == null) return;
        Matcher m = DESC_CLASS.matcher(descriptor);
        while (m.find()) refs.add(m.group(1));
    }

    private record CpEntry(int tag, String text, int a, int b) {}
}
