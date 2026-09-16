# Rule Catalog

## LL1001 — Referenced class is missing

A compiled class references a non-platform class not found in the supplied runtime artifact set.

Typical causes: dependency scope mistakes, exclusions, packaging omissions, missing runtime plugin/provider.

## LL1002 — Referenced method is missing

A caller references an exact method name+descriptor absent from scanned definitions of the owner hierarchy.

Typical runtime symptom: `NoSuchMethodError`.

## LL1003 — Referenced field is missing

A caller references an exact field name+descriptor absent from scanned definitions of the owner hierarchy.

Typical runtime symptom: `NoSuchFieldError`.

## LL1004 — Conflicting duplicate class

The same binary class name appears more than once with different bytecode hashes.

Typical risk: classpath/classloader order decides which implementation wins.

## LL1005 — Class requires a newer Java runtime

A scanned class-file major version exceeds the Java runtime executing LinkageLens.

Typical runtime symptom: `UnsupportedClassVersionError`.
