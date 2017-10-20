
DELIMITER $$
DROP PROCEDURE IF EXISTS CreateLeagueTables$$
CREATE PROCEDURE CreateLeagueTables()
BEGIN
	
	CREATE TABLE IF NOT EXISTS Students (
		# Unique ID from FrontDesk database
		ClientID int(11) NOT NULL,
		PRIMARY KEY (ClientID),
		
		# Student data
		FirstName varchar(20) DEFAULT NULL,
		LastName varchar(25) DEFAULT NULL,
		GithubName varchar(50) DEFAULT NULL,
		Gender int(11) DEFAULT 0,
		GradYear int(11) DEFAULT NULL,
		
		StartDate date DEFAULT NULL,
		Location int(11) DEFAULT 0,
		isInMasterDb BOOLEAN DEFAULT FALSE,
		CurrentClass varchar(25) DEFAULT NULL,
		NumClasses int(11) DEFAULT 0,
		
		UNIQUE KEY (GithubName)
	) ENGINE=InnoDB;
	
	CREATE TABLE IF NOT EXISTS Activities (
		ActivityID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (ActivityID),
		ClientID int(11),
		
		CONSTRAINT fk_activity_student_id
			FOREIGN KEY (ClientID) 
			REFERENCES Students(ClientID) 
			ON DELETE CASCADE,
		
		# Activity data
		ServiceDate date DEFAULT NULL,
		EventName varchar(50) DEFAULT NULL,
		RepoName varchar(50) DEFAULT NULL,
		Comments varchar(150) DEFAULT NULL,
		
		UNIQUE KEY(ClientID, EventName, ServiceDate)
				
	) ENGINE=InnoDB;

END$$
DELIMITER ;
