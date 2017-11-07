package gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class PasswordDialog extends JDialog implements KeyListener {
	String password;
	JPasswordField pwField = new JPasswordField(15);

	public PasswordDialog() {
		setModal(true);

		// Create password panel
		JPanel panel = new JPanel();
		pwField.addKeyListener(this);
		panel.add(pwField);

		// Add panel to dialog
		add(panel);

		// Configure dialog window
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Enter password");
		setSize(300, 100);
		setLocation(200, 200);
		setVisible(true);
	}

	public String getDialogResponse() {
		return password;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			String pw = pwField.getText();
			if (!pw.equals("")) {
				// Save password and dispose the dialog.
				password = pw;
				setVisible(false);
				dispose();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}
}
