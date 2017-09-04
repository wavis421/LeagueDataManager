package model;

import java.sql.Date;

public class StudentModel implements Comparable<StudentModel> {
	private int studentID, clientID;
	private String lastName, firstName, githubName;
	private int homeLocation, gender, gradYear;
	private Date startDate;

	public StudentModel(int personID, int clientID, String lastName, String firstName, String githubName,
			int gender, Date startDate, int homeLocation, int gradYear) {
		this.studentID = personID;
		this.clientID = clientID;
		this.lastName = lastName;
		this.firstName = firstName;
		this.githubName = githubName;
		this.homeLocation = homeLocation;
		this.gender = gender;
		this.gradYear = gradYear;
		this.startDate = startDate;
	}

	public String toString() {
		return firstName + " " + lastName + " (" + clientID + ")";
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
		return lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getGithubName() {
		return githubName;
	}
	
	public int getHomeLocation() {
		return homeLocation;
	}
	
	@Override
	public int compareTo(StudentModel otherPerson) {
		int comp = this.getLastName().compareTo(otherPerson.getLastName());
		if (comp == 0)
			return (this.getFirstName().compareTo(otherPerson.getFirstName()));
		else
			return comp;
	}
}
