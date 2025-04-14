package me.candiesjar.fallbackserveraddon.utils.tasks;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import me.candiesjar.fallbackserveraddon.utils.PacketEventsUtil;
import me.candiesjar.fallbackserveraddon.utils.ProtocolLibUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.*;

@UtilityClass
public class ThreadTask {

    private final FallbackServerAddon plugin = FallbackServerAddon.getInstance();
    private MyScheduledTask task = null;

    private final long MONITOR_INTERVAL = 5 * 20L;
    private final long THREAD_TIMEOUT = 5;

    private boolean locked;
    private int threadLag = 0;

    @SneakyThrows
    public void monitorMainThread() {
        task = UniversalScheduler.getScheduler(plugin).runTaskTimerAsynchronously(() -> {

            if (isMainThreadResponsive()) {
                if (locked) {
                    locked = false;
                    threadLag = 0;
                    plugin.registerPing();
                }
                return;
            }

            if (threadLag < 2) {
                threadLag++;
                return;
            }

            locked = true;
            if (plugin.isPLib()) ProtocolLibUtil.sendKeepAlive();
            if (plugin.isPEvents() && !plugin.isPLib()) PacketEventsUtil.sendKeepAlive();

            for (int i = 0; i<=5; i++) {
                plugin.getLogger().warning("WARNING: This is not caused by FallbackServer Addon - Your server seems frozen!");
                if (!plugin.isPLib() && !plugin.isPEvents())
                    plugin.getLogger().warning("Reconnect feature won't work if you don't have ProtocolLib or PacketEvents integration.");
            }
        }, 1L, MONITOR_INTERVAL);
    }

    public void stopMonitoring() {
        if (task != null) task.cancel();
    }

    private boolean isMainThreadResponsive() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threads = threadBean.dumpAllThreads(false, false);
        for (ThreadInfo info : threads) {
            if (info.getThreadName().equals("Server thread")) {
                if (info.getThreadState() == Thread.State.TIMED_WAITING ||
                        info.getThreadState() == Thread.State.BLOCKED) {
                    return testMainThreadResponsiveness();
                }
            }
        }
        return true;
    }

    private boolean testMainThreadResponsiveness() {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        UniversalScheduler.getScheduler(plugin).runTask(() -> future.complete(true));
        try {
            return future.get(THREAD_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            return false;
        }
    }
}
