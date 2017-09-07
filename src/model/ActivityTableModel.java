package model;

import java.sql.Date;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class ActivityTableModel extends AbstractTableModel {
	private static final int CLIENT_ID_COLUMN = 0;
	private static final int STUDENT_NAME_COLUMN = 1;
	private static final int SERVICE_DATE_COLUMN = 2;
	private static final int EVENT_NAME_COLUMN = 3;
	private static final int COMMENTS_COLUMN = 4;

	private ArrayList<ActivityModel> activitiesList;
	private String colNames[] = { " Client ID ", " Student Name ", " Date ", " Event ", " Comments " };

	public ActivityTableModel(ArrayList<ActivityModel> activities) {
		this.activitiesList = activities;
		System.out.println("Num Activities (0): " + activitiesList.size());
	}

	public void setData(ArrayList<ActivityModel> db) {
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
		return String.class;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	@Override
	public Object getValueAt(int row, int col) {
		ActivityModel activities = activitiesList.get(row);

		switch (col) {
		case CLIENT_ID_COLUMN:
			return String.valueOf(activities.getClientID());
		case STUDENT_NAME_COLUMN:
			return activities.getStudentName();
		case SERVICE_DATE_COLUMN:
			return activities.getServiceDate().toString();
		case EVENT_NAME_COLUMN:
			return activities.getEventName();
		case COMMENTS_COLUMN:
			return activities.getComments();
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
}
