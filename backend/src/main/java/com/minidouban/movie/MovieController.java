package com.minidouban.movie;

import com.minidouban.common.CurrentUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public List<MovieResponse> list() {
        return movieService.list();
    }

    @GetMapping("/{id}")
    public MovieResponse detail(@PathVariable Long id, @AuthenticationPrincipal CurrentUser currentUser) {
        return movieService.detail(id, currentUser == null ? null : currentUser.id());
    }
}
