export default function MovieCard({ movie }) {
  return (
    <article className="movie-card">
      <img src={movie.poster} alt={`${movie.title} 海报`} className="poster" />
      <div className="movie-info">
        <h3>{movie.title}</h3>
        <p className="meta">
          {movie.year} · {movie.region}
        </p>
        <p className="meta">{movie.genre}</p>
        <p className="rating">
          豆瓣评分 <strong>{movie.rating}</strong>
        </p>
        <p className="votes">{movie.votes} 人评价</p>
      </div>
    </article>
  );
}
