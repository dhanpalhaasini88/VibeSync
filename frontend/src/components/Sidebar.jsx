function Sidebar({ currentTab, onTabChange }) {
    const menuItems = [
        { id: 'home', label: 'Home' },
        { id: 'search', label: 'Search' },
        { id: 'weather', label: 'Weather Vibe' },
        { id: 'favorites', label: 'Favorites' },
        { id: 'playlists', label: 'Playlists' },
        { id: 'profile', label: 'Profile' },
    ]

    return (
        <aside className="sidebar">
            {/* Brand Logo */}
            <div className="sidebar-brand">
                <span className="brand-mark" aria-hidden="true">
                    <span className="brand-mark-wave"><i /><i /><i /><i /></span>
                </span>
                <div className="brand-text">
                    <span className="brand-name">
                        <span>Vibe</span><span className="brand-name-accent">Sync</span>
                    </span>
                    <span className="brand-tagline">MUSIC PLAYER</span>
                </div>
            </div>

            {/* Navigation Links */}
            <nav className="sidebar-nav">
                <div className="nav-group-title">DISCOVER</div>
                {menuItems.slice(0, 4).map(item => (
                    <button
                        key={item.id}
                        className={`nav-item ${currentTab === item.id ? 'active' : ''}`}
                        onClick={() => onTabChange(item.id)}
                    >
                        <span className="nav-label">{item.label}</span>
                    </button>
                ))}

                <div className="nav-group-title" style={{ marginTop: '24px' }}>MY LIBRARY</div>
                {menuItems.slice(4).map(item => (
                    <button
                        key={item.id}
                        className={`nav-item ${currentTab === item.id ? 'active' : ''}`}
                        onClick={() => onTabChange(item.id)}
                    >
                        <span className="nav-label">{item.label}</span>
                    </button>
                ))}
            </nav>

        </aside>
    )
}

export default Sidebar
