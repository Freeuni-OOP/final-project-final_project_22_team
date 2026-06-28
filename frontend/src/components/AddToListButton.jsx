import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../style/AddToListButton.css';

// Lets a logged-in user add the current show to one of their own lists.
// Mirrors RecommendButton's overlay/modal pattern for consistency.
export default function AddToListButton({ showId, showName }) {
    const [isOpen, setIsOpen] = useState(false);
    const [lists, setLists] = useState([]);
    const [loading, setLoading] = useState(false);
    const [statusByList, setStatusByList] = useState({}); // { [listId]: 'added' | 'already' | 'error' }
    const navigate = useNavigate();

    const tokenObj = localStorage.getItem('token');
    const token = tokenObj ? JSON.parse(tokenObj).token : null;

    const parseJwt = (t) => {
        if (!t) return null;
        try { return JSON.parse(atob(t.split('.')[1])); } catch (e) { return null; }
    };

    const decodedToken = parseJwt(token);
    const currentUsername = decodedToken?.sub;

    useEffect(() => {
        if (isOpen && currentUsername && token) {
            setLoading(true);
            fetch(`https://localhost:8443/api/lists?actingUsername=${currentUsername}`, {
                headers: { Authorization: `Bearer ${token}` }
            })
                .then(res => res.ok ? res.json() : [])
                .then(data => setLists(data || []))
                .catch(err => console.error('Error fetching lists:', err))
                .finally(() => setLoading(false));
        }
    }, [isOpen, currentUsername, token]);

    const handleAddToList = (listId) => {
        fetch(`https://localhost:8443/api/lists/${listId}/shows?actingUsername=${currentUsername}&showId=${showId}`, {
            method: 'POST',
            headers: { Authorization: `Bearer ${token}` }
        })
            .then(async (res) => {
                if (res.ok) {
                    setStatusByList(prev => ({ ...prev, [listId]: 'added' }));
                } else {
                    const message = await res.text();
                    setStatusByList(prev => ({
                        ...prev,
                        [listId]: message.includes('already in the list') ? 'already' : 'error'
                    }));
                }
            })
            .catch(() => setStatusByList(prev => ({ ...prev, [listId]: 'error' })));
    };

    if (!currentUsername) return null;

    const labelFor = (listId) => {
        switch (statusByList[listId]) {
            case 'added': return '✓ Added';
            case 'already': return 'Already in list';
            case 'error': return 'Failed - retry';
            default: return 'Add';
        }
    };

    return (
        <>
            <button className="design-btn list-btn" onClick={() => setIsOpen(true)}>
                Add to list
            </button>

            {isOpen && (
                <div className="atl-overlay" onClick={() => setIsOpen(false)}>
                    <div className="atl-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="atl-header">
                            <h3>Add <span className="atl-neon-title">"{showName}"</span> to a list</h3>
                            <button className="atl-close" onClick={() => setIsOpen(false)}>✕</button>
                        </div>

                        <div className="atl-list">
                            {loading && <div className="atl-empty">Loading your lists...</div>}

                            {!loading && lists.length === 0 && (
                                <div className="atl-empty">
                                    You don't have any lists yet.{' '}
                                    <span className="atl-create-link" onClick={() => navigate('/lists')}>
                                        Create one
                                    </span>
                                </div>
                            )}

                            {!loading && lists.map(list => {
                                const status = statusByList[list.id];
                                const disabled = status === 'added' || status === 'already';
                                return (
                                    <div key={list.id} className="atl-row">
                                        <span className="atl-list-name">{list.name}</span>
                                        <button
                                            className={`atl-action-btn ${disabled ? 'done' : ''}`}
                                            onClick={() => handleAddToList(list.id)}
                                            disabled={disabled}
                                        >
                                            {labelFor(list.id)}
                                        </button>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}
