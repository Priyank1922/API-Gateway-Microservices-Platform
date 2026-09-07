/**
 * API Gateway & Microservices Platform - Interactive Dashboard Engine
 */

document.addEventListener('DOMContentLoaded', () => {
    initLiveTelemetry();
    initCopyButtons();
});

// 1. Live Telemetry Polling
function initLiveTelemetry() {
    const statsContainer = document.getElementById('live-stats-badge');
    if (statsContainer) {
        fetchStats();
        setInterval(fetchStats, 5000);
    }

    const healthGrid = document.getElementById('services-health-grid');
    if (healthGrid) {
        fetchServiceHealth();
        setInterval(fetchServiceHealth, 6000);
    }
}

async function fetchStats() {
    try {
        const res = await fetch('/gateway-api/stats');
        if (!res.ok) return;
        const data = await res.json();

        updateElementText('stat-uptime', data.uptimeFormatted || '--');
        updateElementText('stat-total-reqs', (data.totalRequests || 0).toLocaleString());
        updateElementText('stat-success-reqs', (data.totalSuccesses || 0).toLocaleString());
        updateElementText('stat-error-reqs', (data.totalErrors || 0).toLocaleString());
        updateElementText('stat-rate-limited', (data.rateLimitedCount || 0).toLocaleString());
        updateElementText('stat-services-count', (data.registeredServicesCount || 0) + ' Nodes');
    } catch (e) {
        console.warn('Telemetry fetch notice:', e);
    }
}

async function fetchServiceHealth() {
    const healthGrid = document.getElementById('services-health-grid');
    if (!healthGrid) return;

    try {
        const res = await fetch('/gateway-api/services-health');
        if (!res.ok) return;
        const services = await res.json();

        healthGrid.innerHTML = services.map(s => {
            const isUp = s.status === 'UP';
            return `
                <div class="service-card card-hover">
                    <div class="service-top">
                        <div>
                            <div class="service-name">${s.serviceName}</div>
                            <div class="service-meta">${s.description} • Port ${s.port}</div>
                        </div>
                        <span class="service-status-badge ${isUp ? 'status-up' : 'status-down'}">
                            <span class="pulse-dot" style="${isUp ? '' : 'background-color: var(--danger);'}"></span>
                            ${s.status}
                        </span>
                    </div>
                    <div style="font-size: 0.75rem; color: var(--text-muted); font-family: monospace; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                        ${s.url}
                    </div>
                </div>
            `;
        }).join('');
    } catch (e) {
        console.warn('Service health check notice:', e);
    }
}

function updateElementText(id, text) {
    const el = document.getElementById(id);
    if (el) el.innerText = text;
}

// 2. Interactive API Sandbox Runner
let activeAuthToken = '';

async function executeApiRequest() {
    const method = document.getElementById('sb-method').value;
    const path = document.getElementById('sb-path').value.trim();
    const token = document.getElementById('sb-token').value.trim();
    const apiKey = document.getElementById('sb-api-key').value.trim();
    const bodyText = document.getElementById('sb-body').value.trim();

    const responseStatusEl = document.getElementById('sb-res-status');
    const responseTimeEl = document.getElementById('sb-res-time');
    const responseTraceEl = document.getElementById('sb-res-trace');
    const responseOutputEl = document.getElementById('sb-res-body');
    const sendBtn = document.getElementById('sb-send-btn');

    if (!path) {
        alert('Please specify a valid path (e.g. /api/products)');
        return;
    }

    sendBtn.disabled = true;
    sendBtn.innerText = 'Routing Request...';
    responseOutputEl.innerText = 'Connecting through Spring Cloud Gateway...';

    const headers = {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
    };

    if (token) {
        headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    }
    if (apiKey) {
        headers['X-API-Key'] = apiKey;
    }

    const options = {
        method: method,
        headers: headers
    };

    if (['POST', 'PUT', 'PATCH'].includes(method) && bodyText) {
        options.body = bodyText;
    }

    const startTime = performance.now();

    try {
        const res = await fetch(path, options);
        const duration = Math.round(performance.now() - startTime);

        const statusClass = res.ok ? 'code-2xx' : (res.status >= 500 ? 'code-5xx' : 'code-4xx');
        responseStatusEl.className = `badge-code ${statusClass}`;
        responseStatusEl.innerText = `${res.status} ${res.statusText}`;

        const traceId = res.headers.get('X-Correlation-Id') || 'trace-' + Math.random().toString(36).substring(2, 9);
        const gwLatency = res.headers.get('X-Response-Time-Millis');
        
        responseTimeEl.innerText = `${duration} ms ${gwLatency ? `(GW: ${gwLatency}ms)` : ''}`;
        responseTraceEl.innerText = traceId;

        const contentType = res.headers.get('Content-Type') || '';
        let data;
        if (contentType.includes('application/json')) {
            data = await res.json();
            responseOutputEl.innerText = JSON.stringify(data, null, 2);

            // Auto-store JWT token if login request succeeded
            if (path.includes('/api/auth/login') && data.token) {
                activeAuthToken = data.token;
                const tokenInput = document.getElementById('sb-token');
                if (tokenInput) tokenInput.value = data.token;
            }
        } else {
            const text = await res.text();
            responseOutputEl.innerText = text;
        }
    } catch (err) {
        const duration = Math.round(performance.now() - startTime);
        responseStatusEl.className = 'badge-code code-5xx';
        responseStatusEl.innerText = 'Network Error';
        responseTimeEl.innerText = `${duration} ms`;
        responseTraceEl.innerText = 'N/A';
        responseOutputEl.innerText = `Error contacting Gateway: ${err.message}`;
    } finally {
        sendBtn.disabled = false;
        sendBtn.innerText = 'Send Request ⚡';
    }
}

// 3. Quick Sandbox Presets
const SANDBOX_PRESETS = {
    'auth-login': {
        method: 'POST',
        path: '/api/auth/login',
        body: JSON.stringify({ username: 'admin', password: 'admin123' }, null, 2)
    },
    'auth-register': {
        method: 'POST',
        path: '/api/auth/register',
        body: JSON.stringify({ username: 'newuser_' + Math.floor(Math.random() * 1000), email: 'user' + Math.floor(Math.random() * 1000) + '@example.com', password: 'password123', role: 'ROLE_USER' }, null, 2)
    },
    'get-products': {
        method: 'GET',
        path: '/api/products',
        body: ''
    },
    'create-product': {
        method: 'POST',
        path: '/api/products',
        body: JSON.stringify({ name: 'Quantum Core Processor V5', sku: 'Q-CORE-' + Math.floor(Math.random() * 9000 + 1000), category: 'Hardware', price: 1450.00, stockQuantity: 35, description: 'Next-gen distributed quantum co-processor module.' }, null, 2)
    },
    'get-orders': {
        method: 'GET',
        path: '/api/orders',
        body: ''
    },
    'create-order': {
        method: 'POST',
        path: '/api/orders',
        body: JSON.stringify({ userId: 1, username: 'admin', paymentMethod: 'CREDIT_CARD', items: [{ productId: 1, productName: 'Edge API Gateway Accelerator X1', sku: 'GATEWAY-X1-PRO', price: 1299.99, quantity: 2 }] }, null, 2)
    },
    'ai-analytics': {
        method: 'GET',
        path: '/api/ai/analytics',
        body: ''
    },
    'ai-threat-test': {
        method: 'GET',
        path: '/api/products?search=\' UNION SELECT username,password FROM users--',
        body: ''
    }
};

function applyPreset(presetKey) {
    const preset = SANDBOX_PRESETS[presetKey];
    if (!preset) return;

    document.getElementById('sb-method').value = preset.method;
    document.getElementById('sb-path').value = preset.path;
    document.getElementById('sb-body').value = preset.body;

    // highlight active chip
    document.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
    const clickedChip = event ? event.currentTarget : null;
    if (clickedChip) clickedChip.classList.add('active');
}

// 4. Rate Limiter Burst Test Lab
let burstTestRunning = false;

async function runRateLimiterBurst(reqCount = 20) {
    if (burstTestRunning) return;
    burstTestRunning = true;

    const logEl = document.getElementById('burst-log-output');
    const gaugeEl = document.getElementById('burst-gauge-fill');
    const countOkEl = document.getElementById('burst-ok-count');
    const count429El = document.getElementById('burst-429-count');
    const startBtn = document.getElementById('burst-start-btn');

    if (startBtn) startBtn.disabled = true;
    if (logEl) logEl.innerHTML = '';
    if (countOkEl) countOkEl.innerText = '0';
    if (count429El) count429El.innerText = '0';

    let okCount = 0;
    let rateLimitedCount = 0;

    const appendLog = (msg, isRateLimited) => {
        if (!logEl) return;
        const p = document.createElement('div');
        p.style.padding = '0.35rem 0.5rem';
        p.style.borderBottom = '1px solid #1E293B';
        p.style.fontSize = '0.8rem';
        p.style.color = isRateLimited ? '#F87171' : '#34D399';
        p.innerText = msg;
        logEl.prepend(p);
    };

    appendLog(`🚀 Initiating rapid burst of ${reqCount} requests to /api/products...`, false);

    const promises = [];
    for (let i = 1; i <= reqCount; i++) {
        // small staggered dispatch
        const p = new Promise(resolve => setTimeout(resolve, i * 25)).then(async () => {
            const start = performance.now();
            try {
                const res = await fetch('/api/products?burstTest=' + i);
                const lat = Math.round(performance.now() - start);
                const remaining = res.headers.get('X-RateLimit-Remaining') || '?';

                if (res.status === 429) {
                    rateLimitedCount++;
                    if (count429El) count429El.innerText = rateLimitedCount;
                    appendLog(`[Req #${i}] 🛑 HTTP 429 Too Many Requests (Rate limit triggered! Remaining: ${remaining}) - ${lat}ms`, true);
                } else {
                    okCount++;
                    if (countOkEl) countOkEl.innerText = okCount;
                    appendLog(`[Req #${i}] ✅ HTTP ${res.status} OK (Tokens remaining: ${remaining}) - ${lat}ms`, false);
                }
            } catch (e) {
                appendLog(`[Req #${i}] ❌ Failed: ${e.message}`, true);
            }
        });
        promises.push(p);
    }

    await Promise.all(promises);

    appendLog(`🏁 Burst test complete. ${okCount} allowed, ${rateLimitedCount} rate limited (HTTP 429).`, rateLimitedCount > 0);
    if (gaugeEl) {
        const percent429 = Math.round((rateLimitedCount / reqCount) * 100);
        gaugeEl.style.width = `${percent429}%`;
    }

    if (startBtn) startBtn.disabled = false;
    burstTestRunning = false;
    fetchStats();
}

// 5. AI Threat Inspector Test Box
async function testAiPayload() {
    const payloadInput = document.getElementById('ai-test-payload');
    const resultBox = document.getElementById('ai-test-result');
    const payload = payloadInput ? payloadInput.value : '';

    if (!resultBox) return;
    resultBox.innerHTML = '<span style="color: var(--text-muted)">Inspecting with Deep Anomaly Engine...</span>';

    try {
        const res = await fetch('/api/ai/analyze-request', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                clientIp: '127.0.0.1',
                endpoint: '/api/products',
                httpMethod: 'POST',
                payload: payload
            })
        });

        const data = await res.json();
        const isMalicious = data.malicious;

        resultBox.innerHTML = `
            <div style="padding: 1rem; border-radius: 8px; background: ${isMalicious ? 'var(--danger-light)' : 'var(--success-light)'}; border: 1px solid ${isMalicious ? 'var(--danger-border)' : 'var(--success-border)'}">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                    <span style="font-weight: 700; color: ${isMalicious ? 'var(--danger)' : 'var(--success)'}; font-size: 1rem;">
                        ${isMalicious ? '⚠️ THREAT INTERCEPTED' : '✅ CLEAN PAYLOAD'}
                    </span>
                    <span class="badge-code ${isMalicious ? 'code-5xx' : 'code-2xx'}">
                        Threat Score: ${data.threatScore}/100 (${data.actionRecommended})
                    </span>
                </div>
                <div style="font-size: 0.85rem; color: var(--text-primary); margin-bottom: 0.5rem;">
                    ${data.explanation}
                </div>
                ${data.detectedPatterns && data.detectedPatterns.length ? `
                    <div style="font-size: 0.75rem; color: var(--danger); font-family: monospace;">
                        Detected: ${data.detectedPatterns.join(', ')}
                    </div>
                ` : ''}
            </div>
        `;
    } catch (e) {
        resultBox.innerHTML = `<span style="color: var(--danger)">Error running inspection: ${e.message}</span>`;
    }
}

// 6. Copy to Clipboard
function initCopyButtons() {
    document.querySelectorAll('.btn-copy').forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.getAttribute('data-target');
            const targetEl = document.getElementById(targetId);
            if (targetEl) {
                navigator.clipboard.writeText(targetEl.innerText || targetEl.value);
                const originalText = btn.innerText;
                btn.innerText = 'Copied! ✓';
                btn.style.color = 'var(--success)';
                setTimeout(() => {
                    btn.innerText = originalText;
                    btn.style.color = '';
                }, 1800);
            }
        });
    });
}
