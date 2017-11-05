package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;

public class NotesWindow {
	public static final int MENU_DESCRIPTION = 0;
	public static final int EXAMPLES = 1;
	public static final int FEEDBACK = 2;
	public static final int ABOUT = 3;

	private static final String[] notes = {
			// Menu Description
			"\n   File Menu:\r\n"
					+ "      => The League Teachers will not typically need to use any functionality from the File Menu, since the imports \r\n"
					+ "            of client, attendance and Github data are retrieved automatically on a daily basis. \r\n"
					+ "      => The only interesting File menu item would be viewing the LOG file which shows the results of the daily imports.\r\n\r\n"
					+ "   Student Menu:\r\n      => The Student menu shows basic information for each student. \r\n"
					+ "      => Using the Right Mouse button, you can view the student's attendance data. From there you can use the \r\n"
					+ "            Right Button on the student name to get back to the Student Data, or double click the Attendance Data to get \r\n"
					+ "            more history on the data.\r\n\r\n   Attendance Menu:\r\n"
					+ "      => The Attendance menu allows you to view ALL attendance data or to view attendance by class.\r\n"
					+ "      => You can right-click on the user name to get the Student Info.\r\n"
					+ "      => Double-click on the attendance column to get the complete attendance history for a student.\r\n"
					+ "      => Right-click on any of the attendance rows to get attendance data by Class to determine what that class has been\r\n"
					+ "            working on for the last 4 weeks. \r\n\r\n",

			// Examples
			"\n   YOU ARE A SUB FOR A CLASS:\r\n        Go to the Attendance menu and select by Class. \r\n"
					+ "        This will show you what the class has been doing for the last 4 weeks. \r\n"
					+ "        If more detail is needed, double click on one of the students to get more of a history.\r\n\r\n"
					+ "   YOU ARE DOING A MAKE-UP SESSION:\r\n"
					+ "        Go to the Student menu and find the student. \r\n"
					+ "        Right-click to select their Attendance data. \r\n"
					+ "        Then right-click in the Github column to view what their class has been doing.	\r\n",

			// Feedback
			"\n                This is a work in progress, and we're in Phase I. Currently this is a Java App \r\n"
					+ "                                        running as a JAR file with a Swing GUI. \r\n\r\n"
					+ "                 Once we've established proof-of-concept and have gotten positive feedback,  \r\n"
					+ "       the GUI will be ported to the web and this will become a web-app downloadable to your device.\r\n\r\n"
					+ "                                    !!! WE APPRECIATE YOUR FEEDBACK !!!\r\n\r\n"
					+ "    *** Please send any feedback, suggestions and bug reports to wendy.avis@jointheleague.org *** \r\n",

			// About League Data Manager
			"\n   VERSION 1.0\r\n\r\n   LEAGUE DATA MANAGER OVERVIEW:\r\n\r\n"
					+ "      The League Data Manager gets data from:\r\n         => Pike13 client database\r\n"
					+ "         => Pike13 enrollment database\r\n         => Github \r\n\r\n"
					+ "      This data is then merged into a single database. \r\n"
					+ "      This merged database is a slave to all 3 of the above databases, and does not create any new data on its own.\r\n"
					+ "      Once every 24 hours, the application will search for all completed attendance for that day, which triggers a search \r\n"
					+ "          in Github for the related student comments and updates the database accordingly. \r\n\r\n"
					+ "      As you will see when you use this Application, data IN is data OUT. So it is very important to emphasize to your \r\n"
					+ "          students that they need to write descriptive comments  when they sync to Github!!\r\n"
					+ "      If it helps, show them this Application, and have them realize that their comments are viewable by the teachers!!\r\n"
					+ "      Naming conventions are to write each recipe that the student worked on separated by commas, and followed by (IP) \r\n"
					+ "          if the recipe is still In Progress. Add comments about written tests and worksheets (WS) also.\r\n" };

	private static final String[] titles = { "Menu Description ", "Examples ", "Provide Feedback ",
			"About League Data Manager " };
	
	private static final Dimension[] sizes = { new Dimension(820, 425), new Dimension(630, 280),
			new Dimension(720, 270), new Dimension(825, 470) };

	public NotesWindow(int notesSelection) {
		JFrame frame = new JFrame(titles[notesSelection]);
		JPanel panel = new JPanel();
		JTextArea notesArea = new JTextArea();

		notesArea.setEditable(false);
		notesArea.setText(notes[notesSelection]);
		notesArea.setFont(CustomFonts.NOTES_WINDOW_FONT);
		notesArea.setLineWrap(true);
		notesArea.setPreferredSize(new Dimension(sizes[notesSelection].width - 20, sizes[notesSelection].height - 40));
		panel.add(notesArea);

		// Set panel size and borders
		panel.setPreferredSize(new Dimension(sizes[notesSelection].width - 10, sizes[notesSelection].height - 20));
		Border innerBorder = BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true);
		Border outerBorder = BorderFactory.createEmptyBorder(5, 1, 1, 1);
		panel.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		frame.add(panel, BorderLayout.CENTER);

		// Configure and show frame
		ImageIcon icon = new ImageIcon(getClass().getResource("PPicon24_Color_F16412.png"));
		frame.setIconImage(icon.getImage());
		frame.setResizable(false);
		frame.setSize(sizes[notesSelection].width, sizes[notesSelection].height);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}
