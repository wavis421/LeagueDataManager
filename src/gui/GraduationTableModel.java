package gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import model.GraduationModel;

/**
 * Table model for Graduation data
 * 
 * @author wavis
 *
 */
public class GraduationTableModel extends AbstractTableModel {
	public static final int STUDENT_NAME_COLUMN = 0;
	public static final int LEVEL_PASSED_COLUMN = 1;
	public static final int PROCESSED_COLUMN = 2;
	public static final int IN_SALESFORCE_COLUMN = 3;
    public static final int CURRENT_CLASS_COLUMN = 4;
	public static final int SKIP_LEVEL_COLUMN = 5;
	public static final int PROMOTED_COLUMN = 6;
	public static final int SCORE_COLUMN = 7;
	public static final int START_DATE_COLUMN = 8;
	public static final int GRAD_DATE_COLUMN = 9;
	public static final int CLIENT_ID_COLUMN = 10; // not a real column
	public static final int NUM_COLUMNS = 11;

	private ArrayList<GraduationModel> gradList;
	private final String colNames[] = { " Student Name ", " Level ", " Processed ", " In SalesForce ",
			" Current Class ", " Skip Level ", " Promoted ", " Score ", " Start Date ", " Grad Date " };

	public GraduationTableModel(ArrayList<GraduationModel> gradList) {
		this.gradList = gradList;
	}

	public void setData(ArrayList<GraduationModel> db) {
		gradList.clear();
		gradList = db;
	}

	public void removeAll() {
		gradList.clear();
	}

	public int getClientID(int row) {
		return gradList.get(row).getClientID();
	}

	public void setProcessed(int row, boolean checked) {
		gradList.get(row).setProcessed(checked);
	}

	public void setScore(int row, String score) {
		gradList.get(row).setScore(score);
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
		return gradList.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case SKIP_LEVEL_COLUMN:
		case PROMOTED_COLUMN:
		case IN_SALESFORCE_COLUMN:
		case PROCESSED_COLUMN:
			return Boolean.class;
		}
		// All remaining columns are Strings
		return String.class;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (col == PROCESSED_COLUMN) // Check-box
			return true;
		else
			return false;
	}

	public Object getValueByColumn(GraduationModel grad, int col) {
		switch (col) {
		case STUDENT_NAME_COLUMN:
			return grad.getStudentName();
		case SKIP_LEVEL_COLUMN:
			return grad.isSkipLevel();
		case PROMOTED_COLUMN:
			return grad.isPromoted();
		case LEVEL_PASSED_COLUMN:
			return grad.getGradLevelString();
		case START_DATE_COLUMN:
			return grad.getStartDate();
		case GRAD_DATE_COLUMN:
			return grad.getEndDate();
		case IN_SALESFORCE_COLUMN:
			return grad.isSfUpdated();
		case CURRENT_CLASS_COLUMN:
			return grad.getCurrentClass();
		case PROCESSED_COLUMN:
			return grad.isProcessed();
		case SCORE_COLUMN:
			return grad.getScore();
		}
		return null;
	}

	public GraduationModel getValueByRow(int row) {
		return gradList.get(row);
	}

	@Override
	public Object getValueAt(int row, int col) {
		GraduationModel course = gradList.get(row);
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

	public ArrayList<GraduationModel> getCsvDataList() {
		return gradList;
	}

	// Conversion of column data to CSV format
	public String convertItemToCsv(Object item) {
		String csvString = "";
		GraduationModel course = (GraduationModel) item;

		for (int i = 0; i < colNames.length; i++) {
			if (i > 0)
				csvString += ",";

			csvString += getValueByColumn(course, i);
		}
		return csvString;
	}
}
