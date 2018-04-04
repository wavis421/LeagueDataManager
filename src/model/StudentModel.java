package model;

import java.sql.Date;

public class StudentModel implements Comparable<StudentModel> {
	private int clientID;
	private String githubName;
	private int homeLocation, gender, gradYear;
	private Date startDate;
	private StudentNameModel nameModel;
	private boolean missingData;

	public StudentModel(int clientID, StudentNameModel nameModel, String githubName, int gender, Date startDate,
			int homeLocation, int gradYear) {
		this.clientID = clientID;
		this.nameModel = nameModel;
		this.githubName = githubName;
		this.homeLocation = homeLocation;
		this.gender = gender;
		this.gradYear = gradYear;
		this.startDate = startDate;

		if (nameModel.getIsInMasterDb()
				&& (homeLocation == LocationModel.CLASS_LOCATION_UNKNOWN || gender == GenderModel.getGenderUnknown()
						|| gradYear == 0 || githubName == null || githubName.equals("")))
			missingData = true;
		else
			missingData = false;
	}

	public String toString() {
		return nameModel.toString() + " (" + clientID + ")";
	}

	public boolean isMissingData() {
		return missingData;
	}

	public int getClientID() {
		return clientID;
	}

	public int getGender() {
		return gender;
	}

	public Date getStartDate() {
		return startDate;
	}

	public int getGradYear() {
		return gradYear;
	}

	public String getLastName() {
		return nameModel.getLastName();
	}

	public String getFirstName() {
		return nameModel.getFirstName();
	}

	public StudentNameModel getNameModel() {
		return nameModel;
	}

	public String getGithubName() {
		return githubName;
	}

	public int getHomeLocation() {
		return homeLocation;
	}

	@Override
	public int compareTo(StudentModel otherName) {
		int comp = this.getLastName().compareTo(otherName.getLastName());
		if (comp == 0)
			return (this.getFirstName().compareTo(otherName.getFirstName()));
		else
			return comp;
	}
}
