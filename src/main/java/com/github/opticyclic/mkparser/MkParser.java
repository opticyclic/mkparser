package com.github.opticyclic.mkparser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MkParser {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Pass the rom directory and the directory of the device you want to scan.");
            System.exit(1);
        }
        Path romRoot = Paths.get(args[0]);
        Path deviceRoot = Paths.get(args[1]);
        MkParser mkParser = new MkParser();
        TreeNode tree = mkParser.parseDir(romRoot, deviceRoot);
        System.out.println(tree.toString());
    }

    public TreeNode parseDir(Path romRoot, Path deviceRoot) {
        //Ignore Android.mk as it inevitably just includes all mk files below
        //Start at AndroidProducts.mk
        Path androidProducts = deviceRoot.resolve("AndroidProducts.mk");
        List<String> productMakeFiles = getProductMakeFiles(androidProducts);
        TreeNode root = new TreeNode(androidProducts.toString());
        for (String productMakeFile : productMakeFiles) {
            addNode(romRoot, root, productMakeFile);
        }
        return root;
    }

    private void addNode(Path romRoot, TreeNode root, String productMakeFile) {
        TreeNode treeNode = root.addChild(productMakeFile);
        if (Files.isRegularFile(Paths.get(productMakeFile))) {
            List<String> inheritedFiles = getInheritedFiles(romRoot, Paths.get(productMakeFile));
            for (String inheritedFile : inheritedFiles) {
                addNode(romRoot, treeNode, inheritedFile);
            }
        } else {
            treeNode.addChild("^^^^^File cannot be found^^^^^");
        }
    }

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

    public List<String> getInheritedFiles(Path romRoot, Path productMakeFile) {
        List<String> files = new ArrayList<>();
        try {
            List<String> allLines = Files.readAllLines(productMakeFile, Charset.defaultCharset());
            files = parseInheritedLines(allLines, romRoot.toString(), productMakeFile.getParent().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    /**
     * Parse out absolute paths to makefiles called via inherit-product directives
     *
     * @param allLines  is the content of the makefile
     * @param rootDir   is the root dir of the ROM
     * @param deviceDir is the device dir that we are currently in
     * @return a list of absolute paths to inherited make files
     */
    public List<String> parseInheritedLines(List<String> allLines, String rootDir, String deviceDir) {
        List<String> files = new ArrayList<>();
        for (String line : allLines) {
            if (line.contains("call inherit-product")) {
                String mkFile = getMkFromInheritLine(line);
                if (!mkFile.isEmpty()) {
                    String makePath = rootDir + "/" + mkFile;
                    makePath.replaceAll("//", "/");
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

    public String getMkFromInheritLine(String line) {
        String filename = "";
        if (line.contains(".mk")) {
            String trim = line.trim();
            //Ignore commented lines
            if (!trim.startsWith("#")) {
                String substring = trim.substring(trim.indexOf("call inherit-product"));
                String[] split = substring.split(",");
                filename = split[1].trim();
                int trailingBracket = filename.lastIndexOf(')');
                int extension = filename.lastIndexOf("mk");
                if (trailingBracket > extension) {
                    filename = filename.substring(0, trailingBracket);
                }
            }
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
