package sfBugsNew;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.ExpectWarning;
import edu.umd.cs.findbugs.annotations.NoWarning;
import edu.umd.cs.findbugs.annotations.ValueBased;

@SuppressWarnings("javadoc")
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

        // it would be great if this worked, but 'optionals.get(0)' gets recognized as string
        // @ExpectWarning("VBC_REF_COMPARISON")
        public void referenceComparisonOfOptionalFromList() {
            List<Optional<?>> optionals = new ArrayList<>();
            Object object = "Bar";

            boolean same = optionals.get(0) == object;
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
        public void referenceComparisonOfLocalDateArray() {
            LocalDate[] dates = new LocalDate[0];
            Object object = new LocalDate[0];

            boolean same = dates == object;
            System.out.println("Reference comparison of 'List' yields: " + same);
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
        public void lockOnOptionalFromList(List<Optional<?>> locks) {
            synchronized (locks.get(0)) {
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

        @NoWarning("VBC_MO_LOCK")
        public void lockOnLocalDateArray(LocalDate[] lock) {
            synchronized (lock) {
                System.out.println("Locking on LocalDate array.");
            }
        }

        // wait on VBC

        @ExpectWarning("VBC_MO_WAIT")
        public void waitOnOptional(Optional<?> lock) throws InterruptedException {
            while (true)
                lock.wait();
        }

        @ExpectWarning("VBC_MO_WAIT")
        public void waitOnOptionalFromList(List<Optional<?>> locks) throws InterruptedException {
            while (true)
                locks.get(0).wait();
        }

        @ExpectWarning("VBC_MO_WAIT")
        public void waitOnMyValueBasedClass(MyValueBasedClass lock) throws InterruptedException {
            while (true)
                lock.wait();
        }

        @NoWarning("VBC_MO_WAIT")
        public void waitOnObject(Object lock) throws InterruptedException {
            while (true)
                lock.wait();
        }

        @NoWarning("VBC_MO_WAIT")
        public void waitOnLocalDateArray(LocalDate[] lock) throws InterruptedException {
            while (true)
                lock.wait();
        }

        @NoWarning("VBC_MO_WAIT")
        public void noRealWait(MyValueBasedClass lock) throws InterruptedException {
            while (true)
                lock.wait("This method is not inherited from object, so no 'real wait'.");
        }

        // notify on VBC

        @ExpectWarning("VBC_MO_NOTIFY")
        public void notifyOnOptional(Optional<?> lock) throws InterruptedException {
            lock.notify();
        }

        @ExpectWarning("VBC_MO_NOTIFY")
        public void notifyOnOptionalFromList(List<Optional<?>> locks) throws InterruptedException {
            while (true)
                locks.get(0).notify();
        }

        @ExpectWarning("VBC_MO_NOTIFY")
        public void notifyOnMyValueBasedClass(MyValueBasedClass lock) throws InterruptedException {
            while (true)
                lock.notify();
        }

        @NoWarning("VBC_MO_NOTIFY")
        public void notifyOnObject(Object lock) throws InterruptedException {
            while (true)
                lock.notify();
        }

        @NoWarning("VBC_MO_NOTIFY")
        public void notifyOnLocalDateArray(LocalDate[] lock) throws InterruptedException {
            while (true)
                lock.notify();
        }

        @NoWarning("VBC_MO_NOTIFY")
        public void noRealNotify(MyValueBasedClass lock) throws InterruptedException {
            while (true)
                lock.notify("This method is not inherited from object, so no 'real notify'.");
        }

        // notifyAll on VBC

        @ExpectWarning("VBC_MO_NOTIFY")
        public void notifyAllOnOptional(Optional<?> lock) throws InterruptedException {
            lock.notifyAll();
        }

        @ExpectWarning("VBC_MO_NOTIFY")
        public void notifyAllOnOptionalFromList(List<Optional<?>> locks) throws InterruptedException {
            while (true)
                locks.get(0).notifyAll();
        }

        @ExpectWarning("VBC_MO_NOTIFY")
        public void notifyAllOnMyValueBasedClass(MyValueBasedClass lock) throws InterruptedException {
            while (true)
                lock.notifyAll();
        }

        @NoWarning("VBC_MO_NOTIFY")
        public void notifyAllOnObject(Object lock) throws InterruptedException {
            while (true)
                lock.notifyAll();
        }

        @NoWarning("VBC_MO_NOTIFY")
        public void notifyAllOnLocalDateArray(LocalDate[] lock) throws InterruptedException {
            while (true)
                lock.notify();
        }

        @NoWarning("VBC_MO_NOTIFY")
        public void noRealNotifyAll(MyValueBasedClass lock) throws InterruptedException {
            while (true)
                lock.notifyAll("This method is not inherited from object, so no 'real notifyAll'.");
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

    public static class IdentityOfValueBasedClass {

        // @NoWarning("VBC_IDENTITY_HASHCODE")
        // public Map<OptionalInt, String> createIdentityMapWithOptionalAsKey() {
        // Map<OptionalInt, String> map = new IdentityHashMap<>();
        // return map;
        // }

        @ExpectWarning("VBC_IDENTITY_HASHCODE")
        public void callIdentityHashCodeWithOptional(Optional<?> argument) {
            System.identityHashCode(argument);
        }

        // it would be great if this worked, but 'optionals.get(0)' gets recognized as string
        // @ExpectWarning("VBC_IDENTITY_HASHCODE")
        public void callIdentityHashCodeWithOptional(List<Optional<?>> arguments) {
            System.identityHashCode(arguments.get(0));
        }

        @ExpectWarning("VBC_IDENTITY_HASHCODE")
        public void callIdentityHashCodeWithValueBasedClass(MyValueBasedClass argument) {
            System.identityHashCode(argument);
        }

        @NoWarning("VBC_IDENTITY_HASHCODE")
        public void callIdentityHashCodeWithLocalDateArray(LocalDate[] argument) {
            System.identityHashCode(argument);
        }

        @NoWarning("VBC_IDENTITY_HASHCODE")
        public void callIdentityHashCodeNotWithValueBasedClass(Object argument) {
            System.identityHashCode(argument);
        }

    }

    /*
     * UTILITIES
     */

    @ValueBased
    public static class MyValueBasedClass {

        /**
         * A method overloading {@link #wait()} to write a test which captures false positives.
         */
        public void wait(String arg) {
            System.out.println(arg);
        }

        /**
         * A method overloading {@link #notify()} to write a test which captures false positives.
         */
        public void notify(String arg) {
            System.out.println(arg);
        }

        /**
         * A method overloading {@link #notifyAll()} to write a test which captures false positives.
         */
        public void notifyAll(String arg) {
            System.out.println(arg);
        }

    }

}
