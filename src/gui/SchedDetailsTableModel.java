package gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import model.ScheduleModel;

/**
 * Table model for weekly scheduled class details
 * 
 * @author wavis
 *
 */
public class SchedDetailsTableModel extends AbstractTableModel {
	public static final int CLASS_NAME_COLUMN = 0;
	public static final int DOW_COLUMN = 1;
	public static final int TIME_COLUMN = 2;
	public static final int MODULE_RANGE_COLUMN = 3;
	public static final int NUM_STUDENTS_COLUMN = 4;
	public static final int AVG_AGE_COLUMN = 5;
	public static final int MIN_AGE_COLUMN = 6;
	public static final int MAX_AGE_COLUMN = 7;
	public static final int NUM_COLUMNS = 8;

	private ArrayList<ScheduleModel> schedList;
	private final String colNames[] = { " Class Name ", " DOW ", " Time ", " Current Module Range ", " # Students ",
			" Avg Age ", " Min Age ", " Max Age " };

	public SchedDetailsTableModel(ArrayList<ScheduleModel> schedList) {
		this.schedList = schedList;
	}

	public void setData(ArrayList<ScheduleModel> db) {
		schedList.clear();
		schedList = db;
	}

	public void removeAll() {
		schedList.clear();
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
		return schedList.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	public Object getValueByColumn(ScheduleModel sched, int col) {
		switch (col) {
		case CLASS_NAME_COLUMN:
			return sched.getClassName();
		case DOW_COLUMN:
			return sched.getDayOfWeekFormatted();
		case TIME_COLUMN:
			return sched.getStartTimeFormatted();
		case NUM_STUDENTS_COLUMN:
			return ((Integer) sched.getAttCount()).toString();
		case MIN_AGE_COLUMN:
			return sched.getAgeMin();
		case MAX_AGE_COLUMN:
			return sched.getAgeMax();
		case AVG_AGE_COLUMN:
			return sched.getAgeAvg();
		case MODULE_RANGE_COLUMN:
			return "";
		}
		return null;
	}

	public ScheduleModel getValueByRow(int row) {
		return schedList.get(row);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ScheduleModel thisClass = schedList.get(row);
		return getValueByColumn(thisClass, col);
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

	public ArrayList<ScheduleModel> getCsvDataList() {
		return schedList;
	}

	// Conversion of column data to CSV format
	public String convertItemToCsv(Object item) {
		String csvString = "";
		ScheduleModel course = (ScheduleModel) item;

		for (int i = 0; i < colNames.length; i++) {
			if (i > 0)
				csvString += ",";

			csvString += getValueByColumn(course, i);
		}
		return csvString;
	}
}
