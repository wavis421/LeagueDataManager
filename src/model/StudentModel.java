package model;

public class StudentModel implements Comparable<StudentModel> {
	private int personID;
	private String lastName, firstName, githubName, location, classTime;

	public StudentModel(int personID, String lastName, String firstName, String githubName) {
		this.personID = personID;
		this.lastName = lastName;
		this.firstName = firstName;
		this.githubName = githubName;
	}

	public String toString() {
		return firstName + " " + lastName;
	}

	public int getPersonID() {
		return personID;
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
	
	public String getLocation() {
		return location;
	}
	
	public String getClassTime() {
		return classTime;
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
