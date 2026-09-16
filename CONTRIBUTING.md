# Contributing to LinkageLens

Thanks for helping improve LinkageLens.

## Development requirements

- JDK 17+
- Bash on macOS/Linux or PowerShell on Windows

Build and run tests:

```bash
./build.sh
```

The integration test dynamically compiles two incompatible versions of a Java API and verifies that LinkageLens detects missing methods, missing fields, missing classes, and conflicting duplicate classes.

## Adding a rule

A rule should be based on concrete binary/JVM evidence and should minimize speculation.

Every new rule should include:

- a stable rule ID;
- severity rationale;
- positive test fixture;
- compatible/negative fixture when possible;
- clear explanation;
- actionable remediation guidance.

## Pull requests

Please keep PRs focused. Explain the binary-linkage scenario being solved and include tests that reproduce it.

## Security reports

Do not submit exploit details in public issues. Follow [`SECURITY.md`](SECURITY.md).
