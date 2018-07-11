package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.jdatepicker.impl.JDatePickerImpl;
import org.joda.time.DateTime;

import model.DateRangeEvent;

public class SelectDateRangeDialog extends JDialog {
	private static final int DIALOG_WIDTH = 300;
	private static final int DIALOG_HEIGHT = 190;

	// Private instance variables
	private DateRangeEvent dateRangeEvent;
	private JDatePickerImpl startDatePicker;
	private JDatePickerImpl endDatePicker;

	public SelectDateRangeDialog(JFrame parentFrame) {
		setModal(true);

		// Default start/end date is for last month
		DateTime startDate = new DateTime();
		startDate = startDate.minusMonths(1);
		startDate = startDate.minusDays(startDate.getDayOfMonth() - 1);
		DateTime endDate = startDate.dayOfMonth().withMaximumValue();

		// Create panels, labels and buttons
		JPanel startDatePanel = new JPanel();
		JLabel startDateLabel = new JLabel("Start date: ");
		JPanel endDatePanel = new JPanel();
		JLabel endDateLabel = new JLabel("End date: ");
		JPanel dateSelectPanel = new JPanel();

		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");

		// Set layouts
		startDatePanel.setLayout(new FlowLayout());
		endDatePanel.setLayout(new FlowLayout());
		dateSelectPanel.setLayout(new BorderLayout());
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));

		// Make OK & cancel buttons the same size
		Dimension btnSize = cancelButton.getPreferredSize();
		okButton.setPreferredSize(btnSize);

		// Add components to panels
		startDatePicker = new DatePicker(startDate).getDatePicker();
		startDatePanel.add(startDateLabel);
		startDatePanel.add(startDatePicker);
		endDatePicker = new DatePicker(endDate).getDatePicker();
		endDatePanel.add(endDateLabel);
		endDatePanel.add(endDatePicker);

		dateSelectPanel.add(startDatePanel, BorderLayout.NORTH);
		dateSelectPanel.add(endDatePanel, BorderLayout.CENTER);

		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);

		// Add button listeners
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dateRangeEvent = new DateRangeEvent(this,
						new DateTime(startDatePicker.getJFormattedTextField().getText()),
						new DateTime(endDatePicker.getJFormattedTextField().getText()));
				setVisible(false);
				dispose();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dateRangeEvent = null;
				setVisible(false);
				dispose();
			}
		});

		// Set borders
		Border innerBorder = BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true);
		Border outerBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		dateSelectPanel.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));

		// Add panels to dialog
		add(dateSelectPanel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.CENTER);

		// Set icon
		ImageIcon icon = new ImageIcon(getClass().getResource("PPicon24_Color_F16412.png"));
		setIconImage(icon.getImage());

		// Configure dialog window
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Select date range");
		setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		setLocation(parentFrame.getX() + 100, parentFrame.getY() + 100);
		setVisible(true);
	}

	public DateRangeEvent getDialogResponse() {
		return dateRangeEvent;
	}
}
