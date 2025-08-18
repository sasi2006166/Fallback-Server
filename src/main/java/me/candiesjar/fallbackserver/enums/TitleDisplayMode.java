package me.candiesjar.fallbackserver.enums;

import lombok.Getter;

@Getter
public enum TitleDisplayMode {
    NORMAL(1), STATIC(1), PULSE(3);

    private final int period;

    TitleDisplayMode(int period) {
        this.period = period;
    }

    public static TitleDisplayMode fromString(String value) {
        return switch (value) {
            case "STATIC" -> STATIC;
            case "PULSE" -> PULSE;
            default -> NORMAL;
        };
    }
}