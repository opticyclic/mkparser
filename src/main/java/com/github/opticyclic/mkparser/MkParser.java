package com.github.opticyclic.mkparser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MkParser {

    public List<String> getProductMakeFiles(Path makefile) {
        List<String> files = new ArrayList<>();
        try {
            List<String> allLines = Files.readAllLines(makefile, Charset.defaultCharset());
            files = parseProductLines(allLines, makefile.getParent().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    public List<String> parseProductLines(List<String> allLines, String rootDir) {
        List<String> files = new ArrayList<>();
        boolean include = false;
        for (String line : allLines) {
            if (include || line.contains("PRODUCT_MAKEFILES")) {
                //If there is a continuation line we add a flag to process the next line
                if (line.contains("\\")) {
                    include = true;
                } else {
                    include = false;
                }
                String mkFile = getMkFromLine(line);
                if (!mkFile.isEmpty()) {
                    String makePath = getPathFromString(rootDir, mkFile);
                    files.add(makePath);
                }
            }
        }
        return files;
    }

    /**
     * Parse the mk file name from a line.
     * Assume that all paths start with a variable "$(..)"
     */
    public String getMkFromLine(String line) {
        String filename = "";
        if (line.contains(".mk")) {
            String trim = line.trim();
            String substring = trim.substring(trim.indexOf('$'));
            String[] split = substring.split(" ");
            filename = split[0];
        }
        return filename;
    }

    /**
     * Take the make file line and try to parse out the variables into full paths
     *
     * @param rootDir is the directory of the file that the mkFile snippet came from
     * @param mkFile  will typically by prefixed wih a variable that needs converting to an absolute path
     * @return an absolute path to the make file even if it doesn't exist
     */
    public String getPathFromString(String rootDir, String mkFile) {
        String fullPath = mkFile.replace("$(LOCAL_DIR)", rootDir);
        return fullPath.replace("//", "/");
    }
}
