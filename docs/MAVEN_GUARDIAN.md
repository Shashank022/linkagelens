# Maven Guardian

LinkageLens Maven Guardian makes linkage analysis part of the path developers already use instead of a command they must remember.

## Where it sits

```text
mvn verify
   │
   ├── compile
   ├── test
   ├── package
   └── LinkageLens check
            │
            ├── packaged application classes
            └── Maven runtime classpath
```

The plugin exposes `linkagelens:check` and defaults to the Maven `verify` phase. The companion Maven core extension injects that execution automatically for every non-`pom` module.

## Coordinates

All Maven Guardian artifacts use the LinkageLens product namespace:

```text
io.linkagelens
```

## Explicit plugin mode

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

## Transparent repository mode

Check this into the consuming repository at `.mvn/extensions.xml`:

```xml
<extensions>
  <extension>
    <groupId>io.linkagelens</groupId>
    <artifactId>linkagelens-maven-extension</artifactId>
    <version>0.2.0-SNAPSHOT</version>
  </extension>
</extensions>
```

Now developers simply run:

```bash
mvn verify
```

No IDE plugin and no separate LinkageLens command are required.

## Controls

```bash
# Report without failing
mvn verify -Dlinkagelens.failOn=NONE

# JSON report under target/linkagelens/
mvn verify -Dlinkagelens.format=json

# SARIF report
mvn verify -Dlinkagelens.format=sarif

# Skip LinkageLens for a build
mvn verify -Dlinkagelens.skip=true

# Disable only automatic extension injection
mvn verify -Dlinkagelens.extension.disable=true
```

## Rollout model

For an individual project, use the plugin directly. For a team, put it in a shared parent POM. For a repository-level guardrail that developers automatically pass through, use the core extension in `.mvn/extensions.xml`.

The long-term direction is to pair this with PR-level base-vs-head analysis so the same rule engine protects local builds, CI, and pull requests.
