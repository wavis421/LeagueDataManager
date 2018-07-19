package model;

public class GraduationModel {
	private int clientID;
	private String gradLevel;
	private double score;
	private String startDate;
	private String endDate;
	private boolean isSfUpdated, isProcessed;

	public GraduationModel(int clientID, String gradLevel, double score, String startDate, String endDate) {
		this.clientID = clientID;
		this.gradLevel = gradLevel;
		this.score = score;
		this.startDate = startDate;
		this.endDate = endDate;
		isSfUpdated = false;
		isProcessed = false;
	}

	public int getClientID() {
		return clientID;
	}

	public String getGradLevel() {
		return gradLevel;
	}

	public double getScore() {
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

	public void setSfUpdated(boolean isSfUpdated) {
		this.isSfUpdated = isSfUpdated;
	}

	public void setProcessed(boolean isProcessed) {
		this.isProcessed = isProcessed;
	}
}
