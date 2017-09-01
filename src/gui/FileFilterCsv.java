package gui;
import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FileFilterCsv extends FileFilter {

	public boolean accept(File file) {
		if (file.isDirectory())
			return true;

		String name = file.getName();
		int index = name.lastIndexOf(".");
		if (index == -1)
			return false;
		if (index == name.length() - 1)
			return false;

		String ext = name.substring(index + 1, name.length());
		if (ext.equals("csv"))
			return true;

		return false;
	}

	public String getDescription() {
		return "CSV files (*.csv)";
	}
}

