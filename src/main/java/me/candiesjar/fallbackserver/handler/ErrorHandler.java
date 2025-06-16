package me.candiesjar.fallbackserver.handler;

import com.google.common.collect.Lists;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.objects.text.Diagnostic;
import me.candiesjar.fallbackserver.utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class ErrorHandler {

    private final FallbackServerVelocity fallbackServerVelocity = FallbackServerVelocity.getInstance();
    private final ProxyServer proxyServer = fallbackServerVelocity.getServer();

    @Getter
    private final List<Diagnostic> diagnostics = Lists.newArrayList();

    public void deleteLogFile() {
        File logDir = new File("plugins/fallbackservervelocity/logs");

        if (!logDir.exists()) return;

        File logFile = new File(logDir, "diagnostics.txt");
        if (logFile.exists()) {
            logFile.delete();
        }
    }

    public void add(Severity severity, String message) {
        Calendar calendar = Calendar.getInstance();
        String time = String.format("%02d:%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
        String date = String.format("%02d/%02d/%04d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
        String formattedMessage = String.format("[%s] [%s] %s", date, time, message);
        diagnostics.add(new Diagnostic(severity, formattedMessage));
    }

    public void handle() {
        if (diagnostics.isEmpty()) {
            return;
        }
        writeToFile();
    }

    public void schedule() {
        proxyServer.getScheduler().buildTask(fallbackServerVelocity, () -> {
                    if (diagnostics.isEmpty()) {
                        return;
                    }
                    writeToFile();
                    diagnostics.clear();
                }).delay(0, TimeUnit.MINUTES)
                .repeat(2, TimeUnit.MINUTES)
                .schedule();
    }

    private void writeToFile() {
        File logDir = new File("plugins/fallbackservervelocity/logs");
        if (!logDir.exists()) logDir.mkdirs();

        File logFile = new File(logDir, "diagnostics.txt");

        try (FileWriter writer = new FileWriter(logFile, false)) {
            writer.write("==== FALLBACKSERVER DIAGNOSTIC ====\n");
            for (Diagnostic diagnostic : diagnostics) {
                writer.write("[" + diagnostic.severity() + "] " + diagnostic.message() + "\n");
            }
        } catch (IOException ignored) {
            Utils.printDebug("§7[ERROR] Failed to write diagnostics to file.", true);
        }
    }
}