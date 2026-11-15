import java.util.concurrent.atomic.AtomicIntegerArray;

public class FilterLock {
    private final AtomicIntegerArray level;
    private final AtomicIntegerArray victim;
    private final int n;

    public FilterLock(int n) {
        this.n = n;
        this.level = new AtomicIntegerArray(n); // initialized to 0
        this.victim = new AtomicIntegerArray(n); // initialized to 0
    }

    public void lock(int i) {
        for (int L = 1; L < n; L++) {
            level.set(i, L);
            victim.set(L, i);
            
            while (isLevelFull(i, L) && victim.get(L) == i) {
                Thread.onSpinWait();
            }
        }
    }

    public void unlock(int i) {
        level.set(i, 0);
    }

    private boolean isLevelFull(int i, int L) {
        for (int k = 0; k < n; k++) {
            if (k != i && level.get(k) >= L) {
                return true;
            }
        }
        return false;
    }
}