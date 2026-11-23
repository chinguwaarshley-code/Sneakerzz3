package com.sneakylink.service;

import com.sneakylink.dto.FilterMetadataResponse;
import com.sneakylink.dto.ShoeRequest;
import com.sneakylink.exception.ResourceNotFoundException;
import com.sneakylink.model.Shoe;
import com.sneakylink.repository.CartItemRepository;
import com.sneakylink.repository.ShoeRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShoeService {

    private final ShoeRepository shoeRepository;
    private final CartItemRepository cartItemRepository;

    public ShoeService(ShoeRepository shoeRepository, CartItemRepository cartItemRepository) {
        this.shoeRepository = shoeRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public List<Shoe> searchShoes(
            String query,
            String colorway,
            String primaryColor,
            String sizeRange,
            String exactSize,
            String material,
            String brand,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStockOnly,
            String sortBy
    ) {
        Specification<Shoe> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("brand")), pattern),
                        cb.like(cb.lower(root.get("sku")), pattern),
                        cb.like(cb.lower(root.get("colorway")), pattern),
                        cb.like(cb.lower(root.get("material")), pattern)
                ));
            }
            if (colorway != null && !colorway.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("colorway")), colorway.trim().toLowerCase()));
            }
            if (primaryColor != null && !primaryColor.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("primaryColor")), primaryColor.trim().toLowerCase()));
            }
            if (sizeRange != null && !sizeRange.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("sizeRange")), sizeRange.trim().toLowerCase()));
            }
            if (exactSize != null && !exactSize.isBlank()) {
                String sizeToken = exactSize.trim();
                predicates.add(cb.or(
                        cb.equal(root.get("availableSizes"), sizeToken),
                        cb.like(root.get("availableSizes"), sizeToken + ",%"),
                        cb.like(root.get("availableSizes"), "%," + sizeToken),
                        cb.like(root.get("availableSizes"), "%," + sizeToken + ",%")
                ));
            }
            if (material != null && !material.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("material")), material.trim().toLowerCase()));
            }
            if (brand != null && !brand.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("brand")), brand.trim().toLowerCase()));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (Boolean.TRUE.equals(inStockOnly)) {
                predicates.add(cb.greaterThan(root.get("stockQuantity"), 0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = resolveSort(sortBy);
        return shoeRepository.findAll(spec, sort);
    }

    public Shoe getShoeById(Long id) {
        return shoeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shoe model not found with ID: " + id));
    }

    public FilterMetadataResponse getFilterMetadata() {
        return new FilterMetadataResponse(
                shoeRepository.findDistinctColorways(),
                shoeRepository.findDistinctPrimaryColors(),
                shoeRepository.findDistinctSizeRanges(),
                shoeRepository.findDistinctMaterials(),
                shoeRepository.findDistinctBrands(),
                shoeRepository.findDistinctCategories(),
                shoeRepository.count()
        );
    }

    @Transactional
    public Shoe createShoe(ShoeRequest request) {
        shoeRepository.findBySkuIgnoreCase(request.sku()).ifPresent(existing -> {
            throw new IllegalArgumentException("A shoe model with SKU '" + request.sku() + "' already exists.");
        });

        Shoe shoe = new Shoe();
        applyFields(shoe, request);
        return shoeRepository.save(shoe);
    }

    @Transactional
    public Shoe updateShoe(Long id, ShoeRequest request) {
        Shoe shoe = getShoeById(id);
        shoeRepository.findBySkuIgnoreCase(request.sku()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("SKU '" + request.sku() + "' is already assigned to another model.");
            }
        });
        applyFields(shoe, request);
        return shoeRepository.save(shoe);
    }

    @Transactional
    public Shoe updateStock(Long id, Integer stockQuantity) {
        Shoe shoe = getShoeById(id);
        shoe.setStockQuantity(stockQuantity);
        return shoeRepository.save(shoe);
    }

    @Transactional
    public Shoe updateImage(Long id, String imageUrl) {
        Shoe shoe = getShoeById(id);
        shoe.setImageUrl(imageUrl.trim());
        return shoeRepository.save(shoe);
    }

    @Transactional
    public void deleteShoe(Long id) {
        Shoe shoe = getShoeById(id);
        cartItemRepository.deleteByShoeId(shoe.getId());
        shoeRepository.delete(shoe);
    }

    private void applyFields(Shoe shoe, ShoeRequest req) {
        shoe.setSku(req.sku().trim().toUpperCase());
        shoe.setName(req.name().trim());
        shoe.setBrand(req.brand().trim());
        shoe.setCategory(req.category().trim());
        shoe.setColorway(req.colorway().trim());
        shoe.setPrimaryColor(req.primaryColor().trim());
        shoe.setSizeRange(req.sizeRange().trim());
        shoe.setAvailableSizes(req.availableSizes().replaceAll("\\s+", ""));
        shoe.setMaterial(req.material().trim());
        shoe.setPrice(req.price());
        shoe.setStockQuantity(req.stockQuantity());
        shoe.setImageUrl(req.imageUrl().trim());
        shoe.setDescription(req.description() == null ? "" : req.description().trim());
        shoe.setFeatured(req.featured());
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Order.desc("featured"), Sort.Order.asc("name"));
        }
        return switch (sortBy) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "stock_desc" -> Sort.by(Sort.Direction.DESC, "stockQuantity");
            case "name_asc" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Order.desc("featured"), Sort.Order.asc("name"));
        };
    }
}
