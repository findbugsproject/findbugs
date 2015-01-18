package valueBasedClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.ExpectWarning;
import edu.umd.cs.findbugs.annotations.NoWarning;

public class ReferenceComparisonOfValueBasedClass {

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
