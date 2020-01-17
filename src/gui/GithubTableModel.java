package gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import model_for_gui.GithubModel;

public class GithubTableModel extends AbstractTableModel {
	public static final int CLIENT_ID_COLUMN = 0;
	public static final int STUDENT_NAME_COLUMN = 1;
	public static final int STUDENT_LEVEL_COLUMN = 2;
	public static final int DOW_COLUMN = 3;
	public static final int CLASS_NAME_COLUMN = 4;
	public static final int GITHUB_NAME_COLUMN = 5;
	public static final int TEACHER_COLUMN = 6;

	private ArrayList<GithubModel> githubList;
	private final String colNames[] = { " ID ", " Student Name ", " Lvl ", " DOW ", " Class ", " Github ", " Teachers " };

	public GithubTableModel(ArrayList<GithubModel> githubList) {
		this.githubList = githubList;
	}

	public void setData(ArrayList<GithubModel> db) {
		githubList.clear();
		githubList = db;
	}

	public void removeAll() {
		githubList.clear();
	}

	public int getNumStudents() {
		return githubList.size();
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
		return githubList.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	public Object getValueByColumn(GithubModel student, int col) {
		switch (col) {
		case CLIENT_ID_COLUMN:
			return String.valueOf(student.getClientID());
		case STUDENT_NAME_COLUMN:
			return student.getStudentName();
		case STUDENT_LEVEL_COLUMN:
			return student.getCurrLevel();
		case DOW_COLUMN:
			return student.getDow();
		case CLASS_NAME_COLUMN:
			return student.getClassName();
		case GITHUB_NAME_COLUMN:
			return student.getGithubName();
		case TEACHER_COLUMN:
			return student.getTeachers();
		}
		return null;
	}

	public GithubModel getValueByRow(int row) {
		return githubList.get(row);
	}

	@Override
	public Object getValueAt(int row, int col) {
		GithubModel student = githubList.get(row);
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

	public ArrayList<GithubModel> getCsvDataList() {
		return githubList;
	}

	public String convertItemToCsv(Object item) {
		String csvString = "";
		GithubModel student = (GithubModel) item;

		for (int i = 0; i < colNames.length; i++) {
			if (i > 0)
				csvString += ",";

			csvString += getValueByColumn(student, i);
		}
		return csvString;
	}
}
