package model;

import java.sql.Date;

public class AttendanceEventModel implements Comparable<AttendanceEventModel> {
	private int clientID, visitID;
	private String eventName, teacherNames, serviceCategory, state;
	private Date serviceDate;
	private String serviceDateString;
	private String githubName, githubComments, repoName;
	private StudentNameModel nameModel;

	public AttendanceEventModel(int clientID, int visitID, Date serviceDate, String event, String githubName,
			String repoName, String githubComments, StudentNameModel nameModel, String serviceCategory, String state) {
		this.clientID = clientID;
		this.visitID = visitID;
		this.serviceDate = serviceDate;

		// Parse long event names for workshops
		eventName = event.trim();
		if (eventName.length() >= 3 && eventName.charAt(1) != '@' && eventName.charAt(2) != '@') {
			if (eventName.contains("Intro to Java Work"))
				eventName = "Intro to Java WShop";
			else if (eventName.contains("Electrical Engineering Intro Work"))
				eventName = "Electrical Eng WShop";
		}
		int idx = eventName.indexOf('(');
		if (idx > 0)
			eventName = eventName.substring(0, idx).trim();

		this.teacherNames = "";
		this.serviceCategory = serviceCategory.trim();
		this.state = state;
		this.githubName = githubName;
		if (githubName != null)
			this.githubName = githubName.trim();
		this.repoName = repoName;
		this.nameModel = nameModel;
		if (githubComments == null || githubComments.trim().equals(""))
			this.githubComments = "";
		else
			this.githubComments = "  > " + githubComments.trim();
	}

	public AttendanceEventModel(int clientID, int visitID, String studentName, String serviceDate, String eventName,
			String teacherNames, String serviceCategory, String state) {
		this.clientID = clientID;
		this.visitID = visitID;
		this.nameModel = new StudentNameModel(studentName, "", false);
		this.serviceDateString = serviceDate;
		this.eventName = eventName;
		this.teacherNames = teacherNames;
		this.serviceCategory = serviceCategory;
		this.state = state;
	}

	public int getClientID() {
		return clientID;
	}

	public int getVisitID() {
		return visitID;
	}

	public String getEventName() {
		return eventName;
	}

	public String getTeacherNames() {
		return teacherNames;
	}

	public Date getServiceDate() {
		return serviceDate;
	}

	public String getServiceDateString() {
		if (serviceDateString != null)
			return serviceDateString;
		else if (serviceDate != null)
			return serviceDate.toString();
		else
			return "";
	}

	public String getServiceCategory() {
		return serviceCategory;
	}

	public String getState() {
		return state;
	}

	public String getGithubName() {
		return githubName;
	}

	public String getGithubComments() {
		return githubComments;
	}

	public void setGithubComments(String comments) {
		// Add comments to existing github comments if unique
		if (githubComments.equals(""))
			githubComments = comments.trim();
		else if (!githubComments.contains(comments))
			githubComments += " / " + comments.trim();
	}

	public String getRepoName() {
		return repoName;
	}

	public StudentNameModel getStudentNameModel() {
		return nameModel;
	}

	@Override
	public int compareTo(AttendanceEventModel other) {
		if (this.clientID < other.getClientID())
			return -1;

		else if (this.clientID > other.getClientID())
			return 1;

		else {
			// Client ID matches, compare date next
			int comp = this.getServiceDateString().compareTo(other.getServiceDateString());
			// Dates in descending order
			if (comp < 0)
				return 1;
			else if (comp > 0)
				return -1;
			else
				return 0;
		}
	}
}
