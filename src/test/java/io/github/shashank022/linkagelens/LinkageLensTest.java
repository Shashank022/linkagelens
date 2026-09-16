package io.github.shashank022.linkagelens;

import io.github.shashank022.linkagelens.analysis.LinkageAnalyzer;
import io.github.shashank022.linkagelens.model.Finding;
import io.github.shashank022.linkagelens.scan.ArtifactScanner;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.nio.file.*;
import java.util.*;

public final class LinkageLensTest {
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("linkagelens-test");
        try {
            Path v1Src = root.resolve("v1src/demo/Api.java");
            Path v2Src = root.resolve("v2src/demo/Api.java");
            Path consumerSrc = root.resolve("consumersrc/demo/Consumer.java");
            Files.createDirectories(v1Src.getParent());
            Files.createDirectories(v2Src.getParent());
            Files.createDirectories(consumerSrc.getParent());

            Files.writeString(v1Src, """
                    package demo;
                    public class Api {
                        public int value = 1;
                        public String oldCall() { return "old"; }
                    }
                    """);
            Files.writeString(v2Src, """
                    package demo;
                    public class Api {
                        public int value = 1;
                        public int newValue = 2;
                        public String oldCall() { return "old"; }
                        public String newCall() { return "new"; }
                    }
                    """);
            Files.writeString(consumerSrc, """
                    package demo;
                    public class Consumer {
                        public String call() {
                            Api api = new Api();
                            return api.newCall() + api.newValue;
                        }
                    }
                    """);

            Path v1 = root.resolve("v1");
            Path v2 = root.resolve("v2");
            Path consumer = root.resolve("consumer");
            compile(v1, null, v1Src);
            compile(v2, null, v2Src);
            compile(consumer, v2, consumerSrc);

            ArtifactScanner scanner = new ArtifactScanner();
            LinkageAnalyzer analyzer = new LinkageAnalyzer();

            List<Finding> mismatch = analyzer.analyze(scanner.scan(List.of(consumer, v1)));
            assertRule(mismatch, "LL1002");
            assertRule(mismatch, "LL1003");

            List<Finding> missing = analyzer.analyze(scanner.scan(List.of(consumer)));
            assertRule(missing, "LL1001");

            List<Finding> duplicate = analyzer.analyze(scanner.scan(List.of(v1, v2)));
            assertRule(duplicate, "LL1004");

            List<Finding> compatible = analyzer.analyze(scanner.scan(List.of(consumer, v2)));
            if (compatible.stream().anyMatch(f -> f.ruleId().equals("LL1001") || f.ruleId().equals("LL1002") || f.ruleId().equals("LL1003"))) {
                throw new AssertionError("Expected compatible classpath but got: " + compatible);
            }

            System.out.println("LinkageLens tests passed");
        } finally {
            delete(root);
        }
    }

    private static void compile(Path out, Path cp, Path source) {
        try { Files.createDirectories(out); } catch (Exception e) { throw new RuntimeException(e); }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<String> args = new ArrayList<>(List.of("--release", "17", "-d", out.toString()));
        if (cp != null) args.addAll(List.of("-classpath", cp.toString()));
        args.add(source.toString());
        int code = compiler.run(null, null, null, args.toArray(String[]::new));
        if (code != 0) throw new AssertionError("Compilation failed for " + source + " exit=" + code);
    }

    private static void assertRule(List<Finding> findings, String id) {
        if (findings.stream().noneMatch(f -> f.ruleId().equals(id))) throw new AssertionError("Expected " + id + " but got " + findings);
    }

    private static void delete(Path root) throws Exception {
        if (!Files.exists(root)) return;
        try (var walk = Files.walk(root)) {
            for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(p);
        }
    }
}
