import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class lamport_bakery_lock {
    private final AtomicBoolean[] flag;
    private final AtomicIntegerArray label;
    private final int n;

    public lamport_bakery_lock(int n){
        this.n = n;
        this.flag = new AtomicBoolean[n];
        for (int i = 0; i < n; i++) {
            flag[i] = new AtomicBoolean(false);
        }
        this.label = new AtomicIntegerArray(n);
    }

    public void lock(int i) {
        flag[i].set(true);
        int max = 0;
        for (int k = 0; k < n; k++){
            if (label.get(k) > max){
                max = label.get(k);
            }
        }
        label.set(i, max +1);

        for (int k = 0; k < n; k++) {
            if (k == i){
                continue;
            }
            while (flag[k].get() && isSomeoneInterested(i, label.get(i), k, label.get(k))){}
        }
    }

    public void unlock(int i){
        flag[i].set(false);
    }

    public boolean isSomeoneInterested(int i, int labelI, int k, int labelK){
        if (labelI > labelK){
            return true;
        }
        if (labelI == labelK && i > k){ // lexicographic order
            return true;
        }
        return false;
    }
}
