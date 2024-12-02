package me.candiesjar.fallbackserver.enums;

import lombok.Getter;

@Getter
public enum TitleMode {
    NORMAL(1), STATIC(1), PULSE(3);

    private final int period;

    TitleMode(int period) {
        this.period = period;
    }

    public static TitleMode fromString(String value) {
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