package valueBasedClasses;

import java.util.List;
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

    // wait on VBC

    @ExpectWarning("VBC_MT_WAIT")
    public void waitOnOptional(Optional<?> lock) throws InterruptedException {
        while (true) {
            lock.wait();
        }
    }

    @ExpectWarning("VBC_MT_WAIT")
    public void waitOnOptionalFromList(List<Optional<?>> locks) throws InterruptedException {
        while (true) {
            locks.get(0).wait();
        }
    }

    @ExpectWarning("VBC_MT_WAIT")
    public void waitOnMyValueBasedClass(MyValueBasedClass lock) throws InterruptedException {
        while (true) {
            lock.wait();
        }
    }

    @NoWarning("VBC_MT_WAIT")
    public void waitOnObject(Object lock) throws InterruptedException {
        while (true) {
            lock.wait();
        }
    }

    @NoWarning("VBC_MT_WAIT")
    public void noRealWait(MyValueBasedClass lock) throws InterruptedException {
        while (true) {
            lock.wait("This method is not inherited from object, so no 'real wait'.");
        }
    }

}
