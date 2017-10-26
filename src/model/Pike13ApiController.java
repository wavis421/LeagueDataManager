package model;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;

public class Pike13ApiController {
	private final String SECRET_TOKEN = "KxjMhMHLUrhrZ6IeJ4OsSsuFAJyM0hGfFi4NdI4D";
	private final String USER_AGENT = "Mozilla/5.0";

	// Indices for person data
	private final int GENDER_IDX = 3;
	private final int GITHUB_IDX = 10;
	private final int GRAD_YEAR_IDX = 12;

	public String[] getStudents() {
		int count = 0;
		try {
			String url = "https://jtl.pike13.com/api/v2/desk/people?is_member=t&page=1&per_page=100";
			do {
				// Get URL connection with authorization
				// TODO: Is it necessary to create a new connection for each URL?
				URL obj = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
				String basicAuth = "Bearer " + SECRET_TOKEN;
				conn.setRequestProperty("Authorization", basicAuth);
				conn.setRequestProperty("User-Agent", USER_AGENT);
				conn.setRequestMethod("GET");

				// Get input stream and create reader for "people"
				InputStream inputStream = conn.getInputStream();
				JsonReader repoReader = Json.createReader(inputStream);
				JsonStructure jsonStruct = repoReader.read();
				JsonArray jsonArray = ((JsonObject) jsonStruct).getJsonArray("people");

				for (int i = 0; i < jsonArray.size(); i++) {
					// Get fields for each person
					JsonObject object = (JsonObject) jsonArray.get(i);
					JsonString firstName = (JsonString) object.get("first_name");
					JsonString lastName = (JsonString) object.get("last_name");
					boolean isMember = (boolean) object.getBoolean("is_member");
					JsonArray customFields = (JsonArray) object.get("custom_fields");

					// TODO: add these filter to URL (not sure how to do this!)
					if (isMember && object.get("guardian_name") != JsonValue.NULL
							&& !object.get("membership").toString().contains("On Hold")
							&& !firstName.toString().startsWith("\"Guest")) {
						// Get fields for this Json array entry
						JsonString github = null;
						JsonString gradYear = null;
						JsonString gender = null;

						if (GITHUB_IDX < customFields.size())
							github = (JsonString) ((JsonObject) customFields.get(GITHUB_IDX)).get("value");

						if (GRAD_YEAR_IDX < customFields.size())
							gradYear = (JsonString) ((JsonObject) customFields.get(GRAD_YEAR_IDX)).get("value");

						if (GENDER_IDX < customFields.size())
							gender = (JsonString) ((JsonObject) customFields.get(GENDER_IDX)).get("value");

						count++;
						System.out.println(firstName + " " + lastName + "(" + object.getJsonNumber("id") + "): "
								+ github + ", " + gradYear + ", " + gender);
					}
				}

				// Check if there are more pages to process
				JsonString jsonUrl = (JsonString) ((JsonObject) jsonStruct).get("next");
				if (jsonUrl == null)
					break;

				// Strip off the quotes
				url = jsonUrl.toString();
				url = url.substring(1, url.length() - 1);

				repoReader.close();
				inputStream.close();

			} while (url != null);

		} catch (IOException e1) {
			System.out.println("IO Exception getting Pike13 data: " + e1.getMessage());
		}

		System.out.println("Student count: " + count);
		return null;
	}
}
