package gui;

public interface TableListeners {
	public void viewStudentTableByStudent(int clientID);
	public void viewAttendanceByClass (String className, String classDate);
	public void viewAttendanceByCourse (String courseName);
	public void viewAttendanceByStudent (String clientID, String studentName);
	public void updateGithubUser(String clientID, String name);
	public void viewEmailByStudent(int clientID);
	public void viewPhoneByStudent(int clientID);
	public void viewActiveTAs();
	public void viewClassDetails(boolean[] dowSelectList);
	public void viewPreviousPage();
	public void deleteLogEntry(int logID);
}
