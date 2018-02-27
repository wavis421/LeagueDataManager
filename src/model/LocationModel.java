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
			return "Gompers";
		case CLASS_LOCATION_WILSON_MIDDLE_SCHOOL:
			return "Wilson Middle";
		case CLASS_LOCATION_SAN_MARCOS_MIDDLE:
			return "San Marcos Middle";
		case CLASS_LOCATION_SAN_ELIJO_MIDDLE:
			return "San Elijo Middle";
		default:
			return "";
		}
	}
}
