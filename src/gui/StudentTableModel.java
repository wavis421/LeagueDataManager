package gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import model.GenderModel;
import model.LocationLookup;
import model.StudentModel;
import model.StudentNameModel;

public class StudentTableModel extends AbstractTableModel {
	public static final int CLIENT_ID_COLUMN = 0;
	public static final int STUDENT_NAME_COLUMN = 1;

	// For student info table
	public static final int GENDER_COLUMN = 2;
	public static final int GITHUB_NAME_COLUMN = 3;
	public static final int HOME_LOCATION_COLUMN = 4;
	public static final int START_DATE_COLUMN = 5;
	public static final int GRAD_YEAR_COLUMN = 6;
	public static final int CURR_CLASS_COLUMN = 7;

	// For student email table
	public static final int STUDENT_EMAIL_COLUMN = 2;
	public static final int ACCT_MGR_EMAIL_COLUMN = 3;
	public static final int EMERGENCY_EMAIL_COLUMN = 4;
	public static final int CURR_CLASS_EMAIL_COLUMN = 5;

	// For student phone table
	public static final int STUDENT_PHONE_COLUMN = 2;
	public static final int ACCT_MGR_PHONE_COLUMN = 3;
	public static final int HOME_PHONE_COLUMN = 4;
	public static final int EMERGENCY_PHONE_COLUMN = 5;
	public static final int CURR_CLASS_PHONE_COLUMN = 6;

	// For student TA table
	public static final int TA_SINCE_COLUMN = 2;
	public static final int TA_PAST_EVENTS = 3;
	public static final int TA_AGE_COLUMN = 4;
	public static final int TA_CURR_LEVEL_COLUMN = 5;
	public static final int TA_EMAIL_COLUMN = 6;
	public static final int TA_PHONE_COLUMN = 7;

	private ArrayList<StudentModel> studentList;
	private int tableType;
	private String colNames[];

	// Table column names for each table type
	private final String colStdNames[] = { " ID ", " Student Name ", " G ", " Github ", " Home Loc ", " Start Date ",
			" Grad Yr ", " Current Class " };
	private final String colEmailNames[] = { " ID ", " Student Name ", " Student Email ", " Acct Mgr Email ",
			" Emerg Email ", " Current Class " };
	private final String colPhoneNames[] = { " ID ", " Student Name ", " Student Phone ", " Acct Mgr Phone ",
			" Home Phone ", " Emerg Phone ", " Current Class " };
	private final String colTANames[] = { " ID ", " Student Name ", " TA Start Date ", " # Classes ", " Curr Age ",
			" Curr Level ", " Student Email ", " Student Phone " };

	public StudentTableModel(ArrayList<StudentModel> students) {
		this.studentList = students;
		this.tableType = StudentTable.STANDARD_STUDENT_TABLE_TYPE;
		colNames = colStdNames;

		System.out.println("Num Students: " + studentList.size());
	}

	public void setData(ArrayList<StudentModel> db, int tableType) {
		this.tableType = tableType;
		if (tableType == StudentTable.STANDARD_STUDENT_TABLE_TYPE)
			colNames = colStdNames;
		else if (tableType == StudentTable.EMAIL_STUDENT_TABLE_TYPE)
			colNames = colEmailNames;
		else if (tableType == StudentTable.PHONE_STUDENT_TABLE_TYPE)
			colNames = colPhoneNames;
		else
			colNames = colTANames;

		studentList.clear();
		studentList = db;
		System.out.println("Num Students: " + studentList.size());
	}

	public void removeAll() {
		studentList.clear();
	}

	public int getNumStudents() {
		return studentList.size();
	}

	@Override
	public int getColumnCount() {
		return colNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return colNames[column];
	}

	@Override
	public int getRowCount() {
		return studentList.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == STUDENT_NAME_COLUMN)
			return StudentNameModel.class;
		else if (tableType == StudentTable.TA_STUDENT_TABLE_TYPE && columnIndex == TA_PAST_EVENTS)
			return Integer.class;
		else
			return String.class;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	public Object getValueByColumn(StudentModel student, int col) {
		if (tableType == StudentTable.STANDARD_STUDENT_TABLE_TYPE) {
			switch (col) {
			case CLIENT_ID_COLUMN:
				return String.valueOf(student.getClientID());
			case STUDENT_NAME_COLUMN:
				return student.getNameModel();
			case GENDER_COLUMN:
				return GenderModel.convertGenderToString(student.getGender());
			case GITHUB_NAME_COLUMN:
				return student.getGithubName();
			case HOME_LOCATION_COLUMN:
				return LocationLookup.convertLocationToString(student.getHomeLocation());
			case START_DATE_COLUMN:
				if (student.getStartDate() == null)
					return "";
				else
					return student.getStartDate().toString();
			case GRAD_YEAR_COLUMN:
				if (student.getGradYear() == 0)
					return "";
				else
					return String.valueOf(student.getGradYear());
			case CURR_CLASS_COLUMN:
				if (student.getCurrentClass() == null)
					return "";
				else
					return student.getCurrentClass();
			}
		} else if (tableType == StudentTable.EMAIL_STUDENT_TABLE_TYPE) {
			switch (col) {
			case CLIENT_ID_COLUMN:
				return String.valueOf(student.getClientID());
			case STUDENT_NAME_COLUMN:
				return student.getNameModel();
			case STUDENT_EMAIL_COLUMN:
				return student.getEmail();
			case ACCT_MGR_EMAIL_COLUMN:
				return student.getAcctMgrEmail();
			case EMERGENCY_EMAIL_COLUMN:
				return student.getEmergEmail();
			case CURR_CLASS_EMAIL_COLUMN:
				if (student.getCurrentClass() == null)
					return "";
				else
					return student.getCurrentClass();
			}
		} else if (tableType == StudentTable.PHONE_STUDENT_TABLE_TYPE) {
			switch (col) {
			case CLIENT_ID_COLUMN:
				return String.valueOf(student.getClientID());
			case STUDENT_NAME_COLUMN:
				return student.getNameModel();
			case STUDENT_PHONE_COLUMN:
				return student.getPhone();
			case ACCT_MGR_PHONE_COLUMN:
				return student.getAcctMgrPhone();
			case HOME_PHONE_COLUMN:
				return student.getHomePhone();
			case EMERGENCY_PHONE_COLUMN:
				return student.getEmergPhone();
			case CURR_CLASS_PHONE_COLUMN:
				if (student.getCurrentClass() == null)
					return "";
				else
					return student.getCurrentClass();
			}
		} else {
			switch (col) {
			case CLIENT_ID_COLUMN:
				return String.valueOf(student.getClientID());
			case STUDENT_NAME_COLUMN:
				return student.getNameModel();
			case TA_SINCE_COLUMN:
				return student.getStaffSinceDate();
			case TA_PAST_EVENTS:
				return (Integer) student.getStaffPastEvents();
			case TA_AGE_COLUMN:
				if (student.getAge() == 0)
					return "";
				else
					return String.valueOf(student.getAge());
			case TA_CURR_LEVEL_COLUMN:
				if (student.getCurrentClass() == null || student.getCurrentClass().length() < 1)
					return "";
				else
					return student.getCurrentClass().substring(0, 1);
			case TA_EMAIL_COLUMN:
				return student.getEmail();
			case TA_PHONE_COLUMN:
				return student.getPhone();
			}
		}
		return null;
	}

	public StudentModel getValueByRow(int row) {
		return studentList.get(row);
	}

	@Override
	public Object getValueAt(int row, int col) {
		StudentModel student = studentList.get(row);
		return getValueByColumn(student, col);
	}

	// CSV file export support
	public String getCsvFileHeader() {
		String header = "";
		for (int i = 0; i < colNames.length; i++) {
			if (i > 0)
				header += ",";
			header += colNames[i];
		}
		return header;
	}

	public ArrayList<StudentModel> getCsvDataList() {
		return studentList;
	}

	public String convertItemToCsv(Object item) {
		String csvString = "";
		StudentModel student = (StudentModel) item;

		for (int i = 0; i < colNames.length; i++) {
			if (i > 0)
				csvString += ",";

			if (i == STUDENT_NAME_COLUMN)
				csvString += getValueByColumn(student, i).toString();
			else
				csvString += getValueByColumn(student, i);
		}
		return csvString;
	}
}
