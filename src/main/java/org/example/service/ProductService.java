package org.example.service;

import java.util.List;
import org.example.model.Product;

public interface ProductService {
    List<Product> getAllFromFile(String filePath);
}
