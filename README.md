# LinkageLens

> **Find Java dependency problems before the JVM does.**

LinkageLens is an open-source Java bytecode/linkage analyzer that checks the classes and JARs you are actually shipping for runtime binary-compatibility problems such as missing classes, missing methods, missing fields, conflicting duplicate classes, and Java bytecode-version mismatches.

## Maven coordinates

LinkageLens uses the product-owned Maven namespace `io.linkagelens`.

### Maven Guardian extension

```xml
<extensions>
  <extension>
    <groupId>io.linkagelens</groupId>
    <artifactId>linkagelens-maven-extension</artifactId>
    <version>0.2.0</version>
  </extension>
</extensions>
```

Then developers simply run:

```bash
mvn verify
```

### Explicit Maven plugin

```xml
<plugin>
  <groupId>io.linkagelens</groupId>
  <artifactId>linkagelens-maven-plugin</artifactId>
  <version>0.2.0</version>
  <executions>
    <execution>
      <phase>verify</phase>
      <goals><goal>check</goal></goals>
    </execution>
  </executions>
</plugin>
```

Useful controls:

```bash
mvn verify -Dlinkagelens.failOn=NONE
mvn verify -Dlinkagelens.format=json
mvn verify -Dlinkagelens.format=sarif
mvn verify -Dlinkagelens.skip=true
mvn verify -Dlinkagelens.extension.disable=true
```

## CLI

Requirements: Java 17+.

```bash
./build.sh
java -jar dist/linkagelens.jar scan /path/to/classes-or-jars
```

Generate SARIF:

```bash
java -jar dist/linkagelens.jar scan app.jar --format sarif --output linkagelens.sarif
```

## Core rules

| Rule | Severity | Detects |
| --- | --- | --- |
| `LL1001` | HIGH | Referenced class missing from the scanned runtime artifacts |
| `LL1002` | HIGH | Referenced method missing from the selected runtime class |
| `LL1003` | HIGH | Referenced field missing from the selected runtime class |
| `LL1004` | HIGH | Duplicate class definitions with different bytecode |
| `LL1005` | HIGH | Class compiled for a newer Java runtime than available |

See [`docs/MAVEN_GUARDIAN.md`](docs/MAVEN_GUARDIAN.md), [`SECURITY.md`](SECURITY.md), and [`CONTRIBUTING.md`](CONTRIBUTING.md).

## License

MIT. See [`LICENSE`](LICENSE).
