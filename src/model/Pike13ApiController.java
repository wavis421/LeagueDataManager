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

	// Custom field names
	private final String GENDER_NAME = "custom_field_106320";
	private final String GITHUB_NAME = "custom_field_127885";
	private final String GRAD_YEAR_NAME = "custom_field_145902";

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

	public ArrayList<StudentImportModel> getClients() {
		ArrayList<StudentImportModel> studentList = new ArrayList<StudentImportModel>();

		try {
			String query = "https://jtl.pike13.com/desk/api/v3/reports/clients/queries";

			// Get URL connection with authorization
			URL url = new URL(query);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			String basicAuth = "Bearer " + SECRET_TOKEN;
			conn.setRequestProperty("Authorization", basicAuth);
			conn.setRequestProperty("User-Agent", USER_AGENT);

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-type", "application/vnd.api+json; charset=UTF-8");
			conn.setDoOutput(true);
			conn.setDoInput(true);

			// Send the query
			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(getClientData.getBytes("UTF-8"));
			outputStream.flush();
			outputStream.close();

			// Check result
			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				System.out.println("HTTP Connection error " + responseCode + ": " + conn.getResponseMessage());
				return null;
			}

			// Get input stream and read data
			InputStream inputStream = conn.getInputStream();
			JsonReader repoReader = Json.createReader(inputStream);
			JsonObject jsonObj = (JsonObject) repoReader.read();
			JsonArray jsonArray = jsonObj.getJsonObject("data").getJsonObject("attributes").getJsonArray("rows");

			for (int i = 0; i < jsonArray.size(); i++) {
				// Get fields for each person
				JsonArray personArray = (JsonArray) jsonArray.get(i);
				String firstName = stripField(personArray.get(FIRST_NAME_IDX).toString());

				if (!firstName.startsWith("Guest")) {
					// Get fields for this Json array entry
					StudentImportModel model = new StudentImportModel(personArray.getInt(CLIENT_ID_IDX),
							stripField(personArray.get(LAST_NAME_IDX).toString()),
							stripField(personArray.get(FIRST_NAME_IDX).toString()),
							stripField(personArray.get(GITHUB_IDX).toString()),
							stripField(personArray.get(GENDER_IDX).toString()),
							stripField(personArray.get(FIRST_VISIT_IDX).toString()),
							stripField(personArray.get(HOME_LOC_IDX).toString()),
							stripField(personArray.get(GRAD_YEAR_IDX).toString()));

					studentList.add(model);
				}
			}

			repoReader.close();
			inputStream.close();
			conn.disconnect();

		} catch (IOException e1) {
			System.out.println("IO Exception getting Pike13 data: " + e1.getMessage());
		}

		return studentList;
	}

	private String stripField(String fieldData) {
		// Strip off quotes around field string
		if (fieldData.equals("\"\"") || fieldData.startsWith("null"))
			return "";
		else
			return fieldData.substring(1, fieldData.length() - 1);
	}
}
