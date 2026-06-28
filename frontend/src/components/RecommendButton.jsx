import React, { useState, useEffect } from 'react';
import '../style/RecommendButton.css';

export default function RecommendButton({ showId, showName }) {
    const [friends, setFriends] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [isOpen, setIsOpen] = useState(false);
    const [selectedFriend, setSelectedFriend] = useState(null);
    const [comment, setComment] = useState('');
    const [sentStatus, setSentStatus] = useState({}); // { მეგობრის_სახელი: true/false }

    const tokenObj = localStorage.getItem('token');
    const token = tokenObj ? JSON.parse(tokenObj).token : null;

    const parseJwt = (t) => {
        if (!t) return null;
        try { return JSON.parse(atob(t.split('.')[1])); } catch (e) { return null; }
    };

    const decodedToken = parseJwt(token);
    const currentUsername = decodedToken?.sub;

    // მეგობრების და არსებული რეკომენდაციების წამოღება მოდალის გახსნისას
    useEffect(() => {
        if (isOpen && currentUsername && token) {
            // 1. მეგობრების სიის წამოღება
            fetch(`https://localhost:8443/api/friends?actingUsername=${currentUsername}`, {
                headers: { Authorization: `Bearer ${token}` }
            })
                .then(res => res.ok ? res.json() : [])
                .then(friendsData => {
                    setFriends(friendsData);

                    // 2. 🟢 ვიძახებთ /sent ენდპოინტს, რომ წინასწარ ვიცოდეთ ვინ რა გააგზავნა!
                    fetch(`https://localhost:8443/api/tracking/recommendations/sent?username=${currentUsername}`, {
                        headers: { Authorization: `Bearer ${token}` }
                    })
                        .then(res => res.ok ? res.json() : [])
                        .then(allSentRecs => {
                            const statusMap = {};
                            allSentRecs.forEach(rec => {
                                // თუ ამ კონკრეტულ შოუზე გვაქვს ჩანაწერი, ვნიშნავთ, რომ targetUsername-ს უკვე მიუვიდა
                                if (rec.showId === showId) {
                                    statusMap[rec.targetUsername] = true;
                                }
                            });
                            setSentStatus(statusMap);
                        })
                        .catch(err => console.error("Error fetching sent status:", err));
                })
                .catch(err => console.error("Error fetching friends:", err));
        }
    }, [isOpen, currentUsername, token, showId]);

    const handleSendRecommend = () => {
        if (!selectedFriend || sentStatus[selectedFriend]) return;

        const finalComment = comment.trim() || "Check out this awesome show!";
        const url = `https://localhost:8443/api/tracking/recommend?senderUsername=${currentUsername}&targetUsername=${selectedFriend}&showId=${showId}&showName=${encodeURIComponent(showName)}&comment=${encodeURIComponent(finalComment)}`;

        fetch(url, {
            method: 'POST',
            headers: { Authorization: `Bearer ${token}` }
        })
            .then(res => {
                if (res.ok) {
                    // 🟢 მოვნიშნოთ ეს იუზერი როგორც "Sent"
                    setSentStatus(prev => ({ ...prev, [selectedFriend]: true }));
                    // გავასუფთავოთ ტექსტარეა შემდეგისთვის
                    setComment('');
                } else if (res.status === 400) {
                    alert("You have already recommended this show to this friend!");
                } else {
                    alert("Failed to send recommendation.");
                }
            })
            .catch(() => alert("Error connecting to server."));
    };

    const handleCloseAll = () => {
        setIsOpen(false);
        setSelectedFriend(null);
        setComment('');
        setSearchQuery('');
    };

    if (!currentUsername) return null;

    const filteredFriends = friends.filter(friend =>
        friend.toLowerCase().includes(searchQuery.toLowerCase())
    );

    return (
        <div className="recommend-wrapper">
            <button className="recommend-main-btn" onClick={() => setIsOpen(true)}>
                Recommend to Friend
            </button>

            {isOpen && (
                <div className="rec-overlay" onClick={handleCloseAll}>
                    <div className="rec-modal" onClick={(e) => e.stopPropagation()}>

                        <div className="rec-header">
                            <h3>Recommend <span className="rec-neon-title">"{showName}"</span></h3>
                            <button className="rec-close" onClick={handleCloseAll}>✕</button>
                        </div>

                        {/* ️⃣ ეტაპი 1: მეგობრის არჩევა */}
                        {!selectedFriend ? (
                            <>
                                <input
                                    type="text"
                                    className="rec-search"
                                    placeholder="Search friends to recommend..."
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                />

                                <div className="rec-list" style={{ marginTop: '12px' }}>
                                    {filteredFriends.length === 0 ? (
                                        <div className="rec-empty">No friends found</div>
                                    ) : (
                                        filteredFriends.map(friendName => (
                                            <div
                                                key={friendName}
                                                className="rec-row"
                                                // 🛑 მკაცრი ბლოკი: თუ უკვე გაგზავნილია (sentStatus არის true), კლიკი საერთოდ არაფერს არ აკეთებს!
                                                onClick={() => !sentStatus[friendName] && setSelectedFriend(friendName)}
                                                style={{
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    justifyContent: 'space-between',
                                                    padding: '12px',
                                                    cursor: sentStatus[friendName] ? 'not-allowed' : 'pointer', // იცვლის მაუსის კურსორსაც (აკრძალვის ნიშანი)
                                                    borderRadius: '6px',
                                                    transition: 'all 0.2s',
                                                    opacity: sentStatus[friendName] ? 0.4 : 1, // ვიზუალურად ბევრად მკვეთრად ჩავამუქოთ
                                                    background: sentStatus[friendName] ? 'rgba(255,255,255,0.01)' : 'transparent'
                                                }}
                                                onMouseEnter={(e) => !sentStatus[friendName] && (e.currentTarget.style.background = 'rgba(0, 255, 213, 0.05)')}
                                                onMouseLeave={(e) => !sentStatus[friendName] && (e.currentTarget.style.background = 'transparent')}
                                            >
                                                <div className="rec-user-info">
                                                    <span className="rec-avatar">👤</span>
                                                    <span className="rec-username" style={{ marginLeft: '8px', color: sentStatus[friendName] ? '#8b949e' : '#fff' }}>{friendName}</span>
                                                </div>
                                                <span style={{ color: sentStatus[friendName] ? '#00ffd5' : '#8b949e', fontSize: '12px', fontWeight: sentStatus[friendName] ? '600' : 'normal' }}>
                                                    {sentStatus[friendName] ? '✓ Sent' : 'Select →'}
                                                </span>
                                            </div>
                                        ))
                                    )}
                                </div>
                            </>
                        ) : (
                            /* ️⃣ ეტაპი 2: კომენტარის დაწერა და გაგზავნა */
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', marginTop: '10px' }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', background: 'rgba(255,255,255,0.02)', padding: '10px', borderRadius: '6px' }}>
                                    <button
                                        onClick={() => setSelectedFriend(null)}
                                        style={{ background: 'none', border: 'none', color: '#00ffd5', cursor: 'pointer', fontSize: '13px', fontWeight: '500' }}
                                    >
                                        ← Back to List
                                    </button>
                                    <span style={{ color: '#8b949e', fontSize: '13px', marginLeft: 'auto' }}>To:</span>
                                    <strong style={{ color: '#fff' }}>{selectedFriend}</strong>
                                </div>

                                <textarea
                                    placeholder="Write a personal note (optional)..."
                                    value={comment}
                                    onChange={(e) => setComment(e.target.value)}
                                    disabled={sentStatus[selectedFriend]} // 🟢 იბლოკება ტექსტის წერაც გაგზავნის მერე
                                    maxLength={250}
                                    style={{
                                        width: '100%',
                                        height: '90px',
                                        background: 'rgba(0, 0, 0, 0.2)',
                                        border: '1px solid rgba(0, 255, 213, 0.15)',
                                        borderRadius: '6px',
                                        padding: '10px',
                                        fontSize: '13px',
                                        color: '#fff',
                                        outline: 'none',
                                        resize: 'none',
                                        fontFamily: 'inherit',
                                        transition: 'border-color 0.2s',
                                        opacity: sentStatus[selectedFriend] ? 0.5 : 1
                                    }}
                                    onFocus={(e) => e.target.style.borderColor = '#00ffd5'}
                                    onBlur={(e) => e.target.style.borderColor = 'rgba(0, 255, 213, 0.15)'}
                                />

                                <button
                                    onClick={handleSendRecommend}
                                    disabled={sentStatus[selectedFriend]} // 🟢 აბსოლუტური ბლოკი კლიკზე
                                    style={{
                                        width: '100%',
                                        padding: '12px',
                                        background: sentStatus[selectedFriend] ? 'rgba(0, 255, 213, 0.15)' : '#00ffd5',
                                        border: 'none',
                                        borderRadius: '6px',
                                        color: sentStatus[selectedFriend] ? '#00ffd5' : '#000',
                                        fontWeight: '600',
                                        cursor: sentStatus[selectedFriend] ? 'default' : 'pointer',
                                        transition: 'all 0.2s',
                                        boxShadow: sentStatus[selectedFriend] ? 'none' : '0 0 10px rgba(0, 255, 213, 0.3)'
                                    }}
                                >
                                    {sentStatus[selectedFriend] ? '✓ Recommendation Sent!' : 'Send Recommendation'}
                                </button>
                            </div>
                        )}

                    </div>
                </div>
            )}
        </div>
    );
}