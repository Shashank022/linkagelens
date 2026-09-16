package io.github.shashank022.linkagelens.scan;

import io.github.shashank022.linkagelens.bytecode.ClassFileParser;
import io.github.shashank022.linkagelens.bytecode.ClassInfo;
import io.github.shashank022.linkagelens.util.Hashing;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

public final class ArtifactScanner {
    public List<ClassDefinition> scan(List<Path> roots) throws IOException {
        List<ClassDefinition> defs = new ArrayList<>();
        Set<Path> visited = new HashSet<>();
        for (Path root : roots) scanPath(root.toAbsolutePath().normalize(), defs, visited);
        return defs;
    }

    private void scanPath(Path path, List<ClassDefinition> defs, Set<Path> visited) throws IOException {
        if (!Files.exists(path)) throw new FileNotFoundException("Path does not exist: " + path);
        if (!visited.add(path)) return;

        if (Files.isDirectory(path)) {
            try (var stream = Files.walk(path)) {
                for (Path p : stream.filter(Files::isRegularFile).sorted().toList()) {
                    String name = p.getFileName().toString();
                    if (name.endsWith(".class")) scanClass(Files.readAllBytes(p), p.toString(), defs);
                    else if (name.endsWith(".jar")) scanJar(Files.newInputStream(p), p.toString(), defs, 0);
                }
            }
        } else if (path.toString().endsWith(".class")) {
            scanClass(Files.readAllBytes(path), path.toString(), defs);
        } else if (path.toString().endsWith(".jar")) {
            scanJar(Files.newInputStream(path), path.toString(), defs, 0);
        } else {
            throw new IOException("Unsupported scan target (expected directory, .class, or .jar): " + path);
        }
    }

    private void scanJar(InputStream raw, String location, List<ClassDefinition> defs, int depth) throws IOException {
        if (depth > 4) return;
        try (JarInputStream jar = new JarInputStream(new BufferedInputStream(raw))) {
            JarEntry entry;
            while ((entry = jar.getNextJarEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (name.startsWith("META-INF/versions/")) continue;
                byte[] bytes = jar.readAllBytes();
                if (name.endsWith(".class")) scanClass(bytes, location + "!/" + name, defs);
                else if (name.endsWith(".jar")) scanJar(new ByteArrayInputStream(bytes), location + "!/" + name, defs, depth + 1);
            }
        }
    }

    private void scanClass(byte[] bytes, String location, List<ClassDefinition> defs) throws IOException {
        try {
            ClassInfo info = ClassFileParser.parse(bytes);
            defs.add(new ClassDefinition(info, location, Hashing.sha256(bytes)));
        } catch (IOException e) {
            throw new IOException("Failed to parse " + location + ": " + e.getMessage(), e);
        }
    }
}
