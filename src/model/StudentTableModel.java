package model;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class StudentTableModel extends AbstractTableModel {
	public static final int CLIENT_ID_COLUMN = 0;
	public static final int STUDENT_NAME_COLUMN = 1;
	public static final int GENDER_COLUMN = 2;
	public static final int GITHUB_NAME_COLUMN = 3;
	public static final int HOME_LOCATION_COLUMN = 4;
	public static final int START_DATE_COLUMN = 5;
	public static final int GRAD_YEAR_COLUMN = 6;

	private ArrayList<StudentModel> studentList;
	private final String colNames[] = { " Client ID ", " Student Name ", " G ", " Github ", " Home Loc ",
			" Start Date ", " Grad Year " };

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

	@Override
	public Object getValueAt(int row, int col) {
		StudentModel student = studentList.get(row);

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
}
