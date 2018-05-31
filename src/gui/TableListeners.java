package gui;

public interface TableListeners {
	public void viewStudentTableByStudent(int clientID);
	public void viewAttendanceByClass (String className);
	public void viewAttendanceByCourse (String courseName);
	public void viewAttendanceByStudent (String clientID, String studentName);
	public void updateGithubUser(String clientID, String name);
}
