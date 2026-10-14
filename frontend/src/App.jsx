import { useState, useEffect, useRef } from 'react'

import './App.css'

import Sidebar from './components/Sidebar'

import AuthModal from './components/AuthModal'

import { favoritesAPI, playlistsAPI, weatherAPI, songsAPI, eventsAPI, aiAPI } from './api/api'



function ProfileAvatar({ variant, large = false }) {

  const girl = variant === 'girl'



  return (

    <span className={`profile-avatar ${large ? 'large' : ''}`} aria-hidden="true">

      <svg viewBox="0 0 40 40">

        <circle cx="20" cy="20" r="20" fill={girl ? '#f0dce2' : '#dce7ed'} />

        <path d={girl ? 'M7 19C7 9 12 4 20 4s13 5 13 15v15H7z' : 'M9 17C9 9 13 5 20 5s11 4 11 12v5H9z'} fill={girl ? '#493443' : '#39313a'} />

        <path d="M7 40c.5-7 4.6-11 13-11s12.5 4 13 11z" fill={girl ? '#9c5b74' : '#607d91'} />

        <path d="M16 26h8v7h-8z" fill="#e8b99f" />

        <ellipse cx="20" cy="18" rx="7" ry="9" fill="#f1c8ad" />

        <path d={girl ? 'M12.5 16c.2-7 3-10 8-10 4.2 0 7.1 2.4 7.5 8-2.3-1.2-4-3-5-5-2 3.4-5.7 5.7-10.5 7z' : 'M13 14c.5-5 3-7 7-7 3.4 0 5.7 1.6 7 4.8-3.3-.7-6-.2-8.4 1.3-1.7 1-3.5 1.3-5.6.9z'} fill={girl ? '#493443' : '#39313a'} />

        <circle cx="17.5" cy="19" r=".7" fill="#49343a" />

        <circle cx="22.5" cy="19" r=".7" fill="#49343a" />

        <path d="M18 23c1.2 1 2.8 1 4 0" fill="none" stroke="#a56867" strokeLinecap="round" strokeWidth=".8" />

      </svg>

    </span>

  )
}

function getRandomSong(candidates) {
  if (!candidates || candidates.length === 0) return null
  const index = Math.floor(Math.random() * candidates.length)
  return candidates[index]
}

function App() {

  const getStoredUser = () => {

    try {

      const savedUser = localStorage.getItem('vibesync_user')

      if (!savedUser) return null



      const parsedUser = JSON.parse(savedUser)

      return parsedUser && typeof parsedUser === 'object' ? parsedUser : null

    } catch (error) {

      console.error('Invalid saved user data:', error)

      localStorage.removeItem('vibesync_user')

      return null

    }

  }



  const [showAuthModal, setShowAuthModal] = useState(() => {

    return !getStoredUser()

  })



  const [currentUser, setCurrentUser] = useState(() => getStoredUser())



  const [songs, setSongs] = useState([])

  const [loading, setLoading] = useState(true)

  const [error, setError] = useState(null)

  const [songsRetry, setSongsRetry] = useState(0)

  const [currentSong, setCurrentSong] = useState(null)

  const [isPlaying, setIsPlaying] = useState(false)

  const audioRef = useRef(null)
  const [currentTime, setCurrentTime] = useState(0)
  const [duration, setDuration] = useState(0)
  const [volume, setVolume] = useState(0.8)
  const [prevVolume, setPrevVolume] = useState(0.8)
  const [isShuffle, setIsShuffle] = useState(false)
  const [repeatMode, setRepeatMode] = useState('off')
  const [isDragging, setIsDragging] = useState(false)
  const [dragTime, setDragTime] = useState(0)

  const songsRef = useRef(songs)
  const currentSongRef = useRef(currentSong)
  const isShuffleRef = useRef(isShuffle)
  const repeatModeRef = useRef(repeatMode)
  const volumeRef = useRef(volume)

  useEffect(() => { songsRef.current = songs }, [songs])
  useEffect(() => { currentSongRef.current = currentSong }, [currentSong])
  useEffect(() => { isShuffleRef.current = isShuffle }, [isShuffle])
  useEffect(() => { repeatModeRef.current = repeatMode }, [repeatMode])
  useEffect(() => { volumeRef.current = volume }, [volume])

  const formatTime = (time) => {
    if (!Number.isFinite(time) || time < 0) return '0:00'
    const minutes = Math.floor(time / 60)
    const seconds = Math.floor(time % 60)
    return `${minutes}:${String(seconds).padStart(2, '0')}`
  }

  const seekTo = (value) => {
    const target = Number(value)
    if (!Number.isFinite(target)) return
    const audio = audioRef.current
    if (audio && Number.isFinite(audio.duration)) {
      audio.currentTime = target
    }
    setCurrentTime(target)
    setDragTime(target)
  }

  const handlePlayPause = () => {
    const audio = audioRef.current
    if (!audio) return
    if (audio.paused) {
      audio.play().then(() => setIsPlaying(true)).catch(err => {
        console.error('Audio playback failed:', err)
        setIsPlaying(false)
      })
    } else {
      audio.pause()
      setIsPlaying(false)
    }
  }

  const changeVolume = (value) => {
    const fraction = Math.max(0, Math.min(1, Number(value) / 100))
    setVolume(fraction)
    volumeRef.current = fraction
    if (audioRef.current) {
      audioRef.current.volume = fraction
      audioRef.current.muted = (fraction === 0)
    }
  }

  const toggleMute = () => {
    if (volume > 0) {
      setPrevVolume(volume)
      changeVolume(0)
    } else {
      const restored = prevVolume > 0 ? prevVolume * 100 : 80
      changeVolume(restored)
    }
  }

  const playAdjacentSong = (direction) => {
    const songList = songsRef.current
    if (!songList || songList.length === 0) return

    const current = currentSongRef.current
    const shuffleActive = isShuffleRef.current
    const repMode = repeatModeRef.current

    if (shuffleActive && direction === 1 && songList.length > 1) {
      const candidates = songList.filter(song => song.id !== current?.id)
      const randomSong = getRandomSong(candidates)
      if (randomSong) {
        playSong(randomSong)
      }
      return
    }

    const currentIndex = songList.findIndex(song => song.id === current?.id)
    if (currentIndex === -1) {
      if (songList.length > 0) playSong(songList[0])
      return
    }

    let nextIndex = currentIndex + direction

    if (nextIndex < 0) {
      if (repMode === 'all') {
        nextIndex = songList.length - 1
      } else {
        if (audioRef.current) {
          audioRef.current.currentTime = 0
        }
        setCurrentTime(0)
        return
      }
    }

    if (nextIndex >= songList.length) {
      if (repMode === 'all') {
        nextIndex = 0
      } else {
        if (audioRef.current) {
          audioRef.current.pause()
          audioRef.current.currentTime = 0
        }
        setIsPlaying(false)
        setCurrentTime(0)
        return
      }
    }

    playSong(songList[nextIndex])
  }

  const toggleRepeat = () => {
    setRepeatMode(mode => mode === 'off' ? 'all' : mode === 'all' ? 'one' : 'off')
  }

  const handleSongEnded = () => {
    const current = currentSongRef.current
    if (!current) return

    recordListeningEvent(current.id, 'COMPLETE')

    const repMode = repeatModeRef.current
    const shuffleActive = isShuffleRef.current
    const songList = songsRef.current

    if (repMode === 'one') {
      const audio = audioRef.current
      if (audio) {
        audio.currentTime = 0
        audio.play().then(() => setIsPlaying(true)).catch(err => {
          console.error('Audio replay failed:', err)
          setIsPlaying(false)
        })
        recordListeningEvent(current.id, 'REPLAY')
      }
      return
    }

    if (shuffleActive && songList.length > 1) {
      const candidates = songList.filter(song => song.id !== current.id)
      const randomSong = getRandomSong(candidates)
      if (randomSong) {
        playSong(randomSong)
      }
      return
    }

    const currentIndex = songList.findIndex(song => song.id === current.id)
    if (currentIndex === -1) return

    if (currentIndex < songList.length - 1) {
      playSong(songList[currentIndex + 1])
    } else {
      if (repMode === 'all') {
        playSong(songList[0])
      } else {
        setIsPlaying(false)
        setCurrentTime(0)
        if (audioRef.current) {
          audioRef.current.currentTime = 0
        }
      }
    }
  }

  const [currentTab, setCurrentTab] = useState('home')

  const [theme, setTheme] = useState(() => localStorage.getItem('vibesync_theme') === 'dark' ? 'dark' : 'light')

  const [profileAvatar, setProfileAvatar] = useState(() => localStorage.getItem('vibesync_profile_avatar') === 'girl' ? 'girl' : 'boy')

  const [searchQuery, setSearchQuery] = useState('')

  const [favourites, setFavourites] = useState([])

  const [recentlyPlayed, setRecentlyPlayed] = useState([])

  const [profileMenuOpen, setProfileMenuOpen] = useState(false)

  const [playlists, setPlaylists] = useState([])

  const [playlistName, setPlaylistName] = useState('')

  const [selectedPlaylist, setSelectedPlaylist] = useState(null)

  const [playlistSongId, setPlaylistSongId] = useState('')

  const [playlistError, setPlaylistError] = useState('')

  const [weatherCityInput, setWeatherCityInput] = useState('')

  const [weatherData, setWeatherData] = useState(null)

  const [weatherLoading, setWeatherLoading] = useState(false)

  const [weatherError, setWeatherError] = useState('')

  const [locationLoading, setLocationLoading] = useState(false)

  const [aiVibePick, setAiVibePick] = useState(null)

  const [aiLoading, setAiLoading] = useState(false)

  const [aiError, setAiError] = useState('')



  useEffect(() => {
    let isMounted = true

    songsAPI.getAll()
      .then(({ data }) => {
        if (!isMounted) return
        setSongs(Array.isArray(data) ? data : [])
        setLoading(false)
      })
      .catch(err => {
        if (!isMounted) return
        console.error('Error connecting to backend:', err)
        setError(err.response
          ? 'The song library could not be loaded.'
          : 'Cannot connect to the backend. Start Spring Boot, then retry.')
        setLoading(false)
      })

    return () => {
      isMounted = false
    }
  }, [songsRetry])



  useEffect(() => {

    const loadFavorites = async () => {

      if (!currentUser) {

        setFavourites([])

        return

      }



      try {

        const response = await favoritesAPI.getAll()

        const ids = Array.isArray(response.data)

          ? response.data.map(song => song.id)

          : []

        setFavourites(ids)

      } catch (err) {

        console.error('Failed to load favorites:', err)

        setFavourites([])

      }

    }



    const loadPlaylists = async () => {

      if (!currentUser) {

        setPlaylists([])

        return

      }



      try {

        const response = await playlistsAPI.getAll()

        setPlaylists(Array.isArray(response.data) ? response.data : [])

      } catch (err) {

        console.error('Failed to load playlists:', err)

        setPlaylists([])

      }

    }



    loadFavorites()

    loadPlaylists()

  }, [currentUser])



  const detectLocation = async () => {

    if (!navigator.geolocation) {

      setWeatherError('Location access is not available in this browser. Try typing a city name manually.')

      return

    }



    setLocationLoading(true)

    setWeatherLoading(true)

    setWeatherError('')



    navigator.geolocation.getCurrentPosition(

      async (position) => {

        try {

          const response = await weatherAPI.getVibeByCoordinates(

            position.coords.latitude,

            position.coords.longitude

          )

          setWeatherData(response.data)

        } catch (error) {

          console.error('Weather lookup by location failed:', error)

          setWeatherError(getWeatherErrorMessage(error))

          setWeatherData(null)

        } finally {

          setLocationLoading(false)

          setWeatherLoading(false)

        }

      },

      (error) => {

        console.error('Geolocation failed:', error)

        setWeatherError('Location permission was blocked. Enter a city manually to see your weather vibe.')

        setLocationLoading(false)

        setWeatherLoading(false)

      },

      { enableHighAccuracy: true, timeout: 15000 }

    )

  }



  const handleTabChange = (tab) => {

    if (tab === 'profile') {

      if (!currentUser) {

        setShowAuthModal(true)

        return

      }

    }



    setCurrentTab(tab)

    if (tab === 'weather') {

      if (weatherCityInput.trim()) {

        loadWeatherVibe(weatherCityInput)

      } else {

        detectLocation()

      }

    }

    setProfileMenuOpen(false)

  }



  const handleThemeChange = (nextTheme) => {

    localStorage.setItem('vibesync_theme', nextTheme)

    setTheme(nextTheme)

  }



  const handleLogin = (user) => {

    setCurrentUser(user)

    setShowAuthModal(false)

    setProfileMenuOpen(false)

    setCurrentTab('home')

  }



  const handleLogout = () => {

    localStorage.removeItem('vibesync_token')

    localStorage.removeItem('vibesync_user')

    setCurrentUser(null)

    setProfileMenuOpen(false)

    setShowAuthModal(true)

    setCurrentTab('home')

  }



  const createPlaylist = async () => {

    if (!currentUser || !playlistName.trim()) return



    try {

      const response = await playlistsAPI.create(playlistName.trim())

      setPlaylists(prev => [response.data, ...prev])

      setPlaylistName('')

      setPlaylistError('')

    } catch (err) {

      console.error('Failed to create playlist:', err)

      setPlaylistError('Could not create playlist. Please try again.')

    }

  }



  const openPlaylist = async (playlist) => {

    setPlaylistError('')

    try {

      const response = await playlistsAPI.getById(playlist.id)

      setSelectedPlaylist(response.data)

    } catch (err) {

      console.error('Failed to load playlist:', err)

      setPlaylistError('Could not load this playlist. Please try again.')

    }

  }



  const addSongToPlaylist = async () => {

    if (!selectedPlaylist || !playlistSongId) return



    try {

      await playlistsAPI.addSong(selectedPlaylist.id, playlistSongId)

      const response = await playlistsAPI.getById(selectedPlaylist.id)

      setSelectedPlaylist(response.data)

      setPlaylists(prev => prev.map(playlist => playlist.id === response.data.id

        ? { ...playlist, songIds: response.data.songs.map(song => song.id) }

        : playlist))

      setPlaylistSongId('')

      setPlaylistError('')

    } catch (err) {

      console.error('Failed to add song to playlist:', err)

      setPlaylistError('Could not add this song. Please try again.')

    }

  }



  const removeSongFromPlaylist = async (songId) => {

    if (!selectedPlaylist) return



    try {

      await playlistsAPI.removeSong(selectedPlaylist.id, songId)

      const response = await playlistsAPI.getById(selectedPlaylist.id)

      setSelectedPlaylist(response.data)

      setPlaylists(prev => prev.map(playlist => playlist.id === response.data.id

        ? { ...playlist, songIds: response.data.songs.map(song => song.id) }

        : playlist))

    } catch (err) {

      console.error('Failed to remove song from playlist:', err)

      setPlaylistError('Could not remove this song. Please try again.')

    }

  }



  const deletePlaylist = async () => {

    if (!selectedPlaylist) return



    try {

      await playlistsAPI.delete(selectedPlaylist.id)

      setPlaylists(prev => prev.filter(playlist => playlist.id !== selectedPlaylist.id))

      setSelectedPlaylist(null)

      setPlaylistError('')

    } catch (err) {

      console.error('Failed to delete playlist:', err)

      setPlaylistError('Could not delete this playlist. Please try again.')

    }

  }



  const loadWeatherVibe = async (city) => {

    if (!city || !city.trim()) return



    setWeatherLoading(true)

    setWeatherError('')



    try {

      const response = await weatherAPI.getVibe(city.trim())

      setWeatherData(response.data)

      setWeatherCityInput(city.trim())

    } catch (err) {

      console.error('Weather fetch failed:', err)

      setWeatherError(getWeatherErrorMessage(err))

      setWeatherData(null)

    } finally {

      setWeatherLoading(false)

    }

  }



  const getWeatherErrorMessage = (error) => {

    const backendMessage = error.response?.data?.message

      || error.response?.data?.detail



    if (backendMessage) return backendMessage

    if (error.response?.status === 403) {

      return 'The backend denied the weather request. Restart Spring Boot and try again.'

    }

    if (error.response?.status === 502 || error.response?.status === 503) {

      return error.response?.data?.detail || 'Weather is unavailable. Check the backend WEATHER_API_KEY and try again.'

    }

    if (error.response?.status === 400) {

      return 'No weather data was found for that location. Enter a nearby city or use your location.'

    }

    if (!error.response) {

      return 'Cannot reach the backend. Make sure Spring Boot is running on port 8081.'

    }

    return 'Weather lookup failed. Check the backend API key or try another city.'

  }



  const getAiVibePick = async () => {

    if (!currentUser) {

      setShowAuthModal(true)

      return

    }



    setAiLoading(true)

    setAiError('')

    try {

      const mood = currentSong?.mood || weatherData?.mood || recentlyPlayed[0]?.mood || 'Calm'

      const response = await aiAPI.getVibePick(mood)

      setAiVibePick(response.data)

    } catch (err) {

      const message = err.response?.data?.message || err.response?.data?.detail

      setAiError(message || 'Could not get an AI pick. Check that the backend is running and try again.')

      setAiVibePick(null)

    } finally {

      setAiLoading(false)

    }

  }



  const recordListeningEvent = async (songId, eventType) => {

    if (!currentUser) return



    try {

      await eventsAPI.record(songId, eventType)

    } catch (err) {

      console.error(`Failed to record ${eventType} event:`, err)

    }

  }



  const toggleFavourite = async (songId) => {

    if (!currentUser) {

      setShowAuthModal(true)

      return

    }



    const isCurrentlyFavorite = favourites.includes(songId)



    try {

      if (isCurrentlyFavorite) {

        await favoritesAPI.remove(songId)

        setFavourites(prev => prev.filter(id => id !== songId))

      } else {

        await favoritesAPI.add(songId)

        setFavourites(prev => [...prev, songId])

        recordListeningEvent(songId, 'LIKE')

      }

    } catch (err) {

      console.error('Favorite toggle failed:', err)

    }

  }



  const playSong = (song) => {
    const audioPlayer = audioRef.current

    if (currentSong?.id === song.id) {
      if (audioPlayer?.paused) {
        const isReplay = Number.isFinite(audioPlayer.duration)
          && audioPlayer.currentTime >= audioPlayer.duration - 1
        if (isReplay) {
          audioPlayer.currentTime = 0
          recordListeningEvent(song.id, 'REPLAY')
        } else {
          recordListeningEvent(song.id, 'PLAY')
        }
        audioPlayer.play().then(() => setIsPlaying(true)).catch(err => {
          console.error('Audio playback failed:', err)
          setIsPlaying(false)
        })
      } else {
        audioPlayer?.pause()
        setIsPlaying(false)
      }
      return
    }

    if (currentSong && audioPlayer && !audioPlayer.paused) {
      const isUnfinished = !Number.isFinite(audioPlayer.duration)
        || audioPlayer.currentTime < audioPlayer.duration - 1
      if (isUnfinished) recordListeningEvent(currentSong.id, 'SKIP')
    }

    // Reset playback progress when switching songs
    setCurrentTime(0)
    setDuration(0)
    if (audioPlayer) {
      audioPlayer.currentTime = 0
    }

    setCurrentSong(song)
    setIsPlaying(false)
    recordListeningEvent(song.id, 'PLAY')

    setRecentlyPlayed(prev => {
      const updated = [
        song,
        ...prev.filter(item => item.id !== song.id)
      ]

      return updated.slice(0, 5)
    })
  }



  const filteredSongs = songs.filter(song =>

    song.title.toLowerCase().includes(searchQuery.toLowerCase()) ||

    song.artist.toLowerCase().includes(searchQuery.toLowerCase()) ||

    song.genre.toLowerCase().includes(searchQuery.toLowerCase()) ||

    song.mood.toLowerCase().includes(searchQuery.toLowerCase())

  )



  const displayedSongs = currentTab === 'favorites'

    ? filteredSongs.filter(song => favourites.includes(song.id))

    : filteredSongs

  const isLibraryTab = ['home', 'search', 'favorites'].includes(currentTab)



  const profileName = currentUser ? (currentUser.name || currentUser.email?.split('@')[0] || 'Profile') : 'Login / Sign up'



  return (

    <div className="app-layout" data-theme={theme}>

      <Sidebar

        currentTab={currentTab}

        onTabChange={handleTabChange}

      />



      <div className="main-content">

        <header className="topbar">

          <div className="search-box">

            <span className="search-icon" aria-hidden="true" />



            <input

              type="text"

              placeholder="Search by song, artist, mood, or genre..."

              value={searchQuery}

              onChange={(e) => setSearchQuery(e.target.value)}

              className="search-input"

            />



            {searchQuery && (

              <button

                className="clear-btn"

                onClick={() => setSearchQuery('')}

              >

                ✕

              </button>

            )}

          </div>



          <div className="topbar-actions">

            <div className="theme-switch" aria-label="Choose color theme">

              <button

                type="button"

                className={theme === 'light' ? 'selected' : ''}

                aria-pressed={theme === 'light'}

                onClick={() => handleThemeChange('light')}

              >

                Light

              </button>

              <button

                type="button"

                className={theme === 'dark' ? 'selected' : ''}

                aria-pressed={theme === 'dark'}

                onClick={() => handleThemeChange('dark')}

              >

                Dark

              </button>

            </div>



            <div className="user-profile-area">

              <button

                className="user-profile"

                onClick={() => {

                  if (!currentUser) {

                    setShowAuthModal(true)

                    return

                  }

                  setProfileMenuOpen(prev => !prev)

                }}

              >

                {currentUser && (

                  <ProfileAvatar variant={profileAvatar} />

                )}

                <span className="username">{profileName}</span>

              </button>



              {profileMenuOpen && currentUser && (

                <div className="profile-menu">

                  <button type="button" onClick={() => { setCurrentTab('profile'); setProfileMenuOpen(false) }}>

                    Profile

                  </button>

                  <button type="button" onClick={handleLogout}>

                    Logout

                  </button>

                </div>

              )}

            </div>

          </div>

        </header>



        <div className="content-body">



          {currentTab === 'home' && (

            <>

              <section className="weather-notification">

                <div className="weather-notification-copy">

                  <span className="notification-badge">Weather-based rec</span>

                  <strong>

                    {weatherData

                      ? `${weatherData.city}: ${weatherData.mood} vibe`

                      : 'Check the weather around you'}

                  </strong>

                  <p>

                    {weatherData

                      ? `${weatherData.condition} • ${weatherData.moodDescription}`

                      : 'Use your location or enter a city to get song suggestions that match the weather.'}

                  </p>

                </div>

                <button type="button" onClick={() => setCurrentTab('weather')}>

                  {weatherData ? 'View vibe' : 'Get vibe'}

                </button>

              </section>



              <section className="ai-card">

                <div className="ai-heading">

                  <div>

                    <strong className="mood-picker-title">Song suggestion</strong>

                    <p>One pick based on your current listening mood.</p>

                  </div>

                  <div className="ai-controls">

                    <button type="button" onClick={getAiVibePick} disabled={aiLoading}>

                      {aiLoading ? 'Choosing...' : 'Get suggestion'}

                    </button>

                  </div>

                </div>



                {aiError && <div className="status-msg error">{aiError}</div>}

                {aiVibePick && (

                  <div className="ai-pick-result">

                    <img src={aiVibePick.song.coverUrl} alt="" />

                    <div className="ai-pick-info">

                      <strong>{aiVibePick.song.title}</strong>

                      <span>{aiVibePick.song.artist}</span>

                      <p>{aiVibePick.reason}</p>

                    </div>

                    <button type="button" onClick={() => playSong(aiVibePick.song)}>Play</button>

                  </div>

                )}

              </section>

            </>

          )}



          {currentTab === 'profile' && currentUser && (

            <section className="profile-panel">

              <div className="profile-layout">

                <div className="profile-card">

                  <div className="profile-header">

                    <ProfileAvatar variant={profileAvatar} large />

                    <div>

                      <span className="profile-eyebrow">Your music profile</span>

                      <h2>{currentUser.name || 'Music Lover'}</h2>

                      <p>{currentUser.email}</p>

                    </div>

                  </div>



                  <div className="profile-meta">

                    <div>

                      <label>Favorite tracks</label>

                      <strong>{favourites.length}</strong>

                    </div>

                    <div>

                      <label>Playlists</label>

                      <strong>{playlists.length}</strong>

                    </div>

                    <div>

                      <label>Recently played</label>

                      <strong>{recentlyPlayed.length}</strong>

                    </div>

                    <div>

                      <label>Account role</label>

                      <strong>{currentUser.role || 'USER'}</strong>

                    </div>

                  </div>

                </div>



                <section className="profile-settings">

                  <div className="profile-settings-heading">

                    <h3>Settings</h3>

                    <p>Personalize your VibeSync profile.</p>

                  </div>



                  <div className="profile-setting-row">

                    <div>

                      <strong>Profile avatar</strong>

                      <span>Choose the illustration shown beside your name.</span>

                    </div>

                    <div className="profile-avatar-options" role="group" aria-label="Choose profile avatar">

                      {['boy', 'girl'].map(variant => (

                        <button

                          key={variant}

                          type="button"

                          className={profileAvatar === variant ? 'selected' : ''}

                          aria-pressed={profileAvatar === variant}

                          onClick={() => {

                            localStorage.setItem('vibesync_profile_avatar', variant)

                            setProfileAvatar(variant)

                          }}

                        >

                          <ProfileAvatar variant={variant} />

                          <span>{variant === 'boy' ? 'Boy' : 'Girl'}</span>

                        </button>

                      ))}

                    </div>

                  </div>



                  <div className="profile-setting-row">

                    <div>

                      <strong>Color theme</strong>

                      <span>Your preference is saved on this device.</span>

                    </div>

                    <div className="profile-theme-options" role="group" aria-label="Choose color theme">

                      {['light', 'dark'].map(option => (

                        <button

                          key={option}

                          type="button"

                          className={theme === option ? 'selected' : ''}

                          aria-pressed={theme === option}

                          onClick={() => handleThemeChange(option)}

                        >

                          {option === 'light' ? 'Light' : 'Dark'}

                        </button>

                      ))}

                    </div>

                  </div>

                </section>

              </div>

            </section>

          )}



          {currentTab === 'playlists' && (

            <section className="playlist-panel">

              <div className="playlist-heading">

                <div>

                  <h2>{selectedPlaylist ? selectedPlaylist.name : 'Your Playlists'}</h2>

                  <p>{selectedPlaylist

                    ? `${selectedPlaylist.songs?.length || 0} songs`

                    : 'Create and manage your song collections.'}</p>

                </div>

                {selectedPlaylist && (

                  <div className="playlist-actions">

                    <button type="button" onClick={() => setSelectedPlaylist(null)}>All playlists</button>

                    <button type="button" className="playlist-delete-btn" onClick={deletePlaylist}>Delete playlist</button>

                  </div>

                )}

              </div>



              {playlistError && <div className="status-msg error">{playlistError}</div>}



              {!selectedPlaylist && (

                <>

                  <div className="playlist-creator">

                    <input

                      type="text"

                      placeholder="Create a playlist name"

                      value={playlistName}

                      onChange={(e) => setPlaylistName(e.target.value)}

                      onKeyDown={(e) => { if (e.key === 'Enter') createPlaylist() }}

                    />

                    <button type="button" onClick={createPlaylist}>Create</button>

                  </div>



                  <div className="playlist-grid">

                    {playlists.length === 0 ? (

                      <div className="empty-state small">

                        <h3>No playlists yet</h3>

                        <p>Create your first playlist.</p>

                      </div>

                    ) : playlists.map(playlist => (

                      <button

                        key={playlist.id}

                        type="button"

                        className="playlist-card"

                        onClick={() => openPlaylist(playlist)}

                      >

                        <h3>{playlist.name}</h3>

                        <p>{playlist.songIds?.length || 0} songs</p>

                      </button>

                    ))}

                  </div>

                </>

              )}



              {selectedPlaylist && (

                <>

                  <div className="playlist-add-song">

                    <select value={playlistSongId} onChange={(e) => setPlaylistSongId(e.target.value)}>

                      <option value="">Choose a song to add</option>

                      {songs

                        .filter(song => !selectedPlaylist.songs?.some(playlistSong => playlistSong.id === song.id))

                        .map(song => <option key={song.id} value={song.id}>{song.title} - {song.artist}</option>)}

                    </select>

                    <button type="button" onClick={addSongToPlaylist} disabled={!playlistSongId}>Add song</button>

                  </div>



                  {selectedPlaylist.songs?.length ? (

                    <div className="playlist-song-list">

                      {selectedPlaylist.songs.map(song => (

                        <div className="playlist-song-row" key={song.id}>

                          <button type="button" className="playlist-song-play" onClick={() => playSong(song)}>Play</button>

                          <div className="playlist-song-copy">

                            <strong>{song.title}</strong>

                            <span>{song.artist}</span>

                          </div>

                          <button type="button" className="playlist-remove-btn" onClick={() => removeSongFromPlaylist(song.id)}>Remove</button>

                        </div>

                      ))}

                    </div>

                  ) : (

                    <div className="empty-state small">

                      <h3>This playlist is empty</h3>

                      <p>Choose a song above to add it.</p>

                    </div>

                  )}

                </>

              )}

            </section>

          )}



          {currentTab === 'weather' && (

            <section className="weather-panel">

              <div className="weather-header-row">

                <div>

                  <span className="weather-pill">Weather Vibe</span>

                  <h3>Music matched to the weather outside</h3>

                  <p className="weather-attribution">Song picks based on your location</p>

                </div>

              </div>



              <div className="weather-city-box">

                <input

                  type="text"

                  value={weatherCityInput}

                  onChange={(e) => setWeatherCityInput(e.target.value)}

                  onKeyDown={(e) => {

                    if (e.key === 'Enter') {

                      e.preventDefault()

                      loadWeatherVibe(weatherCityInput)

                    }

                  }}

                  placeholder="Enter city and press Enter"

                />

                <button

                  type="button"

                  onClick={detectLocation}

                  disabled={locationLoading}

                >

                  {locationLoading ? 'Detecting...' : 'Use my location'}

                </button>

              </div>



              {weatherLoading && <div className="status-msg">Loading weather vibe...</div>}

              {weatherError && <div className="status-msg error">{weatherError}</div>}



              {weatherData && (

                <>

                  <div className="weather-result">

                    <div>

                      <p className="weather-city">{weatherData.city}</p>

                      <span>{weatherData.condition} · {weatherData.description}</span>

                    </div>



                    <div className="weather-mood-box">

                      <span>{weatherData.mood}</span>

                      <strong>{weatherData.moodDescription}</strong>

                    </div>

                  </div>



                  <div className="section-header">

                    <h2>Suggested songs for {weatherData.mood}</h2>

                    <span className="count-badge">{weatherData.songs?.length || 0} tracks</span>

                  </div>



                  <div className="song-grid">

                    {(weatherData.songs || []).map(song => (

                      <div

                        key={song.id}

                        className={`song-card ${currentSong?.id === song.id ? 'active-playing' : ''}`}

                        onClick={() => playSong(song)}

                      >

                        <div className="cover-wrapper" data-title={song.title}>

                          <img

                            src={song.coverUrl}

                            alt={song.title}

                            className="cover-img"

                            onError={(event) => {

                              event.currentTarget.hidden = true

                              event.currentTarget.parentElement?.classList.add('cover-unavailable')

                            }}

                          />



                          <button

                            className="play-overlay-btn"

                            onClick={(e) => {

                              e.stopPropagation()

                              playSong(song)

                            }}

                          >

                            {currentSong?.id === song.id && isPlaying ? '⏸' : '▶'}

                          </button>



                          <button

                            className="favorite-btn"

                            onClick={(e) => {

                              e.stopPropagation()

                              toggleFavourite(song.id)

                            }}

                          >

                            {favourites.includes(song.id) ? '❤️' : '♡'}

                          </button>

                        </div>



                        <div className="song-info">

                          {currentSong?.id === song.id && isPlaying && (

                            <span className="now-playing-indicator" aria-label="Now playing">

                              <span className="playing-bars" aria-hidden="true"><i /><i /><i /></span>

                              Now playing

                            </span>

                          )}

                          <h3 className="song-title">{song.title}</h3>

                          <p className="song-artist">{song.artist}</p>



                          <div className="tags">

                            <span className="tag mood-tag">{song.mood}</span>

                            <span className="tag genre-tag">{song.genre}</span>

                          </div>

                        </div>

                      </div>

                    ))}

                  </div>



                  {(!weatherData.songs || weatherData.songs.length === 0) && (

                    <div className="empty-state small">

                      <h3>No songs found for this weather mood</h3>

                      <p>Try another city or switch to a different vibe.</p>

                    </div>

                  )}

                </>

              )}

            </section>

          )}



          {/* Status Messages */}

          {isLibraryTab && loading && (

            <div className="status-msg">

              Connecting to database...

            </div>

          )}



          {isLibraryTab && error && (

            <div className="status-msg error" role="alert">

              <span>{error}</span>

              <button
                type="button"
                className="retry-button"
                onClick={() => {
                  setLoading(true)
                  setError(null)
                  setSongsRetry(retry => retry + 1)
                }}
              >

                Retry

              </button>

            </div>

          )}



          {/* Section Header */}

          {isLibraryTab && !error && <div className="section-header">



            <h2>

              {currentTab === 'favorites'

                ? 'Your Favorites'

                : searchQuery

                  ? `Results for "${searchQuery}"`

                  : 'Songs'}

            </h2>



            <span className="count-badge">

              {displayedSongs.length} Songs

            </span>



          </div>}



          {/* Main Song Grid */}

          {isLibraryTab && !error && <div className="song-grid">



            {displayedSongs.map(song => (



              <div

                key={song.id}

                className={`song-card ${currentSong?.id === song.id

                  ? 'active-playing'

                  : ''

                  }`}

                onClick={() => playSong(song)}

              >



                {/* Album Cover */}

                <div className="cover-wrapper" data-title={song.title}>



                  <img

                    src={song.coverUrl}

                    alt={song.title}

                    className="cover-img"

                    onError={(event) => {

                      event.currentTarget.hidden = true

                      event.currentTarget.parentElement?.classList.add('cover-unavailable')

                    }}

                  />



                  {/* Play Button */}

                  <button

                    className="play-overlay-btn"

                    onClick={(e) => {

                      e.stopPropagation()

                      playSong(song)

                    }}

                  >

                    {currentSong?.id === song.id && isPlaying ? '⏸' : '▶'}

                  </button>



                  {/* Favourite Button */}

                  <button

                    className="favorite-btn"

                    onClick={(e) => {

                      e.stopPropagation()

                      toggleFavourite(song.id)

                    }}

                  >

                    {favourites.includes(song.id) ? '❤️' : '♡'}

                  </button>



                </div>



                {/* Song Information */}

                <div className="song-info">



                  {currentSong?.id === song.id && isPlaying && (

                    <span className="now-playing-indicator" aria-label="Now playing">

                      <span className="playing-bars" aria-hidden="true"><i /><i /><i /></span>

                      Now playing

                    </span>

                  )}



                  <h3 className="song-title">

                    {song.title}

                  </h3>



                  <p className="song-artist">

                    {song.artist}

                  </p>



                  <div className="tags">



                    <span className="tag mood-tag">

                      {song.mood}

                    </span>



                    <span className="tag genre-tag">

                      {song.genre}

                    </span>



                  </div>



                </div>



              </div>



            ))}



          </div>}



          {/* Recently Played */}

          {currentTab === 'home' && recentlyPlayed.length > 0 && (

            <section className="recent-section">



              <div className="section-header">

                <h2>Recently Played</h2>



                <span className="count-badge">

                  {recentlyPlayed.length} Songs

                </span>

              </div>



              <div className="song-grid">



                {recentlyPlayed.map(song => (



                  <div

                    key={`recent-${song.id}`}

                    className={`song-card ${currentSong?.id === song.id

                      ? 'active-playing'

                      : ''

                      }`}

                    onClick={() => playSong(song)}

                  >



                    <div className="cover-wrapper" data-title={song.title}>



                      <img

                        src={song.coverUrl}

                        alt={song.title}

                        className="cover-img"

                        onError={(event) => {

                          event.currentTarget.hidden = true

                          event.currentTarget.parentElement?.classList.add('cover-unavailable')

                        }}

                      />



                      <button

                        className="play-overlay-btn"

                        onClick={(e) => {

                          e.stopPropagation()

                          playSong(song)

                        }}

                      >

                        {currentSong?.id === song.id && isPlaying ? '⏸' : '▶'}

                      </button>



                    </div>



                    <div className="song-info">



                      {currentSong?.id === song.id && isPlaying && (

                        <span className="now-playing-indicator" aria-label="Now playing">

                          <span className="playing-bars" aria-hidden="true"><i /><i /><i /></span>

                          Now playing

                        </span>

                      )}



                      <h3 className="song-title">

                        {song.title}

                      </h3>



                      <p className="song-artist">

                        {song.artist}

                      </p>



                      <div className="tags">



                        <span className="tag mood-tag">

                          {song.mood}

                        </span>



                        <span className="tag genre-tag">

                          {song.genre}

                        </span>



                      </div>



                    </div>



                  </div>



                ))}



              </div>



            </section>

          )}



          {/* Empty State */}

          {isLibraryTab && displayedSongs.length === 0 && !loading && !error && (



            <div className="empty-state">



              <h3>

                {currentTab === 'favorites'

                  ? 'No favorite songs yet'

                  : searchQuery

                    ? `No songs found for "${searchQuery}"`

                    : 'No songs in your library yet'}

              </h3>



              <p>

                {currentTab === 'favorites'

                  ? 'Favorite a song to keep it here.'

                  : searchQuery

                    ? 'Try a different title, artist, mood, or genre.'

                    : 'Songs will appear here once the library is populated.'}

              </p>



            </div>



          )}



        </div>



      </div>



      {currentSong && (
        <footer className="player-bar" aria-label="Music player">
          <div className="player-track">
            <div className="player-thumb-wrapper" data-title={currentSong.title}>
              <img
                src={currentSong.coverUrl}
                alt={currentSong.title || 'Current song artwork'}
                className="player-thumb"
                onError={(event) => {
                  event.currentTarget.hidden = true
                  event.currentTarget.parentElement?.classList.add('player-thumb-fallback')
                }}
              />
            </div>
            <div className="player-info">
              <span className="player-title" title={currentSong.title}>{currentSong.title}</span>
              <span className="player-artist" title={currentSong.artist}>{currentSong.artist}</span>
            </div>
            <button
              type="button"
              className={`player-favorite ${favourites.includes(currentSong.id) ? 'liked' : ''}`}
              onClick={() => toggleFavourite(currentSong.id)}
              aria-label={favourites.includes(currentSong.id) ? 'Remove from favourites' : 'Add to favourites'}
              title={favourites.includes(currentSong.id) ? 'Remove from favourites' : 'Add to favourites'}
            >
              {favourites.includes(currentSong.id) ? '♥' : '♡'}
            </button>
          </div>

          <div className="player-center">
            <div className="player-controls">
              <button
                type="button"
                className={`player-icon-btn player-mode-btn shuffle-btn ${isShuffle ? 'active' : ''}`}
                onClick={() => setIsShuffle(prev => !prev)}
                aria-label={isShuffle ? 'Turn shuffle off' : 'Turn shuffle on'}
                aria-pressed={isShuffle}
                title={isShuffle ? 'Shuffle on' : 'Shuffle off'}
              >
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <polyline points="16 3 21 3 21 8" />
                  <line x1="4" y1="20" x2="21" y2="3" />
                  <polyline points="21 16 21 21 16 21" />
                  <line x1="15" y1="15" x2="21" y2="21" />
                  <line x1="4" y1="4" x2="9" y2="9" />
                </svg>
              </button>

              <button
                type="button"
                className="player-icon-btn prev-btn"
                onClick={() => playAdjacentSong(-1)}
                aria-label="Previous song"
                title="Previous song"
              >
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <polygon points="19 20 9 12 19 4 19 20" />
                  <line x1="5" y1="19" x2="5" y2="5" />
                </svg>
              </button>

              <button
                type="button"
                className="player-main-btn play-pause-btn"
                onClick={handlePlayPause}
                aria-label={isPlaying ? 'Pause' : 'Play'}
                title={isPlaying ? 'Pause' : 'Play'}
              >
                {isPlaying ? (
                  <svg viewBox="0 0 24 24" aria-hidden="true" fill="currentColor">
                    <rect x="6" y="4" width="4" height="16" rx="1" />
                    <rect x="14" y="4" width="4" height="16" rx="1" />
                  </svg>
                ) : (
                  <svg viewBox="0 0 24 24" aria-hidden="true" fill="currentColor">
                    <polygon points="6 4 20 12 6 20 6 4" />
                  </svg>
                )}
              </button>

              <button
                type="button"
                className="player-icon-btn next-btn"
                onClick={() => playAdjacentSong(1)}
                aria-label="Next song"
                title="Next song"
              >
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <polygon points="5 4 15 12 5 20 5 4" />
                  <line x1="19" y1="5" x2="19" y2="19" />
                </svg>
              </button>

              <button
                type="button"
                className={`player-icon-btn player-mode-btn repeat-btn ${repeatMode !== 'off' ? 'active' : ''}`}
                onClick={toggleRepeat}
                aria-label={`Repeat mode: ${repeatMode === 'off' ? 'Off' : repeatMode === 'all' ? 'Repeat All' : 'Repeat One'}. Click to cycle.`}
                aria-pressed={repeatMode !== 'off'}
                title={`Repeat: ${repeatMode === 'off' ? 'Off' : repeatMode === 'all' ? 'All' : 'One'}`}
              >
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <polyline points="17 1 21 5 17 9" />
                  <path d="M3 11V9a4 4 0 0 1 4-4h14" />
                  <polyline points="7 23 3 19 7 15" />
                  <path d="M21 13v2a4 4 0 0 1-4 4H3" />
                  {repeatMode === 'one' && (
                    <text x="12" y="15" textAnchor="middle" fontSize="9" fontWeight="bold" fill="currentColor" stroke="none">1</text>
                  )}
                </svg>
              </button>
            </div>

            <div className="player-progress">
              <span className="time-display current-time">
                {formatTime(isDragging ? dragTime : currentTime)}
              </span>
              <div className="progress-slider-wrapper">
                <input
                  type="range"
                  min="0"
                  max={duration > 0 ? duration : 0}
                  step="0.1"
                  value={Math.min(isDragging ? dragTime : currentTime, duration || 0)}
                  onMouseDown={() => {
                    setIsDragging(true)
                    setDragTime(currentTime)
                  }}
                  onTouchStart={() => {
                    setIsDragging(true)
                    setDragTime(currentTime)
                  }}
                  onInput={(event) => {
                    setDragTime(Number(event.target.value))
                  }}
                  onChange={(event) => {
                    seekTo(event.target.value)
                    setIsDragging(false)
                  }}
                  onMouseUp={(event) => {
                    seekTo(event.target.value)
                    setIsDragging(false)
                  }}
                  onTouchEnd={(event) => {
                    seekTo(event.target.value)
                    setIsDragging(false)
                  }}
                  style={{
                    '--progress': `${duration > 0 ? ((isDragging ? dragTime : currentTime) / duration) * 100 : 0}%`
                  }}
                  aria-label="Song progress"
                  aria-valuemin="0"
                  aria-valuemax={Math.round(duration || 0)}
                  aria-valuenow={Math.round(isDragging ? dragTime : currentTime)}
                  aria-valuetext={`${formatTime(isDragging ? dragTime : currentTime)} of ${formatTime(duration)}`}
                  title="Seek playback time"
                />
              </div>
              <span className="time-display total-duration">{formatTime(duration)}</span>
            </div>
          </div>

          <div className="player-volume">
            <button
              type="button"
              className="player-icon-btn volume-btn"
              onClick={toggleMute}
              aria-label={volume === 0 ? 'Unmute' : 'Mute'}
              title={volume === 0 ? 'Unmute' : 'Mute'}
            >
              {volume === 0 ? (
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
                  <line x1="23" y1="9" x2="17" y2="15" />
                  <line x1="17" y1="9" x2="23" y2="15" />
                </svg>
              ) : volume < 0.5 ? (
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
                  <path d="M15.54 8.46a5 5 0 0 1 0 7.07" />
                </svg>
              ) : (
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
                  <path d="M15.54 8.46a5 5 0 0 1 0 7.07" />
                  <path d="M19.07 4.93a10 10 0 0 1 0 14.14" />
                </svg>
              )}
            </button>
            <input
              type="range"
              min="0"
              max="100"
              step="1"
              value={Math.round(volume * 100)}
              onChange={(event) => changeVolume(event.target.value)}
              style={{ '--progress': `${volume * 100}%` }}
              aria-label="Volume"
              aria-valuemin="0"
              aria-valuemax="100"
              aria-valuenow={Math.round(volume * 100)}
              aria-valuetext={`${Math.round(volume * 100)} percent`}
              title={`Volume: ${Math.round(volume * 100)}%`}
            />
            <span className="volume-percent">{Math.round(volume * 100)}%</span>
          </div>

          <audio
            ref={audioRef}
            key={currentSong.id}
            autoPlay
            preload="metadata"
            src={currentSong.audioUrl}
            className="audio-element"
            onPlay={() => setIsPlaying(true)}
            onPause={() => setIsPlaying(false)}
            onTimeUpdate={(event) => {
              if (!isDragging) {
                setCurrentTime(event.currentTarget.currentTime)
              }
            }}
            onLoadedMetadata={(event) => {
              const audio = event.currentTarget
              const dur = Number.isFinite(audio.duration) ? audio.duration : 0
              setDuration(dur)
              audio.volume = volumeRef.current
              audio.muted = (volumeRef.current === 0)
            }}
            onError={(event) => {
              console.error('Audio playback error on source:', event)
              setIsPlaying(false)
            }}
            onEnded={handleSongEnded}
          />
        </footer>
      )}


      {showAuthModal && (

        <AuthModal

          onClose={() => setShowAuthModal(false)}

          onLogin={handleLogin}

        />

      )}

    </div>

  )

}



export default App