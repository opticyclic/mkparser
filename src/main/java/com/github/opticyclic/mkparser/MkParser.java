package com.github.opticyclic.mkparser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MkParser {

    private Set<String> fileNames = new HashSet<>();

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
        //Now loop over the rest of the mk files in the dir in case we missed anything
        List<File> files = getFilesOfType(deviceRoot, "*.mk");
        for (File file : files) {
            addNode(romRoot, root, file.getAbsolutePath());
        }
        return root;
    }

    private void addNode(Path romRoot, TreeNode root, String productMakeFile) {
        //Don't recurse over the same object twice
        if (!fileNames.contains(productMakeFile)) {
            fileNames.add(productMakeFile);
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
            if (!line.trim().startsWith("#") && !line.contains("all-makefiles-under")) {
                if (line.contains("call inherit-product")) {
                    String mkFile = getMkFromInheritLine(line);
                    if (!mkFile.isEmpty()) {
                        String makePath;
                        if (mkFile.contains("$(SRC_TARGET_DIR)")) {
                            makePath = mkFile.replace("$(SRC_TARGET_DIR)", "build/target");
                            makePath = rootDir + "/" + makePath;
                        } else {
                            makePath = rootDir + "/" + mkFile;
                        }
                        makePath.replaceAll("//", "/");
                        files.add(makePath);
                    }
                } else if (line.contains("include")) {
                    String mkFile = line.substring(line.indexOf("include") + 7).trim();
                    String makePath;
                    if (mkFile.contains("$(LOCAL_PATH)")) {
                        makePath = mkFile.replace("$(LOCAL_PATH)", deviceDir);
                    } else {
                        makePath = rootDir + "/" + mkFile;
                    }
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

    private List<File> getFilesOfType(Path directory, String glob) {
        List<File> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, glob)) {
            for (Path entry : stream) {
                files.add(entry.toFile());
            }
            return files;
        } catch (IOException e) {
            throw new RuntimeException("Error reading directory " + directory, e);
        }
    }
}
