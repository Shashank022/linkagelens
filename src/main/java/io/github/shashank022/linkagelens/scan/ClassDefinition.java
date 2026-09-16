package io.github.shashank022.linkagelens.scan;

import io.github.shashank022.linkagelens.bytecode.ClassInfo;

public record ClassDefinition(ClassInfo info, String location, String sha256) {}
