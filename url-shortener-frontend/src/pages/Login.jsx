import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'

export default function Login() {
    const navigate = useNavigate()
    const [form, setForm]     = useState({ email: '', password: '' })
    const [error, setError]   = useState('')
    const [loading, setLoading] = useState(false)

    const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value })

    const handleSubmit = async e => {
        e.preventDefault()
        setError('')
        setLoading(true)
        try {
            const { data } = await api.post('/auth/login', form)
            localStorage.setItem('token', data.data.token)
            localStorage.setItem('user', JSON.stringify({ name: data.data.name, email: data.data.email }))
            navigate('/dashboard')
        } catch (err) {
            setError(err.response?.data?.message || 'Invalid email or password')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="auth-page">
            <div className="auth-box">
                <div style={{ textAlign: 'center', marginBottom: 32 }}>
                    <div style={{ fontSize: 40, marginBottom: 8 }}>🔗</div>
                    <div className="navbar-brand" style={{ fontSize: 28 }}>LinkSnap</div>
                </div>

                <div className="card">
                    <h1 className="auth-title">Welcome back</h1>
                    <p className="auth-sub">Sign in to your account</p>

                    {error && <div className="alert alert-error">{error}</div>}

                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label>Email</label>
                            <input
                                name="email"
                                type="email"
                                placeholder="john@example.com"
                                value={form.email}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label>Password</label>
                            <input
                                name="password"
                                type="password"
                                placeholder="Your password"
                                value={form.password}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <button className="btn-primary" type="submit" disabled={loading}>
                            {loading ? <><span className="spinner"></span>Signing in...</> : 'Sign in'}
                        </button>
                    </form>
                </div>

                <div className="auth-footer">
                    Don't have an account? <Link to="/register">Sign up free</Link>
                </div>
            </div>
        </div>
    )
}