package model;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

public class ScheduleModel implements Comparable<ScheduleModel> {
	private int scheduleID, dayOfWeek, duration, numStudents;
	private boolean roomMismatch;
	private String startTime, startTimeFormatted, dowFormatted, className, room;
	private String youngest, oldest, averageAge, moduleCount;
	private static final String[] dowAsString = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
			"Saturday" };

	public ScheduleModel(int scheduleID, int dayOfWeek, String startTime, int duration, String className) {
		this.scheduleID = scheduleID;
		this.dayOfWeek = dayOfWeek;
		this.startTime = startTime;
		this.duration = duration;
		this.className = className;
		youngest = oldest = averageAge = "";

		// Need original startTime for sorting; create formatted start time as 12 hour
		MutableDateTime mdt = (new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"))).toMutableDateTime();
		mdt.setHourOfDay(Integer.parseInt(startTime.substring(0, 2)));
		mdt.setMinuteOfHour(Integer.parseInt(startTime.substring(3, 5)));
		this.startTimeFormatted = mdt.toDateTime().toString("h:mm a");
		this.dowFormatted = dowAsString[dayOfWeek];
	}

	public int getScheduleID() {
		return scheduleID;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public String getDayOfWeekFormatted() {
		return dowFormatted;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getStartTimeFormatted() {
		return startTimeFormatted;
	}

	public int getDuration() {
		return duration;
	}

	public String getClassName() {
		return className;
	}

	public int getAttCount() {
		return numStudents;
	}

	public String getYoungest() {
		return youngest;
	}

	public String getOldest() {
		return oldest;
	}

	public String getAverageAge() {
		return averageAge;
	}

	public String getModuleCount() {
		return moduleCount;
	}
	
	public String getRoom() {
		return room;
	}
	
	public boolean getRoomMismatch() {
		return roomMismatch;
	}

	public void setMiscSchedFields(int numStudents, String youngest, String oldest, String averageAge,
			String moduleCount, String room, boolean roomMismatch) {
		this.numStudents = numStudents;
		this.youngest = youngest;
		this.oldest = oldest;
		this.averageAge = averageAge;
		this.moduleCount = moduleCount;
		this.room = room;
		this.roomMismatch = roomMismatch;
	}

	public boolean miscSchedFieldsMatch(ScheduleModel other) {
		// Check if miscellaneous schedule fields match
		if (numStudents == other.getAttCount() && youngest.equals(other.getYoungest())
				&& oldest.equals(other.getOldest()) && averageAge.equals(other.getAverageAge())
				&& moduleCount.equals(other.getModuleCount()) 
				&& room.equals(other.getRoom()) && roomMismatch == other.getRoomMismatch())
			return true;
		else
			return false;
	}

	@Override
	public int compareTo(ScheduleModel other) {
		// Compare order is: DayOfWeek, StartTime, ClassName
		if (this.className.equals(other.className) && this.dayOfWeek == other.dayOfWeek
				&& this.startTime.equals(other.startTime))
			// All fields match
			return 0;

		else if (this.dayOfWeek < other.dayOfWeek)
			return -1;

		else if (this.dayOfWeek > other.dayOfWeek)
			return 1;

		else {
			// Day of week matches, so now compare start time
			int compare = this.startTime.compareTo(other.startTime);
			if (compare == 0) {
				// DOW and start time both match, so check class name
				compare = this.className.compareTo(other.className);
			}
			return compare;
		}
	}
}
