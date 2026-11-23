package com.sneakylink.dto;

import java.util.List;

public record FilterMetadataResponse(
        List<String> colorways,
        List<String> primaryColors,
        List<String> sizeRanges,
        List<String> materials,
        List<String> brands,
        List<String> categories,
        long totalModels
) {}
