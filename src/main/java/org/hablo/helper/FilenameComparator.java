package org.hablo.helper;

import java.io.File;
import java.util.Comparator;

public final class FilenameComparator
        implements Comparator<File> {

    @Override
    public int compare(File o1, File o2) {
        String s1 = o1.getName();
        String s2 = o2.getName();
        final int s1Dot = s1.lastIndexOf('.');
        final int s2Dot = s2.lastIndexOf('.');
        if ((s1Dot == -1) == (s2Dot == -1)) { // both or neither
            s1 = s1.substring(s1Dot + 1);
            s2 = s2.substring(s2Dot + 1);
            return s1.compareTo(s2);
        } else if (s1Dot == -1) { // only s2 has an extension, so s1 goes first
            return -1;
        } else { // only s1 has an extension, so s1 goes second
            return 1;
        }
    }
}
