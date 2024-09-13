package JavaCompiler.service;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class CommandRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        executeCommand();
    }

    public void executeCommand() {
        String directoryPath = "/root/VSCODE/Projects/SmartCompiler/Shared-Data/output"; // Directory containing .class files
        String jarFileName = "Files.jar"; // Name of the JAR file

        // Gather all .class files in the directory
        List<String> classFiles = new ArrayList<>();
        try {
            Files.list(Paths.get(directoryPath))
                .filter(path -> path.toString().endsWith(".class"))
                .forEach(path -> classFiles.add(path.getFileName().toString()));
        } catch (IOException e) {
            System.out.println("Error reading class files: " + e.getMessage());
            return;
        }

        // Build the command
        List<String> command = new ArrayList<>();
        command.add("jar");
        command.add("cvf");
        command.add(jarFileName);
        command.addAll(classFiles); // Add all class files to the command

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File(directoryPath)); // Set the working directory

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor(); // Wait for the process to finish
            
            // Capture error output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Error: " + line);
            }

            System.out.println("Command executed with exit code: " + exitCode);
        } catch (IOException e) {
            System.out.println("IOException occurred: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Process was interrupted: " + e.getMessage());
        }
    }
}
