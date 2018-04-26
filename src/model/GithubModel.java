package model;

public class GithubModel implements Comparable<GithubModel> {
	int clientID;
	String studentName, dow, className, githubName, teachers;

	public GithubModel(int clientID, String studentName, String dow, String className, String githubName,
			String teachers) {
		this.clientID = clientID;
		this.studentName = studentName;
		this.dow = dow;
		this.className = className;
		this.teachers = teachers;
		this.githubName = githubName;
		if (githubName == null)
			this.githubName = "";
	}

	public String getStudentName() {
		return studentName;
	}

	public String getDow() {
		return dow;
	}

	public String getClassName() {
		return className;
	}

	public String getGithubName() {
		return githubName;
	}

	public int getClientID() {
		return clientID;
	}

	public String getTeachers() {
		return teachers;
	}

	@Override
	public int compareTo(GithubModel otherPerson) {
		return this.getStudentName().compareTo(otherPerson.getStudentName());
	}
}
