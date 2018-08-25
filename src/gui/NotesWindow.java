package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import model.LocationLookup;

public class NotesWindow {
	public static final int FILE_MENU = 0;
	public static final int STUDENT_MENU = 1;
	public static final int ATTENDANCE_MENU = 2;
	public static final int SCHEDULE_MENU = 3;
	public static final int EXAMPLES = 4;
	public static final int SEARCH_COPY = 5;
	public static final int LOCATION_CODES = 6;
	public static final int FEEDBACK = 7;
	public static final int ABOUT = 8;

	private static final String[] notes = { "\n"
			// File Menu
			+ "   File Menu:\n\n"
			+ "      => Viewing the LOG Data shows the results of the daily imports of student attendance.\n"
			+ "            Select and right click to view a student's information or attendance data.\n"
			+ "      => The Print Table menu option prints the currently displayed table. Note that the printing uses\n"
			+ "            auto-fit width, so if you're printing attendance data then landscape will be your best option.\n"
			+ "      => The Export Data to CSV file option allows you to export the currently displayed table to\n"
			+ "            a CSV file, which can then be imported to an Excel spreadsheet. The tables that can\n"
			+ "            be exported include the Student, Courses, Graduation, Missing Github and Log Data tables.",
			
			// Student Menu
			"     Student Menu:\n\n"
			+ "      => The Student menu shows basic information for ALL students as well as for inactive students.\n"
			+ "            If a student name is italicized, this indicates that the student is currently inactive.\n"
			+ "      => 'View students without recent Github' shows students who have attended at least 3 out of the\n"
			+ "            last 4 weeks of classes without any Github commits. This indicates that either the student's\n"
			+ "            Github user name is incorrect or that the student is not committing his/her work.\n"
			+ "      => 'View pending graduates' shows a list of students who have recently been graduated. They are\n"
			+ "            then removed from the list once they have been assigned to a new class/level.\n"
			+ "      => From the Student tables, select a student and then use the Right Mouse button to viewthe student's\n"
			+ "            attendance data. Refer to the Attendance menu description for more details regarding attendance.",
			
			// Attendance Menu
			"     Attendance Menu:\n\n"
			+ "      => The Attendance menu allows you to view ALL attendance data and to view attendance by class.\n"
			+ "      => From the attendance table, select and right-click the user name to get the Student Info or\n"
			+ "            Student Attendance.\n"
			+ "      => Double-click on the right column to get the complete attendance history for a student.\n"
			+ "      => Select and right-click on any of the attendance rows to get attendance data by Class to \n"
			+ "            determine what that class has been working on for the last 4 weeks.",
			
			// Schedule Menu
			"     Schedule Menu:\n\n"
			+ "      => View the weekly class schedule to show the class schedule for each day of the week.\n"
			+ "            Then select a class and right click to view the attendance data for that class.\n"
			+ "            Note that you can filter the Schedule data using the search box, e.g. all 'Make-Up' classes.\n"
			+ "      => Viewing Workshops and Summer Slam shows all the courses available. From there, select\n"
			+ "            and then right-click a course to view the students enrolled in the course.",

			// Examples
			"\n   YOU ARE A SUB FOR A CLASS:\n"
			+ "        There are two ways to select a class that you want to view:\n"
			+ "        1) From the Attendance menu, select 'View by Class', then select the level\n"
			+ "            and the class you're interested in.\n"
			+ "        2) If you do not know the class name, go to the Schedule menu and select,\n"
			+ "            then right-click the class by day-of-week and time.\n\n"
			+ "        This will show you what the class has been doing for the last 4 weeks. \n"
			+ "        If more detail is needed, double-click the right column for the student\n"
			+ "        to get more attendance history.\n\n" 
			+ "   YOU ARE DOING A MAKE-UP SESSION:\n"
			+ "        Go to the Attendance menu and find the student using the search filter.\n"
			+ "        Select the class you want to view from the right menu, then right-click \n"
			+ "        to show what this class has been doing for the last 4 weeks.\n\n"
			+ "        Alternatively, if you know the day-of-week and time of the student's regular class,\n"
			+ "        then you can access the class data from the Schedule menu.",

			// Search and copy features
			"     Search, Filter and Copy Features:\n\n"
			+ "      => All tables have the ability to do a search filter. Enter text in the Search box to display only the\n"
			+ "            rows in the table where one of the fields starts with this text. Words are delineated by (, -, :, / etc.\n"
			+ "            The only exception is the Attendance table where the right column is not searched.\n\n"
			+ "      => A cell or multiple contiguous cells can be selected and then copied. Double-click on a cell to select the\n"
			+ "            entire row. Cells are copied in CSV format so that the copied cells can be imported into Excel if\n"
			+ "            desired. The only exception is the Attendance table where the right column is not copied using CSV format.\n"
			+ "            Note that if selected cells are non-contiguous, they will instead be copied with standard tab separators.",
			
			// Location Codes
			"\n     LOCATION CODES:\n\n",
			
			// Feedback
			"\n\n\n"
			+ "                                       !!! WE APPRECIATE YOUR FEEDBACK !!!\n\n"
			+ "      *** Please send any feedback, suggestions or bug reports to wendy.avis@jointheleague.org *** \n",
			
			// About League Student Tracker
			"\n   VERSION 4.6G (G = includes Graduation Dialog, Limited Distribution) \n\n"
			+ "   LEAGUE STUDENT TRACKER OVERVIEW: \n\n"
			+ "      The League Student Tracker gets data from:\n" 
			+ "         => Pike13 client database\n"
			+ "         => Pike13 attendance database\n" 
			+ "         => Pike13 schedule database\n"
			+ "         => Github \n\n"
			+ "      This data is then merged into a single database. \n"
			+ "      This merged database is a slave to all of the above databases, and does not create any new data on its own.\n"
			+ "      Once every 24 hours, the application searches for all completed attendance for that day, which triggers a search \n"
			+ "          in Github for the related student comments and updates the database accordingly. \n\n"
			+ "      As you will see when you use this Application, data IN is data OUT. So it is very important to emphasize to your \n"
			+ "          students that they need to write descriptive comments  when they sync to Github!!\n"
			+ "      If it helps, show them this Application, and have them realize that their comments are viewable by the teachers!!\n\n"
			+ "      Naming conventions are to write each recipe that the student worked on separated by commas, and followed by (IP) \n"
			+ "          if the recipe is still In Progress. Please also add comments about written tests and worksheets (WS).\n" };

	private static final String[] titles = { "File Menu Description ", "Student Menu Description ", "Attendance Menu Description ", 
			"Schedule Menu Description ", "Examples ", "Search, Filter and Copy Features", 
			"Location Codes ", "Provide Feedback ",	"About League Student Tracker " };

	private static final Dimension[] sizes = { new Dimension(820, 275), new Dimension(820, 300), new Dimension(820, 275), 
			new Dimension(820, 275), new Dimension(630, 440), new Dimension(820, 300), new Dimension(680, 720), 
			new Dimension(720, 270), new Dimension(825, 520) };

	public NotesWindow(int notesSelection) {
		JFrame frame = new JFrame(titles[notesSelection]);
		JPanel panel = new JPanel();
		JTextArea notesArea = new JTextArea();

		notesArea.setEditable(false);
		if (notesSelection == LOCATION_CODES)
			notesArea.setText(notes[notesSelection] + LocationLookup.getAllLocsForDisplay());
		else
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
