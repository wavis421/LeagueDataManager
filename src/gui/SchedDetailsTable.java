package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter.SortKey;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import model.ScheduleModel;

/**
 * Table showing detailed information for weekly scheduled classes.
 * 
 * @author wavis
 *
 */
public class SchedDetailsTable extends JPanel {
	private static final int ROW_GAP = 5;

	private static final int POPUP_WIDTH = 240;
	private static final int POPUP_HEIGHT = 30;

	private JPanel tablePanel;
	private JTable table;
	private SchedDetailsTableModel schedTablemodel;
	private TableListeners schedListener;
	private JScrollPane scrollPane;

	private TableRowSorter<SchedDetailsTableModel> rowSorter;
	private List<? extends SortKey> defaultSortKeys, currSortKeys = null;

	public SchedDetailsTable(JPanel tablePanel, ArrayList<ScheduleModel> arrayList) {
		this.tablePanel = tablePanel;

		schedTablemodel = new SchedDetailsTableModel(arrayList);
		table = new JTable(schedTablemodel);

		createTablePanel();
		createSchedTablePopups();

		rowSorter = new TableRowSorter<SchedDetailsTableModel>((SchedDetailsTableModel) table.getModel());
		defaultSortKeys = rowSorter.getSortKeys();
	}

	public void setTableListener(TableListeners listener) {
		this.schedListener = listener;
	}

	public JTable getTable() {
		return table;
	}

	public void setData(JPanel tablePanel, ArrayList<ScheduleModel> schedList, boolean clearTableSettings) {
		scrollPane.setVisible(true);
		this.tablePanel = tablePanel;
		tablePanel.add(scrollPane, BorderLayout.NORTH);
		currSortKeys = rowSorter.getSortKeys();

		// Update schedule data model
		schedTablemodel.setData(schedList);

		// If table settings being cleared, go back to default sorting
		if (clearTableSettings)
			rowSorter.setSortKeys(defaultSortKeys);
		else
			rowSorter.setSortKeys(currSortKeys);

		schedTablemodel.fireTableDataChanged();
	}

	public void removeData() {
		if (schedTablemodel.getRowCount() > 0) {
			schedTablemodel.removeAll();
		}

		scrollPane.setVisible(false);
	}

	private void createTablePanel() {
		// Set up table parameters
		table.setFont(CustomFonts.TABLE_TEXT_FONT);
		table.setGridColor(CustomFonts.TABLE_GRID_COLOR);
		table.setShowGrid(true);
		int origRowHeight = table.getRowHeight();
		table.setRowHeight(origRowHeight + ROW_GAP);

		// Configure column widths
		table.getColumnModel().getColumn(SchedDetailsTableModel.CLASS_NAME_COLUMN).setPreferredWidth(300);
		table.getColumnModel().getColumn(SchedDetailsTableModel.CLASS_NAME_COLUMN).setMaxWidth(600);
		table.getColumnModel().getColumn(SchedDetailsTableModel.DOW_COLUMN).setPreferredWidth(170);
		table.getColumnModel().getColumn(SchedDetailsTableModel.DOW_COLUMN).setMaxWidth(200);
		table.getColumnModel().getColumn(SchedDetailsTableModel.TIME_COLUMN).setPreferredWidth(170);
		table.getColumnModel().getColumn(SchedDetailsTableModel.TIME_COLUMN).setMaxWidth(200);
		table.getColumnModel().getColumn(SchedDetailsTableModel.NUM_STUDENTS_COLUMN).setPreferredWidth(170);
		table.getColumnModel().getColumn(SchedDetailsTableModel.NUM_STUDENTS_COLUMN).setMaxWidth(200);
		table.getColumnModel().getColumn(SchedDetailsTableModel.MIN_AGE_COLUMN).setPreferredWidth(170);
		table.getColumnModel().getColumn(SchedDetailsTableModel.MIN_AGE_COLUMN).setMaxWidth(200);
		table.getColumnModel().getColumn(SchedDetailsTableModel.MAX_AGE_COLUMN).setPreferredWidth(170);
		table.getColumnModel().getColumn(SchedDetailsTableModel.MAX_AGE_COLUMN).setMaxWidth(200);
		table.getColumnModel().getColumn(SchedDetailsTableModel.AVG_AGE_COLUMN).setPreferredWidth(170);
		table.getColumnModel().getColumn(SchedDetailsTableModel.AVG_AGE_COLUMN).setMaxWidth(200);
		table.getColumnModel().getColumn(SchedDetailsTableModel.MODULE_RANGE_COLUMN).setPreferredWidth(400);

		table.setDefaultRenderer(Object.class, new SchedDetailTableRenderer());
		table.getTableHeader().setDefaultRenderer(new SchedTableHdrRenderer());
		table.setAutoCreateRowSorter(true);
		table.setCellSelectionEnabled(true);
		new TableKeystrokeHandler(table);

		tablePanel.setLayout(new BorderLayout());
		scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, tablePanel.getPreferredSize().height - 70));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1)); // top, left, bottom, right
		tablePanel.add(scrollPane, BorderLayout.CENTER);
	}

	private void createSchedTablePopups() {
		// Table panel POP UP menu
		JPopupMenu tablePopup = new JPopupMenu();
		JMenuItem viewByClass = new JMenuItem("View by Class ");
		tablePopup.add(viewByClass);
		tablePopup.setPreferredSize(new Dimension(POPUP_WIDTH, POPUP_HEIGHT));

		// POP UP action listeners
		viewByClass.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Retrieve class name from selected row
				int row = table.convertRowIndexToModel(table.getSelectedRow());
				SchedDetailsTableModel model = (SchedDetailsTableModel) table.getModel();
				String className = (String) model.getValueAt(row, SchedDetailsTableModel.CLASS_NAME_COLUMN);

				// Display attendance table for selected class
				table.clearSelection();
				schedListener.viewAttendanceByClass(className, null);
			}
		});
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int row = table.getSelectedRow();
				if (e.getButton() == MouseEvent.BUTTON3 && row != -1) {
					// Show popup menu
					tablePopup.show(table, e.getX(), e.getY());
				}
			}

			public void mouseClicked(MouseEvent e) {
				// Single row selects one cell (default), double click to get entire row
				if (e.getClickCount() == 2) {
					int col = table.getSelectedColumn();
					int row = table.getSelectedRow();
					if (row > -1 && col > -1) {
						table.setRowSelectionInterval(row, row);
						table.setColumnSelectionInterval(0, table.getColumnCount() - 1);
					}
				}
			}
		});
	}

	public void updateSearchField(String searchText) {
		if (searchText.equals("")) {
			rowSorter.setRowFilter(null);
		} else {
			try {
				rowSorter.setRowFilter(RowFilter.regexFilter("(?i)\\b" + searchText));

			} catch (java.util.regex.PatternSyntaxException e) {
				System.out.println(e.getMessage());
				return;
			}
		}
		table.setRowSorter(rowSorter);
	}

	/**** Table renderer sub-class ***/
	private class SchedDetailTableRenderer extends JLabel implements TableCellRenderer {
		private SchedDetailTableRenderer() {
			super();
			super.setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			String text = ((String) value);
			setText(text);

			if (column != -1) {
				setFont(CustomFonts.TABLE_TEXT_FONT);
				super.setForeground(Color.black);

				if (isSelected)
					super.setBackground(CustomFonts.SELECTED_BACKGROUND_COLOR);
				else
					super.setBackground(CustomFonts.UNSELECTED_BACKGROUND_COLOR);
				super.setBorder(BorderFactory.createEmptyBorder());
				super.setHorizontalAlignment(CENTER);
			}
			return this;
		}
	}

	/**** Table header renderer sub-class ***/
	private class SchedTableHdrRenderer extends JLabel implements TableCellRenderer {
		// Render the table header
		Border innerBorder = BorderFactory.createLineBorder(CustomFonts.TABLE_GRID_COLOR, 2, true);
		Border outerBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

		private SchedTableHdrRenderer() {
			super();
			super.setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			String text = ((String) value);
			setText(text);

			if (column != -1) {
				super.setHorizontalAlignment(CENTER);
				setFont(CustomFonts.TABLE_HEADER_FONT);
				super.setForeground(Color.black);
				setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
			}
			return this;
		}
	}
}
