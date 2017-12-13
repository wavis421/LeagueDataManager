package gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import model.InvoiceModel;

public class InvoiceTableModel extends AbstractTableModel {

	public static final int INVOICE_DATE_COLUMN = 0;
	public static final int AMOUNT_COLUMN = 1;
	public static final int ITEM_NAME_COLUMN = 2;
	public static final int CLIENT_ID_COLUMN = 3;
	public static final int STUDENT_NAME_COLUMN = 4;
	public static final int PAYER_NAME_COLUMN = 5;
	public static final int START_DATE_COLUMN = 6;
	public static final int END_DATE_COLUMN = 7;
	public static final int PAYMENT_METHOD_COLUMN = 8;
	public static final int TRANSACTION_ID_COLUMN = 9;

	private ArrayList<InvoiceModel> invoiceList;
	private final String[] colNames = { " Date ", " Amount ", " Item Name ", " Client ID ", " Student Name ",
			" Payer Name ", " Start ", " End ", " Pay Method ", " Ext Transaction # " };

	public InvoiceTableModel(ArrayList<InvoiceModel> invoiceList) {
		this.invoiceList = invoiceList;
	}

	public void setData(ArrayList<InvoiceModel> db) {
		invoiceList = db;
	}

	public void removeAll() {
		invoiceList.clear();
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
		return invoiceList.size();
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
		InvoiceModel invoiceData = invoiceList.get(row);

		switch (col) {
		case INVOICE_DATE_COLUMN:
			return invoiceData.getInvoiceDate();
		case AMOUNT_COLUMN:
			return "$" + (invoiceData.getAmount() / 100);
		case ITEM_NAME_COLUMN:
			return invoiceData.getItemName();
		case STUDENT_NAME_COLUMN:
			return invoiceData.getStudentName();
		case PAYER_NAME_COLUMN:
			return invoiceData.getPayerName();
		case CLIENT_ID_COLUMN:
			return invoiceData.getClientID().toString();
		case START_DATE_COLUMN:
			return invoiceData.getItemStartDate();
		case END_DATE_COLUMN:
			return invoiceData.getItemEndDate();
		case PAYMENT_METHOD_COLUMN:
			return invoiceData.getPayMethod();
		case TRANSACTION_ID_COLUMN:
			return invoiceData.getTransactionID();
		}
		return null;
	}
}
