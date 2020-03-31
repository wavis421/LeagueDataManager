package model;

import java.sql.Date;

public class AttendanceEventModel implements Comparable<AttendanceEventModel> {
	private int clientID, visitID;
	private String eventName, teacherNames, serviceCategory, state, lastSFState;
	private Date serviceDate;
	private String serviceDateString, serviceTime, classLevel;
	private String githubName, githubComments, repoName;
	private StudentNameModel nameModel;
	private boolean markForDeletion;
	private String gitDescription;

	public AttendanceEventModel(int clientID, int visitID, Date serviceDate, String serviceTime, String event,
			String githubName, String repoName, String githubComments, String githubDescription, StudentNameModel nameModel,
			String serviceCategory, String state, String lastSFState, String teacherNames, String classLevel) {
		this.clientID = clientID;
		this.visitID = visitID;
		this.serviceDate = serviceDate;
		this.serviceTime = serviceTime;
		this.classLevel = classLevel;
		this.teacherNames = teacherNames;
		if (teacherNames == null)
			this.teacherNames = "";

		// Parse long event names for workshops
		eventName = event.trim();
		if (eventName.length() >= 3 && eventName.charAt(1) != '@' && eventName.charAt(2) != '@') {
			if (eventName.contains("Intro to Java Work"))
				eventName = "Intro to Java WShop";
			else if (eventName.contains("Electrical Engineering Intro Work"))
				eventName = "Electrical Eng WShop";
			else if (eventName.contains("Contest: Cyber Security"))
				eventName = "Contest: Cyber Security";
			else if (eventName.contains("Workshop"))
				eventName = eventName.replace("Workshop", "WShop");
		}
		int idx = eventName.indexOf('(');
		if (idx > 0)
			eventName = eventName.substring(0, idx).trim();

		idx = eventName.indexOf(",");
		if (idx > 0)
			eventName = eventName.substring(0, idx);

		this.serviceCategory = serviceCategory.trim();
		this.state = state;
		this.lastSFState = lastSFState;
		this.githubName = githubName;
		if (githubName != null)
			this.githubName = githubName.trim();
		this.repoName = repoName;
		this.nameModel = nameModel;
		if (githubComments == null || githubComments.trim().equals(""))
			this.githubComments = "";
		else
			this.githubComments = "  > " + githubComments.trim();
		this.gitDescription = githubDescription;
	}

	public AttendanceEventModel(int clientID, int visitID, String studentName, String serviceDate, String serviceTime,
			String eventName, String teacherNames, String serviceCategory, String state, String lastSFState) {
		this.clientID = clientID;
		this.visitID = visitID;
		this.nameModel = new StudentNameModel(studentName, "", false);
		this.serviceDateString = serviceDate;
		this.serviceTime = serviceTime;
		this.eventName = eventName;
		this.teacherNames = teacherNames;
		this.serviceCategory = serviceCategory;
		this.state = state;
		this.lastSFState = lastSFState;
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

	public String getClassLevel() {
		return classLevel;
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

	public String getServiceTime() {
		return serviceTime;
	}

	public String getServiceCategory() {
		return serviceCategory;
	}

	public String getState() {
		return state;
	}

	public String getLastSFState() {
		return lastSFState;
	}

	public String getGithubName() {
		return githubName;
	}

	public String getGithubComments() {
		return githubComments;
	}

	public void setGithubComments(String fullMsg) {
		fullMsg = fullMsg.trim();		
		String comments = fullMsg;
		String description = "";
		
		// Extract the summary and description
		int idx = fullMsg.indexOf("\n");
		if (idx > -1) {
			// Extract summary
			comments = fullMsg.substring(0, idx);
			
			// Remove all new line characters
			while (idx < fullMsg.length() && fullMsg.charAt(idx) == '\n')
				idx++;
			
			// Store everything after summary as the description
			if (idx < fullMsg.length()) {
				description = fullMsg.substring(idx);
			}
		}

		// Add comments to existing github comments if unique
		if (githubComments.equals(""))
			githubComments = comments;
		else if (!githubComments.contains(comments))
			githubComments += " / " + comments;
		
		// Only store description if not already present
		if (gitDescription.equals(""))
			gitDescription = description;
	}

	public String getGitDescription() {
		return gitDescription;
	}

	public String getRepoName() {
		return repoName;
	}

	public StudentNameModel getStudentNameModel() {
		return nameModel;
	}

	public boolean isMarkForDeletion() {
		return markForDeletion;
	}

	public void setMarkForDeletion(boolean markForDeletion) {
		this.markForDeletion = markForDeletion;
	}

	@Override
	public int compareTo(AttendanceEventModel other) {
		if (this.clientID < other.getClientID())
			return -1;

		else if (this.clientID > other.getClientID())
			return 1;

		// Client ID matches, compare date next
		int comp = this.getServiceDateString().compareTo(other.getServiceDateString());
		// Dates in descending order
		if (comp < 0)
			return 1;
		else if (comp > 0)
			return -1;

		// Client ID and service date match, sort visit ID
		else if (this.visitID < other.getVisitID())
			return -1;
		else if (this.visitID > other.getVisitID())
			return 1;

		// ClientID, date and Visit ID match, order by time.
		// Visit ID will be null for future visits.
		comp = this.getServiceTime().compareTo(other.getServiceTime());
		if (comp < 0)
			return -1;
		else if (comp > 0)
			return 1;
		else
			return 0;
	}
}
