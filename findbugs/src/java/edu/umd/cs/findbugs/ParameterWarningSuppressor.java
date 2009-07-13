package edu.umd.cs.findbugs;

public class ParameterWarningSuppressor extends ClassWarningSuppressor {

	final IMethodAnnotation method;

	final int register;

	public ParameterWarningSuppressor(String bugPattern, IClassAnnotation clazz, IMethodAnnotation method, int register) {
		super(bugPattern, clazz);
		this.method = method;
		this.register = register;
	}

	@Override
	public boolean match(BugInstance bugInstance) {

		if (!super.match(bugInstance))
			return false;

		IMethodAnnotation bugMethod = bugInstance.getPrimaryMethod();
		ILocalVariableAnnotation localVariable = bugInstance.getPrimaryLocalVariableAnnotation();
		if (bugMethod == null || !method.equals(bugMethod))
			return false;
		if (localVariable == null || localVariable.getRegister() != register)
			return false;
		if (DEBUG)
			System.out.println("Suppressing " + bugInstance);
		return true;
	}
}
