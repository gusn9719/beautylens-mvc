package kr.ac.kopo.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.kopo.common.vo.ApiResponse;
import kr.ac.kopo.common.vo.PageParam;
import kr.ac.kopo.product.service.ProductService;
import kr.ac.kopo.product.vo.ProductVO;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductVO>>> list(
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String skinType,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false, defaultValue = "false") boolean imageOnly,
            @RequestParam(required = false) String sortBy) {

        PageParam param = new PageParam();
        param.setPage(page);
        param.setSize(size);
        param.setKeyword(keyword);
        param.setCategory(category);
        param.setBrand(brand);
        param.setSkinType(skinType);
        param.setPlatform(platform);
        param.setImageOnly(imageOnly);
        param.setSortBy(sortBy);

        List<ProductVO> list = productService.getProducts(param);
        return ResponseEntity.ok(new ApiResponse<>(true, "products found", list));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductVO>> detail(@PathVariable int productId) {
        ProductVO product = productService.getProduct(productId);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "product not found", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "product found", product));
    }
}
