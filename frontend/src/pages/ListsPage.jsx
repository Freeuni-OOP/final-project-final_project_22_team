import React, { useState, useEffect, useCallback } from 'react';
import '../style/ListsPage.css';

// "My Lists" below is real data from the Lists backend (MovieList /
// MovieListItem). Featured Lists, Popular This Week, Recently Liked, and
// Crew Picks have no backend behind them yet (no concept of official lists,
// likes, or popularity tracking exists) - those sections stay fixed
// placeholder content purely to show the intended layout, mirroring the
// Letterboxd Lists page. The "Upgrade to Pro" ad banner from the reference
// screenshot was intentionally skipped since it's monetization-specific
// and not relevant to this app.

const LISTS_BASE_URL = 'https://localhost:8443/api/lists';

const FEATURED_LISTS = [
    { title: 'Top 500 Narrative Feature Films', creator: 'Official Lists', official: true },
    { title: 'Most Fans on Letterboxd', creator: 'Official Lists', official: true },
    { title: 'One Million Watched Club', creator: 'Alexander', official: false },
];

const POPULAR_LISTS = [
    { title: 'Movies To Fuel Your Misandry', creator: 'sapphixx', films: 23, likes: '6.4K', comments: 336 },
    { title: "Letterboxd's Top 500 Films", creator: 'Official Lists', official: true, films: 500, likes: '393K', comments: '33K' },
    { title: 'Movies everyone should watch at least once during their lifetime', creator: 'fcbarcelona', films: 800, likes: '404K', comments: '1.8K' },
];

const RECENTLY_LIKED = [
    { title: "2000's", creator: 'gabi', films: 31, likes: 480, comments: 2, desc: "iconic 2000's girly films" },
    { title: 'favorites', creator: 'lisraa', films: 30, likes: 4 },
    { title: 'Giant Insects & Naked Ladies!', creator: 'Funktual', films: 26, likes: 1 },
    { title: "2000's chick flicks", creator: 'paden19', films: 150, likes: '2.5K', comments: 7, desc: "literally every 2000's chick flick you can think of and ones you don't even know about" },
];

const CREW_PICKS = [
    { title: 'Summerween', creator: 'Luke', films: 52 },
    { title: 'movies for fucked up girls', creator: 'kind_cruelty', films: 150 },
    { title: 'you seem pretty sad for a girl' },
];

const AVATAR_COLORS = ['#00b4a2', '#e85d75', '#f2b134', '#5b8def', '#9b59b6', '#2ecc71'];

function colorForName(name) {
    if (!name) return AVATAR_COLORS[0];
    const code = name.charCodeAt(0) || 0;
    return AVATAR_COLORS[code % AVATAR_COLORS.length];
}

function MiniAvatar({ name, size = 22 }) {
    const initial = name ? name.trim().charAt(0).toUpperCase() : '?';
    return (
        <div
            className="lp-avatar"
            style={{ width: size, height: size, backgroundColor: colorForName(name), fontSize: size * 0.5 }}
        >
            {initial}
        </div>
    );
}

function OfficialBadge() {
    return (
        <span className="lp-official-badge" aria-label="Official Lists">
            <span className="lp-official-dot lp-dot-orange" />
            <span className="lp-official-dot lp-dot-green" />
            <span className="lp-official-dot lp-dot-blue" />
        </span>
    );
}

function HeartIcon() {
    return (
        <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor">
            <path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.6l-1-1a5.5 5.5 0 0 0-7.8 7.8l1 1L12 21l7.8-7.8 1-1a5.5 5.5 0 0 0 0-7.8z" />
        </svg>
    );
}

function CommentIcon() {
    return (
        <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor">
            <path d="M4 4h16a1 1 0 0 1 1 1v11a1 1 0 0 1-1 1H9l-5 4V5a1 1 0 0 1 1-1z" />
        </svg>
    );
}

function PosterStrip({ size = 'lg', count = 5 }) {
    return (
        <div className={`lp-poster-strip lp-poster-strip-${size}`}>
            {Array.from({ length: count }).map((_, i) => (
                <div key={i} className={`lp-poster-strip-item lp-poster-strip-item-${size}`} />
            ))}
        </div>
    );
}

function ListCard({ item }) {
    return (
        <div className="lp-card">
            <PosterStrip size="lg" />
            <div className="lp-card-title">{item.title}</div>
            {item.creator && (
                <div className="lp-card-meta">
                    {item.official ? <OfficialBadge /> : <MiniAvatar name={item.creator} />}
                    <span>
                        Created by <span className="lp-creator-name">{item.creator}</span>
                    </span>
                </div>
            )}
            {(item.films !== undefined) && (
                <div className="lp-card-stats">
                    {item.films !== undefined && <span className="lp-stat">{item.films} films</span>}
                    {item.likes !== undefined && <span className="lp-stat"><HeartIcon /> {item.likes}</span>}
                    {item.comments !== undefined && <span className="lp-stat"><CommentIcon /> {item.comments}</span>}
                </div>
            )}
        </div>
    );
}

function MyListCard({ list, expanded, items, addShowValue, onToggleExpand, onDelete, onAddShowChange, onAddShow, onRemoveShow }) {
    return (
        <div className="lp-my-list-card">
            <div className="lp-my-list-top" onClick={() => onToggleExpand(list.id)}>
                <PosterStrip size="sm" count={Math.max(items?.length || 0, 1)} />
                <div className="lp-my-list-info">
                    <div className="lp-card-title">{list.name}</div>
                    <div className="lp-card-meta">
                        <span className="lp-pill">{list.isPublic ? 'Public' : 'Private'}</span>
                    </div>
                    {list.description && <p className="lp-liked-desc">{list.description}</p>}
                </div>
                <button
                    className="lp-my-list-delete"
                    onClick={(e) => { e.stopPropagation(); onDelete(list.id); }}
                    aria-label="Delete list"
                >
                    Delete
                </button>
            </div>

            {expanded && (
                <div className="lp-my-list-detail">
                    {(!items || items.length === 0) && (
                        <p className="lp-section-empty">No shows in this list yet.</p>
                    )}
                    {items && items.length > 0 && (
                        <ul className="lp-my-list-items">
                            {items.map((item) => (
                                <li key={item.id} className="lp-my-list-item-row">
                                    <span>Show #{item.showId}</span>
                                    <button
                                        className="lp-mini-remove"
                                        onClick={() => onRemoveShow(list.id, item.showId)}
                                    >
                                        Remove
                                    </button>
                                </li>
                            ))}
                        </ul>
                    )}
                    <div className="lp-add-show-form">
                        <input
                            className="lp-add-input"
                            type="number"
                            placeholder="TMDB show id"
                            value={addShowValue || ''}
                            onChange={(e) => onAddShowChange(list.id, e.target.value)}
                        />
                        <button className="lp-add-btn" onClick={() => onAddShow(list.id)}>Add show</button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default function ListsPage() {
    const [lists, setLists] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const [expandedListId, setExpandedListId] = useState(null);
    const [itemsByListId, setItemsByListId] = useState({});
    const [addShowValues, setAddShowValues] = useState({});

    const [showCreateForm, setShowCreateForm] = useState(false);
    const [newListName, setNewListName] = useState('');
    const [newListDescription, setNewListDescription] = useState('');
    const [newListPublic, setNewListPublic] = useState(true);
    const [formError, setFormError] = useState('');

    const tokenObj = localStorage.getItem('token');
    const token = tokenObj ? JSON.parse(tokenObj).token : null;

    const parseJwt = (t) => {
        if (!t) return null;
        try { return JSON.parse(atob(t.split('.')[1])); } catch (e) { return null; }
    };

    const decodedToken = parseJwt(token);
    const username = decodedToken?.sub;
    const authHeaders = { Authorization: `Bearer ${token}` };

    const loadLists = useCallback(() => {
        if (!username) {
            setLoading(false);
            return;
        }
        setLoading(true);
        setError('');
        fetch(`${LISTS_BASE_URL}?actingUsername=${username}`, { headers: authHeaders })
            .then(r => (r.ok ? r.json() : []))
            .then(data => setLists(data || []))
            .catch(() => setError('Could not load your lists.'))
            .finally(() => setLoading(false));
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [username]);

    useEffect(() => {
        loadLists();
    }, [loadLists]);

    const handleCreateList = () => {
        if (!username) return;
        if (!newListName.trim()) {
            setFormError('List name cannot be empty.');
            return;
        }
        setFormError('');
        const params = new URLSearchParams({
            actingUsername: username,
            name: newListName.trim(),
            description: newListDescription,
            isPublic: String(newListPublic),
        });
        fetch(`${LISTS_BASE_URL}?${params.toString()}`, { method: 'POST', headers: authHeaders })
            .then(res => (res.ok ? res.json() : res.text().then(msg => Promise.reject(msg))))
            .then(() => {
                setNewListName('');
                setNewListDescription('');
                setNewListPublic(true);
                setShowCreateForm(false);
                loadLists();
            })
            .catch((msg) => setFormError(typeof msg === 'string' ? msg : 'Could not create list.'));
    };

    const handleDeleteList = (listId) => {
        fetch(`${LISTS_BASE_URL}/${listId}?actingUsername=${username}`, { method: 'DELETE', headers: authHeaders })
            .then(() => {
                if (expandedListId === listId) setExpandedListId(null);
                loadLists();
            });
    };

    const loadItemsFor = (listId) => {
        fetch(`${LISTS_BASE_URL}/${listId}`, { headers: authHeaders })
            .then(r => (r.ok ? r.json() : null))
            .then(data => {
                setItemsByListId(prev => ({ ...prev, [listId]: data?.items || [] }));
            });
    };

    const handleToggleExpand = (listId) => {
        if (expandedListId === listId) {
            setExpandedListId(null);
            return;
        }
        setExpandedListId(listId);
        loadItemsFor(listId);
    };

    const handleAddShowChange = (listId, value) => {
        setAddShowValues(prev => ({ ...prev, [listId]: value }));
    };

    const handleAddShow = (listId) => {
        const showId = parseInt(addShowValues[listId], 10);
        if (!showId || Number.isNaN(showId)) return;
        fetch(`${LISTS_BASE_URL}/${listId}/shows?actingUsername=${username}&showId=${showId}`, {
            method: 'POST',
            headers: authHeaders,
        })
            .then(res => (res.ok ? null : res.text().then(msg => Promise.reject(msg))))
            .then(() => {
                setAddShowValues(prev => ({ ...prev, [listId]: '' }));
                loadItemsFor(listId);
            })
            .catch(() => { /* show stays silent in this first pass */ });
    };

    const handleRemoveShow = (listId, showId) => {
        fetch(`${LISTS_BASE_URL}/${listId}/shows/${showId}?actingUsername=${username}`, {
            method: 'DELETE',
            headers: authHeaders,
        }).then(() => loadItemsFor(listId));
    };

    return (
        <div className="lp-page">
            <main className="lp-main">
                <p className="lp-tagline">Collect, curate, and share. Lists are the perfect way to group films.</p>
                <div className="lp-cta-wrap">
                    <button className="lp-start-btn" onClick={() => setShowCreateForm(v => !v)}>
                        Start your own list
                    </button>
                </div>

                <section className="lp-section">
                    <div className="lp-section-header">
                        <span className="lp-kicker">My Lists</span>
                    </div>

                    {!username && (
                        <p className="lp-section-empty">Log in to create and manage your own lists.</p>
                    )}

                    {username && showCreateForm && (
                        <div className="lp-create-form">
                            <input
                                className="lp-add-input"
                                placeholder="List name"
                                value={newListName}
                                onChange={(e) => setNewListName(e.target.value)}
                            />
                            <input
                                className="lp-add-input"
                                placeholder="Description (optional)"
                                value={newListDescription}
                                onChange={(e) => setNewListDescription(e.target.value)}
                            />
                            <label className="lp-checkbox-label">
                                <input
                                    type="checkbox"
                                    checked={newListPublic}
                                    onChange={(e) => setNewListPublic(e.target.checked)}
                                />
                                Public
                            </label>
                            <button className="lp-add-btn" onClick={handleCreateList}>Create</button>
                            {formError && <p className="lp-panel-message">{formError}</p>}
                        </div>
                    )}

                    {username && loading && <p className="lp-section-empty">Loading your lists...</p>}
                    {username && error && <p className="lp-panel-message">{error}</p>}

                    {username && !loading && !error && lists.length === 0 && (
                        <p className="lp-section-empty">You haven't created any lists yet.</p>
                    )}

                    {username && lists.length > 0 && (
                        <div className="lp-my-lists-grid">
                            {lists.map((list) => (
                                <MyListCard
                                    key={list.id}
                                    list={list}
                                    expanded={expandedListId === list.id}
                                    items={itemsByListId[list.id]}
                                    addShowValue={addShowValues[list.id]}
                                    onToggleExpand={handleToggleExpand}
                                    onDelete={handleDeleteList}
                                    onAddShowChange={handleAddShowChange}
                                    onAddShow={handleAddShow}
                                    onRemoveShow={handleRemoveShow}
                                />
                            ))}
                        </div>
                    )}
                </section>

                <section className="lp-section">
                    <div className="lp-section-header">
                        <span className="lp-kicker">Featured Lists</span>
                        <span className="lp-section-link">All &bull; Official</span>
                    </div>
                    <div className="lp-grid-3">
                        {FEATURED_LISTS.map((item, i) => <ListCard key={i} item={item} />)}
                    </div>
                </section>

                <section className="lp-section">
                    <div className="lp-section-header">
                        <span className="lp-kicker">Popular This Week</span>
                        <span className="lp-section-link">More</span>
                    </div>
                    <div className="lp-grid-3">
                        {POPULAR_LISTS.map((item, i) => <ListCard key={i} item={item} />)}
                    </div>
                </section>

                <div className="lp-two-col">
                    <section className="lp-section lp-recently-liked">
                        <div className="lp-section-header">
                            <span className="lp-kicker">Recently Liked</span>
                        </div>
                        <div className="lp-liked-list">
                            {RECENTLY_LIKED.map((item, i) => (
                                <div key={i} className="lp-liked-row">
                                    <PosterStrip size="sm" />
                                    <div className="lp-liked-content">
                                        <div className="lp-liked-title">{item.title}</div>
                                        <div className="lp-liked-meta">
                                            <MiniAvatar name={item.creator} size={18} />
                                            <span className="lp-creator-name">{item.creator}</span>
                                            <span className="lp-stat">{item.films} films</span>
                                            {item.likes !== undefined && <span className="lp-stat"><HeartIcon /> {item.likes}</span>}
                                            {item.comments !== undefined && <span className="lp-stat"><CommentIcon /> {item.comments}</span>}
                                        </div>
                                        {item.desc && <p className="lp-liked-desc">{item.desc}</p>}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </section>

                    <section className="lp-section lp-crew-picks">
                        <div className="lp-section-header">
                            <span className="lp-kicker">Crew Picks</span>
                        </div>
                        <div className="lp-crew-picks-list">
                            {CREW_PICKS.map((item, i) => (
                                <div key={i} className="lp-crew-pick-item">
                                    <PosterStrip size="sm" />
                                    <div className="lp-crew-pick-title">{item.title}</div>
                                    {item.creator && (
                                        <div className="lp-crew-pick-meta">
                                            <MiniAvatar name={item.creator} size={18} />
                                            <span className="lp-creator-name">{item.creator}</span>
                                            {item.films !== undefined && <span className="lp-stat">{item.films} films</span>}
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    </section>
                </div>

            </main>
        </div>
    );
}
