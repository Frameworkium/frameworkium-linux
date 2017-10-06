package com.frameworkium;

import ru.yandex.qatools.allure.annotations.Step;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Scanner;


public class FileUtils {

    /**
     * read text file and return contents as string
     * @param pathname
     * @return file in string format
     */
    @Step("read file: {0}")
    public static String readFile(String pathname) {
        File file = new File(pathname);
        StringBuilder fileContents = new StringBuilder((int) file.length());
        Scanner scanner = null;

        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String lineSeparator = System.getProperty("line.separator");

        try {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine());
                fileContents.append(lineSeparator);
            }
            return fileContents.toString();
        } finally {
            scanner.close();
        }
    }


    /**
     * Get the path of a file on the classpath
     * @param fileName
     * @return file path
     */
    @Step("Get file path from {0}")
    public String getFilePath(String fileName){
        URL filePath = getClass().getClassLoader().getResource(fileName);

        if (filePath == null){
            throw new RuntimeException(String.format("File '%s' not found on the classpath",fileName));
        } else {
            return filePath.getPath();
        }

    }

    /**
     * Deletes Folder with all of its content
     *
     * @param folder path to folder which should be deleted
     */
    @Step("Delete folder {0}")
    public static void deleteFolderContent(final Path folder) throws IOException {
        Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }else if(!dir.equals(folder)){
                    Files.delete(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * @param directoryName
     * @param fileextension
     * @return list of files in a directory with specified extension
     */
    @Step("Get list of {1} files from {0}")
    public static File[] getFileLists(String directoryName,final String fileextension){
        File directory = new File(directoryName);
        File[] filelist= directory.listFiles(new FilenameFilter() {
            public boolean accept(File directory, String name) {
                return name.toLowerCase().endsWith(fileextension);
            }
        });

        return filelist;

    }

}
