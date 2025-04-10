package com.bank.transaction.util;

import org.springframework.stereotype.Component;

/**
 * Snowflake ID Generator
 * Generates 64-bit long integer IDs with the following structure:
 * 0 - 41bits: Timestamp (approx. 69 years)
 * 41 - 51bits: Worker ID (10 bits, can be deployed on 1024 machines)
 * 51 - 64bits: Sequence number (12 bits, can generate 4096 IDs per millisecond)
 */
@Component
public class SnowflakeIdGenerator {
    /**
     * Start timestamp, used as offset (2024-01-01)
     */
    private final long START_TIMESTAMP = 1704067200000L;

    /**
     * Number of bits allocated for worker ID
     */
    private final long WORKER_ID_BITS = 5L;

    /**
     * Number of bits allocated for data center ID
     */
    private final long DATA_CENTER_ID_BITS = 5L;

    /**
     * Number of bits allocated for sequence
     */
    private final long SEQUENCE_BITS = 12L;

    /**
     * Maximum worker ID: 31
     */
    private final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * Maximum data center ID: 31
     */
    private final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    /**
     * Sequence mask (4095)
     */
    private final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /**
     * Worker ID shift bits
     */
    private final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * Data center ID shift bits
     */
    private final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * Timestamp shift bits
     */
    private final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    /**
     * Worker ID
     */
    private long workerId;

    /**
     * Data center ID
     */
    private long dataCenterId;

    /**
     * Sequence number
     */
    private long sequence = 0L;

    /**
     * Last timestamp when ID was generated
     */
    private long lastTimestamp = -1L;

    /**
     * Default constructor
     */
    public SnowflakeIdGenerator() {
        this(0L, 0L);
    }

    /**
     * Constructor with worker and data center IDs
     * @param workerId Worker ID (0-31)
     * @param dataCenterId Data center ID (0-31)
     */
    public SnowflakeIdGenerator(long workerId, long dataCenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("Worker ID can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("DataCenter ID can't be greater than %d or less than 0", MAX_DATA_CENTER_ID));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * Gets the next ID
     * @return The next ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        // If current time is less than last time, it means system clock has been rolled back
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", 
                                  lastTimestamp - timestamp));
        }

        // If it's the same millisecond, increment sequence
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // If sequence overflows, wait for next millisecond
            if (sequence == 0) {
                timestamp = waitForNextMillis(lastTimestamp);
            }
        } else {
            // Reset sequence for different millisecond
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // Combine all parts to generate ID
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT) |
                (dataCenterId << DATA_CENTER_ID_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                sequence;
    }

    /**
     * Wait until next millisecond to get a new timestamp
     * @param lastTimestamp Last timestamp when ID was generated
     * @return Next millisecond timestamp
     */
    private long waitForNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
} 