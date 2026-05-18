package com.fitness.fitness_app.repository;

import java.io.File;

final class JsonFileUtils {
    private JsonFileUtils() {}

    static File resolve(String filePath) {
        File file = new File(filePath);
        if (file.isAbsolute()) {
            return file;
        }

        File workingDirectory = new File(System.getProperty("user.dir"));
        File directPath = new File(workingDirectory, filePath);

        // If IntelliJ is opened one level above the Maven module, use the module folder.
        File nestedModulePath = new File(workingDirectory, "fitness-app" + File.separator + filePath);
        if (!new File(workingDirectory, "pom.xml").exists() && nestedModulePath.exists()) {
            return nestedModulePath;
        }

        return directPath;
    }
}
