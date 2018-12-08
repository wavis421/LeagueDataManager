package model;

/**
 * This model contains class level and module information for a student.
 * 
 * It is used as a working model during database import.
 * 
 */
public class StudentClassLevelModel {
	int clientID;
	String eventName, serviceDate, moduleName, repoName, currentLevel;
	StudentNameModel studentName;

	public StudentClassLevelModel(int clientID, String eventName, String serviceDate, String repoName) {
		this.clientID = clientID;
		this.eventName = eventName;
		this.serviceDate = serviceDate;
		this.repoName = repoName;
	}

	public StudentClassLevelModel(int clientID, String eventName, String moduleName) {
		this.clientID = clientID;
		this.eventName = eventName;
		this.moduleName = moduleName;
	}

	public StudentClassLevelModel(int clientID, StudentNameModel studentName, String eventName, String moduleName,
			String currentLevel) {
		this.clientID = clientID;
		this.studentName = studentName;
		this.eventName = eventName;
		this.moduleName = moduleName;
		this.currentLevel = currentLevel;
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

	public String getModuleName() {
		return moduleName;
	}

	public String getCurrentLevel() {
		return currentLevel;
	}

	public String getServiceDate() {
		return serviceDate;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setEventName(String name) {
		this.eventName = name;
	}

	public void setServiceDate(String serviceDate) {
		this.serviceDate = serviceDate;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}
}
