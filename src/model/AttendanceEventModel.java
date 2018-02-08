package model;

import java.sql.Date;

public class AttendanceEventModel implements Comparable<AttendanceEventModel> {
	private int clientID;
	private String eventName;
	private Date serviceDate;
	private String serviceDateString;
	private String githubName, githubComments, repoName;
	private StudentNameModel nameModel;

	public AttendanceEventModel(int clientID, Date serviceDate, String eventName, String githubName, String repoName,
			String githubComments, StudentNameModel nameModel) {
		this.clientID = clientID;
		this.serviceDate = serviceDate;
		this.eventName = eventName;
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

	public AttendanceEventModel(int clientID, String studentName, String serviceDate, String eventName) {
		this.clientID = clientID;
		this.nameModel = new StudentNameModel(studentName, "", false);
		this.serviceDateString = serviceDate;
		this.eventName = eventName;
	}

	public int getClientID() {
		return clientID;
	}

	public String getEventName() {
		return eventName;
	}

	public Date getServiceDate() {
		return serviceDate;
	}

	public String getServiceDateString() {
		if (serviceDateString != null)
			return serviceDateString;
		else
			return serviceDate.toString();
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
		if (clientID < other.getClientID())
			return -1;

		else if (clientID > other.getClientID())
			return 1;

		else {
			// Client ID matches
			int comp = this.getServiceDateString().compareTo(other.getServiceDateString());
			if (comp != 0)
				// Dates in descending order
				return -comp;
			else
				return this.getEventName().compareTo(other.getEventName());
		}
	}
}
