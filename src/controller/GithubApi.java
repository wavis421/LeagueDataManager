package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.joda.time.DateTime;

import model.AttendanceEventModel;
import model.LogDataModel;
import model.MySqlDatabase;
import model.StudentModel;
import model.StudentNameModel;

public class GithubApi {
	private static final String DATABASE_START_DATE = "2017-09-01";

	private MySqlDatabase sqlDb;
	private RepositoryService repoService;
	private CommitService commitService;

	public GithubApi(MySqlDatabase sqlDb, String githubToken) {
		this.sqlDb = sqlDb;

		// OAuth2 token authentication
		GitHubClient client = new GitHubClient();
		client.setOAuth2Token(githubToken);

		// Get Repo and Commit services
		repoService = new RepositoryService(client);
		commitService = new CommitService(client);
	}

	public boolean importGithubComments(String startDate, int clientID) {
		// Get all attendance since 'startDate' w/ github user name (use null for ALL
		// users) and no comments.
		ArrayList<AttendanceEventModel> eventList = sqlDb.getEventsWithNoComments(startDate, clientID);
		String lastGithubUser = "";
		List<Repository> repoList = null;

		for (int i = 0; i < eventList.size(); i++) {
			// Get commit info from DB for each student/date combo
			AttendanceEventModel event = eventList.get(i);
			String gitUser = event.getGithubName();

			try {
				if (!gitUser.equals(lastGithubUser)) {
					// New github user, need to get new repo array
					lastGithubUser = gitUser;
					repoList = repoService.getRepositories(gitUser);

					// Loop through all repos to check for updates
					for (int j = 0; j < repoList.size(); j++) {
						Repository repo = repoList.get(j);

						// Update all user comments in this repo list
						updateUserGithubComments(gitUser, startDate, eventList, repo);
					}
				}

			} catch (IOException e) {
				if (e.getMessage().startsWith("API rate limit exceeded")) {
					// Rate limit exceeded, so abort
					sqlDb.insertLogData(LogDataModel.GITHUB_IMPORT_ABORTED, new StudentNameModel("", "", false), 0,
							": Github API rate limit exceeded ***");
					return false;

				} else {
					sqlDb.insertLogData(LogDataModel.GITHUB_IMPORT_FAILURE, event.getStudentNameModel(),
							event.getClientID(), " for gitUser '" + gitUser + "': " + e.getMessage());
				}
			}
		}
		return true;
	}

	public void importGithubCommentsByLevel(int level, String startDate, int clientID) {
		// Get all repositories by league level
		List<Repository> repoList = getRepoListByLevel(level);
		if (repoList == null)
			return;

		// Get all attendance since start date w/ github user name (use null for ALL
		// users) and no comments
		ArrayList<AttendanceEventModel> eventList = sqlDb.getEventsWithNoComments(startDate, clientID);
		String lastGithubUser = "";

		for (int i = 0; i < eventList.size(); i++) {
			// Get commit info from DB for each student/date combo
			AttendanceEventModel event = eventList.get(i);
			String gitUser = event.getGithubName();

			if (!gitUser.equals(lastGithubUser)) {
				// New github user, need to get new repos
				lastGithubUser = gitUser;

				for (int j = 0; j < repoList.size(); j++) {
					Repository repo = repoList.get(j);

					if (repo.getName().endsWith("-" + gitUser)) {
						// Update all user comments in this repo list
						updateUserGithubComments(gitUser, startDate, eventList, repo);
					}
				}
			}
		}
	}

	public void updateMissingGithubComments() {
		// Import github comments from start date for new github user names
		ArrayList<StudentModel> newGithubList = sqlDb.getStudentsUsingFlag("NewGithub");

		for (int i = 0; i < newGithubList.size(); i++) {
			StudentModel student = newGithubList.get(i);
			if (student.getStartDate() != null) {
				String catchupStartDate = student.getStartDate().toString();
				if (catchupStartDate.compareTo(DATABASE_START_DATE) < 0)
					catchupStartDate = DATABASE_START_DATE;

				importGithubComments(catchupStartDate, student.getClientID());
				importGithubCommentsByLevel(0, catchupStartDate, student.getClientID());

				// Set student 'new github' flag back to false
				sqlDb.updateStudentFlags(student, "NewGithub", 0);
			}
		}
	}

	private List<Repository> getRepoListByLevel(int level) {
		// League levels use Github classroom with user 'League-Level0-Student'
		String ownerName = "League-Level" + level + "-Student";
		List<Repository> repoList = null;

		try {
			repoList = repoService.getRepositories(ownerName);
			if (repoList.size() == 0)
				return null;

		} catch (IOException e1) {
			if (e1.getMessage().startsWith("API rate limit exceeded"))
				sqlDb.insertLogData(LogDataModel.GITHUB_IMPORT_ABORTED, new StudentNameModel("", "", false), 0,
						": Github API rate limit exceeded ***");
			else
				sqlDb.insertLogData(LogDataModel.GITHUB_MODULE_REPO_ERROR, null, 0,
						" for " + ownerName + ": " + e1.getMessage());
		}

		return repoList;
	}

	private void updateUserGithubComments(String githubUser, String startDate, List<AttendanceEventModel> eventList,
			Repository repo) {
		// Get all the commits for this repo within date range
		try {
			for (Collection<RepositoryCommit> commitPage : commitService.pageCommits(repo, 20)) {
				// Loop through each commit for this page
				for (RepositoryCommit commit : commitPage) {
					// Get commit date
					long commitDateLong = commit.getCommit().getCommitter().getDate().getTime();
					String commitDate = new DateTime(commitDateLong).toString("yyyy-MM-dd");

					// Commits ordered by date, so once date is old then move on
					if (commitDate.compareTo(startDate) < 0)
						return;

					// Find gituser & date match in event list; append multiple comments
					for (int k = 0; k < eventList.size(); k++) {
						AttendanceEventModel event = eventList.get(k);
						if (commitDate.equals(event.getServiceDateString())
								&& githubUser.equals(event.getGithubName())) {
							// Trim github message to get only summary data
							String message = trimMessage(commit.getCommit().getMessage());

							// Update comments & repo name, continue to next commit
							if (!message.equals("")) {
								event.setGithubComments(message);
								sqlDb.updateAttendance(event.getClientID(), event.getStudentNameModel(), commitDate,
										repo.getName(), event.getGithubComments());
							}
						}
					}
				}
			}

		} catch (NoSuchPageException e) {
			// Repo is empty, so just return
		}
	}

	private String trimMessage(String inputMsg) {
		// Trim message up to first new-line character
		inputMsg = inputMsg.trim();
		int idx = inputMsg.indexOf("\n");
		if (idx > -1)
			return inputMsg.substring(0, idx);
		else
			return inputMsg;
	}
}
