package controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import model.AttendanceEventModel;
import model.CoursesModel;
import model.LogDataModel;
import model.MySqlDatabase;
import model.MySqlDbLogging;
import model.SalesForceAttendanceModel;
import model.SalesForceStaffHoursModel;
import model.ScheduleModel;
import model.StaffMemberModel;
import model.StudentImportModel;
import model.StudentModel;
import model.StudentNameModel;

public class Pike13Api {
	private final String USER_AGENT = "Mozilla/5.0";

	// Custom field names for client data
	private final String GENDER_FIELD = "custom_field_106320";
	private final String GITHUB_FIELD = "custom_field_127885";
	private final String GRAD_YEAR_FIELD = "custom_field_145902";
	private final String SCHOOL_ATTENDING_FIELD = "custom_field_106316";
	private final String TSHIRT_SIZE_FIELD = "custom_field_106318";
	private final String EMERG_CONTACT_NAME_FIELD = "custom_field_106321";
	private final String EMERG_CONTACT_PHONE_FIELD = "custom_field_106322";
	private final String EMERG_CONTACT_EMAIL_FIELD = "custom_field_149434";
	private final String CURRENT_GRADE_FIELD = "custom_field_106463";
	private final String HOME_PHONE_FIELD = "custom_field_106498";
	private final String HEAR_ABOUT_US_FIELD = "custom_field_128371";
	private final String WHO_TO_THANK_FIELD = "custom_field_147039";
	private final String FINANCIAL_AID_FIELD = "custom_field_106317";
	private final String FINANCIAL_AID_PERCENT_FIELD = "custom_field_108413";
	private final String GRANT_INFO_FIELD = "custom_field_148317";
	private final String LEAVE_REASON_FIELD = "custom_field_148655";
	private final String STOP_EMAIL_FIELD = "custom_field_149207";

	// Custom field names for Staff Member data
	private final String STAFF_SF_CLIENT_ID_FIELD = "custom_field_152501";
	private final String STAFF_CATEGORY_FIELD = "custom_field_106325";
	private final String STAFF_GENDER_FIELD = "custom_field_106320";
	private final String STAFF_HOME_PHONE_FIELD = "custom_field_106498";
	private final String STAFF_OCCUPATION_FIELD = "custom_field_106324";
	private final String STAFF_EMPLOYER_FIELD = "custom_field_133180";
	private final String STAFF_START_INFO_FIELD = "custom_field_140367";
	private final String STAFF_ALTERNATE_EMAIL_FIELD = "custom_field_140368";
	private final String STAFF_KEY_HOLDER_FIELD = "custom_field_149098";
	private final String STAFF_LIVE_SCAN_DATE_FIELD = "custom_field_149097";
	private final String STAFF_T_SHIRT_FIELD = "custom_field_106737";
	private final String STAFF_WHERE_DID_YOU_HEAR_FIELD = "custom_field_128371";
	private final String STAFF_LEAVE_FIELD = "custom_field_149559";
	private final String STAFF_EMERG_NAME_FIELD = "custom_field_106321";
	private final String STAFF_EMERG_EMAIL_FIELD = "custom_field_149434";
	private final String STAFF_EMERG_PHONE_FIELD = "custom_field_106322";
	private final String STAFF_CURR_BOARD_MEMBER_FIELD = "custom_field_153299";
	private final String STAFF_CURR_STAFF_MEMBER_FIELD = "custom_field_153300";
	private final String STAFF_GITHUB_USER_FIELD = "custom_field_127885";
	private final String STAFF_TITLE_FIELD = "custom_field_154726";

	// Indices for client data
	private final int CLIENT_ID_IDX = 0;
	private final int FIRST_NAME_IDX = 1;
	private final int LAST_NAME_IDX = 2;
	private final int GITHUB_IDX = 3;
	private final int GRAD_YEAR_IDX = 4;
	private final int GENDER_IDX = 5;
	private final int HOME_LOC_IDX = 6;
	private final int FIRST_VISIT_IDX = 7;
	private final int EMAIL_IDX = 10;
	private final int ACCT_MGR_EMAIL_IDX = 11;
	private final int EMERG_EMAIL_IDX = 12;
	private final int MOBILE_PHONE_IDX = 13;
	private final int ACCT_MGR_PHONE_IDX = 14;
	private final int HOME_PHONE_IDX = 15;
	private final int EMERG_PHONE_IDX = 16;

	// Indices for client data import to SF
	private final int CLIENT_SF_ID_IDX = 0;
	private final int CLIENT_EMAIL_IDX = 1;
	private final int CLIENT_MOBILE_PHONE_IDX = 2;
	private final int CLIENT_FULL_ADDRESS_IDX = 3;
	private final int CLIENT_BIRTHDATE_IDX = 4;
	private final int CLIENT_COMPLETED_VISITS_IDX = 5;
	private final int CLIENT_FUTURE_VISITS_IDX = 6;
	private final int CLIENT_HAS_SIGNED_WAIVER_IDX = 7;
	private final int CLIENT_HAS_MEMBERSHIP_IDX = 8;
	private final int CLIENT_PASS_ON_FILE_IDX = 9;
	private final int CLIENT_HOME_LOC_LONG_IDX = 10;
	private final int CLIENT_FIRST_NAME_IDX = 11;
	private final int CLIENT_LAST_NAME_IDX = 12;
	private final int CLIENT_SCHOOL_NAME_IDX = 13;
	private final int CLIENT_TSHIRT_SIZE_IDX = 14;
	private final int CLIENT_GENDER_IDX = 15;
	private final int CLIENT_EMERG_CONTACT_NAME_IDX = 16;
	private final int CLIENT_EMERG_CONTACT_PHONE_IDX = 17;
	private final int CLIENT_CURR_GRADE_IDX = 18;
	private final int CLIENT_HEAR_ABOUT_US_IDX = 19;
	private final int CLIENT_GRAD_YEAR_IDX = 20;
	private final int CLIENT_WHO_TO_THANK_IDX = 21;
	private final int CLIENT_EMERG_CONTACT_EMAIL_IDX = 22;
	private final int CLIENT_FINANCIAL_AID_IDX = 23;
	private final int CLIENT_FINANCIAL_AID_PERCENT_IDX = 24;
	private final int CLIENT_GITHUB_IDX = 25;
	private final int CLIENT_GRANT_INFO_IDX = 26;
	private final int CLIENT_LEAVE_REASON_IDX = 27;
	private final int CLIENT_STOP_EMAIL_IDX = 28;
	private final int CLIENT_FIRST_VISIT_IDX = 29;
	private final int CLIENT_HOME_PHONE_IDX = 30;
	private final int CLIENT_ACCOUNT_MGR_NAMES_IDX = 31;
	private final int CLIENT_ACCOUNT_MGR_EMAILS_IDX = 32;
	private final int CLIENT_ACCOUNT_MGR_PHONES_IDX = 33;
	private final int CLIENT_DEPENDENT_NAMES_IDX = 34;

	// Indices for enrollment data
	private final int ENROLL_CLIENT_ID_IDX = 0;
	private final int ENROLL_FULL_NAME_IDX = 1;
	private final int ENROLL_SERVICE_DATE_IDX = 2;
	private final int ENROLL_EVENT_NAME_IDX = 3;
	private final int ENROLL_VISIT_ID_IDX = 4;
	private final int ENROLL_TEACHER_NAMES_IDX = 5;
	private final int ENROLL_SERVICE_CATEGORY_IDX = 6;
	private final int ENROLL_STATE_IDX = 7;

	// Indices for SalesForce enrollment data
	private final int SF_PERSON_ID_IDX = 0;
	private final int SF_SERVICE_DATE_IDX = 1;
	private final int SF_SERVICE_TIME_IDX = 2;
	private final int SF_EVENT_NAME_IDX = 3;
	private final int SF_SERVICE_NAME_IDX = 4;
	private final int SF_SERVICE_CATEGORY_IDX = 5;
	private final int SF_STATE_IDX = 6;
	private final int SF_VISIT_ID_IDX = 7;
	private final int SF_EVENT_OCCURRENCE_ID_IDX = 8;
	private final int SF_LOCATION_NAME_IDX = 9;
	private final int SF_INSTRUCTOR_NAMES_IDX = 10;
	private final int SF_FULL_NAME_IDX = 11;

	// Indices for schedule data
	private final int SCHED_SERVICE_DAY_IDX = 0;
	private final int SCHED_SERVICE_TIME_IDX = 1;
	private final int SCHED_DURATION_MINS_IDX = 2;
	private final int SCHED_WKLY_EVENT_NAME_IDX = 3;

	// Indices for courses data
	private final int COURSES_SCHEDULE_ID_IDX = 0;
	private final int COURSES_EVENT_NAME_IDX = 1;
	private final int COURSE_ENROLLMENT_IDX = 2;

	// Indices for Person Plans by Product ID
	private final int PLAN2_IS_CANCELED_IDX = 0;

	// Indices for Staff Member data
	private final int TEACHER_CLIENT_ID_IDX = 0;
	private final int TEACHER_FIRST_NAME_IDX = 1;
	private final int TEACHER_LAST_NAME_IDX = 2;
	private final int TEACHER_SF_CLIENT_ID_IDX = 3;
	private final int TEACHER_CATEGORY_IDX = 4;
	private final int TEACHER_ROLE_IDX = 5;
	private final int TEACHER_OCCUPATION_IDX = 6;
	private final int TEACHER_EMPLOYER_IDX = 7;
	private final int TEACHER_START_INFO_IDX = 8;
	private final int TEACHER_GENDER_IDX = 9;
	private final int TEACHER_PHONE_IDX = 10;
	private final int TEACHER_HOME_PHONE_IDX = 11;
	private final int TEACHER_ADDRESS_IDX = 12;
	private final int TEACHER_EMAIL_IDX = 13;
	private final int TEACHER_ALTERNATE_EMAIL_IDX = 14;
	private final int TEACHER_HOME_LOCATION_IDX = 15;
	private final int TEACHER_GITHUB_USER_IDX = 16;
	private final int TEACHER_BIRTHDATE_IDX = 17;
	private final int TEACHER_PAST_EVENTS_IDX = 18;
	private final int TEACHER_FUTURE_EVENTS_IDX = 19;
	private final int TEACHER_KEY_HOLDER_IDX = 20;
	private final int TEACHER_LIVE_SCAN_DATE_IDX = 21;
	private final int TEACHER_T_SHIRT_IDX = 22;
	private final int TEACHER_WHERE_DID_YOU_HEAR_IDX = 23;
	private final int TEACHER_LEAVE_IDX = 24;
	private final int TEACHER_EMERG_NAME_IDX = 25;
	private final int TEACHER_EMERG_EMAIL_IDX = 26;
	private final int TEACHER_EMERG_PHONE_IDX = 27;
	private final int TEACHER_CURR_BOARD_MEMBER_IDX = 28;
	private final int TEACHER_CURR_STAFF_MEMBER_IDX = 29;
	private final int TEACHER_IS_ALSO_CLIENT_IDX = 30;
	private final int TEACHER_TITLE_IDX = 31;

	// Indices for Staff Hours data
	private final int STAFF_CLIENT_ID_IDX = 0;
	private final int STAFF_EVENT_SERVICE_NAME_IDX = 1;
	private final int STAFF_EVENT_SERVICE_DATE_IDX = 2;
	private final int STAFF_EVENT_SERVICE_TIME_IDX = 3;
	private final int STAFF_EVENT_DURATION_IDX = 4;
	private final int STAFF_EVENT_LOCATION_IDX = 5;
	private final int STAFF_EVENT_COMPLETED_COUNT_IDX = 6;
	private final int STAFF_EVENT_NO_SHOW_COUNT_IDX = 7;
	private final int STAFF_EVENT_CANCELED_COUNT_IDX = 8;
	private final int STAFF_EVENT_NAME_IDX = 9;
	private final int STAFF_EVENT_SCHEDULE_ID_IDX = 10;
	private final int STAFF_EVENT_FULL_NAME_IDX = 11;
	private final int STAFF_EVENT_SERVICE_CATEGORY_IDX = 12;

	private final String getClientData = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit and filters
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"person_id\",\"first_name\",\"last_name\",\"" + GITHUB_FIELD + "\",\"" + GRAD_YEAR_FIELD + "\","
			+ "            \"" + GENDER_FIELD + "\",\"home_location_name\",\"first_visit_date\","
			+ "            \"future_visits\",\"completed_visits\",\"email\",\"account_manager_emails\","
			+ "            \"" + EMERG_CONTACT_EMAIL_FIELD + "\",\"phone\",\"account_manager_phones\","
			+ "            \"" + HOME_PHONE_FIELD + "\",\"" + EMERG_CONTACT_PHONE_FIELD + "\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500";
	
	private final String getClientData2 = "},"
			// Filter on Dependents NULL and either has future visits or recent completed visits
			+ "\"filter\":[\"and\",[[\"eq\",\"person_state\",\"active\"],"
			+ "                     [\"emp\",\"dependent_names\"],"
			+ "                     [\"or\",[[\"gt\",\"future_visits\",0],"
			+ "                              [\"gt\",\"last_visit_date\",\"0000-00-00\"],"
			+ "                              [\"eq\",\"has_membership\",\"t\"]]]]]}}}";

	private final String getClientDataForSF = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit and filters
			+ "\"attributes\":{"
			// Select fields for client data import to SF
			+ "\"fields\":[\"person_id\",\"email\",\"phone\",\"address\",\"birthdate\",\"completed_visits\",\"future_visits\","
			+ "            \"has_signed_waiver\",\"has_membership\",\"current_plans\","
			+ "            \"home_location_name\",\"first_name\",\"last_name\",\"" + SCHOOL_ATTENDING_FIELD + "\","
			+ "            \"" + TSHIRT_SIZE_FIELD + "\",\"" + GENDER_FIELD + "\",\"" + EMERG_CONTACT_NAME_FIELD + "\","
			+ "            \"" + EMERG_CONTACT_PHONE_FIELD + "\",\"" + CURRENT_GRADE_FIELD + "\","
			+ "            \"" + HEAR_ABOUT_US_FIELD + "\",\"" + GRAD_YEAR_FIELD + "\",\"" + WHO_TO_THANK_FIELD + "\","
			+ "            \"" + EMERG_CONTACT_EMAIL_FIELD + "\",\"" + FINANCIAL_AID_FIELD + "\",\"" + FINANCIAL_AID_PERCENT_FIELD + "\","
			+ "            \"" + GITHUB_FIELD + "\",\"" + GRANT_INFO_FIELD + "\",\"" + LEAVE_REASON_FIELD + "\","
			+ "            \"" + STOP_EMAIL_FIELD + "\",\"first_visit_date\",\"" + HOME_PHONE_FIELD + "\","
			+ "            \"account_manager_names\",\"account_manager_emails\",\"account_manager_phones\",\"dependent_names\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500";

	private final String getClientDataForSF2 = "},"
			// Filter on Dependents NULL or not NULL ("MMM" filled in at run-time)
			+ "\"filter\":[\"and\",[[\"MMM\",\"dependent_names\"],"
			+ "                     [\"eq\",\"person_state\",\"active\"]]]}}}";

	// Getting enrollment data is in 2 parts since page info gets inserted in middle.
	private final String getEnrollmentStudentTracker = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"person_id\",\"full_name\",\"service_date\",\"event_name\",\"visit_id\",\"instructor_names\","
			+ "            \"service_category\",\"state\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500";

	private final String getEnrollmentStudentTracker2 = "},"
			// Filter on State completed and since date OR make-up class for this week
			+ "\"filter\":[\"or\",[[\"and\",[[\"eq\",\"state\",\"completed\"],"
			+ "                              [\"btw\",\"service_date\",[\"0000-00-00\",\"1111-11-11\"]]]],"
			+ "                    [\"and\",[[\"eq\",\"state\",\"registered\"],"
			+ "                              [\"btw\",\"service_date\",[\"2222-22-22\",\"3333-33-33\"]],"
			+ "                              [\"starts\",\"service_category\",\"class\"]]]]]}}}";

	private final String getEnrollmentStudentTracker2WithName = "},"
			// Filter on State completed, since date and student name
			+ "\"filter\":[\"and\",[[\"eq\",\"state\",\"completed\"],"
			+ "                     [\"btw\",\"service_date\",[\"0000-00-00\",\"1111-11-11\"]],"
			+ "                     [\"starts\",\"service_category\",\"Class\"],"
			+ "                     [\"eq\",\"full_name\",\"NNNNNN\"]]]}}}";

	private final String getCourseEnrollmentStudentTracker2 = "},"
			// Filter on course, state completed or enrolled and since date
			+ "\"filter\":[\"and\",[[\"or\",[[\"eq\",\"state\",\"completed\"],"
			+ "                              [\"eq\",\"state\",\"registered\"]]],"
			+ "                     [\"btw\",\"service_date\",[\"0000-00-00\",\"1111-11-11\"]],"
			+ "                     [\"starts\",\"service_type\",\"course\"],"
			+ "                     [\"nemp\",\"event_name\"]]]}}}";

	private final String getEnrollmentSalesForce = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"person_id\",\"service_date\",\"service_time\",\"event_name\",\"service_name\","
			+ "            \"service_category\",\"state\",\"visit_id\",\"event_occurrence_id\","
			+ "            \"service_location_name\",\"instructor_names\",\"full_name\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500";

	private final String getEnrollmentSalesForce2 = "},"
			// Filter on between start/end dates OR all future summer slam, workshops and leave
			+ "\"filter\":[\"or\",[[\"btw\",\"service_date\",[\"0000-00-00\",\"1111-11-11\"]],"
			+ "                    [\"and\",[[\"gt\",\"service_date\",\"2222-22-22\"],"
			+ "                              [\"starts\",\"service_category\",\"class jslam\"]]],"
			+ "                    [\"and\",[[\"gt\",\"service_date\",\"2222-22-22\"],"
			+ "                              [\"starts\",\"service_category\",\"works\"]]],"
			+ "                    [\"and\",[[\"gt\",\"service_date\",\"2222-22-22\"],"
			+ "                              [\"eq\",\"service_category\",\"leave\"]]]]]}}}";

	// Get schedule data
	private final String getScheduleData = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit and filters
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"service_day\",\"service_time\",\"duration_in_minutes\",\"event_name\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500},"
			// Filter on 'this week' and 'starts with Class' and event name not null
			+ "\"filter\":[\"and\",[[\"btw\",\"service_date\",[\"0000-00-00\",\"1111-11-11\"]],"
			+ "                     [\"starts\",\"service_category\",\"Class\"],"
			+ "                     [\"ne\",\"service_category\",\"class jslam\"],"
			+ "                     [\"nemp\",\"event_name\"]]]}}}";

	// Get workshop and summer slam data
	private final String getCoursesData = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit and filters
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"event_id\",\"event_name\",\"enrollment_count\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500},"
			// Filter on 'this week' and 'starts with Class' and event name not null
			+ "\"filter\":[\"and\",[[\"btw\",\"service_date\",[\"0000-00-00\",\"1111-11-11\"]],"
			+ "                     [\"starts\",\"service_type\",\"course\"]]]}}}";

	// Get person plan data by product ID
	private final String getPersonPlanDataByProductId = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit and filters
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"is_canceled\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500},"
			// Filter on Product ID for this client
			+ "\"filter\":[\"and\",[[\"eq\",\"person_id\",IIII],"
			+ "                     [\"eq\",\"product_id\",PPPP]]]}}}";

	// Get staff member data
	private final String getStaffMemberData = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit and filters
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"person_id\",\"first_name\",\"last_name\",\"" + STAFF_SF_CLIENT_ID_FIELD + "\","
			+ "            \"" + STAFF_CATEGORY_FIELD + "\",\"role\",\"" + STAFF_OCCUPATION_FIELD + "\","
			+ "            \"" + STAFF_EMPLOYER_FIELD + "\",\"" + STAFF_START_INFO_FIELD + "\","
			+ "            \"" + STAFF_GENDER_FIELD + "\",\"phone\",\"" + STAFF_HOME_PHONE_FIELD + "\",\"address\","
			+ "            \"email\",\"" + STAFF_ALTERNATE_EMAIL_FIELD + "\",\"home_location_name\","
			+ "            \"" + STAFF_GITHUB_USER_FIELD + "\",\"birthdate\",\"past_events\","
			+ "            \"future_events\",\"" + STAFF_KEY_HOLDER_FIELD + "\",\"" + STAFF_LIVE_SCAN_DATE_FIELD + "\","
			+ "            \"" + STAFF_T_SHIRT_FIELD + "\",\"" + STAFF_WHERE_DID_YOU_HEAR_FIELD + "\","
			+ "            \"" + STAFF_LEAVE_FIELD + "\",\"" + STAFF_EMERG_NAME_FIELD + "\",\"" + STAFF_EMERG_EMAIL_FIELD + "\","
			+ "            \"" + STAFF_EMERG_PHONE_FIELD + "\",\"" + STAFF_CURR_BOARD_MEMBER_FIELD + "\","
			+ "            \"" + STAFF_CURR_STAFF_MEMBER_FIELD + "\",\"also_client\",\"" + STAFF_TITLE_FIELD + "\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500},"
			// Filter on Staff Category and staff member active
			+ "\"filter\":[\"and\",[[\"eq\",\"person_state\",\"active\"],"
			+ "                     [\"or\",[[\"eq\",\"" + STAFF_CATEGORY_FIELD + "\",\"Teaching Staff\"],"
			+ "                              [\"eq\",\"" + STAFF_CATEGORY_FIELD + "\",\"Vol Teacher\"],"
			+ "                              [\"eq\",\"" + STAFF_CATEGORY_FIELD + "\",\"Volunteer\"],"
			+ "                              [\"eq\",\"" + STAFF_CATEGORY_FIELD + "\",\"Student TA\"],"
			+ "                              [\"eq\",\"" + STAFF_CATEGORY_FIELD + "\",\"Admin Staff\"],"
			+ "                              [\"eq\",\"" + STAFF_CATEGORY_FIELD + "\",\"Board Member\"]]]]]}}}";

	// Get staff hours data
	private final String getStaffHoursSalesForce = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"person_id\",\"service_name\",\"service_date\",\"service_time\",\"duration_in_hours\","
			+ "            \"service_location_name\",\"completed_enrollment_count\",\"noshowed_enrollment_count\","
			+ "            \"late_canceled_enrollment_count\",\"event_name\",\"event_occurrence_id\",\"full_name\","
			+ "            \"service_category\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500";

	private final String getStaffHoursSalesForce2 = "},"
			// Filter on since date
			+ "\"filter\":[\"and\",[[\"btw\",\"service_date\",[\"0000-00-00\",\"1111-11-11\"]],"
			+ "                     [\"or\",[[\"eq\",\"attendance_completed\",\"t\"],"
			+ "                              [\"eq\",\"service_name\",\"Volunteer Time\"]]],"
			+ "                     [\"wo\",\"home_location_name\",\"Tax ID#\"]]]}}}";

	private MySqlDatabase mysqlDb;
	private String pike13Token;

	public Pike13Api(MySqlDatabase mysqlDb, String pike13Token) {
		this.mysqlDb = mysqlDb;
		this.pike13Token = pike13Token;
	}

	public ArrayList<StudentImportModel> getClients() {
		ArrayList<StudentImportModel> studentList = new ArrayList<StudentImportModel>();
		boolean hasMore = false;
		String lastKey = "";

		// Insert since date for completed visit (in last 30 days)
		String clients2 = getClientData2.replaceFirst("0000-00-00",
				new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles")).
					minusDays(MySqlDatabase.CLASS_ATTEND_NUM_DAYS_TO_KEEP).toString("yyyy-MM-dd"));

		do {
			// Get URL connection with authorization
			HttpURLConnection conn;

			// Send the query
			if (hasMore)
				conn = sendQueryToUrl("clients", getClientData + ",\"starting_after\":\"" + lastKey + "\"" + clients2);
			else
				conn = sendQueryToUrl("clients", getClientData + clients2);

			if (conn == null)
				return studentList;

			// Get input stream and read data
			JsonObject jsonObj = readInputStream(conn);
			if (jsonObj == null) {
				conn.disconnect();
				return studentList;
			}
			JsonArray jsonArray = jsonObj.getJsonArray("rows");

			for (int i = 0; i < jsonArray.size(); i++) {
				// Get fields for each person
				JsonArray personArray = (JsonArray) jsonArray.get(i);
				String firstName = stripQuotes(personArray.get(FIRST_NAME_IDX).toString());

				if (!firstName.startsWith("Guest")) {
					// Get fields for this Json array entry
					StudentImportModel model = new StudentImportModel(personArray.getInt(CLIENT_ID_IDX),
							stripQuotes(personArray.get(LAST_NAME_IDX).toString()),
							stripQuotes(personArray.get(FIRST_NAME_IDX).toString()),
							stripQuotes(personArray.get(GITHUB_IDX).toString()),
							stripQuotes(personArray.get(GENDER_IDX).toString()),
							stripQuotes(personArray.get(FIRST_VISIT_IDX).toString()),
							stripQuotes(personArray.get(HOME_LOC_IDX).toString()),
							stripQuotes(personArray.get(GRAD_YEAR_IDX).toString()),
							stripQuotes(personArray.get(EMAIL_IDX).toString()),
							stripQuotes(personArray.get(ACCT_MGR_EMAIL_IDX).toString()),
							stripQuotes(personArray.get(EMERG_EMAIL_IDX).toString()),
							stripQuotes(personArray.get(MOBILE_PHONE_IDX).toString()),
							stripQuotes(personArray.get(ACCT_MGR_PHONE_IDX).toString()),
							stripQuotes(personArray.get(HOME_PHONE_IDX).toString()),
							stripQuotes(personArray.get(EMERG_PHONE_IDX).toString()));
					studentList.add(model);
				}
			}

			// Check to see if there are more pages
			hasMore = jsonObj.getBoolean("has_more");
			if (hasMore)
				lastKey = jsonObj.getString("last_key");

			conn.disconnect();

		} while (hasMore);

		return studentList;
	}

	public ArrayList<StudentImportModel> getClientsForSfImport(boolean isAcctMgr) {
		ArrayList<StudentImportModel> studentList = new ArrayList<StudentImportModel>();
		boolean hasMore = false;
		String lastKey = "";

		do {
			// URL connection with authorization
			HttpURLConnection conn;

			// Send the query (set dependents not empty for manager)
			String cmd2;
			if (isAcctMgr)
				cmd2 = getClientDataForSF2.replace("MMM", "nemp");
			else
				cmd2 = getClientDataForSF2.replace("MMM", "emp");
			if (hasMore)
				conn = sendQueryToUrl("clients", getClientDataForSF + ",\"starting_after\":\"" + lastKey + "\"" + cmd2);
			else
				conn = sendQueryToUrl("clients", getClientDataForSF + cmd2);

			// Check result
			if (conn == null)
				return null;

			// Get input stream and read data
			JsonObject jsonObj = readInputStream(conn);
			if (jsonObj == null) {
				conn.disconnect();
				return null;
			}
			JsonArray jsonArray = jsonObj.getJsonArray("rows");

			for (int i = 0; i < jsonArray.size(); i++) {
				// Get fields for each person
				JsonArray personArray = (JsonArray) jsonArray.get(i);

				// Get fields for this Json array entry
				StudentImportModel model = new StudentImportModel(personArray.getInt(CLIENT_SF_ID_IDX),
						personArray.getString(CLIENT_FIRST_NAME_IDX),
						personArray.getString(CLIENT_LAST_NAME_IDX),
						stripQuotes(personArray.get(CLIENT_GENDER_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_BIRTHDATE_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_CURR_GRADE_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_GRAD_YEAR_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_FIRST_VISIT_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_HOME_LOC_LONG_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_EMAIL_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_MOBILE_PHONE_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_FULL_ADDRESS_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_SCHOOL_NAME_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_GITHUB_IDX).toString()),
						personArray.getInt(CLIENT_COMPLETED_VISITS_IDX),
						personArray.getInt(CLIENT_FUTURE_VISITS_IDX),
						stripQuotes(personArray.get(CLIENT_TSHIRT_SIZE_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_HAS_SIGNED_WAIVER_IDX).toString()).equals("t") ? true : false,
						stripQuotes(personArray.get(CLIENT_HAS_MEMBERSHIP_IDX).toString()).equals("t") ? "Yes" : "No",
						stripQuotes(personArray.get(CLIENT_PASS_ON_FILE_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_STOP_EMAIL_IDX).toString()).equals("t") ? true : false,
						stripQuotes(personArray.get(CLIENT_FINANCIAL_AID_IDX).toString()).equals("t") ? true : false,
						stripQuotes(personArray.get(CLIENT_FINANCIAL_AID_PERCENT_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_GRANT_INFO_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_LEAVE_REASON_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_HEAR_ABOUT_US_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_WHO_TO_THANK_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_EMERG_CONTACT_NAME_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_EMERG_CONTACT_PHONE_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_EMERG_CONTACT_EMAIL_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_HOME_PHONE_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_ACCOUNT_MGR_NAMES_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_ACCOUNT_MGR_PHONES_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_ACCOUNT_MGR_EMAILS_IDX).toString()),
						stripQuotes(personArray.get(CLIENT_DEPENDENT_NAMES_IDX).toString()));

				studentList.add(model);
			}

			// Check to see if there are more pages
			hasMore = jsonObj.getBoolean("has_more");
			if (hasMore)
				lastKey = jsonObj.getString("last_key");

			conn.disconnect();

		} while (hasMore);

		return studentList;
	}

	public ArrayList<AttendanceEventModel> getAttendance(String startDate) {
		// Insert start date and end date into enrollment command string
		DateTime today = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles"));
		String enroll2 = getEnrollmentStudentTracker2.replaceFirst("0000-00-00", startDate);
		enroll2 = enroll2.replaceFirst("1111-11-11", today.toString("yyyy-MM-dd"));
		enroll2 = enroll2.replaceFirst("2222-22-22", today.toString("yyyy-MM-dd"));
		enroll2 = enroll2.replaceFirst("3333-33-33", today.plusDays(6).toString("yyyy-MM-dd"));

		// Get attendance for all students
		return getEnrollmentByCmdString(getEnrollmentStudentTracker, enroll2);
	}

	private ArrayList<AttendanceEventModel> getEnrollmentByCmdString(String cmdString1, String cmdString2) {
		ArrayList<AttendanceEventModel> eventList = new ArrayList<AttendanceEventModel>();
		boolean hasMore = false;
		String lastKey = "";

		do {
			// Get URL connection with authorization and send the query; add page info if necessary
			HttpURLConnection conn;
			if (hasMore)
				conn = sendQueryToUrl("enrollments", cmdString1 + ",\"starting_after\":\"" + lastKey + "\"" + cmdString2);
			else
				conn = sendQueryToUrl("enrollments", cmdString1 + cmdString2);

			if (conn == null)
				return eventList;

			// Get input stream and read data
			JsonObject jsonObj = readInputStream(conn);
			if (jsonObj == null) {
				conn.disconnect();
				return eventList;
			}
			JsonArray jsonArray = jsonObj.getJsonArray("rows");

			for (int i = 0; i < jsonArray.size(); i++) {
				// Get fields for each event
				JsonArray eventArray = (JsonArray) jsonArray.get(i);
				String eventName = stripQuotes(eventArray.get(ENROLL_EVENT_NAME_IDX).toString());
				String serviceDate = stripQuotes(eventArray.get(ENROLL_SERVICE_DATE_IDX).toString());

				// Add event to list
				if (!eventName.equals("") && !eventName.equals("\"\"") && !serviceDate.equals("")) {
					eventList.add(new AttendanceEventModel(eventArray.getInt(ENROLL_CLIENT_ID_IDX),
							eventArray.getInt(ENROLL_VISIT_ID_IDX),
							stripQuotes(eventArray.get(ENROLL_FULL_NAME_IDX).toString()), serviceDate, eventName,
							stripQuotes(eventArray.get(ENROLL_TEACHER_NAMES_IDX).toString()),
							stripQuotes(eventArray.get(ENROLL_SERVICE_CATEGORY_IDX).toString()),
							stripQuotes(eventArray.get(ENROLL_STATE_IDX).toString()), null));
				}
			}

			// Check to see if there are more pages
			hasMore = jsonObj.getBoolean("has_more");
			if (hasMore)
				lastKey = jsonObj.getString("last_key");

			conn.disconnect();

		} while (hasMore && cmdString2 != "");

		return eventList;
	}

	public ArrayList<SalesForceAttendanceModel> getSalesForceAttendance(String startDate, String endDate) {
		// Get attendance for export to SalesForce database
		ArrayList<SalesForceAttendanceModel> eventList = new ArrayList<SalesForceAttendanceModel>();
		boolean hasMore = false;
		String lastKey = "";

		// Insert start date and end date into enrollment command string
		String enroll2 = getEnrollmentSalesForce2.replaceFirst("0000-00-00", startDate);
		enroll2 = enroll2.replaceFirst("1111-11-11", endDate);
		enroll2 = enroll2.replace("2222-22-22", startDate);

		do {
			// Get URL connection and send the query; add page info if necessary
			HttpURLConnection conn;
			if (hasMore)
				conn = sendQueryToUrl("enrollments", getEnrollmentSalesForce + ",\"starting_after\":\"" + lastKey + "\"" + enroll2);
			else
				conn = sendQueryToUrl("enrollments", getEnrollmentSalesForce + enroll2);

			if (conn == null)
				return null;

			// Get input stream and read data
			JsonObject jsonObj = readInputStream(conn);
			if (jsonObj == null) {
				conn.disconnect();
				return null;
			}
			JsonArray jsonArray = jsonObj.getJsonArray("rows");

			for (int i = 0; i < jsonArray.size(); i++) {
				// Get fields for each event
				JsonArray eventArray = (JsonArray) jsonArray.get(i);

				// Add event to list
				eventList.add(new SalesForceAttendanceModel(eventArray.get(SF_PERSON_ID_IDX).toString(),
						stripQuotes(eventArray.get(SF_FULL_NAME_IDX).toString()),
						stripQuotes(eventArray.get(SF_SERVICE_DATE_IDX).toString()),
						stripQuotes(eventArray.get(SF_SERVICE_TIME_IDX).toString()),
						stripQuotes(eventArray.get(SF_EVENT_NAME_IDX).toString()),
						stripQuotes(eventArray.get(SF_SERVICE_CATEGORY_IDX).toString()),
						stripQuotes(eventArray.get(SF_SERVICE_NAME_IDX).toString()),
						stripQuotes(eventArray.get(SF_STATE_IDX).toString()),
						eventArray.get(SF_VISIT_ID_IDX).toString(),
						eventArray.get(SF_EVENT_OCCURRENCE_ID_IDX).toString(),
						stripQuotes(eventArray.get(SF_LOCATION_NAME_IDX).toString()),
						stripQuotes(eventArray.get(SF_INSTRUCTOR_NAMES_IDX).toString())));
			}

			// Check to see if there are more pages
			hasMore = jsonObj.getBoolean("has_more");
			if (hasMore)
				lastKey = jsonObj.getString("last_key");

			conn.disconnect();

		} while (hasMore);

		return eventList;
	}

	public ArrayList<AttendanceEventModel> getMissingAttendance(String endDate, ArrayList<StudentModel> studentList) {
		ArrayList<AttendanceEventModel> eventList = new ArrayList<AttendanceEventModel>();

		// Insert end date into enrollment command string
		String enroll2 = getEnrollmentStudentTracker2WithName.replaceFirst("1111-11-11", endDate);

		for (int i = 0; i < studentList.size(); i++) {
			StudentModel student = studentList.get(i);
			if (student.getStartDate() != null) {
				// Get student start date and ignore if date is beyond end date
				String catchupStartDate = student.getStartDate().toString();
				if (catchupStartDate.compareTo(endDate) >= 0)
					continue;

				// Catch up only as far back as 3 months ago
				String earliestDate = new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles")).minusMonths(3)
						.toString("yyyy-MM-dd");
				if (catchupStartDate.compareTo(earliestDate) < 0)
					catchupStartDate = earliestDate;

				// Get attendance for this student
				String enrollTemp = enroll2.replaceFirst("0000-00-00", catchupStartDate);
				enrollTemp = enrollTemp.replaceFirst("NNNNNN", student.getFirstName() + " " + student.getLastName());
				eventList.addAll(getEnrollmentByCmdString(getEnrollmentStudentTracker, enrollTemp));

				// Set student 'NewStudent' flag back to false
				mysqlDb.updateStudentFlags(student, "NewStudent", 0);
			}
		}

		return eventList;
	}

	public ArrayList<ScheduleModel> getSchedule(String startDate) {
		ArrayList<ScheduleModel> scheduleList = new ArrayList<ScheduleModel>();

		// Insert start date and end date into schedule command string.
		String scheduleString = getScheduleData.replaceFirst("0000-00-00", startDate);
		scheduleString = scheduleString.replaceFirst("1111-11-11",
				new DateTime().withZone(DateTimeZone.forID("America/Los_Angeles")).toString("yyyy-MM-dd"));

		// Get URL connection with authorization and send query
		HttpURLConnection conn = sendQueryToUrl("event_occurrences", scheduleString);
		if (conn == null)
			return scheduleList;

		// Get input stream and read data
		JsonObject jsonObj = readInputStream(conn);
		if (jsonObj == null) {
			conn.disconnect();
			return scheduleList;
		}
		JsonArray jsonArray = jsonObj.getJsonArray("rows");

		for (int i = 0; i < jsonArray.size(); i++) {
			// Get fields for each event in the schedule
			JsonArray scheduleArray = (JsonArray) jsonArray.get(i);

			// Get event name, day-of-week and duration
			String eventName = stripQuotes(scheduleArray.get(SCHED_WKLY_EVENT_NAME_IDX).toString());
			String serviceDayString = stripQuotes(scheduleArray.get(SCHED_SERVICE_DAY_IDX).toString());
			int serviceDay = Integer.parseInt(serviceDayString);
			String startTime = stripQuotes(scheduleArray.get(SCHED_SERVICE_TIME_IDX).toString());
			int duration = scheduleArray.getInt(SCHED_DURATION_MINS_IDX);

			// Add event to list
			scheduleList.add(new ScheduleModel(0, serviceDay, startTime, duration, eventName));
		}

		conn.disconnect();
		return scheduleList;
	}

	public ArrayList<CoursesModel> getCourses(String startDate, String endDate) {
		ArrayList<CoursesModel> coursesList = new ArrayList<CoursesModel>();

		// Insert start date and end date into schedule command string.
		String coursesString = getCoursesData.replaceFirst("0000-00-00", startDate);
		coursesString = coursesString.replaceFirst("1111-11-11", endDate);

		// Get URL connection with authorization and send query
		HttpURLConnection conn = sendQueryToUrl("event_occurrences", coursesString);
		if (conn == null)
			return coursesList;

		// Get input stream and read data
		JsonObject jsonObj = readInputStream(conn);
		if (jsonObj == null) {
			conn.disconnect();
			return coursesList;
		}
		JsonArray jsonArray = jsonObj.getJsonArray("rows");

		for (int i = 0; i < jsonArray.size(); i++) {
			// Get fields for each event in the schedule
			JsonArray coursesArray = (JsonArray) jsonArray.get(i);

			// Add event to list
			coursesList.add(new CoursesModel(coursesArray.getInt(COURSES_SCHEDULE_ID_IDX),
					stripQuotes(coursesArray.get(COURSES_EVENT_NAME_IDX).toString()),
					coursesArray.getInt(COURSE_ENROLLMENT_IDX)));
		}

		conn.disconnect();
		return coursesList;
	}

	public ArrayList<AttendanceEventModel> getCourseAttendance(String startDate, String endDate) {
		// Insert start date and end date into enrollment command string
		String enroll2 = getCourseEnrollmentStudentTracker2.replaceFirst("0000-00-00", startDate);
		enroll2 = enroll2.replaceFirst("1111-11-11", endDate);

		// Get attendance for all students
		return getEnrollmentByCmdString(getEnrollmentStudentTracker, enroll2);
	}

	public boolean getCanceledFlagsByProductId(Integer clientID, Integer productID) {
		// Get URL connection and send the query
		String planCmd = getPersonPlanDataByProductId.replace("IIII", clientID.toString());
		planCmd = planCmd.replace("PPPP", productID.toString());
		HttpURLConnection conn = sendQueryToUrl("person_plans", planCmd);

		if (conn == null)
			return true;

		// Get input stream and read data
		JsonObject jsonObj = readInputStream(conn);
		if (jsonObj == null) {
			conn.disconnect();
			return true;
		}
		JsonArray jsonArray = jsonObj.getJsonArray("rows");

		for (int i = 0; i < jsonArray.size(); i++) {
			// Get fields for each plan in the list
			JsonArray planArray = (JsonArray) jsonArray.get(i);

			if (planArray.getString(PLAN2_IS_CANCELED_IDX).equals("f")) {
				conn.disconnect();
				return false;
			}
		}
		conn.disconnect();
		return true;
	}

	public ArrayList<StaffMemberModel> getSalesForceStaffMembers() {
		ArrayList<StaffMemberModel> staffList = new ArrayList<StaffMemberModel>();

		// Get URL connection and send the query
		HttpURLConnection conn = sendQueryToUrl("staff_members", getStaffMemberData);
		if (conn == null)
			return null;

		// Get input stream and read data
		JsonObject jsonObj = readInputStream(conn);
		if (jsonObj == null) {
			conn.disconnect();
			return null;
		}
		JsonArray jsonArray = jsonObj.getJsonArray("rows");

		for (int i = 0; i < jsonArray.size(); i++) {
			// Get fields for each staff member
			JsonArray staffArray = (JsonArray) jsonArray.get(i);

			// Get fields for this Json array entry
			String sfClientID = null;
			if (staffArray.get(TEACHER_SF_CLIENT_ID_IDX) != null)
				sfClientID = stripQuotes(staffArray.get(TEACHER_SF_CLIENT_ID_IDX).toString());

			staffList.add(new StaffMemberModel(staffArray.get(TEACHER_CLIENT_ID_IDX).toString(), sfClientID,
					staffArray.getString(TEACHER_FIRST_NAME_IDX), staffArray.getString(TEACHER_LAST_NAME_IDX),
					stripQuotes(staffArray.get(TEACHER_CATEGORY_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_ROLE_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_OCCUPATION_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_EMPLOYER_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_START_INFO_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_GENDER_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_BIRTHDATE_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_PHONE_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_HOME_PHONE_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_ADDRESS_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_EMAIL_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_ALTERNATE_EMAIL_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_HOME_LOCATION_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_GITHUB_USER_IDX).toString()),
					staffArray.getInt(TEACHER_PAST_EVENTS_IDX),
					staffArray.getInt(TEACHER_FUTURE_EVENTS_IDX),
					stripQuotes(staffArray.get(TEACHER_KEY_HOLDER_IDX).toString()).equals("t") ? true : false,
					stripQuotes(staffArray.get(TEACHER_LIVE_SCAN_DATE_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_T_SHIRT_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_WHERE_DID_YOU_HEAR_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_LEAVE_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_EMERG_NAME_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_EMERG_EMAIL_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_EMERG_PHONE_IDX).toString()),
					stripQuotes(staffArray.get(TEACHER_CURR_BOARD_MEMBER_IDX).toString()).equalsIgnoreCase("t") ? true : false,
					stripQuotes(staffArray.get(TEACHER_CURR_STAFF_MEMBER_IDX).toString()).equalsIgnoreCase("t") ? true : false,
					stripQuotes(staffArray.get(TEACHER_IS_ALSO_CLIENT_IDX).toString()).equalsIgnoreCase("t") ? true : false,
					stripQuotes(staffArray.get(TEACHER_TITLE_IDX).toString())));
		}

		conn.disconnect();
		return staffList;
	}

	public ArrayList<SalesForceStaffHoursModel> getSalesForceStaffHours(String startDate, String endDate) {
		// Get staff hours for export to SalesForce database
		ArrayList<SalesForceStaffHoursModel> eventList = new ArrayList<SalesForceStaffHoursModel>();
		boolean hasMore = false;
		String lastKey = "";

		// Insert start date and end date into staff hours command string
		String staffHours2 = getStaffHoursSalesForce2.replaceFirst("0000-00-00", startDate);
		staffHours2 = staffHours2.replaceFirst("1111-11-11", endDate);

		do {
			// Get URL connection and send the query; add page info if necessary
			HttpURLConnection conn;
			if (hasMore)
				conn = sendQueryToUrl("event_occurrence_staff_members",
						getStaffHoursSalesForce + ",\"starting_after\":\"" + lastKey + "\"" + staffHours2);
			else
				conn = sendQueryToUrl("event_occurrence_staff_members", getStaffHoursSalesForce + staffHours2);

			if (conn == null)
				return null;

			// Get input stream and read data
			JsonObject jsonObj = readInputStream(conn);
			if (jsonObj == null) {
				conn.disconnect();
				return null;
			}
			JsonArray jsonArray = jsonObj.getJsonArray("rows");

			for (int i = 0; i < jsonArray.size(); i++) {
				// Get fields for each event
				JsonArray eventArray = (JsonArray) jsonArray.get(i);

				// Add event to list
				eventList.add(new SalesForceStaffHoursModel(eventArray.get(STAFF_CLIENT_ID_IDX).toString(),
						eventArray.getString(STAFF_EVENT_FULL_NAME_IDX),
						eventArray.getString(STAFF_EVENT_SERVICE_NAME_IDX),
						eventArray.getString(STAFF_EVENT_SERVICE_DATE_IDX),
						eventArray.getString(STAFF_EVENT_SERVICE_TIME_IDX),
						eventArray.getJsonNumber(STAFF_EVENT_DURATION_IDX).doubleValue(),
						eventArray.getString(STAFF_EVENT_LOCATION_IDX),
						eventArray.getJsonNumber(STAFF_EVENT_COMPLETED_COUNT_IDX).doubleValue(),
						eventArray.getJsonNumber(STAFF_EVENT_NO_SHOW_COUNT_IDX).doubleValue(),
						eventArray.getJsonNumber(STAFF_EVENT_CANCELED_COUNT_IDX).doubleValue(),
						stripQuotes(eventArray.get(STAFF_EVENT_NAME_IDX).toString()),
						eventArray.get(STAFF_EVENT_SCHEDULE_ID_IDX).toString(),
						stripQuotes(eventArray.get(STAFF_EVENT_SERVICE_CATEGORY_IDX).toString())));
			}

			// Check to see if there are more pages
			hasMore = jsonObj.getBoolean("has_more");
			if (hasMore)
				lastKey = jsonObj.getString("last_key");

			conn.disconnect();

		} while (hasMore);

		return eventList;
	}

	private HttpURLConnection connectUrl(String endPoint) {
		try {
			// Get URL connection with authorization
			URL url = new URL("https://jtl.pike13.com/desk/api/v3/reports/" + endPoint + "/queries");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			String basicAuth = "Bearer " + pike13Token;
			conn.setRequestProperty("Authorization", basicAuth);
			conn.setRequestProperty("User-Agent", USER_AGENT);

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-type", "application/vnd.api+json; charset=UTF-8");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			return conn;

		} catch (IOException e) {
			MySqlDbLogging.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, new StudentNameModel("", "", false), 0,
					": " + e.getMessage());
		}
		return null;
	}

	private HttpURLConnection sendQueryToUrl(String connName, String getCommand) {
		try {
			// If necessary, try twice to send query
			for (int i = 0; i < 2; i++) {
				// Get URL connection with authorization
				HttpURLConnection conn = connectUrl(connName);

				// Send the query
				OutputStream outputStream = conn.getOutputStream();
				outputStream.write(getCommand.getBytes("UTF-8"));
				outputStream.flush();
				outputStream.close();

				// Check result
				int responseCode = conn.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK)
					return conn;
				else {
					conn.disconnect();
					MySqlDbLogging.insertLogData(LogDataModel.PIKE13_CONNECTION_ERROR, new StudentNameModel("", "", false), 0,
							" " + responseCode + " (attempt #" + (i + 1) + "): " + conn.getResponseMessage());
				}
			}

		} catch (IOException e) {
			MySqlDbLogging.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, new StudentNameModel("", "", false), 0, 
					": " + e.getMessage());
		}

		return null;
	}

	private JsonObject readInputStream(HttpURLConnection conn) {
		try {
			// Get input stream and read data
			InputStream inputStream = conn.getInputStream();
			JsonReader repoReader = Json.createReader(inputStream);
			JsonObject object = ((JsonObject) repoReader.read()).getJsonObject("data").getJsonObject("attributes");

			repoReader.close();
			inputStream.close();
			return object;

		} catch (IOException e) {
			MySqlDbLogging.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, new StudentNameModel("", "", false), 0,
					": " + e.getMessage());
		}
		return null;
	}

	private String stripQuotes(String fieldData) {
		// Strip off quotes around field string
		if (fieldData.equals("\"\"") || fieldData.startsWith("null"))
			return "";
		else
			return fieldData.substring(1, fieldData.length() - 1);
	}
}
