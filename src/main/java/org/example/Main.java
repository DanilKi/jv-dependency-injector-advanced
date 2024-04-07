package org.example;

import java.util.List;
import org.example.lib.Injector;
import org.example.model.Product;
import org.example.service.ProductService;

public class Main {

    public static void main(String[] args) {
        Injector injector = Injector.getInjector("org.example");
        ProductService productService = (ProductService) injector.getInstance(ProductService.class);
        List<Product> products = productService.getAllFromFile("products.txt");
        products.forEach(System.out::println);
    }
}
