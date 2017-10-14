package model;

public class StudentNameModel implements Comparable<StudentNameModel> {
	private String firstName;
	private String lastName;
	private boolean isInMasterDb;

	// This class is required when sorting table with first/last name.
	// Natural sorting is done by first name, we want sort by last name!
	public StudentNameModel(String firstName, String lastName, boolean isInMasterDb) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.isInMasterDb = isInMasterDb;
	}

	public String toString() {
		return firstName + " " + lastName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public boolean getIsInMasterDb() {
		return isInMasterDb;
	}

	@Override
	public int compareTo(StudentNameModel otherName) {
		int comp = this.getFirstName().compareTo(otherName.getFirstName());
		if (comp == 0)
			return this.getLastName().compareTo(otherName.getLastName());
		else
			return comp;
	}
}
