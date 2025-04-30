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
        switch (value) {
            case "STATIC":
                return STATIC;
            case "PULSE":
                return PULSE;
            default:
                return NORMAL;
        }
    }
}