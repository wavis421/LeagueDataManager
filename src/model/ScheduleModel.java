package model;

public class ScheduleModel implements Comparable<ScheduleModel> {
	private int scheduleID, dayOfWeek;
	private String startTime, endTime, className;

	public ScheduleModel(int scheduleID, int dayOfWeek, String startTime, String endTime, String className) {
		this.scheduleID = scheduleID;
		this.dayOfWeek = dayOfWeek;
		this.startTime = startTime;
		this.endTime = endTime;
		this.className = className;
	}

	public int getScheduleID() {
		return scheduleID;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public int compareTo(ScheduleModel other) {
		// Compare order is: DayOfWeek, StartTime, ClassName, EndTime
		if (this.getClassName().equals(other.getClassName()) && this.getDayOfWeek() == other.getDayOfWeek()
				&& this.getStartTime().equals(other.getStartTime()) && this.getEndTime().equals(other.getEndTime()))
			// All fields match
			return 0;

		else if (this.getDayOfWeek() < other.getDayOfWeek())
			return -1;

		else if (this.getDayOfWeek() > other.getDayOfWeek())
			return 1;

		else {
			// Day of week matches, so now compare start time
			int compare = this.getStartTime().compareTo(other.getStartTime());
			if (compare == 0) {
				// DOW and start time both match, so next check class name
				compare = this.getClassName().compareTo(other.getClassName());
				if (compare == 0)
					// DOW, start time and class name match, end time has changed
					return this.getEndTime().compareTo(other.getEndTime());
				else
					return compare;
			} else
				// Start time does not match
				return compare;
		}
	}
}
