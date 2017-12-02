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

import model.AttendanceEventModel;
import model.LogDataModel;
import model.MySqlDatabase;
import model.ScheduleModel;
import model.StudentImportModel;

public class Pike13Api {
	private final String USER_AGENT = "Mozilla/5.0";

	// Indices for client data
	private final int CLIENT_ID_IDX = 0;
	private final int FIRST_NAME_IDX = 1;
	private final int LAST_NAME_IDX = 2;
	private final int GITHUB_IDX = 3;
	private final int GRAD_YEAR_IDX = 4;
	private final int GENDER_IDX = 5;
	private final int HOME_LOC_IDX = 6;
	private final int FIRST_VISIT_IDX = 7;

	// Custom field names for client data
	private final String GENDER_NAME = "custom_field_106320";
	private final String GITHUB_NAME = "custom_field_127885";
	private final String GRAD_YEAR_NAME = "custom_field_145902";

	// Indices for enrollment data
	private final int FULL_NAME_IDX = 1;
	private final int SERVICE_DATE_IDX = 2;
	private final int EVENT_NAME_IDX = 3;

	// Indices for schedule data
	private final int SERVICE_DAY_IDX = 0;
	private final int SERVICE_TIME_IDX = 1;
	private final int DURATION_MINS_IDX = 2;
	private final int WKLY_EVENT_NAME_IDX = 3;

	// TODO: Currently getting up to 500 fields; get multi pages if necessary
	private final String getClientData = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit and filters
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"person_id\",\"first_name\",\"last_name\",\"" + GITHUB_NAME + "\",\"" + GRAD_YEAR_NAME + "\"," 
			+ "            \"" + GENDER_NAME + "\",\"home_location_name\",\"first_visit_date\",\"future_visits\"," 
			+ "            \"completed_visits\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500},"
			// Filter on Dependents NULL and future/completed visits both > 0
			+ "\"filter\":[\"and\",[[\"emp\",\"dependent_names\"],"
			+ "                     [\"gt\",\"future_visits\",0],"
			+ "                     [\"gt\",\"completed_visits\",0]]]}}}";

	// Getting enrollment data is in 2 parts since page info gets inserted in middle
	private final String getEnrollmentData1 = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"person_id\",\"full_name\",\"service_date\",\"event_name\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500";

	private final String getEnrollmentData2 = "},"
			// Filter on State completed and since date
			+ "\"filter\":[\"and\",[[\"eq\",\"state\",\"completed\"],"
			+ "           [\"btw\",\"service_date\",[\"0000-00-00\",\"1111-11-11\"]],"
			+ "           [\"starts\",\"service_category\",\"Class\"]]]}}}";

	// Get schedule data
	private final String getScheduleData = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit and filters
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"service_day\",\"service_time\",\"duration_in_minutes\",\"event_name\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500},"
			// Filter on 'this week' and 'starts with Class' and event name not null
			+ "\"filter\":[\"and\",[[\"btw\",\"service_date\",[\"0000-00-00\",\"1111-11-11\"]],[\"starts\",\"service_category\",\"Class\"],"
			+ "           [\"nemp\",\"event_name\"]]]}}}";

	private MySqlDatabase mysqlDb;
	private String pike13Token;

	public Pike13Api(MySqlDatabase mysqlDb, String pike13Token) {
		this.mysqlDb = mysqlDb;
		this.pike13Token = pike13Token;
	}

	public ArrayList<StudentImportModel> getClients() {
		ArrayList<StudentImportModel> studentList = new ArrayList<StudentImportModel>();

		try {
			// Get URL connection with authorization
			HttpURLConnection conn = connectUrl("https://jtl.pike13.com/desk/api/v3/reports/clients/queries");

			// Send the query
			sendQueryToUrl(conn, getClientData);

			// Check result
			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				mysqlDb.insertLogData(LogDataModel.PIKE13_CONNECTION_ERROR, null, 0,
						" " + responseCode + ": " + conn.getResponseMessage());
				return studentList;
			}

			// Get input stream and read data
			JsonObject jsonObj = readInputStream(conn);
			if (jsonObj == null)
				return studentList;
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
							stripQuotes(personArray.get(GRAD_YEAR_IDX).toString()));

					studentList.add(model);
				}
			}

			conn.disconnect();

		} catch (IOException e1) {
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, " for Client DB: " + e1.getMessage());
		}

		return studentList;
	}

	public ArrayList<AttendanceEventModel> getEnrollment(String startDate) {
		ArrayList<AttendanceEventModel> eventList = new ArrayList<AttendanceEventModel>();
		boolean hasMore = false;
		String lastKey = "";

		// Insert start date and end date into enrollment command string
		String enroll2 = getEnrollmentData2.replaceFirst("0000-00-00", startDate);
		enroll2 = enroll2.replaceFirst("1111-11-11", new DateTime().toString("yyyy-MM-dd"));

		try {
			do {
				// Get URL connection with authorization
				HttpURLConnection conn = connectUrl("https://jtl.pike13.com/desk/api/v3/reports/enrollments/queries");

				// Send the query; add page info if necessary
				if (hasMore)
					sendQueryToUrl(conn, getEnrollmentData1 + ",\"starting_after\":\"" + lastKey + "\"" + enroll2);
				else
					sendQueryToUrl(conn, getEnrollmentData1 + enroll2);

				// Check result
				int responseCode = conn.getResponseCode();
				if (responseCode != HttpURLConnection.HTTP_OK) {
					mysqlDb.insertLogData(LogDataModel.PIKE13_CONNECTION_ERROR, null, 0,
							" " + responseCode + ": " + conn.getResponseMessage());
					return eventList;
				}

				// Get input stream and read data
				JsonObject jsonObj = readInputStream(conn);
				if (jsonObj == null)
					return eventList;
				JsonArray jsonArray = jsonObj.getJsonArray("rows");

				for (int i = 0; i < jsonArray.size(); i++) {
					// Get fields for each event
					JsonArray eventArray = (JsonArray) jsonArray.get(i);
					String eventName = stripQuotes(eventArray.get(EVENT_NAME_IDX).toString());
					String serviceDate = stripQuotes(eventArray.get(SERVICE_DATE_IDX).toString());

					// Add event to list
					if (!eventName.equals("") && !eventName.equals("\"\"") && !serviceDate.equals("")) {
						eventList.add(new AttendanceEventModel(eventArray.getInt(CLIENT_ID_IDX),
								stripQuotes(eventArray.get(FULL_NAME_IDX).toString()), serviceDate, eventName));
					}
				}

				// Check to see if there are more pages
				hasMore = jsonObj.getBoolean("has_more");
				if (hasMore)
					lastKey = jsonObj.getString("last_key");

				conn.disconnect();

			} while (hasMore);

		} catch (IOException e1) {
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, " for Enrollment DB: " + e1.getMessage());
		}

		return eventList;
	}

	public ArrayList<ScheduleModel> getSchedule() {
		ArrayList<ScheduleModel> scheduleList = new ArrayList<ScheduleModel>();

		// Insert start date and end date into schedule command string.
		// Get last 2 weeks of data since if there's a holiday there won't be any data!
		DateTime today = new DateTime();
		String scheduleString = getScheduleData.replaceFirst("0000-00-00", today.minusDays(14).toString("yyyy-MM-dd"));
		scheduleString = scheduleString.replaceFirst("1111-11-11", today.toString("yyyy-MM-dd"));

		try {
			// Get URL connection with authorization
			HttpURLConnection conn = connectUrl("https://jtl.pike13.com/desk/api/v3/reports/event_occurrences/queries");

			// Send the query
			sendQueryToUrl(conn, scheduleString);

			// Check result
			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				mysqlDb.insertLogData(LogDataModel.PIKE13_CONNECTION_ERROR, null, 0,
						" " + responseCode + ": " + conn.getResponseMessage());
				return scheduleList;
			}

			// Get input stream and read data
			JsonObject jsonObj = readInputStream(conn);
			if (jsonObj == null)
				return scheduleList;
			JsonArray jsonArray = jsonObj.getJsonArray("rows");

			for (int i = 0; i < jsonArray.size(); i++) {
				// Get fields for each event in the schedule
				JsonArray scheduleArray = (JsonArray) jsonArray.get(i);

				// Get event name, day-of-week and duration
				String eventName = stripQuotes(scheduleArray.get(WKLY_EVENT_NAME_IDX).toString());
				String serviceDayString = stripQuotes(scheduleArray.get(SERVICE_DAY_IDX).toString());
				int serviceDay = Integer.parseInt(serviceDayString);
				String startTime = stripQuotes(scheduleArray.get(SERVICE_TIME_IDX).toString());
				int duration = scheduleArray.getInt(DURATION_MINS_IDX);

				// Add event to list
				scheduleList.add(new ScheduleModel(0, serviceDay, startTime, duration, eventName));
			}

			conn.disconnect();

		} catch (IOException e1) {
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, " for Schedule DB: " + e1.getMessage());
		}

		return scheduleList;
	}

	private HttpURLConnection connectUrl(String queryUrl) {
		try {
			// Get URL connection with authorization
			URL url = new URL(queryUrl);
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
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, ": " + e.getMessage());
		}
		return null;
	}

	private void sendQueryToUrl(HttpURLConnection conn, String getCommand) {
		try {
			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(getCommand.getBytes("UTF-8"));
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, ": " + e.getMessage());
		}
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
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, ": " + e.getMessage());
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
