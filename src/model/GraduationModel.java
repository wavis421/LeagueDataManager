package model;

public class GraduationModel {
	private int clientID;
	private String studentName, gradLevel;
	private int score;
	private String startDate, endDate;
	private boolean isSfUpdated, isNewClass;

	public GraduationModel(int clientID, String studentName, String gradLevel, int score, String startDate,
			String endDate, boolean isSfUpdated, boolean isNewClass) {
		this.clientID = clientID;
		this.studentName = studentName;
		this.gradLevel = gradLevel.trim();
		this.score = score;
		this.startDate = startDate;
		this.endDate = endDate;
		this.isSfUpdated = isSfUpdated;
		this.isNewClass = isNewClass;
	}

	public int getClientID() {
		return clientID;
	}

	public String getStudentName() {
		return studentName;
	}

	public String getGradLevel() {
		return gradLevel;
	}

	public int getScore() {
		return score;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public boolean isSfUpdated() {
		return isSfUpdated;
	}

	public boolean isNewClass() {
		return isNewClass;
	}

	public void setSfUpdated(boolean isSfUpdated) {
		this.isSfUpdated = isSfUpdated;
	}

	public void setNewClass(boolean isNewClass) {
		this.isNewClass = isNewClass;
	}
}
