package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.sql.Date;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellRenderer;

import model.StudentModel;
import model.StudentNameModel;
import model.StudentTableModel;

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
		table.getColumnModel().getColumn(studentTableModel.getColumnForGender()).setMaxWidth(35);
		table.getColumnModel().getColumn(studentTableModel.getColumnForClientID()).setMaxWidth(75);
		table.getColumnModel().getColumn(studentTableModel.getColumnForStartDate()).setMaxWidth(105);
		table.getColumnModel().getColumn(studentTableModel.getColumnForGradYear()).setMaxWidth(95);
		table.getColumnModel().getColumn(studentTableModel.getColumnForHomeLocation()).setMaxWidth(165);

		table.getColumnModel().getColumn(studentTableModel.getColumnForStartDate()).setPreferredWidth(100);
		table.getColumnModel().getColumn(studentTableModel.getColumnForGradYear()).setPreferredWidth(90);
		table.getColumnModel().getColumn(studentTableModel.getColumnForHomeLocation()).setPreferredWidth(160);

		table.setDefaultRenderer(Object.class, new StudentTableRenderer());
		table.setAutoCreateRowSorter(true);

		tablePanel.setLayout(new BorderLayout());
		scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, tablePanel.getPreferredSize().height - 70));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tablePanel.add(scrollPane, BorderLayout.NORTH);
	}

	public void setData(JPanel tablePanel, ArrayList<StudentModel> studentList) {
		scrollPane.setVisible(true);
		this.tablePanel = tablePanel;
		tablePanel.add(scrollPane, BorderLayout.NORTH);

		studentTableModel.setData(studentList);
		studentTableModel.fireTableDataChanged();
	}

	public void removeData() {
		System.out.println("Remove data, student table rows = " + studentTableModel.getRowCount());

		if (studentTableModel.getRowCount() > 0) {
			studentTableModel.removeAll();
			studentTableModel.fireTableDataChanged();
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
