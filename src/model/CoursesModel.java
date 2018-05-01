package model;

public class CoursesModel implements Comparable<CoursesModel> {
	private int scheduleID, enrollment;
	private String eventName;

	public CoursesModel(int scheduleID, String eventName, int enrollment) {
		this.scheduleID = scheduleID;
		this.eventName = eventName;
		this.enrollment = enrollment;
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
