import { useState } from 'react'
import './Auth.css'

function Register() {
  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
  })

  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')
    setLoading(true)

    try {
      const response = await fetch('https://cleanloop-ta4c.onrender.com/api/v1/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      })

      const result = await response.json()

      if (!response.ok || !result.success) {
        throw new Error(result.message || 'Registration failed. Please try again.')
      }

      setMessage('Registration successful! You can now log in.')
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
        <p className="auth-eyebrow">JOIN THE COMMUNITY</p>
        <h1>Create your account</h1>
        <p className="auth-subtitle">
          Start making a difference in your community.
        </p>

        <form onSubmit={handleSubmit}>
          <label htmlFor="name">Full name</label>
          <input
            id="name"
            name="name"
            type="text"
            value={form.name}
            onChange={handleChange}
            placeholder="Your full name"
            autoComplete="name"
            required
          />

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
            placeholder="Create a password"
            autoComplete="new-password"
            minLength={8}
            required
          />

          <button className="auth-submit" type="submit" disabled={loading}>
            {loading ? 'Creating account...' : 'Create account →'}
          </button>
        </form>

        {message && (
          <p className="auth-message" role="status">{message}</p>
        )}

        <p className="auth-switch">
          Already registered? <a href="/login">Log in</a>
        </p>
      </section>

      <p className="auth-footer">Small actions. Cleaner neighbourhoods. 🌍</p>
    </main>
  )
}

export default Register