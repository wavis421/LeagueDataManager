package model;

public class TableHistoryModel {
	// Local constants
	public static final int UNKNOWN_TABLE       = 0;
	public static final int STUDENT_TABLE       = 1;
	public static final int ATTENDANCE_TABLE    = 2;
	public static final int COURSE_TABLE        = 3;
	public static final int LOG_TABLE           = 4;
	public static final int SCHEDULE_TABLE      = 5;
	public static final int SCHED_DETAILS_TABLE = 6;
	public static final int GITHUB_TABLE        = 7;

	// Location member variables
	String tableHeader = "";
	int tableType = UNKNOWN_TABLE;
	int tblSubType = 0;
	int clientID = 0;
	String searchText = "", className, classDate;
	boolean byStudentOrSinceDate;
	
	public TableHistoryModel (int tableType, String header, int clientID, int subType, String searchString) {
		this.tableType   = tableType;
		this.tableHeader = header;
		this.clientID    = clientID;
		this.tblSubType  = subType;
		this.searchText  = searchString;
	}
	
	public TableHistoryModel (int tableType, String header, String clientIDString, int subType, String searchString, 
			String className, String classDate, boolean byStudent) {
		this.tableType    = tableType;
		this.tableHeader  = header;
		if (clientIDString.equals(""))
			this.clientID = 0;
		else
			this.clientID = Integer.parseInt(clientIDString);
		this.tblSubType   = subType;
		this.searchText   = searchString;
		this.className    = className;
		this.classDate    = classDate;
		this.byStudentOrSinceDate = byStudent;
	}
	
	public String getTableHeader() {
		return tableHeader;
	}

	public int getTableType () {
		return tableType;
	}
	
	public Integer getClientID () {
		return clientID;
	}
	
	public int getTblSubType () {
		return tblSubType;
	}
	
	public String getSearchText () {
		return searchText;
	}
	
	public void setSearchText (String search) {
		searchText = search;
	}

	public String getClassName() {
		return className;
	}

	public String getClassDate() {
		return classDate;
	}

	public boolean isByStudentOrSinceDate() {
		return byStudentOrSinceDate;
	}
}
