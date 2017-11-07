package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.joda.time.DateTime;

import model.ActivityEventModel;
import model.LogDataModel;
import model.MySqlDatabase;

public class GitApiController {
	private MySqlDatabase sqlDb;
	private RepositoryService repoService;
	private CommitService commitService;

	public GitApiController(MySqlDatabase sqlDb, String githubToken) {
		this.sqlDb = sqlDb;

		// OAuth2 token authentication
		GitHubClient client = new GitHubClient();
		client.setOAuth2Token(githubToken);

		// Get Repo and Commit services
		repoService = new RepositoryService(client);
		commitService = new CommitService(client);
	}

	public void importGithubComments(String startDate) {
		// Get all activities since 'startDate' w/ github user name and no comments
		ArrayList<ActivityEventModel> eventList = sqlDb.getEventsWithNoComments(startDate);
		String lastGithubUser = "";
		List<Repository> repoList = null;

		for (int i = 0; i < eventList.size(); i++) {
			// Get commit info from DB for each student/date combo
			ActivityEventModel event = eventList.get(i);
			String gitUser = event.getGithubName();

			try {
				if (!gitUser.equals(lastGithubUser)) {
					// New github user, need to get new repo array
					lastGithubUser = gitUser;
					repoList = repoService.getRepositories(gitUser);
					if (repoList.size() == 0)
						continue;

					// Loop through all repos to check for updates
					for (int j = 0; j < repoList.size(); j++) {
						Repository repo = repoList.get(j);
						if (repo.getSize() == 0)
							continue;

						// Update all user comments in this repo list
						updateUserGithubComments(gitUser, startDate, eventList, repo);
					}
				}

			} catch (IOException e) {
				if (e.getMessage().startsWith("API rate limit exceeded")) {
					// Rate limit exceeded, so abort
					JOptionPane.showMessageDialog(null,
							"Aborting Github import: Github API rate limit exceeded.\nPlease wait 1 hour and try again.");
					break;

				} else {
					sqlDb.insertLogData(LogDataModel.GITHUB_IMPORT_FAILURE, event.getStudentNameModel(),
							event.getClientID(), " for gitUser '" + gitUser + "': " + e.getMessage());
				}
			}
		}
	}

	public void importGithubCommentsByLevel(int level, String startDate) {
		// Get all repositories by league level
		List<Repository> repoList = getRepoListByLevel(level);
		if (repoList == null)
			return;

		// Get all activities since start date w/ github user name and no comments
		ArrayList<ActivityEventModel> eventList = sqlDb.getEventsWithNoComments(startDate);
		String lastGithubUser = "";

		for (int i = 0; i < eventList.size(); i++) {
			// Get commit info from DB for each student/date combo
			ActivityEventModel event = eventList.get(i);
			String gitUser = event.getGithubName();

			if (!gitUser.equals(lastGithubUser)) {
				// New github user, need to get new repos
				lastGithubUser = gitUser;

				for (int j = 0; j < repoList.size(); j++) {
					Repository repo = repoList.get(j);
					if (repo.getSize() == 0)
						continue;

					if (repo.getName().endsWith("-" + gitUser)) {
						// Update all user comments in this repo list
						updateUserGithubComments(gitUser, startDate, eventList, repo);
					}
				}
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
			sqlDb.insertLogData(LogDataModel.GITHUB_MODULE_REPO_ERROR, null, 0,
					" for " + ownerName + ": " + e1.getMessage());
		}

		return repoList;
	}

	private void updateUserGithubComments(String githubUser, String startDate, List<ActivityEventModel> eventList,
			Repository repo) {
		// Get all the commits for this repo within date range
		try {
			for (Collection<RepositoryCommit> commitPage : commitService.pageCommits(repo, 200)) {
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
						ActivityEventModel event = eventList.get(k);
						if (commitDate.equals(event.getServiceDateString())
								&& githubUser.equals(event.getGithubName())) {
							// Trim github message to get only summary data
							String message = trimMessage(commit.getCommit().getMessage());

							// Update comments & repo name, continue to next commit
							event.setGithubComments(message);
							sqlDb.updateActivity(event.getClientID(), event.getStudentNameModel(), commitDate,
									repo.getName(), event.getGithubComments());
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
