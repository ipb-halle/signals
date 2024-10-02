package de.ipb_halle.signals.entity;

public enum IncludedTypes {
    EXPERIMENT("experiment"),
    JOURNAL("journal"),
    LOCATION("location"),
    REQUEST("request"),
    SAMPLE("sample"),
    TEXT("text"),
    TASK("task"),
    WORKSHEET("worksheet");

    private final String includedType;

    IncludedTypes(String includedType) {
        this.includedType = includedType;
    }

    public String getIncludedType() {
        return includedType;
    }
}
