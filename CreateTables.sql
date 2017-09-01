
DELIMITER $$
DROP PROCEDURE IF EXISTS CreateLeagueTables$$
CREATE PROCEDURE CreateLeagueTables()
BEGIN
	
	CREATE TABLE IF NOT EXISTS Students (
		StudentID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (StudentID),
		
		# Student data
		LastName varchar(25) DEFAULT NULL,
		FirstName varchar(20) DEFAULT NULL,
		GithubName varchar(50) DEFAULT NULL,
		Gender CHAR DEFAULT NULL,
		BirthDate date DEFAULT NULL,
		
		StartDate date DEFAULT NULL,
		Location varchar(25) DEFAULT NULL,

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
		ActivityDate date DEFAULT NULL,
		RecipesCompleted varchar(200)
				
	) ENGINE=InnoDB;

END$$
DELIMITER ;
