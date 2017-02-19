package com.github.opticyclic.mkparser;

public class MkParser {

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
        return mkFile.replace("$(LOCAL_DIR)", rootDir);
    }
}
