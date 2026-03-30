export default function MovieTabs({ tabs, activeKey, onChange }) {
  return (
    <div className="movie-tabs" role="tablist" aria-label="豆瓣电影分类">
      {tabs.map((tab) => (
        <button
          key={tab.key}
          type="button"
          role="tab"
          aria-selected={activeKey === tab.key}
          className={`tab-btn ${activeKey === tab.key ? "active" : ""}`}
          onClick={() => onChange(tab.key)}
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
}
