package me.candiesjar.fallbackserver.objects.text;

public record Placeholder(String key, String value) {
    public Placeholder(String key, String value) {
        this.key = "%" + key + "%";
        this.value = value;
    }
}
