import React, { useState, useEffect } from 'react';
import '../style/RecommendButton.css';

export default function RecommendButton({ showId, showName }) {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [friendSearch, setFriendSearch] = useState('');
    const [friends, setFriends] = useState([]); // რეალური მეგობრების სია
    const [loadingFriends, setLoadingFriends] = useState(false);
    const [sentStatus, setSentStatus] = useState({}); // { friendUsername: true }

    // 🔑 ტოკენიდან იუზერნეიმის ამოღება (ისევე როგორც ShowsDetailsPage-ში გაქვს)
    const tokenObj = localStorage.getItem('token');
    const token = tokenObj ? JSON.parse(tokenObj).token : null;

    const parseJwt = (token) => {
        if (!token) return null;
        try { return JSON.parse(atob(token.split('.')[1])); }
        catch (e) { return null; }
    };

    const decodedToken = parseJwt(token);
    const currentUsername = decodedToken?.sub; // მიმდინარე ავტორიზებული იუზერი

    // 👥 წამოვიღოთ რეალური მეგობრების სია ბექენდიდან მოდალის გახსნისას
    useEffect(() => {
        if (isModalOpen && currentUsername) {
            setLoadingFriends(true);
            fetch(`https://localhost:8443/api/friends?actingUsername=${currentUsername}`)
                .then(res => {
                    if (!res.ok) throw new Error("Failed to load friends");
                    return res.json();
                })
                .then(data => {
                    // რადგან ბექენდი აბრუნებს უბრალოდ იუზერნეიმების მასივს ["user1", "user2"], გადავაქციოთ ობიექტებად
                    const formattedFriends = data.map((username, index) => ({
                        id: index,
                        name: username,
                        avatar: "👤" // დროებითი ავატარი სტილისთვის
                    }));
                    setFriends(formattedFriends);
                })
                .catch(err => console.error("Error fetching friends:", err))
                .finally(() => setLoadingFriends(false));
        }
    }, [isModalOpen, currentUsername]);

    // ✉️ რეკომენდაციის გაგზავნა ბექენდზე
    const handleSend = (targetUsername) => {
        if (!currentUsername) return;

        fetch(`https://localhost:8443/api/shows/${showId}/recommend?senderUsername=${currentUsername}&targetUsername=${targetUsername}`, {
            method: 'POST'
        })
            .then(res => {
                if (res.ok) {
                    // თუ წარმატებით გაიგზავნა, ვიზუალურად შემწვანდეს ღილაკი
                    setSentStatus(prev => ({ ...prev, [targetUsername]: true }));
                } else {
                    alert("Could not send recommendation");
                }
            })
            .catch(err => console.error("Error sending recommendation:", err));
    };

    const filteredFriends = friends.filter(f =>
        f.name.toLowerCase().includes(friendSearch.toLowerCase())
    );

    // თუ მომხმარებელი არ არის შესული საიტზე, ღილაკი საერთოდ არ გამოვაჩინოთ
    if (!currentUsername) return null;

    return (
        <div className="recommend-wrapper">
            <button className="recommend-main-btn" onClick={() => setIsModalOpen(true)}>
                <span className="btn-icon">✉️</span> Recommend to...
            </button>

            {isModalOpen && (
                <div className="rec-overlay" onClick={() => setIsModalOpen(false)}>
                    <div className="rec-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="rec-header">
                            <h3>Recommend <span className="rec-neon-title">"{showName}"</span></h3>
                            <button className="rec-close" onClick={() => setIsModalOpen(false)}>✕</button>
                        </div>

                        <input
                            type="text"
                            placeholder="Search friends..."
                            value={friendSearch}
                            onChange={(e) => setFriendSearch(e.target.value)}
                            className="rec-search"
                        />

                        <div className="rec-list">
                            {loadingFriends ? (
                                <div className="rec-empty">Loading friends...</div>
                            ) : (
                                <>
                                    {filteredFriends.map(friend => {
                                        const isSent = sentStatus[friend.name];
                                        return (
                                            <div key={friend.id} className="rec-row">
                                                <div className="rec-user-info">
                                                    <span className="rec-avatar">{friend.avatar}</span>
                                                    <span className="rec-username">{friend.name}</span>
                                                </div>
                                                <button
                                                    className={`rec-action-btn ${isSent ? 'sent' : ''}`}
                                                    onClick={() => handleSend(friend.name)}
                                                    disabled={isSent}
                                                >
                                                    {isSent ? "Sent ✓" : "Send"}
                                                </button>
                                            </div>
                                        );
                                    })}
                                    {filteredFriends.length === 0 && (
                                        <div className="rec-empty">No friends found.</div>
                                    )}
                                </>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}