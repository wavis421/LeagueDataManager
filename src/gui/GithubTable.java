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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellRenderer;

import model.GithubModel;

public class GithubTable extends JPanel {
	private static final int ROW_GAP = 5;

	private static final int POPUP_WIDTH = 240;
	private static final int POPUP_HEIGHT_2ROWS = 50;

	private JPanel tablePanel;
	private JTable table;
	private GithubTableModel githubTableModel;
	private TableListeners githubListener;
	private JScrollPane scrollPane;

	public GithubTable(JPanel tablePanel, ArrayList<GithubModel> arrayList) {
		this.tablePanel = tablePanel;

		githubTableModel = new GithubTableModel(arrayList);
		table = new JTable(githubTableModel);

		createTablePanel();
		createGithubTablePopups();
	}

	public void setTableListener(TableListeners listener) {
		this.githubListener = listener;
	}

	public JTable getTable() {
		return table;
	}

	public void setData(JPanel tablePanel, ArrayList<GithubModel> githubList) {
		scrollPane.setVisible(true);
		this.tablePanel = tablePanel;
		tablePanel.add(scrollPane, BorderLayout.NORTH);

		githubTableModel.setData(githubList);
		githubTableModel.fireTableDataChanged();
	}

	public void removeData() {
		if (githubTableModel.getRowCount() > 0) {
			githubTableModel.removeAll();
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
		table.getColumnModel().getColumn(GithubTableModel.CLIENT_ID_COLUMN).setMaxWidth(75);
		table.getColumnModel().getColumn(GithubTableModel.DOW_COLUMN).setMaxWidth(200);
		table.getColumnModel().getColumn(GithubTableModel.DOW_COLUMN).setPreferredWidth(200);

		table.setDefaultRenderer(Object.class, new GithubTableRenderer());
		table.setAutoCreateRowSorter(true);

		tablePanel.setLayout(new BorderLayout());
		scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(0, tablePanel.getPreferredSize().height - 70));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		tablePanel.add(scrollPane, BorderLayout.NORTH);
	}

	private void createGithubTablePopups() {
		// Table panel POP UP menu
		JPopupMenu tablePopup = new JPopupMenu();
		JMenuItem showStudentAttendanceItem = new JMenuItem("Show attendance ");
		JMenuItem updateGithubUserItem = new JMenuItem("Update Github user name ");
		tablePopup.add(showStudentAttendanceItem);
		tablePopup.add(updateGithubUserItem);
		tablePopup.setPreferredSize(new Dimension(POPUP_WIDTH, POPUP_HEIGHT_2ROWS));

		// POP UP action listeners
		updateGithubUserItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get row, model, and clientID for the row
				int row = table.convertRowIndexToModel(table.getSelectedRow());
				GithubTableModel model = (GithubTableModel) table.getModel();
				String clientID = (String) model.getValueAt(row, GithubTableModel.CLIENT_ID_COLUMN);
				String name = (String) model.getValueAt(row, GithubTableModel.STUDENT_NAME_COLUMN);

				// Send email to LeagueBot with new github user name
				githubListener.updateGithubUser(clientID, name);
				table.clearSelection();
			}
		});
		showStudentAttendanceItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get student name for selected row/column
				int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
				GithubTableModel model = (GithubTableModel) table.getModel();
				String clientID = (String) model.getValueAt(modelRow, GithubTableModel.CLIENT_ID_COLUMN);
				String studentName = (String) model.getValueAt(modelRow, GithubTableModel.STUDENT_NAME_COLUMN);

				// Display attendance table for selected student
				table.clearSelection();
				System.out.println("Show attendance for " + clientID + ", " + studentName);
				githubListener.viewAttendanceByStudent(clientID, studentName);
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
		});
	}

	public class GithubTableRenderer extends JLabel implements TableCellRenderer {
		private GithubTableRenderer() {
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
				super.setBorder(BorderFactory.createEmptyBorder());

				if (((GithubTableModel) table.getModel()).getValueByRow(row).getGithubName().equals("")) {
					// Text RED for students missing data
					if (column == GithubTableModel.STUDENT_NAME_COLUMN)
						super.setForeground(Color.red);

					// Border RED for cells with missing data
					if (text == null || text.equals(""))
						super.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.red));
				}

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
