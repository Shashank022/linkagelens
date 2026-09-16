package io.github.shashank022.linkagelens.report;

import io.github.shashank022.linkagelens.model.Finding;
import java.util.*;

public final class TextReporter implements Reporter {
    @Override public String render(List<Finding> findings, int classCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("LinkageLens 0.1.0\n");
        sb.append("Scanned ").append(classCount).append(" class definitions\n");
        sb.append("Findings: ").append(findings.size()).append("\n\n");
        if (findings.isEmpty()) {
            sb.append("No linkage problems detected in the scanned artifact set.\n");
            return sb.toString();
        }
        for (Finding f : findings) {
            sb.append(f.severity()).append("  ").append(f.ruleId()).append("  ").append(f.title()).append("\n");
            sb.append("Location: ").append(f.location()).append("\n\n");
            sb.append(f.message()).append("\n\nEvidence:\n").append(f.evidence()).append("\n\nSuggested direction:\n")
                    .append(f.recommendation()).append("\n\n");
            sb.append("------------------------------------------------------------\n\n");
        }
        return sb.toString();
    }
}
