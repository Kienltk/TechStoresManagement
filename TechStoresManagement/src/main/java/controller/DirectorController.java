package controller;

import java.io.File;

public class DirectorController {
    public static void deleteTempProductImage() {
        File directory = new File("src/main/resources/view/images");
        String pattern = "img_temp";

        // Ensure the directory exists
        if (!directory.isDirectory()) {
            System.out.println("The specified path is not a directory.");
            return;
        }

        // List all files in the directory
        File[] files = directory.listFiles();
        if (files == null) {
            System.out.println("No files found in the directory.");
            return;
        }

        // Delete files that match the pattern
        for (File file : files) {
            if (file.isFile() && file.getName().startsWith(pattern)) {
                if (file.delete()) {
                    System.out.println("Deleted: " + file.getAbsolutePath());
                } else {
                    System.out.println("Failed to delete: " + file.getAbsolutePath());
                }
            }
        }
    }
}
