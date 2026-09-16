package io.github.shashank022.linkagelens.model;

public enum Severity {
    INFO(0), LOW(1), MEDIUM(2), HIGH(3), CRITICAL(4);

    private final int rank;
    Severity(int rank) { this.rank = rank; }
    public int rank() { return rank; }
    public boolean atLeast(Severity other) { return this.rank >= other.rank; }
    public static Severity parse(String value) {
        return Severity.valueOf(value.trim().toUpperCase());
    }
}
