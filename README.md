# LinkageLens

> **Find Java dependency problems before the JVM does.**

LinkageLens is an open-source Java bytecode/linkage analyzer that checks the classes and JARs you are actually shipping for runtime binary-compatibility problems such as missing classes, missing methods, missing fields, conflicting duplicate classes, and Java bytecode-version mismatches.

Java projects can compile successfully and still fail later with errors such as:

```text
java.lang.NoSuchMethodError
java.lang.NoClassDefFoundError
java.lang.NoSuchFieldError
java.lang.UnsupportedClassVersionError
```

LinkageLens analyzes JVM bytecode directly so it can compare **what compiled code references** with **what the runtime classpath actually provides**.

## Maven coordinates

LinkageLens uses the product-owned Maven namespace:

```text
io.linkagelens
```

Consumer-facing artifacts do not use a maintainer username in their Maven coordinates.

## Quick start — CLI

Requirements: Java 17+.

```bash
./build.sh
java -jar dist/linkagelens.jar scan /path/to/classes-or-jars
```

Generate SARIF:

```bash
java -jar dist/linkagelens.jar scan app.jar \
  --format sarif \
  --output linkagelens.sarif
```

Fail CI on HIGH findings:

```bash
java -jar dist/linkagelens.jar scan app.jar --fail-on HIGH
```

## Maven Guardian — recommended developer layer

The CLI is useful for diagnostics, but developers should not have to remember another command. **Maven Guardian puts LinkageLens directly in the Maven lifecycle.**

```text
Developer
   │
   └── mvn verify
          │
          ├── compile
          ├── test
          ├── package
          └── LinkageLens check   ← automatic
                   │
                   └── resolved runtime classpath
```

### Transparent repository mode

After the LinkageLens Maven artifacts are installed/published, add this once to the consuming repository as `.mvn/extensions.xml`:

```xml
<extensions>
  <extension>
    <groupId>io.linkagelens</groupId>
    <artifactId>linkagelens-maven-extension</artifactId>
    <version>0.2.0-SNAPSHOT</version>
  </extension>
</extensions>
```

Then developers continue using exactly what they already use:

```bash
mvn verify
```

No LinkageLens CLI command and no IDE plugin are required. The extension injects the `linkagelens:check` goal into `verify` for Java modules.

### Explicit plugin mode

Teams that prefer explicit POM configuration can bind the Maven plugin directly:

```xml
<plugin>
  <groupId>io.linkagelens</groupId>
  <artifactId>linkagelens-maven-plugin</artifactId>
  <version>0.2.0-SNAPSHOT</version>
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

See [`docs/MAVEN_GUARDIAN.md`](docs/MAVEN_GUARDIAN.md).

## Core rules

| Rule | Severity | Detects |
| --- | --- | --- |
| `LL1001` | HIGH | Referenced class missing from the scanned runtime artifacts |
| `LL1002` | HIGH | Referenced method missing from the selected runtime class |
| `LL1003` | HIGH | Referenced field missing from the selected runtime class |
| `LL1004` | HIGH | Duplicate class definitions with different bytecode |
| `LL1005` | HIGH | Class compiled for a newer Java runtime than available |

## Architecture direction

```text
                    LinkageLens
                        │
          ┌─────────────┼─────────────┐
          ▼             ▼             ▼
         CLI       Maven Guardian   PR Guardian
                         │             │
                         └──────┬──────┘
                                ▼
                        Bytecode engine
                                │
                     runtime linkage graph
                                │
                     class / method / field
```

The Maven layer is the first step toward making LinkageLens a guardrail developers naturally pass through. The next major step is PR-aware base-vs-head analysis so dependency upgrades can be evaluated automatically before merge.

## Security

See [`SECURITY.md`](SECURITY.md) for private vulnerability reporting. The repository also includes Dependabot and OpenSSF Scorecard automation.

## Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md).

## License

MIT. See [`LICENSE`](LICENSE).
