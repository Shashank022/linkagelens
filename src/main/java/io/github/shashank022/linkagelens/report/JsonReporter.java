package io.github.shashank022.linkagelens.report;

import io.github.shashank022.linkagelens.model.Finding;
import io.github.shashank022.linkagelens.util.Json;
import java.util.*;

public final class JsonReporter implements Reporter {
    @Override public String render(List<Finding> findings, int classCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"tool\": \"LinkageLens\",\n  \"version\": \"0.1.0\",\n  \"classesScanned\": ")
          .append(classCount).append(",\n  \"findings\": [\n");
        for (int i=0;i<findings.size();i++) {
            Finding f=findings.get(i);
            sb.append("    {\"ruleId\":").append(Json.q(f.ruleId()))
              .append(",\"severity\":").append(Json.q(f.severity().name()))
              .append(",\"title\":").append(Json.q(f.title()))
              .append(",\"location\":").append(Json.q(f.location()))
              .append(",\"message\":").append(Json.q(f.message()))
              .append(",\"evidence\":").append(Json.q(f.evidence()))
              .append(",\"recommendation\":").append(Json.q(f.recommendation())).append("}");
            if (i+1<findings.size()) sb.append(',');
            sb.append('\n');
        }
        return sb.append("  ]\n}\n").toString();
    }
}
