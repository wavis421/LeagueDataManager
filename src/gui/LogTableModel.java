package gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import model.LogDataModel;
import model.StudentNameModel;

public class LogTableModel extends AbstractTableModel {
	public static final int CLIENT_ID_COLUMN = 0;
	public static final int STUDENT_NAME_COLUMN = 1;
	public static final int STATUS_COLUMN = 2;

	private ArrayList<LogDataModel> logList;
	private final String[] colNames = { " Client ID ", " Student Name ", " Status Message " };

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

	public int getLogDataSize() {
		return logList.size();
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
		else
			return String.class;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	@Override
	public Object getValueAt(int row, int col) {
		LogDataModel logData = logList.get(row);

		switch (col) {
		case CLIENT_ID_COLUMN:
			if (logData.getClientID() == 0)
				return "";
			else
				return String.valueOf(logData.getClientID());
		case STUDENT_NAME_COLUMN:
			return logData.getStudentName();
		case STATUS_COLUMN:
			return "  " + logData.getLogString();
		}
		return null;
	}
}
