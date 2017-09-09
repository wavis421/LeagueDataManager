package model;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class ActivityTableModel extends AbstractTableModel {
	private static final int CLIENT_ID_COLUMN = 0;
	private static final int STUDENT_NAME_COLUMN = 1;
	private static final int SERVICE_DATE_COLUMN = 2;
	private static final int CLASS_NAME_COLUMN = 3;
	private static final int COMMENTS_COLUMN_WITH_CLASS = 4;
	private static final int COMMENTS_COLUMN_NO_CLASS = 3;

	private ArrayList<ActivityModel> activitiesList;
	private boolean includeClassName;
	private final String[] colNamesWithClass = { " Client ID ", " Student Name ", " Date ", " Class Name ",
			" Comments " };
	private final String[] colNamesNoClass = { " Client ID ", " Student Name ", " Date ", " Comments " };
	private String[] colNames;

	public ActivityTableModel(ArrayList<ActivityModel> activities, boolean includeClassName) {
		this.activitiesList = activities;
		this.includeClassName = includeClassName;
		System.out.println("Num Activities (0): " + activitiesList.size());

		if (includeClassName)
			colNames = colNamesWithClass;
		else
			colNames = colNamesNoClass;
	}

	public void setData(ArrayList<ActivityModel> db) {
		if (includeClassName)
			colNames = colNamesWithClass;
		else
			colNames = colNamesNoClass;
		
		activitiesList.clear();
		activitiesList = db;
		System.out.println("Num Activities (1): " + activitiesList.size());
	}

	public void removeAll() {
		activitiesList.clear();
	}

	public int getNumStudents() {
		return activitiesList.size();
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
		return activitiesList.size();
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
		ActivityModel activities = activitiesList.get(row);

		if (colNames == colNamesWithClass) {
			switch (col) {
			case CLIENT_ID_COLUMN:
				return String.valueOf(activities.getClientID());
			case STUDENT_NAME_COLUMN:
				return activities.getStudentName();
			case SERVICE_DATE_COLUMN:
				return activities.getServiceDate().toString();
			case CLASS_NAME_COLUMN:
				return activities.getEventName();
			case COMMENTS_COLUMN_WITH_CLASS:
				return activities.getComments();
			}
		} else { // No class-name column
			switch (col) {
			case CLIENT_ID_COLUMN:
				return String.valueOf(activities.getClientID());
			case STUDENT_NAME_COLUMN:
				return activities.getStudentName();
			case SERVICE_DATE_COLUMN:
				return activities.getServiceDate().toString();
			case COMMENTS_COLUMN_NO_CLASS:
				return activities.getComments();
			}
		}
		return null;
	}

	public int getColumnForStudentName() {
		return STUDENT_NAME_COLUMN;
	}

	public int getColumnForClientID() {
		return CLIENT_ID_COLUMN;
	}

	public int getColumnForServiceDate() {
		return SERVICE_DATE_COLUMN;
	}

	public int getColumnForEventName() {
		return CLASS_NAME_COLUMN;
	}
}
