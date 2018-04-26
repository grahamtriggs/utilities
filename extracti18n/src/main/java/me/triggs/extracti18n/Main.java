package me.triggs.extracti18n;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    static Pattern pattern = Pattern.compile("\\$\\{i18n\\(\\).([^\\}]*)\\}");

    public static void main(String [] args) {
        String option = null;
        String dir = null;
        Set<String> i18n = new HashSet<String>();

        if (args.length < 1) {
            System.exit(1);
        }

        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (option == null) {
                    option = arg;
                } else {
                    System.exit(1);
                }
            } else {
                if (dir == null) {
                    dir = arg;
                } else {
                    System.exit(1);
                }
            }
        }

        if (dir == null) {
            System.exit(0);
        }

        File path = new File(dir);
        if (path.isDirectory()) {
            scanDirectory(i18n, path);
        }

        if ("-e".equalsIgnoreCase(option)) {
            Set<String> errors   = new HashSet<String>();
            Set<String> warnings = new HashSet<String>();
            for (String str : i18n) {
                if (str.contains("?")) {
                    errors.add(str);
                } else if (str.contains("(")) {
                    warnings.add(str);
                }
            }

            if (errors.size() > 0) {
                System.out.println("Errors");
                System.out.println("------");
                System.out.println("");
                for (String err : errors) {
                    System.out.println(err);
                }
                System.out.println("");
            }

            if (warnings.size() > 0) {
                System.out.println("Warnings");
                System.out.println("--------");
                System.out.println("");
                for (String warn : warnings) {
                    System.out.println(warn);
                }
                System.out.println("");
            }
        } else if (option == null || "-p".equalsIgnoreCase(option)) {
            Set<String> keys = new HashSet<String>();
            for (String str : i18n) {
                if (str.contains("?")) {
                    keys.add(str.substring(0, str.indexOf("?")));
                } else if (str.contains("(")) {
                    keys.add(str.substring(0, str.indexOf("(")));
                } else {
                    keys.add(str);
                }
            }

            for (String key : keys) {
                System.out.println(key + " = 0000-1111-2222-3333");
            }
        }
    }

    public static void scanDirectory(Set<String> keys, File path) {
        for (File file : path.listFiles()) {
            if (file.isDirectory()) {
                scanDirectory(keys, file);
            } else if (file.isFile()) {
                String name = file.getName();
                if (name != null) {
                    name = name.toLowerCase();
                    if (name.endsWith(".ftl")) {
                        try {
                            BufferedReader reader = new BufferedReader(new FileReader(file));
                            try {
                                String line = reader.readLine();
                                while (line != null) {
                                    Matcher m = pattern.matcher(line);
                                    while (m.find()) {
                                        keys.add(m.group(1));
                                    }
                                    line = reader.readLine();
                                }
                            } finally {
                                reader.close();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException ioe) {

                        }
                    }
                }
            }
        }
    }
}
