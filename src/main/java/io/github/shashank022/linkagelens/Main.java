package io.github.shashank022.linkagelens;

import io.github.shashank022.linkagelens.analysis.LinkageAnalyzer;
import io.github.shashank022.linkagelens.model.*;
import io.github.shashank022.linkagelens.report.*;
import io.github.shashank022.linkagelens.scan.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class Main {
    private static final String VERSION = "0.1.0";

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || isHelp(args[0])) { usage(); return; }
        switch (args[0]) {
            case "scan" -> runScan(Arrays.copyOfRange(args, 1, args.length));
            case "rules" -> printRules();
            case "explain" -> explain(args);
            case "version", "--version", "-v" -> System.out.println("LinkageLens " + VERSION);
            default -> { System.err.println("Unknown command: " + args[0]); usage(); System.exit(1); }
        }
    }

    private static void runScan(String[] args) throws Exception {
        if (args.length == 0) throw new IllegalArgumentException("scan requires a target path");
        List<Path> paths = new ArrayList<>();
        String format = "text";
        Path output = null;
        Severity failOn = null;

        paths.add(Paths.get(args[0]));
        for (int i=1;i<args.length;i++) {
            switch (args[i]) {
                case "--classpath" -> {
                    requireArg(args, i, "--classpath");
                    for (String p : args[++i].split(java.io.File.pathSeparator)) if (!p.isBlank()) paths.add(Paths.get(p));
                }
                case "--classpath-file" -> {
                    requireArg(args, i, "--classpath-file");
                    String content = Files.readString(Paths.get(args[++i])).trim();
                    if (!content.isBlank()) for (String p : content.split(java.io.File.pathSeparator)) if (!p.isBlank()) paths.add(Paths.get(p));
                }
                case "--format" -> { requireArg(args,i,"--format"); format=args[++i].toLowerCase(); }
                case "--output" -> { requireArg(args,i,"--output"); output=Paths.get(args[++i]); }
                case "--fail-on" -> { requireArg(args,i,"--fail-on"); failOn=Severity.parse(args[++i]); }
                default -> throw new IllegalArgumentException("Unknown scan option: " + args[i]);
            }
        }

        ArtifactScanner scanner = new ArtifactScanner();
        List<ClassDefinition> defs = scanner.scan(paths);
        List<Finding> findings = new LinkageAnalyzer().analyze(defs);
        Reporter reporter = switch (format) {
            case "text" -> new TextReporter();
            case "json" -> new JsonReporter();
            case "sarif" -> new SarifReporter();
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
        String rendered = reporter.render(findings, defs.size());
        if (output == null) System.out.print(rendered);
        else {
            if (output.toAbsolutePath().getParent()!=null) Files.createDirectories(output.toAbsolutePath().getParent());
            Files.writeString(output, rendered);
            System.out.println("Wrote " + format + " report to " + output);
            System.out.println("Scanned " + defs.size() + " class definitions; findings: " + findings.size());
        }

        if (failOn != null) {
            final Severity threshold = failOn;
            boolean fail = findings.stream().anyMatch(f -> f.severity().atLeast(threshold));
            if (fail) System.exit(2);
        }
    }

    private static void requireArg(String[] args, int index, String option) {
        if (index+1 >= args.length) throw new IllegalArgumentException(option + " requires a value");
    }

    private static void printRules() {
        System.out.println("LL1001 HIGH  Referenced class is missing");
        System.out.println("LL1002 HIGH  Referenced method is missing");
        System.out.println("LL1003 HIGH  Referenced field is missing");
        System.out.println("LL1004 HIGH  Conflicting duplicate class");
        System.out.println("LL1005 HIGH  Class requires a newer Java runtime");
    }

    private static void explain(String[] args) {
        if (args.length < 2) throw new IllegalArgumentException("explain requires a rule ID");
        String text = switch (args[1].toUpperCase()) {
            case "LL1001" -> "LL1001: A caller references a class that is absent from the scanned runtime artifact set. This is a common path to NoClassDefFoundError/ClassNotFoundException when dependency scope, exclusions, packaging, or deployment classpaths differ.";
            case "LL1002" -> "LL1002: Bytecode references an exact method name+descriptor that is not provided by the scanned target class hierarchy. A version mismatch can surface as NoSuchMethodError.";
            case "LL1003" -> "LL1003: Bytecode references an exact field name+descriptor that is not provided by the scanned target class hierarchy. A version mismatch can surface as NoSuchFieldError.";
            case "LL1004" -> "LL1004: The same class name exists more than once with different bytecode. Which definition wins depends on classpath/classloader order and can change behavior across environments.";
            case "LL1005" -> "LL1005: A class file targets a newer Java release than the current JVM. Deploying it to this JVM can fail with UnsupportedClassVersionError.";
            default -> "Unknown rule: " + args[1];
        };
        System.out.println(text);
    }

    private static boolean isHelp(String s) { return s.equals("help") || s.equals("--help") || s.equals("-h"); }

    private static void usage() {
        System.out.println("""
                LinkageLens 0.1.0 — Find Java dependency problems before the JVM does.

                Usage:
                  java -jar linkagelens.jar scan <jar|class-dir|directory> [options]
                  java -jar linkagelens.jar rules
                  java -jar linkagelens.jar explain <RULE_ID>
                  java -jar linkagelens.jar version

                Scan options:
                  --classpath <paths>       Additional runtime paths separated by the OS path separator
                  --classpath-file <file>   File containing an OS-separated runtime classpath
                  --format text|json|sarif  Output format (default: text)
                  --output <file>           Write report to a file
                  --fail-on <severity>      Exit 2 when finding severity reaches threshold

                Examples:
                  java -jar dist/linkagelens.jar scan target/my-service.jar
                  java -jar dist/linkagelens.jar scan target/classes --classpath-file target/runtime-classpath.txt
                  java -jar dist/linkagelens.jar scan app.jar --format sarif --output linkagelens.sarif --fail-on HIGH
                """);
    }
}
