package sfBugsNew;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.ExpectWarning;
import edu.umd.cs.findbugs.annotations.NoWarning;
import edu.umd.cs.findbugs.annotations.ValueBased;

public class Feature313jdk8 {

    /*
     * TESTS
     */

    public static class ReferenceComparisonOfValueBasedClass {

        @ExpectWarning("VBC_REF_COMPARISON")
        public void referenceComparisonOfOptional() {
            Optional<String> optional = Optional.of("Foo");
            Object object = Optional.of("Bar");

            boolean same = optional == object;
            System.out.println("Reference comparison of 'Optional' yields: " + same);
        }

        @ExpectWarning("VBC_REF_COMPARISON")
        public void referenceComparisonOfMyValueBasedClass() {
            MyValueBasedClass my = new MyValueBasedClass();
            Object object = new MyValueBasedClass();

            boolean same = my == object;
            System.out.println("Reference comparison of 'MyValueBasedClass' yields: " + same);
        }

        @NoWarning("VBC_REF_COMPARISON")
        public void referenceComparisonOfNotValueBasedClasses() {
            List<String> list = new ArrayList<String>();
            Object object = new ArrayList<Integer>();

            boolean same = list == object;
            System.out.println("Reference comparison of 'List' yields: " + same);
        }

    }

    public static class MonitoringValueBasedClass {

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

    public static class SerializingValueBasedClass implements Serializable {

        private static final long serialVersionUID = 0L;

        @ExpectWarning("SE_BAD_FIELD")
        private Optional<String> nonTransientFieldOptional;

        @ExpectWarning("SE_BAD_FIELD")
        private MyValueBasedClass nonTransientFieldValueBasedClass;

        public SerializingValueBasedClass() {
            nonTransientFieldOptional = Optional.empty();
            nonTransientFieldValueBasedClass = new MyValueBasedClass();
        }

        @ExpectWarning("DMI_NONSERIALIZABLE_OBJECT_WRITTEN ")
        private void writeObject(ObjectOutputStream stream) throws IOException {
            stream.writeObject(nonTransientFieldOptional);
            stream.writeObject(nonTransientFieldValueBasedClass);
        }

    }

    /*
     * UTILITIES
     */

    @ValueBased
    public static class MyValueBasedClass {

        /**
         * A method overloading {@link #wait()} to write a test which captures
         * false positives.
         */
        public void wait(String arg) {
            System.out.println(arg);
        }

    }

}
