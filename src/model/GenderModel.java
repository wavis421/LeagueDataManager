package model;

public class GenderModel {
	// This method seems to be more efficient than ENUM type
	private static final int GENDER_UNKNOWN = 0;
	private static final int GENDER_MALE = 1;
	private static final int GENDER_FEMALE = 2;

	public static int getGenderMale() {
		return GENDER_MALE;
	}

	public static int getGenderFemale() {
		return GENDER_FEMALE;
	}

	public static int getGenderUnknown() {
		return GENDER_UNKNOWN;
	}

	public static int convertStringToGender(String genderString) {
		switch (genderString) {
		case "Female":
			return GENDER_FEMALE;
		case "Male":
			return GENDER_MALE;
		default:
			return GENDER_UNKNOWN;
		}
	}

	public static String convertGenderToString(int gender) {
		switch (gender) {
		case GENDER_FEMALE:
			return "F";
		case GENDER_MALE:
			return "M";
		default:
			return "";
		}
	}
}
