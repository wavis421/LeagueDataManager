package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
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
	private final static int FRAME_WIDTH = 730;
	private final static int FRAME_HEIGHT = 460;

	// Main panel heights
	private final static int CONTROL_PANEL_HEIGHT = 185;
	private final static int TABLE_PANEL_HEIGHT = 150;
	private final static int BUTTON_PANEL_HEIGHT = 80;

	// Control sub-panel height
	private final static int CONTROL_SUB_PANEL_HEIGHT = 35;

	// Panel widths
	private final static int PANEL_WIDTH = FRAME_WIDTH - 10;
	private final static int PASSWORD_FIELD_WIDTH = 20;

	// Table width/height
	private final static int GRAD_TABLE_WIDTH = PANEL_WIDTH - 20;
	private final static int GRAD_TABLE_HEIGHT = TABLE_PANEL_HEIGHT - 10;

	// GUI components
	private JComboBox<String> emailUserList;
	private JPasswordField emailPwField;
	private JComboBox<String> gradLevelList;
	private JDatePickerImpl gradDatePicker;
	private JTable gradTable;
	private GradTableModel gradTableModel;
	private JButton emailAllButton, printAllButton;
	private JButton submitButton;
	private JButton doneButton;
	private JLabel errorField;
	private static String[] gradLevels = new String[10];

	// Temporary: for test only
	private String[] teacherEmails = { "wendy.avis@jointheleague.org", "jackie.a@jointheleague.org" };
	private static Controller controller;

	public GraduationDialog(Controller newController, int clientID, String studentName) {
		// Graduate by student: create list with 1 student
		ArrayList<DialogGradModel> gradList = new ArrayList<DialogGradModel>();
		gradList.add(new DialogGradModel(clientID, studentName));

		controller = newController;
		createGui(0, gradList, "Graduate " + studentName);
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
		JPanel emailPanel = new JPanel();
		JPanel passwordPanel = new JPanel();
		JPanel levelPanel = new JPanel();
		JPanel gradDatePanel = new JPanel();

		// Create sub-panels inside of button panel
		JPanel errorPanel = new JPanel();
		JPanel okCancelPanel = new JPanel();

		// Create labels and right justify
		JLabel emailUserLabel = new JLabel("Teacher email: ");
		JLabel emailPwLabel = new JLabel(" Password: ");
		JLabel levelLabel = new JLabel("Level passed: ");
		JLabel gradDateLabel = new JLabel("Graduation date: ");

		emailUserLabel.setHorizontalAlignment(JLabel.RIGHT);
		emailPwLabel.setHorizontalAlignment(JLabel.RIGHT);
		levelLabel.setHorizontalAlignment(JLabel.RIGHT);
		gradDateLabel.setHorizontalAlignment(JLabel.RIGHT);

		// Create input fields
		emailUserList = new JComboBox<String>(teacherEmails);
		emailPwField = new JPasswordField(PASSWORD_FIELD_WIDTH);
		gradLevelList = new JComboBox<String>(gradLevels);
		gradLevelList.setSelectedIndex(gradLevelNum);
		gradDatePicker = new DatePicker(new DateTime()).getDatePicker();

		// Create table field
		gradTableModel = new GradTableModel(gradList);
		gradTable = new JTable(gradTableModel);
		gradTable.addMouseListener(new GradTableListener());
		JScrollPane gradScrollPane = createTablePanel(gradTable);
		gradScrollPane.setPreferredSize(new Dimension(GRAD_TABLE_WIDTH, GRAD_TABLE_HEIGHT));

		// Create error field and OK/Cancel buttons
		errorField = new JLabel(" ");
		errorField.setForeground(CustomFonts.TITLE_COLOR);
		errorField.setPreferredSize(new Dimension(PANEL_WIDTH - 20, errorField.getPreferredSize().height));
		errorField.setHorizontalTextPosition(JLabel.CENTER);
		errorField.setHorizontalAlignment(JLabel.CENTER);
		emailAllButton = new JButton("Select all 'Email Parents'");
		printAllButton = new JButton("Select all 'Print Certs'");
		submitButton = new JButton("Submit Scores");
		doneButton = new JButton("Done");

		// Set panel height/width
		controlPanel.setPreferredSize(new Dimension(PANEL_WIDTH, CONTROL_PANEL_HEIGHT));
		emailPanel.setPreferredSize(new Dimension(PANEL_WIDTH, CONTROL_SUB_PANEL_HEIGHT));
		passwordPanel.setPreferredSize(new Dimension(PANEL_WIDTH, CONTROL_SUB_PANEL_HEIGHT));
		levelPanel.setPreferredSize(new Dimension(PANEL_WIDTH, CONTROL_SUB_PANEL_HEIGHT));
		gradDatePanel.setPreferredSize(new Dimension(PANEL_WIDTH, CONTROL_SUB_PANEL_HEIGHT + 5));
		buttonPanel.setPreferredSize(new Dimension(PANEL_WIDTH, BUTTON_PANEL_HEIGHT));

		// Add orange borders for all input fields
		Border innerBorder = BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true);
		Border outerBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		emailUserList.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		emailPwField.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		gradLevelList.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		gradDatePicker.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		gradScrollPane.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));

		// Add all of the above to the panels
		emailPanel.add(emailUserLabel);
		emailPanel.add(emailUserList);
		passwordPanel.add(emailPwLabel);
		passwordPanel.add(emailPwField);
		levelPanel.add(levelLabel);
		levelPanel.add(gradLevelList);
		gradDatePanel.add(gradDateLabel);
		gradDatePanel.add(gradDatePicker);

		controlPanel.add(emailPanel);
		controlPanel.add(passwordPanel);
		controlPanel.add(levelPanel);
		controlPanel.add(gradDatePanel);

		tablePanel.add(gradScrollPane);
		errorPanel.add(errorField, JPanel.CENTER_ALIGNMENT);
		okCancelPanel.add(printAllButton);
		okCancelPanel.add(emailAllButton);
		okCancelPanel.add(submitButton);
		okCancelPanel.add(doneButton);
		buttonPanel.add(errorPanel, JPanel.CENTER_ALIGNMENT);
		buttonPanel.add(okCancelPanel);

		// Add panels to dialog
		setLayout(new BorderLayout());
		add(controlPanel, BorderLayout.NORTH);
		add(tablePanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		// Add listener to buttons
		printAllButton.addActionListener(this);
		emailAllButton.addActionListener(this);
		submitButton.addActionListener(this);
		doneButton.addActionListener(this);

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
		table.getColumnModel().getColumn(GradTableModel.SCORE_COLUMN).setPreferredWidth(80);
		table.getColumnModel().getColumn(GradTableModel.SCORE_COLUMN).setMaxWidth(80);
		table.getColumnModel().getColumn(GradTableModel.EMAIL_PARENT_COLUMN).setPreferredWidth(120);
		table.getColumnModel().getColumn(GradTableModel.EMAIL_PARENT_COLUMN).setMaxWidth(120);
		table.getColumnModel().getColumn(GradTableModel.PASSED_COLUMN).setPreferredWidth(120);
		table.getColumnModel().getColumn(GradTableModel.PASSED_COLUMN).setMaxWidth(120);
		table.getColumnModel().getColumn(GradTableModel.PRINT_CERTS_COLUMN).setPreferredWidth(120);
		table.getColumnModel().getColumn(GradTableModel.PRINT_CERTS_COLUMN).setMaxWidth(120);

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

	private void addGradRecordToDb(int clientID, String studentName, Double score) {
		String levelString = ((Integer) gradLevelList.getSelectedIndex()).toString();
		String startDate = controller.getStartDateByClientIdAndLevel(clientID, levelString);
		GraduationModel gradModel = new GraduationModel(clientID, studentName, levelString, score, startDate,
				gradDatePicker.getJFormattedTextField().getText(), false, false);
		controller.addGraduationRecord(gradModel);
	}

	// TODO: Make this a class to share with "github dialog"
	private boolean generateAndSendEmail(String emailUser, String emailPassword, String emailBody) {
		// Currently hard-coded to send using gmail SMTP
		Properties properties = System.getProperties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.socketFactory.port", "465");
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.connectiontimeout", 10000); // 10 seconds

		Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(emailUser, new String(emailPassword));
			}
		});

		// Set cursor to "wait" cursor
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			// Set message fields
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(emailUser));
			message.setSubject("Graduate class");
			message.setText(emailBody, "utf-8");
			message.setSentDate(new Date());

			// Set email recipient
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailUser));

			// Send email
			Transport.send(message);

			// Set cursor back to default
			this.setCursor(Cursor.getDefaultCursor());

			return true;

		} catch (MessagingException e) {
			System.out.println(e.getMessage());
			errorField.setText("Failure sending email to " + emailUser);
		}

		// Set cursor back to default
		this.setCursor(Cursor.getDefaultCursor());
		return false;
	}

	private int getLevelFromClassName(String className) {
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

	private void updateEmailParents() {
		// User selected Email Parents button: set true for rows with valid score
		for (int i = 0; i < gradTable.getRowCount(); i++) {
			String score = ((JTextField) (gradTable.getValueAt(i, GradTableModel.SCORE_COLUMN))).getText();
			if (score.equals(""))
				gradTableModel.setEmailParents(i, false);
			else
				gradTableModel.setEmailParents(i, true);
		}
		updateCheckBoxes();
	}

	private void updatePrintCerts() {
		// User selected Print Certs button: set true for rows with valid score
		for (int i = 0; i < gradTable.getRowCount(); i++) {
			String score = ((JTextField) (gradTable.getValueAt(i, GradTableModel.SCORE_COLUMN))).getText();
			if (score.equals(""))
				gradTableModel.setPrintCertificates(i, false);
			else
				gradTableModel.setPrintCertificates(i, true);
		}
		updateCheckBoxes();
	}

	private void updateCheckBoxes() {
		for (int i = 0; i < gradTable.getRowCount(); i++) {
			String scoreString = ((JTextField) (gradTable.getValueAt(i, GradTableModel.SCORE_COLUMN))).getText();

			if (scoreString.equals("")) {
				clearClassPassedFlags(i);
				continue;
			}

			// There is data in the score field, so try to convert to double
			try {
				Double score = Double.parseDouble(scoreString);
				if (score > 100.0) {
					clearClassPassedFlags(i);
					errorField.setText("Student score cannot be greater than 100%");
				} else if (score < 70.0) {
					clearClassPassedFlags(i);

				} else {
					// Passed!
					gradTableModel.setPassedTest(i, true);
				}

			} catch (NumberFormatException e2) {
				errorField.setText("Student score (%) must be a number");
				clearClassPassedFlags(i);
			}
		}
		gradTableModel.fireTableDataChanged();
	}

	private void clearClassPassedFlags(int i) {
		gradTableModel.setPassedTest(i, false);
		gradTableModel.setEmailParents(i, false);
		gradTableModel.setPrintCertificates(i, false);
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
			String pw = emailPwField.getText();
			int countGrads = 0;

			// Check that all fields are filled in, then create body and send email
			if (!pw.equals("")) {
				String body = "Test Graduating Level " + (Integer) gradLevelList.getSelectedIndex() + ": ";
				for (int i = 0; i < gradTableModel.getRowCount(); i++) {
					int clientID = ((int) gradTableModel.getValueAt(i, GradTableModel.CLIENT_ID_COLUMN));
					String scoreString = ((JTextField) gradTableModel.getValueAt(i, GradTableModel.SCORE_COLUMN))
							.getText();
					String studentName = (String) gradTableModel.getValueAt(i, GradTableModel.STUDENT_NAME_COLUMN);
					boolean emailParent = ((boolean) gradTableModel.getValueAt(i, GradTableModel.EMAIL_PARENT_COLUMN));
					boolean printCerts = ((boolean) gradTableModel.getValueAt(i, GradTableModel.PRINT_CERTS_COLUMN));
					boolean passedTest = ((boolean) gradTableModel.getValueAt(i, GradTableModel.PASSED_COLUMN));

					if (!scoreString.equals("")) {
						try {
							Double score = Double.parseDouble(scoreString);
							if (score > 100.0) {
								updateCheckBoxes();
								return;
							}
							if (score < 70.0) {
								if (passedTest) {
									gradTableModel.setPassedTest(i, false);
									gradTableModel.fireTableDataChanged();
								}
								if (emailParent) {
									errorField.setText("Cannot email parent without a passing grade");
									return;
								} else if (printCerts) {
									errorField.setText("Cannot print certificates without a passing grade");
									return;
								}
							} else {
								gradTableModel.setPassedTest(i, true);
							}

							// Now we're finally good to go...
							body += "\n\t" + gradTableModel.getValueAt(i, GradTableModel.STUDENT_NAME_COLUMN)
									+ ", Score: " + scoreString + "%, Email Parents: " + emailParent;

							// Add record to database
							addGradRecordToDb(clientID, studentName, score);
							countGrads++;

						} catch (NumberFormatException e2) {
							updateCheckBoxes();
							errorField.setText("Student score (%) must be a number");
							return;
						}

					} else if (emailParent) {
						errorField.setText("Cannot send parent email without a valid score");
						return;
					} else if (printCerts) {
						errorField.setText("Cannot print certificates without a valid score");
						return;
					}
				}
				// Update all checkboxes that have changed
				updateCheckBoxes();

				if (countGrads == 0) {
					errorField.setText("No student grades entered");
					return;
				}

				// Send email. If error occurs, generateAndSendEmail method will report error
				if (generateAndSendEmail(emailUserList.getSelectedItem().toString(), pw, body))
					errorField.setText("Email sent successfully");

			} else {
				errorField.setText("Teacher email password required");
			}

		} else if (e.getSource() == emailAllButton) {
			updateEmailParents();

		} else if (e.getSource() == printAllButton) {
			updatePrintCerts();

		} else { // Done button
			setVisible(false);
			dispose();
		}
	}

	/***** DIALOG MODEL SUB-CLASS *****/
	private class DialogGradModel {
		private int clientID;
		private String studentName;
		private JTextField score = new JTextField();
		private boolean emailParents;
		private boolean passedTest;
		private boolean printCerts;

		public DialogGradModel(int clientID, String studentName) {
			this.clientID = clientID;
			this.studentName = studentName;
			this.score.setText("");
			this.emailParents = false;
			this.passedTest = false;
			this.printCerts = false;

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

		public JTextField getScore() {
			return score;
		}

		public boolean isEmailParents() {
			return emailParents;
		}

		public boolean isPassedTest() {
			return passedTest;
		}

		public boolean isPrintCerts() {
			return printCerts;
		}
	}

	/***** TABLE MODEL SUB-CLASS *****/
	private class GradTableModel extends AbstractTableModel {
		public static final int STUDENT_NAME_COLUMN = 0;
		public static final int SCORE_COLUMN = 1;
		public static final int PASSED_COLUMN = 2;
		public static final int PRINT_CERTS_COLUMN = 3;
		public static final int EMAIL_PARENT_COLUMN = 4;
		public static final int CLIENT_ID_COLUMN = 5; // Not actually a table column
		public static final int NUM_COLUMNS = 6;

		private final String[] colNames = { " Student Name ", " Score (%) ", " Passed ", " Print Certs ",
				" Email Parents " };
		private Object[][] tableObjects;

		public GradTableModel(ArrayList<DialogGradModel> grads) {
			tableObjects = new Object[grads.size()][NUM_COLUMNS];

			for (int row = 0; row < grads.size(); row++) {
				tableObjects[row][STUDENT_NAME_COLUMN] = grads.get(row).getStudentName();
				tableObjects[row][SCORE_COLUMN] = grads.get(row).getScoreTextField();
				tableObjects[row][PASSED_COLUMN] = grads.get(row).isPassedTest();
				tableObjects[row][PRINT_CERTS_COLUMN] = grads.get(row).isPrintCerts();
				tableObjects[row][EMAIL_PARENT_COLUMN] = grads.get(row).isEmailParents();
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
			if (col == EMAIL_PARENT_COLUMN || col == SCORE_COLUMN || col == PRINT_CERTS_COLUMN)
				return true;
			else
				return false;
		}

		public void setEmailParents(int row, boolean checked) {
			tableObjects[row][EMAIL_PARENT_COLUMN] = checked;
		}

		public void setPrintCertificates(int row, boolean checked) {
			tableObjects[row][PRINT_CERTS_COLUMN] = checked;
		}

		public void setPassedTest(int row, boolean checked) {
			tableObjects[row][PASSED_COLUMN] = checked;
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
			updateCheckBoxes();
			return textField.getText();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			textField = (JTextField) ((GradTableModel) table.getModel()).getValueAt(row, column);
			updateCheckBoxes();
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

	/***** TABLE LISTENER SUB-CLASS *****/
	private class GradTableListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			int row = gradTable.getSelectedRow();
			int col = gradTable.getSelectedColumn();

			if (e.getButton() == MouseEvent.BUTTON1 && row > -1) {
				if (col == GradTableModel.EMAIL_PARENT_COLUMN || col == GradTableModel.PRINT_CERTS_COLUMN) {
					String score = ((JTextField) (gradTable.getValueAt(row, GradTableModel.SCORE_COLUMN))).getText();
					if (score.equals("")) {
						gradTableModel.setEmailParents(row, false);
						gradTableModel.setPrintCertificates(row, false);
					} else {
						boolean checked = (boolean) gradTable.getValueAt(row, col);
						if (col == GradTableModel.EMAIL_PARENT_COLUMN)
							gradTableModel.setEmailParents(row, !checked);
						else
							gradTableModel.setPrintCertificates(row, !checked);
					}
					updateCheckBoxes();
				}
			}
		}
	}
}
