package gui;

import java.awt.Color;
import java.awt.Font;

import acm.util.JTFTools;

public class CustomFonts {
	// Color selections
	public static final Color TITLE_COLOR = new Color(0xF16412);
	public static final Color ICON_COLOR = new Color(0xF16412);  // League orange?
	public static final Color DAYBOX_HIGHLIGHT_COLOR = new Color(0xF00000);
	public static final Color EMPTY_BACKGROUND_COLOR = new Color(0xDDDDDD);
	public static final Color SELECTED_BACKGROUND_COLOR = new Color(0xDDDDDD);
	public static final Color UNSELECTED_BACKGROUND_COLOR = Color.WHITE;
	public static final Color EDIT_TEXT_COLOR = new Color(0x00994C);
	public static final Color DEFAULT_TEXT_COLOR = Color.BLACK;
	public static final Color TABLE_GRID_COLOR = new Color(0xA0A0A0);

	// Header and title fonts
	public static final Font TITLE_FONT = JTFTools.decodeFont("Serif-italic-24");
	public static final Font PANEL_HEADER_FONT = JTFTools.decodeFont("Serif-italic-20");
	public static final Font NOTES_WINDOW_FONT = JTFTools.decodeFont("Serif-bold-15");

	// Table fonts
	public static final Font TABLE_HEADER_FONT = JTFTools.decodeFont("Dialog-bold-15");
	public static final Font TABLE_TEXT_FONT = JTFTools.decodeFont("Dialog-bold-12");
	public static final Font TABLE_ITALIC_TEXT_FONT = JTFTools.decodeFont("Dialog-italic-12");
}
