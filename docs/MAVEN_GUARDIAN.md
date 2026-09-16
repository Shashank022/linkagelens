# Maven Guardian

LinkageLens Maven Guardian makes linkage analysis part of the path developers already use instead of a command they must remember.

## Coordinates

```text
io.linkagelens:linkagelens-maven-plugin:0.2.0
io.linkagelens:linkagelens-maven-extension:0.2.0
```

## Transparent repository mode

Check this into the consuming repository at `.mvn/extensions.xml`:

```xml
<extensions>
  <extension>
    <groupId>io.linkagelens</groupId>
    <artifactId>linkagelens-maven-extension</artifactId>
    <version>0.2.0</version>
  </extension>
</extensions>
```

Then run `mvn verify`. The extension injects the LinkageLens Maven plugin automatically.

## Explicit plugin mode

```xml
<plugin>
  <groupId>io.linkagelens</groupId>
  <artifactId>linkagelens-maven-plugin</artifactId>
  <version>0.2.0</version>
  <executions><execution><phase>verify</phase><goals><goal>check</goal></goals></execution></executions>
</plugin>
```

## Controls

```bash
mvn verify -Dlinkagelens.failOn=NONE
mvn verify -Dlinkagelens.format=json
mvn verify -Dlinkagelens.format=sarif
mvn verify -Dlinkagelens.skip=true
mvn verify -Dlinkagelens.extension.disable=true
```
