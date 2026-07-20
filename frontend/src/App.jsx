import { useState, useEffect, useRef } from 'react'
import './App.css'

function App() {
  const [query, setQuery] = useState('')
  const [responses, setResponses] = useState([])
  const [metrics, setMetrics] = useState({
    cacheHit: false,
    cacheHitTimeMs: 0,
    retrievalTimeMs: 0,
    llmLatencyMs: 0,
    totalTimeMs: 0
  })
  const [isConnected, setIsConnected] = useState(false)
  const wsRef = useRef(null)

  useEffect(() => {
    // Generate a simple session ID for this demo
    const sessionId = Math.random().toString(36).substring(7)
    
    // Connect to WebSocket
    const ws = new WebSocket(`ws://localhost:8080/ws/chat?requestId=${sessionId}`)
    
    ws.onopen = () => {
      setIsConnected(true)
      console.log('WebSocket connected')
    }
    
    ws.onmessage = (event) => {
      const data = JSON.parse(event.data)
      setResponses((prev) => [...prev, data.token])
      
      if (data.final) {
        setMetrics({
          cacheHit: data.cacheHit,
          cacheHitTimeMs: data.cacheHitTimeMs,
          retrievalTimeMs: data.retrievalTimeMs,
          llmLatencyMs: data.llmLatencyMs,
          totalTimeMs: data.totalTimeMs
        })
      }
    }
    
    ws.onclose = () => {
      setIsConnected(false)
      console.log('WebSocket disconnected')
    }
    
    wsRef.current = ws
    
    return () => {
      ws.close()
    }
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!query.trim()) return
    
    // Extract requestId from WS URL
    const urlParams = new URL(wsRef.current.url).searchParams
    const requestId = urlParams.get('requestId')
    
    // Send via REST
    try {
      setResponses([])
      const res = await fetch('http://localhost:8080/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ requestId, query })
      })
      if (res.ok) {
        setQuery('')
      }
    } catch (err) {
      console.error('Error submitting chat:', err)
    }
  }

  return (
    <div className="container">
      <header className="header">
        <h1>Cortex Gate</h1>
        <div className={`status ${isConnected ? 'connected' : 'disconnected'}`}>
          {isConnected ? 'Connected' : 'Disconnected'}
        </div>
      </header>
      
      <div className="main-content">
        <div className="chat-section">
          <div className="response-view">
            {responses.length === 0 ? (
              <div className="placeholder">Responses will appear here...</div>
            ) : (
              <div className="response-content">
                {responses.join('')}
              </div>
            )}
          </div>
          
          <form className="chat-input" onSubmit={handleSubmit}>
            <textarea 
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Ask a question..."
              rows={3}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  handleSubmit(e);
                }
              }}
            />
            <button type="submit" disabled={!isConnected || !query.trim()}>
              Send
            </button>
          </form>
        </div>
        
        <div className="metrics-panel">
          <h2>Live Metrics</h2>
          <div className="metric-item">
            <span className="label">Cache Status:</span>
            <span className={`value ${metrics.cacheHit ? 'hit' : 'miss'}`}>
              {metrics.cacheHit ? 'HIT' : 'MISS'}
            </span>
          </div>
          <div className="metric-item">
            <span className="label">Cache Check Time:</span>
            <span className="value">{metrics.cacheHitTimeMs} ms</span>
          </div>
          <div className="metric-item">
            <span className="label">Vector Retrieval:</span>
            <span className="value">{metrics.retrievalTimeMs} ms</span>
          </div>
          <div className="metric-item">
            <span className="label">LLM Latency:</span>
            <span className="value">{metrics.llmLatencyMs} ms</span>
          </div>
          <div className="metric-item total">
            <span className="label">Total Request Time:</span>
            <span className="value">{metrics.totalTimeMs} ms</span>
          </div>
        </div>
      </div>
    </div>
  )
}

export default App
