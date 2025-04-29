package me.candiesjar.fallbackserver.objects.text;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.enums.Severity;

@Getter
@RequiredArgsConstructor
public class Diagnostic {
    private final Severity severity;
    private final String message;
}
