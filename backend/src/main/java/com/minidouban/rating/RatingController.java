package com.minidouban.rating;

import com.minidouban.common.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies/{movieId}/rating")
public class RatingController {
    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PutMapping
    public RatingResponse upsert(
            @PathVariable Long movieId,
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody RatingRequest request
    ) {
        return ratingService.upsert(currentUser.id(), movieId, request.score());
    }
}
