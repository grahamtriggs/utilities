package me.triggs.propmerge;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PropMerge {
    public static void main(String [] args) {
        File outputFile = null;
        File templateFile = null;
        List<File> inputFiles = new ArrayList<>();

        String option = null;

        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (option == null) {
                    option = arg;
                } else {
                    System.err.println("Error: more than one option supplied");
                    System.exit(1);
                }
            } else {
                if (outputFile == null) {
                    outputFile = resolveFile(arg, false);
                } else if (templateFile == null) {
                    templateFile = resolveFile(arg, true);
                } else {
                    inputFiles.add(resolveFile(arg, true));
                }
            }
        }

        if (outputFile == null) {
            printUsage();
            System.exit(1);
        }

        if (templateFile == null) {
            System.err.println("You must supply at least two file names.");
            System.exit(1);
        }

        try {
            Properties outputProps = new Properties();
            Properties templateProps = readProperties(templateFile);
            List<Properties> inputProps = new ArrayList<>();
            for (File inputFile : inputFiles) {
                inputProps.add(readProperties(inputFile));
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
                try (BufferedReader br = new BufferedReader(new FileReader(templateFile))) {
                    boolean skipLine = false;
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (skipLine) {
                            if (!line.endsWith("\\")) {
                                skipLine = false;
                            }
                        } else if (line.contains("=")) {
                            String key = line.split("=")[0].trim();
                            if (key.startsWith("#")) {
                                bw.write(line);
                                bw.newLine();
                            } else {
                                boolean matchedKey = false;
                                for (Properties inputProp : inputProps) {
                                    if (inputProp.containsKey(key)) {
                                        matchedKey = true;
                                        bw.write(key);
                                        bw.write(" = ");
                                        bw.write(inputProp.getProperty(key));
                                        bw.newLine();
                                        break;
                                    }
                                }

                                if (matchedKey) {
                                    if (line.endsWith("\\")) {
                                        skipLine = true;
                                    }
                                } else {
                                    bw.write(line);
                                    bw.newLine();
                                }

                            }
                        } else {
                            bw.write(line);
                            bw.newLine();
                        }
                    }
                }
            }

        } catch (Exception e) {

        }
    }

    private static void printUsage() {
        System.err.println("Usage: java -jar propmerge.jar output template [input1] [input2]...");
        System.err.println("");
        System.err.println("Only one option may be supplied:");
        System.err.println("");
        System.err.println("-d\tFind keys that have different text in each file");
        System.err.println("-n\tFind keys that are present in file1 but not file2");
        System.err.println("-m\tFind keys that have exactly the same text");
        System.err.println("");
        System.err.println("If no option is supplied, default is to find exact matches");
    }

    private static File resolveFile(String filename, boolean mustExist) {
        File file = new File(filename);

        if (mustExist) {
            if (!file.exists() || !file.isFile()) {
                System.out.println(filename + " does not exist");
                System.exit(1);
            }
        }

        return file;
    }

    private static Properties readProperties(File file) throws IOException {
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(file);
        Reader reader = new InputStreamReader(fis, "UTF-8");
        try {
            props.load(reader);
        } finally {
            reader.close();
        }

        return props;
    }
}
