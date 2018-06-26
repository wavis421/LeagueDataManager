package model;

public class LocationModel {
	// This method seems to be more efficient than ENUM type
	public static final int CLASS_LOCATION_UNKNOWN = 0;
	private static final int CLASS_LOCATION_CARMEL_VALLEY = 1;
	private static final int CLASS_LOCATION_DOWNTOWN_CENTRAL_LIBRARY = 2;
	private static final int CLASS_LOCATION_MALCOLM_X = 3;
	private static final int CLASS_LOCATION_GOMPERS = 4;
	private static final int CLASS_LOCATION_WILSON_MIDDLE_SCHOOL = 5;
	private static final int CLASS_LOCATION_SAN_MARCOS_MIDDLE = 6;
	private static final int CLASS_LOCATION_SAN_ELIJO_MIDDLE = 7;
	private static final int CLASS_LOCATION_HOOVER_HIGH = 8;
	private static final int CLASS_LOCATION_E3_CIVIC_HIGH = 9;
	private static final int CLASS_LOCATION_OFFSITE = 10;
	private static final int CLASS_LOCATION_SERRA_HIGH = 11;
	private static final int CLASS_LOCATION_SOLANA_RANCH_ELEM = 12;
	private static final int CLASS_LOCATION_MONROE_CLARK_MIDDLE = 13;
	private static final int CLASS_LOCATION_ONLINE = 14;
	private static final int CLASS_LOCATION_ROBO_CAMP = 15;
	private static final int CLASS_LOCATION_SAN_MARCOS_DISTRICT_OFFICE = 16;
	private static final int CLASS_LOCATION_SAN_MARCOS_CAMPUS = 17;

	private static final int CLASS_LAST_LOCATION_NUM = 17; // Must be last!

	private static final String[] locationCodes = new String[] { "", "CV", "DL", "MX", "GPA", "WMS", "SMM", "SEM",
			"HHS", "E3", "OS", "SHS", "SRE", "MCM", "OL", "ORC", "SMD", "SMC" };

	public static int convertStringToLocation(String classString) {
		if (classString.startsWith("Carmel Valley"))
			return CLASS_LOCATION_CARMEL_VALLEY;
		else if (classString.startsWith("Downtown Central Library"))
			return CLASS_LOCATION_DOWNTOWN_CENTRAL_LIBRARY;
		else if (classString.startsWith("Malcolm X Library"))
			return CLASS_LOCATION_MALCOLM_X;
		else if (classString.startsWith("Gompers Prep"))
			return CLASS_LOCATION_GOMPERS;
		else if (classString.startsWith("Wilson Middle School"))
			return CLASS_LOCATION_WILSON_MIDDLE_SCHOOL;
		else if (classString.startsWith("San Marcos Middle"))
			return CLASS_LOCATION_SAN_MARCOS_MIDDLE;
		else if (classString.startsWith("San Elijo Middle"))
			return CLASS_LOCATION_SAN_ELIJO_MIDDLE;
		else if (classString.startsWith("Hoover High"))
			return CLASS_LOCATION_HOOVER_HIGH;
		else if (classString.startsWith("E3Civic High") || classString.startsWith("E3 Civic High"))
			return CLASS_LOCATION_E3_CIVIC_HIGH;
		else if (classString.startsWith("Offsite"))
			return CLASS_LOCATION_OFFSITE;
		else if (classString.startsWith("Serra High"))
			return CLASS_LOCATION_SERRA_HIGH;
		else if (classString.startsWith("Solana Ranch Elem"))
			return CLASS_LOCATION_SOLANA_RANCH_ELEM;
		else if (classString.startsWith("Monroe Clark Middle"))
			return CLASS_LOCATION_MONROE_CLARK_MIDDLE;
		else if (classString.toLowerCase().startsWith("on-line"))
			return CLASS_LOCATION_ONLINE;
		else if (classString.toLowerCase().startsWith("robocamp"))
			return CLASS_LOCATION_ROBO_CAMP;
		else if (classString.startsWith("San Marcos District"))
			return CLASS_LOCATION_SAN_MARCOS_DISTRICT_OFFICE;
		else if (classString.startsWith("San Marcos Campus"))
			return CLASS_LOCATION_SAN_MARCOS_CAMPUS;
		else
			return CLASS_LOCATION_UNKNOWN;
	}

	public static String convertLocationToString(int location) {
		switch (location) {
		case CLASS_LOCATION_CARMEL_VALLEY:
			return "Carmel Valley";
		case CLASS_LOCATION_DOWNTOWN_CENTRAL_LIBRARY:
			return "Downtown Library";
		case CLASS_LOCATION_MALCOLM_X:
			return "Malcolm X";
		case CLASS_LOCATION_GOMPERS:
			return "Gompers Prep";
		case CLASS_LOCATION_WILSON_MIDDLE_SCHOOL:
			return "Wilson Middle";
		case CLASS_LOCATION_SAN_MARCOS_MIDDLE:
			return "San Marcos Middle";
		case CLASS_LOCATION_SAN_MARCOS_DISTRICT_OFFICE:
			return "San Marcos District";
		case CLASS_LOCATION_SAN_MARCOS_CAMPUS:
			return "San Marcos Campus";
		case CLASS_LOCATION_SAN_ELIJO_MIDDLE:
			return "San Elijo Middle";
		case CLASS_LOCATION_HOOVER_HIGH:
			return "Hoover High";
		case CLASS_LOCATION_E3_CIVIC_HIGH:
			return "E3 Civic High";
		case CLASS_LOCATION_OFFSITE:
			return "Offsite";
		case CLASS_LOCATION_SERRA_HIGH:
			return "Serra High";
		case CLASS_LOCATION_SOLANA_RANCH_ELEM:
			return "Solana Ranch Elem";
		case CLASS_LOCATION_MONROE_CLARK_MIDDLE:
			return "Monroe Clark Middle";
		case CLASS_LOCATION_ONLINE:
			return "On-line";
		case CLASS_LOCATION_ROBO_CAMP:
			return "RoboCamp";
		default:
			return "";
		}
	}

	public static String getLocationCodeString(int locNum) {
		locNum++;
		if (locNum <= CLASS_LAST_LOCATION_NUM)
			return locationCodes[locNum];
		else
			return "";
	}

	public static int getNumLocactions() {
		return CLASS_LAST_LOCATION_NUM;
	}

	public static int findLocationCodeMatch(String locCode, String locLongString) {
		for (int i = 1; i <= CLASS_LAST_LOCATION_NUM; i++) {
			if (locationCodes[i].equals(locCode) && convertStringToLocation(locLongString) == i)
				return i;
		}
		return -1;
	}
}
