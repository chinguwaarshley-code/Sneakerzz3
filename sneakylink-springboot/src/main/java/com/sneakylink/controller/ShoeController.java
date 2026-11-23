package com.sneakylink.controller;

import com.sneakylink.dto.FilterMetadataResponse;
import com.sneakylink.model.Shoe;
import com.sneakylink.service.ShoeService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/shoes")
public class ShoeController {

    private final ShoeService shoeService;

    public ShoeController(ShoeService shoeService) {
        this.shoeService = shoeService;
    }

    @GetMapping
    public List<Shoe> listShoes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String colorway,
            @RequestParam(required = false) String primaryColor,
            @RequestParam(required = false) String sizeRange,
            @RequestParam(required = false) String exactSize,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStockOnly,
            @RequestParam(required = false) String sortBy
    ) {
        return shoeService.searchShoes(
                query, colorway, primaryColor, sizeRange, exactSize,
                material, brand, category, minPrice, maxPrice, inStockOnly, sortBy
        );
    }

    @GetMapping("/{id}")
    public Shoe getShoe(@PathVariable Long id) {
        return shoeService.getShoeById(id);
    }

    @GetMapping("/filters")
    public FilterMetadataResponse getFilters() {
        return shoeService.getFilterMetadata();
    }
}
