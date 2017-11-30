package model;

public class StudentImportModel implements Comparable<StudentImportModel> {
	private int clientID;
	private String lastName, firstName, githubName, startDate, homeLocString;
	private int homeLocation, gender, gradYear, isInMasterDb;

	public StudentImportModel(int clientID, String lastName, String firstName, String githubName, String gender,
			String startDate, String homeLocation, String gradYear) {

		// CSV File data
		this.clientID = clientID;
		this.lastName = lastName;
		this.firstName = firstName;
		this.githubName = parseGithubName(githubName);
		this.gender = GenderModel.convertStringToGender(gender);
		this.startDate = startDate;

		this.homeLocation = LocationModel.convertStringToLocation(homeLocation);
		this.homeLocString = LocationModel.convertLocationToString(this.homeLocation);

		if (gradYear.equals("") || gradYear.equals("\"\""))
			this.gradYear = 0;
		else {
			try {
				this.gradYear = Integer.parseInt(gradYear);

			} catch (NumberFormatException e) {
				this.gradYear = 0;
			}
		}

		isInMasterDb = 1;
	}

	public StudentImportModel(int clientID, String lastName, String firstName, String githubName, int gender,
			String startDate, int homeLocation, int gradYear, int isInMasterDb) {

		// Database format being converted for comparison purposes
		this.clientID = clientID;
		this.lastName = lastName;
		this.firstName = firstName;
		this.githubName = parseGithubName(githubName);
		this.gender = gender;
		this.startDate = startDate;
		this.homeLocation = homeLocation;
		this.gradYear = gradYear;
		this.isInMasterDb = isInMasterDb;
	}

	private String parseGithubName(String githubName) {
		if (githubName == null || githubName.equals("") || githubName.equals("\"\""))
			githubName = "";
		else {
			int index = githubName.indexOf('(');
			if (index != -1)
				githubName = githubName.substring(0, index);
			githubName.trim();
		}
		return githubName;
	}

	public String toString() {
		return firstName + " " + lastName + " (" + clientID + ")";
	}

	public int getClientID() {
		return clientID;
	}

	public String getLastName() {
		return lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getGithubName() {
		return githubName;
	}

	public int getGender() {
		return gender;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getHomeLocAsString() {
		return homeLocString;
	}

	public int getHomeLocation() {
		return homeLocation;
	}

	public int getGradYear() {
		return gradYear;
	}

	public int getIsInMasterDb() {
		return isInMasterDb;
	}

	@Override
	public int compareTo(StudentImportModel other) {
		if (clientID < other.getClientID())
			return -1;

		else if (clientID > other.getClientID())
			return 1;

		// Client ID matches
		else if (lastName.equals(other.getLastName()) && firstName.equals(other.getFirstName())
				&& githubName.equals(other.getGithubName()) && startDate.equals(other.getStartDate())
				&& homeLocation == other.getHomeLocation() && gender == other.getGender()
				&& gradYear == other.getGradYear() && isInMasterDb == other.getIsInMasterDb())
			return 0;

		else {
			// Client ID matches but data does not
			return -2;
		}
	}

	public String displayAll() {
		return (clientID + ": " + firstName + " " + lastName + " (" + gender + "), github: " + githubName + ", home: "
				+ homeLocString + ", start: " + startDate + ", grad: " + gradYear + ", " + isInMasterDb);
	}
}
