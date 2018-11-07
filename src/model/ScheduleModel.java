package model;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

public class ScheduleModel implements Comparable<ScheduleModel> {
	private int scheduleID, dayOfWeek, duration, attCount;
	private String startTime, startTimeFormatted, className;
	private String ageMin, ageMax, ageAvg;

	public ScheduleModel(int scheduleID, int dayOfWeek, String startTime, int duration, String className) {
		this.scheduleID = scheduleID;
		this.dayOfWeek = dayOfWeek;
		this.startTime = startTime;
		this.duration = duration;
		this.className = className;
		ageMin = ageMax = ageAvg = "";

		// Need original startTime for sorting; create formatted start time as 12 hour
		MutableDateTime mdt = (new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"))).toMutableDateTime();
		mdt.setHourOfDay(Integer.parseInt(startTime.substring(0, 2)));
		mdt.setMinuteOfHour(Integer.parseInt(startTime.substring(3, 5)));
		this.startTimeFormatted = mdt.toDateTime().toString("h:mm a");
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
		return attCount;
	}

	public String getAgeMin() {
		return ageMin;
	}

	public String getAgeMax() {
		return ageMax;
	}

	public String getAgeAvg() {
		return ageAvg;
	}

	public void setMiscSchedFields(int attCount, String minAge, String maxAge, String avgAge) {
		this.attCount = attCount;
		this.ageMin = minAge;
		this.ageMax = maxAge;
		this.ageAvg = avgAge;
	}

	public boolean miscSchedFieldsMatch(ScheduleModel other) {
		// Check if miscellaneous schedule fields match
		if (attCount == other.getAttCount() && ageMin.equals(other.getAgeMin()) && ageMax.equals(other.getAgeMax())
				&& ageAvg.equals(other.getAgeAvg()))
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
