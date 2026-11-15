class filter_lock_test{

    private static int count = 0;

    public static void main(String[] args) throws InterruptedException {
        // Test for n ranging from 2 to 8 threads
        for (int n = 2; n <= 8; n++) {
            runTest(n);
        }
    }

    private static void runTest(int n) throws InterruptedException {
        filter_lock lock = new filter_lock(n);
        Thread[] threads = new Thread[n];

        for (int i = 0; i < n; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10; j++) { // Each thread increments count 10 times
                    lock.lock(threadId);
                    System.out.println("Thread " + threadId + " acquired the lock.");
                    count++;
                    lock.unlock(threadId);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Final count with " + n + " threads: " + count);
        count = 0; // Reset count for the next test
    }
}
