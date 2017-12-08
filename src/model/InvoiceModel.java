package model;

public class InvoiceModel {

	private String invoiceNum, invoiceDate, itemName, itemStartDate, itemEndDate, studentName, payMethod;
	private Integer amount, clientID;

	public InvoiceModel(String invoiceNum, String invoiceDate, String itemName, String itemStartDate,
			String itemEndDate, String studentName, int clientID, String payMethod, int amount) {
		this.invoiceNum = invoiceNum;
		this.invoiceDate = invoiceDate;
		this.itemName = itemName;
		this.itemStartDate = itemStartDate;
		this.itemEndDate = itemEndDate;
		this.studentName = studentName;
		this.clientID = clientID;
		this.payMethod = payMethod;
		this.amount = amount;
	}

	public String getInvoiceDate() {
		return invoiceDate;
	}

	public String getItemName() {
		return itemName;
	}

	public String getItemStartDate() {
		return itemStartDate;
	}

	public String getItemEndDate() {
		return itemEndDate;
	}

	public String getStudentName() {
		return studentName;
	}

	public String getPayMethod() {
		return payMethod;
	}

	public Integer getAmount() {
		return amount;
	}

	public String getInvoiceNum() {
		return invoiceNum;
	}

	public Integer getClientID() {
		return clientID;
	}

	public void setClientID(Integer clientID) {
		this.clientID = clientID;
	}

	public void setItemStartDate(String itemStartDate) {
		this.itemStartDate = itemStartDate;
	}

	public void setItemEndDate(String itemEndDate) {
		this.itemEndDate = itemEndDate;
	}

	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}
}
