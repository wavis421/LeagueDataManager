package model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.stream.JsonParsingException;

import org.joda.time.DateTime;

public class GitApiController {
	// Repo name format: level-X-module-Y-username
	private static final String URL_MODULE_PATTERN_MATCH = "module-";
	private static final int modulePatternLength = URL_MODULE_PATTERN_MATCH.length();

	// TODO: Get this from a file or website
	private static final String token = "223bcb4816e95309f88c1154377f721e3d77568a";
	private MySqlDatabase sqlDb;

	public GitApiController(MySqlDatabase sqlDb) {
		this.sqlDb = sqlDb;
	}

	public void importGithubComments(String startDate) {
		// Get all activities w/ github user name and no comments
		ArrayList<ActivityEventModel> eventList = sqlDb.getEventsWithNoComments(startDate);
		String lastGithubUser = "";
		JsonArray repoJsonArray = null;

		for (int i = 0; i < eventList.size(); i++) {
			// Get commit info from DB for each student/date combo
			ActivityEventModel event = eventList.get(i);
			String gitUser = event.getGithubName();

			if (!gitUser.equals(lastGithubUser)) {
				// New github user, get new repo array
				lastGithubUser = gitUser;
				repoJsonArray = getReposForGithubUser(event);
				if (repoJsonArray == null)
					continue;

			} else if (repoJsonArray == null) {
				// This git account does not exist!!
				continue;
			}

			DateTime startDay = new DateTime(event.getServiceDate().toString());
			DateTime endDay = startDay.plusDays(1);

			for (int j = 0; j < repoJsonArray.size(); j++) {
				// Get commits data for each repo/date match
				String repoName = ((JsonObject) repoJsonArray.get(j)).getString("name").trim();
				String url = "https://api.github.com/repos/" + event.getGithubName() + "/" + repoName
						+ "/commits?since=" + startDay.toString("YYYY-MM-dd") + "&until="
						+ endDay.toString("YYYY-MM-dd");
				InputStream commitStream = executeCurlCommand(url);

				if (commitStream == null) {
					sqlDb.getDbLogData()
							.add(new LogDataModel(LogDataModel.GITHUB_IMPORT_FAILURE, event.getStudentNameModel(),
									event.getClientID(), " for user '" + event.getGithubName() + "'"));
					continue;
				}
				// TODO: If more than 1 commit for this date, append comments
				processGithubCommitsStream(event, commitStream, repoName);
			}
		}
	}

	public void importGithubCommentsByLevel(int level, String startDate) {
		// Get all activities w/ github user name and no comments
		ArrayList<ActivityEventModel> eventList = sqlDb.getEventsWithNoComments(startDate);

		JsonArray repoJsonArray = getReposByLevel(level);
		if (repoJsonArray == null) {
			sqlDb.getDbLogData().add(new LogDataModel(LogDataModel.GITHUB_IMPORT_FAILURE,
					new StudentNameModel("", "", false), 0, " for Level " + level));
			return;
		}

		for (int i = 0; i < repoJsonArray.size(); i++) {
			// Parse repo to get user name; allow for multiple digit module #
			String repoName = ((JsonObject) repoJsonArray.get(i)).getString("name");
			int idx = repoName.indexOf(URL_MODULE_PATTERN_MATCH) + modulePatternLength;
			idx += (repoName.substring(idx)).indexOf('-') + 1;
			String userName = repoName.substring(idx);

			// Search for user in eventList
			for (int j = 0; j < eventList.size(); j++) {
				ActivityEventModel event = eventList.get(j);
				if (userName.equals(event.getGithubName())) {
					DateTime startDay = new DateTime(event.getServiceDate().toString());
					DateTime endDay = startDay.plusDays(1);

					// Get commits data for repo/date match
					String url = "https://api.github.com/repos/League-Level" + level + "-Student/" + repoName
							+ "/commits?since=" + startDay.toString("YYYY-MM-dd") + "&until="
							+ endDay.toString("YYYY-MM-dd");
					InputStream commitStream = executeCurlCommand(url);

					if (commitStream == null) {
						sqlDb.getDbLogData().add(new LogDataModel(LogDataModel.GITHUB_IMPORT_FAILURE,
								event.getStudentNameModel(), event.getClientID(), " for user '" + userName + "'"));
						continue;
					}
					// TODO: If more than 1 commit for this date, append comments
					processGithubCommitsStream(event, commitStream, repoName);
				}
			}
		}
	}

	private InputStream executeCurlCommand(String url) {
		String[] command = { "C:\\Program Files\\curl\\curl.exe", "-u", "wavis421:" + token, url };

		ProcessBuilder process = new ProcessBuilder(command);
		Process p;
		InputStream inputStream = null;

		try {
			p = process.start();
			inputStream = p.getInputStream();

		} catch (IOException e) {
			System.out.println("Error executing Curl command: " + e.getMessage());
		}
		return inputStream;
	}

	private JsonArray getReposForGithubUser(ActivityEventModel event) {
		// Get all repos for this github user
		String gitUser = event.getGithubName();
		String url = "https://api.github.com/users/" + gitUser + "/repos";
		InputStream inputStream = executeCurlCommand(url);
		JsonArray jsonArray = null;

		if (inputStream == null) {
			sqlDb.getDbLogData().add(new LogDataModel(LogDataModel.GITHUB_IMPORT_FAILURE, event.getStudentNameModel(),
					event.getClientID(), " for Github user '" + gitUser + "'"));
			return null;
		}

		try {
			// Get all repos for this user
			JsonReader repoReader = Json.createReader(inputStream);
			JsonStructure repoStruct = repoReader.read();

			if (repoStruct instanceof JsonObject) {
				// Expecting an array of data, so this is an error!
				sqlDb.getDbLogData().add(new LogDataModel(LogDataModel.GITHUB_IMPORT_FAILURE,
						event.getStudentNameModel(), event.getClientID(),
						" for Github user '" + gitUser + "': " + ((JsonObject) repoStruct).getString("message")));

			} else {
				jsonArray = (JsonArray) repoStruct;
			}

			repoReader.close();
			inputStream.close();

		} catch (JsonParsingException e1) {
			sqlDb.getDbLogData().add(new LogDataModel(LogDataModel.GITHUB_PARSING_ERROR, event.getStudentNameModel(),
					event.getClientID(), " for Github user '" + event.getGithubName() + "': " + e1.getMessage()));

		} catch (IOException e2) {
			sqlDb.getDbLogData()
					.add(new LogDataModel(LogDataModel.GITHUB_IMPORT_FAILURE, event.getStudentNameModel(),
							event.getClientID(),
							" (IO Excpetion) for Github user '" + event.getGithubName() + "': " + e2.getMessage()));
		}
		return jsonArray;
	}

	private JsonArray getReposByLevel(int level) {
		// Get all repos for this level
		String url = "https://api.github.com/users/League-Level" + level + "-Student/repos";
		InputStream inputStream = executeCurlCommand(url);
		JsonArray jsonArray = null;

		if (inputStream == null) {
			sqlDb.getDbLogData().add(new LogDataModel(LogDataModel.GITHUB_IMPORT_FAILURE,
					new StudentNameModel("", "", false), 0, " for Level " + level + ": Input Stream null"));
			return null;
		}

		try {
			// Get all repos for this level
			JsonReader repoReader = Json.createReader(inputStream);
			JsonStructure repoStruct = repoReader.read();

			if (repoStruct instanceof JsonObject) {
				// Expecting an array of data, so this is an error!
				sqlDb.getDbLogData()
						.add(new LogDataModel(LogDataModel.GITHUB_IMPORT_FAILURE, new StudentNameModel("", "", false),
								0, " for Level " + level + ": " + ((JsonObject) repoStruct).getString("message")));
			} else {
				jsonArray = (JsonArray) repoStruct;
			}

			repoReader.close();
			inputStream.close();

		} catch (JsonParsingException e1) {
			System.out.println("Github parsing error for Level " + level + ": " + e1.getMessage());

		} catch (IOException e2) {
			System.out.println("IO Exception while parsing Level " + level + ": " + e2.getMessage());
		}
		return jsonArray;
	}

	private void processGithubCommitsStream(ActivityEventModel event, InputStream inputStream, String repoName) {
		try {
			JsonReader commitReader = Json.createReader(inputStream);
			JsonStructure jsonStruct = commitReader.read();

			if (jsonStruct instanceof JsonObject) {
				commitReader.close();
				return;
			}

			// Get commit items from JSON input stream
			JsonArray commitJsonArray = (JsonArray) jsonStruct;

			if (commitJsonArray == null || commitJsonArray.size() == 0) {
				// No JSON data (repository is empty)
				commitReader.close();
				return;
			}

			for (int i = 0; i < commitJsonArray.size(); i++) {
				// Process each commit
				String message = ((JsonObject) commitJsonArray.get(i)).getJsonObject("commit").getString("message");

				// Trim message to get only summary data
				int idx = message.indexOf("\n");
				if (idx > -1)
					message = message.trim().substring(0, idx);

				// Update comments & repo name
				sqlDb.updateActivity(event.getClientID(), event.getStudentNameModel(),
						event.getServiceDate().toString(), repoName, message);
			}
			commitReader.close();

		} catch (JsonException e) {
			System.out.println("Failure parsing Github input stream: " + e.getMessage());
		}
	}
}
