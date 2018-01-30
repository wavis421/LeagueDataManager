package controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.joda.time.DateTime;

import model.AttendanceEventModel;
import model.DateRangeEvent;
import model.InvoiceModel;
import model.LogDataModel;
import model.MySqlDatabase;
import model.SalesForceAttendanceModel;
import model.SalesForceStaffHoursModel;
import model.ScheduleModel;
import model.StudentImportModel;
import model.StudentModel;

public class Pike13Api {
	private final String USER_AGENT = "Mozilla/5.0";

	// Indices for client data
	private final int CLIENT_ID_IDX = 0;
	private final int FIRST_NAME_IDX = 1;
	private final int LAST_NAME_IDX = 2;
	private final int GITHUB_IDX = 3;
	private final int GRAD_YEAR_IDX = 4;
	private final int GENDER_IDX = 5;
	private final int HOME_LOC_IDX = 6;
	private final int FIRST_VISIT_IDX = 7;

	// Custom field names for client data
	private final String GENDER_NAME = "custom_field_106320";
	private final String GITHUB_NAME = "custom_field_127885";
	private final String GRAD_YEAR_NAME = "custom_field_145902";

	// Indices for enrollment data
	private final int FULL_NAME_IDX = 1;
	private final int SERVICE_DATE_IDX = 2;
	private final int EVENT_NAME_IDX = 3;

	// Indices for Sales Force enrollment data
	private final int SF_PERSON_ID_IDX = 0;
	private final int SF_SERVICE_DATE_IDX = 1;
	private final int SF_SERVICE_TIME_IDX = 2;
	private final int SF_EVENT_NAME_IDX = 3;
	private final int SF_SERVICE_NAME_IDX = 4;
	private final int SF_SERVICE_CATEGORY_IDX = 5;
	private final int SF_STATE_IDX = 6;
	private final int SF_VISIT_ID_IDX = 7;
	private final int SF_EVENT_OCCURRENCE_ID_IDX = 8;
	private final int SF_LOCATION_NAME_IDX = 9;
	private final int SF_INSTRUCTOR_NAMES_IDX = 10;

	// Indices for schedule data
	private final int SERVICE_DAY_IDX = 0;
	private final int SERVICE_TIME_IDX = 1;
	private final int DURATION_MINS_IDX = 2;
	private final int WKLY_EVENT_NAME_IDX = 3;

	// Indices for transaction data
	private final int PLAN_ID_IDX = 0;
	private final int PAYMENT_METHOD_IDX = 1;
	private final int TRANSACTION_ID_IDX = 2;

	// Indices for invoice data
	private final int INVOICE_ISSUED_DATE_IDX = 0;
	private final int INVOICE_GROSS_AMOUNT_IDX = 1;
	private final int INVOICE_PRODUCT_NAME_IDX = 2;
	private final int INVOICE_PLAN_ID_IDX = 3;
	private final int INVOICE_COUPON_CODE_IDX = 4;
	private final int INVOICE_RECIPIENT_NAME_IDX = 5;
	private final int INVOICE_PAYER_NAME_IDX = 6;

	// Indices for Person Plans data
	private final int PLAN_CLIENT_ID_IDX = 0;
	private final int PLAN_START_DATE_IDX = 1;
	private final int PLAN_END_DATE_IDX = 2;

	// Indices for Staff Hours data
	private final int STAFF_CLIENT_ID_IDX = 0;
	private final int STAFF_SERVICE_NAME_IDX = 1;
	private final int STAFF_SERVICE_DATE_IDX = 2;
	private final int STAFF_SERVICE_TIME_IDX = 3;
	private final int STAFF_DURATION_IDX = 4;
	private final int STAFF_LOCATION_IDX = 5;
	private final int STAFF_COMPLETED_COUNT_IDX = 6;
	private final int STAFF_NO_SHOW_COUNT_IDX = 7;
	private final int STAFF_CANCELED_COUNT_IDX = 8;
	private final int STAFF_EVENT_NAME_IDX = 9;
	private final int STAFF_SCHEDULE_ID_IDX = 10;
	private final int STAFF_FULL_NAME_IDX = 11;

	// TODO: Currently getting up to 500 fields; get multi pages if necessary
	private final String getClientData = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit and filters
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"person_id\",\"first_name\",\"last_name\",\"" + GITHUB_NAME + "\",\"" + GRAD_YEAR_NAME + "\","
			+ "            \"" + GENDER_NAME + "\",\"home_location_name\",\"first_visit_date\",\"future_visits\","
			+ "            \"completed_visits\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500},"
			// Filter on Dependents NULL and future/completed visits both > 0
			+ "\"filter\":[\"and\",[[\"emp\",\"dependent_names\"],"
			+ "                     [\"gt\",\"future_visits\",0],"
			+ "                     [\"gt\",\"completed_visits\",0]]]}}}";

	// Getting enrollment data is in 2 parts since page info gets inserted in middle.
	private final String getEnrollmentStudentTracker = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"person_id\",\"full_name\",\"service_date\",\"event_name\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500";

	private final String getEnrollmentStudentTracker2 = "},"
			// Filter on State completed and since date
			+ "\"filter\":[\"and\",[[\"eq\",\"state\",\"completed\"],"
			+ "           [\"btw\",\"service_date\",[\"0000-00-00\",\"1111-11-11\"]],"
			+ "           [\"starts\",\"service_category\",\"Class\"]]]}}}";

	private final String getEnrollmentStudentTracker2WithName = "},"
			// Filter on State completed, since date and student name
			+ "\"filter\":[\"and\",[[\"eq\",\"state\",\"completed\"],"
			+ "           [\"btw\",\"service_date\",[\"0000-00-00\",\"1111-11-11\"]],"
			+ "           [\"starts\",\"service_category\",\"Class\"],"
			+ "           [\"eq\",\"full_name\",\"NNNNNN\"]]]}}}";

	private final String getEnrollmentSalesForce = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"person_id\",\"service_date\",\"service_time\",\"event_name\",\"service_name\","
			+ "            \"service_category\",\"state\",\"visit_id\",\"event_occurrence_id\","
			+ "            \"service_location_name\",\"instructor_names\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500";

	private final String getEnrollmentSalesForce2 = "},"
			// Filter on since date
			+ "\"filter\":[\"btw\",\"service_date\",[\"0000-00-00\",\"1111-11-11\"]]}}}";

	// Get schedule data
	private final String getScheduleData = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit and filters
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"service_day\",\"service_time\",\"duration_in_minutes\",\"event_name\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500},"
			// Filter on 'this week' and 'starts with Class' and event name not null
			+ "\"filter\":[\"and\",[[\"btw\",\"service_date\",[\"0000-00-00\",\"1111-11-11\"]],"
			+ "                     [\"starts\",\"service_category\",\"Class\"],[\"nemp\",\"event_name\"]]]}}}";

	// Get invoice data
	private final String getInvoiceData = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit and filters
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"issued_date\",\"gross_amount\",\"product_name\",\"plan_id\",\"coupon_code\","
			+ "            \"recipient_names\",\"invoice_payer_name\"],"
			// Page limit max is 150
			+ "\"page\":{\"limit\":150},"
			// Filter on hard-coded month for now
			+ "\"filter\":[\"and\",[[\"btw\",\"issued_date\",[\"0000-00-00\",\"1111-11-11\"]],"
			+ "                     [\"eq\",\"revenue_category\",\"Courses\"]]]}}}";

	// Get transaction data
	private final String getTransactionData = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit and filters
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"plan_id\",\"payment_method\",\"processor_transaction_id\"],"
			// Page limit max is 150
			+ "\"page\":{\"limit\":150},"
			// Filter on hard-coded month for now
			+ "\"filter\":[\"btw\",\"transaction_date\",[\"0000-00-00\",\"1111-11-11\"]]}}}";

	// Get person plan data
	private final String getPersonPlanData = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit and filters
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"person_id\",\"start_date\",\"end_date\"],"
			// Page limit max is 10
			+ "\"page\":{\"limit\":10},"
			// Filter on plan_id which is filled in at run-time
			+ "\"filter\":[\"eq\",\"plan_id\",0]}}}";

	private final String getStaffHoursSalesForce = "{\"data\":{\"type\":\"queries\","
			// Get attributes: fields, page limit
			+ "\"attributes\":{"
			// Select fields
			+ "\"fields\":[\"person_id\",\"service_name\",\"service_date\",\"service_time\",\"duration_in_hours\","
			+ "            \"service_location_name\",\"completed_enrollment_count\",\"noshowed_enrollment_count\","
			+ "            \"late_canceled_enrollment_count\",\"event_name\",\"event_occurrence_id\",\"full_name\"],"
			// Page limit max is 500
			+ "\"page\":{\"limit\":500";

	private final String getStaffHoursSalesForce2 = "},"
			// Filter on since date
			+ "\"filter\":[\"and\",[[\"btw\",\"service_date\",[\"0000-00-00\",\"1111-11-11\"]],"
			+ "           [\"eq\",\"attendance_completed\",\"t\"],"
			+ "           [\"ne\",\"full_name\",\"Sub Teacher\"],[\"ne\",\"full_name\",\"League Admin\"]]]}}}";

	private MySqlDatabase mysqlDb;
	private String pike13Token;

	public Pike13Api(MySqlDatabase mysqlDb, String pike13Token) {
		this.mysqlDb = mysqlDb;
		this.pike13Token = pike13Token;
	}

	public ArrayList<StudentImportModel> getClients() {
		ArrayList<StudentImportModel> studentList = new ArrayList<StudentImportModel>();

		try {
			// Get URL connection with authorization
			HttpURLConnection conn = connectUrl("https://jtl.pike13.com/desk/api/v3/reports/clients/queries");

			// Send the query
			sendQueryToUrl(conn, getClientData);

			// Check result
			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				mysqlDb.insertLogData(LogDataModel.PIKE13_CONNECTION_ERROR, null, 0,
						" " + responseCode + ": " + conn.getResponseMessage());
				conn.disconnect();
				return studentList;
			}

			// Get input stream and read data
			JsonObject jsonObj = readInputStream(conn);
			if (jsonObj == null) {
				conn.disconnect();
				return studentList;
			}
			JsonArray jsonArray = jsonObj.getJsonArray("rows");

			for (int i = 0; i < jsonArray.size(); i++) {
				// Get fields for each person
				JsonArray personArray = (JsonArray) jsonArray.get(i);
				String firstName = stripQuotes(personArray.get(FIRST_NAME_IDX).toString());

				if (!firstName.startsWith("Guest")) {
					// Get fields for this Json array entry
					StudentImportModel model = new StudentImportModel(personArray.getInt(CLIENT_ID_IDX),
							stripQuotes(personArray.get(LAST_NAME_IDX).toString()),
							stripQuotes(personArray.get(FIRST_NAME_IDX).toString()),
							stripQuotes(personArray.get(GITHUB_IDX).toString()),
							stripQuotes(personArray.get(GENDER_IDX).toString()),
							stripQuotes(personArray.get(FIRST_VISIT_IDX).toString()),
							stripQuotes(personArray.get(HOME_LOC_IDX).toString()),
							stripQuotes(personArray.get(GRAD_YEAR_IDX).toString()));

					studentList.add(model);
				}
			}

			conn.disconnect();

		} catch (IOException e1) {
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, " for Client DB: " + e1.getMessage());
		}

		return studentList;
	}

	public ArrayList<AttendanceEventModel> getAttendance(String startDate) {
		// Insert start date and end date into enrollment command string
		String enroll2 = getEnrollmentStudentTracker2.replaceFirst("0000-00-00", startDate);
		enroll2 = enroll2.replaceFirst("1111-11-11", new DateTime().toString("yyyy-MM-dd"));

		// Get attendance for all students
		return getEnrollmentByCmdString(enroll2);
	}

	private ArrayList<AttendanceEventModel> getEnrollmentByCmdString(String cmdString) {
		ArrayList<AttendanceEventModel> eventList = new ArrayList<AttendanceEventModel>();
		boolean hasMore = false;
		String lastKey = "";

		try {
			do {
				// Get URL connection with authorization
				HttpURLConnection conn = connectUrl("https://jtl.pike13.com/desk/api/v3/reports/enrollments/queries");

				// Send the query; add page info if necessary
				if (hasMore)
					sendQueryToUrl(conn,
							getEnrollmentStudentTracker + ",\"starting_after\":\"" + lastKey + "\"" + cmdString);
				else
					sendQueryToUrl(conn, getEnrollmentStudentTracker + cmdString);

				// Check result
				int responseCode = conn.getResponseCode();
				if (responseCode != HttpURLConnection.HTTP_OK) {
					mysqlDb.insertLogData(LogDataModel.PIKE13_CONNECTION_ERROR, null, 0,
							" " + responseCode + ": " + conn.getResponseMessage());
					conn.disconnect();
					return eventList;
				}

				// Get input stream and read data
				JsonObject jsonObj = readInputStream(conn);
				if (jsonObj == null) {
					conn.disconnect();
					return eventList;
				}
				JsonArray jsonArray = jsonObj.getJsonArray("rows");

				for (int i = 0; i < jsonArray.size(); i++) {
					// Get fields for each event
					JsonArray eventArray = (JsonArray) jsonArray.get(i);
					String eventName = stripQuotes(eventArray.get(EVENT_NAME_IDX).toString());
					String serviceDate = stripQuotes(eventArray.get(SERVICE_DATE_IDX).toString());

					// Add event to list
					if (!eventName.equals("") && !eventName.equals("\"\"") && !serviceDate.equals("")) {
						eventList.add(new AttendanceEventModel(eventArray.getInt(CLIENT_ID_IDX),
								stripQuotes(eventArray.get(FULL_NAME_IDX).toString()), serviceDate, eventName));
					}
				}

				// Check to see if there are more pages
				hasMore = jsonObj.getBoolean("has_more");
				if (hasMore)
					lastKey = jsonObj.getString("last_key");

				conn.disconnect();

			} while (hasMore);

		} catch (IOException e1) {
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, " for Enrollment DB: " + e1.getMessage());
		}

		return eventList;
	}

	public ArrayList<SalesForceAttendanceModel> getSalesForceAttendance(String startDate, String endDate) {
		// Get attendance for export to Sales Force database
		ArrayList<SalesForceAttendanceModel> eventList = new ArrayList<SalesForceAttendanceModel>();
		boolean hasMore = false;
		String lastKey = "";

		// Insert start date and end date into enrollment command string
		String enroll2 = getEnrollmentSalesForce2.replaceFirst("0000-00-00", startDate);
		enroll2 = enroll2.replaceFirst("1111-11-11", endDate);

		try {
			do {
				// Get URL connection with authorization
				HttpURLConnection conn = connectUrl("https://jtl.pike13.com/desk/api/v3/reports/enrollments/queries");

				// Send the query; add page info if necessary
				if (hasMore)
					sendQueryToUrl(conn, getEnrollmentSalesForce + ",\"starting_after\":\"" + lastKey + "\"" + enroll2);
				else
					sendQueryToUrl(conn, getEnrollmentSalesForce + enroll2);

				// Check result
				int responseCode = conn.getResponseCode();
				if (responseCode != HttpURLConnection.HTTP_OK) {
					mysqlDb.insertLogData(LogDataModel.PIKE13_CONNECTION_ERROR, null, 0,
							" " + responseCode + ": " + conn.getResponseMessage());
					conn.disconnect();
					return eventList;
				}

				// Get input stream and read data
				JsonObject jsonObj = readInputStream(conn);
				if (jsonObj == null) {
					conn.disconnect();
					return eventList;
				}
				JsonArray jsonArray = jsonObj.getJsonArray("rows");

				for (int i = 0; i < jsonArray.size(); i++) {
					// Get fields for each event
					JsonArray eventArray = (JsonArray) jsonArray.get(i);

					// Add event to list
					eventList.add(new SalesForceAttendanceModel(eventArray.get(SF_PERSON_ID_IDX).toString(),
							stripQuotes(eventArray.get(SF_SERVICE_DATE_IDX).toString()),
							stripQuotes(eventArray.get(SF_SERVICE_TIME_IDX).toString()),
							stripQuotes(eventArray.get(SF_EVENT_NAME_IDX).toString()),
							stripQuotes(eventArray.get(SF_SERVICE_CATEGORY_IDX).toString()),
							stripQuotes(eventArray.get(SF_SERVICE_NAME_IDX).toString()),
							stripQuotes(eventArray.get(SF_STATE_IDX).toString()),
							eventArray.get(SF_VISIT_ID_IDX).toString(),
							eventArray.get(SF_EVENT_OCCURRENCE_ID_IDX).toString(),
							stripQuotes(eventArray.get(SF_LOCATION_NAME_IDX).toString()),
							stripQuotes(eventArray.get(SF_INSTRUCTOR_NAMES_IDX).toString())));
				}

				// Check to see if there are more pages
				hasMore = jsonObj.getBoolean("has_more");
				if (hasMore)
					lastKey = jsonObj.getString("last_key");

				conn.disconnect();

			} while (hasMore);

		} catch (IOException e1) {
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, " for Enrollment DB: " + e1.getMessage());
		}

		return eventList;
	}

	public ArrayList<AttendanceEventModel> getMissingAttendance(String endDate, ArrayList<StudentModel> studentList) {
		ArrayList<AttendanceEventModel> eventList = new ArrayList<AttendanceEventModel>();

		// Insert end date into enrollment command string
		String enroll2 = getEnrollmentStudentTracker2WithName.replaceFirst("1111-11-11", endDate);

		for (int i = 0; i < studentList.size(); i++) {
			StudentModel student = studentList.get(i);
			if (student.getStartDate() != null) {
				// Get student start date and ignore if date is beyond end date
				String catchupStartDate = student.getStartDate().toString();
				if (catchupStartDate.compareTo(endDate) >= 0)
					continue;

				// Catch up only as far back as 3 months ago
				String earliestDate = new DateTime().minusMonths(3).toString("yyyy-MM-dd");
				if (catchupStartDate.compareTo(earliestDate) < 0)
					catchupStartDate = earliestDate;

				// Get attendance for this student
				String enrollTemp = enroll2.replaceFirst("0000-00-00", catchupStartDate);
				enrollTemp = enrollTemp.replaceFirst("NNNNNN", student.getFirstName() + " " + student.getLastName());
				eventList.addAll(getEnrollmentByCmdString(enrollTemp));

				// Set student 'NewStudent' flag back to false
				mysqlDb.updateStudentFlags(student, "NewStudent", 0);
			}
		}

		return eventList;
	}

	public ArrayList<ScheduleModel> getSchedule(String startDate) {
		ArrayList<ScheduleModel> scheduleList = new ArrayList<ScheduleModel>();

		// Insert start date and end date into schedule command string.
		String scheduleString = getScheduleData.replaceFirst("0000-00-00", startDate);
		scheduleString = scheduleString.replaceFirst("1111-11-11", new DateTime().toString("yyyy-MM-dd"));

		try {
			// Get URL connection with authorization
			HttpURLConnection conn = connectUrl("https://jtl.pike13.com/desk/api/v3/reports/event_occurrences/queries");

			// Send the query
			sendQueryToUrl(conn, scheduleString);

			// Check result
			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				mysqlDb.insertLogData(LogDataModel.PIKE13_CONNECTION_ERROR, null, 0,
						" " + responseCode + ": " + conn.getResponseMessage());
				conn.disconnect();
				return scheduleList;
			}

			// Get input stream and read data
			JsonObject jsonObj = readInputStream(conn);
			if (jsonObj == null) {
				conn.disconnect();
				return scheduleList;
			}
			JsonArray jsonArray = jsonObj.getJsonArray("rows");

			for (int i = 0; i < jsonArray.size(); i++) {
				// Get fields for each event in the schedule
				JsonArray scheduleArray = (JsonArray) jsonArray.get(i);

				// Get event name, day-of-week and duration
				String eventName = stripQuotes(scheduleArray.get(WKLY_EVENT_NAME_IDX).toString());
				String serviceDayString = stripQuotes(scheduleArray.get(SERVICE_DAY_IDX).toString());
				int serviceDay = Integer.parseInt(serviceDayString);
				String startTime = stripQuotes(scheduleArray.get(SERVICE_TIME_IDX).toString());
				int duration = scheduleArray.getInt(DURATION_MINS_IDX);

				// Add event to list
				scheduleList.add(new ScheduleModel(0, serviceDay, startTime, duration, eventName));
			}

			conn.disconnect();

		} catch (IOException e1) {
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, " for Schedule DB: " + e1.getMessage());
		}

		return scheduleList;
	}

	public ArrayList<InvoiceModel> getInvoices(DateRangeEvent dateRange) {
		ArrayList<InvoiceModel> invoiceList = new ArrayList<InvoiceModel>();

		try {
			// Get URL connection with authorization
			HttpURLConnection conn = connectUrl("https://jtl.pike13.com/desk/api/v3/reports/invoice_items/queries");

			// Send the query
			String invoiceCmd = getInvoiceData.replaceFirst("0000-00-00",
					dateRange.getStartDate().toString("yyyy-MM-dd"));
			invoiceCmd = invoiceCmd.replaceFirst("1111-11-11", dateRange.getEndDate().toString("yyyy-MM-dd"));
			sendQueryToUrl(conn, invoiceCmd);

			// Check result
			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				mysqlDb.insertLogData(LogDataModel.PIKE13_CONNECTION_ERROR, null, 0,
						" " + responseCode + ": " + conn.getResponseMessage());
				conn.disconnect();
				return invoiceList;
			}

			// Get input stream and read data
			JsonObject jsonObj = readInputStream(conn);
			if (jsonObj == null) {
				conn.disconnect();
				return invoiceList;
			}
			JsonArray jsonArray = jsonObj.getJsonArray("rows");

			for (int i = 0; i < jsonArray.size(); i++) {
				// Get fields for each invoice in the list
				JsonArray invoiceArray = (JsonArray) jsonArray.get(i);

				// Get invoice date/amount, student name, product name
				int planID = invoiceArray.getInt(INVOICE_PLAN_ID_IDX);
				InvoiceModel model = new InvoiceModel(stripQuotes(invoiceArray.get(INVOICE_ISSUED_DATE_IDX).toString()),
						invoiceArray.getString(INVOICE_PRODUCT_NAME_IDX).toString(), "", "", 0,
						invoiceArray.getString(INVOICE_RECIPIENT_NAME_IDX),
						invoiceArray.getString(INVOICE_PAYER_NAME_IDX), planID, "", "",
						invoiceArray.getInt(INVOICE_GROSS_AMOUNT_IDX));

				JsonValue couponCode = invoiceArray.get(INVOICE_COUPON_CODE_IDX);
				if (couponCode != null)
					model.setPayMethod(stripQuotes(couponCode.toString()));

				// Fill in remaining person plan data: client ID, start/end date
				getPersonPlans(model, planID);

				// Add invoice to list
				invoiceList.add(model);
			}
			conn.disconnect();

			// Fill in payment method and transaction ID
			getPaymentInfo(invoiceList, dateRange);

			// Now clear out all start/end date fields except the final one
			for (int i = invoiceList.size() - 1; i >= 0; i--) {
				InvoiceModel invoice = invoiceList.get(i);
				if (invoice.getItemStartDate() == null)
					continue;

				int clientID = invoice.getClientID();
				for (int j = i - 1; j >= 0; j--) {
					if (invoiceList.get(j).getClientID() == clientID) {
						invoiceList.get(j).setItemStartDate(null);
						invoiceList.get(j).setItemEndDate(null);
					}
				}
			}

		} catch (IOException e1) {
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, " for Invoice DB: " + e1.getMessage());
		}

		return invoiceList;
	}

	private void getPaymentInfo(ArrayList<InvoiceModel> invoiceList, DateRangeEvent dateRange) {
		try {
			// Get URL connection with authorization
			HttpURLConnection conn = connectUrl(
					"https://jtl.pike13.com/desk/api/v3/reports/invoice_item_transactions/queries");

			// Send the query
			String transCmd = getTransactionData.replaceFirst("0000-00-00",
					dateRange.getStartDate().toString("yyyy-MM-dd"));
			transCmd = transCmd.replaceFirst("1111-11-11", dateRange.getEndDate().toString("yyyy-MM-dd"));
			sendQueryToUrl(conn, transCmd);

			// Check result
			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				mysqlDb.insertLogData(LogDataModel.PIKE13_CONNECTION_ERROR, null, 0,
						" " + responseCode + ": " + conn.getResponseMessage());
				conn.disconnect();
				return;
			}

			// Get input stream and read data
			JsonObject jsonObj = readInputStream(conn);
			if (jsonObj == null) {
				conn.disconnect();
				return;
			}
			JsonArray jsonArray = jsonObj.getJsonArray("rows");

			for (int i = 0; i < jsonArray.size(); i++) {
				// Get fields for each invoice in the list
				JsonArray transactionArray = (JsonArray) jsonArray.get(i);

				for (int j = 0; j < invoiceList.size(); j++) {
					InvoiceModel invoice = invoiceList.get(j);
					if (transactionArray.getInt(PLAN_ID_IDX) == invoice.getPlanID()) {
						// Plan ID match, update transaction ID and payment method
						invoice.setPayMethod(stripQuotes(transactionArray.get(PAYMENT_METHOD_IDX).toString()));
						invoice.setTransactionID(transactionArray.getString(TRANSACTION_ID_IDX).toString());
						break;
					}
				}
			}
			conn.disconnect();

		} catch (IOException e1) {
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, " for Transaction DB: " + e1.getMessage());
		}
	}

	private void getPersonPlans(InvoiceModel invoice, Integer planID) {
		try {
			// Get URL connection with authorization
			HttpURLConnection conn = connectUrl("https://jtl.pike13.com/desk/api/v3/reports/person_plans/queries");

			// Fill in plan_id field and send the query
			String planString = getPersonPlanData.replace("\"plan_id\",0", "\"plan_id\"," + planID.toString());
			sendQueryToUrl(conn, planString);

			// Check result
			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				mysqlDb.insertLogData(LogDataModel.PIKE13_CONNECTION_ERROR, null, 0,
						" " + responseCode + ": " + conn.getResponseMessage());
				conn.disconnect();
				return;
			}

			// Get input stream and read data
			JsonObject jsonObj = readInputStream(conn);
			if (jsonObj == null) {
				conn.disconnect();
				return;
			}
			JsonArray jsonArray = jsonObj.getJsonArray("rows");

			for (int i = 0; i < jsonArray.size(); i++) {
				// Get fields for each plan in the list
				JsonArray invoiceArray = (JsonArray) jsonArray.get(i);

				// Add person plans fields to invoice model
				invoice.setClientID(invoiceArray.getInt(PLAN_CLIENT_ID_IDX));
				invoice.setItemStartDate(stripQuotes(invoiceArray.get(PLAN_START_DATE_IDX).toString()));
				invoice.setItemEndDate(stripQuotes(invoiceArray.get(PLAN_END_DATE_IDX).toString()));
			}
			conn.disconnect();

		} catch (IOException e1) {
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, " for Invoice DB: " + e1.getMessage());
		}
	}

	public ArrayList<SalesForceStaffHoursModel> getSalesForceStaffHours(String startDate, String endDate) {
		// Get staff hours for export to Sales Force database
		ArrayList<SalesForceStaffHoursModel> eventList = new ArrayList<SalesForceStaffHoursModel>();
		boolean hasMore = false;
		String lastKey = "";

		// Insert start date and end date into staff hours command string
		String staffHours2 = getStaffHoursSalesForce2.replaceFirst("0000-00-00", startDate);
		staffHours2 = staffHours2.replaceFirst("1111-11-11", endDate);

		try {
			do {
				// Get URL connection with authorization
				HttpURLConnection conn = connectUrl("https://jtl.pike13.com/desk/api/v3/reports/event_occurrence_staff_members/queries");

				// Send the query; add page info if necessary
				if (hasMore)
					sendQueryToUrl(conn, getStaffHoursSalesForce + ",\"starting_after\":\"" + lastKey + "\"" + staffHours2);
				else
					sendQueryToUrl(conn, getStaffHoursSalesForce + staffHours2);

				// Check result
				int responseCode = conn.getResponseCode();
				if (responseCode != HttpURLConnection.HTTP_OK) {
					mysqlDb.insertLogData(LogDataModel.PIKE13_CONNECTION_ERROR, null, 0,
							" " + responseCode + ": " + conn.getResponseMessage());
					conn.disconnect();
					return eventList;
				}

				// Get input stream and read data
				JsonObject jsonObj = readInputStream(conn);
				if (jsonObj == null) {
					conn.disconnect();
					return eventList;
				}
				JsonArray jsonArray = jsonObj.getJsonArray("rows");

				for (int i = 0; i < jsonArray.size(); i++) {
					// Get fields for each event
					JsonArray eventArray = (JsonArray) jsonArray.get(i);

					// Add event to list
					eventList.add(new SalesForceStaffHoursModel(eventArray.get(STAFF_CLIENT_ID_IDX).toString(),
							eventArray.getString(STAFF_FULL_NAME_IDX),
							eventArray.getString(STAFF_SERVICE_NAME_IDX),
							eventArray.getString(STAFF_SERVICE_DATE_IDX),
							eventArray.getString(STAFF_SERVICE_TIME_IDX),
							eventArray.getJsonNumber(STAFF_DURATION_IDX).doubleValue(),
							eventArray.getString(STAFF_LOCATION_IDX),
							eventArray.getJsonNumber(STAFF_COMPLETED_COUNT_IDX).doubleValue(),
							eventArray.getJsonNumber(STAFF_NO_SHOW_COUNT_IDX).doubleValue(),
							eventArray.getJsonNumber(STAFF_CANCELED_COUNT_IDX).doubleValue(),
							eventArray.getString(STAFF_EVENT_NAME_IDX),
							eventArray.get(STAFF_SCHEDULE_ID_IDX).toString()));
				}

				// Check to see if there are more pages
				hasMore = jsonObj.getBoolean("has_more");
				if (hasMore)
					lastKey = jsonObj.getString("last_key");

				conn.disconnect();

			} while (hasMore);

		} catch (IOException e1) {
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, " for Staff Hours DB: " + e1.getMessage());
		}

		return eventList;
	}

	private HttpURLConnection connectUrl(String queryUrl) {
		try {
			// Get URL connection with authorization
			URL url = new URL(queryUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			String basicAuth = "Bearer " + pike13Token;
			conn.setRequestProperty("Authorization", basicAuth);
			conn.setRequestProperty("User-Agent", USER_AGENT);

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-type", "application/vnd.api+json; charset=UTF-8");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			return conn;

		} catch (IOException e) {
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, ": " + e.getMessage());
		}
		return null;
	}

	private void sendQueryToUrl(HttpURLConnection conn, String getCommand) {
		try {
			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(getCommand.getBytes("UTF-8"));
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, ": " + e.getMessage());
		}
	}

	private JsonObject readInputStream(HttpURLConnection conn) {
		try {
			// Get input stream and read data
			InputStream inputStream = conn.getInputStream();
			JsonReader repoReader = Json.createReader(inputStream);
			JsonObject object = ((JsonObject) repoReader.read()).getJsonObject("data").getJsonObject("attributes");

			repoReader.close();
			inputStream.close();
			return object;

		} catch (IOException e) {
			mysqlDb.insertLogData(LogDataModel.PIKE13_IMPORT_ERROR, null, 0, ": " + e.getMessage());
		}
		return null;
	}

	private String stripQuotes(String fieldData) {
		// Strip off quotes around field string
		if (fieldData.equals("\"\"") || fieldData.startsWith("null"))
			return "";
		else
			return fieldData.substring(1, fieldData.length() - 1);
	}
}
