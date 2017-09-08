package model;

import java.util.Comparator;

public class StudentNameModel implements Comparable<StudentNameModel> {
	private String firstName;
	private String lastName;

	// This class is required when sorting table with first/last name.
	// Natural sorting is done by first name, we want sort by last name!
	public StudentNameModel(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
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

	@Override
	public int compareTo(StudentNameModel otherName) {
		int comp = this.getLastName().compareTo(otherName.getLastName());
		if (comp == 0)
			return this.getFirstName().compareTo(otherName.getFirstName());
		else
			return comp;
	}
}
