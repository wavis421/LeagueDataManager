package gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import model.ScheduleModel;

public class ScheduleTableModel extends AbstractTableModel {
	public static final int START_TIME_COLUMN = 0;
	public static final int CLASS_NAME_COLUMN = 1;

	private ArrayList<ScheduleModel> scheduleList;
	private int dow;
	private final String[] colNames = { " Start ", " Class Name " };

	public ScheduleTableModel(ArrayList<ScheduleModel> scheduleList, int dow) {
		this.scheduleList = scheduleList;
		this.dow = dow; // Starts Sunday (0)
	}

	public void setData(ArrayList<ScheduleModel> db) {
		scheduleList = db;
	}

	public void removeAll() {
		scheduleList.clear();
	}

	public int getDow() {
		return dow;
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
		return scheduleList.size();
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
		ScheduleModel scheduleData = scheduleList.get(row);

		switch (col) {
		case START_TIME_COLUMN:
			return scheduleData.getStartTimeFormatted();
		case CLASS_NAME_COLUMN:
			return scheduleData.getClassName();
		}
		return null;
	}
}
