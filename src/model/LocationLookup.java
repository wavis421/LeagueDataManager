package model;

import java.util.ArrayList;
import java.util.Collections;

public class LocationLookup {
	// Locations are static.
	// They are read from the online database and do not change.

	// Locations are represented by Location Index.
	// Each Location Index is mapped to a location code (2-3 digits) and
	// a full location name.

	private static ArrayList<LocationModel> locList;

	public static void setLocationData(ArrayList<LocationModel> list) {
		locList = list;
	}

	public static int convertStringToLocation(String locName) {
		for (LocationModel m : locList) {
			if (locName.startsWith(m.getLocNameLong()))
				return m.getLocIdx();
		}
		return LocationModel.CLASS_LOCATION_UNKNOWN;
	}

	public static String convertLocationToString(int locNum) {
		for (LocationModel m : locList) {
			if (locNum == m.getLocIdx())
				return m.getLocName();
		}
		return "";
	}

	public static String getLocationCodeString(int locNum) {
		for (LocationModel m : locList) {
			if (locNum == m.getLocIdx())
				return m.getLocCode();
		}
		return "";
	}

	public static int getNumLocactions() {
		return locList.size();
	}

	public static boolean findLocationCodeMatch(String locCode, String locStringLong) {
		// Any class can be made 'virtual' (temporary? added to support virtual classes during COVID-19)
		if (locStringLong.startsWith("Virtual"))
			return true;
		
		for (LocationModel m : locList) {
			if (m.getLocCode().equals(locCode) && locStringLong.startsWith(m.getLocNameLong()))
				return true;
		}
		return false;
	}

	public static String getAllLocsForDisplay() {
		String allLocs = "";
		Collections.sort(locList);
		for (LocationModel m : locList) {
			allLocs += "\t" + m.getLocCode() + "\t" + m.getLocNameLong() + "\n";
		}
		return allLocs;
	}
}
