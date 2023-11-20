package modelimport;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

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

}