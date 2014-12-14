package valueBasedClasses;

import java.io.Serializable;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.ExpectWarning;

@SuppressWarnings("unused")
public class SerializingValueBasedClass implements Serializable{
    
    private static final long serialVersionUID = 0L;

    @ExpectWarning("SE_BAD_FIELD")
    private Optional<String> nonTransientFieldOptional;
    
    @ExpectWarning("SE_BAD_FIELD")
    private MyValueBasedClass nonTransientFieldValueBasedClass;
    
}
