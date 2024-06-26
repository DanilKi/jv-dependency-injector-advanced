package org.example.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.example.lib.Component;
import org.example.service.FileReaderService;

@Component
public class FileReaderServiceImpl implements FileReaderService {
    @Override
    public List<String> readFile(String fileName) {
        try {
            return Files.readAllLines(new File(fileName).toPath());
        } catch (IOException e) {
            throw new RuntimeException("Can't read file: " + fileName, e);
        }
    }
}
