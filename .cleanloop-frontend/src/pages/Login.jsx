import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import './Auth.css'

function Login() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    email: '',
    password: '',
  })

  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (localStorage.getItem('cleanloop_access_token')) {
      navigate('/dashboard')
    }
  }, [navigate])

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')
    setLoading(true)

    try {
      const response = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      })

      const result = await response.json().catch(() => ({}))

      if (!response.ok || !result.success) {
        const validationError =
          result.validationErrors && Object.values(result.validationErrors)[0]
        throw new Error(validationError || result.message || 'Login failed. Check your details.')
      }

      const data = result.data
      const accessToken = data?.accessToken
      const refreshToken = data?.refreshToken

      if (!accessToken) {
        throw new Error('Login succeeded, but no access token was returned.')
      }

      localStorage.setItem('cleanloop_access_token', accessToken)

      if (refreshToken) {
        localStorage.setItem('cleanloop_refresh_token', refreshToken)
      }

      if (data?.fullName) {
        localStorage.setItem('cleanloop_user_name', data.fullName)
      }

      if (data?.email) {
        localStorage.setItem('cleanloop_user_email', data.email)
      }

      setMessage('Login successful! Redirecting to dashboard...')
      navigate('/dashboard')
    } catch (error) {
      setMessage(
        error.message === 'Failed to fetch'
          ? 'Could not connect to CleanLoop. Please try again shortly.'
          : error.message
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="auth-page">
      <a className="auth-brand" href="/">♻ CleanLoop</a>

      <section className="auth-card">
        <div className="auth-symbol">🌿</div>
        <p className="auth-eyebrow">WELCOME BACK</p>
        <h1>Log in to CleanLoop</h1>
        <p className="auth-subtitle">
          Continue making a difference in your community.
        </p>

        <form onSubmit={handleSubmit}>
          <label htmlFor="email">Email address</label>
          <input
            id="email"
            name="email"
            type="email"
            value={form.email}
            onChange={handleChange}
            placeholder="you@example.com"
            autoComplete="email"
            required
          />

          <label htmlFor="password">Password</label>
          <input
            id="password"
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            placeholder="Enter your password"
            autoComplete="current-password"
            required
          />

          <button className="auth-submit" type="submit" disabled={loading}>
            {loading ? 'Logging in...' : 'Log in →'}
          </button>
        </form>

        {message && (
          <p className="auth-message" role="status">{message}</p>
        )}

        <p className="auth-switch">
          New to CleanLoop? <a href="/register">Create an account</a>
        </p>
      </section>

      <p className="auth-footer">Small actions. Cleaner neighbourhoods. 🌍</p>
    </main>
  )
}

export default Login