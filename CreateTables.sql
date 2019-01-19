
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
		CurrentClass varchar(75) DEFAULT '',
		NewGithub boolean DEFAULT 0,
		NewStudent boolean DEFAULT 0,
		Email varchar(70) DEFAULT '',
		EmergencyEmail varchar(70) DEFAULT '',
		AcctMgrEmail varchar(140) DEFAULT '',
		Phone varchar(20) DEFAULT '',
		AcctMgrPhone varchar(60) DEFAULT '',
		EmergencyPhone varchar(30) DEFAULT '',
		HomePhone varchar(15) DEFAULT '',
		Birthdate varchar(20) DEFAULT '',
		TASinceDate varchar(20) DEFAULT '',
		TAPastEvents int(5) DEFAULT 0,
		CurrentModule varchar(3) DEFAULT NULL,
		TALastServDate varchar(20) DEFAULT '',
		CurrentLevel varchar(5) DEFAULT '',
		LastVisitDate date DEFAULT NULL,
		RegisterClass varchar(75) DEFAULT '',
		LastScore varchar(20) DEFAULT '',
		
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
		ServiceTime varchar(10) DEFAULT '',
		EventName varchar(100) DEFAULT NULL,
		RepoName varchar(50) DEFAULT NULL,
		Comments varchar(150) DEFAULT NULL,
		VisitID int(11) DEFAULT NULL,
		TeacherNames varchar(100) DEFAULT NULL,
		SerivceCategory varchar(30) DEFAULT NULL,
		State varchar(30) DEFAULT NULL,
		LastSFState varchar(30) DEFAULT NULL,
		ClassLevel varchar(1) DEFAULT '',
		
		UNIQUE KEY(ClientID, EventName, ServiceDate, ServiceTime)
				
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
		NumStudents int(5) DEFAULT 0,
		Youngest varchar(6) DEFAULT '';
		Oldest varchar(6) DEFAULT '';
		AverageAge varchar(6) DEFAULT '';
		ModuleCount varchar(80) DEFAULT '';

		# All fields of this table must be unique
		UNIQUE KEY(DayOfWeek, StartTime, ClassName)

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
	
	CREATE TABLE IF NOT EXISTS Location (
		LocDataID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (LocDataID),
		
		# Location data
		LocIdx int(11) DEFAULT 0,
		LocCode varchar(10) NOT NULL,
		LocName varchar(50) NOT NULL,
		Notes varchar(50) DEFAULT NULL,
		LocNameLong varchar(50) DEFAULT NULL,

		UNIQUE KEY(LocIdx, LocCode, LocName)

	) ENGINE=InnoDB;
	
	CREATE TABLE IF NOT EXISTS Graduation (
		GradID int(11) NOT NULL AUTO_INCREMENT,
		PRIMARY KEY (GradID),
		ClientID int(11),
		
		CONSTRAINT fk_graduation_student_id
			FOREIGN KEY (ClientID) 
			REFERENCES Students(ClientID) 
			ON DELETE CASCADE,
		
		# Graduation data
		GradLevel int(4) NOT NULL,
		StartDate date DEFAULT NULL,
		EndDate date DEFAULT NULL,
		Score varchar(50) DEFAULT NULL,
		InSalesForce boolean DEFAULT 0,
		SkipLevel boolean DEFAULT 0,
		VerifiedSF boolean DEFAULT 0,
		CurrentClass varchar(75) DEFAULT '',
		Promoted boolean DEFAULT 0,
		
		UNIQUE KEY(ClientID, GradLevel)
		
	) ENGINE=InnoDB;
	
END$$
DELIMITER ;
