package model;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class StudentTableModel extends AbstractTableModel {
	private static final int STUDENT_NAME_COLUMN = 0;
	private static final int GITHUB_NAME_COLUMN = 1;
	private static final int LOCATION_COLUMN = 2;
	private static final int CLASS_TIME_COLUMN = 3;

	private ArrayList<StudentModel> studentList;
	private String colNames[] = { "Name", "Github", "Location", "Class" };

	public StudentTableModel(ArrayList<StudentModel> students) {
		this.studentList = students;
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
		case STUDENT_NAME_COLUMN:
			return student.getFirstName() + " " + student.getLastName();
		case GITHUB_NAME_COLUMN:
			return student.getGithubName();
		case LOCATION_COLUMN:
			return student.getLocation();
		case CLASS_TIME_COLUMN:
			return student.getClassTime();
		}
		return null;
	}

	public int getColumnForStudentName() {
		return STUDENT_NAME_COLUMN;
	}

	/*
	public int getColumnForLocation() {
		return LOCATION_COLUMN;
	}

	public int getColumnForPhone() {
		return CLASS_TIME_COLUMN;
	}
	*/
}
