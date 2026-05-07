package com.minidouban.movie;

import com.minidouban.common.BusinessException;
import com.minidouban.rating.RatingRepository;
import com.minidouban.rating.RatingStats;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MovieService {
    private final MovieRepository movieRepository;
    private final RatingRepository ratingRepository;

    public MovieService(MovieRepository movieRepository, RatingRepository ratingRepository) {
        this.movieRepository = movieRepository;
        this.ratingRepository = ratingRepository;
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> list() {
        Map<Long, RatingStats> statsByMovieId = ratingRepository.findAllStats().stream()
                .collect(Collectors.toMap(RatingStats::movieId, Function.identity()));
        return movieRepository.findAllByOrderByIdAsc().stream()
                .map(movie -> toResponse(movie, statsByMovieId.get(movie.getId()), null))
                .toList();
    }

    @Transactional(readOnly = true)
    public MovieResponse detail(Long movieId, Long currentUserId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "MOVIE_NOT_FOUND", "电影不存在"));
        RatingStats stats = ratingRepository.findStatsByMovieId(movieId).orElse(null);
        Integer myScore = currentUserId == null
                ? null
                : ratingRepository.findByUserIdAndMovieId(currentUserId, movieId).map(rating -> rating.getScore()).orElse(null);
        return toResponse(movie, stats, myScore);
    }

    private MovieResponse toResponse(Movie movie, RatingStats stats, Integer myScore) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getReleaseYear(),
                movie.getPosterUrl(),
                movie.getSynopsis(),
                stats == null ? null : round(stats.averageScore()),
                stats == null ? 0L : stats.ratingCount(),
                myScore
        );
    }

    private BigDecimal round(Double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP);
    }
}
