package me.candiesjar.fallbackserver.handlers;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.objects.text.Diagnostic;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.io.FilesUtils;
import net.md_5.bungee.api.ProxyServer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@UtilityClass
public class ErrorHandler {

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final ProxyServer proxyServer = fallbackServerBungee.getProxy();
    private final File logDir = fallbackServerBungee.getLogDir();

    @Getter
    private final List<Diagnostic> diagnostics = Lists.newArrayList();

    public void add(Severity severity, String message) {
        LocalDateTime now = LocalDateTime.now();
        String time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String date = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String formattedMessage = String.format("[%s] [%s] %s", date, time, message);
        diagnostics.add(new Diagnostic(severity, formattedMessage));
    }

    public void save() {
        if (diagnostics.isEmpty()) {
            return;
        }
        writeToFile();
    }

    public void clear() {
        diagnostics.clear();
    }

    public boolean checkForErrors() {
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.getSeverity() == Severity.ERROR) {
                return true;
            }
        }
        return false;
    }

    private void writeToFile() {
        File logFile = new File(logDir, "diagnostics.txt");

        String proxyName = proxyServer.getName();
        String proxyVersion = proxyServer.getVersion();
        String javaVersion = System.getProperty("java.version");
        String pluginVersion = fallbackServerBungee.getDescription().getVersion();

        if (checkForSize(logFile.toPath())) {
            FilesUtils.renameFile("diagnostics.txt", "diagnostics_old.txt", logDir);
            Utils.printDebug("§7[INFO] Renamed existing diagnostics log file due to size limit.", false);
        }

        try (FileWriter writer = new FileWriter(logFile, false)) {
            writer.write("==== FALLBACKSERVER DIAGNOSTIC ====\n");
            writer.write("Proxy Name: " + proxyName + "\n");
            writer.write("Proxy Version: " + proxyVersion + "\n");
            writer.write("Java Version: " + javaVersion + "\n");
            writer.write("Plugin Version: " + pluginVersion + "\n");
            writer.write("Log date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\n");
            writer.write("Online mode: " + proxyServer.getConfig().isOnlineMode() + "\n");
            writer.write("===================================\n\n");
            for (Diagnostic diagnostic : diagnostics) {
                writer.write("[" + diagnostic.getSeverity() + "] " + diagnostic.getMessage() + "\n");
            }
        } catch (IOException e) {
            Utils.printDebug("§7[ERROR] Failed to write diagnostics to file.", true);
            Utils.printDebug("§7[ERROR] " + e.getMessage(), true);
        }

        diagnostics.clear();
    }

    private boolean checkForSize(Path logPath) {
        File logFile = logPath.toFile();
        long maxSizeBytes = 5 * 1024 * 1024; // 5 MB
        return logFile.exists() && logFile.length() >= maxSizeBytes;
    }
}
