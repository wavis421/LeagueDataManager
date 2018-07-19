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
	public static final int CLIENT_ID_COLUMN = 4; // not a real column
	public static final int NUM_COLUMNS = 5;

	private ArrayList<GraduationModel> gradList;
	private final String colNames[] = { " Student Name ", " Level Passed ", " Processed ", " In SalesForce " };

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

	public int getNumCourses() {
		return gradList.size();
	}

	public void setProcessed(int row, boolean checked) {
		gradList.get(row).setProcessed(checked);
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
		case LEVEL_PASSED_COLUMN:
			return grad.getGradLevel();
		case IN_SALESFORCE_COLUMN:
			return grad.isSfUpdated();
		case PROCESSED_COLUMN:
			return grad.isProcessed();
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
