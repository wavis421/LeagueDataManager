
DELIMITER $$
DROP PROCEDURE IF EXISTS CreateLeagueTables$$
CREATE PROCEDURE CreateLeagueTables()
BEGIN
	
	CREATE TABLE IF NOT EXISTS Students (
		StudentID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (StudentID),
		
		# Student data
		FirstName varchar(20) DEFAULT NULL,
		LastName varchar(25) DEFAULT NULL,
		GithubName varchar(50) DEFAULT NULL,
		Gender int(11) DEFAULT 0,
		GradYear int(11) DEFAULT NULL,
		
		StartDate date DEFAULT NULL,
		Location int(11) DEFAULT 0,
		CurrentClass varchar(25) DEFAULT NULL,
		NumClasses int(11) DEFAULT 0,
		
		# Unique ID from FrontDesk database
		ClientID int(11) NOT NULL,
		
		UNIQUE KEY (ClientID),
		UNIQUE KEY (GithubName)
	) ENGINE=InnoDB;
	
	CREATE TABLE IF NOT EXISTS Activities (
		ActivityID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (ActivityID),
		StudentID int(11),
		
		CONSTRAINT fk_activity_student_id
			FOREIGN KEY (StudentID) 
			REFERENCES Students(StudentID) 
			ON DELETE CASCADE,
		
		# Activity data
		ServiceDate date DEFAULT NULL,
		EventName varchar(50) DEFAULT NULL,
		Comments varchar(100) DEFAULT NULL,
		
		UNIQUE KEY(StudentID, EventName, ServiceDate)
				
	) ENGINE=InnoDB;

END$$
DELIMITER ;
