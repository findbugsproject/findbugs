package valueBasedClasses;

import edu.umd.cs.findbugs.annotations.ValueBased;

@ValueBased
public class MyValueBasedClass {
    
    /**
     * A method overloading {@link #wait()} to write a test which captures false positives.
     */
    public void wait(String arg) {
        System.out.println(arg);
    }

}
