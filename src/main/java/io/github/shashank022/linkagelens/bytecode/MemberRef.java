package io.github.shashank022.linkagelens.bytecode;

public record MemberRef(Kind kind, String owner, String name, String descriptor) {
    public enum Kind { FIELD, METHOD, INTERFACE_METHOD }
    public MemberKey key() { return new MemberKey(name, descriptor); }
    public String display() { return owner.replace('/', '.') + "." + name + descriptor; }
}
