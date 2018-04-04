package gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class GithubUserDialog extends JDialog implements ActionListener {
	JTextField emailUserField;
	JPasswordField emailPwField;
	JTextField githubField;
	JButton cancelButton;
	JButton okButton;
	String clientID, studentName;

	public GithubUserDialog(String clientID, String studentName) {
		setModal(true);
		
		this.clientID = clientID;
		this.studentName = studentName;

		// Create panel for github user text box and email info
		JPanel panel = new JPanel();

		// Create labels
		JLabel emailUserLabel = new JLabel("Sender email: ");
		JLabel emailPwLabel = new JLabel("Email password: ");
		JLabel githubLabel = new JLabel("New Github user name: ");
		JLabel spacerLabel = new JLabel("      ");

		// Set label sizes
		Dimension labelDimension = new Dimension(140, emailUserLabel.getPreferredSize().height);
		emailUserLabel.setPreferredSize(labelDimension);
		emailPwLabel.setPreferredSize(labelDimension);
		githubLabel.setPreferredSize(labelDimension);
		spacerLabel.setPreferredSize(new Dimension(400, emailUserLabel.getPreferredSize().height));

		// Right justify the label text
		emailUserLabel.setHorizontalAlignment(JLabel.RIGHT);
		emailPwLabel.setHorizontalAlignment(JLabel.RIGHT);
		githubLabel.setHorizontalAlignment(JLabel.RIGHT);

		// Create text input fields
		emailUserField = new JTextField(25);
		emailPwField = new JPasswordField(25);
		githubField = new JTextField(25);
		emailUserField.setText("@jointheleague.org");
		emailUserField.setCaretPosition(0);

		// Add orange borders for all input fields
		Border innerBorder = BorderFactory.createLineBorder(CustomFonts.TITLE_COLOR, 2, true);
		Border outerBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		emailUserField.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		emailPwField.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		githubField.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));

		// Create OK/Cancel buttons
		cancelButton = new JButton("Cancel");
		okButton = new JButton("Send email to League");

		// Add all of the above to the panel
		panel.add(emailUserLabel);
		panel.add(emailUserField);
		panel.add(emailPwLabel);
		panel.add(emailPwField);
		panel.add(githubLabel);
		panel.add(githubField);
		panel.add(spacerLabel);
		panel.add(cancelButton);
		panel.add(okButton);

		// Add panel to dialog
		add(panel);

		// Add listener to buttons
		cancelButton.addActionListener(this);
		okButton.addActionListener(this);

		// Set icon
		ImageIcon icon = new ImageIcon(getClass().getResource("PPicon24_Color_F16412.png"));
		setIconImage(icon.getImage());

		// Configure dialog window
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Update Github user for " + studentName);
		setSize(480, 200);
		setLocation(200, 200);
		setResizable(false);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			String pw = emailPwField.getText();
			
			// Check that all fields are filled in, then create body and send email
			if (!pw.equals("") && !emailUserField.getText().equals("") && !githubField.getText().equals("")) {
				String body = "Client ID: " + clientID + "\nStudent Name: " + studentName + "\nNew Github user name: "
						+ githubField.getText();
				generateAndSendEmail(emailUserField.getText(), pw, body);

				setVisible(false);
				dispose();
			}

		} else {
			setVisible(false);
			dispose();
		}
	}

	private void generateAndSendEmail(String emailUser, String emailPassword, String emailBody) {
		// Currently hard-coded to send using gmail SMTP
		Properties properties = System.getProperties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.socketFactory.port", "465");
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.port", "465");

		Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(emailUser, new String(emailPassword));
			}
		});

		// Set cursor to "wait" cursor
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			// Set message fields
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(emailUser));
			message.setSubject("Update Github user name");
			message.setText(emailBody, "utf-8");
			message.setSentDate(new Date());

			// Set email recipient
			message.addRecipient(Message.RecipientType.TO, new InternetAddress("leaguebot@jointheleague.org"));

			// Send email
			Transport.send(message);

		} catch (MessagingException e) {
			JOptionPane.showMessageDialog(getParent(), "Failure sending email to leaguebot: " + e.getMessage());
		}

		// Set cursor back to default
		this.setCursor(Cursor.getDefaultCursor());
	}
}
