
package edu.umd.cs.findbugs;

public class FieldWarningSuppressor extends ClassWarningSuppressor {

	IFieldAnnotation field;


	public FieldWarningSuppressor(String bugPattern, IClassAnnotation clazz, IFieldAnnotation field) {
		super(bugPattern, clazz);
		this.field = field;
		}
	@Override
	public boolean match(BugInstance bugInstance) {

		if (!super.match(bugInstance)) return false;

	IFieldAnnotation bugField = bugInstance.getPrimaryField();
	if (bugField == null ||
		!field.equals(bugField)) return false;
	if (DEBUG)
	System.out.println("Suppressing " + bugInstance);
	return true;
	}
}

