# LinkageLens

> **Find Java dependency problems before the JVM does.**

LinkageLens is an open-source Java bytecode/linkage analyzer that checks the classes and JARs you are actually shipping for runtime binary-compatibility problems such as missing classes, missing methods, missing fields, duplicate conflicting classes, and Java bytecode-version mismatches.

## Why LinkageLens exists

Java projects can compile successfully and still fail at startup or under production traffic with errors like:

```text
java.lang.NoSuchMethodError
java.lang.NoClassDefFoundError
java.lang.ClassNotFoundException
java.lang.NoSuchFieldError
```

These failures often come from dependency mediation, transitive dependencies, shading, fat JAR packaging, or upgrading one library while another still expects an older binary contract.

LinkageLens analyzes JVM bytecode directly so it can compare **what compiled code references** with **what the runtime classpath actually provides**.

## Quick start

Requirements: Java 17+.

```bash
./build.sh
java -jar dist/linkagelens.jar scan /path/to/classes-or-jars
```

Or scan a single JAR:

```bash
java -jar dist/linkagelens.jar scan app.jar
```

Generate SARIF for CI/code scanning:

```bash
java -jar dist/linkagelens.jar scan app.jar \
  --format sarif \
  --output linkagelens.sarif
```

Fail CI on HIGH findings:

```bash
java -jar dist/linkagelens.jar scan app.jar --fail-on HIGH
```

## v0.1 rules

| Rule | Severity | Detects |
| --- | --- | --- |
| `LL1001` | HIGH | Referenced class missing from the scanned runtime artifacts |
| `LL1002` | HIGH | Referenced method missing from the selected runtime class |
| `LL1003` | HIGH | Referenced field missing from the selected runtime class |
| `LL1004` | HIGH | Duplicate class definitions with different bytecode |
| `LL1005` | HIGH | Class compiled for a newer Java runtime than configured/available |

## Architecture

```text
Maven/Gradle build output / JARs
            │
            ▼
       Artifact discovery
            │
            ▼
      Class-file parser
            │
            ▼
      Symbol references
            │
            ▼
       Linkage graph
            │
      ┌─────┼─────┐
      ▼     ▼     ▼
   class  method field
      │     │     │
      └─────┼─────┘
            ▼
      Runtime provider
            │
            ▼
       mismatch?
            │
            ▼
         Finding
      ┌─────┼─────┐
      ▼     ▼     ▼
    text   JSON  SARIF
```

## Security

See [`SECURITY.md`](SECURITY.md) for private vulnerability reporting. The repository also includes Dependabot and OpenSSF Scorecard automation.

## Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md).

## License

MIT. See [`LICENSE`](LICENSE).
