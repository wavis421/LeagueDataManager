package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import controller.Controller;
import model.GraduationModel;

/**
 * Table showing pending Graduation records
 * 
 * @author wavis
 *
 */
public class GraduationTable extends JPanel {
	private static final int ROW_GAP = 5;

	private JPanel tablePanel;
	private JTable table;
	private GraduationTableModel gradTableModel;
	private TableListeners gradListener;
	private JScrollPane scrollPane;

	private TableRowSorter<GraduationTableModel> rowSorter;

	public GraduationTable(JPanel tablePanel, ArrayList<GraduationModel> arrayList) {
		this.tablePanel = tablePanel;

		gradTableModel = new GraduationTableModel(arrayList);
		table = new JTable(gradTableModel);

		createTablePanel();
		createMouseListeners();

		rowSorter = new TableRowSorter<GraduationTableModel>((GraduationTableModel) table.getModel());
	}

	public void setTableListener(TableListeners listener) {
		this.gradListener = listener;
	}

	public JTable getTable() {
		return table;
	}

	public void setData(JPanel tablePanel, ArrayList<GraduationModel> gradList) {
		scrollPane.setVisible(true);
		this.tablePanel = tablePanel;
		tablePanel.add(scrollPane, BorderLayout.NORTH);

		gradTableModel.setData(gradList);
		gradTableModel.fireTableDataChanged();
	}

	public void removeData() {
		if (gradTableModel.getRowCount() > 0) {
			gradTableModel.removeAll();
		}

		scrollPane.setVisible(false);
	}

	private void createTablePanel() {
		// Set up table parameters
		table.setFont(CustomFonts.TABLE_TEXT_FONT);
		table.setGridColor(CustomFonts.TABLE_GRID_COLOR);
		table.setShowGrid(true);
		table.getTableHeader().setFont(CustomFonts.TABLE_HEADER_FONT);
		int origRowHeight = table.getRowHeight();
		table.setRowHeight(origRowHeight + ROW_GAP);

		// Configure column widths
		table.getColumnModel().getColumn(GraduationTableModel.IN_SALESFORCE_COLUMN).setMaxWidth(120);
		table.getColumnModel().getColumn(GraduationTableModel.IN_SALESFORCE_COLUMN).setPreferredWidth(120);
		table.getColumnModel().getColumn(GraduationTableModel.PROCESSED_COLUMN).setMaxWidth(120);
		table.getColumnModel().getColumn(GraduationTableModel.PROCESSED_COLUMN).setPreferredWidth(120);
		table.getColumnModel().getColumn(GraduationTableModel.LEVEL_PASSED_COLUMN).setMaxWidth(120);
		table.getColumnModel().getColumn(GraduationTableModel.LEVEL_PASSED_COLUMN).setPreferredWidth(120);
		table.getColumnModel().getColumn(GraduationTableModel.TESTED_OUT_COLUMN).setMaxWidth(120);
		table.getColumnModel().getColumn(GraduationTableModel.TESTED_OUT_COLUMN).setPreferredWidth(120);
		table.getColumnModel().getColumn(GraduationTableModel.START_DATE_COLUMN).setMaxWidth(130);
		table.getColumnModel().getColumn(GraduationTableModel.START_DATE_COLUMN).setPreferredWidth(130);
		table.getColumnModel().getColumn(GraduationTableModel.GRAD_DATE_COLUMN).setMaxWidth(130);
		table.getColumnModel().getColumn(GraduationTableModel.GRAD_DATE_COLUMN).setPreferredWidth(130);
		table.getColumnModel().getColumn(GraduationTableModel.STUDENT_NAME_COLUMN).setMaxWidth(250);
		table.getColumnModel().getColumn(GraduationTableModel.STUDENT_NAME_COLUMN).setPreferredWidth(250);
		table.getColumnModel().getColumn(GraduationTableModel.SCORE_COLUMN).setMaxWidth(100);
		table.getColumnModel().getColumn(GraduationTableModel.SCORE_COLUMN).setPreferredWidth(100);

		table.setDefaultRenderer(Object.class, new GradTableRenderer());
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

	private void createMouseListeners() {
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int row = table.getSelectedRow();
				int col = table.getSelectedColumn();
				if (row < 0)
					return;

				if (e.getButton() == MouseEvent.BUTTON1 && col == GraduationTableModel.PROCESSED_COLUMN) {
					int modelRow = table.convertRowIndexToModel(row);
					boolean checked = (boolean) table.getValueAt(row, col);
					gradTableModel.setProcessed(modelRow, !checked);
					gradTableModel.fireTableDataChanged();

					// Update changes to database
					gradListener.updateGradField((int) gradTableModel.getClientID(modelRow),
							(String) table.getValueAt(row, GraduationTableModel.STUDENT_NAME_COLUMN),
							(String) table.getValueAt(row, GraduationTableModel.LEVEL_PASSED_COLUMN),
							Controller.GRAD_MODEL_PROCESSED_FIELD, !checked);
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
	private class GradTableRenderer extends JLabel implements TableCellRenderer {
		private GradTableRenderer() {
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

				if (column == GraduationTableModel.STUDENT_NAME_COLUMN) {
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
