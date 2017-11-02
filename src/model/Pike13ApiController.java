package model;

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

public class Pike13ApiController {
	private final String SECRET_TOKEN = "KxjMhMHLUrhrZ6IeJ4OsSsuFAJyM0hGfFi4NdI4D";
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

	// TODO: Currently getting up to 500 fields; get multi pages if necessary
	private final String getClientData = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit and filters
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"person_id\",\"first_name\",\"last_name\",\"" + GITHUB_NAME + "\",\"" + GRAD_YEAR_NAME
			+ "\",\"" + GENDER_NAME + "\",\"home_location_name\",\"first_visit_date\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500},"
			// Filter on Dependents NULL and has membership
			+ "\"filter\":[\"and\",[[\"emp\",\"dependent_names\",\"\"],[\"eq\",\"has_membership\",\"t\"]]]}}}";

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
			+ "           [\"or\",[[\"starts\",\"service_category\",\"Classes\"],[\"starts\",\"service_category\",\"Open Labs\"]]]]]"
			+ "}}}";
	
	ArrayList<LogDataModel> logData;
	
	public Pike13ApiController(ArrayList<LogDataModel> logData) {
		this.logData = logData;
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
				logData.add(new LogDataModel(LogDataModel.PIKE13_CONNECTION_ERROR, null, 0,
						" " + responseCode + ": " + conn.getResponseMessage()));
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
			logData.add(new LogDataModel(LogDataModel.PIKE13_IMPORT_ERROR, null, 0,
					" (IO Exception): " + e1.getMessage()));
		}

		return studentList;
	}

	public ArrayList<ActivityEventModel> getEnrollment(String startDate) {
		ArrayList<ActivityEventModel> eventList = new ArrayList<ActivityEventModel>();
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
					logData.add(new LogDataModel(LogDataModel.PIKE13_CONNECTION_ERROR, null, 0,
							" " + responseCode + ": " + conn.getResponseMessage()));
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
						eventList.add(new ActivityEventModel(eventArray.getInt(CLIENT_ID_IDX),
								stripQuotes(eventArray.get(FULL_NAME_IDX).toString()), serviceDate, eventName));
					}
				}

				// Check to see if there are more pages
				hasMore = jsonObj.getBoolean("has_more");
				lastKey = jsonObj.getString("last_key");

				conn.disconnect();

			} while (hasMore);

		} catch (IOException e1) {
			logData.add(new LogDataModel(LogDataModel.PIKE13_IMPORT_ERROR, null, 0,
					" (IO Exception): " + e1.getMessage()));
		}

		return eventList;
	}

	private HttpURLConnection connectUrl(String queryUrl) {
		try {
			// Get URL connection with authorization
			URL url = new URL(queryUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			String basicAuth = "Bearer " + SECRET_TOKEN;
			conn.setRequestProperty("Authorization", basicAuth);
			conn.setRequestProperty("User-Agent", USER_AGENT);

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-type", "application/vnd.api+json; charset=UTF-8");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			return conn;

		} catch (IOException e) {
			logData.add(new LogDataModel(LogDataModel.PIKE13_IMPORT_ERROR, null, 0,
					" (IO Exception): " + e.getMessage()));
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
			logData.add(new LogDataModel(LogDataModel.PIKE13_IMPORT_ERROR, null, 0,
					" (IO Exception): " + e.getMessage()));
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
			logData.add(new LogDataModel(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, e.getMessage()));
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
