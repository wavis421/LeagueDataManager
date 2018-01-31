package model;

public class StaffMemberModel {
	// clientID and staffID are identical except for TA's
	private String clientID;
	private String staffID;
	private String fullName;
	private String category; // teacher, volunteer, TA

	public StaffMemberModel(String clientID, String staffID, String fullName, String category) {
		this.clientID = clientID;
		if (staffID != null && (staffID.startsWith("null") || staffID.equals("")))
			this.staffID = null;
		else
			this.staffID = staffID;
		this.fullName = fullName;
		this.category = category;
	}

	@Override
	public String toString() {
		return clientID + ", " + staffID + ": " + fullName + " (" + category + ")";
	}

	public String getClientID() {
		return clientID;
	}

	public String getStaffID() {
		if (staffID == null)
			return clientID;
		else
			return staffID;
	}

	public String getFullName() {
		return fullName;
	}

	public String getCategory() {
		return category;
	}
}
