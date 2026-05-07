package com.minidouban.movie;

import java.math.BigDecimal;

public record MovieResponse(
        Long id,
        String title,
        String director,
        Integer releaseYear,
        String posterUrl,
        String synopsis,
        BigDecimal averageScore,
        long ratingCount,
        Integer myScore
) {
}
