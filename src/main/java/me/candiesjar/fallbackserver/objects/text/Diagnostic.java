package me.candiesjar.fallbackserver.objects.text;

import me.candiesjar.fallbackserver.enums.Severity;

public record Diagnostic(Severity severity, String message) {
}
