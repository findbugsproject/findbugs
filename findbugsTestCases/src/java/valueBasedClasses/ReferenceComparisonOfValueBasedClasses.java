package valueBasedClasses;

import java.io.Serializable;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.ExpectWarning;

public class ReferenceComparisonOfValueBasedClasses {

    @ExpectWarning("VBC_REF_COMPARISON")
    public void referenceComparisonOfOptional() {
        Optional<String> optional = Optional.of("Foo");
        Object object = Optional.of("Bar");
        
        boolean same = optional == object;
        System.out.println("Reference Comparison of Optional yields: " + same);
    }
    
}
