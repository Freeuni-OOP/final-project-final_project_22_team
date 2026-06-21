import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';

function ShowsDetailsPage() {
    const { id } = useParams();
    const [showData, setShowData] = useState(null);
    const [activeStatus, setActiveStatus] = useState(null);
    const [watchedEpisodes, setWatchedEpisodes] = useState([]);


    const tokenObj = localStorage.getItem('token');
    const token = tokenObj ? JSON.parse(tokenObj).token : null;

    if (token) {
        const payload = JSON.parse(atob(token.split('.')[1]));
        console.log("ტოკენის შიგთავსი:", payload);
        console.log("ამჟამინდელი დრო:", Math.floor(Date.now() / 1000));
    }

    const parseJwt = (token) => {
        try {
            return JSON.parse(atob(token.split('.')[1]));
        } catch (e) {
            return null;
        }
    };

    const decodedToken = token ? parseJwt(token) : null;

    const currentUserId = decodedToken ? decodedToken.id : null;

    useEffect(() => {
        // load shows details (if someone is logged in or not)
        fetch(`https://localhost:8443/api/shows/${id}`)
            .then(res => res.json())
            .then(data => setShowData(data));

        // do it only when someone is logged in
        if (decodedToken?.sub) {
            fetch(`https://localhost:8443/api/tracking/watched-episodes?username=${decodedToken.sub}&showId=${id}`, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            })
                .then(res => res.json())
                .then(data => setWatchedEpisodes(data))
                .catch(err => console.error("Error fetching watched episodes:", err));
        }
    }, [id, currentUserId, token]);

    //  Serial status update
    const handleStatusUpdate = (statusName) => {
        const newStatus = activeStatus === statusName ? null : statusName;
        setActiveStatus(newStatus);

        console.log("გასაგზავნი ტოკენი:", token);

        // რადგან ტოკენში ID არ გვიზის, userId-ის ნაცვლად ბექენდს გადავცეთ username (მაგ. 'me')
        fetch(`https://localhost:8443/api/tracking/show-status?username=${decodedToken?.sub}&showId=${id}&status=${statusName}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
            .then(res => {
                console.log("ბექენდის სტატუს კოდი:", res.status);
                return res.text();
            })
            .then(data => console.log("ბექენდის პასუხი:", data))
            .catch(err => console.error("რექვესთი ჩავარდა:", err));
    };

    // episode toggle
    const handleEpisodeToggle = (seasonNum, episodeNum) => {
        fetch(`https://localhost:8443/api/tracking/toggle-episode?username=${decodedToken?.sub}&showId=${id}&seasonNumber=${seasonNum}&episodeNumber=${episodeNum}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
            .then(() => {
                const isAlreadyWatched = watchedEpisodes.some(ep => ep.seasonNumber === seasonNum && ep.episodeNumber === episodeNum);
                if (isAlreadyWatched) {
                    setWatchedEpisodes(watchedEpisodes.filter(ep => !(ep.seasonNumber === seasonNum && ep.episodeNumber === episodeNum)));
                } else {
                    setWatchedEpisodes([...watchedEpisodes, { seasonNumber: seasonNum, episodeNumber: episodeNum }]);
                }
            });
    };

    // checks if this episode is watched in database
    const isEpisodeWatched = (seasonNum, episodeNum) => {
        return watchedEpisodes.some(ep => ep.seasonNumber === seasonNum && ep.episodeNumber === episodeNum);
    };

    if (!showData) return <div className="text-white text-center mt-10">loading...</div>;

    return (
        <div className="p-8 bg-neutral-900 text-white min-h-screen">

            {/* upper section: show's poster and details */}
            <div className="flex flex-col md:flex-row gap-8 items-start mb-10">
                <img
                    src={`https://image.tmdb.org/t/p/w300${showData.poster_path}`}
                    alt={showData.name}
                    className="rounded-2xl shadow-2xl border border-neutral-800"
                />
                <div className="flex-1">
                    <h1 className="text-4xl font-bold mb-3">{showData.name}</h1>
                    <div className="text-yellow-500 font-semibold mb-4">⭐ {showData.vote_average?.toFixed(1)} / 10</div>
                    <p className="text-neutral-400 leading-relaxed mb-6 max-w-2xl">{showData.overview}</p>

                    {/* status manage buttons */}
                    <div className="bg-neutral-800 p-4 rounded-2xl border border-neutral-700 max-w-xl">
                        <div className="text-xs text-neutral-400 font-semibold uppercase tracking-wider mb-3">მიანიჭე სტატუსი:</div>
                        <div className="flex flex-wrap gap-2">

                            {/* 👁️ WATCHING */}
                            <button
                                onClick={() => handleStatusUpdate('WATCHING')}
                                className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
                                    activeStatus === 'WATCHING' ? 'bg-blue-600 text-white shadow-lg shadow-blue-600/30' : 'bg-neutral-700 hover:bg-neutral-600 text-neutral-300'
                                }`}
                            >
                                👁️ Watching
                            </button>

                            {/* ✅ COMPLETED */}
                            <button
                                onClick={() => handleStatusUpdate('COMPLETED')}
                                className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
                                    activeStatus === 'COMPLETED' ? 'bg-green-600 text-white shadow-lg shadow-green-600/30' : 'bg-neutral-700 hover:bg-neutral-600 text-neutral-300'
                                }`}
                            >
                                ✅ Completed
                            </button>

                            {/* 📌 PLAN TO WATCH */}
                            <button
                                onClick={() => handleStatusUpdate('PLAN_TO_WATCH')}
                                className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
                                    activeStatus === 'PLAN_TO_WATCH' ? 'bg-yellow-600 text-white shadow-lg shadow-yellow-600/30' : 'bg-neutral-700 hover:bg-neutral-600 text-neutral-300'
                                }`}
                            >
                                📌 Plan to Watch
                            </button>

                            {/* ❌ DROPPED */}
                            <button
                                onClick={() => handleStatusUpdate('DROPPED')}
                                className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
                                    activeStatus === 'DROPPED' ? 'bg-red-600 text-white shadow-lg shadow-red-600/30' : 'bg-neutral-700 hover:bg-neutral-600 text-neutral-300'
                                }`}
                            >
                                ❌ Dropped
                            </button>

                        </div>
                    </div>
                </div>
            </div>

            <hr className="border-neutral-800 my-8" />

            {/* lower section: seasons and episodes */}
            <h2 className="text-2xl font-bold mb-4">Episode Tracking</h2>
            <div className="space-y-3 max-w-3xl">
                {/* for example, first season's episodes*/}
                {showData.seasons?.[0]?.episodes?.map((episode) => {
                    const isWatched = isEpisodeWatched(1, episode.episode_number);

                    return (
                        <div key={episode.id} className="flex items-center justify-between bg-neutral-800 p-4 rounded-xl border border-neutral-700 hover:border-neutral-600 transition-all">
                            <div>
                                <span className="text-blue-400 font-bold mr-3 text-sm">ეპ. {episode.episode_number}</span>
                                <span className="font-medium text-neutral-200">{episode.name}</span>
                            </div>

                            {/* 🔳 Check / Uncheck Toggle button */}
                            <button
                                onClick={() => handleEpisodeToggle(1, episode.episode_number)}
                                className={`px-4 py-1.5 rounded-xl text-xs font-bold transition-all ${
                                    isWatched
                                        ? 'bg-green-600 hover:bg-green-700 text-white shadow-md'
                                        : 'bg-neutral-700 hover:bg-neutral-600 text-neutral-300'
                                }`}
                            >
                                {isWatched ? '✓ ნანახია' : 'მონიშვნა'}
                            </button>
                        </div>
                    );
                })}
            </div>

        </div>
    );
}

export default ShowsDetailsPage;
