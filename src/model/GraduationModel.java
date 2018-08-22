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
	private boolean isSfUpdated, isProcessed, isSkipLevel;

	public GraduationModel(int clientID, String studentName, int gradLevel, String score, String startDate,
			String endDate, boolean isSfUpdated, boolean isProcessed, boolean skipLevel) {
		// Graduation record with score and start date
		this.clientID = clientID;
		this.studentName = studentName;
		this.gradLevel = gradLevel;
		this.score = score;
		this.startDate = startDate;
		this.endDate = endDate;
		this.isSfUpdated = isSfUpdated;
		this.isProcessed = isProcessed;
		this.isSkipLevel = skipLevel;
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
