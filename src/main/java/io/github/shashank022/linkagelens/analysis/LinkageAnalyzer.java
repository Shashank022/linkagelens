package io.github.shashank022.linkagelens.analysis;

import io.github.shashank022.linkagelens.bytecode.*;
import io.github.shashank022.linkagelens.model.*;
import io.github.shashank022.linkagelens.scan.ClassDefinition;

import java.util.*;
import java.util.stream.Collectors;

public final class LinkageAnalyzer {
    private final PlatformTypes platform = new PlatformTypes();

    public List<Finding> analyze(List<ClassDefinition> definitions) {
        Map<String, List<ClassDefinition>> byName = definitions.stream()
                .collect(Collectors.groupingBy(d -> d.info().name(), LinkedHashMap::new, Collectors.toList()));

        List<Finding> findings = new ArrayList<>();
        detectConflictingDuplicates(byName, findings);
        detectNewerBytecode(definitions, findings);

        for (ClassDefinition caller : definitions) {
            detectMissingClasses(caller, byName, findings);
            detectMissingMembers(caller, byName, findings);
        }

        findings.sort(Comparator
                .comparing((Finding f) -> f.severity().rank()).reversed()
                .thenComparing(Finding::ruleId)
                .thenComparing(Finding::location)
                .thenComparing(Finding::evidence));
        return findings;
    }

    private void detectConflictingDuplicates(Map<String, List<ClassDefinition>> byName, List<Finding> out) {
        for (var e : byName.entrySet()) {
            List<ClassDefinition> defs = e.getValue();
            Set<String> hashes = defs.stream().map(ClassDefinition::sha256).collect(Collectors.toSet());
            if (defs.size() > 1 && hashes.size() > 1) {
                String locations = defs.stream().map(ClassDefinition::location).collect(Collectors.joining("\n  - ", "  - ", ""));
                out.add(new Finding(
                        "LL1004", Severity.HIGH, "Conflicting duplicate class",
                        defs.get(0).location(),
                        "The same binary class name is present with different bytecode in more than one scanned artifact. The JVM will load one definition according to classpath/classloader order, which can make behavior environment-dependent.",
                        e.getKey().replace('/', '.') + " found at:\n" + locations,
                        "Remove the unintended duplicate, align dependency versions, or make classpath precedence explicit. If shading is intentional, relocate packages to avoid collisions."));
            }
        }
    }

    private void detectNewerBytecode(List<ClassDefinition> defs, List<Finding> out) {
        int runtimeMajor = Runtime.version().feature() + 44;
        for (ClassDefinition def : defs) {
            int major = def.info().majorVersion();
            if (major > runtimeMajor) {
                out.add(new Finding(
                        "LL1005", Severity.HIGH, "Class requires a newer Java runtime",
                        def.location(),
                        "This class file was compiled for a newer Java release than the JVM currently running LinkageLens. Deploying it to this JVM would fail with UnsupportedClassVersionError.",
                        def.info().dottedName() + " has class-file major " + major + "; current runtime supports up to " + runtimeMajor,
                        "Run the application on a compatible/newer JDK or compile the dependency/application for the deployment Java version."));
            }
        }
    }

    private void detectMissingClasses(ClassDefinition caller, Map<String, List<ClassDefinition>> byName, List<Finding> out) {
        for (String ref : caller.info().referencedClasses()) {
            if (ref == null || ref.isBlank() || ref.startsWith("[")) continue;
            if (!byName.containsKey(ref) && !platform.isPlatform(ref)) {
                out.add(new Finding(
                        "LL1001", Severity.HIGH, "Referenced class is missing",
                        caller.location(),
                        "Compiled bytecode references a class that is not present in the scanned runtime artifacts and is not provided by the current Java platform.",
                        caller.info().dottedName() + " -> " + ref.replace('/', '.'),
                        "Add the missing runtime dependency, correct dependency scope/exclusions, or scan again with the complete runtime classpath."));
            }
        }
    }

    private void detectMissingMembers(ClassDefinition caller, Map<String, List<ClassDefinition>> byName, List<Finding> out) {
        for (MemberRef ref : caller.info().memberRefs()) {
            if (platform.isPlatform(ref.owner())) continue;
            List<ClassDefinition> owners = byName.get(ref.owner());
            if (owners == null || owners.isEmpty()) continue;

            boolean found = owners.stream().anyMatch(d -> memberExists(d.info(), ref, byName, new HashSet<>()));
            if (found) continue;

            String id = ref.kind() == MemberRef.Kind.FIELD ? "LL1003" : "LL1002";
            String title = ref.kind() == MemberRef.Kind.FIELD ? "Referenced field is missing" : "Referenced method is missing";
            String error = ref.kind() == MemberRef.Kind.FIELD ? "NoSuchFieldError" : "NoSuchMethodError";
            out.add(new Finding(
                    id, Severity.HIGH, title,
                    caller.location(),
                    "The caller bytecode contains a symbolic member reference, but none of the scanned definitions for the target class hierarchy provides the exact name+descriptor. At runtime this can surface as " + error + ".",
                    caller.info().dottedName() + " -> " + ref.display(),
                    "Align the runtime dependency with the version used at compile time, remove the conflicting older JAR, or update the caller to an API available in the selected runtime dependency."));
        }
    }

    private boolean memberExists(ClassInfo info, MemberRef ref, Map<String, List<ClassDefinition>> byName, Set<String> seen) {
        if (info == null || !seen.add(info.name())) return false;
        Set<MemberKey> members = ref.kind() == MemberRef.Kind.FIELD ? info.fields() : info.methods();
        if (members.contains(ref.key())) return true;
        if (searchParents(info.superName(), ref, byName, seen)) return true;
        for (String iface : info.interfaces()) if (searchParents(iface, ref, byName, seen)) return true;
        return false;
    }

    private boolean searchParents(String parent, MemberRef ref, Map<String, List<ClassDefinition>> byName, Set<String> seen) {
        if (parent == null) return false;
        if (platform.isPlatform(parent)) return platform.memberExists(parent, ref);
        List<ClassDefinition> defs = byName.get(parent);
        if (defs == null) return false;
        for (ClassDefinition def : defs) if (memberExists(def.info(), ref, byName, seen)) return true;
        return false;
    }
}
