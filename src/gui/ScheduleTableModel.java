package gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import model.ScheduleModel;

public class ScheduleTableModel extends AbstractTableModel {
	public static final int DAY_OF_WEEK_COLUMN = 0;
	public static final int START_TIME_COLUMN = 1;
	public static final int CLASS_NAME_COLUMN = 2;

	private ArrayList<ScheduleModel> scheduleList;
	private final String[] colNames = { " Day of Week ", " Start ", " Class Name " };
	private final String[] dayOfWeek = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };

	public ScheduleTableModel(ArrayList<ScheduleModel> scheduleList) {
		this.scheduleList = scheduleList;
		System.out.println("Num Classes: " + scheduleList.size());
	}

	public void setData(ArrayList<ScheduleModel> db) {
		scheduleList.clear();
		scheduleList = db;
		System.out.println("Num Classes: " + scheduleList.size());
	}

	public void removeAll() {
		scheduleList.clear();
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
		case DAY_OF_WEEK_COLUMN:
			return dayOfWeek[scheduleData.getDayOfWeek()];
		case START_TIME_COLUMN:
			return scheduleData.getStartTimeFormatted();
		case CLASS_NAME_COLUMN:
			return scheduleData.getClassName();
		}
		return null;
	}
}
