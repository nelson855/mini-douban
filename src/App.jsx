import { useMemo, useState } from "react";
import MovieTabs from "./components/MovieTabs";
import MovieCard from "./components/MovieCard";
import { movieMap, movieTabs } from "./data/movieData";

export default function App() {
  const [activeTab, setActiveTab] = useState(movieTabs[0].key);

  const currentMovies = useMemo(() => {
    return movieMap[activeTab] || [];
  }, [activeTab]);

  return (
    <main className="page">
      <section className="movie-panel">
        <header className="panel-header">
          <h1>豆瓣电影</h1>
          <p>正在热映与高分推荐</p>
        </header>

        <MovieTabs tabs={movieTabs} activeKey={activeTab} onChange={setActiveTab} />

        <div className="movie-grid">
          {currentMovies.map((movie) => (
            <MovieCard movie={movie} key={movie.id} />
          ))}
        </div>
      </section>
    </main>
  );
}
