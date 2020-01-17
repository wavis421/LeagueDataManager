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

import model_for_gui.GithubModel;

public class GithubTable extends JPanel {
	private static final int ROW_GAP = 5;

	private static final int POPUP_WIDTH = 240;
	private static final int POPUP_HEIGHT_4ROWS = 90;

	private JPanel tablePanel;
	private JTable table;
	private GithubTableModel githubTableModel;
	private TableListeners githubListener;
	private JScrollPane scrollPane;

	private TableRowSorter<GithubTableModel> rowSorter;
	private List<? extends SortKey> defaultSortKeys;

	public GithubTable(JPanel tablePanel, ArrayList<GithubModel> arrayList) {
		this.tablePanel = tablePanel;

		githubTableModel = new GithubTableModel(arrayList);
		table = new JTable(githubTableModel);

		createTablePanel();
		createGithubTablePopups();

		rowSorter = new TableRowSorter<GithubTableModel>((GithubTableModel) table.getModel());
		defaultSortKeys = rowSorter.getSortKeys();
		table.setAutoCreateRowSorter(true);
		table.setRowSorter(rowSorter);
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

		rowSorter.setSortKeys(defaultSortKeys);

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
		table.getTableHeader().setDefaultRenderer(new GithubTableHeaderRenderer());
		int origRowHeight = table.getRowHeight();
		table.setRowHeight(origRowHeight + ROW_GAP);

		// Configure column widths
		table.getColumnModel().getColumn(GithubTableModel.CLIENT_ID_COLUMN).setMaxWidth(75);
		table.getColumnModel().getColumn(GithubTableModel.STUDENT_LEVEL_COLUMN).setMaxWidth(60);
		table.getColumnModel().getColumn(GithubTableModel.DOW_COLUMN).setMaxWidth(130);
		table.getColumnModel().getColumn(GithubTableModel.DOW_COLUMN).setPreferredWidth(130);
		table.getColumnModel().getColumn(GithubTableModel.CLASS_NAME_COLUMN).setMaxWidth(200);
		table.getColumnModel().getColumn(GithubTableModel.CLASS_NAME_COLUMN).setPreferredWidth(170);

		table.setDefaultRenderer(Object.class, new GithubTableRenderer());
		table.setCellSelectionEnabled(true);
		new TableKeystrokeHandler(table);

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
		JMenuItem showStudentEmailItem = new JMenuItem("Show student email ");
		JMenuItem showStudentPhoneItem = new JMenuItem("Show student phone ");
		JMenuItem updateGithubUserItem = new JMenuItem("Update Github user name ");
		tablePopup.add(showStudentAttendanceItem);
		tablePopup.add(showStudentEmailItem);
		tablePopup.add(showStudentPhoneItem);
		tablePopup.add(updateGithubUserItem);
		tablePopup.setPreferredSize(new Dimension(POPUP_WIDTH, POPUP_HEIGHT_4ROWS));

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
				githubListener.viewAttendanceByStudent(clientID, studentName);
			}
		});
		showStudentEmailItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get student ClientID for selected row/column
				int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
				GithubTableModel model = (GithubTableModel) table.getModel();
				String clientID = (String) model.getValueAt(modelRow, GithubTableModel.CLIENT_ID_COLUMN);

				// Display email table for selected student
				table.clearSelection();
				githubListener.viewEmailByStudent(Integer.parseInt(clientID));
			}
		});
		showStudentPhoneItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Get student ClientID for selected row/column
				int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
				GithubTableModel model = (GithubTableModel) table.getModel();
				String clientID = (String) model.getValueAt(modelRow, GithubTableModel.CLIENT_ID_COLUMN);

				// Display phone number table for selected student
				table.clearSelection();
				githubListener.viewPhoneByStudent(Integer.parseInt(clientID));
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

	// ===== NESTED Class: Renderer for table ===== //
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

				int modelRow = table.convertRowIndexToModel(row);
				if (((GithubTableModel) table.getModel()).getValueByRow(modelRow).getGithubName().equals("")) {
					// Text ORANGE for students missing data
					if (column == GithubTableModel.STUDENT_NAME_COLUMN)
						super.setForeground(CustomFonts.TITLE_COLOR);

					// Border ORANGE for missing github
					else if (column == GithubTableModel.GITHUB_NAME_COLUMN && (text == null || text.equals("")))
						super.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, CustomFonts.TITLE_COLOR));
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

	// ===== NESTED Class: Renderer for table header ===== //
	public class GithubTableHeaderRenderer extends JLabel implements TableCellRenderer {
		Border innerBorder = BorderFactory.createLineBorder(CustomFonts.TABLE_GRID_COLOR, 2, true);
		Border outerBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

		private GithubTableHeaderRenderer() {
			super();
			super.setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.setHorizontalAlignment(CENTER);
			super.setFont(CustomFonts.TABLE_HEADER_FONT);
			super.setForeground(Color.black);
			setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));

			super.setText(((String) value));
			return (this);
		}
	}
}
