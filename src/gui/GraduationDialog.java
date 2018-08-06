package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jdatepicker.impl.JDatePickerImpl;
import org.joda.time.DateTime;

import controller.Controller;
import model.AttendanceModel;
import model.GraduationModel;

public class GraduationDialog extends JDialog implements ActionListener {
	private final static int FRAME_WIDTH = 400;
	private final static int FRAME_HEIGHT = 390;

	// Main panel heights
	private final static int CONTROL_PANEL_HEIGHT = 110;
	private final static int TABLE_PANEL_HEIGHT = 150;
	private final static int BUTTON_PANEL_HEIGHT = 80;

	// Control sub-panel height
	private final static int CONTROL_SUB_PANEL_HEIGHT = 40;

	// Panel widths
	private final static int PANEL_WIDTH = FRAME_WIDTH - 10;

	// Table width/height
	private final static int GRAD_TABLE_WIDTH = PANEL_WIDTH - 20;
	private final static int GRAD_TABLE_HEIGHT = TABLE_PANEL_HEIGHT - 10;

	// GUI components
	private JComboBox<String> gradLevelList;
	private JDatePickerImpl gradDatePicker;
	private JTable gradTable;
	private GradTableModel gradTableModel;
	private JButton submitButton;
	private JButton exitButton;
	private JLabel errorField;
	private static String[] gradLevels = new String[10];
	private static Controller controller;

	public GraduationDialog(Controller newController, int clientID, String studentName) {
		// Graduate by student: create list with 1 student
		ArrayList<DialogGradModel> gradList = new ArrayList<DialogGradModel>();
		gradList.add(new DialogGradModel(clientID, studentName));

		controller = newController;
		createGui(0, gradList, "Graduate " + studentName);
	}

	public GraduationDialog(Controller newController, int clientID, String studentName, String className) {
		// Graduate by student: create list with 1 student
		ArrayList<DialogGradModel> gradList = new ArrayList<DialogGradModel>();
		gradList.add(new DialogGradModel(clientID, studentName));

		controller = newController;
		int gradLevelNum = getLevelFromClassName(className);
		createGui(gradLevelNum, gradList, "Graduate " + studentName);
	}

	public GraduationDialog(Controller newController, String gradClassName, ArrayList<AttendanceModel> attendanceList) {
		// Graduate by class
		int gradLevelNum = getLevelFromClassName(gradClassName);

		// Create Grad List
		ArrayList<DialogGradModel> gradList = new ArrayList<DialogGradModel>();
		for (AttendanceModel a : attendanceList)
			gradList.add(new DialogGradModel(a.getClientID(), a.getStudentName().toString()));

		controller = newController;
		createGui(gradLevelNum, gradList, "Graduate class '" + gradClassName + "'");
	}

	private void createGui(int gradLevelNum, ArrayList<DialogGradModel> gradList, String dialogTitle) {
		setModal(true);
		createGradLevelList();

		// Create top level panels
		JPanel controlPanel = new JPanel();
		JPanel tablePanel = new JPanel();
		JPanel buttonPanel = new JPanel();

		// Create sub-panels inside of control panel
		JPanel levelPanel = new JPanel();
		JPanel gradDatePanel = new JPanel();

		// Create sub-panels inside of button panel
		JPanel errorPanel = new JPanel();
		JPanel okCancelPanel = new JPanel();

		// Create labels and right justify
		JLabel levelLabel = new JLabel("Level passed: ");
		JLabel gradDateLabel = new JLabel("Graduation date: ");

		levelLabel.setHorizontalAlignment(JLabel.RIGHT);
		gradDateLabel.setHorizontalAlignment(JLabel.RIGHT);

		// Create input fields
		gradLevelList = new JComboBox<String>(gradLevels);
		gradLevelList.setSelectedIndex(gradLevelNum);
		gradDatePicker = new DatePicker(new DateTime()).getDatePicker();

		// Create table field
		gradTableModel = new GradTableModel(gradList);
		gradTable = new JTable(gradTableModel);
		JScrollPane gradScrollPane = createTablePanel(gradTable);
		gradScrollPane.setPreferredSize(new Dimension(GRAD_TABLE_WIDTH, GRAD_TABLE_HEIGHT));

		// Create error field and OK/Cancel buttons
		errorField = new JLabel(" ");
		errorField.setForeground(CustomFonts.TITLE_COLOR);
		errorField.setPreferredSize(new Dimension(PANEL_WIDTH - 20, errorField.getPreferredSize().height));
		errorField.setHorizontalTextPosition(JLabel.CENTER);
		errorField.setHorizontalAlignment(JLabel.CENTER);
		submitButton = new JButton("Submit Scores");
		exitButton = new JButton("Exit");

		// Set panel height/width
		controlPanel.setPreferredSize(new Dimension(PANEL_WIDTH, CONTROL_PANEL_HEIGHT));
		levelPanel.setPreferredSize(new Dimension(PANEL_WIDTH, CONTROL_SUB_PANEL_HEIGHT));
		gradDatePanel.setPreferredSize(new Dimension(PANEL_WIDTH, CONTROL_SUB_PANEL_HEIGHT));
		buttonPanel.setPreferredSize(new Dimension(PANEL_WIDTH, BUTTON_PANEL_HEIGHT));

		// Add orange borders for all input fields
		Border innerBorder = BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true);
		Border outerBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		gradLevelList.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		gradDatePicker.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		gradScrollPane.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));

		// Add all of the above to the panels
		levelPanel.add(levelLabel);
		levelPanel.add(gradLevelList);
		gradDatePanel.add(gradDateLabel);
		gradDatePanel.add(gradDatePicker);

		controlPanel.add(levelPanel);
		controlPanel.add(gradDatePanel);

		tablePanel.add(gradScrollPane);
		errorPanel.add(errorField, JPanel.CENTER_ALIGNMENT);
		okCancelPanel.add(submitButton);
		okCancelPanel.add(exitButton);
		buttonPanel.add(errorPanel, JPanel.CENTER_ALIGNMENT);
		buttonPanel.add(okCancelPanel);

		// Add panels to dialog
		setLayout(new BorderLayout());
		add(controlPanel, BorderLayout.NORTH);
		add(tablePanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		// Add listener to buttons
		submitButton.addActionListener(this);
		exitButton.addActionListener(this);

		// Set icon
		// TODO: Don't hard-code file name
		ImageIcon icon = new ImageIcon(getClass().getResource("PPicon24_Color_F16412.png"));
		setIconImage(icon.getImage());

		// Configure dialog window
		// TODO: Locate dialog relative to parent
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle(dialogTitle);
		setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		setLocation(300, 300);
		setResizable(false);
		pack();
		setVisible(true);
	}

	private JScrollPane createTablePanel(JTable table) {
		// Set up table parameters
		table.setFont(CustomFonts.TABLE_TEXT_FONT);
		table.setGridColor(CustomFonts.TABLE_GRID_COLOR);
		table.setShowGrid(true);
		table.getTableHeader().setFont(CustomFonts.TABLE_HEADER_FONT);

		// Configure column widths
		table.getColumnModel().getColumn(GradTableModel.SCORE_COLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(GradTableModel.SCORE_COLUMN).setMaxWidth(100);

		// Set table properties
		table.setDefaultRenderer(Object.class, new GradTableRenderer());
		table.getColumnModel().getColumn(GradTableModel.SCORE_COLUMN).setCellEditor(new GradCellEditor());
		table.setCellSelectionEnabled(true);

		// Create scroll pane for table
		JScrollPane scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		return scrollPane;
	}

	private void addGradRecordToDb(int clientID, String studentName, int score) {
		String levelString = ((Integer) gradLevelList.getSelectedIndex()).toString();
		String startDate = controller.getStartDateByClientIdAndLevel(clientID, levelString);
		GraduationModel gradModel = new GraduationModel(clientID, studentName, levelString, score, startDate,
				gradDatePicker.getJFormattedTextField().getText(), false, false);
		controller.addGraduationRecord(gradModel);
	}

	private int getLevelFromClassName(String className) {
		if (className == null || className.length() < 2)
			return 0;

		if (className.charAt(0) >= '0' && className.charAt(0) <= '9' && className.charAt(1) == '@')
			return className.charAt(0) - '0';
		else
			return 0;
	}

	private void createGradLevelList() {
		for (int i = 0; i < gradLevels.length; i++) {
			gradLevels[i] = "Level " + i + "  ";
		}
	}

	public static boolean isValidClassName(String className) {
		if (className == null || className.length() < 2)
			return false;

		if (className.charAt(0) >= '0' && className.charAt(0) <= '9' && className.charAt(1) == '@')
			return true;
		else
			return false;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == submitButton) {
			errorField.setText("");
			int countGrads = 0;

			// Check that all fields are filled in
			for (int i = 0; i < gradTableModel.getRowCount(); i++) {
				int clientID = ((int) gradTableModel.getValueAt(i, GradTableModel.CLIENT_ID_COLUMN));
				String scoreString = ((JTextField) gradTableModel.getValueAt(i, GradTableModel.SCORE_COLUMN)).getText();
				String studentName = (String) gradTableModel.getValueAt(i, GradTableModel.STUDENT_NAME_COLUMN);

				if (!scoreString.equals("")) {
					try {
						Integer score = Integer.parseInt(scoreString);
						if (score > 100) {
							errorField.setText("Student score cannot be greater than 100% (student #" + (i + 1) + ")");
							return;
						}

						// Add record to database
						addGradRecordToDb(clientID, studentName, score);
						countGrads++;

					} catch (NumberFormatException e2) {
						errorField.setText("Student score must be an integer from 0 - 100% (student #" + (i + 1) + ")");
						return;
					}
				}
			}

			if (countGrads == 0)
				// No grades entered
				errorField.setText("No student scores entered");
			else
				// Successfully added records
				errorField.setText(countGrads + " graduation record(s) successfully added to database");

		} else { // Exit button
			setVisible(false);
			dispose();
		}
	}

	/***** DIALOG MODEL SUB-CLASS *****/
	private class DialogGradModel {
		private int clientID;
		private String studentName;
		private JTextField score = new JTextField();

		public DialogGradModel(int clientID, String studentName) {
			this.clientID = clientID;
			this.studentName = studentName;
			this.score.setText("");

			this.score.setFont(CustomFonts.TABLE_TEXT_FONT);
			this.score.setHorizontalAlignment(JTextField.CENTER);
		}

		public int getClientID() {
			return clientID;
		}

		public String getStudentName() {
			return studentName;
		}

		public JTextField getScoreTextField() {
			return score;
		}
	}

	/***** TABLE MODEL SUB-CLASS *****/
	private class GradTableModel extends AbstractTableModel {
		public static final int STUDENT_NAME_COLUMN = 0;
		public static final int SCORE_COLUMN = 1;
		public static final int CLIENT_ID_COLUMN = 2; // Not actually a table column
		public static final int NUM_COLUMNS = 3;

		private final String[] colNames = { " Student Name ", " Score (%) " };
		private Object[][] tableObjects;

		public GradTableModel(ArrayList<DialogGradModel> grads) {
			tableObjects = new Object[grads.size()][NUM_COLUMNS];

			for (int row = 0; row < grads.size(); row++) {
				tableObjects[row][STUDENT_NAME_COLUMN] = grads.get(row).getStudentName();
				tableObjects[row][SCORE_COLUMN] = grads.get(row).getScoreTextField();
				tableObjects[row][CLIENT_ID_COLUMN] = grads.get(row).getClientID();
			}
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
			if (tableObjects == null)
				return 0;
			else
				return tableObjects.length;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (tableObjects.length == 0)
				return Object.class;
			else
				return tableObjects[0][columnIndex].getClass();
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			if (col == SCORE_COLUMN)
				return true;
			else
				return false;
		}

		@Override
		public Object getValueAt(int row, int col) {
			return tableObjects[row][col];
		}
	}

	/***** TABLE CELL EDITOR SUB-CLASS *****/
	private class GradCellEditor extends AbstractCellEditor implements TableCellEditor {
		// The only column using the cell editor is the SCORE column
		JTextField textField;

		@Override
		public Object getCellEditorValue() {
			return textField.getText();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			textField = (JTextField) ((GradTableModel) table.getModel()).getValueAt(row, column);
			return textField;
		}
	}

	/***** TABLE RENDERER SUB-CLASS *****/
	private class GradTableRenderer extends JLabel implements TableCellRenderer {
		private GradTableRenderer() {
			super();
			super.setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			String text;
			if (column == GradTableModel.SCORE_COLUMN) {
				text = ((JTextField) value).getText();
			} else
				text = ((String) value);
			setText(text);

			if (column != -1) {
				setFont(CustomFonts.TABLE_TEXT_FONT);
				super.setForeground(Color.black);

				if (isSelected)
					super.setBackground(CustomFonts.SELECTED_BACKGROUND_COLOR);
				else
					super.setBackground(CustomFonts.UNSELECTED_BACKGROUND_COLOR);

				if (column == GradTableModel.STUDENT_NAME_COLUMN) {
					super.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0)); // left pad
					super.setHorizontalAlignment(LEFT);
				} else {
					super.setBorder(BorderFactory.createEmptyBorder());
					super.setHorizontalAlignment(CENTER);
				}
			}
			return this;
		}
	}
}
