package model;

public class LocationModel {
	// Location constants
	public static final int CLASS_LOCATION_UNKNOWN = 0;

	// Location member variables
	private int locIdx;
	private String locCode;
	private String locName, locNameLong;
	private String notes;

	public LocationModel(int locIdx, String locCode, String locName, String locNameLong, String notes) {
		this.locIdx = locIdx;
		this.locCode = locCode;
		this.locName = locName;
		this.locNameLong = locNameLong;
		this.notes = notes;
	}

	// Getters
	public int getLocIdx() {
		return locIdx;
	}

	public String getLocCode() {
		return locCode;
	}

	public String getLocName() {
		return locName;
	}

	public String getLocNameLong() {
		return locNameLong;
	}

	public String getNotes() {
		return notes;
	}
}
