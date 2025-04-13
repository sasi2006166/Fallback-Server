package me.candiesjar.fallbackserver.handlers;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.objects.Diagnostic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@UtilityClass
public class ErrorHandler {

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();

    private Logger getLogger() {
        return fallbackServerBungee.getLogger();
    }

    @Getter
    private final List<Diagnostic> diagnostics = Lists.newArrayList();

    public void add(Severity severity, String message) {
        diagnostics.add(new Diagnostic(severity, message));
    }

    public void handle() {
        if (diagnostics.isEmpty()) {
            return;
        }
        writeToFile();
    }

    private void writeToFile() {
        File logDir = new File("plugins/FallbackServer/logs");
        if (!logDir.exists()) logDir.mkdirs();

        File logFile = new File(logDir, "diagnostics.txt");

        try (FileWriter writer = new FileWriter(logFile, false)) {
            writer.write("==== FALLBACKSERVER DIAGNOSTIC ====\n");
            for (Diagnostic diagnostic : diagnostics) {
                writer.write("[" + diagnostic.getSeverity() + "] " + diagnostic.getMessage() + "\n");
            }
        } catch (IOException e) {
            getLogger().severe("§cError on writing diagnostic file: " + e.getMessage());
        }
    }
}
