package io.github.shashank022.linkagelens.maven;

import io.github.shashank022.linkagelens.analysis.LinkageAnalyzer;
import io.github.shashank022.linkagelens.model.Finding;
import io.github.shashank022.linkagelens.model.Severity;
import io.github.shashank022.linkagelens.report.JsonReporter;
import io.github.shashank022.linkagelens.report.Reporter;
import io.github.shashank022.linkagelens.report.SarifReporter;
import io.github.shashank022.linkagelens.report.TextReporter;
import io.github.shashank022.linkagelens.scan.ArtifactScanner;
import io.github.shashank022.linkagelens.scan.ClassDefinition;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY, requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public final class LinkageLensMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true) private MavenProject project;
    @Parameter(property = "linkagelens.skip", defaultValue = "false") private boolean skip;
    @Parameter(property = "linkagelens.failOn", defaultValue = "HIGH") private String failOn;
    @Parameter(property = "linkagelens.format", defaultValue = "text") private String format;
    @Parameter(property = "linkagelens.includeDependencies", defaultValue = "true") private boolean includeDependencies;
    @Parameter(defaultValue = "${project.build.directory}/linkagelens", readonly = true) private File reportDirectory;

    @Override public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip || "pom".equals(project.getPackaging())) return;
        try {
            List<Path> roots = runtimeRoots();
            if (roots.isEmpty()) { getLog().info("LinkageLens: no compiled or packaged artifacts found; skipping."); return; }
            getLog().info("LinkageLens Maven Guardian — scanning " + project.getGroupId() + ":" + project.getArtifactId());
            List<ClassDefinition> definitions = new ArtifactScanner().scan(roots);
            List<Finding> findings = new LinkageAnalyzer().analyze(definitions);
            Reporter reporter = switch (format.toLowerCase(Locale.ROOT)) {
                case "text" -> new TextReporter();
                case "json" -> new JsonReporter();
                case "sarif" -> new SarifReporter();
                default -> throw new MojoExecutionException("Unsupported linkagelens.format: " + format);
            };
            String rendered = reporter.render(findings, definitions.size());
            if ("text".equalsIgnoreCase(format)) {
                for (String line : rendered.split("\\R")) if (!line.isBlank()) getLog().info(line);
            } else {
                Files.createDirectories(reportDirectory.toPath());
                String ext = format.equalsIgnoreCase("sarif") ? "sarif" : "json";
                Path report = reportDirectory.toPath().resolve("linkagelens." + ext);
                Files.writeString(report, rendered);
                getLog().info("LinkageLens report: " + report);
            }
            Severity threshold = parseThreshold(failOn);
            if (threshold != null && findings.stream().anyMatch(f -> f.severity().atLeast(threshold))) {
                long count = findings.stream().filter(f -> f.severity().atLeast(threshold)).count();
                throw new MojoFailureException("LinkageLens found " + count + " finding(s) at or above " + threshold + ".");
            }
        } catch (MojoFailureException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("LinkageLens analysis failed: " + e.getMessage(), e);
        }
    }

    private List<Path> runtimeRoots() throws DependencyResolutionRequiredException {
        Set<Path> roots = new LinkedHashSet<>();
        File artifact = project.getArtifact() == null ? null : project.getArtifact().getFile();
        if (artifact != null && artifact.isFile() && artifact.getName().endsWith(".jar")) {
            roots.add(artifact.toPath().toAbsolutePath().normalize());
        } else if (project.getBuild().getOutputDirectory() != null) {
            Path p = Path.of(project.getBuild().getOutputDirectory()).toAbsolutePath().normalize();
            if (Files.exists(p)) roots.add(p);
        }
        if (includeDependencies) {
            for (String element : project.getRuntimeClasspathElements()) {
                Path p = Path.of(element).toAbsolutePath().normalize();
                if (Files.exists(p)) roots.add(p);
            }
        }
        return new ArrayList<>(roots);
    }

    private Severity parseThreshold(String value) throws MojoExecutionException {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("NONE") || value.equalsIgnoreCase("OFF")) return null;
        try {
            return Severity.parse(value);
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("Invalid linkagelens.failOn: " + value);
        }
    }
}
