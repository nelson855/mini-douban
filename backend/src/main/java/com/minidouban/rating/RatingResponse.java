package com.minidouban.rating;

import java.math.BigDecimal;

public record RatingResponse(Long movieId, Integer myScore, BigDecimal averageScore, long ratingCount) {
}
