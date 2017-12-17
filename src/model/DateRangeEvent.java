package model;

import java.util.EventObject;

import org.joda.time.DateTime;

public class DateRangeEvent extends EventObject {
	private DateTime startDate;
	private DateTime endDate;

	public DateRangeEvent(Object source, DateTime startDate, DateTime endDate) {
		super(source);

		this.startDate = startDate;
		this.endDate = endDate;
	}

	public String toString() {
		if (startDate != null && endDate != null)
			return (startDate.toString("yyyy-MM-dd") + " to " + endDate.toString("yyyy-MM-dd"));
		else
			return "";
	}

	public DateTime getStartDate() {
		return startDate;
	}

	public DateTime getEndDate() {
		return endDate;
	}
}
