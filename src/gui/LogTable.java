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

import model.LogDataModel;
import model.LogTableModel;
import model.StudentNameModel;

public class LogTable extends JPanel {
	private static final int ROW_GAP = 5;

	private JPanel tablePanel;
	private JTable table;
	private LogTableModel logTableModel;
	private JScrollPane scrollPane;

	public LogTable(JPanel tablePanel, ArrayList<LogDataModel> logList) {
		this.tablePanel = tablePanel;

		logTableModel = new LogTableModel(logList);
		table = new JTable(logTableModel);

		createTablePanel();
	}

	private void createTablePanel() {
		// Set up table parameters
		table.setFont(CustomFonts.TABLE_TEXT_FONT);
		table.getTableHeader().setFont(CustomFonts.TABLE_HEADER_FONT);
		int origRowHeight = table.getRowHeight();
		table.setRowHeight(origRowHeight + ROW_GAP);

		configureColumnWidths();

		// Set table properties
		table.setDefaultRenderer(Object.class, new LogTableRenderer());
		table.setAutoCreateRowSorter(true);

		tablePanel.setLayout(new BorderLayout());
		scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(
				new Dimension(tablePanel.getPreferredSize().width, tablePanel.getPreferredSize().height - 70));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tablePanel.add(scrollPane, BorderLayout.NORTH);
	}

	public void setData(JPanel tablePanel, ArrayList<LogDataModel> logList) {
		scrollPane.setVisible(true);
		this.tablePanel = tablePanel;
		tablePanel.add(scrollPane, BorderLayout.NORTH);

		logTableModel.setData(logList);
		logTableModel.fireTableDataChanged();
	}

	public void removeData() {
		if (logTableModel.getRowCount() > 0) {
			logTableModel.removeAll();
			logTableModel.fireTableDataChanged();
		}

		scrollPane.setVisible(false);
	}

	private void configureColumnWidths() {
		// Configure column widths
		table.getColumnModel().getColumn(LogTableModel.CLIENT_ID_COLUMN).setMaxWidth(75);
		table.getColumnModel().getColumn(LogTableModel.STUDENT_NAME_COLUMN).setMaxWidth(220);
		table.getColumnModel().getColumn(LogTableModel.STUDENT_NAME_COLUMN).setPreferredWidth(180);
	}

	// TODO: share this table renderer
	public class LogTableRenderer extends JLabel implements TableCellRenderer {
		private LogTableRenderer() {
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

				if (column == LogTableModel.STATUS_COLUMN)
					super.setHorizontalAlignment(LEFT);
				else
					super.setHorizontalAlignment(CENTER);
			}
			return this;
		}
	}
}
