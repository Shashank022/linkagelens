package io.github.shashank022.linkagelens.maven.extension;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;

public final class LinkageLensLifecycleParticipant extends AbstractMavenLifecycleParticipant {
    private static final String GROUP_ID = "io.linkagelens";
    private static final String ARTIFACT_ID = "linkagelens-maven-plugin";
    private static final String VERSION = "0.2.0";

    @Override public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        if (Boolean.parseBoolean(session.getUserProperties().getProperty("linkagelens.extension.disable", "false"))) return;
        for (MavenProject project : session.getProjects()) {
            if ("pom".equals(project.getPackaging())) continue;
            if (Boolean.parseBoolean(project.getProperties().getProperty("linkagelens.skip", "false"))) continue;
            inject(project);
        }
    }

    private void inject(MavenProject project) {
        Plugin plugin = project.getBuildPlugins().stream().filter(p -> GROUP_ID.equals(p.getGroupId()) && ARTIFACT_ID.equals(p.getArtifactId())).findFirst().orElse(null);
        if (plugin == null) { plugin = new Plugin(); plugin.setGroupId(GROUP_ID); plugin.setArtifactId(ARTIFACT_ID); plugin.setVersion(VERSION); project.getBuild().addPlugin(plugin); }
        if (plugin.getExecutions().stream().anyMatch(e -> e.getGoals().contains("check"))) return;
        PluginExecution execution = new PluginExecution(); execution.setId("linkagelens-auto-verify"); execution.setPhase("verify"); execution.addGoal("check"); plugin.addExecution(execution);
    }
}
