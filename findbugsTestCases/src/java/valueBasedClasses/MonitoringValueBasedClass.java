package valueBasedClasses;

import java.util.Optional;

import edu.umd.cs.findbugs.annotations.ExpectWarning;
import edu.umd.cs.findbugs.annotations.NoWarning;

public class MonitoringValueBasedClass {

    // use VBC as lock in synchronized block

    @ExpectWarning("VBC_MO_LOCK")
    public void lockOnOptional(Optional<?> lock) {
        synchronized (lock) {
            System.out.println("Locking on Optional.");
        }
    }

    @ExpectWarning("VBC_MO_LOCK")
    public void lockOnMyValueBasedClass(MyValueBasedClass lock) {
        synchronized (lock) {
            System.out.println("Locking on MyValueBasedClass.");
        }
    }

    @NoWarning("VBC_MO_LOCK")
    public void lockOnObject(Object lock) {
        synchronized (lock) {
            System.out.println("Locking on MyValueBasedClass.");
        }
    }

}
