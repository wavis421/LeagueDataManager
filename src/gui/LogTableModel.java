package gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import model.LogDataModel;
import model.StudentNameModel;

public class LogTableModel extends AbstractTableModel {
	public static final int DATE_COLUMN = 0;
	public static final int CLIENT_ID_COLUMN = 1;
	public static final int STUDENT_NAME_COLUMN = 2;
	public static final int STATUS_COLUMN = 3;
	public static final int LOG_ID_COLUMN = 4;

	private ArrayList<LogDataModel> logList;
	private final String[] colNames = { "  Date  ", " ID ", " Student Name ", " Status Message ", "" };

	public LogTableModel(ArrayList<LogDataModel> logData) {
		this.logList = logData;
	}

	public void setData(ArrayList<LogDataModel> db) {
		logList.clear();
		logList = db;
	}

	public void removeAll() {
		logList.clear();
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
		return logList.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == STUDENT_NAME_COLUMN)
			return StudentNameModel.class;
		else if (columnIndex == LOG_ID_COLUMN)
			return Integer.class;
		else
			return String.class;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	private Object getValueByColumn(LogDataModel logData, int col) {
		switch (col) {
		case DATE_COLUMN:
			return logData.getDate();
		case CLIENT_ID_COLUMN:
			if (logData.getClientID() == 0)
				return "";
			else
				return String.valueOf(logData.getClientID());
		case STUDENT_NAME_COLUMN:
			return logData.getStudentName();
		case STATUS_COLUMN:
			return logData.getLogString();
		case LOG_ID_COLUMN:
			return logData.getLogID();
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		LogDataModel logData = logList.get(row);
		return getValueByColumn(logData, col);
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

	public ArrayList<LogDataModel> getCsvDataList() {
		return logList;
	}

	public String convertItemToCsv(Object item) {
		String csvString = "";
		LogDataModel logData = (LogDataModel) item;

		for (int i = 0; i < colNames.length; i++) {
			if (i > 0)
				csvString += ",";
			csvString += getValueByColumn(logData, i);
		}
		return csvString;
	}
}
