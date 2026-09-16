# Reproducing a binary mismatch

The automated integration test under `src/test` creates this scenario dynamically:

```text
Api v1                Api v2
------                ------
oldCall()             oldCall()
                      newCall()
                      newValue

Consumer compiles against Api v2
Runtime scan contains Consumer + Api v1
```

LinkageLens then detects:

```text
LL1002 missing newCall()
LL1003 missing newValue
```

Run it with:

```bash
./build.sh
```
