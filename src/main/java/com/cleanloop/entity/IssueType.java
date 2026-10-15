package com.cleanloop.entity;

public enum IssueType {
    OVERFLOWING_BIN("Overflowing Garbage Bins"),
    ILLEGAL_DUMPING("Illegal Dumping"),
    MISSED_COLLECTION("Missed Garbage Collection"),
    WASTE_ACCUMULATION("Waste Accumulation in Public Areas"),
    PLASTIC_WASTE("Plastic Waste"),
    ORGANIC_WASTE("Organic Waste"),
    PAPER_CARDBOARD("Paper and Cardboard Waste"),
    GLASS_WASTE("Glass Waste"),
    METAL_WASTE("Metal Waste"),
    OTHER("Other Waste Issue");

    private final String displayName;

    IssueType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
