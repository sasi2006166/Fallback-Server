package me.candiesjar.fallbackserver.enums;

import lombok.Getter;
import me.candiesjar.fallbackserver.handlers.ErrorHandler;

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
            case "NORMAL":
                return NORMAL;
            default:
                ErrorHandler.add(Severity.WARNING, "[RECONNECT TITLE] Invalid title display mode: " + value);
                return NORMAL;
        }
    }
}