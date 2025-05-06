package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Category;
import com.inn.cafe.POJO.Product;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.ProductDao;
import com.inn.cafe.service.ProductService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.wrapper.ProductWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private JwtFilter jwtFilter;

    @PostMapping("/product/add")
    @Override
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        try {
            System.out.println("Incoming request for addNewProduct: " + requestMap);
            if (jwtFilter.isAdmin()) {
                if (validateProductMap(requestMap, false)) {
                    try {
                        Product product = getProductFromMap(requestMap, false);
                        System.out.println("Mapped Product: " + product);
                        productDao.save(product);
                        return CafeUtils.getResponseEntity("Product Added Successfully.", HttpStatus.OK);
                    } catch (NumberFormatException e) {
                        System.out.println("Number format exception: " + e.getMessage());
                        return CafeUtils.getResponseEntity("Invalid numeric values in request.",
                                HttpStatus.BAD_REQUEST);}}
                System.out.println("Validation failed for request: " + requestMap);
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
            System.out.println("Unauthorized access attempt detected.");
            return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            System.out.println("Error while adding product: " + e.getMessage());
            e.printStackTrace();
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }}


    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {
        System.out.println("Validating request data: " + requestMap);
        boolean isValid = requestMap.containsKey("name") &&
                requestMap.containsKey("categoryId") &&
                requestMap.containsKey("price") &&
                requestMap.containsKey("description") &&
                (!validateId || requestMap.containsKey("id"));

        if (!isValid) {
            System.out.println("Validation failed for data: " + requestMap);
        }
        return isValid;
    }


    private Product getProductFromMap(Map<String, String> requestMap, boolean isAdd) {
        try {
            Category category = new Category();
            category.setId(Integer.parseInt(requestMap.get("categoryId")));

            Product product = new Product();
            if (isAdd) {
                product.setId(Integer.parseInt(requestMap.get("id")));
            }
            product.setStatus("true");
            product.setCategory(category);
            product.setName(requestMap.get("name"));
            product.setDescription(requestMap.get("description"));
            product.setPrice((int) Double.parseDouble(requestMap.get("price"))); // Use Double for price

            return product;
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format in request: " + requestMap);
            throw e; // Or handle with a custom exception
        }
    }


    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProduct() {
        try {
            return new ResponseEntity<>(productDao.getAllProduct(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @PostMapping("/product/update")
    @Override
    @Transactional
    public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {
        System.out.println("Request received to update product: " + requestMap);
        try {
            if (jwtFilter.isAdmin()) {
                System.out.println("Admin access confirmed.");
                if (validateProductMap(requestMap, true)) {
                    System.out.println("Validation passed for product update.");
                    Optional<Product> optional = productDao.findById(Integer.parseInt(requestMap.get("id")));
                    if (optional.isPresent()) {
                        System.out.println("Product found, updating...");
                        Product product = getProductFromMap(requestMap, true);
                        product.setStatus(optional.get().getStatus());
                        productDao.save(product);
                        System.out.println("Product updated successfully.");
                        return CafeUtils.getResponseEntity("Product Updated Successfully.", HttpStatus.OK);
                    } else {
                        System.out.println("Product not found for ID: " + requestMap.get("id"));
                        return CafeUtils.getResponseEntity("Product ID does not exist.", HttpStatus.BAD_REQUEST);
                    }
                }
                System.out.println("Validation failed for product update.");
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
            System.out.println("Unauthorized access attempt.");
            return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            System.out.println("Error during product update: " + e.getMessage());
            e.printStackTrace();
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> deleteProduct(Integer id) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<Product> optional = productDao.findById(id);
                if (optional.isPresent()) {
                    productDao.deleteById(id);
                    return CafeUtils.getResponseEntity("Product Deleted Successfully.", HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity("Product ID does not exist.", HttpStatus.BAD_REQUEST);
            }
            return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            e.printStackTrace();
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<Product> optional = productDao.findById(Integer.parseInt(requestMap.get("id")));
                if (optional.isPresent()) {
                    productDao.updateProductStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    return CafeUtils.getResponseEntity("Product Status Updated Successfully.", HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity("Product ID does not exist.", HttpStatus.BAD_REQUEST);
            }
            return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            e.printStackTrace();
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getByCategory(Integer id) {
        try {
            return new ResponseEntity<>(productDao.getProductByCategory(id), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<ProductWrapper> getProductById(Integer id) {
        try {
            return new ResponseEntity<>(productDao.getProductById(id), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ProductWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
