package JavaCompiler.service;
//20th of the sixth 

import org.springframework.http.HttpHeaders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import JavaCompiler.model.JavaModel;

@Service
public class MainService {
    private final RestTemplate restTemplate;

    @Autowired
    CommandRunner Runner;

    public MainService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Main Java Service
    public void JavaService(JavaModel model) {
        gitCloner(model);
        MainCompiler();
        gitPusher(model);
    }

    public void gitCloner(JavaModel model) {
        String targetUrl = "http://142.132.225.181:4000/cloner";
        String requestBody = "{\"gitrepo\":\"" + model.getGitrepo() +
                "\",\"name\":\"" + model.getName() + "\"}";
        sendPostRequestWithJsonBody(targetUrl, requestBody);
    }

    public void gitPusher(JavaModel model) {
        String targetUrl1 = "http://142.132.225.181:4000/pusher";
        String requestBody1 = "{\n" +
                "\"exerepo\":\"../Shared-Data/output\",\n" +
                "\"name\":\"" + model.getName() + "\",\n" +
                "\"filename\":\"Files.jar\"\n" +
                "}";

        sendPostRequestWithJsonBody(targetUrl1, requestBody1);
    }

    // JAVA COMPILER
    public void MainCompiler() {

        File directory = new File("../Shared-Data/source");
        List<File> Javafiles = new ArrayList<>();
        findJavaFiles(directory, Javafiles);
        copyFilesToDirectory("../Shared-Data/output", Javafiles);

        try {
            // Get all .java files from the specified directory
            List<File> javaFiles = Files.walk(Path.of("../Shared-Data/output"))
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            // Convert the list to an array
            File[] filesArray = javaFiles.toArray(new File[0]);

            // Set up the Java compiler
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

            // Create compilation units from the files
            Iterable<? extends JavaFileObject> compilationUnits = fileManager
                    .getJavaFileObjectsFromFiles(Arrays.asList(filesArray));

            // Compile the files
            boolean success = compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
            fileManager.close();

            // Check the compilation result
            if (success) {
                System.out.println("Compiled Successfully");
                // GENERATE the JAR file
                Runner.executeCommand();
                System.out.println("Jar File Created");
            } else {
                System.out.println("Compilation failed!");
            }
        } catch (IOException e) {
            System.out.println("IO Exception occurred: " + e.getMessage());
        }
    }

    public void copyFilesToDirectory(String targetDirectory, List<File> files) {

        for (File file : files) {
            Path sourcePath = file.toPath();
            Path targetPath = Path.of(targetDirectory, file.getName());

            try {
                // Copy the file to the target directory
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Copied: " + file.getName());
            } catch (IOException e) {
                System.err.println("Failed to copy: " + file.getName() + " due to " + e.getMessage());
            }
        }
    }

    public static void findJavaFiles(File directory, List<File> javaFiles) {

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        findJavaFiles(file, javaFiles);
                    } else if (file.getName().endsWith(".java")) {
                        javaFiles.add(file);
                        System.out.println(file);
                    }
                }
            }
        }

    }

    public void sendPostRequestWithJsonBody(String targetUrl, String requestBody) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(targetUrl, HttpMethod.POST, requestEntity,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("POST request successful. Response: " + response.getBody());
        } else {
            System.out.println("POST request failed. Status code: " + response.getStatusCode());
        }
    }

}
