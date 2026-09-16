package io.github.shashank022.linkagelens.bytecode;

import java.util.List;
import java.util.Set;

public record ClassInfo(
        String name,
        String superName,
        List<String> interfaces,
        Set<MemberKey> fields,
        Set<MemberKey> methods,
        Set<String> referencedClasses,
        List<MemberRef> memberRefs,
        int majorVersion) {

    public String dottedName() { return name.replace('/', '.'); }
}
