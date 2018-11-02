package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * TableHeaderBox
 * 
 * This class formats the main table header. When displaying the Student TA
 * table, combo & text boxes are added for filtering purposes.
 * 
 */

public class TableHeaderBox implements ActionListener {
	// Header states
	public static final int UNINIT = 0;
	public static final int STANDARD = 1;
	public static final int EXTRA = 2;

	private static TableListeners filterListener;

	// Panel containing the header label plus filter components
	private static JPanel hdrPanel;
	private static int hdrState = UNINIT;

	// Header and filter labels
	private static JLabel hdrLabel;
	private static JLabel leftLabel;
	private static JLabel clearLabel;
	private static JLabel classesLabel;
	private static JLabel lvlLabel;
	private static JLabel ageLabel;

	// Level/age data and the filter text/combo boxes
	private static Integer[] lvlArray = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	private static Integer[] ageArray = { 10, 11, 12, 13, 14, 15, 16, 17 };
	private static JTextField classesField;
	private static JComboBox<Integer> lvlBox;
	private static JComboBox<Integer> ageBox;
	private static JRadioButton clearButton;

	public TableHeaderBox(JLabel lbl) {
		hdrLabel = lbl;
		hdrPanel = new JPanel(new BorderLayout());

		// Initialize all labels
		leftLabel = new JLabel(" ");
		clearLabel = new JLabel("Clear filters: ");
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

		// Configure sizes
		classesField.setPreferredSize(new Dimension(60, classesField.getPreferredSize().height));
		classesField.setMaximumSize(new Dimension(60, classesField.getPreferredSize().height));
		ageBox.setPreferredSize(new Dimension(60, ageBox.getPreferredSize().height));
		ageBox.setMaximumSize(new Dimension(60, ageBox.getPreferredSize().height));
		lvlBox.setPreferredSize(new Dimension(60, lvlBox.getPreferredSize().height));
		lvlBox.setMaximumSize(new Dimension(60, lvlBox.getPreferredSize().height));

		// Border and colors
		classesField.setBorder(BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true));
		ageBox.setBorder(BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true));
		lvlBox.setBorder(BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true));

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
		hdrBox.add(leftLabel, BorderLayout.WEST);
		hdrBox.add(Box.createGlue());
		hdrBox.add(hdrLabel, BorderLayout.CENTER);
		hdrBox.add(Box.createGlue());

		if (hdrState == EXTRA) {
			// These components only added when extra filters requested
			hdrBox.add(clearLabel);
			hdrBox.add(clearButton);
			hdrBox.add(classesLabel);
			hdrBox.add(classesField);
			hdrBox.add(ageLabel);
			hdrBox.add(ageBox);
			hdrBox.add(lvlLabel);
			hdrBox.add(lvlBox);
		}
		hdrPanel.add(hdrBox);
		return hdrPanel;
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
		if (event.getSource() == clearButton) {
			// Clear all filter fields
			classesField.setText("0");
			ageBox.setSelectedIndex(0);
			lvlBox.setSelectedIndex(0);

			clearButton.setSelected(true);

		} else if (!classesField.getText().matches("\\d+")) {
			// Make sure classes field is an Integer!!
			classesField.setText("0");
		}

		// Now force a data update based on filter values
		filterListener.viewActiveTAs();
	}
}
