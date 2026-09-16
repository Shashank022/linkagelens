package io.github.shashank022.linkagelens.report;

import io.github.shashank022.linkagelens.model.Finding;
import java.util.List;

public interface Reporter {
    String render(List<Finding> findings, int classCount);
}
