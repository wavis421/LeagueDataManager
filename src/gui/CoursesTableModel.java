package gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import model.CoursesModel;

public class CoursesTableModel extends AbstractTableModel {
	public static final int COURSE_ID_COLUMN = 0;
	public static final int ENROLLED_COLUMN = 1;
	public static final int COURSE_DATE_COLUMN = 2;
	public static final int COURSE_NAME_COLUMN = 3;

	private ArrayList<CoursesModel> coursesList;
	private final String colNames[] = { " ID ", " # Enrolled " , " Date ", " Course Name " };

	public CoursesTableModel(ArrayList<CoursesModel> coursesList) {
		this.coursesList = coursesList;
	}

	public void setData(ArrayList<CoursesModel> db) {
		coursesList.clear();
		coursesList = db;
	}

	public void removeAll() {
		coursesList.clear();
	}

	public int getNumCourses() {
		return coursesList.size();
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
		return coursesList.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	public Object getValueByColumn(CoursesModel course, int col) {
		switch (col) {
		case COURSE_ID_COLUMN:
			return String.valueOf(course.getScheduleID());
		case COURSE_DATE_COLUMN:
			int openParen = course.getEventName().indexOf('(');
			int closeParen = course.getEventName().indexOf(')');
			if (openParen >= 0 && closeParen > openParen)
				return course.getEventName().substring(openParen + 1, closeParen);
			else
				return "";
		case COURSE_NAME_COLUMN:
			return "     " + course.getEventName();
		case ENROLLED_COLUMN:
			return String.valueOf(course.getEnrollment());
		}
		return null;
	}

	public CoursesModel getValueByRow(int row) {
		return coursesList.get(row);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CoursesModel course = coursesList.get(row);
		return getValueByColumn(course, col);
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

	public ArrayList<CoursesModel> getCsvDataList() {
		return coursesList;
	}

	public String convertItemToCsv(Object item) {
		String csvString = "";
		CoursesModel course = (CoursesModel) item;

		for (int i = 0; i < colNames.length; i++) {
			if (i > 0)
				csvString += ",";

			csvString += getValueByColumn(course, i);
		}
		return csvString;
	}
}
