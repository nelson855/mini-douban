package com.minidouban.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserIdAndMovieId(Long userId, Long movieId);

    @Query("select new com.minidouban.rating.RatingStats(r.movie.id, avg(r.score), count(r)) from Rating r group by r.movie.id")
    List<RatingStats> findAllStats();

    @Query("select new com.minidouban.rating.RatingStats(r.movie.id, avg(r.score), count(r)) from Rating r where r.movie.id = :movieId group by r.movie.id")
    Optional<RatingStats> findStatsByMovieId(Long movieId);
}
