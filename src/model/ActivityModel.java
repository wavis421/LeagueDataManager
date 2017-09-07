package model;

import java.sql.Date;
import java.sql.Time;

public class ActivityModel implements Comparable<ActivityModel> {
	private int clientID;
	private String studentName, eventName;
	private Date serviceDate;
	private String comments;

	public ActivityModel(int clientID, String studentName, Date serviceDate, String eventName, String comments) {
		this.clientID = clientID;
		this.studentName = studentName;
		this.serviceDate = serviceDate;
		this.eventName = eventName;
		this.comments = comments;
	}

	public String toString() {
		return studentName + " (" + clientID + ")";
	}

	@Override
	public int compareTo(ActivityModel otherPerson) {
		return this.getStudentName().compareTo(otherPerson.getStudentName());
	}

	public int getClientID() {
		return clientID;
	}

	public String getStudentName() {
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
}
