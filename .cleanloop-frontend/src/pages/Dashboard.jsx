import { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import './Dashboard.css'

export default function Dashboard({ initialTab = 'overview' }) {
  const navigate = useNavigate()

  // Authentication & Current User
  const [user, setUser] = useState({
    fullName: localStorage.getItem('cleanloop_user_name') || 'Citizen',
    email: localStorage.getItem('cleanloop_user_email') || '',
    role: localStorage.getItem('cleanloop_user_role') || 'CITIZEN',
    phoneNumber: '',
    serviceArea: '',
  })

  // Navigation tab
  const [activeTab, setActiveTab] = useState(initialTab)

  // Status and Alerts
  const [feedback, setFeedback] = useState(null)
  const [loading, setLoading] = useState(true)

  // Data states
  const [metrics, setMetrics] = useState({
    totalReports: 0,
    resolvedReports: 0,
    pendingReports: 0,
    activePickups: 0,
    completedPickups: 0,
    upcomingSchedules: [],
  })
  const [authorityMetrics, setAuthorityMetrics] = useState(null)
  const [adminMetrics, setAdminMetrics] = useState(null)

  const [reports, setReports] = useState([])
  const [allReports, setAllReports] = useState([])
  const [pickups, setPickups] = useState([])
  const [allPickups, setAllPickups] = useState([])
  const [schedules, setSchedules] = useState([])
  const [scheduleLocality, setScheduleLocality] = useState('')
  const [recyclingCenters, setRecyclingCenters] = useState([])
  const [materialFilter, setMaterialFilter] = useState('')
  const [recyclingSearch, setRecyclingSearch] = useState('')
  const [tips, setTips] = useState([])
  const [notifications, setNotifications] = useState([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [categories, setCategories] = useState([])
  const [usersList, setUsersList] = useState([])
  const [auditLogs, setAuditLogs] = useState([])
  const [aiStatus, setAiStatus] = useState('CHECKING')

  // UI Action toggles
  const [showReportForm, setShowReportForm] = useState(false)
  const [showPickupForm, setShowPickupForm] = useState(false)
  const [showTipForm, setShowTipForm] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  // Report Form State
  const [newReport, setNewReport] = useState({
    title: '',
    description: '',
    issueType: 'ILLEGAL_DUMPING',
    userCategoryId: '',
    latitude: '',
    longitude: '',
    address: '',
    locality: '',
  })
  const [reportImage, setReportImage] = useState(null)
  const [reportImagePreview, setReportImagePreview] = useState(null)

  // Pickup Form State
  const [newPickup, setNewPickup] = useState({
    categoryId: '',
    description: '',
    address: '',
    locality: '',
    latitude: '',
    longitude: '',
    preferredDate: '',
    timeWindow: '09:00 - 12:00',
    isSpecialHandling: false,
    specialHandlingNotes: '',
  })
  const [pickupImage, setPickupImage] = useState(null)
  const [pickupImagePreview, setPickupImagePreview] = useState(null)

  // Profile Form State
  const [profileForm, setProfileForm] = useState({
    fullName: '',
    phoneNumber: '',
    serviceArea: '',
  })
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  })

  // Eco Tip Form State
  const [newTip, setNewTip] = useState({
    title: '',
    content: '',
    category: 'Recycling',
    imageUrl: '',
  })

  // Schedule Form State (Authority/Admin)
  const [showScheduleForm, setShowScheduleForm] = useState(false)
  const [newSchedule, setNewSchedule] = useState({
    locality: '',
    serviceArea: '',
    collectionDayOfWeek: 'Monday',
    startTime: '08:00',
    endTime: '12:00',
    categoryId: '',
    notes: '',
  })

  // Recycling Centre Form State (Authority/Admin)
  const [showCenterForm, setShowCenterForm] = useState(false)
  const [newCenter, setNewCenter] = useState({
    name: '',
    description: '',
    address: '',
    latitude: '',
    longitude: '',
    contactPhone: '',
    contactEmail: '',
    operatingHours: 'Mon-Sat 08:00 - 18:00',
    websiteUrl: '',
    acceptedMaterials: 'Plastic, Paper, Glass, Metal',
  })

  const token = localStorage.getItem('cleanloop_access_token')

  const showFeedback = (type, text) => {
    setFeedback({ type, text })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  // Helper for authenticated requests
  const authFetch = useCallback(
    async (url, options = {}) => {
      const headers = {
        Authorization: `Bearer ${token}`,
        ...options.headers,
      }
      const res = await fetch(url, { ...options, headers })
      if (res.status === 401) {
        localStorage.removeItem('cleanloop_access_token')
        localStorage.removeItem('cleanloop_refresh_token')
        localStorage.removeItem('cleanloop_user_name')
        localStorage.removeItem('cleanloop_user_email')
        localStorage.removeItem('cleanloop_user_role')
        navigate('/login')
        throw new Error('Session expired. Please log in again.')
      }
      return res
    },
    [token, navigate]
  )

  // Sync tab with route
  const switchTab = (tab) => {
    setActiveTab(tab)
    setFeedback(null)
    const routeMap = {
      overview: '/dashboard',
      reports: '/reports',
      pickups: '/pickups',
      schedules: '/schedules',
      recycling: '/recycling-centers',
      ai: '/ai-classifier',
      ecotips: '/eco-tips',
      notifications: '/notifications',
      profile: '/profile',
      authority: '/authority',
      admin: '/admin',
    }
    if (routeMap[tab]) {
      navigate(routeMap[tab])
    }
  }

  // Load Initial Data
  useEffect(() => {
    if (!token) {
      navigate('/login')
      return
    }

    async function initDashboard() {
      setLoading(true)
      try {
        // 1. User Profile
        const userRes = await authFetch('/api/v1/users/me')
        if (userRes.ok) {
          const uData = await userRes.json()
          if (uData?.data) {
            setUser({
              fullName: uData.data.fullName || 'Citizen',
              email: uData.data.email || '',
              role: uData.data.role || 'CITIZEN',
              phoneNumber: uData.data.phoneNumber || '',
              serviceArea: uData.data.serviceArea || '',
            })
            setProfileForm({
              fullName: uData.data.fullName || '',
              phoneNumber: uData.data.phoneNumber || '',
              serviceArea: uData.data.serviceArea || '',
            })
            if (uData.data.fullName) localStorage.setItem('cleanloop_user_name', uData.data.fullName)
            if (uData.data.role) localStorage.setItem('cleanloop_user_role', uData.data.role)
          }
        }

        // 2. Categories
        const catRes = await fetch('/api/v1/categories')
        if (catRes.ok) {
          const catData = await catRes.json()
          if (Array.isArray(catData?.data)) {
            setCategories(catData.data)
          }
        }

        // 3. Citizen Metrics
        const metricsRes = await authFetch('/api/v1/dashboard/citizen')
        if (metricsRes.ok) {
          const mData = await metricsRes.json()
          if (mData?.data) {
            setMetrics({
              totalReports: mData.data.totalReports || 0,
              resolvedReports: mData.data.resolvedReports || 0,
              pendingReports: mData.data.pendingReports || 0,
              activePickups: mData.data.activePickups || 0,
              completedPickups: mData.data.completedPickups || 0,
              upcomingSchedules: mData.data.upcomingSchedules || [],
            })
          }
        }

        // 4. Notifications Count & List
        const countRes = await authFetch('/api/v1/notifications/unread-count')
        if (countRes.ok) {
          const cData = await countRes.json()
          setUnreadCount(cData?.data || 0)
        }

        // 5. System Health & AI Status
        const healthRes = await fetch('/api/v1/health')
        if (healthRes.ok) {
          const hData = await healthRes.json()
          if (hData?.data?.components?.aiServiceBoundary) {
            setAiStatus(hData.data.components.aiServiceBoundary)
          }
        }

        // 6. Eco Tips
        const tipsRes = await fetch('/api/v1/eco-tips')
        if (tipsRes.ok) {
          const tData = await tipsRes.json()
          if (Array.isArray(tData?.data)) {
            setTips(tData.data)
          }
        }
      } catch (err) {
        console.warn('Initial dashboard fetch warning:', err)
      } finally {
        setLoading(false)
      }
    }

    initDashboard()
  }, [token, authFetch, navigate])

  // Tab-specific Data Loaders
  useEffect(() => {
    if (!token) return

    if (activeTab === 'reports') {
      authFetch('/api/v1/reports/my?size=20')
        .then((r) => r.json())
        .then((d) => setReports(d?.data?.content || []))
        .catch((e) => console.warn('Error loading reports:', e))
    } else if (activeTab === 'pickups') {
      authFetch('/api/v1/pickups/my?size=20')
        .then((r) => r.json())
        .then((d) => setPickups(d?.data?.content || []))
        .catch((e) => console.warn('Error loading pickups:', e))
    } else if (activeTab === 'schedules') {
      const url = scheduleLocality.trim()
        ? `/api/v1/schedules/locality?locality=${encodeURIComponent(scheduleLocality.trim())}`
        : '/api/v1/schedules'
      fetch(url)
        .then((r) => r.json())
        .then((d) => setSchedules(d?.data || []))
        .catch((e) => console.warn('Error loading schedules:', e))
    } else if (activeTab === 'recycling') {
      const url = materialFilter.trim()
        ? `/api/v1/recycling-centers?material=${encodeURIComponent(materialFilter.trim())}`
        : '/api/v1/recycling-centers'
      fetch(url)
        .then((r) => r.json())
        .then((d) => setRecyclingCenters(d?.data || []))
        .catch((e) => console.warn('Error loading recycling centres:', e))
    } else if (activeTab === 'notifications') {
      authFetch('/api/v1/notifications?size=30')
        .then((r) => r.json())
        .then((d) => setNotifications(d?.data?.content || []))
        .catch((e) => console.warn('Error loading notifications:', e))
    } else if (activeTab === 'authority') {
      authFetch('/api/v1/dashboard/authority')
        .then((r) => r.json())
        .then((d) => setAuthorityMetrics(d?.data || null))
        .catch((e) => console.warn('Authority metrics error:', e))

      authFetch('/api/v1/reports?size=30')
        .then((r) => r.json())
        .then((d) => setAllReports(d?.data?.content || []))
        .catch((e) => console.warn('Authority reports error:', e))

      authFetch('/api/v1/pickups?size=30')
        .then((r) => r.json())
        .then((d) => setAllPickups(d?.data?.content || []))
        .catch((e) => console.warn('Authority pickups error:', e))
    } else if (activeTab === 'admin') {
      authFetch('/api/v1/dashboard/admin')
        .then((r) => r.json())
        .then((d) => setAdminMetrics(d?.data || null))
        .catch((e) => console.warn('Admin metrics error:', e))

      authFetch('/api/v1/admin/users?size=50')
        .then((r) => r.json())
        .then((d) => setUsersList(d?.data?.content || []))
        .catch((e) => console.warn('Admin users error:', e))

      authFetch('/api/v1/admin/audit-logs?size=30')
        .then((r) => r.json())
        .then((d) => setAuditLogs(d?.data?.content || []))
        .catch((e) => console.warn('Admin audit logs error:', e))
    }
  }, [activeTab, scheduleLocality, materialFilter, token, authFetch])

  // Geolocation detector
  const detectLocation = (target) => {
    if (!navigator.geolocation) {
      showFeedback('error', 'Geolocation is not supported by your browser.')
      return
    }
    showFeedback('info', 'Detecting your coordinates...')
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const lat = pos.coords.latitude.toFixed(6)
        const lng = pos.coords.longitude.toFixed(6)
        if (target === 'report') {
          setNewReport((prev) => ({ ...prev, latitude: lat, longitude: lng }))
        } else if (target === 'pickup') {
          setNewPickup((prev) => ({ ...prev, latitude: lat, longitude: lng }))
        } else if (target === 'center') {
          setNewCenter((prev) => ({ ...prev, latitude: lat, longitude: lng }))
        }
        showFeedback('success', `Coordinates detected: ${lat}, ${lng}`)
      },
      (err) => {
        showFeedback('error', `Could not detect location: ${err.message}`)
      },
      { timeout: 10000, enableHighAccuracy: true }
    )
  }

  // Handle Logout
  const handleLogout = async () => {
    if (token) {
      try {
        await authFetch('/api/v1/auth/logout', { method: 'POST' })
      } catch {
        // Ignore network failure on logout
      }
    }
    localStorage.removeItem('cleanloop_access_token')
    localStorage.removeItem('cleanloop_refresh_token')
    localStorage.removeItem('cleanloop_user_name')
    localStorage.removeItem('cleanloop_user_email')
    localStorage.removeItem('cleanloop_user_role')
    navigate('/login')
  }

  // Report Creation Handler
  const handleCreateReport = async (e) => {
    e.preventDefault()
    if (!newReport.title.trim() || !newReport.description.trim()) {
      showFeedback('error', 'Please provide both title and description.')
      return
    }
    if (!newReport.latitude || !newReport.longitude) {
      showFeedback('error', 'Please provide or detect latitude and longitude coordinates.')
      return
    }

    setSubmitting(true)
    try {
      const formData = new FormData()
      const reportPayload = {
        title: newReport.title.trim(),
        description: newReport.description.trim(),
        issueType: newReport.issueType,
        userCategoryId: newReport.userCategoryId ? parseInt(newReport.userCategoryId, 10) : null,
        latitude: parseFloat(newReport.latitude),
        longitude: parseFloat(newReport.longitude),
        address: newReport.address.trim() || null,
        locality: newReport.locality.trim() || null,
      }

      formData.append(
        'report',
        new Blob([JSON.stringify(reportPayload)], { type: 'application/json' })
      )
      if (reportImage) {
        formData.append('images', reportImage)
      }

      const res = await fetch('/api/v1/reports', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formData,
      })

      const data = await res.json()
      if (!res.ok) {
        throw new Error(data?.message || 'Failed to submit waste report.')
      }

      showFeedback('success', `Waste report #${data?.data?.id || ''} submitted successfully!`)
      setShowReportForm(false)
      setNewReport({
        title: '',
        description: '',
        issueType: 'ILLEGAL_DUMPING',
        userCategoryId: '',
        latitude: '',
        longitude: '',
        address: '',
        locality: '',
      })
      setReportImage(null)
      setReportImagePreview(null)

      // Refresh list
      const updated = await authFetch('/api/v1/reports/my?size=20')
      if (updated.ok) {
        const uData = await updated.json()
        setReports(uData?.data?.content || [])
      }
      setMetrics((prev) => ({ ...prev, totalReports: prev.totalReports + 1, pendingReports: prev.pendingReports + 1 }))
    } catch (err) {
      showFeedback('error', err.message)
    } finally {
      setSubmitting(false)
    }
  }

  // Pickup Request Handler
  const handleCreatePickup = async (e) => {
    e.preventDefault()
    if (!newPickup.categoryId) {
      showFeedback('error', 'Please select a waste category.')
      return
    }
    if (!newPickup.description.trim() || !newPickup.address.trim()) {
      showFeedback('error', 'Please provide item description and address.')
      return
    }
    if (!newPickup.preferredDate) {
      showFeedback('error', 'Please select a preferred pickup date.')
      return
    }

    setSubmitting(true)
    try {
      const formData = new FormData()
      const pickupPayload = {
        categoryId: parseInt(newPickup.categoryId, 10),
        description: newPickup.description.trim(),
        address: newPickup.address.trim(),
        locality: newPickup.locality.trim() || null,
        latitude: newPickup.latitude ? parseFloat(newPickup.latitude) : null,
        longitude: newPickup.longitude ? parseFloat(newPickup.longitude) : null,
        preferredDate: newPickup.preferredDate,
        timeWindow: newPickup.timeWindow,
        isSpecialHandling: Boolean(newPickup.isSpecialHandling),
        specialHandlingNotes: newPickup.specialHandlingNotes.trim() || null,
      }

      formData.append(
        'pickup',
        new Blob([JSON.stringify(pickupPayload)], { type: 'application/json' })
      )
      if (pickupImage) {
        formData.append('image', pickupImage)
      }

      const res = await fetch('/api/v1/pickups', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formData,
      })

      const data = await res.json()
      if (!res.ok) {
        throw new Error(data?.message || 'Failed to submit pickup request.')
      }

      showFeedback('success', `Special pickup request #${data?.data?.id || ''} scheduled successfully!`)
      setShowPickupForm(false)
      setNewPickup({
        categoryId: '',
        description: '',
        address: '',
        locality: '',
        latitude: '',
        longitude: '',
        preferredDate: '',
        timeWindow: '09:00 - 12:00',
        isSpecialHandling: false,
        specialHandlingNotes: '',
      })
      setPickupImage(null)
      setPickupImagePreview(null)

      // Refresh list
      const updated = await authFetch('/api/v1/pickups/my?size=20')
      if (updated.ok) {
        const uData = await updated.json()
        setPickups(uData?.data?.content || [])
      }
      setMetrics((prev) => ({ ...prev, activePickups: prev.activePickups + 1 }))
    } catch (err) {
      showFeedback('error', err.message)
    } finally {
      setSubmitting(false)
    }
  }

  // Cancel Pickup Handler
  const handleCancelPickup = async (pickupId) => {
    if (!window.confirm(`Are you sure you want to cancel pickup request #${pickupId}?`)) {
      return
    }
    try {
      const res = await authFetch(`/api/v1/pickups/${pickupId}/cancel?reason=Cancelled+by+citizen`, {
        method: 'POST',
      })
      const data = await res.json()
      if (!res.ok) {
        throw new Error(data?.message || 'Could not cancel pickup.')
      }
      showFeedback('success', `Pickup #${pickupId} cancelled.`)
      setPickups((prev) =>
        prev.map((p) => (p.id === pickupId ? { ...p, status: 'CANCELLED' } : p))
      )
    } catch (err) {
      showFeedback('error', err.message)
    }
  }

  // Trigger On-Demand AI Classification on Image
  const handleTriggerAiClassification = async (reportId, imageId) => {
    try {
      showFeedback('info', `Triggering AI classification on image #${imageId}...`)
      const res = await authFetch(`/api/v1/reports/${reportId}/images/${imageId}/classify`, {
        method: 'POST',
      })
      const data = await res.json()
      if (!res.ok) {
        throw new Error(data?.message || 'AI classification request failed.')
      }
      showFeedback('success', `AI classification complete for image #${imageId}!`)
      // Update report in state
      if (data?.data) {
        setReports((prev) => prev.map((r) => (r.id === reportId ? data.data : r)))
      }
    } catch (err) {
      showFeedback('error', err.message)
    }
  }

  // Authority Report Status Update
  const handleUpdateReportStatus = async (reportId, newStatus) => {
    try {
      const res = await authFetch(`/api/v1/reports/${reportId}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus, notes: `Status updated to ${newStatus}` }),
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data?.message || 'Could not update status.')
      showFeedback('success', `Report #${reportId} status updated to ${newStatus}`)
      setAllReports((prev) =>
        prev.map((r) => (r.id === reportId ? { ...r, status: newStatus } : r))
      )
    } catch (err) {
      showFeedback('error', err.message)
    }
  }

  // Authority Pickup Status Update
  const handleUpdatePickupStatus = async (pickupId, newStatus) => {
    try {
      const res = await authFetch(`/api/v1/pickups/${pickupId}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus, notes: `Status set to ${newStatus}` }),
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data?.message || 'Could not update status.')
      showFeedback('success', `Pickup #${pickupId} status updated to ${newStatus}`)
      setAllPickups((prev) =>
        prev.map((p) => (p.id === pickupId ? { ...p, status: newStatus } : p))
      )
    } catch (err) {
      showFeedback('error', err.message)
    }
  }

  // Admin User Role Change
  const handleUpdateUserRole = async (userId, newRole) => {
    try {
      const res = await authFetch(`/api/v1/admin/users/${userId}/role?role=${newRole}`, {
        method: 'PATCH',
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data?.message || 'Failed to update user role.')
      showFeedback('success', `User #${userId} role updated to ${newRole}`)
      setUsersList((prev) =>
        prev.map((u) => (u.id === userId ? { ...u, role: newRole } : u))
      )
    } catch (err) {
      showFeedback('error', err.message)
    }
  }

  // Admin User Status Toggle
  const handleToggleUserStatus = async (userId, currentActive) => {
    const nextStatus = !currentActive
    try {
      const res = await authFetch(`/api/v1/admin/users/${userId}/status?active=${nextStatus}`, {
        method: 'PATCH',
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data?.message || 'Failed to update user status.')
      showFeedback('success', `User #${userId} marked ${nextStatus ? 'ACTIVE' : 'INACTIVE'}`)
      setUsersList((prev) =>
        prev.map((u) => (u.id === userId ? { ...u, active: nextStatus } : u))
      )
    } catch (err) {
      showFeedback('error', err.message)
    }
  }

  // Eco Tip Creation Handler
  const handleCreateEcoTip = async (e) => {
    e.preventDefault()
    if (!newTip.title.trim() || !newTip.content.trim()) {
      showFeedback('error', 'Please provide a title and content.')
      return
    }

    try {
      const res = await authFetch('/api/v1/eco-tips', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title: newTip.title.trim(),
          content: newTip.content.trim(),
          category: newTip.category,
          imageUrl: newTip.imageUrl.trim() || null,
        }),
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data?.message || 'Failed to publish eco tip.')
      showFeedback('success', 'Eco tip published successfully!')
      setShowTipForm(false)
      setNewTip({ title: '', content: '', category: 'Recycling', imageUrl: '' })
      if (data?.data) {
        setTips((prev) => [data.data, ...prev])
      }
    } catch (err) {
      showFeedback('error', err.message)
    }
  }

  // Schedule Creation Handler (Authority/Admin)
  const handleCreateSchedule = async (e) => {
    e.preventDefault()
    if (!newSchedule.locality.trim() || !newSchedule.serviceArea.trim() || !newSchedule.categoryId) {
      showFeedback('error', 'Please fill in locality, service area, and waste category.')
      return
    }

    try {
      const res = await authFetch('/api/v1/schedules', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          locality: newSchedule.locality.trim(),
          serviceArea: newSchedule.serviceArea.trim(),
          collectionDayOfWeek: newSchedule.collectionDayOfWeek,
          startTime: newSchedule.startTime + ':00',
          endTime: newSchedule.endTime + ':00',
          categoryId: parseInt(newSchedule.categoryId, 10),
          notes: newSchedule.notes.trim() || null,
        }),
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data?.message || 'Failed to create collection schedule.')
      showFeedback('success', 'Collection schedule created successfully!')
      setShowScheduleForm(false)
      setNewSchedule({
        locality: '',
        serviceArea: '',
        collectionDayOfWeek: 'Monday',
        startTime: '08:00',
        endTime: '12:00',
        categoryId: '',
        notes: '',
      })
      if (data?.data) {
        setSchedules((prev) => [data.data, ...prev])
      }
    } catch (err) {
      showFeedback('error', err.message)
    }
  }

  // Recycling Centre Registration Handler (Authority/Admin)
  const handleCreateCenter = async (e) => {
    e.preventDefault()
    if (!newCenter.name.trim() || !newCenter.address.trim() || !newCenter.latitude || !newCenter.longitude || !newCenter.acceptedMaterials.trim()) {
      showFeedback('error', 'Please fill in centre name, address, coordinates, and accepted materials.')
      return
    }

    try {
      const res = await authFetch('/api/v1/recycling-centers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: newCenter.name.trim(),
          description: newCenter.description.trim() || null,
          address: newCenter.address.trim(),
          latitude: parseFloat(newCenter.latitude),
          longitude: parseFloat(newCenter.longitude),
          contactPhone: newCenter.contactPhone.trim() || null,
          contactEmail: newCenter.contactEmail.trim() || null,
          operatingHours: newCenter.operatingHours.trim() || null,
          websiteUrl: newCenter.websiteUrl.trim() || null,
          acceptedMaterials: newCenter.acceptedMaterials.trim(),
        }),
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data?.message || 'Failed to register recycling centre.')
      showFeedback('success', 'Recycling centre registered successfully!')
      setShowCenterForm(false)
      setNewCenter({
        name: '',
        description: '',
        address: '',
        latitude: '',
        longitude: '',
        contactPhone: '',
        contactEmail: '',
        operatingHours: 'Mon-Sat 08:00 - 18:00',
        websiteUrl: '',
        acceptedMaterials: 'Plastic, Paper, Glass, Metal',
      })
      if (data?.data) {
        setRecyclingCenters((prev) => [data.data, ...prev])
      }
    } catch (err) {
      showFeedback('error', err.message)
    }
  }

  // Notifications Handlers
  const handleMarkNotificationRead = async (id) => {
    try {
      await authFetch(`/api/v1/notifications/${id}/read`, { method: 'PATCH' })
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      )
      setUnreadCount((c) => Math.max(0, c - 1))
    } catch (err) {
      console.warn('Mark read warning:', err)
    }
  }

  const handleMarkAllNotificationsRead = async () => {
    try {
      await authFetch('/api/v1/notifications/read-all', { method: 'POST' })
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })))
      setUnreadCount(0)
      showFeedback('success', 'All notifications marked as read.')
    } catch (err) {
      showFeedback('error', err.message)
    }
  }

  // Profile and Password Update Handlers
  const handleUpdateProfile = async (e) => {
    e.preventDefault()
    if (!profileForm.fullName.trim()) {
      showFeedback('error', 'Full name cannot be empty.')
      return
    }

    try {
      const res = await authFetch('/api/v1/users/me', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          fullName: profileForm.fullName.trim(),
          phoneNumber: profileForm.phoneNumber.trim() || null,
          serviceArea: profileForm.serviceArea.trim() || null,
        }),
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data?.message || 'Failed to update profile.')
      showFeedback('success', 'Profile updated successfully!')
      setUser((prev) => ({
        ...prev,
        fullName: profileForm.fullName.trim(),
        phoneNumber: profileForm.phoneNumber.trim(),
        serviceArea: profileForm.serviceArea.trim(),
      }))
      localStorage.setItem('cleanloop_user_name', profileForm.fullName.trim())
    } catch (err) {
      showFeedback('error', err.message)
    }
  }

  const handleChangePassword = async (e) => {
    e.preventDefault()
    if (!passwordForm.currentPassword || !passwordForm.newPassword) {
      showFeedback('error', 'Please fill in both current and new password.')
      return
    }
    if (passwordForm.newPassword.length < 6) {
      showFeedback('error', 'New password must be at least 6 characters.')
      return
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      showFeedback('error', 'New passwords do not match.')
      return
    }

    try {
      const res = await authFetch('/api/v1/users/me/password', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          currentPassword: passwordForm.currentPassword,
          newPassword: passwordForm.newPassword,
        }),
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data?.message || 'Failed to change password.')
      showFeedback('success', 'Password changed successfully!')
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
    } catch (err) {
      showFeedback('error', err.message)
    }
  }

  // Filtered Recycling Centres
  const displayedCentres = recyclingCenters.filter((c) => {
    if (!recyclingSearch.trim()) return true
    const term = recyclingSearch.toLowerCase()
    return (
      c.name?.toLowerCase().includes(term) ||
      c.address?.toLowerCase().includes(term) ||
      c.acceptedMaterials?.toLowerCase().includes(term)
    )
  })

  return (
    <div className="dashboard-page">
      {/* Top Header */}
      <header className="dashboard-header">
        <div className="dashboard-nav-top">
          <a href="/" className="brand-link">
            <span className="brand-icon">♻</span> CleanLoop
          </a>
          <div className="dashboard-nav-actions">
            <button
              className="notif-badge-btn"
              onClick={() => switchTab('notifications')}
              title="Notifications"
            >
              <span>🔔</span>
              {unreadCount > 0 && <span className="notif-count">{unreadCount}</span>}
            </button>
            <div className="user-badge" onClick={() => switchTab('profile')} style={{ cursor: 'pointer' }}>
              <span>👤</span>
              <span>{user.fullName}</span>
              <span
                className={`role-pill ${
                  user.role === 'ADMIN' ? 'admin' : user.role === 'AUTHORITY' ? 'authority' : ''
                }`}
              >
                {user.role}
              </span>
            </div>
            <button className="logout-btn" onClick={handleLogout}>
              Log Out
            </button>
          </div>
        </div>

        {/* Unified Tab Bar */}
        <div className="dashboard-tabs-bar">
          <div className="tabs-container">
            <button
              className={`tab-btn ${activeTab === 'overview' ? 'active' : ''}`}
              onClick={() => switchTab('overview')}
            >
              📊 Overview
            </button>
            <button
              className={`tab-btn ${activeTab === 'reports' ? 'active' : ''}`}
              onClick={() => switchTab('reports')}
            >
              📸 Waste Reports
              {metrics.totalReports > 0 && (
                <span className="tab-pill">{metrics.totalReports}</span>
              )}
            </button>
            <button
              className={`tab-btn ${activeTab === 'pickups' ? 'active' : ''}`}
              onClick={() => switchTab('pickups')}
            >
              🚛 Pickups
              {metrics.activePickups > 0 && (
                <span className="tab-pill">{metrics.activePickups}</span>
              )}
            </button>
            <button
              className={`tab-btn ${activeTab === 'schedules' ? 'active' : ''}`}
              onClick={() => switchTab('schedules')}
            >
              📅 Schedules
            </button>
            <button
              className={`tab-btn ${activeTab === 'recycling' ? 'active' : ''}`}
              onClick={() => switchTab('recycling')}
            >
              🗺️ Recycling Centres
            </button>
            <button
              className={`tab-btn ${activeTab === 'ai' ? 'active' : ''}`}
              onClick={() => switchTab('ai')}
            >
              🤖 AI Classifier
            </button>
            <button
              className={`tab-btn ${activeTab === 'ecotips' ? 'active' : ''}`}
              onClick={() => switchTab('ecotips')}
            >
              💡 Eco Tips
            </button>
            <button
              className={`tab-btn ${activeTab === 'profile' ? 'active' : ''}`}
              onClick={() => switchTab('profile')}
            >
              ⚙️ Profile
            </button>
            {(user.role === 'AUTHORITY' || user.role === 'ADMIN') && (
              <button
                className={`tab-btn ${activeTab === 'authority' ? 'active' : ''}`}
                onClick={() => switchTab('authority')}
              >
                🛡️ Authority Ops
              </button>
            )}
            {user.role === 'ADMIN' && (
              <button
                className={`tab-btn ${activeTab === 'admin' ? 'active' : ''}`}
                onClick={() => switchTab('admin')}
              >
                ⚡ Admin Panel
              </button>
            )}
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="dashboard-content">
        {/* Feedback Alert */}
        {feedback && (
          <div className={`feedback-alert ${feedback.type}`}>
            <span>{feedback.text}</span>
            <button onClick={() => setFeedback(null)}>✕</button>
          </div>
        )}

        {/* -------------------- 1. OVERVIEW TAB -------------------- */}
        {activeTab === 'overview' && (
          <>
            <section className="welcome-banner">
              <span className="welcome-eyebrow">🌱 Community Waste Management Workspace</span>
              <h1>Welcome back, {user.fullName}!</h1>
              <p>
                Connected to the CleanLoop platform. Report neighbourhood waste issues, book
                municipal bulky pickups, track collection schedules, and locate recycling drop-off
                points.
              </p>
            </section>

            {/* Quick Action Buttons */}
            <section>
              <div className="action-bar">
                <h2>Quick Actions</h2>
              </div>
              <div className="quick-actions-grid">
                <div
                  className="quick-action-card"
                  onClick={() => {
                    switchTab('reports')
                    setShowReportForm(true)
                  }}
                >
                  <span className="quick-action-icon">📸</span>
                  <h3>Report Waste</h3>
                  <p>Submit geotagged photos of overflowing bins or illegal dumping.</p>
                </div>
                <div
                  className="quick-action-card"
                  onClick={() => {
                    switchTab('pickups')
                    setShowPickupForm(true)
                  }}
                >
                  <span className="quick-action-icon">🚛</span>
                  <h3>Book Pickup</h3>
                  <p>Schedule doorstep collection for bulky furniture and electronics.</p>
                </div>
                <div className="quick-action-card" onClick={() => switchTab('recycling')}>
                  <span className="quick-action-icon">🗺️</span>
                  <h3>Find Centres</h3>
                  <p>Locate nearby certified facilities accepting specific materials.</p>
                </div>
                <div className="quick-action-card" onClick={() => switchTab('schedules')}>
                  <span className="quick-action-icon">📅</span>
                  <h3>Collection Route</h3>
                  <p>Check the scheduled municipal collection days for your neighborhood.</p>
                </div>
              </div>
            </section>

            {/* Live KPI Statistics */}
            <section>
              <div className="action-bar">
                <h2>Your Environmental Impact</h2>
              </div>
              <div className="stats-grid">
                <div className="stat-card">
                  <div className="stat-header">
                    <span>Reports Submitted</span>
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
                    {metrics.completedPickups} completed routes
                  </p>
                </div>

                <div className="stat-card">
                  <div className="stat-header">
                    <span>Local Schedules</span>
                    <span className="stat-icon">📅</span>
                  </div>
                  <p className="stat-value">
                    {loading ? '...' : metrics.upcomingSchedules.length}
                  </p>
                  <p className="stat-subtext">Scheduled municipal collections</p>
                </div>

                <div className="stat-card">
                  <div className="stat-header">
                    <span>AI Model Status</span>
                    <span className="stat-icon">🤖</span>
                  </div>
                  <p className="stat-value" style={{ fontSize: '20px' }}>
                    {aiStatus === 'CONFIGURED' ? 'MobileNetV2' : aiStatus}
                  </p>
                  <p className="stat-subtext">TrashNet Automated Classifier</p>
                </div>
              </div>
            </section>

            {/* Featured Educational Eco Tips */}
            {tips.length > 0 && (
              <section className="eco-tips-section">
                <div className="action-bar">
                  <h2>Community Eco Tips</h2>
                  <button className="secondary-action-btn" onClick={() => switchTab('ecotips')}>
                    View All Tips ({tips.length}) →
                  </button>
                </div>
                <div className="tips-list">
                  {tips.slice(0, 3).map((tip) => (
                    <div key={tip.id} className="tip-card">
                      <span className="tip-category">{tip.category || 'General'}</span>
                      <h4 className="tip-title">{tip.title}</h4>
                      <p className="tip-content">{tip.content}</p>
                    </div>
                  ))}
                </div>
              </section>
            )}
          </>
        )}

        {/* -------------------- 2. WASTE REPORTS TAB -------------------- */}
        {activeTab === 'reports' && (
          <section>
            <div className="action-bar">
              <div>
                <h2>Waste Issue Reports</h2>
                <p className="section-desc">
                  Report illegal dumping, overflowing bins, and hazardous waste issues in your area.
                </p>
              </div>
              <button
                className="primary-action-btn"
                onClick={() => setShowReportForm(!showReportForm)}
              >
                {showReportForm ? '✕ Close Form' : '+ Submit New Report'}
              </button>
            </div>

            {/* Interactive Report Submission Form */}
            {showReportForm && (
              <div className="form-panel" style={{ marginBottom: '24px' }}>
                <div className="form-panel-header">
                  <h3>Submit a Waste Issue Report</h3>
                  <p>All reports are sent directly to municipal authorities for resolution.</p>
                </div>
                <form onSubmit={handleCreateReport}>
                  <div className="form-grid">
                    <div className="form-group form-full-width">
                      <label>Report Title *</label>
                      <input
                        type="text"
                        placeholder="e.g. Overflowing garbage container on Oak Street"
                        value={newReport.title}
                        onChange={(e) =>
                          setNewReport({ ...newReport, title: e.target.value })
                        }
                        required
                      />
                    </div>

                    <div className="form-group">
                      <label>Issue Type *</label>
                      <select
                        value={newReport.issueType}
                        onChange={(e) =>
                          setNewReport({ ...newReport, issueType: e.target.value })
                        }
                        required
                      >
                        <option value="ILLEGAL_DUMPING">Illegal Dumping</option>
                        <option value="OVERFLOWING_BIN">Overflowing Public Bin</option>
                        <option value="HAZARDOUS_WASTE">Hazardous / Chemical Waste</option>
                        <option value="BLOCKED_DRAIN">Blocked Drainage / Litter</option>
                        <option value="OTHER">Other Waste Issue</option>
                      </select>
                    </div>

                    <div className="form-group">
                      <label>Suggested Category (Optional)</label>
                      <select
                        value={newReport.userCategoryId}
                        onChange={(e) =>
                          setNewReport({ ...newReport, userCategoryId: e.target.value })
                        }
                      >
                        <option value="">-- Auto-detect or select --</option>
                        {categories.map((c) => (
                          <option key={c.id} value={c.id}>
                            {c.name}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="form-group form-full-width">
                      <label>Coordinates (Latitude & Longitude) *</label>
                      <div className="location-input-row">
                        <input
                          type="number"
                          step="any"
                          placeholder="Latitude (e.g. 12.971598)"
                          value={newReport.latitude}
                          onChange={(e) =>
                            setNewReport({ ...newReport, latitude: e.target.value })
                          }
                          required
                        />
                        <input
                          type="number"
                          step="any"
                          placeholder="Longitude (e.g. 77.594566)"
                          value={newReport.longitude}
                          onChange={(e) =>
                            setNewReport({ ...newReport, longitude: e.target.value })
                          }
                          required
                        />
                        <button
                          type="button"
                          className="location-detect-btn"
                          onClick={() => detectLocation('report')}
                        >
                          📍 Detect My Location
                        </button>
                      </div>
                    </div>

                    <div className="form-group">
                      <label>Street Address</label>
                      <input
                        type="text"
                        placeholder="e.g. 142 Green Avenue"
                        value={newReport.address}
                        onChange={(e) =>
                          setNewReport({ ...newReport, address: e.target.value })
                        }
                      />
                    </div>

                    <div className="form-group">
                      <label>Locality / Neighborhood</label>
                      <input
                        type="text"
                        placeholder="e.g. Downtown Central"
                        value={newReport.locality}
                        onChange={(e) =>
                          setNewReport({ ...newReport, locality: e.target.value })
                        }
                      />
                    </div>

                    <div className="form-group form-full-width">
                      <label>Detailed Description *</label>
                      <textarea
                        placeholder="Describe the issue, estimated volume, hazards, or accessibility..."
                        value={newReport.description}
                        onChange={(e) =>
                          setNewReport({ ...newReport, description: e.target.value })
                        }
                        required
                      />
                    </div>

                    <div className="form-group form-full-width">
                      <label>Attach Evidence Photo (triggers AI Waste Classification)</label>
                      <input
                        type="file"
                        accept="image/*"
                        onChange={(e) => {
                          const file = e.target.files?.[0]
                          setReportImage(file || null)
                          if (file) {
                            setReportImagePreview(URL.createObjectURL(file))
                          } else {
                            setReportImagePreview(null)
                          }
                        }}
                      />
                      {reportImagePreview && (
                        <div className="image-preview-box">
                          <img src={reportImagePreview} alt="Preview" />
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="form-actions-row">
                    <button
                      type="button"
                      className="secondary-action-btn"
                      onClick={() => setShowReportForm(false)}
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      className="primary-action-btn"
                      disabled={submitting}
                    >
                      {submitting ? 'Submitting Report...' : 'Submit Report ↗'}
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* Reports List */}
            {reports.length === 0 ? (
              <div className="empty-state">
                <div className="empty-state-icon">📋</div>
                <h3>No reports submitted yet</h3>
                <p>Have you spotted an issue with waste or dumping? Be the first to report it!</p>
                <button
                  className="primary-action-btn"
                  onClick={() => setShowReportForm(true)}
                >
                  + Submit Your First Report
                </button>
              </div>
            ) : (
              <div className="items-list-grid">
                {reports.map((item) => (
                  <div key={item.id} className="item-card">
                    <div className="item-card-header">
                      <div>
                        <h4 className="item-title">#{item.id} · {item.title}</h4>
                        <div className="item-meta">
                          <span>📅 {new Date(item.createdAt).toLocaleDateString()}</span>
                          {item.locality && <span>📍 {item.locality}</span>}
                        </div>
                      </div>
                      <span className={`status-pill ${item.status?.toLowerCase()}`}>
                        {item.status}
                      </span>
                    </div>

                    <p className="item-description">{item.description}</p>

                    {item.images && item.images.length > 0 && (
                      <div style={{ marginTop: '4px' }}>
                        {item.images.map((img) => (
                          <div key={img.id} style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                            <img
                              src={`/api/v1/files/${img.filePath}`}
                              alt={img.fileName || 'Report attachment'}
                              className="item-image-thumb"
                              onError={(e) => {
                                e.target.style.display = 'none'
                              }}
                            />
                            {/* AI Classification Info Box */}
                            <div className="ai-classification-box">
                              <div>
                                <span className="ai-badge">AI CLASSIFIER</span>
                                {img.classification ? (
                                  <span>
                                    <strong>{img.classification.predictedCategory?.name || img.classification.predictedCategory || 'Processed'}</strong>
                                    {img.classification.confidenceScore && (
                                      <span> ({Math.round(img.classification.confidenceScore * 100)}% match)</span>
                                    )}
                                  </span>
                                ) : (
                                  <span>Classification recorded</span>
                                )}
                              </div>
                              <button
                                className="ai-classify-btn"
                                onClick={() => handleTriggerAiClassification(item.id, img.id)}
                              >
                                Re-classify
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}

                    <div className="item-footer-actions">
                      <span style={{ fontSize: '12px', color: '#63776b' }}>
                        Issue: <strong>{item.issueType || 'General'}</strong>
                      </span>
                      {item.latitude && item.longitude && (
                        <a
                          href={`https://www.google.com/maps/search/?api=1&query=${item.latitude},${item.longitude}`}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="map-action-btn"
                        >
                          🗺️ View Map
                        </a>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        {/* -------------------- 3. SPECIAL PICKUPS TAB -------------------- */}
        {activeTab === 'pickups' && (
          <section>
            <div className="action-bar">
              <div>
                <h2>Special & Bulky Waste Pickups</h2>
                <p className="section-desc">
                  Schedule direct home pickup for electronics, furniture, and oversized recyclables.
                </p>
              </div>
              <button
                className="primary-action-btn"
                onClick={() => setShowPickupForm(!showPickupForm)}
              >
                {showPickupForm ? '✕ Close Form' : '+ Book Special Pickup'}
              </button>
            </div>

            {/* Interactive Pickup Form */}
            {showPickupForm && (
              <div className="form-panel" style={{ marginBottom: '24px' }}>
                <div className="form-panel-header">
                  <h3>Schedule Doorstep Waste Pickup</h3>
                  <p>Certified municipal service providers will collect items on your chosen date.</p>
                </div>
                <form onSubmit={handleCreatePickup}>
                  <div className="form-grid">
                    <div className="form-group">
                      <label>Waste Category *</label>
                      <select
                        value={newPickup.categoryId}
                        onChange={(e) =>
                          setNewPickup({ ...newPickup, categoryId: e.target.value })
                        }
                        required
                      >
                        <option value="">-- Select Category --</option>
                        {categories.map((c) => (
                          <option key={c.id} value={c.id}>
                            {c.name} {c.isRecyclable ? '(Recyclable)' : ''}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="form-group">
                      <label>Preferred Date *</label>
                      <input
                        type="date"
                        min={new Date().toISOString().split('T')[0]}
                        value={newPickup.preferredDate}
                        onChange={(e) =>
                          setNewPickup({ ...newPickup, preferredDate: e.target.value })
                        }
                        required
                      />
                    </div>

                    <div className="form-group">
                      <label>Time Window *</label>
                      <select
                        value={newPickup.timeWindow}
                        onChange={(e) =>
                          setNewPickup({ ...newPickup, timeWindow: e.target.value })
                        }
                        required
                      >
                        <option value="09:00 - 12:00">Morning (09:00 - 12:00)</option>
                        <option value="12:00 - 15:00">Afternoon (12:00 - 15:00)</option>
                        <option value="15:00 - 18:00">Evening (15:00 - 18:00)</option>
                      </select>
                    </div>

                    <div className="form-group">
                      <label>Locality / Area</label>
                      <input
                        type="text"
                        placeholder="e.g. North Sector"
                        value={newPickup.locality}
                        onChange={(e) =>
                          setNewPickup({ ...newPickup, locality: e.target.value })
                        }
                      />
                    </div>

                    <div className="form-group form-full-width">
                      <label>Pickup Address *</label>
                      <input
                        type="text"
                        placeholder="House / Apartment number, street name"
                        value={newPickup.address}
                        onChange={(e) =>
                          setNewPickup({ ...newPickup, address: e.target.value })
                        }
                        required
                      />
                    </div>

                    <div className="form-group form-full-width">
                      <label>Coordinates (Optional for Navigation)</label>
                      <div className="location-input-row">
                        <input
                          type="number"
                          step="any"
                          placeholder="Latitude"
                          value={newPickup.latitude}
                          onChange={(e) =>
                            setNewPickup({ ...newPickup, latitude: e.target.value })
                          }
                        />
                        <input
                          type="number"
                          step="any"
                          placeholder="Longitude"
                          value={newPickup.longitude}
                          onChange={(e) =>
                            setNewPickup({ ...newPickup, longitude: e.target.value })
                          }
                        />
                        <button
                          type="button"
                          className="location-detect-btn"
                          onClick={() => detectLocation('pickup')}
                        >
                          📍 Detect My Location
                        </button>
                      </div>
                    </div>

                    <div className="form-group form-full-width">
                      <label>Item Description & Quantity *</label>
                      <textarea
                        placeholder="e.g. 1 broken refrigerator, 2 old wooden chairs, 3 bags of e-waste cables..."
                        value={newPickup.description}
                        onChange={(e) =>
                          setNewPickup({ ...newPickup, description: e.target.value })
                        }
                        required
                      />
                    </div>

                    <div className="form-group form-full-width">
                      <div className="form-checkbox-row">
                        <input
                          type="checkbox"
                          id="isSpecialHandling"
                          checked={newPickup.isSpecialHandling}
                          onChange={(e) =>
                            setNewPickup({
                              ...newPickup,
                              isSpecialHandling: e.target.checked,
                            })
                          }
                        />
                        <label htmlFor="isSpecialHandling">
                          Requires Special Handling (heavy lifting, sharp metals, glass, chemical precautions)
                        </label>
                      </div>
                    </div>

                    {newPickup.isSpecialHandling && (
                      <div className="form-group form-full-width">
                        <label>Special Handling Instructions</label>
                        <input
                          type="text"
                          placeholder="e.g. 3rd floor apartment without elevator; heavy glass panels"
                          value={newPickup.specialHandlingNotes}
                          onChange={(e) =>
                            setNewPickup({
                              ...newPickup,
                              specialHandlingNotes: e.target.value,
                            })
                          }
                        />
                      </div>
                    )}

                    <div className="form-group form-full-width">
                      <label>Attach Item Photo (Optional)</label>
                      <input
                        type="file"
                        accept="image/*"
                        onChange={(e) => {
                          const file = e.target.files?.[0]
                          setPickupImage(file || null)
                          if (file) {
                            setPickupImagePreview(URL.createObjectURL(file))
                          } else {
                            setPickupImagePreview(null)
                          }
                        }}
                      />
                      {pickupImagePreview && (
                        <div className="image-preview-box">
                          <img src={pickupImagePreview} alt="Pickup Preview" />
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="form-actions-row">
                    <button
                      type="button"
                      className="secondary-action-btn"
                      onClick={() => setShowPickupForm(false)}
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      className="primary-action-btn"
                      disabled={submitting}
                    >
                      {submitting ? 'Scheduling...' : 'Schedule Pickup ↗'}
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* Pickups List */}
            {pickups.length === 0 ? (
              <div className="empty-state">
                <div className="empty-state-icon">🚛</div>
                <h3>No pickup requests yet</h3>
                <p>Have bulky waste, appliances, or recyclables that require special transport?</p>
                <button
                  className="primary-action-btn"
                  onClick={() => setShowPickupForm(true)}
                >
                  + Book Special Pickup
                </button>
              </div>
            ) : (
              <div className="items-list-grid">
                {pickups.map((item) => (
                  <div key={item.id} className="item-card">
                    <div className="item-card-header">
                      <div>
                        <h4 className="item-title">Pickup #{item.id}</h4>
                        <div className="item-meta">
                          <span>📅 Preferred: {item.preferredDate}</span>
                          <span>⏰ {item.timeWindow}</span>
                        </div>
                      </div>
                      <span className={`status-pill ${item.status?.toLowerCase()}`}>
                        {item.status}
                      </span>
                    </div>

                    <p className="item-description">{item.description}</p>

                    <div style={{ fontSize: '13px', color: '#556b5f' }}>
                      <p style={{ margin: '4px 0' }}>📍 <strong>Address:</strong> {item.address}</p>
                      {item.category && (
                        <p style={{ margin: '4px 0' }}>📦 <strong>Category:</strong> {item.category.name}</p>
                      )}
                      {item.isSpecialHandling && (
                        <p style={{ margin: '4px 0', color: '#b45309' }}>
                          ⚠️ <strong>Special Handling:</strong> {item.specialHandlingNotes || 'Yes'}
                        </p>
                      )}
                    </div>

                    {item.imagePath && (
                      <img
                        src={`/api/v1/files/${item.imagePath}`}
                        alt="Pickup item"
                        className="item-image-thumb"
                        onError={(e) => {
                          e.target.style.display = 'none'
                        }}
                      />
                    )}

                    <div className="item-footer-actions">
                      <span style={{ fontSize: '12.5px', color: '#7a8e82' }}>
                        Requested {new Date(item.createdAt).toLocaleDateString()}
                      </span>
                      {['REQUESTED', 'SCHEDULED'].includes(item.status) && (
                        <button
                          className="cancel-action-btn"
                          onClick={() => handleCancelPickup(item.id)}
                        >
                          ✕ Cancel Request
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        {/* -------------------- 4. COLLECTION SCHEDULES TAB -------------------- */}
        {activeTab === 'schedules' && (
          <section>
            <div className="action-bar">
              <div>
                <h2>Municipal Collection Schedules</h2>
                <p className="section-desc">
                  View scheduled municipal waste and recycling collection days for your locality.
                </p>
              </div>
              {(user.role === 'AUTHORITY' || user.role === 'ADMIN') && (
                <button
                  className="primary-action-btn"
                  onClick={() => setShowScheduleForm(!showScheduleForm)}
                >
                  {showScheduleForm ? '✕ Close Form' : '+ Add Collection Route'}
                </button>
              )}
            </div>

            {/* Interactive Schedule Creation Form */}
            {showScheduleForm && (user.role === 'AUTHORITY' || user.role === 'ADMIN') && (
              <div className="form-panel" style={{ marginBottom: '24px' }}>
                <div className="form-panel-header">
                  <h3>Create Municipal Collection Route</h3>
                  <p>Designate collection schedules for specific neighborhoods and waste categories.</p>
                </div>
                <form onSubmit={handleCreateSchedule}>
                  <div className="form-grid">
                    <div className="form-group">
                      <label>Locality / Neighborhood *</label>
                      <input
                        type="text"
                        placeholder="e.g. Downtown Central"
                        value={newSchedule.locality}
                        onChange={(e) =>
                          setNewSchedule({ ...newSchedule, locality: e.target.value })
                        }
                        required
                      />
                    </div>

                    <div className="form-group">
                      <label>Service Area *</label>
                      <input
                        type="text"
                        placeholder="e.g. Ward 3, Sector B"
                        value={newSchedule.serviceArea}
                        onChange={(e) =>
                          setNewSchedule({ ...newSchedule, serviceArea: e.target.value })
                        }
                        required
                      />
                    </div>

                    <div className="form-group">
                      <label>Collection Day *</label>
                      <select
                        value={newSchedule.collectionDayOfWeek}
                        onChange={(e) =>
                          setNewSchedule({
                            ...newSchedule,
                            collectionDayOfWeek: e.target.value,
                          })
                        }
                        required
                      >
                        <option value="Monday">Monday</option>
                        <option value="Tuesday">Tuesday</option>
                        <option value="Wednesday">Wednesday</option>
                        <option value="Thursday">Thursday</option>
                        <option value="Friday">Friday</option>
                        <option value="Saturday">Saturday</option>
                        <option value="Sunday">Sunday</option>
                      </select>
                    </div>

                    <div className="form-group">
                      <label>Waste Category *</label>
                      <select
                        value={newSchedule.categoryId}
                        onChange={(e) =>
                          setNewSchedule({
                            ...newSchedule,
                            categoryId: e.target.value,
                          })
                        }
                        required
                      >
                        <option value="">-- Select Category --</option>
                        {categories.map((c) => (
                          <option key={c.id} value={c.id}>
                            {c.name}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="form-group">
                      <label>Start Time *</label>
                      <input
                        type="time"
                        value={newSchedule.startTime}
                        onChange={(e) =>
                          setNewSchedule({ ...newSchedule, startTime: e.target.value })
                        }
                        required
                      />
                    </div>

                    <div className="form-group">
                      <label>End Time *</label>
                      <input
                        type="time"
                        value={newSchedule.endTime}
                        onChange={(e) =>
                          setNewSchedule({ ...newSchedule, endTime: e.target.value })
                        }
                        required
                      />
                    </div>

                    <div className="form-group form-full-width">
                      <label>Route Notes & Instructions</label>
                      <input
                        type="text"
                        placeholder="e.g. Leave bins curbside by 07:30 AM; separate dry paper"
                        value={newSchedule.notes}
                        onChange={(e) =>
                          setNewSchedule({ ...newSchedule, notes: e.target.value })
                        }
                      />
                    </div>
                  </div>

                  <div className="form-actions-row">
                    <button
                      type="button"
                      className="secondary-action-btn"
                      onClick={() => setShowScheduleForm(false)}
                    >
                      Cancel
                    </button>
                    <button type="submit" className="primary-action-btn">
                      Create Schedule Route ↗
                    </button>
                  </div>
                </form>
              </div>
            )}

            <div className="filter-bar">
              <input
                type="text"
                className="search-input"
                placeholder="Filter by locality (e.g. Downtown, West District)..."
                value={scheduleLocality}
                onChange={(e) => setScheduleLocality(e.target.value)}
              />
            </div>

            {schedules.length === 0 ? (
              <div className="empty-state">
                <div className="empty-state-icon">📅</div>
                <h3>No schedules found</h3>
                <p>No municipal collection routes currently match your locality filter.</p>
              </div>
            ) : (
              <div className="items-list-grid">
                {schedules.map((s) => (
                  <div key={s.id} className="item-card">
                    <div className="item-card-header">
                      <div>
                        <h4 className="item-title">{s.locality}</h4>
                        <div className="item-meta">
                          <span>Area: {s.serviceArea}</span>
                        </div>
                      </div>
                      <span className="status-pill scheduled">
                        {s.collectionDayOfWeek}
                      </span>
                    </div>

                    <div style={{ fontSize: '13.5px', color: '#334e40' }}>
                      <p style={{ margin: '4px 0' }}>
                        ⏰ <strong>Time:</strong> {s.startTime} - {s.endTime}
                      </p>
                      {s.wasteCategory && (
                        <p style={{ margin: '4px 0' }}>
                          ♻️ <strong>Collected Waste:</strong> {s.wasteCategory.name}
                        </p>
                      )}
                      {s.notes && (
                        <p style={{ margin: '6px 0', color: '#556b5f', fontStyle: 'italic' }}>
                          ℹ️ {s.notes}
                        </p>
                      )}
                    </div>

                    <div className="item-footer-actions">
                      <span style={{ fontSize: '12px', color: '#245d3e' }}>
                        ● Active Route
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        {/* -------------------- 5. RECYCLING CENTRES TAB -------------------- */}
        {activeTab === 'recycling' && (
          <section>
            <div className="action-bar">
              <div>
                <h2>Certified Recycling Centres</h2>
                <p className="section-desc">
                  Find verified local drop-off facilities, accepted materials, operating hours, and directions.
                </p>
              </div>
              {(user.role === 'AUTHORITY' || user.role === 'ADMIN') && (
                <button
                  className="primary-action-btn"
                  onClick={() => setShowCenterForm(!showCenterForm)}
                >
                  {showCenterForm ? '✕ Close Form' : '+ Register Recycling Centre'}
                </button>
              )}
            </div>

            {/* Interactive Recycling Centre Registration Form */}
            {showCenterForm && (user.role === 'AUTHORITY' || user.role === 'ADMIN') && (
              <div className="form-panel" style={{ marginBottom: '24px' }}>
                <div className="form-panel-header">
                  <h3>Register Certified Recycling Facility</h3>
                  <p>Add a verified recycling or drop-off centre with accepted materials and directions.</p>
                </div>
                <form onSubmit={handleCreateCenter}>
                  <div className="form-grid">
                    <div className="form-group form-full-width">
                      <label>Centre Name *</label>
                      <input
                        type="text"
                        placeholder="e.g. EcoGreen Regional Recovery Centre"
                        value={newCenter.name}
                        onChange={(e) =>
                          setNewCenter({ ...newCenter, name: e.target.value })
                        }
                        required
                      />
                    </div>

                    <div className="form-group form-full-width">
                      <label>Street Address *</label>
                      <input
                        type="text"
                        placeholder="e.g. 500 Industrial Parkway, West Sector"
                        value={newCenter.address}
                        onChange={(e) =>
                          setNewCenter({ ...newCenter, address: e.target.value })
                        }
                        required
                      />
                    </div>

                    <div className="form-group form-full-width">
                      <label>Coordinates (Latitude & Longitude) *</label>
                      <div className="location-input-row">
                        <input
                          type="number"
                          step="any"
                          placeholder="Latitude"
                          value={newCenter.latitude}
                          onChange={(e) =>
                            setNewCenter({ ...newCenter, latitude: e.target.value })
                          }
                          required
                        />
                        <input
                          type="number"
                          step="any"
                          placeholder="Longitude"
                          value={newCenter.longitude}
                          onChange={(e) =>
                            setNewCenter({ ...newCenter, longitude: e.target.value })
                          }
                          required
                        />
                        <button
                          type="button"
                          className="location-detect-btn"
                          onClick={() => detectLocation('center')}
                        >
                          📍 Detect Location
                        </button>
                      </div>
                    </div>

                    <div className="form-group form-full-width">
                      <label>Accepted Materials (Comma-separated) *</label>
                      <input
                        type="text"
                        placeholder="e.g. Plastic, Paper, Glass, Metal, E-Waste"
                        value={newCenter.acceptedMaterials}
                        onChange={(e) =>
                          setNewCenter({
                            ...newCenter,
                            acceptedMaterials: e.target.value,
                          })
                        }
                        required
                      />
                    </div>

                    <div className="form-group">
                      <label>Operating Hours</label>
                      <input
                        type="text"
                        placeholder="e.g. Mon-Sat 08:00 - 18:00"
                        value={newCenter.operatingHours}
                        onChange={(e) =>
                          setNewCenter({
                            ...newCenter,
                            operatingHours: e.target.value,
                          })
                        }
                      />
                    </div>

                    <div className="form-group">
                      <label>Contact Phone</label>
                      <input
                        type="tel"
                        placeholder="e.g. +1 555-0188"
                        value={newCenter.contactPhone}
                        onChange={(e) =>
                          setNewCenter({
                            ...newCenter,
                            contactPhone: e.target.value,
                          })
                        }
                      />
                    </div>

                    <div className="form-group">
                      <label>Contact Email</label>
                      <input
                        type="email"
                        placeholder="e.g. info@ecogreen.org"
                        value={newCenter.contactEmail}
                        onChange={(e) =>
                          setNewCenter({
                            ...newCenter,
                            contactEmail: e.target.value,
                          })
                        }
                      />
                    </div>

                    <div className="form-group">
                      <label>Website URL</label>
                      <input
                        type="url"
                        placeholder="https://example.org"
                        value={newCenter.websiteUrl}
                        onChange={(e) =>
                          setNewCenter({ ...newCenter, websiteUrl: e.target.value })
                        }
                      />
                    </div>

                    <div className="form-group form-full-width">
                      <label>Facility Description & Drop-off Guidelines</label>
                      <textarea
                        placeholder="e.g. Accepts domestic and commercial recyclables. Direct vehicle bay access..."
                        value={newCenter.description}
                        onChange={(e) =>
                          setNewCenter({ ...newCenter, description: e.target.value })
                        }
                      />
                    </div>
                  </div>

                  <div className="form-actions-row">
                    <button
                      type="button"
                      className="secondary-action-btn"
                      onClick={() => setShowCenterForm(false)}
                    >
                      Cancel
                    </button>
                    <button type="submit" className="primary-action-btn">
                      Register Facility ↗
                    </button>
                  </div>
                </form>
              </div>
            )}

            <div className="filter-bar">
              <input
                type="text"
                className="search-input"
                placeholder="Search centres by name, location, or address..."
                value={recyclingSearch}
                onChange={(e) => setRecyclingSearch(e.target.value)}
              />
              <select
                className="filter-select"
                value={materialFilter}
                onChange={(e) => setMaterialFilter(e.target.value)}
              >
                <option value="">All Materials</option>
                <option value="Plastic">Plastic</option>
                <option value="Glass">Glass</option>
                <option value="Paper">Paper & Cardboard</option>
                <option value="Metal">Metals</option>
                <option value="E-Waste">E-Waste</option>
              </select>
            </div>

            {displayedCentres.length === 0 ? (
              <div className="empty-state">
                <div className="empty-state-icon">🗺️</div>
                <h3>No recycling centres match your criteria</h3>
                <p>Try clearing your material filter or search term.</p>
              </div>
            ) : (
              <div className="items-list-grid">
                {displayedCentres.map((center) => (
                  <div key={center.id} className="item-card">
                    <div className="item-card-header">
                      <div>
                        <h4 className="item-title">{center.name}</h4>
                        <div className="item-meta">
                          <span>📍 {center.address}</span>
                        </div>
                      </div>
                    </div>

                    {center.description && (
                      <p className="item-description">{center.description}</p>
                    )}

                    <div>
                      <strong style={{ fontSize: '12px', color: '#245d3e', display: 'block', marginBottom: '4px' }}>
                        ACCEPTED MATERIALS:
                      </strong>
                      <div className="material-chips">
                        {(center.acceptedMaterials || '')
                          .split(',')
                          .map((m, idx) => (
                            <span key={idx} className="material-chip">
                              {m.trim()}
                            </span>
                          ))}
                      </div>
                    </div>

                    <div style={{ fontSize: '13px', color: '#556b5f' }}>
                      {center.operatingHours && (
                        <p style={{ margin: '4px 0' }}>🕒 <strong>Hours:</strong> {center.operatingHours}</p>
                      )}
                      {center.contactPhone && (
                        <p style={{ margin: '4px 0' }}>📞 <strong>Phone:</strong> {center.contactPhone}</p>
                      )}
                    </div>

                    <div className="item-footer-actions">
                      {center.websiteUrl ? (
                        <a
                          href={center.websiteUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          style={{ fontSize: '12.5px', color: '#245d3e' }}
                        >
                          Visit Website ↗
                        </a>
                      ) : (
                        <span></span>
                      )}
                      {center.latitude && center.longitude && (
                        <a
                          href={`https://www.google.com/maps/search/?api=1&query=${center.latitude},${center.longitude}`}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="map-action-btn"
                        >
                          🗺️ Open in Google Maps ↗
                        </a>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        {/* -------------------- 6. AI CLASSIFIER SHOWCASE TAB -------------------- */}
        {activeTab === 'ai' && (
          <section>
            <div className="ai-showcase-panel">
              <span className="welcome-eyebrow" style={{ color: '#245d3e' }}>
                AI-Powered Smart Waste Management
              </span>
              <h2>MobileNetV2 Waste Classification Service</h2>
              <p style={{ color: '#4b6355', fontSize: '14.5px', lineHeight: '1.6', maxWidth: '780px' }}>
                CleanLoop uses a deep convolutional neural network based on MobileNetV2 trained on the
                TrashNet benchmark dataset. When citizens upload waste report images, the microservice
                classifies the material category with confidence scoring and recommends proper recycling
                protocols.
              </p>

              <div className={`ai-service-status ${aiStatus === 'CONFIGURED' ? 'online' : 'offline'}`}>
                <strong>Boundary Architecture Status:</strong> {aiStatus}
                <p style={{ margin: '4px 0 0', fontSize: '12.5px' }}>
                  The backend microservice boundary (`FastApiClientClassificationService`) handles
                  on-demand image classification. If the dedicated GPU/FastAPI microservice is offline,
                  the platform stores reports safely with status <code>UNAVAILABLE</code> without crashing.
                </p>
              </div>

              <div className="ai-showcase-grid">
                <div>
                  <h4 style={{ margin: '0 0 10px', color: '#183c2d' }}>Supported Waste Classes</h4>
                  <div className="ai-classes-list">
                    <span className="ai-class-tag">♻️ Plastic</span>
                    <span className="ai-class-tag">📦 Cardboard</span>
                    <span className="ai-class-tag">📄 Paper</span>
                    <span className="ai-class-tag">🍾 Glass</span>
                    <span className="ai-class-tag">🥫 Metal</span>
                    <span className="ai-class-tag">🗑️ Trash / Mixed</span>
                    <span className="ai-class-tag">⚡ E-Waste</span>
                  </div>
                  <p style={{ fontSize: '13px', color: '#63776b', marginTop: '12px' }}>
                    Predictions are mapped directly to municipal recycling categories and provide real-time
                    handling advice to municipal authorities.
                  </p>
                </div>

                <div>
                  <h4 style={{ margin: '0 0 10px', color: '#183c2d' }}>How to Use Real AI Classification</h4>
                  <ol style={{ fontSize: '13.5px', color: '#4b6355', lineHeight: '1.6', paddingLeft: '20px' }}>
                    <li>
                      Go to the <strong>Waste Reports</strong> tab and click <strong>Submit New Report</strong>.
                    </li>
                    <li>
                      Attach an image of recyclable waste (e.g., plastic bottle, cardboard box, aluminum can).
                    </li>
                    <li>
                      The system automatically sends the image to <code>POST /api/v1/reports</code>.
                    </li>
                    <li>
                      Inspect your report card to view real-time confidence scores and category matching.
                    </li>
                  </ol>
                  <button
                    className="primary-action-btn"
                    onClick={() => {
                      switchTab('reports')
                      setShowReportForm(true)
                    }}
                    style={{ marginTop: '12px' }}
                  >
                    Try AI Report Submission ↗
                  </button>
                </div>
              </div>
            </div>
          </section>
        )}

        {/* -------------------- 7. ECO TIPS TAB -------------------- */}
        {activeTab === 'ecotips' && (
          <section>
            <div className="action-bar">
              <div>
                <h2>Educational Eco Tips</h2>
                <p className="section-desc">
                  Household guidelines, segregation advice, and sustainable waste reduction practices.
                </p>
              </div>
              {(user.role === 'AUTHORITY' || user.role === 'ADMIN') && (
                <button
                  className="primary-action-btn"
                  onClick={() => setShowTipForm(!showTipForm)}
                >
                  {showTipForm ? '✕ Close Form' : '+ Publish Eco Tip'}
                </button>
              )}
            </div>

            {/* Eco Tip Publishing Form */}
            {showTipForm && (
              <div className="form-panel" style={{ marginBottom: '24px' }}>
                <div className="form-panel-header">
                  <h3>Publish New Educational Eco Tip</h3>
                  <p>Provide verified community guidance for waste segregation and reduction.</p>
                </div>
                <form onSubmit={handleCreateEcoTip}>
                  <div className="form-grid">
                    <div className="form-group form-full-width">
                      <label>Tip Title *</label>
                      <input
                        type="text"
                        placeholder="e.g. How to properly rinse and prepare plastics for recycling"
                        value={newTip.title}
                        onChange={(e) => setNewTip({ ...newTip, title: e.target.value })}
                        required
                      />
                    </div>

                    <div className="form-group">
                      <label>Category *</label>
                      <select
                        value={newTip.category}
                        onChange={(e) => setNewTip({ ...newTip, category: e.target.value })}
                        required
                      >
                        <option value="Recycling">Recycling</option>
                        <option value="Composting">Composting</option>
                        <option value="Zero Waste">Zero Waste</option>
                        <option value="Segregation">Segregation</option>
                        <option value="Hazardous">Hazardous Disposal</option>
                      </select>
                    </div>

                    <div className="form-group">
                      <label>Image URL (Optional)</label>
                      <input
                        type="url"
                        placeholder="https://example.com/image.jpg"
                        value={newTip.imageUrl}
                        onChange={(e) => setNewTip({ ...newTip, imageUrl: e.target.value })}
                      />
                    </div>

                    <div className="form-group form-full-width">
                      <label>Guidance Content *</label>
                      <textarea
                        placeholder="Detailed advice, household tips, step-by-step instructions..."
                        value={newTip.content}
                        onChange={(e) => setNewTip({ ...newTip, content: e.target.value })}
                        required
                      />
                    </div>
                  </div>

                  <div className="form-actions-row">
                    <button
                      type="button"
                      className="secondary-action-btn"
                      onClick={() => setShowTipForm(false)}
                    >
                      Cancel
                    </button>
                    <button type="submit" className="primary-action-btn">
                      Publish Tip ↗
                    </button>
                  </div>
                </form>
              </div>
            )}

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

        {/* -------------------- 8. NOTIFICATIONS TAB -------------------- */}
        {activeTab === 'notifications' && (
          <section>
            <div className="action-bar">
              <div>
                <h2>Your Notifications</h2>
                <p className="section-desc">
                  Status updates regarding your reports, pickups, and municipal notices.
                </p>
              </div>
              {notifications.length > 0 && (
                <button
                  className="secondary-action-btn"
                  onClick={handleMarkAllNotificationsRead}
                >
                  ✓ Mark All as Read
                </button>
              )}
            </div>

            {notifications.length === 0 ? (
              <div className="empty-state">
                <div className="empty-state-icon">🔔</div>
                <h3>No notifications</h3>
                <p>You are all caught up with your reports and pickup updates!</p>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {notifications.map((n) => (
                  <div
                    key={n.id}
                    className="item-card"
                    style={{
                      borderLeft: n.isRead ? '1px solid #e4eadf' : '4px solid #3cb974',
                      background: n.isRead ? '#ffffff' : '#f9fdfa',
                    }}
                  >
                    <div className="item-card-header">
                      <div>
                        <h4 className="item-title">{n.title}</h4>
                        <div className="item-meta">
                          <span>{new Date(n.createdAt).toLocaleString()}</span>
                          {n.type && <span>· Type: {n.type}</span>}
                        </div>
                      </div>
                      {!n.isRead && (
                        <button
                          className="secondary-action-btn"
                          style={{ padding: '4px 10px', fontSize: '12px' }}
                          onClick={() => handleMarkNotificationRead(n.id)}
                        >
                          Mark as read
                        </button>
                      )}
                    </div>
                    <p className="item-description">{n.message}</p>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        {/* -------------------- 9. USER PROFILE TAB -------------------- */}
        {activeTab === 'profile' && (
          <section>
            <div className="action-bar">
              <div>
                <h2>Account & Preferences</h2>
                <p className="section-desc">
                  Manage your personal details and account credentials.
                </p>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '24px' }}>
              {/* Profile Details Form */}
              <div className="form-panel">
                <div className="form-panel-header">
                  <h3>Personal Information</h3>
                  <p>Your details are used to verify pickups and route updates.</p>
                </div>
                <form onSubmit={handleUpdateProfile}>
                  <div className="form-group" style={{ marginBottom: '14px' }}>
                    <label>Email Address</label>
                    <input type="email" value={user.email} disabled style={{ opacity: 0.7 }} />
                  </div>
                  <div className="form-group" style={{ marginBottom: '14px' }}>
                    <label>Full Name *</label>
                    <input
                      type="text"
                      value={profileForm.fullName}
                      onChange={(e) =>
                        setProfileForm({ ...profileForm, fullName: e.target.value })
                      }
                      required
                    />
                  </div>
                  <div className="form-group" style={{ marginBottom: '14px' }}>
                    <label>Phone Number</label>
                    <input
                      type="tel"
                      placeholder="e.g. +1 555-0199"
                      value={profileForm.phoneNumber}
                      onChange={(e) =>
                        setProfileForm({ ...profileForm, phoneNumber: e.target.value })
                      }
                    />
                  </div>
                  <div className="form-group" style={{ marginBottom: '18px' }}>
                    <label>Service Area / Locality</label>
                    <input
                      type="text"
                      placeholder="e.g. Ward 4, Downtown"
                      value={profileForm.serviceArea}
                      onChange={(e) =>
                        setProfileForm({ ...profileForm, serviceArea: e.target.value })
                      }
                    />
                  </div>
                  <button type="submit" className="primary-action-btn">
                    Save Profile Changes
                  </button>
                </form>
              </div>

              {/* Password Change Form */}
              <div className="form-panel">
                <div className="form-panel-header">
                  <h3>Change Password</h3>
                  <p>Keep your account secure with a strong password.</p>
                </div>
                <form onSubmit={handleChangePassword}>
                  <div className="form-group" style={{ marginBottom: '14px' }}>
                    <label>Current Password *</label>
                    <input
                      type="password"
                      placeholder="••••••••"
                      value={passwordForm.currentPassword}
                      onChange={(e) =>
                        setPasswordForm({
                          ...passwordForm,
                          currentPassword: e.target.value,
                        })
                      }
                      required
                    />
                  </div>
                  <div className="form-group" style={{ marginBottom: '14px' }}>
                    <label>New Password (min 6 characters) *</label>
                    <input
                      type="password"
                      placeholder="••••••••"
                      value={passwordForm.newPassword}
                      onChange={(e) =>
                        setPasswordForm({
                          ...passwordForm,
                          newPassword: e.target.value,
                        })
                      }
                      required
                    />
                  </div>
                  <div className="form-group" style={{ marginBottom: '18px' }}>
                    <label>Confirm New Password *</label>
                    <input
                      type="password"
                      placeholder="••••••••"
                      value={passwordForm.confirmPassword}
                      onChange={(e) =>
                        setPasswordForm({
                          ...passwordForm,
                          confirmPassword: e.target.value,
                        })
                      }
                      required
                    />
                  </div>
                  <button type="submit" className="primary-action-btn">
                    Update Password
                  </button>
                </form>
              </div>
            </div>
          </section>
        )}

        {/* -------------------- 10. AUTHORITY DASHBOARD TAB -------------------- */}
        {activeTab === 'authority' && (user.role === 'AUTHORITY' || user.role === 'ADMIN') && (
          <section>
            <div className="action-bar">
              <div>
                <h2>Authority Operational Console</h2>
                <p className="section-desc">
                  Manage municipal workloads, review submitted community reports, and update pickup statuses.
                </p>
              </div>
            </div>

            {/* Authority Metrics Grid */}
            {authorityMetrics && (
              <div className="stats-grid" style={{ marginBottom: '24px' }}>
                <div className="stat-card">
                  <div className="stat-header">
                    <span>Pending Reports</span>
                    <span className="stat-icon">⚠️</span>
                  </div>
                  <p className="stat-value">{authorityMetrics.pendingReports ?? 0}</p>
                  <p className="stat-subtext">Awaiting municipal action</p>
                </div>
                <div className="stat-card">
                  <div className="stat-header">
                    <span>Unassigned Pickups</span>
                    <span className="stat-icon">🚛</span>
                  </div>
                  <p className="stat-value">{authorityMetrics.unassignedPickups ?? 0}</p>
                  <p className="stat-subtext">Needs vehicle allocation</p>
                </div>
                <div className="stat-card">
                  <div className="stat-header">
                    <span>Active Routes</span>
                    <span className="stat-icon">📅</span>
                  </div>
                  <p className="stat-value">{authorityMetrics.activeSchedules ?? 0}</p>
                  <p className="stat-subtext">In service area</p>
                </div>
                <div className="stat-card">
                  <div className="stat-header">
                    <span>Resolved This Month</span>
                    <span className="stat-icon">✅</span>
                  </div>
                  <p className="stat-value">{authorityMetrics.resolvedThisMonth ?? 0}</p>
                  <p className="stat-subtext">Issues cleared</p>
                </div>
              </div>
            )}

            {/* Reports Management Table */}
            <h3 style={{ margin: '24px 0 12px', color: '#183c2d' }}>Municipal Reports Workload</h3>
            <div className="table-panel">
              <div className="data-table-wrapper">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Title</th>
                      <th>Locality</th>
                      <th>Type</th>
                      <th>Current Status</th>
                      <th>Update Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {allReports.map((r) => (
                      <tr key={r.id}>
                        <td><strong>#{r.id}</strong></td>
                        <td>{r.title}</td>
                        <td>{r.locality || 'N/A'}</td>
                        <td>{r.issueType}</td>
                        <td>
                          <span className={`status-pill ${r.status?.toLowerCase()}`}>
                            {r.status}
                          </span>
                        </td>
                        <td>
                          <select
                            className="inline-select"
                            value={r.status}
                            onChange={(e) => handleUpdateReportStatus(r.id, e.target.value)}
                          >
                            <option value="SUBMITTED">SUBMITTED</option>
                            <option value="UNDER_REVIEW">UNDER_REVIEW</option>
                            <option value="IN_PROGRESS">IN_PROGRESS</option>
                            <option value="RESOLVED">RESOLVED</option>
                            <option value="REJECTED">REJECTED</option>
                          </select>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Pickups Management Table */}
            <h3 style={{ margin: '30px 0 12px', color: '#183c2d' }}>Scheduled Pickups Workload</h3>
            <div className="table-panel">
              <div className="data-table-wrapper">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Address</th>
                      <th>Date</th>
                      <th>Time</th>
                      <th>Current Status</th>
                      <th>Update Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {allPickups.map((p) => (
                      <tr key={p.id}>
                        <td><strong>#{p.id}</strong></td>
                        <td>{p.address}</td>
                        <td>{p.preferredDate}</td>
                        <td>{p.timeWindow}</td>
                        <td>
                          <span className={`status-pill ${p.status?.toLowerCase()}`}>
                            {p.status}
                          </span>
                        </td>
                        <td>
                          <select
                            className="inline-select"
                            value={p.status}
                            onChange={(e) => handleUpdatePickupStatus(p.id, e.target.value)}
                          >
                            <option value="REQUESTED">REQUESTED</option>
                            <option value="SCHEDULED">SCHEDULED</option>
                            <option value="IN_PROGRESS">IN_PROGRESS</option>
                            <option value="COMPLETED">COMPLETED</option>
                            <option value="CANCELLED">CANCELLED</option>
                          </select>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </section>
        )}

        {/* -------------------- 11. ADMIN DASHBOARD TAB -------------------- */}
        {activeTab === 'admin' && user.role === 'ADMIN' && (
          <section>
            <div className="action-bar">
              <div>
                <h2>System Administrator Portal</h2>
                <p className="section-desc">
                  Platform-wide user provisioning, role assignments, system metrics, and audit log inspection.
                </p>
              </div>
            </div>

            {/* Admin Metrics Grid */}
            {adminMetrics && (
              <div className="stats-grid" style={{ marginBottom: '24px' }}>
                <div className="stat-card">
                  <div className="stat-header">
                    <span>Total Registered Users</span>
                    <span className="stat-icon">👥</span>
                  </div>
                  <p className="stat-value">{adminMetrics.totalUsers ?? 0}</p>
                  <p className="stat-subtext">{adminMetrics.activeUsers ?? 0} active</p>
                </div>
                <div className="stat-card">
                  <div className="stat-header">
                    <span>Total Reports</span>
                    <span className="stat-icon">📋</span>
                  </div>
                  <p className="stat-value">{adminMetrics.totalReports ?? 0}</p>
                  <p className="stat-subtext">{adminMetrics.resolvedReports ?? 0} resolved</p>
                </div>
                <div className="stat-card">
                  <div className="stat-header">
                    <span>Total Pickups</span>
                    <span className="stat-icon">🚛</span>
                  </div>
                  <p className="stat-value">{adminMetrics.totalPickups ?? 0}</p>
                  <p className="stat-subtext">All time requests</p>
                </div>
                <div className="stat-card">
                  <div className="stat-header">
                    <span>Recycling Centres</span>
                    <span className="stat-icon">🏢</span>
                  </div>
                  <p className="stat-value">{adminMetrics.totalRecyclingCenters ?? 0}</p>
                  <p className="stat-subtext">Verified facilities</p>
                </div>
              </div>
            )}

            {/* User Management Table */}
            <h3 style={{ margin: '24px 0 12px', color: '#183c2d' }}>User Management & Role Provisioning</h3>
            <div className="table-panel">
              <div className="data-table-wrapper">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Name</th>
                      <th>Email</th>
                      <th>Role</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {usersList.map((u) => (
                      <tr key={u.id}>
                        <td><strong>#{u.id}</strong></td>
                        <td>{u.fullName}</td>
                        <td>{u.email}</td>
                        <td>
                          <select
                            className="inline-select"
                            value={u.role}
                            onChange={(e) => handleUpdateUserRole(u.id, e.target.value)}
                          >
                            <option value="CITIZEN">CITIZEN</option>
                            <option value="AUTHORITY">AUTHORITY</option>
                            <option value="SERVICE_PROVIDER">SERVICE_PROVIDER</option>
                            <option value="ADMIN">ADMIN</option>
                          </select>
                        </td>
                        <td>
                          <span
                            className="status-pill"
                            style={{
                              background: u.active ? '#f0fdf4' : '#fef2f2',
                              color: u.active ? '#15803d' : '#b91c1c',
                            }}
                          >
                            {u.active ? 'ACTIVE' : 'INACTIVE'}
                          </span>
                        </td>
                        <td>
                          <button
                            className="secondary-action-btn"
                            style={{ padding: '4px 10px', fontSize: '12px' }}
                            onClick={() => handleToggleUserStatus(u.id, u.active)}
                          >
                            {u.active ? 'Deactivate' : 'Activate'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* System Audit Logs */}
            <h3 style={{ margin: '30px 0 12px', color: '#183c2d' }}>Chronological Audit Logs</h3>
            <div className="table-panel">
              <div className="data-table-wrapper">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Timestamp</th>
                      <th>Action</th>
                      <th>Entity</th>
                      <th>Actor</th>
                      <th>Details</th>
                    </tr>
                  </thead>
                  <tbody>
                    {auditLogs.map((log) => (
                      <tr key={log.id}>
                        <td style={{ whiteSpace: 'nowrap' }}>
                          {new Date(log.createdAt).toLocaleString()}
                        </td>
                        <td>
                          <span className="status-pill submitted">{log.action}</span>
                        </td>
                        <td>{log.entityType} #{log.entityId}</td>
                        <td>{log.userEmail || 'System'}</td>
                        <td>{log.details}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </section>
        )}
      </main>

      {/* Footer */}
      <footer className="dashboard-footer">
        <p>CleanLoop · AI-Powered Smart Waste Management and Recycling System 🌍</p>
      </footer>
    </div>
  )
}
