
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
		NewGithub boolean DEFAULT 0,
		NewStudent boolean DEFAULT 0,
		
		UNIQUE KEY (GithubName)
	) ENGINE=InnoDB;
	
	CREATE TABLE IF NOT EXISTS Attendance (
		AttendanceID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (AttendanceID),
		ClientID int(11),
		
		CONSTRAINT fk_attendance_student_id
			FOREIGN KEY (ClientID) 
			REFERENCES Students(ClientID) 
			ON DELETE CASCADE,
		
		# Attendance data
		ServiceDate date DEFAULT NULL,
		EventName varchar(50) DEFAULT NULL,
		RepoName varchar(50) DEFAULT NULL,
		Comments varchar(150) DEFAULT NULL,
		TeacherNames varchar(100) DEFAULT NULL,
		
		UNIQUE KEY(ClientID, EventName, ServiceDate)
				
	) ENGINE=InnoDB;
	
	CREATE TABLE IF NOT EXISTS LogData (
		LogDataID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (LogDataID),
		
		# Log data
		ClientID int(11) DEFAULT NULL,
		LogType int(11) NOT NULL,
		StudentName varchar(50) DEFAULT NULL,
		AppendedString varchar(120) DEFAULT NULL,
		LogDate datetime DEFAULT NULL,

		UNIQUE KEY(ClientID, LogType, StudentName, AppendedString)

	) ENGINE=InnoDB;

	CREATE TABLE IF NOT EXISTS Schedule (
		ScheduleID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (ScheduleID),
		
		# Schedule data
		DayOfWeek int(11) DEFAULT NULL,
		StartTime varchar(10) DEFAULT NULL,
		Duration int(5) DEFAULT NULL,
		ClassName varchar(40) DEFAULT NULL,

		# All fields of this table must be unique
		UNIQUE KEY(DayOfWeek, StartTime, Duration, ClassName)

	) ENGINE=InnoDB;
	
	CREATE TABLE IF NOT EXISTS Courses (
		CourseID int(11) NOT NULL,
		PRIMARY KEY (CourseID),
		
		# Course data
		EventName varchar(100) DEFAULT NULL,
		Enrolled int(11) DEFAULT 0,

		# Course ID is unique
		UNIQUE KEY(CourseID)

	) ENGINE=InnoDB;
	
END$$
DELIMITER ;
