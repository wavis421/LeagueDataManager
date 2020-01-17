package model_for_gui;

public class GithubModel implements Comparable<GithubModel> {
	int clientID, dowInt;
	String studentName, dow, className, githubName, teachers, currLevel;

	public GithubModel(int clientID, String studentName, String currLevel, String dow, int dowInt, String className, String githubName,
			String teachers) {
		this.clientID = clientID;
		this.studentName = studentName;
		this.currLevel = currLevel;
		this.dow = dow;
		this.dowInt = dowInt;
		this.className = className;
		this.teachers = teachers;
		this.githubName = githubName;
		if (githubName == null)
			this.githubName = ""; 
	}

	public String getStudentName() {
		return studentName;
	}

	public String getCurrLevel() {
		return currLevel;
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
	
	private int getDowInt() {
		return dowInt;
	}

	@Override
	public int compareTo(GithubModel other) {
		if (dowInt == other.getDowInt())
			return this.getStudentName().compareTo(other.getStudentName());
		else
			return (dowInt - other.getDowInt());
	}
}
