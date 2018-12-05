package model;

import java.sql.Date;

public class StudentModel implements Comparable<StudentModel> {
	private int clientID;
	private String githubName, currentClass, currentModule, currentLevel, staffSinceDate;
	private int homeLocation, gender, gradYear, staffPastEvents;
	private Double age;
	private Date startDate;
	private StudentNameModel nameModel;
	private boolean missingData;
	private String email, acctMgrEmail, emergEmail, phone, acctMgrPhone, homePhone, emergPhone;

	public StudentModel(int clientID, StudentNameModel nameModel, Double age, String githubName, int gender,
			Date startDate, int homeLocation, int gradYear, String currClass, String email, String acctMgrEmail,
			String emergEmail, String phone, String acctMgrPhone, String homePhone, String emergPhone,
			String currModule, String currLevel) {
		this.clientID = clientID;
		this.nameModel = nameModel;
		this.age = age;
		this.githubName = githubName;
		this.homeLocation = homeLocation;
		this.gender = gender;
		this.gradYear = gradYear;
		this.startDate = startDate;
		this.currentClass = currClass;
		this.currentModule = currModule;
		this.currentLevel = currLevel;

		this.email = email;
		this.acctMgrEmail = acctMgrEmail;
		this.emergEmail = emergEmail;
		this.phone = phone;
		this.acctMgrPhone = acctMgrPhone;
		this.homePhone = homePhone;
		this.emergPhone = emergPhone;

		if (nameModel.getIsInMasterDb()
				&& (homeLocation == LocationModel.CLASS_LOCATION_UNKNOWN || gender == GenderModel.getGenderUnknown()
						|| gradYear == 0 || githubName == null || githubName.equals("") || age == 0))
			missingData = true;
		else
			missingData = false;
	}

	public StudentModel(int clientID, StudentNameModel nameModel, Double age, String currClass, String staffSinceDate,
			int staffPastEvents, String email, String phone) {
		// Student model for student TA's
		this.clientID = clientID;
		this.nameModel = nameModel;
		this.age = age;
		this.currentClass = currClass;
		this.staffSinceDate = staffSinceDate;
		this.staffPastEvents = staffPastEvents;
		this.email = email;
		this.phone = phone;
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

	public String getCurrentClass() {
		return currentClass;
	}

	public String getCurrentModule() {
		return currentModule;
	}

	public String getCurrentLevel() {
		return currentLevel;
	}

	public String getEmail() {
		return email;
	}

	public String getAcctMgrEmail() {
		return acctMgrEmail;
	}

	public String getEmergEmail() {
		return emergEmail;
	}

	public String getPhone() {
		return phone;
	}

	public String getAcctMgrPhone() {
		return acctMgrPhone;
	}

	public String getHomePhone() {
		return homePhone;
	}

	public String getEmergPhone() {
		return emergPhone;
	}

	public Double getAge() {
		return age;
	}

	public String getStaffSinceDate() {
		return staffSinceDate;
	}

	public int getStaffPastEvents() {
		return staffPastEvents;
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
