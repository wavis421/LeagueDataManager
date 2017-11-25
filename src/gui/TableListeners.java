package gui;

public interface TableListeners {
	public void viewStudentTableByStudent(int clientID);
	public void viewAttendanceByClass (String className);
	public void viewAttendanceByStudent (String clientID, String studentName);
	public void removeStudent(int clientID);
}
