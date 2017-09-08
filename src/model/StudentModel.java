package model;

import java.sql.Date;
import java.util.Comparator;

public class StudentModel implements Comparable<StudentModel> {
	private int studentID, clientID;
	private String lastName, firstName, githubName;
	private int homeLocation, gender, gradYear;
	private Date startDate;
	private StudentNameModel nameModel;

	public StudentModel(int personID, int clientID, StudentNameModel nameModel, String githubName,
			int gender, Date startDate, int homeLocation, int gradYear) {
		this.studentID = personID;
		this.clientID = clientID;
		this.nameModel = nameModel;
		this.githubName = githubName;
		this.homeLocation = homeLocation;
		this.gender = gender;
		this.gradYear = gradYear;
		this.startDate = startDate;
	}

	public String toString() {
		return nameModel.toString() + " (" + clientID + ")";
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

	public int getPersonID() {
		return studentID;
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
