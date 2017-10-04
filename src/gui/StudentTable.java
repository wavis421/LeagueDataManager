package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.sql.Date;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellRenderer;

import model.StudentModel;
import model.StudentNameModel;

public class StudentTable extends JPanel {
	private static final int ROW_GAP = 5;

	private JPanel tablePanel;
	private JTable table;
	private StudentTableModel studentTableModel;
	private JScrollPane scrollPane;

	public StudentTable(JPanel tablePanel, ArrayList<StudentModel> studentList) {
		this.tablePanel = tablePanel;

		studentTableModel = new StudentTableModel(studentList);
		table = new JTable(studentTableModel);

		createTablePanel();
	}

	private void createTablePanel() {
		// Set up table parameters
		table.setFont(CustomFonts.TABLE_TEXT_FONT);
		table.getTableHeader().setFont(CustomFonts.TABLE_HEADER_FONT);
		int origRowHeight = table.getRowHeight();
		table.setRowHeight(origRowHeight + ROW_GAP);

		// Configure column widths
		table.getColumnModel().getColumn(StudentTableModel.GENDER_COLUMN).setMaxWidth(35);
		table.getColumnModel().getColumn(StudentTableModel.CLIENT_ID_COLUMN).setMaxWidth(75);
		table.getColumnModel().getColumn(StudentTableModel.START_DATE_COLUMN).setMaxWidth(105);
		table.getColumnModel().getColumn(StudentTableModel.GRAD_YEAR_COLUMN).setMaxWidth(95);
		table.getColumnModel().getColumn(StudentTableModel.HOME_LOCATION_COLUMN).setMaxWidth(165);

		table.getColumnModel().getColumn(StudentTableModel.START_DATE_COLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(StudentTableModel.GRAD_YEAR_COLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(StudentTableModel.HOME_LOCATION_COLUMN).setPreferredWidth(160);

		table.setDefaultRenderer(Object.class, new StudentTableRenderer());
		table.setAutoCreateRowSorter(true);

		tablePanel.setLayout(new BorderLayout());
		scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, tablePanel.getPreferredSize().height - 70));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tablePanel.add(scrollPane, BorderLayout.NORTH);
	}

	public JTable getTable() {
		return table;
	}

	public void setData(JPanel tablePanel, ArrayList<StudentModel> studentList) {
		scrollPane.setVisible(true);
		this.tablePanel = tablePanel;
		tablePanel.add(scrollPane, BorderLayout.NORTH);

		studentTableModel.setData(studentList);
		studentTableModel.fireTableDataChanged();
	}

	public void removeData() {
		if (studentTableModel.getRowCount() > 0) {
			studentTableModel.removeAll();
		}

		scrollPane.setVisible(false);
	}

	public class StudentTableRenderer extends JLabel implements TableCellRenderer {
		private StudentTableRenderer() {
			super();
			super.setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			if (value instanceof Integer)
				setText(String.valueOf(value));
			else if (value instanceof Date)
				setText(((Date) value).toString());
			else if (value instanceof StudentNameModel)
				setText(((StudentNameModel) value).toString());
			else
				setText((String) value);

			if (column != -1) {
				if (value instanceof StudentNameModel && ((StudentNameModel) value).getIsInMasterDb() == false)
					setFont(CustomFonts.TABLE_ITALIC_TEXT_FONT);
				else
					setFont(CustomFonts.TABLE_TEXT_FONT);
				super.setForeground(Color.black);

				if (isSelected)
					super.setBackground(CustomFonts.SELECTED_BACKGROUND_COLOR);
				else
					super.setBackground(CustomFonts.UNSELECTED_BACKGROUND_COLOR);

				super.setHorizontalAlignment(CENTER);
			}
			return this;
		}
	}
}
