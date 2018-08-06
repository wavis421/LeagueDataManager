package gui;

import java.awt.Dimension;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Properties;

import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilCalendarModel;
import org.joda.time.DateTime;

public class DatePicker {

	JDatePickerImpl datePicker;

	public DatePicker(DateTime date) {
		// Create date model & panel
		Calendar myDate = Calendar.getInstance();
		myDate.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
		myDate.set(Calendar.MONTH, date.getMonthOfYear() - 1);
		myDate.set(Calendar.YEAR, date.getYear());

		UtilCalendarModel dateModel = new UtilCalendarModel(myDate);
		Properties prop = new Properties();
		JDatePanelImpl datePanel;

		prop.put("text.today", date.getDayOfMonth());
		prop.put("text.month", date.getMonthOfYear() - 1);
		prop.put("text.year", date.getYear());

		datePanel = new JDatePanelImpl(dateModel, prop);
		datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
		datePicker.setPreferredSize(new Dimension(150, 32));
		
		JFormattedTextField textField = datePicker.getJFormattedTextField();
		textField.setFont(CustomFonts.TABLE_TEXT_FONT);
		textField.setHorizontalAlignment(JTextField.CENTER);
	}

	public JDatePickerImpl getDatePicker() {
		return datePicker;
	}

	// Nested class for formatting date picker
	private class DateLabelFormatter extends AbstractFormatter {
		private static final String datePattern = "yyyy-MM-dd";

		@Override
		public String stringToValue(String text) throws ParseException {
			DateTime date = new DateTime(text);
			return date.toString(datePattern);
		}

		@Override
		public String valueToString(Object value) throws ParseException {
			if (value != null) {
				DateTime date = new DateTime(value);
				return date.toString(datePattern);
			}
			return "";
		}
	}
}
