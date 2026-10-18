import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import './Dashboard.css'

function Dashboard() {
  const navigate = useNavigate()

  const [user, setUser] = useState({
    fullName: localStorage.getItem('cleanloop_user_name') || 'Citizen',
    email: localStorage.getItem('cleanloop_user_email') || '',
    role: 'CITIZEN',
  })

  const [metrics, setMetrics] = useState({
    totalReports: 0,
    resolvedReports: 0,
    pendingReports: 0,
    activePickups: 0,
    completedPickups: 0,
    upcomingSchedules: [],
  })

  const [tips, setTips] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('cleanloop_access_token')
    if (!token) {
      navigate('/login')
      return
    }

    async function loadDashboardData() {
      try {
        // Fetch User Profile
        const userRes = await fetch('/api/v1/users/me', {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        })

        if (userRes.status === 401) {
          // Token expired or invalid
          localStorage.removeItem('cleanloop_access_token')
          localStorage.removeItem('cleanloop_refresh_token')
          localStorage.removeItem('cleanloop_user_name')
          localStorage.removeItem('cleanloop_user_email')
          navigate('/login')
          return
        }

        if (userRes.ok) {
          const userData = await userRes.json()
          if (userData?.data) {
            setUser({
              fullName: userData.data.fullName || 'Citizen',
              email: userData.data.email || '',
              role: userData.data.role || 'CITIZEN',
            })
            if (userData.data.fullName) {
              localStorage.setItem('cleanloop_user_name', userData.data.fullName)
            }
          }
        }

        // Fetch Citizen Metrics
        const metricsRes = await fetch('/api/v1/dashboard/citizen', {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        })

        if (metricsRes.ok) {
          const metricsData = await metricsRes.json()
          if (metricsData?.data) {
            setMetrics({
              totalReports: metricsData.data.totalReports || 0,
              resolvedReports: metricsData.data.resolvedReports || 0,
              pendingReports: metricsData.data.pendingReports || 0,
              activePickups: metricsData.data.activePickups || 0,
              completedPickups: metricsData.data.completedPickups || 0,
              upcomingSchedules: metricsData.data.upcomingSchedules || [],
            })
          }
        }

        // Fetch Educational Eco Tips (public)
        const tipsRes = await fetch('/api/v1/eco-tips')
        if (tipsRes.ok) {
          const tipsData = await tipsRes.json()
          if (Array.isArray(tipsData?.data)) {
            setTips(tipsData.data.slice(0, 3))
          }
        }
      } catch (err) {
        console.warn('Dashboard data fetch warning:', err)
      } finally {
        setLoading(false)
      }
    }

    loadDashboardData()
  }, [navigate])

  async function handleLogout() {
    const token = localStorage.getItem('cleanloop_access_token')
    if (token) {
      try {
        await fetch('/api/v1/auth/logout', {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        })
      } catch {
        // Ignore network errors on logout
      }
    }

    localStorage.removeItem('cleanloop_access_token')
    localStorage.removeItem('cleanloop_refresh_token')
    localStorage.removeItem('cleanloop_user_name')
    localStorage.removeItem('cleanloop_user_email')
    navigate('/login')
  }

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div className="dashboard-nav">
          <a href="/" className="brand">
            <span className="brand-icon">♻</span> CleanLoop
          </a>
          <div className="dashboard-nav-actions">
            <div className="user-badge">
              <span>👤</span>
              <span>{user.fullName}</span>
              <span className="role-pill">{user.role}</span>
            </div>
            <button className="logout-btn" onClick={handleLogout}>
              Log Out
            </button>
          </div>
        </div>
      </header>

      <main className="dashboard-content">
        <section className="welcome-banner">
          <span className="welcome-eyebrow">🌱 Citizen Workspace</span>
          <h1>Welcome back, {user.fullName}!</h1>
          <p>
            Connected to CleanLoop Waste Management Platform. Track your reports,
            monitor community collection schedules, and discover sustainable recycling options.
          </p>
        </section>

        <section>
          <h2 className="section-title">Your Environmental Impact</h2>
          <p className="section-desc">Live statistics from your CleanLoop account</p>

          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-header">
                <span>Waste Reports</span>
                <span className="stat-icon">📋</span>
              </div>
              <p className="stat-value">{loading ? '...' : metrics.totalReports}</p>
              <p className="stat-subtext">
                {metrics.pendingReports} pending · {metrics.resolvedReports} resolved
              </p>
            </div>

            <div className="stat-card">
              <div className="stat-header">
                <span>Special Pickups</span>
                <span className="stat-icon">🚛</span>
              </div>
              <p className="stat-value">{loading ? '...' : metrics.activePickups}</p>
              <p className="stat-subtext">
                {metrics.completedPickups} completed
              </p>
            </div>

            <div className="stat-card">
              <div className="stat-header">
                <span>Upcoming Collections</span>
                <span className="stat-icon">📅</span>
              </div>
              <p className="stat-value">
                {loading ? '...' : metrics.upcomingSchedules.length}
              </p>
              <p className="stat-subtext">Scheduled municipal routes</p>
            </div>

            <div className="stat-card">
              <div className="stat-header">
                <span>Citizen Status</span>
                <span className="stat-icon">🌿</span>
              </div>
              <p className="stat-value">Active</p>
              <p className="stat-subtext">Verified community participant</p>
            </div>
          </div>
        </section>

        <section>
          <h2 className="section-title">CleanLoop Features & Services</h2>
          <p className="section-desc">Explore tools available in the CleanLoop ecosystem</p>

          <div className="features-grid">
            <div className="feature-card">
              <div className="feature-top">
                <div className="feature-icon-wrapper">📸</div>
                <span className="badge badge-ready">API Active</span>
              </div>
              <div className="feature-body">
                <h3>Report Waste Issue</h3>
                <p>
                  Submit reports for illegal dumping, overflowing public bins, or hazardous waste.
                  Supports geo-tagging and image attachments.
                </p>
              </div>
              <div className="feature-footer">
                <span className="endpoint-hint">POST /api/v1/reports</span>
                <span className="status-chip">Form Coming Soon</span>
              </div>
            </div>

            <div className="feature-card">
              <div className="feature-top">
                <div className="feature-icon-wrapper">🗺️</div>
                <span className="badge badge-ready">API Active</span>
              </div>
              <div className="feature-body">
                <h3>Recycling Centres</h3>
                <p>
                  Locate certified recycling drop-off facilities near your coordinates and view accepted
                  material types (plastics, metals, e-waste).
                </p>
              </div>
              <div className="feature-footer">
                <span className="endpoint-hint">GET /api/v1/recycling-centers</span>
                <span className="status-chip">Map View Coming Soon</span>
              </div>
            </div>

            <div className="feature-card">
              <div className="feature-top">
                <div className="feature-icon-wrapper">🚛</div>
                <span className="badge badge-ready">API Active</span>
              </div>
              <div className="feature-body">
                <h3>Bulky Waste Pickup</h3>
                <p>
                  Request special collections for bulky household furniture, white goods,
                  and recyclables requiring scheduled vehicle transport.
                </p>
              </div>
              <div className="feature-footer">
                <span className="endpoint-hint">POST /api/v1/pickups</span>
                <span className="status-chip">Booking Coming Soon</span>
              </div>
            </div>

            <div className="feature-card">
              <div className="feature-top">
                <div className="feature-icon-wrapper">💡</div>
                <span className="badge badge-ready">API Active</span>
              </div>
              <div className="feature-body">
                <h3>Eco Tips & Best Practices</h3>
                <p>
                  Access community guides for waste reduction, segregation tips,
                  and circular economy practices for your household.
                </p>
              </div>
              <div className="feature-footer">
                <span className="endpoint-hint">GET /api/v1/eco-tips</span>
                <span className="status-chip active">Live Tips Available</span>
              </div>
            </div>
          </div>
        </section>

        {tips.length > 0 && (
          <section className="eco-tips-section">
            <h2 className="section-title">Featured Eco Tips</h2>
            <p className="section-desc">Sustainable recommendations from environmental experts</p>
            <div className="tips-list">
              {tips.map((tip) => (
                <div key={tip.id} className="tip-card">
                  <span className="tip-category">{tip.category || 'General'}</span>
                  <h4 className="tip-title">{tip.title}</h4>
                  <p className="tip-content">{tip.content}</p>
                </div>
              ))}
            </div>
          </section>
        )}
      </main>

      <footer className="dashboard-footer">
        <p>CleanLoop · Better waste habits. A better world. 🌍</p>
      </footer>
    </div>
  )
}

export default Dashboard
