package com.device.fot.virtual.controller;

/**
 *
 * @author Uellington Damasceno
 */
import com.device.fot.virtual.model.LatencyRecord;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class LatencyRecordPool {

    private static final Queue<LatencyRecord> pool = new ConcurrentLinkedQueue<>();

    private LatencyRecordPool() {
    }

    public static LatencyRecord borrow() {
        LatencyRecord record = pool.poll();
        if (record == null) {
            record = new LatencyRecord();
        }
        return record;
    }

    public static void release(LatencyRecord record) {
        if (record != null) {
            record.reset();
            pool.offer(record);
        }
    }

}
