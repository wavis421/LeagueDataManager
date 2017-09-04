package model;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class StudentTableModel extends AbstractTableModel {
	private static final int CLIENT_ID_COLUMN = 0;
	private static final int STUDENT_NAME_COLUMN = 1;
	private static final int GENDER_COLUMN = 2;
	private static final int GITHUB_NAME_COLUMN = 3;
	private static final int HOME_LOCATION_COLUMN = 4;
	private static final int START_DATE_COLUMN = 5;
	private static final int GRAD_YEAR_COLUMN = 6;

	private ArrayList<StudentModel> studentList;
	private String colNames[] = { " Client ID ", " Student Name ", " G ", " Github ", " Home Loc ", " Start Date ",
			" Grad Year " };

	public StudentTableModel(ArrayList<StudentModel> students) {
		this.studentList = students;
		System.out.println("Num Students: " + studentList.size());
	}

	public void setData(ArrayList<StudentModel> db) {
		studentList.clear();
		studentList = db;
		System.out.println("Num Students: " + studentList.size());
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
			return student.getClientID();
		case STUDENT_NAME_COLUMN:
			return student.getFirstName() + " " + student.getLastName();
		case GENDER_COLUMN:
			return GenderModel.convertGenderToString(student.getGender());
		case GITHUB_NAME_COLUMN:
			return student.getGithubName();
		case HOME_LOCATION_COLUMN:
			return LocationModel.convertLocationToString(student.getHomeLocation());
		case START_DATE_COLUMN:
			return student.getStartDate();
		case GRAD_YEAR_COLUMN:
			if (student.getGradYear() == 0)
				return "";
			else
				return String.valueOf(student.getGradYear());
		}
		return null;
	}

	public int getColumnForStudentName() {
		return STUDENT_NAME_COLUMN;
	}

	public int getColumnForGender() {
		return GENDER_COLUMN;
	}

	public int getColumnForClientID() {
		return CLIENT_ID_COLUMN;
	}

	public int getColumnForStartDate() {
		return START_DATE_COLUMN;
	}

	public int getColumnForGradYear() {
		return GRAD_YEAR_COLUMN;
	}

	public int getColumnForHomeLocation() {
		return HOME_LOCATION_COLUMN;
	}
}
