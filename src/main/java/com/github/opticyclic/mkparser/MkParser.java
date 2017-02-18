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

}
