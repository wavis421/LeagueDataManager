package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.sql.Date;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import model.StudentModel;
import model.StudentTableModel;

public class StudentTable extends JPanel {
	private static final int ROW_GAP = 5;

	private JPanel tablePanel = new JPanel();
	private JTable table;
	private StudentTableModel tableModel;

	private ArrayList<StudentModel> studentList;

	public StudentTable(ArrayList<StudentModel> studentList) {
		this.studentList = studentList;
		createStudentTablePanel();
	}

	public JPanel getTablePanel() {
		return tablePanel;
	}

	private void createStudentTablePanel() {
		tableModel = new StudentTableModel(studentList);
		table = new JTable(tableModel);

		// Set up table parameters
		table.setFont(CustomFonts.TABLE_TEXT_FONT);
		table.getTableHeader().setFont(CustomFonts.TABLE_HEADER_FONT);
		int origRowHeight = table.getRowHeight();
		table.setRowHeight(origRowHeight + ROW_GAP);

		// Configure column widths
		table.getColumnModel().getColumn(tableModel.getColumnForGender()).setMaxWidth(35);
		table.getColumnModel().getColumn(tableModel.getColumnForClientID()).setMaxWidth(75);
		table.getColumnModel().getColumn(tableModel.getColumnForStartDate()).setMaxWidth(105);
		table.getColumnModel().getColumn(tableModel.getColumnForGradYear()).setMaxWidth(95);
		table.getColumnModel().getColumn(tableModel.getColumnForHomeLocation()).setMaxWidth(165);

		table.getColumnModel().getColumn(tableModel.getColumnForStartDate()).setPreferredWidth(100);
		table.getColumnModel().getColumn(tableModel.getColumnForGradYear()).setPreferredWidth(90);
		table.getColumnModel().getColumn(tableModel.getColumnForHomeLocation()).setPreferredWidth(160);

		table.setDefaultRenderer(Object.class, new StudentTableRenderer());
		table.setAutoCreateRowSorter(true);

		tablePanel.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tablePanel.add(scrollPane, BorderLayout.NORTH);
	}

	public void setData(ArrayList<StudentModel> db) {
		tableModel.setData(db);
		tableModel.fireTableDataChanged();
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
			else
				setText((String) value);

			if (column != -1) {
				setFont(CustomFonts.TABLE_TEXT_FONT);
				super.setForeground(Color.black);

				if (isSelected)
					super.setBackground(CustomFonts.SELECTED_BACKGROUND_COLOR);
				else
					super.setBackground(CustomFonts.UNSELECTED_BACKGROUND_COLOR);

				super.setVerticalAlignment(TOP);
				super.setHorizontalAlignment(CENTER);
			}
			return this;
		}
	}
}
