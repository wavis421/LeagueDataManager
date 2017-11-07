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
			"\n   File Menu:\n"
					+ "      => The View Log Data shows the results of the daily imports of student attendance data.\n\n"
					+ "   Student Menu:\n      => The Student menu shows basic information for ALL students and for inactive students.\n"
					+ "            If a student name is italicized, this indicates that the student is currently inactive.\n"
					+ "      => You can select a student and use the Right Mouse button to view the student's attendance data.\n"
					+ "            See below for more details.\n\n" + "   Attendance Menu:\n"
					+ "      => The Attendance menu allows you to view ALL attendance data and to view attendance by class.\n"
					+ "      => You can right-click on the user name to get the Student Info.\n"
					+ "      => Double-click on the attendance column to get the complete attendance history for a student.\n"
					+ "      => Right-click on any of the attendance rows to get attendance data by Class to determine what that class has been\n"
					+ "            working on for the last 4 weeks. \n\n",

			// Examples
			"\n   YOU ARE A SUB FOR A CLASS:\n        Go to the Attendance menu and select by Class. \n"
					+ "        This will show you what the class has been doing for the last 4 weeks. \n"
					+ "        If more detail is needed, double click on one of the students to get more of a history.\n\n"
					+ "   YOU ARE DOING A MAKE-UP SESSION:\n"
					+ "        Go to the Student menu and find the student. \n"
					+ "        Right-click to select their Attendance data. \n"
					+ "        Then right-click in the Github column to view what their class has been doing.	\n",

			// Feedback
			"\n                                         This is a work in progress, and we're in Phase I.\n"
					+ "                                        Currently this is a Java App running as a JAR file.\n\n"
					+ "                 Once we've established proof-of-concept and have gotten positive feedback,  \n"
					+ "       the GUI will be ported to the web and this will become a web-app downloadable to your device.\n\n"
					+ "                                      !!! WE APPRECIATE YOUR FEEDBACK !!!\n\n"
					+ "    *** Please send any feedback, suggestions and bug reports to wendy.avis@jointheleague.org *** \n",

			// About League Data Manager
			"\n   VERSION 1.0\n\n   LEAGUE DATA MANAGER OVERVIEW:\n\n"
					+ "      The League Data Manager gets data from:\n         => Pike13 client database\n"
					+ "         => Pike13 enrollment database\n         => Github \n\n"
					+ "      This data is then merged into a single database. \n"
					+ "      This merged database is a slave to all 3 of the above databases, and does not create any new data on its own.\n"
					+ "      Once every 24 hours, the application will search for all completed attendance for that day, which triggers a search \n"
					+ "          in Github for the related student comments and updates the database accordingly. \n\n"
					+ "      As you will see when you use this Application, data IN is data OUT. So it is very important to emphasize to your \n"
					+ "          students that they need to write descriptive comments  when they sync to Github!!\n"
					+ "      If it helps, show them this Application, and have them realize that their comments are viewable by the teachers!!\n\n"
					+ "      Naming conventions are to write each recipe that the student worked on separated by commas, and followed by (IP) \n"
					+ "          if the recipe is still In Progress. Please also add comments about written tests and worksheets (WS).\n" };

	private static final String[] titles = { "Menu Description ", "Examples ", "Provide Feedback ",
			"About League Data Manager " };

	private static final Dimension[] sizes = { new Dimension(820, 420), new Dimension(630, 280),
			new Dimension(720, 270), new Dimension(825, 490) };

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
