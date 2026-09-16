# LinkageLens Architecture

## Design goal

LinkageLens analyzes compiled JVM class files and compares what caller bytecode requires with what the scanned runtime artifact set provides.

## Core model

A JVM symbolic method reference is not just a method name. It includes:

```text
owner class + method name + JVM descriptor
```

For example:

```text
com/acme/Client.send
(Ljava/lang/String;I)Lcom/acme/Result;
```

Two methods with the same name but different descriptors are different linkage targets.

## Pipeline

1. Discover `.class` and `.jar` inputs.
2. Recursively inspect nested JARs.
3. Parse class-file constant pools and class/member definitions.
4. Build a class-name -> definitions index.
5. Resolve member references through scanned class hierarchies.
6. Treat JDK platform types using the platform classloader plus exact reflection descriptors.
7. Emit findings.
8. Render text, JSON, or SARIF.

## Why no ASM in v0.1?

The initial parser deliberately handles only the class-file structures needed for linkage analysis. This keeps the standalone runtime at zero external dependencies and makes the proof-of-concept easy to distribute as one JAR.

A future version may adopt ASM or another mature bytecode library if deeper instruction-level/source-line analysis justifies the dependency.

## Multi-release JARs

`META-INF/versions/*` is skipped in v0.1. Correct multi-release selection depends on target runtime semantics and should be modeled explicitly rather than accidentally reported as duplicate classes.
