package ubb.dissertation.common;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class OshiLogger {

    private final CentralProcessor processor;
    private final GlobalMemory memory;
    private final PrintWriter writer;

    private long lastMsgCount = 0;
    private long lastTime = System.currentTimeMillis();
    private long[] prevTicks;

    private final AtomicLong totalMessages = new AtomicLong(0);
    private final AtomicLong totalLatencyMs = new AtomicLong(0);

    public OshiLogger(String outputCsvPath) throws IOException {
        SystemInfo systemInfo = new SystemInfo();
        processor = systemInfo.getHardware().getProcessor();
        memory = systemInfo.getHardware().getMemory();
        prevTicks = processor.getSystemCpuLoadTicks();

        writer = new PrintWriter(new FileWriter(outputCsvPath, true));
        writer.println("timestamp,cpu_percent,mem_used_mb,latency_ms,throughput_mps");
    }

    public void log() {
        long[] newTicks = processor.getSystemCpuLoadTicks();
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        prevTicks = newTicks;

        long usedMemory = memory.getTotal() - memory.getAvailable();

        long now = System.currentTimeMillis();
        long elapsedMs = now - lastTime;

        long msgCount = totalMessages.get();
        long deltaMsg = msgCount - lastMsgCount;
        double throughput = (deltaMsg * 1000.0) / elapsedMs;

        double avgLatency = msgCount > 0 ? totalLatencyMs.get() / (double) msgCount : 0;

        String timestamp = Instant.ofEpochMilli(now).toString();
        writer.printf("%s,%.2f,%.2f,%.2f,%.2f%n",
                timestamp,
                cpuLoad,
                usedMemory / 1024.0 / 1024,
                avgLatency,
                throughput
        );

        writer.flush();
        lastMsgCount = msgCount;
        lastTime = now;
    }

    public void recordMessage(long latencyMillis) {
        totalMessages.incrementAndGet();
        totalLatencyMs.addAndGet(latencyMillis);
    }
}
