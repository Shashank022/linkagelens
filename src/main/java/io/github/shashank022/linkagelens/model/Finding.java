package io.github.shashank022.linkagelens.model;

public record Finding(
        String ruleId,
        Severity severity,
        String title,
        String location,
        String message,
        String evidence,
        String recommendation) {

    public String fingerprint() {
        return ruleId + "|" + location + "|" + evidence;
    }
}
