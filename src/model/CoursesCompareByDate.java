package model;

import java.util.Comparator;

public class CoursesCompareByDate implements Comparator {
	@Override
    public int compare(Object o1, Object o2) {
		CoursesModel c1 = (CoursesModel) o1;
		CoursesModel c2 = (CoursesModel) o2;
		
    	int comp = c1.getDate().compareTo(c2.getDate());
    	if (comp == 0) {
    		comp = c1.getEventName().compareTo(c2.getEventName());
    	}
    	return comp;
    }
}
