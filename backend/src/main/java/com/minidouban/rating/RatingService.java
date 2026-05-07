package com.minidouban.rating;

import com.minidouban.common.BusinessException;
import com.minidouban.movie.Movie;
import com.minidouban.movie.MovieRepository;
import com.minidouban.user.User;
import com.minidouban.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RatingService {
    private final RatingRepository ratingRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    public RatingService(RatingRepository ratingRepository, MovieRepository movieRepository, UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RatingResponse upsert(Long userId, Long movieId, Integer score) {
        if (score == null || score < 1 || score > 5) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_SCORE", "评分必须是 1-5 的整数");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "请先登录"));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "MOVIE_NOT_FOUND", "电影不存在"));

        Rating rating = ratingRepository.findByUserIdAndMovieId(userId, movieId)
                .map(existing -> {
                    existing.setScore(score);
                    return existing;
                })
                .orElseGet(() -> new Rating(user, movie, score));
        ratingRepository.saveAndFlush(rating);

        RatingStats stats = ratingRepository.findStatsByMovieId(movieId)
                .orElse(new RatingStats(movieId, null, 0L));
        return new RatingResponse(movieId, score, round(stats.averageScore()), stats.ratingCount());
    }

    private BigDecimal round(Double value) {
        return value == null ? null : BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP);
    }
}
