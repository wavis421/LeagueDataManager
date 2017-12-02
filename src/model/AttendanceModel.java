package model;

import java.util.ArrayList;

public class AttendanceModel implements Comparable<AttendanceModel> {
	private int clientID;
	private StudentNameModel studentName;
	private String githubName;
	private ArrayList<AttendanceEventModel> attendanceEventList = new ArrayList<AttendanceEventModel>();

	public AttendanceModel(int clientID, StudentNameModel studentName, String githubName, AttendanceEventModel event) {
		this.clientID = clientID;
		this.studentName = studentName;
		this.githubName = githubName;
		this.attendanceEventList.add(event);
	}

	public void addAttendanceData(AttendanceEventModel event) {
		attendanceEventList.add(event);
	}

	public String toString() {
		return studentName + " (" + clientID + ")";
	}

	public int getClientID() {
		return clientID;
	}

	public StudentNameModel getStudentName() {
		return studentName;
	}

	public String getGithubName() {
		return githubName;
	}

	public ArrayList<AttendanceEventModel> getAttendanceEventList() {
		return attendanceEventList;
	}

	@Override
	public int compareTo(AttendanceModel otherPerson) {
		return this.getStudentName().compareTo(otherPerson.getStudentName());
	}
}
