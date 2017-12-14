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
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellRenderer;

import model.InvoiceModel;

public class InvoiceTable extends JPanel {
	private static final int ROW_GAP = 5;

	private JPanel tablePanel;
	private JTable table;
	private InvoiceTableModel invoiceTableModel;
	private JScrollPane scrollPane;

	public InvoiceTable(JPanel tablePanel, ArrayList<InvoiceModel> invoiceList) {
		this.tablePanel = tablePanel;

		invoiceTableModel = new InvoiceTableModel(invoiceList);
		table = new JTable(invoiceTableModel);

		createTablePanel();
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
		table.getColumnModel().getColumn(InvoiceTableModel.INVOICE_DATE_COLUMN).setPreferredWidth(94);
		table.getColumnModel().getColumn(InvoiceTableModel.START_DATE_COLUMN).setMaxWidth(94);
		table.getColumnModel().getColumn(InvoiceTableModel.START_DATE_COLUMN).setPreferredWidth(94);
		table.getColumnModel().getColumn(InvoiceTableModel.END_DATE_COLUMN).setMaxWidth(94);
		table.getColumnModel().getColumn(InvoiceTableModel.END_DATE_COLUMN).setPreferredWidth(94);
		table.getColumnModel().getColumn(InvoiceTableModel.STUDENT_NAME_COLUMN).setMaxWidth(250);
		table.getColumnModel().getColumn(InvoiceTableModel.STUDENT_NAME_COLUMN).setPreferredWidth(200);
		table.getColumnModel().getColumn(InvoiceTableModel.PAYER_NAME_COLUMN).setMaxWidth(250);
		table.getColumnModel().getColumn(InvoiceTableModel.PAYER_NAME_COLUMN).setPreferredWidth(200);
		table.getColumnModel().getColumn(InvoiceTableModel.PAYMENT_METHOD_COLUMN).setMaxWidth(200);
		table.getColumnModel().getColumn(InvoiceTableModel.PAYMENT_METHOD_COLUMN).setPreferredWidth(120);
		table.getColumnModel().getColumn(InvoiceTableModel.AMOUNT_COLUMN).setMaxWidth(200);
		table.getColumnModel().getColumn(InvoiceTableModel.TRANSACTION_ID_COLUMN).setMinWidth(190);
		table.getColumnModel().getColumn(InvoiceTableModel.ITEM_NAME_COLUMN).setMinWidth(250);
		table.getColumnModel().getColumn(InvoiceTableModel.CLIENT_ID_COLUMN).setMinWidth(76);
		table.getColumnModel().getColumn(InvoiceTableModel.CLIENT_ID_COLUMN).setMaxWidth(80);
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
				super.setBackground(CustomFonts.UNSELECTED_BACKGROUND_COLOR);
				super.setHorizontalAlignment(CENTER);
			}
			return this;
		}
	}
}