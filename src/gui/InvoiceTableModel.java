package gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import model.InvoiceModel;

public class InvoiceTableModel extends AbstractTableModel {

	public static final int INVOICE_DATE_COLUMN = 0;
	public static final int AMOUNT_COLUMN = 1;
	public static final int ITEM_NAME_COLUMN = 2;
	public static final int IS_CANCELED_COLUMN = 3;
	public static final int STUDENT_NAME_COLUMN = 4;
	public static final int PAYER_NAME_COLUMN = 5;
	public static final int START_DATE_COLUMN = 6;
	public static final int END_DATE_COLUMN = 7;
	public static final int PAYMENT_METHOD_COLUMN = 8;
	public static final int TRANSACTION_ID_COLUMN = 9;

	private ArrayList<InvoiceModel> invoiceList;
	private final String[] colNames = { " Date ", " Amount ", " Item Name ", "  ", " Student Name ",
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

	private Object getValueByColumn(InvoiceModel invoiceData, int col) {
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
		case IS_CANCELED_COLUMN:
			if (invoiceData.getIsCanceled())
				return "X";
			else
				return "";
		case START_DATE_COLUMN:
			if (invoiceData.getItemStartDate() == null)
				return "";
			else
				return invoiceData.getItemStartDate();
		case END_DATE_COLUMN:
			if (invoiceData.getItemEndDate() == null)
				return "";
			else
				return invoiceData.getItemEndDate();
		case PAYMENT_METHOD_COLUMN:
			return invoiceData.getPayMethod();
		case TRANSACTION_ID_COLUMN:
			return invoiceData.getTransactionID();
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		InvoiceModel invoiceData = invoiceList.get(row);
		return getValueByColumn(invoiceData, col);
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

	public ArrayList<InvoiceModel> getCsvDataList() {
		return invoiceList;
	}

	public String convertItemToCsv(Object item) {
		String csvString = "";
		InvoiceModel invoice = (InvoiceModel) item;

		for (int i = 0; i < colNames.length; i++) {
			if (i > 0)
				csvString += ",";
			csvString += getValueByColumn(invoice, i);
		}
		return csvString;
	}
}
