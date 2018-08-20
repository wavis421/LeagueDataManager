package model;

/**
 * 
 * This is the Graduation Model to track graduation information by student.
 * 
 * @author wavis
 *
 */
public class GraduationModel {
	private int clientID;
	private String studentName, score;
	private Integer gradLevel;
	private String startDate, endDate;
	private boolean isSfUpdated, isNewClass;
	private String notes = "";

	public GraduationModel(int clientID, String studentName, int gradLevel, String score, String startDate,
			String endDate, boolean isSfUpdated, boolean isNewClass) {
		this.clientID = clientID;
		this.studentName = studentName;
		this.gradLevel = gradLevel;
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

	public int getGradLevel() {
		return gradLevel;
	}

	public String getGradLevelString() {
		return gradLevel.toString();
	}

	public String getScore() {
		return score;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public String getNotes() {
		return notes;
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

	public void setNotes(String note) {
		this.notes = note;
	}
}
