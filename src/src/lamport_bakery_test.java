import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.*;

class lamport_bakery_test{

    private static int count = 0;
    private static final AtomicInteger inCriticalSection = new AtomicInteger();
    static volatile boolean violation = false;
    static final int ITER = 100_000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting Lamport Bakery Lock mutual exclusion test!");
        // Test for 2 to 8 threads
        for (int n = 2; n <= 8; n++) {
            runMutualExclusionTest(n, true);
            runMutualExclusionTest(n, false);
        }
        // Test for overtaking
        overtakeTest(8);
    }

    private static void runMutualExclusionTest(int n, boolean useLock) throws InterruptedException {
        lamport_bakery_lock lock = new lamport_bakery_lock(n);
        count = 0;
        violation = false;
        Thread[] threads = new Thread[n];

        for (int i = 0; i < n; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < ITER; j++) { // Each thread increments count 100000 times
                    if (useLock){
                        lock.lock(threadId);
                    }
                    if (inCriticalSection.incrementAndGet() > 1){ // should be only 1 thread in critical section at a time
                        violation = true;
                    }
                    count++;
                    inCriticalSection.decrementAndGet();
                    if (useLock){
                        lock.unlock(threadId);
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        boolean passed = count == n * ITER && !violation; // all threads were able to successfully increment and there was always a singular thread in the CS
        System.out.printf("n=%d lock=%b count=%d expected=%d violation=%b -> %s%n",
                n, useLock, count, n * ITER, violation, passed ? "PASS" : "FAIL");
    }

    private static void overtakeTest(int n) throws InterruptedException {
        final int ITER = 20_000;
        lamport_bakery_lock lock = new lamport_bakery_lock(n);
        AtomicLong clock = new AtomicLong();
        long[] stampLog = new long[n * ITER]; // arrival time of the thread at each CS entry
        int[] ownerLog = new int[n * ITER];   // which thread entered
        int[] pos = {0};

        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < ITER; j++) {
                    long stamp = clock.incrementAndGet(); // arrival, taken before lock()
                    lock.lock(id);
                    stampLog[pos[0]] = stamp;
                    ownerLog[pos[0]] = id;
                    pos[0]++;
                    for (int w = 0; w < 50; w++){
                        Thread.onSpinWait(); // some work to create contention
                    }
                    lock.unlock(id);
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();

        int[] maxPerThread = new int[n];
        long total = 0;
        for (int p = 0; p < stampLog.length; p++) {
            int count = 0;
            for (int q = 0; q < p; q++) {
                if (stampLog[q] > stampLog[p]) count++;
            }
            total += count;
            maxPerThread[ownerLog[p]] = Math.max(maxPerThread[ownerLog[p]], count);
        }

        System.out.println("Starting Overtaking test for lamport bakery lock.");
        System.out.println("Total overtake events: " + total);
        for (int i = 0; i < n; i++){
            System.out.println("Thread " + i + ": max times overtaken in one lock() call = " + maxPerThread[i]);
        }
    }
}
