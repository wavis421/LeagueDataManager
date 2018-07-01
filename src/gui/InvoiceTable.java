package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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

import model.InvoiceModel;

public class InvoiceTable extends JPanel {
	private static final int ROW_GAP = 5;

	private JPanel tablePanel;
	private JTable table;
	private InvoiceTableModel invoiceTableModel;
	private JScrollPane scrollPane;

	private TableRowSorter<InvoiceTableModel> rowSorter;

	public InvoiceTable(JPanel tablePanel, ArrayList<InvoiceModel> invoiceList) {
		this.tablePanel = tablePanel;

		invoiceTableModel = new InvoiceTableModel(invoiceList);
		table = new JTable(invoiceTableModel);

		createTablePanel();

		rowSorter = new TableRowSorter<InvoiceTableModel>((InvoiceTableModel) table.getModel());
	}

	public JTable getTable() {
		return table;
	}

	public void setData(JPanel tablePanel, ArrayList<InvoiceModel> invoiceList) {
		scrollPane.setVisible(true);
		this.tablePanel = tablePanel;
		tablePanel.add(scrollPane, BorderLayout.NORTH);

		invoiceTableModel.setData(invoiceList);
		invoiceTableModel.fireTableDataChanged();
	}

	public void removeData() {
		if (invoiceTableModel.getRowCount() > 0) {
			invoiceTableModel.removeAll();
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

		configureColumnWidths();

		// Set table properties
		table.setDefaultRenderer(Object.class, new InvoiceTableRenderer());
		table.setAutoCreateRowSorter(true);
		table.setCellSelectionEnabled(true);
		new TableKeystrokeHandler(table);

		tablePanel.setLayout(new BorderLayout());
		scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(
				new Dimension(tablePanel.getPreferredSize().width, tablePanel.getPreferredSize().height - 70));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tablePanel.add(scrollPane, BorderLayout.NORTH);
	}

	private void configureColumnWidths() {
		// Configure column widths
		table.getColumnModel().getColumn(InvoiceTableModel.INVOICE_DATE_COLUMN).setMaxWidth(94);
		table.getColumnModel().getColumn(InvoiceTableModel.INVOICE_DATE_COLUMN).setPreferredWidth(92);
		table.getColumnModel().getColumn(InvoiceTableModel.START_DATE_COLUMN).setMaxWidth(94);
		table.getColumnModel().getColumn(InvoiceTableModel.START_DATE_COLUMN).setPreferredWidth(92);
		table.getColumnModel().getColumn(InvoiceTableModel.STUDENT_NAME_COLUMN).setMaxWidth(250);
		table.getColumnModel().getColumn(InvoiceTableModel.STUDENT_NAME_COLUMN).setPreferredWidth(185);
		table.getColumnModel().getColumn(InvoiceTableModel.PAYER_NAME_COLUMN).setMaxWidth(250);
		table.getColumnModel().getColumn(InvoiceTableModel.PAYER_NAME_COLUMN).setPreferredWidth(185);
		table.getColumnModel().getColumn(InvoiceTableModel.PAYMENT_METHOD_COLUMN).setMaxWidth(200);
		table.getColumnModel().getColumn(InvoiceTableModel.PAYMENT_METHOD_COLUMN).setPreferredWidth(120);
		table.getColumnModel().getColumn(InvoiceTableModel.AMOUNT_COLUMN).setMaxWidth(200);
		table.getColumnModel().getColumn(InvoiceTableModel.TRANSACTION_ID_COLUMN).setMaxWidth(200);
		table.getColumnModel().getColumn(InvoiceTableModel.TRANSACTION_ID_COLUMN).setPreferredWidth(170);
		table.getColumnModel().getColumn(InvoiceTableModel.ITEM_NAME_COLUMN).setPreferredWidth(410);
		table.getColumnModel().getColumn(InvoiceTableModel.IS_CANCELED_COLUMN).setMaxWidth(20);
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

	public class InvoiceTableRenderer extends JLabel implements TableCellRenderer {
		private InvoiceTableRenderer() {
			super();
			super.setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			setText((String) value);

			if (column != -1) {
				super.setFont(CustomFonts.TABLE_TEXT_FONT);
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