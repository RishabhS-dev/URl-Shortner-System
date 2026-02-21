import { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/axios'

export default function Dashboard() {
    const navigate  = useNavigate()
    const user      = JSON.parse(localStorage.getItem('user') || '{}')

    const [urls, setUrls]       = useState([])
    const [loading, setLoading] = useState(true)
    const [form, setForm]       = useState({ originalUrl: '', expiryDays: '' })
    const [error, setError]     = useState('')
    const [success, setSuccess] = useState('')
    const [creating, setCreating] = useState(false)
    const [copiedId, setCopiedId] = useState(null)

    // Load user's URLs
    const fetchUrls = useCallback(async () => {
        try {
            const { data } = await api.get('/urls/my')
            setUrls(data.data || [])
        } catch {
            setError('Failed to load URLs')
        } finally {
            setLoading(false)
        }
    }, [])

    useEffect(() => { fetchUrls() }, [fetchUrls])

    // Shorten a new URL
    const handleShorten = async e => {
        e.preventDefault()
        setError('')
        setSuccess('')
        setCreating(true)
        try {
            const payload = { originalUrl: form.originalUrl }
            if (form.expiryDays) payload.expiryDays = parseInt(form.expiryDays)

            await api.post('/urls/shorten', payload)
            setForm({ originalUrl: '', expiryDays: '' })
            setSuccess('✅ Short URL created!')
            fetchUrls()
            setTimeout(() => setSuccess(''), 3000)
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to shorten URL')
        } finally {
            setCreating(false)
        }
    }

    // Delete a URL
    const handleDelete = async shortCode => {
        if (!confirm('Delete this short URL?')) return
        try {
            await api.delete(`/urls/${shortCode}`)
            setUrls(urls.filter(u => u.shortCode !== shortCode))
        } catch {
            setError('Failed to delete URL')
        }
    }

    // Copy short URL to clipboard
    const handleCopy = async (shortUrl, id) => {
        await navigator.clipboard.writeText(shortUrl)
        setCopiedId(id)
        setTimeout(() => setCopiedId(null), 2000)
    }

    const handleLogout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login')
    }

    // Stats
    const totalClicks = urls.reduce((sum, u) => sum + (u.clickCount || 0), 0)

    return (
        <div style={{ minHeight: '100vh' }}>

            {/* Navbar */}
            <nav className="navbar">
                <span className="navbar-brand">🔗 LinkSnap</span>
                <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <span style={{ color: '#64748b', fontSize: 14 }}>
            👋 {user.name}
          </span>
                    <button className="navbar-logout" onClick={handleLogout}>
                        Logout
                    </button>
                </div>
            </nav>

            <div style={{ maxWidth: 1100, margin: '0 auto', padding: '32px 24px' }}>

                {/* Stats */}
                <div className="stats-row">
                    <div className="stat-card">
                        <div className="stat-value">{urls.length}</div>
                        <div className="stat-label">Total Links</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value">{totalClicks}</div>
                        <div className="stat-label">Total Clicks</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value">
                            {urls.length > 0 ? Math.round(totalClicks / urls.length) : 0}
                        </div>
                        <div className="stat-label">Avg Clicks / Link</div>
                    </div>
                </div>

                {/* Create short URL form */}
                <div className="card" style={{ marginBottom: 24 }}>
                    <h2 style={{ fontSize: 18, fontWeight: 600, marginBottom: 20 }}>
                        ✂️ Shorten a URL
                    </h2>

                    {error   && <div className="alert alert-error">{error}</div>}
                    {success && <div className="alert alert-success">{success}</div>}

                    <form onSubmit={handleShorten}
                          style={{ display: 'flex', gap: 12, alignItems: 'flex-end' }}>
                        <div className="form-group" style={{ flex: 1, marginBottom: 0 }}>
                            <label>Long URL</label>
                            <input
                                type="url"
                                placeholder="https://very-long-url.com/with/lots/of/path?and=params"
                                value={form.originalUrl}
                                onChange={e => setForm({ ...form, originalUrl: e.target.value })}
                                required
                            />
                        </div>
                        <div className="form-group" style={{ width: 160, marginBottom: 0 }}>
                            <label>Expiry (days)</label>
                            <input
                                type="number"
                                placeholder="Never"
                                min="1"
                                max="365"
                                value={form.expiryDays}
                                onChange={e => setForm({ ...form, expiryDays: e.target.value })}
                            />
                        </div>
                        <button
                            className="btn-primary"
                            type="submit"
                            disabled={creating}
                            style={{ width: 'auto', padding: '12px 28px', marginBottom: 1 }}>
                            {creating ? <><span className="spinner"></span>Creating...</> : 'Shorten →'}
                        </button>
                    </form>
                </div>

                {/* URL Table */}
                <div className="card">
                    <h2 style={{ fontSize: 18, fontWeight: 600, marginBottom: 20 }}>
                        🔗 Your Links
                    </h2>

                    {loading ? (
                        <div style={{ textAlign: 'center', padding: 40, color: '#64748b' }}>
                            <span className="spinner"></span> Loading...
                        </div>
                    ) : urls.length === 0 ? (
                        <div className="empty-state">
                            <div className="empty-icon">🔗</div>
                            <p>No links yet. Shorten your first URL above!</p>
                        </div>
                    ) : (
                        <div style={{ overflowX: 'auto' }}>
                            <table className="url-table">
                                <thead>
                                <tr>
                                    <th>Short URL</th>
                                    <th>Original URL</th>
                                    <th>Clicks</th>
                                    <th>Created</th>
                                    <th>Expires</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {urls.map(url => (
                                    <tr key={url.shortCode}>
                                        <td>
                                            <a className="short-link" href={url.shortUrl}
                                               target="_blank" rel="noopener noreferrer">
                                                /{url.shortCode}
                                            </a>
                                        </td>
                                        <td>
                        <span className="original-url" title={url.originalUrl}>
                          {url.originalUrl}
                        </span>
                                        </td>
                                        <td>
                                            <span className="badge">{url.clickCount} clicks</span>
                                        </td>
                                        <td style={{ color: '#64748b', fontSize: 13 }}>
                                            {new Date(url.createdAt).toLocaleDateString()}
                                        </td>
                                        <td style={{ color: '#64748b', fontSize: 13 }}>
                                            {url.expiresAt
                                                ? new Date(url.expiresAt).toLocaleDateString()
                                                : <span style={{ color: '#22c55e' }}>Never</span>}
                                        </td>
                                        <td>
                                            <button
                                                className={`copy-btn ${copiedId === url.shortCode ? 'copied' : ''}`}
                                                onClick={() => handleCopy(url.shortUrl, url.shortCode)}>
                                                {copiedId === url.shortCode ? '✓ Copied' : 'Copy'}
                                            </button>
                                            <button
                                                className="btn-danger"
                                                onClick={() => handleDelete(url.shortCode)}>
                                                Delete
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>
        </div>
    )
}