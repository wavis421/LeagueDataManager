package gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import model.GenderModel;
import model.LocationModel;
import model.StudentModel;
import model.StudentNameModel;

public class StudentTableModel extends AbstractTableModel {
	public static final int CLIENT_ID_COLUMN = 0;
	public static final int STUDENT_NAME_COLUMN = 1;
	public static final int GENDER_COLUMN = 2;
	public static final int GITHUB_NAME_COLUMN = 3;
	public static final int HOME_LOCATION_COLUMN = 4;
	public static final int START_DATE_COLUMN = 5;
	public static final int GRAD_YEAR_COLUMN = 6;

	private ArrayList<StudentModel> studentList;
	private final String colNames[] = { " ID ", " Student Name ", " G ", " Github ", " Home Loc ", " Start Date ",
			" Grad Yr " };

	public StudentTableModel(ArrayList<StudentModel> students) {
		this.studentList = students;
		System.out.println("Num Students: " + studentList.size());
	}

	public void setData(ArrayList<StudentModel> db) {
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
		else
			return String.class;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	public Object getValueByColumn(StudentModel student, int col) {
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
			return LocationModel.convertLocationToString(student.getHomeLocation());
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
		}
		return null;
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
