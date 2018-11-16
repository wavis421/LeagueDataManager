package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * TableHeaderBox
 * 
 * This class formats the main table header:
 * 
 * => When displaying the Student TA table, combo & text boxes are added for
 * filtering purposes.
 * 
 * => For the Detailed Schedule table, a list of checkboxes is added to filter
 * on day-of-week.
 * 
 */

public class TableHeaderBox implements ActionListener {
	// Header states
	public static final int HDR_UNINIT = 0;
	public static final int HDR_EMPTY = 1;
	public static final int HDR_STUDENT_TA = 2;
	public static final int HDR_CLASS_DETAILS = 3;

	private static TableListeners filterListener;

	// Panel containing the header label plus filter components
	private static JPanel hdrPanel;
	private static int hdrState = HDR_UNINIT;

	// Header and filter labels
	private static JLabel hdrLabel;
	private static JLabel blankLabel;
	private static JLabel clearLabel;
	private static JLabel classesLabel;
	private static JLabel lvlLabel;
	private static JLabel ageLabel;
	private static JLabel resetLabel;

	// Level/age data and the filter text/combo boxes
	private static Integer[] lvlArray = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	private static Integer[] ageArray = { 10, 11, 12, 13, 14, 15, 16, 17 };
	private static JCheckBoxMenuItem[] dowCBoxArray = { new JCheckBoxMenuItem("Sunday", false),
			new JCheckBoxMenuItem("Monday", false), new JCheckBoxMenuItem("Tuesday", false),
			new JCheckBoxMenuItem("Wednesday", false), new JCheckBoxMenuItem("Thursday", false),
			new JCheckBoxMenuItem("Friday", false), new JCheckBoxMenuItem("Saturday", false) };

	private static JTextField classesField;
	private static JComboBox<Integer> lvlBox;
	private static JComboBox<Integer> ageBox;
	private static JRadioButton clearButton;
	private static JMenuBar dowMenuBar;
	private static JMenu dowMenu;

	public TableHeaderBox(JLabel lbl) {
		hdrLabel = lbl;
		hdrPanel = new JPanel(new BorderLayout());

		// Initialize all labels
		blankLabel = new JLabel("        ");
		clearLabel = new JLabel("Clear filters: ");
		resetLabel = new JLabel("Reset DOW filter: ");
		classesLabel = new JLabel("      Min # Classes:  ");
		ageLabel = new JLabel("   Min Age:  ");
		lvlLabel = new JLabel("   Min Level:  ");

		// Initialize all text/combo boxes
		clearButton = new JRadioButton();
		clearButton.setSelected(true);
		classesField = new JTextField(3);
		classesField.setText("0");
		ageBox = new JComboBox<Integer>(ageArray);
		lvlBox = new JComboBox<Integer>(lvlArray);

		dowMenuBar = new JMenuBar();
		dowMenu = new JMenu("Day-of-Week Filter  ");
		dowMenuBar.add(dowMenu);
		for (int i = 0; i < dowCBoxArray.length; i++) {
			dowMenu.add(dowCBoxArray[i]);
			dowCBoxArray[i].addActionListener(this);
		}

		// Configure sizes
		classesField.setPreferredSize(new Dimension(65, classesField.getPreferredSize().height));
		classesField.setMaximumSize(new Dimension(65, classesField.getPreferredSize().height));
		ageBox.setPreferredSize(new Dimension(70, ageBox.getPreferredSize().height));
		ageBox.setMaximumSize(new Dimension(70, ageBox.getPreferredSize().height));
		lvlBox.setPreferredSize(new Dimension(65, lvlBox.getPreferredSize().height));
		lvlBox.setMaximumSize(new Dimension(65, lvlBox.getPreferredSize().height));
		dowMenuBar.setPreferredSize(new Dimension(160, 40));
		dowMenuBar.setMaximumSize(new Dimension(160, 40));

		// Border and colors
		classesField.setBorder(BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true));
		ageBox.setBorder(BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true));
		lvlBox.setBorder(BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true));
		dowMenuBar.setBorder(BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true));

		// Create listeners for clear button, # classes, aga & level boxes
		clearButton.addActionListener(this);
		classesField.addActionListener(this);
		ageBox.addActionListener(this);
		lvlBox.addActionListener(this);
	}

	public static JPanel refreshHeader(int state) {
		// Fix classes field if it contains garbage!!
		if (!classesField.getText().matches("\\d+"))
			classesField.setText("0");

		// Nothing to do if state unchanged
		if (hdrState == state)
			return hdrPanel;
		hdrState = state;

		// Remove existing components from header panel
		hdrPanel.removeAll();

		// Create horizontal glue box to align left/center/right
		Box hdrBox = Box.createHorizontalBox();
		hdrBox.add(blankLabel, BorderLayout.WEST);
		hdrBox.add(Box.createGlue());
		hdrBox.add(hdrLabel, BorderLayout.CENTER);
		hdrBox.add(Box.createGlue());

		// These components only added when extra filters requested
		if (hdrState == HDR_STUDENT_TA) {
			hdrBox.add(clearLabel);
			hdrBox.add(clearButton);
			hdrBox.add(classesLabel);
			hdrBox.add(classesField);
			hdrBox.add(ageLabel);
			hdrBox.add(ageBox);
			hdrBox.add(lvlLabel);
			hdrBox.add(lvlBox);

		} else if (hdrState == HDR_CLASS_DETAILS) {
			hdrBox.add(resetLabel);
			hdrBox.add(clearButton);
			hdrBox.add(blankLabel);
			hdrBox.add(dowMenuBar);
		}
		hdrPanel.add(hdrBox);
		return hdrPanel;
	}

	public static void clearFilters() {
		// Clear all filter fields
		classesField.setText("0");
		ageBox.setSelectedIndex(0);
		lvlBox.setSelectedIndex(0);

		for (int i = 0; i < dowCBoxArray.length; i++)
			dowCBoxArray[i].setSelected(false);
	}

	public static boolean[] getDowSelectList() {
		boolean[] dowValues = new boolean[dowCBoxArray.length];

		for (int i = 0; i < dowCBoxArray.length; i++)
			dowValues[i] = dowCBoxArray[i].isSelected();

		return dowValues;
	}

	public static void setTableListener(TableListeners listener) {
		filterListener = listener;
	}

	public static String getMinClasses() {
		return classesField.getText();
	}

	public static int getMinAge() {
		return (int) ageBox.getSelectedItem();
	}

	public static int getMinLevel() {
		return (int) lvlBox.getSelectedItem();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (hdrState == HDR_STUDENT_TA) {
			if (event.getSource() == clearButton) {
				// Clear all filter fields
				clearFilters();
				clearButton.setSelected(true);

			} else if (!classesField.getText().matches("\\d+")) {
				// Make sure classes field is an Integer!!
				classesField.setText("0");
			}

			// Now force a data update based on filter values
			filterListener.viewActiveTAs();

		} else if (hdrState == HDR_CLASS_DETAILS) {
			if (event.getSource() == clearButton) {
				// Reset all filter fields
				clearFilters();
				clearButton.setSelected(true);
			}

			// Now force a data update based on the filter values
			filterListener.viewClassDetails(getDowSelectList());
		}
	}
}
