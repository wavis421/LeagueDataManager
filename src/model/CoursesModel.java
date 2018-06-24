package model;

public class CoursesModel implements Comparable<CoursesModel> {
	private int scheduleID, enrollment;
	private String eventName;
	private String date;

	public CoursesModel(int scheduleID, String eventName, int enrollment) {
		this.scheduleID = scheduleID;
		this.eventName = eventName.trim();
		this.enrollment = enrollment;
		
		// Extract date field from event name
		int openParen = eventName.indexOf('(');
		int closeParen = eventName.indexOf(')');
		if (openParen >= 0 && closeParen > openParen)
			date = eventName.substring(openParen + 1, closeParen);
		else
			date = "";
	}

	public int getScheduleID() {
		return scheduleID;
	}

	public String getEventName() {
		return eventName;
	}

	public int getEnrollment() {
		return enrollment;
	}

	public String getDate() {
		return date;
	}

	@Override
	public int compareTo(CoursesModel other) {
		// Compare order is: Schedule ID, teachers
		if (this.getScheduleID() == other.getScheduleID()) {
			if (this.getEventName().equals(other.getEventName()) && this.getEnrollment() == other.getEnrollment())
				return 0;
			else
				return 2;
		}

		return (this.getScheduleID() < other.getScheduleID() ? -1 : 1);
	}
}
