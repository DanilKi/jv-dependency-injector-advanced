package org.example.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.example.lib.Component;
import org.example.lib.Inject;
import org.example.model.Product;
import org.example.service.FileReaderService;
import org.example.service.ProductParser;
import org.example.service.ProductService;

@Component
public class ProductServiceImpl implements ProductService {
    @Inject
    private ProductParser productParser;
    @Inject
    private FileReaderService fileReaderService;

    @Override
    public List<Product> getAllFromFile(String filePath) {
        return fileReaderService.readFile(filePath)
                .stream()
                .map(productParser::parse)
                .collect(Collectors.toList());
    }
}
