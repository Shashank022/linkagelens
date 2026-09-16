package io.github.shashank022.linkagelens.bytecode;

public record MemberKey(String name, String descriptor) {
    @Override public String toString() { return name + descriptor; }
}
