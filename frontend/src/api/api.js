// Central API client for VibeSync
// All backend calls go through here — JWT token is attached automatically

import axios from 'axios'

// Base URL for all backend requests
const apiBaseUrl = import.meta.env.VITE_API_URL || 'https://vibesync-1-kq5k.onrender.com'

const api = axios.create({
  baseURL: apiBaseUrl.replace(/\/$/, ''),
  headers: { 'Content-Type': 'application/json' }
})

// Attach JWT token to every request automatically
api.interceptors.request.use(config => {
  const token = localStorage.getItem('vibesync_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ── Auth APIs ─────────────────────────────────────────────────────────────
export const authAPI = {
  register: (name, email, password) =>
    api.post('/api/auth/register', { name, email, password }),

  login: (email, password) =>
    api.post('/api/auth/login', { email, password })
}

// ── Songs APIs ────────────────────────────────────────────────────────────
export const songsAPI = {
  getAll: () =>
    api.get('/api/songs'),

  getById: (id) =>
    api.get(`/api/songs/${id}`),

  searchByQuery: (q) =>
    api.get(`/api/songs/search?q=${encodeURIComponent(q)}`),

  getByMood: (mood) =>
    api.get(`/api/songs/mood/${mood}`),

  getByGenre: (genre) =>
    api.get(`/api/songs/genre/${genre}`)
}

// ── Favorites APIs ────────────────────────────────────────────────────────
export const favoritesAPI = {
  getAll: () =>
    api.get('/api/favorites'),

  add: (songId) =>
    api.post(`/api/favorites/${songId}`),

  remove: (songId) =>
    api.delete(`/api/favorites/${songId}`)
}

// ── Playlists APIs ────────────────────────────────────────────────────────
export const playlistsAPI = {
  getAll: () =>
    api.get('/api/playlists'),

  getById: (id) =>
    api.get(`/api/playlists/${id}`),

  create: (name) =>
    api.post('/api/playlists', { name }),

  delete: (id) =>
    api.delete(`/api/playlists/${id}`),

  addSong: (playlistId, songId) =>
    api.post(`/api/playlists/${playlistId}/songs/${songId}`),

  removeSong: (playlistId, songId) =>
    api.delete(`/api/playlists/${playlistId}/songs/${songId}`)
}

// ── Weather Vibe API ──────────────────────────────────────────────────────
export const weatherAPI = {
  getVibe: (city) =>
    api.get(`/api/weather/vibe?city=${encodeURIComponent(city)}`),

  getVibeByCoordinates: (latitude, longitude) =>
    api.get('/api/weather/vibe', { params: { lat: latitude, lon: longitude } }),

  getRaw: (city) =>
    api.get(`/api/weather?city=${encodeURIComponent(city)}`)
}

// ── Listening Events API ──────────────────────────────────────────────────
export const eventsAPI = {
  record: (songId, eventType) =>
    api.post('/api/events', { songId, eventType })
}

// ── Never Skip API ────────────────────────────────────────────────────────
export const recommendationsAPI = {
  getNeverSkip: () =>
    api.get('/api/recommendations/never-skip')
}

export const aiAPI = {
  getVibePick: (mood) =>
    api.post('/api/ai/vibe-pick', { mood })
}

export default api
