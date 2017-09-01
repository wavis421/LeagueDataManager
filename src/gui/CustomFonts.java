package gui;

import java.awt.Color;
import java.awt.Font;

import acm.util.JTFTools;

public class CustomFonts {
	// Color selections
	public static final Color TITLE_COLOR = new Color(0x0022DD);
	public static final Color ICON_COLOR = new Color(0x196F3D);
	public static final Color DAYBOX_HIGHLIGHT_COLOR = new Color(0xF00000);
	public static final Color EMPTY_BACKGROUND_COLOR = new Color(0xDDDDDD);
	public static final Color SELECTED_BACKGROUND_COLOR = new Color(0xDDDDDD);
	public static final Color UNSELECTED_BACKGROUND_COLOR = Color.WHITE;
	public static final Color EDIT_TEXT_COLOR = new Color(0x00994C);
	public static final Color DEFAULT_TEXT_COLOR = Color.BLACK;

	// Calendar panel fonts
	public static final Font TITLE_FONT = JTFTools.decodeFont("Serif-italic-36");
	public static final Font CAL_TITLE_BOLD_FONT = JTFTools.decodeFont("Serif-bold-36");
	public static final Font CAL_SUBTITLE_FONT = JTFTools.decodeFont("Serif-italic-22");
	public static final Font CAL_BOLD_DEFAULT_FONT = JTFTools.decodeFont("Serif-bold-14");
	public static final Font CAL_ITALIC_DEFAULT_FONT = JTFTools.decodeFont("Serif-italic-14");
	public static final Font CAL_ITALIC_SMALL_FONT = JTFTools.decodeFont("Serif-italic-13");
	public static final Font CAL_DATE_FONT = JTFTools.decodeFont("Serif-11");

	// Table fonts
	public static final Font TABLE_HEADER_FONT = JTFTools.decodeFont("Dialog-bold-15");
	public static final Font TABLE_TEXT_FONT = JTFTools.decodeFont("Dialog-bold-12");
}
