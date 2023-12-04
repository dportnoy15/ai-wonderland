package modelimport;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public class Utils {
 
    public static int invokeScript(String... cliArgs) throws InterruptedException, IOException {
        System.out.println("CLI command: " + String.join(" ", cliArgs));

        Process process = new ProcessBuilder(cliArgs).inheritIO().start();

        ProcessHandle processHandle = process.toHandle();

        int exitCode = process.waitFor();

        System.out.println("Process finished running");

        processHandle.destroyForcibly();

        return exitCode;
    }

    public static void deleteDirectory(String dirPath) throws IOException {
        Path dir = Paths.get(dirPath);

        Files
            .walk(dir)
            .sorted(Comparator.reverseOrder())
            .forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }

    public static Background getBackgroundColor(Color c, double radius) {
        BackgroundFill backgroundFill = new BackgroundFill(c, new CornerRadii(radius), null );
        return new Background(backgroundFill);
    }

    public static Background getBackgroundColor(Color c) {
        return getBackgroundColor(c, 10);
    }

    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");

        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }

}