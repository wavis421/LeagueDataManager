package model;

import java.util.ArrayList;

public class ActivityModel implements Comparable<ActivityModel> {
	private int clientID;
	private StudentNameModel studentName;
	private ArrayList<ActivityEventModel> activityEventList = new ArrayList<ActivityEventModel>();

	public ActivityModel(int clientID, StudentNameModel studentName, ActivityEventModel event) {
		this.clientID = clientID;
		this.studentName = studentName;
		this.activityEventList.add(event);
	}

	public void addActivityData(ActivityEventModel event) {
		activityEventList.add(event);
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

	public ArrayList<ActivityEventModel> getActivityEventList() {
		return activityEventList;
	}

	@Override
	public int compareTo(ActivityModel otherPerson) {
		return this.getStudentName().compareTo(otherPerson.getStudentName());
	}
}
