package model;

import java.sql.Date;
import java.sql.Time;

public class ActivityModel implements Comparable<ActivityModel> {
	private int clientID;
	private String eventName;
	private Date serviceDate;
	private String comments;
	private StudentNameModel studentName;

	public ActivityModel(int clientID, StudentNameModel studentName, Date serviceDate, String eventName, String comments) {
		this.clientID = clientID;
		this.studentName = studentName;
		this.serviceDate = serviceDate;
		this.eventName = eventName;
		this.comments = comments;
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

	public String getEventName() {
		return eventName;
	}

	public Date getServiceDate() {
		return serviceDate;
	}

	public String getComments() {
		return comments;
	}

	@Override
	public int compareTo(ActivityModel otherPerson) {
		return this.getStudentName().compareTo(otherPerson.getStudentName());
	}
}
