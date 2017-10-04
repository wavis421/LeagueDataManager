package model;

import java.sql.Date;

public class ActivityEventModel {
	private int clientID;
	private String eventName;
	private Date serviceDate;
	private String githubName, githubComments, repoName;
	private StudentNameModel nameModel;

	public ActivityEventModel(int clientID, Date serviceDate, String eventName, String githubName, String repoName,
			String githubComments, StudentNameModel nameModel) {
		this.clientID = clientID;
		this.serviceDate = serviceDate;
		this.eventName = eventName;
		this.githubName = githubName;
		this.repoName = repoName;
		this.nameModel = nameModel;
		if (githubComments == null || githubComments.equals(""))
			this.githubComments = "";
		else
			this.githubComments = "  > " + githubComments.trim();
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

	public String getGithubName() {
		return githubName;
	}

	public String getGithubComments() {
		return githubComments;
	}

	public String getRepoName() {
		return repoName;
	}

	public StudentNameModel getStudentNameModel() {
		return nameModel;
	}
}
