package io.github.shashank022.linkagelens.report;

import io.github.shashank022.linkagelens.model.*;
import io.github.shashank022.linkagelens.util.Json;
import java.util.*;

public final class SarifReporter implements Reporter {
    @Override public String render(List<Finding> findings, int classCount) {
        Map<String, Finding> rules = new LinkedHashMap<>();
        for (Finding f: findings) rules.putIfAbsent(f.ruleId(), f);
        StringBuilder sb = new StringBuilder();
        sb.append("{\"version\":\"2.1.0\",\"$schema\":\"https://json.schemastore.org/sarif-2.1.0.json\",\"runs\":[{");
        sb.append("\"tool\":{\"driver\":{\"name\":\"LinkageLens\",\"version\":\"0.1.0\",\"rules\":[");
        int r=0;
        for (Finding f: rules.values()) {
            if (r++>0) sb.append(',');
            sb.append("{\"id\":").append(Json.q(f.ruleId())).append(",\"name\":").append(Json.q(f.title()))
              .append(",\"shortDescription\":{\"text\":").append(Json.q(f.title())).append("}}");
        }
        sb.append("]}},\"properties\":{\"classesScanned\":").append(classCount).append("},\"results\":[");
        for (int i=0;i<findings.size();i++) {
            Finding f=findings.get(i);
            if (i>0) sb.append(',');
            sb.append("{\"ruleId\":").append(Json.q(f.ruleId())).append(",\"level\":").append(Json.q(level(f.severity())))
              .append(",\"message\":{\"text\":").append(Json.q(f.message()+" Evidence: "+f.evidence()+" Suggested: "+f.recommendation())).append("},")
              .append("\"locations\":[{\"physicalLocation\":{\"artifactLocation\":{\"uri\":").append(Json.q(sanitize(f.location()))).append("}}}]}");
        }
        return sb.append("]}]}\n").toString();
    }

    private String level(Severity s) { return s.atLeast(Severity.HIGH) ? "error" : s.atLeast(Severity.MEDIUM) ? "warning" : "note"; }
    private String sanitize(String p) { return p.replace('\\','/').replace(" ", "%20"); }
}
