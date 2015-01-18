package valueBasedClasses;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.ExpectWarning;

public class SerializingValueBasedClass implements Serializable {

    private static final long serialVersionUID = 0L;

    @ExpectWarning("SE_BAD_FIELD")
    private Optional<String> nonTransientFieldOptional;

    @ExpectWarning("SE_BAD_FIELD")
    private MyValueBasedClass nonTransientFieldValueBasedClass;

    public SerializingValueBasedClass() {
        nonTransientFieldOptional = null;
        nonTransientFieldValueBasedClass = null;
    }

    @ExpectWarning("DMI_NONSERIALIZABLE_OBJECT_WRITTEN ")
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(nonTransientFieldOptional);
        stream.writeObject(nonTransientFieldValueBasedClass);
    }

}
