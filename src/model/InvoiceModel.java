package model;

public class InvoiceModel implements Comparable<InvoiceModel> {

	private String transactionID, invoiceDate, itemName, itemStartDate, itemEndDate, payMethod;
	String studentName, payerName;
	private Integer amount, clientID;

	public InvoiceModel(String invoiceDate, String itemName, String itemStartDate, String itemEndDate, int clientID,
			String studentName, String payerName, String payMethod, String transactionID, int amount) {
		this.invoiceDate = invoiceDate;
		this.itemName = itemName;
		this.itemStartDate = itemStartDate;
		this.itemEndDate = itemEndDate;

		this.clientID = clientID;
		this.studentName = studentName;
		this.payerName = payerName;

		this.payMethod = payMethod;
		this.transactionID = transactionID;
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

	public void setItemStartDate(String itemStartDate) {
		this.itemStartDate = itemStartDate;
	}

	public String getItemEndDate() {
		return itemEndDate;
	}

	public void setItemEndDate(String itemEndDate) {
		this.itemEndDate = itemEndDate;
	}

	public Integer getClientID() {
		return clientID;
	}

	public void setClientID(Integer clientID) {
		this.clientID = clientID;
	}

	public String getStudentName() {
		return studentName;
	}

	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}

	public String getPayerName() {
		if (payerName.equals(studentName))
			return "";
		else
			return payerName;
	}

	public String getPayMethod() {
		return payMethod;
	}

	public void setPayMethod(String payMethod) {
		this.payMethod = payMethod;
	}

	public String getTransactionID() {
		return transactionID;
	}

	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}

	public Integer getAmount() {
		return amount;
	}

	@Override
	public int compareTo(InvoiceModel other) {
		// Compare by invoice date, then student name
		int comp = this.invoiceDate.compareTo(other.invoiceDate);
		if (comp != 0)
			return comp;
		else
			return this.studentName.compareTo(other.studentName);
	}
}
