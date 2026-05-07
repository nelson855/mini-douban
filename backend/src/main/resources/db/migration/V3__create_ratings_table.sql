CREATE TABLE ratings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    score SMALLINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ratings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ratings_movie FOREIGN KEY (movie_id) REFERENCES movies(id),
    CONSTRAINT uk_ratings_user_movie UNIQUE (user_id, movie_id),
    CONSTRAINT ck_ratings_score CHECK (score BETWEEN 1 AND 5)
);
