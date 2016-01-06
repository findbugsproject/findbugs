import javax.annotation.CheckForNull;

class PreferZeroLengthArrays {

    public int[] foo(int i) {
        return null;
    }

    public int[] bar(int i) {
        return new int[0];
    }

    @CheckForNull
    public int[] fooCheckForNull(int i) {
        return null;
    }
}
