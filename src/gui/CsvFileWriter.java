package gui;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.TableModel;

public class CsvFileWriter {
	public static void writeTableToCsvFile(Component parent, TableModel model) {
		Method getCsvFileHdr = null;
		Method getCsvDataList = null;
		Method convertItemToCsv = null;

		// First determine whether CSV export is supported for this table
		try {
			getCsvFileHdr = model.getClass().getDeclaredMethod("getCsvFileHeader");
			getCsvDataList = model.getClass().getDeclaredMethod("getCsvDataList");
			convertItemToCsv = model.getClass().getDeclaredMethod("convertItemToCsv", Object.class);

		} catch (NoSuchMethodException | SecurityException e) {
			JOptionPane.showMessageDialog(parent, "CSV Export not supported for this table", "CSV Export Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Select file
		File file = null;
		JFileChooser fileChooser = new JFileChooser();
		if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
		}
		if (file == null) {
			return;
		}

		// Write records to CSV file
		FileWriter fileWriter = null;
		try {
			// Write the CSV file header
			fileWriter = new FileWriter(file);
			fileWriter.append((CharSequence) getCsvFileHdr.invoke(model));
			fileWriter.append("\n");

			// Write each object in list to the CSV file
			ArrayList<Object> list = (ArrayList<Object>) getCsvDataList.invoke(model);
			for (Object item : list) {
				fileWriter.append((CharSequence) convertItemToCsv.invoke(model, item));
				fileWriter.append((CharSequence) "\n");
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent, e.getMessage(), "CSV File Write Error", JOptionPane.ERROR_MESSAGE);

		} finally {
			try {
				if (fileWriter != null) {
					fileWriter.flush();
					fileWriter.close();
				}

			} catch (IOException e) {
				// Don't care
			}
		}
	}
}
