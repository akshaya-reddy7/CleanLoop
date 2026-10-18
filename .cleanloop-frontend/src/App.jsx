
import './App.css'
import { BrowserRouter, Routes, Route, useNavigate } from 'react-router-dom'
import Register from './pages/Register'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'

function Home() {
  const navigate = useNavigate()

  return (
    <div className="app">
      <nav className="navbar">
        <a href="#home" className="brand">
          <span className="brand-icon">♻</span> CleanLoop
        </a>
        <div className="nav-links">
          <a href="#how-it-works">How It Works</a>
          <a href="#features">Features</a>
          <a href="#impact">Our Impact</a>
        </div>
        <div className="nav-actions">
          {localStorage.getItem('cleanloop_access_token') ? (
            <button className="primary-btn" onClick={() => navigate('/dashboard')}>Dashboard ↗</button>
          ) : (
            <>
              <button className="login-btn" onClick={() => navigate('/login')}>Log In</button>
              <button className="primary-btn" onClick={() => navigate('/register')}>Get Started ↗</button>
            </>
          )}
        </div>
      </nav>

      <main>
        <section className="hero" id="home">
          <div className="hero-content">
            <span className="eyebrow"><span className="status-dot"></span>A smarter way to manage waste</span>
            <h1>Waste less.<br />Live <span className="highlight">greener.</span></h1>
            <p className="hero-description">
              Meet your new partner in sustainability. Report waste,
              discover recycling centres, and make every action count
              toward a cleaner community.
            </p>
            <div className="hero-actions">
              <button className="primary-btn large" onClick={() => navigate('/register')}>
                Start making an impact <span>↗</span>
              </button>
              <a href="#how-it-works" className="text-link">Discover how it works <span>↓</span></a>
            </div>
            <div className="hero-note">
              <div className="avatar-stack"><span>🌱</span><span>♻️</span><span>🌍</span></div>
              <p><strong>Small actions.</strong> Cleaner neighbourhoods.</p>
            </div>
          </div>

          <div className="hero-visual">
            <div className="visual-glow"></div>
            <div className="visual-card">
              <div className="visual-top">
                <span className="live-label"><span></span> CLEANER FUTURE</span>
                <span className="leaf-icon">✳</span>
              </div>
              <div className="earth-illustration">
                <div className="earth-ring ring-one"></div>
                <div className="earth-ring ring-two"></div>
                <div className="earth">🌎</div>
                <span className="float-leaf leaf-one">🍃</span>
                <span className="float-leaf leaf-two">🌿</span>
                <span className="float-leaf leaf-three">♻️</span>
              </div>
              <div className="visual-caption">
                <div><span className="caption-label">YOUR COMMUNITY</span><h3>A little greener, every day.</h3></div>
                <span className="caption-arrow">↗</span>
              </div>
            </div>
            <div className="floating-note note-top">
              <span className="note-icon">🌿</span><div><strong>Think green</strong><small>Act locally</small></div>
            </div>
            <div className="floating-note note-bottom">
              <span className="note-icon">♻️</span><div><strong>Waste to worth</strong><small>Give materials a new life</small></div>
            </div>
          </div>
        </section>

        <section className="feature-strip" id="features">
          <div><span>01</span><strong>Report waste issues</strong><p>Help your community stay clean.</p></div>
          <div><span>02</span><strong>Find recycling centres</strong><p>Know where your waste belongs.</p></div>
          <div><span>03</span><strong>Track your impact</strong><p>Follow your reports and pickups.</p></div>
        </section>

        <section className="how-section" id="how-it-works">
          <span className="section-label">CLEANER LIVING, MADE SIMPLE</span>
          <h2>Good habits. <span>Real change.</span></h2>
          <p className="section-description">A few simple steps can make a meaningful difference in the places we share.</p>
          <div className="steps-grid">
            <article className="step-card">
              <span className="step-number">01</span><div className="step-icon">📸</div>
              <h3>Report an issue</h3><p>Spot a waste problem? Submit a report and help bring it to the right people.</p>
            </article>
            <article className="step-card">
              <span className="step-number">02</span><div className="step-icon">🗺️</div>
              <h3>Find a solution</h3><p>Explore recycling centres and request pickups for waste that needs special handling.</p>
            </article>
            <article className="step-card">
              <span className="step-number">03</span><div className="step-icon">🌱</div>
              <h3>Make an impact</h3><p>Keep track of your requests and build better habits for a greener community.</p>
            </article>
          </div>
        </section>

        <section className="impact-section" id="impact">
          <div>
            <span className="section-label">THE FUTURE IS A TEAM EFFORT</span>
            <h2>Let's make clean<br />the new <span>normal.</span></h2>
          </div>
          <div className="impact-right">
            <p>Cleaner streets start with people who care. Join CleanLoop and take the first step toward a more responsible, sustainable community.</p>
            <button className="light-btn" onClick={() => navigate('/register')}>Join CleanLoop <span>↗</span></button>
          </div>
        </section>

        <footer className="footer">
          <a href="#home" className="brand"><span className="brand-icon">♻</span> CleanLoop</a>
          <p>Better waste habits. A better world.</p>
          <span>Built for a cleaner tomorrow 🌍</span>
        </footer>
      </main>
    </div>
  )
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" element={<Dashboard initialTab="overview" />} />
        <Route path="/reports" element={<Dashboard initialTab="reports" />} />
        <Route path="/pickups" element={<Dashboard initialTab="pickups" />} />
        <Route path="/schedules" element={<Dashboard initialTab="schedules" />} />
        <Route path="/recycling-centers" element={<Dashboard initialTab="recycling" />} />
        <Route path="/ai-classifier" element={<Dashboard initialTab="ai" />} />
        <Route path="/eco-tips" element={<Dashboard initialTab="ecotips" />} />
        <Route path="/notifications" element={<Dashboard initialTab="notifications" />} />
        <Route path="/profile" element={<Dashboard initialTab="profile" />} />
        <Route path="/authority" element={<Dashboard initialTab="authority" />} />
        <Route path="/admin" element={<Dashboard initialTab="admin" />} />
        <Route path="*" element={<Home />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
