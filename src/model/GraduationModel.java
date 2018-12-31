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
	private String studentName, score, currentClass;
	private Integer gradLevel;
	private String startDate, endDate;
	private boolean isSfUpdated, isProcessed, isSkipLevel, isPromoted;

	public GraduationModel(int clientID, String studentName, int gradLevel, String score, String currentClass,
			String startDate, String endDate, boolean isSfUpdated, boolean isProcessed, boolean skipLevel,
			boolean promoted) {
		// Graduation record with score and start date
		this.clientID = clientID;
		this.studentName = studentName;
		this.gradLevel = gradLevel;
		this.score = score;
		this.currentClass = currentClass;
		this.startDate = startDate;
		this.endDate = endDate;
		this.isSfUpdated = isSfUpdated;
		this.isProcessed = isProcessed;
		this.isSkipLevel = skipLevel;
		this.isPromoted = promoted;
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
		if (score == null || score.equals("0"))
			return "";
		else
			return score;
	}

	public String getCurrentClass() {
		return currentClass;
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

	public boolean isProcessed() {
		return isProcessed;
	}

	public boolean isSkipLevel() {
		return isSkipLevel;
	}

	public boolean isPromoted() {
		return isPromoted;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public void setSfUpdated(boolean isSfUpdated) {
		this.isSfUpdated = isSfUpdated;
	}

	public void setProcessed(boolean isProcessed) {
		this.isProcessed = isProcessed;
	}
}
