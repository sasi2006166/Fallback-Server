package me.candiesjar.fallbackserver.objects;

import lombok.Getter;

@Getter
public class Placeholder {
    private final String key;
    private final String value;

    public Placeholder(String key, String value) {
        this.key = "%" + key + "%";
        this.value = value;
    }
}
