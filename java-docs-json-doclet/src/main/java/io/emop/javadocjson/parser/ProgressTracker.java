package io.emop.javadocjson.parser;

import org.apache.maven.plugin.logging.Log;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Progress tracking utility for the Javadoc crawler.
 */
public class ProgressTracker {
    
    private final Log log;
    private final AtomicInteger totalClasses;
    private final AtomicInteger processedClasses;
    private final AtomicInteger skippedClasses;
    private final AtomicInteger failedClasses;
    
    private long startTime;
    private long lastLogTime;
    private final long logInterval; // Log interval in milliseconds
    
    public ProgressTracker(Log log) {
        this(log, 5000); // Default log interval of 5 seconds
    }
    
    public ProgressTracker(Log log, long logIntervalMs) {
        this.log = log;
        this.totalClasses = new AtomicInteger(0);
        this.processedClasses = new AtomicInteger(0);
        this.skippedClasses = new AtomicInteger(0);
        this.failedClasses = new AtomicInteger(0);
        this.logInterval = logIntervalMs;
        this.startTime = System.currentTimeMillis();
        this.lastLogTime = startTime;
    }
    
    /**
     * Starts tracking progress.
     */
    public void start() {
        this.startTime = System.currentTimeMillis();
        this.lastLogTime = startTime;
        log.info("Starting Javadoc crawling...");
    }
    
    /**
     * Sets the total number of classes to be processed.
     * 
     * @param total The total number of classes
     */
    public void setTotalClasses(int total) {
        totalClasses.set(total);
        log.info("Total classes to process: " + total);
    }
    
    /**
     * Adds to the total number of classes.
     * 
     * @param count The number of classes to add
     */
    public void addToTotalClasses(int count) {
        int newTotal = totalClasses.addAndGet(count);
        log.debug("Added " + count + " classes to total. New total: " + newTotal);
    }
    
    /**
     * Increments the count of processed classes.
     */
    public void incrementProcessed() {
        int processed = processedClasses.incrementAndGet();
        logProgressIfNeeded(processed);
    }
    
    /**
     * Increments the count of skipped classes.
     */
    public void incrementSkipped() {
        skippedClasses.incrementAndGet();
        // Calculate total processed (including skipped) for progress tracking
        int totalProcessed = processedClasses.get() + skippedClasses.get();
        logProgressIfNeeded(totalProcessed);
    }
    
    /**
     * Increments the count of failed classes.
     */
    public void incrementFailed() {
        failedClasses.incrementAndGet();
        // Calculate total processed (including failed) for progress tracking
        int totalProcessed = processedClasses.get() + skippedClasses.get() + failedClasses.get();
        logProgressIfNeeded(totalProcessed);
    }
    
    /**
     * Logs progress if enough time has passed since the last log.
     * 
     * @param currentProcessed The current number of processed classes
     */
    private void logProgressIfNeeded(int currentProcessed) {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastLogTime >= logInterval) {
            logProgress();
            lastLogTime = currentTime;
        }
    }
    
    /**
     * Forces a progress log regardless of timing.
     */
    public void logProgress() {
        int total = totalClasses.get();
        int processed = processedClasses.get();
        int skipped = skippedClasses.get();
        int failed = failedClasses.get();
        
        if (total > 0) {
            double percentage = (double) processed / total * 100;
            long elapsed = System.currentTimeMillis() - startTime;
            double rate = processed > 0 ? (double) processed / (elapsed / 1000.0) : 0;
            
            log.info(String.format("Progress: %d/%d (%.1f%%) processed, %d skipped, %d failed. Rate: %.1f classes/sec", 
                                 processed, total, percentage, skipped, failed, rate));
        } else {
            log.info(String.format("Progress: %d processed, %d skipped, %d failed", 
                                 processed, skipped, failed));
        }
    }
    
    /**
     * Logs the final summary when crawling is complete.
     */
    public void logFinalSummary() {
        long totalTime = System.currentTimeMillis() - startTime;
        int total = totalClasses.get();
        int processed = processedClasses.get();
        int skipped = skippedClasses.get();
        int failed = failedClasses.get();
        
        double totalTimeSeconds = totalTime / 1000.0;
        double averageRate = processed > 0 ? processed / totalTimeSeconds : 0;
        
        log.info("=== Crawling Summary ===");
        log.info(String.format("Total time: %.2f seconds", totalTimeSeconds));
        log.info(String.format("Classes processed: %d", processed));
        log.info(String.format("Classes skipped: %d", skipped));
        log.info(String.format("Classes failed: %d", failed));
        
        if (total > 0) {
            log.info(String.format("Total classes found: %d", total));
            double successRate = (double) processed / total * 100;
            log.info(String.format("Success rate: %.1f%%", successRate));
        }
        
        log.info(String.format("Average processing rate: %.2f classes/second", averageRate));
        log.info("======================");
    }
    
    /**
     * Gets the current number of processed classes.
     * 
     * @return The number of processed classes
     */
    public int getProcessedCount() {
        return processedClasses.get();
    }
    
    /**
     * Gets the current number of skipped classes.
     * 
     * @return The number of skipped classes
     */
    public int getSkippedCount() {
        return skippedClasses.get();
    }
    
    /**
     * Gets the current number of failed classes.
     * 
     * @return The number of failed classes
     */
    public int getFailedCount() {
        return failedClasses.get();
    }
    
    /**
     * Gets the total number of classes.
     * 
     * @return The total number of classes
     */
    public int getTotalCount() {
        return totalClasses.get();
    }
    
    /**
     * Gets the current progress percentage.
     * 
     * @return The progress percentage (0-100)
     */
    public double getProgressPercentage() {
        int total = totalClasses.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) processedClasses.get() / total * 100;
    }
    
    /**
     * Gets the elapsed time in milliseconds.
     * 
     * @return The elapsed time since start
     */
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Gets the current processing rate in classes per second.
     * 
     * @return The processing rate
     */
    public double getProcessingRate() {
        long elapsed = getElapsedTime();
        if (elapsed == 0) {
            return 0.0;
        }
        return (double) processedClasses.get() / (elapsed / 1000.0);
    }
    
    /**
     * Estimates the remaining time in milliseconds.
     * 
     * @return The estimated remaining time, or -1 if cannot be estimated
     */
    public long getEstimatedRemainingTime() {
        int total = totalClasses.get();
        int processed = processedClasses.get();
        
        if (total == 0 || processed == 0) {
            return -1;
        }
        
        int remaining = total - processed;
        double rate = getProcessingRate();
        
        if (rate == 0) {
            return -1;
        }
        
        return (long) (remaining / rate * 1000);
    }
    
    /**
     * Resets all counters and timers.
     */
    public void reset() {
        totalClasses.set(0);
        processedClasses.set(0);
        skippedClasses.set(0);
        failedClasses.set(0);
        startTime = System.currentTimeMillis();
        lastLogTime = startTime;
    }
}