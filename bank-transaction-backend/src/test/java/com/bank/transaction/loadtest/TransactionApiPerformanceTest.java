package com.bank.transaction.loadtest;

import com.bank.transaction.model.Transaction;
import com.bank.transaction.model.TransactionType;
import com.bank.transaction.model.TransactionStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Performance test class for testing transaction API performance under high concurrency
 */
public class TransactionApiPerformanceTest {

    // Base URL address
    private static final String BASE_URL = "http://localhost:8080/api/transactions";
    
    // Test configuration
    private static final int CONCURRENT_USERS = 100;
    private static final int OPERATIONS_PER_USER = 10;

    // Statistics information
    private static Map<String, List<Long>> responseTimes = new HashMap<>();
    private static Map<String, AtomicInteger> successCounts = new HashMap<>();
    private static Map<String, AtomicInteger> failureCounts = new HashMap<>();
    
    // List of created transaction IDs for subsequent tests
    private static List<Long> createdTransactionIds = Collections.synchronizedList(new ArrayList<>());
    
    // RestTemplate instance
    private static final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("===== Bank Transaction API Performance Test =====");
        
        // Initialize statistics data
        initializeCounters();
        
        // Execute tests in sequence
        System.out.println("\n[1/5] Executing CREATE transaction test...");
        executeTest("CREATE", CONCURRENT_USERS, OPERATIONS_PER_USER, TransactionApiPerformanceTest::testCreateTransaction);
        
        // Ensure enough transaction IDs for subsequent tests
        if (createdTransactionIds.size() < CONCURRENT_USERS * OPERATIONS_PER_USER) {
            System.out.println("\nInsufficient transaction count, creating additional transactions...");
            int needed = (CONCURRENT_USERS * OPERATIONS_PER_USER) - createdTransactionIds.size();
            for (int i = 0; i < needed; i++) {
                Long id = createTransaction();
                if (id != null) {
                    createdTransactionIds.add(id);
                }
                Thread.sleep(50); // Avoid too rapid requests
            }
        }
        
        System.out.println("\n[2/5] Executing GET single transaction test...");
        executeTest("GET", CONCURRENT_USERS, OPERATIONS_PER_USER, TransactionApiPerformanceTest::testGetTransaction);
        
        System.out.println("\n[3/5] Executing LIST transactions test...");
        executeTest("LIST", CONCURRENT_USERS, OPERATIONS_PER_USER, TransactionApiPerformanceTest::testListTransactions);
        
        System.out.println("\n[4/5] Executing UPDATE transaction test...");
        executeTest("UPDATE", CONCURRENT_USERS, OPERATIONS_PER_USER, TransactionApiPerformanceTest::testUpdateTransaction);
        
        System.out.println("\n[5/5] Executing DELETE transaction test...");
        executeTest("DELETE", CONCURRENT_USERS, OPERATIONS_PER_USER, TransactionApiPerformanceTest::testDeleteTransaction);
        
        // Output overall test results report
        System.out.println("\n===== Performance Test Summary Report =====");
        printSummaryReport();
    }
    
    /**
     * Initialize counters
     */
    private static void initializeCounters() {
        String[] operations = {"CREATE", "GET", "LIST", "UPDATE", "DELETE"};
        for (String op : operations) {
            responseTimes.put(op, Collections.synchronizedList(new ArrayList<>()));
            successCounts.put(op, new AtomicInteger(0));
            failureCounts.put(op, new AtomicInteger(0));
        }
    }
    
    /**
     * Generic test execution method
     */
    private static void executeTest(String operationType, int concurrentUsers, int operationsPerUser, 
                                  TestOperation operation) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch latch = new CountDownLatch(concurrentUsers);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < concurrentUsers; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerUser; j++) {
                        try {
                            long responseTime = operation.execute(userId, j);
                            if (responseTime > 0) {
                                responseTimes.get(operationType).add(responseTime);
                                successCounts.get(operationType).incrementAndGet();
                            } else {
                                failureCounts.get(operationType).incrementAndGet();
                            }
                            
                        } catch (Exception e) {
                            System.err.println("User " + userId + " operation failed: " + e.getMessage());
                            failureCounts.get(operationType).incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // Print test results for this operation type
        printTestResult(operationType, totalTime);
    }
    
    /**
     * Test create transaction
     */
    private static long testCreateTransaction(int userId, int operationIndex) {
        return measureResponseTime(() -> {
            Long id = createTransaction();
            if (id != null) {
                createdTransactionIds.add(id);
                return true;
            }
            return false;
        });
    }
    
    /**
     * Test get single transaction
     */
    private static long testGetTransaction(int userId, int operationIndex) {
        if (createdTransactionIds.isEmpty()) {
            System.err.println("No transaction IDs available for testing");
            return 0;
        }
        
        // Randomly select a created transaction ID
        int randomIndex = (int) (Math.random() * createdTransactionIds.size());
        Long transactionId = createdTransactionIds.get(randomIndex);
        
        return measureResponseTime(() -> getTransaction(transactionId));
    }
    
    /**
     * Test get transaction list
     */
    private static long testListTransactions(int userId, int operationIndex) {
        // Test different query parameters randomly
        int page = (int) (Math.random() * 3);
        int size = 10 + (int) (Math.random() * 20);
        
        return measureResponseTime(() -> getTransactionsPaged(page, size));
    }
    
    /**
     * Test update transaction
     */
    private static long testUpdateTransaction(int userId, int operationIndex) {
        if (createdTransactionIds.isEmpty()) {
            System.err.println("No transaction IDs available for testing");
            return 0;
        }
        
        // Randomly select a created transaction ID
        int randomIndex = (int) (Math.random() * createdTransactionIds.size());
        Long transactionId = createdTransactionIds.get(randomIndex);
        
        return measureResponseTime(() -> updateTransaction(transactionId));
    }
    
    /**
     * Test delete transaction
     */
    private static long testDeleteTransaction(int userId, int operationIndex) {
        if (createdTransactionIds.isEmpty()) {
            System.err.println("No transaction IDs available for testing");
            return 0;
        }
        
        // Get and remove a transaction ID from the list (ensure no duplicate deletion)
        Long transactionId = null;
        synchronized (createdTransactionIds) {
            if (!createdTransactionIds.isEmpty()) {
                transactionId = createdTransactionIds.remove(0);
            }
        }
        
        if (transactionId == null) {
            return 0;
        }
        
        final Long idToDelete = transactionId;
        return measureResponseTime(() -> deleteTransaction(idToDelete));
    }
    
    /**
     * Create new transaction
     */
    private static Long createTransaction() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Transaction transaction = new Transaction();
            
            transaction.setDescription("Performance Test Transaction");
            
            // Randomly select one of the three transaction types
            int typeSelector = (int)(Math.random() * 3); // 0, 1, or 2
            TransactionType type;
            
            switch (typeSelector) {
                case 0:
                    type = TransactionType.DEPOSIT;
                    break;
                case 1:
                    type = TransactionType.WITHDRAWAL;
                    break;
                case 2:
                    type = TransactionType.TRANSFER;
                    break;
                default:
                    type = TransactionType.DEPOSIT; // Fallback
            }
            
            transaction.setType(type);
            
            // For deposits, destination account is not empty, source account can be null
            // For withdrawals, source account is not empty, destination account can be null
            // For transfers, both accounts are non-empty
            if (type == TransactionType.DEPOSIT) {
                transaction.setDestinationAccount("DA" + String.format("%012d", (int)(Math.random() * 999999999999L)));
                transaction.setSourceAccount(null);
            } else if (type == TransactionType.WITHDRAWAL) {
                transaction.setSourceAccount("SA" + String.format("%012d", (int)(Math.random() * 999999999999L)));
                transaction.setDestinationAccount(null);
            } else { // TRANSFER
                transaction.setSourceAccount("SA" + String.format("%012d", (int)(Math.random() * 999999999999L)));
                transaction.setDestinationAccount("DA" + String.format("%012d", (int)(Math.random() * 999999999999L)));
            }
            
            transaction.setAmount(BigDecimal.valueOf(100 + Math.random() * 1000).setScale(2, BigDecimal.ROUND_HALF_UP));
            transaction.setStatus(TransactionStatus.INITIATED);
            // Set processing date and timestamp
            LocalDateTime now = LocalDateTime.now();
            transaction.setTimestamp(now);
            transaction.setProcessingDate(now);
            
            HttpEntity<Transaction> requestEntity = new HttpEntity<>(transaction, headers);
            
            ResponseEntity<Transaction> response = restTemplate.postForEntity(
                    BASE_URL, requestEntity, Transaction.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getId();
            }
        } catch (Exception e) {
            System.err.println("Failed to create transaction: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("validation")) {
                System.err.println("Validation error details: " + e.getMessage());
            }
        }
        return null;
    }
    
    /**
     * Get single transaction
     */
    private static boolean getTransaction(Long id) {
        try {
            ResponseEntity<Transaction> response = restTemplate.getForEntity(
                    BASE_URL + "/" + id, Transaction.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Failed to get transaction: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get paged transaction list
     */
    private static boolean getTransactionsPaged(int page, int size) {
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(
                    BASE_URL + "/paged?page=" + page + "&size=" + size, Object.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Failed to get paged transactions: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update transaction
     */
    private static boolean updateTransaction(Long id) {
        try {
            // First get the transaction
            ResponseEntity<Transaction> getResponse = restTemplate.getForEntity(
                    BASE_URL + "/" + id, Transaction.class);
            
            if (!getResponse.getStatusCode().is2xxSuccessful() || getResponse.getBody() == null) {
                return false;
            }
            
            Transaction transaction = getResponse.getBody();
            
            // Modify transaction attributes
            transaction.setDescription(transaction.getDescription() + " (Updated)");
            transaction.setAmount(transaction.getAmount().add(BigDecimal.valueOf(10.0)));
            if (transaction.getStatus() == TransactionStatus.INITIATED) {
                transaction.setStatus(TransactionStatus.PROCESSING);
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Transaction> requestEntity = new HttpEntity<>(transaction, headers);
            
            ResponseEntity<Transaction> response = restTemplate.exchange(
                    BASE_URL + "/" + id, HttpMethod.PUT, requestEntity, Transaction.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("更新交易失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除交易
     */
    private static boolean deleteTransaction(Long id) {
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    BASE_URL + "/" + id, HttpMethod.DELETE, null, Void.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("删除交易失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测量API调用的响应时间
     */
    private static long measureResponseTime(ApiOperation operation) {
        long startTime = System.nanoTime();
        boolean success = operation.execute();
        long endTime = System.nanoTime();
        
        if (success) {
            return TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        } else {
            return 0; // 请求失败
        }
    }
    
    /**
     * 打印测试结果
     */
    private static void printTestResult(String operationType, long totalTime) {
        List<Long> times = responseTimes.get(operationType);
        int successCount = successCounts.get(operationType).get();
        int failureCount = failureCounts.get(operationType).get();
        int totalRequests = successCount + failureCount;
        
        System.out.println("\n----- " + getOperationName(operationType) + " operation performance test results -----");
        System.out.println("Total Requests: " + totalRequests);
        System.out.println("Successful Requests: " + successCount);
        System.out.println("Failed Requests: " + failureCount);
        
        if (successCount > 0) {
            double successRate = (double) successCount / totalRequests * 100;
            double throughput = (double) successCount / (totalTime / 1000.0);
            
            DoubleSummaryStatistics stats = times.stream()
                    .mapToDouble(Long::doubleValue)
                    .summaryStatistics();
            
            List<Long> sortedTimes = new ArrayList<>(times);
            Collections.sort(sortedTimes);
            
            long p50 = percentile(sortedTimes, 50);
            long p90 = percentile(sortedTimes, 90);
            long p95 = percentile(sortedTimes, 95);
            long p99 = percentile(sortedTimes, 99);
            
            System.out.println("Success Rate: " + String.format("%.2f%%", successRate));
            System.out.println("Total Execution Time: " + totalTime + " ms");
            System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/second");
            System.out.println("Response Time (ms):");
            System.out.println("  Min: " + stats.getMin());
            System.out.println("  Max: " + stats.getMax());
            System.out.println("  Average: " + String.format("%.2f", stats.getAverage()));
            System.out.println("  Median (P50): " + p50);
            System.out.println("  P90: " + p90);
            System.out.println("  P95: " + p95);
            System.out.println("  P99: " + p99);
        } else {
            System.out.println("No successful requests, unable to calculate performance metrics");
        }
    }
    
    /**
     * 打印总体测试结果摘要
     */
    private static void printSummaryReport() {
        System.out.println("\nOperation Comparison:");
        System.out.println("Operation\tTotal\tSuccess\tFailed\tSuccess Rate\tAvg Response(ms)\tThroughput(req/s)");
        System.out.println("-------------------------------------------------------------------------------------");
        
        for (String opType : new String[]{"CREATE", "GET", "LIST", "UPDATE", "DELETE"}) {
            List<Long> times = responseTimes.get(opType);
            int successCount = successCounts.get(opType).get();
            int failureCount = failureCounts.get(opType).get();
            int totalRequests = successCount + failureCount;
            
            if (totalRequests == 0) {
                System.out.printf("%-10s\t%d\t%d\t%d\t--\t--\t--\n", 
                    getOperationName(opType), totalRequests, successCount, failureCount);
                continue;
            }
            
            double successRate = (double) successCount / totalRequests * 100;
            
            if (successCount == 0) {
                System.out.printf("%-10s\t%d\t%d\t%d\t%.2f%%\t--\t--\n", 
                    getOperationName(opType), totalRequests, successCount, failureCount, successRate);
                continue;
            }
            
            double avgResponseTime = times.stream().mapToDouble(Long::doubleValue).average().orElse(0);
            double throughput = (double) successCount / (times.stream().mapToLong(Long::longValue).sum() / 1000.0);
            
            System.out.printf("%-10s\t%d\t%d\t%d\t%.2f%%\t%.2f\t%.2f\n", 
                getOperationName(opType), totalRequests, successCount, failureCount, 
                successRate, avgResponseTime, throughput);
        }
        
        System.out.println("\nOverall Performance Assessment:");
        int totalSuccess = successCounts.values().stream().mapToInt(AtomicInteger::get).sum();
        int totalFailure = failureCounts.values().stream().mapToInt(AtomicInteger::get).sum();
        int totalRequests = totalSuccess + totalFailure;
        
        if (totalRequests > 0) {
            double overallSuccessRate = (double) totalSuccess / totalRequests * 100;
            System.out.println("Total Requests: " + totalRequests);
            System.out.println("Overall Success Rate: " + String.format("%.2f%%", overallSuccessRate));
            
            if (totalSuccess > 0) {
                double overallAvgResponseTime = responseTimes.values().stream()
                    .flatMap(List::stream)
                    .mapToDouble(Long::doubleValue)
                    .average()
                    .orElse(0);
                
                System.out.println("Overall Average Response Time: " + String.format("%.2f ms", overallAvgResponseTime));
                
                // Analyze performance bottlenecks
                String slowestOp = "";
                double maxAvgTime = 0;
                for (String opType : new String[]{"CREATE", "GET", "LIST", "UPDATE", "DELETE"}) {
                    List<Long> times = responseTimes.get(opType);
                    if (!times.isEmpty()) {
                        double avg = times.stream().mapToDouble(Long::doubleValue).average().orElse(0);
                        if (avg > maxAvgTime) {
                            maxAvgTime = avg;
                            slowestOp = opType;
                        }
                    }
                }
                
                if (!slowestOp.isEmpty()) {
                    System.out.println("Performance Bottleneck: " + getOperationName(slowestOp) + 
                                       " operation (Average Response Time: " + String.format("%.2f ms", maxAvgTime) + ")");
                }
            }
        }
        
        System.out.println("\nTest Completed At: " + LocalDateTime.now());
    }
    
    /**
     * Get readable operation name
     */
    private static String getOperationName(String opType) {
        switch (opType) {
            case "CREATE": return "Create";
            case "GET": return "Get";
            case "LIST": return "List";
            case "UPDATE": return "Update";
            case "DELETE": return "Delete";
            default: return opType;
        }
    }
    
    /**
     * Calculate percentile
     */
    private static long percentile(List<Long> sortedList, double percentile) {
        if (sortedList.isEmpty()) {
            return 0;
        }
        
        int index = (int) Math.ceil(percentile / 100.0 * sortedList.size()) - 1;
        return sortedList.get(Math.max(0, index));
    }
    
    /**
     * API operation interface
     */
    @FunctionalInterface
    private interface ApiOperation {
        boolean execute();
    }
    
    /**
     * Test operation interface
     */
    @FunctionalInterface
    private interface TestOperation {
        long execute(int userId, int operationIndex);
    }
} 