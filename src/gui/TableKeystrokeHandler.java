package gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTable;

/**
 * KeyAdapter to detect Windows standard copy keystroke (Ctrl-C) on a JTable and
 * put it on the clipboard in a friendly plain text format. Rows are copied to
 * clipboard in CSV format with line break appended to each row.
 * 
 */
public class TableKeystrokeHandler extends KeyAdapter {

	private static final String LINE_BREAK = "\n";
	private static final String CELL_BREAK = ", ";
	private static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

	private final JTable table;

	public TableKeystrokeHandler(JTable table) {
		this.table = table;
		table.addKeyListener(this);
	}

	@Override
	public void keyReleased(KeyEvent event) {
		if (event.isControlDown()) {
			if (event.getKeyCode() == KeyEvent.VK_C) { // Copy
				cancelEditing();
				copyToClipboard();
			}
		}
	}

	private void copyToClipboard() {
		int numCols = table.getSelectedColumnCount();
		int numRows = table.getSelectedRowCount();
		int[] rowsSelected = table.getSelectedRows();
		int[] colsSelected = table.getSelectedColumns();
		if (numRows != rowsSelected[rowsSelected.length - 1] - rowsSelected[0] + 1 || numRows != rowsSelected.length
				|| numCols != colsSelected[colsSelected.length - 1] - colsSelected[0] + 1
				|| numCols != colsSelected.length) {

			System.out.println("Invalid Copy Selection");
			return;
		}

		StringBuffer copyStr = new StringBuffer();
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				Object value = table.getValueAt(rowsSelected[i], colsSelected[j]);
				if (value == null)
					value = "";
				copyStr.append(value);
				if (j < (numCols - 1)) {
					copyStr.append(CELL_BREAK);
				}
			}
			if (i < (numRows - 1))
				copyStr.append(LINE_BREAK);
		}
		StringSelection sel = new StringSelection(copyStr.toString());
		CLIPBOARD.setContents(sel, sel);
	}

	private void cancelEditing() {
		if (table.getCellEditor() != null) {
			table.getCellEditor().cancelCellEditing();
		}
	}
}
