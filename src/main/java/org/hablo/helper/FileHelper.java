package org.hablo.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileHelper {
    public static File[] getFiles(String desiredFile, String parentFolder) throws Exception {
        File[] fs;
        File fp;
        //process all files under parent
        if (desiredFile == null || desiredFile.isEmpty()) {

            List<Path> filePaths = Files
                    .find(Paths.get(parentFolder), Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())
                    .collect(Collectors.toList());

            File[] files = new File[filePaths.size()];
            int i = 0;
            for (Path p : filePaths) {
                files[i] = p.toFile();
                i++;
            }
            return files;
        } else {
            //process desired file
            fp = new File(parentFolder + desiredFile);
            if (!fp.exists()) {
                throw new FileNotFoundException();
            }
            //process desired folder
            if (fp.isDirectory()) {
                fs = fp.listFiles();
            } else {
                fs = new File[]{fp};
            }
        }
        return fs;
    }
}
