package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField.AbstractFormatter;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilCalendarModel;

public class DatePickerUtility extends JDialog implements ActionListener {

	String myDate;
	JDatePickerImpl datePicker;

	public DatePickerUtility() {
		setModal(true);
		
		// Add icon
		ImageIcon icon = new ImageIcon(getClass().getResource("PPicon24_Color_F16412.png"));
		setIconImage(icon.getImage());

		// Create date model & panel
		UtilCalendarModel dateModel = new UtilCalendarModel(Calendar.getInstance());
		Properties prop = new Properties();
		JDatePanelImpl datePanel;

		prop.put("text.today", "today");
		prop.put("text.month", "month");
		prop.put("text.year", "year");

		datePanel = new JDatePanelImpl(dateModel, prop);
		datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
		datePicker.setFont(CustomFonts.TABLE_TEXT_FONT);
		datePicker.addActionListener(this);

		add(datePicker);
		pack();
		
		// Configure dialog window
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Select import start date");
		setSize(300, 90);
		setVisible(true);
	}

	public String getDialogResponse() {
		return myDate;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// User selected date. Save date and dispose the dialog.
		myDate = datePicker.getJFormattedTextField().getText();
		setVisible(false);
		dispose();
	}

	// Nested class for formatting date
	public class DateLabelFormatter extends AbstractFormatter {
		// TODO: improve by using joda date/time
		private static final String datePattern = "yyyy-MM-dd";
		private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

		@Override
		public Object stringToValue(String text) throws ParseException {
			return dateFormatter.parseObject(text);
		}

		@Override
		public String valueToString(Object value) throws ParseException {
			if (value != null) {
				Calendar cal = (Calendar) value;
				return dateFormatter.format(cal.getTime());
			}
			return "";
		}
	}
}
