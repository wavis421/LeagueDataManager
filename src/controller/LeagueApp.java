package controller;

import javax.swing.SwingUtilities;

import gui.MainFrame;

public class LeagueApp {
public static void main(String[] args) {
	// Start application from MainFrame
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
			new MainFrame();
		}
	});
}
}
