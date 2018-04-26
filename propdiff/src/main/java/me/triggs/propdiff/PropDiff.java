package me.triggs.propdiff;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropDiff {
    public static void main(String [] args) {
        String filename1 = null;
        String filename2 = null;
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
                if (filename1 == null) {
                    filename1 = arg;
                } else if (filename2 == null) {
                    filename2 = arg;
                } else {
                    System.err.println("Error: more than two file names supplied");
                    System.exit(1);
                }
            }
        }

        if (filename1 == null) {
            printUsage();
            System.exit(1);
        }

        if (filename2 == null) {
            System.err.println("You must supply at least two file names.");
            System.exit(1);
        }

        File file1 = resolveFile(filename1);
        File file2 = resolveFile(filename2);

        try {
            Properties props1 = readProperties(file1);
            Properties props2 = readProperties(file2);

            if ("-d".equalsIgnoreCase(option)) {
                findKeysRedefined(props1, props2);
            } else if ("-n".equalsIgnoreCase(option)) {
                findKeysNotPresent(props1, props2, false);
            } else if ("-np".equalsIgnoreCase(option)) {
                findKeysNotPresent(props1, props2, true);
            } else if (option == null || "-m".equalsIgnoreCase(option)) {
                findExactMatches(props1, props2);
            } else {
                printUsage();
            }
        } catch (Exception e) {

        }
    }

    private static void printUsage() {
        System.err.println("Usage: java -jar propdiff.jar [-d] [-n] [-m] file1 file2");
        System.err.println("");
        System.err.println("Only one option may be supplied:");
        System.err.println("");
        System.err.println("-d\tFind keys that have different text in each file");
        System.err.println("-n\tFind keys that are present in file1 but not file2");
        System.err.println("-m\tFind keys that have exactly the same text");
        System.err.println("");
        System.err.println("If no option is supplied, default is to find exact matches");
    }

    private static File resolveFile(String filename) {
        File file = new File(filename);

        if (!file.exists() || !file.isFile()) {
            System.out.println(filename + " does not exist");
            System.exit(1);
        }

        return file;
    }

    private static void findExactMatches(Properties props1, Properties props2) {
        List<String> output = new ArrayList<String>();

        for (String name : props1.stringPropertyNames()) {
            String value1 = props1.getProperty(name);
            String value2 = props2.getProperty(name);

            if (value1 != null && value2 != null) {
                if (value1.equals(value2)) {
                    output.add(name);
                }
            }
        }

        if (output.size() == 0) {
            System.out.println("No matches found");
        } else {
            System.out.println("Matching keys:");
            for (String key : output) {
                System.out.println(key);
            }
        }
    }

    private static void findKeysRedefined(Properties props1, Properties props2) {
        List<String> output = new ArrayList<String>();

        for (String name : props1.stringPropertyNames()) {
            String value1 = props1.getProperty(name);
            String value2 = props2.getProperty(name);

            if (value1 != null && value2 != null) {
                if (!value1.equals(value2)) {
                    output.add(name);
                }
            }
        }

        if (output.size() == 0) {
            System.out.println("No redefined keys");
        } else {
            System.out.println("Keys present in both files with different definitions:");
            for (String key : output) {
                System.out.println(key);
            }
        }
    }

    private static void findKeysNotPresent(Properties props1, Properties props2, boolean outputAsProps) {
        List<String> output = new ArrayList<String>();

        for (String name : props1.stringPropertyNames()) {
            if (!props2.containsKey(name)) {
                output.add(name);
            }
        }

        if (outputAsProps) {
            for (String key : output) {
                System.out.println(key + " = " + props1.getProperty(key));
            }
        } else {
            if (output.size() == 0) {
                System.out.println("No missing keys");
            } else {
                System.out.println("Keys not present in file 2:");
                for (String key : output) {
                    System.out.println(key);
                }
            }
        }
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
