/**
 * Automated Load and Stress Testing Suite for Finance Tracker API
 * Measures: Baseline (1 VU), Target Load (100 VUs - NFR-02), and Stress / Degradation Curve.
 */

const http = require('http');
const fs = require('fs');
const path = require('path');

const BASE_URL = 'http://localhost:8080';
const HOST = 'localhost';
const PORT = 8080;

function httpRequest(options, postData = null) {
  return new Promise((resolve, reject) => {
    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => data += chunk);
      res.on('end', () => {
        resolve({
          statusCode: res.statusCode,
          headers: res.headers,
          body: data
        });
      });
    });

    req.on('error', (err) => reject(err));
    req.setTimeout(10000, () => {
      req.destroy(new Error('Request timed out'));
    });

    if (postData) {
      req.write(postData);
    }
    req.end();
  });
}

async function setupTestData() {
  console.log('[Setup] Preparing test user, account, and category...');
  const ts = Date.now();
  const email = `load_agent_${ts}@example.com`;
  const password = 'Password123!';

  // 1. Register User
  const regPayload = JSON.stringify({
    email,
    password,
    fullName: 'Load Testing Agent'
  });

  const regRes = await httpRequest({
    hostname: HOST,
    port: PORT,
    path: '/api/users/register',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(regPayload)
    }
  }, regPayload);

  let token;
  if (regRes.statusCode === 201) {
    token = JSON.parse(regRes.body).token;
  } else {
    // Login
    const loginPayload = JSON.stringify({ email, password });
    const loginRes = await httpRequest({
      hostname: HOST,
      port: PORT,
      path: '/api/users/login',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(loginPayload)
      }
    }, loginPayload);
    token = JSON.parse(loginRes.body).token;
  }

  // 2. Create Account
  const accPayload = JSON.stringify({
    name: `Load Test Card ${ts}`,
    type: 'CARD'
  });
  const accRes = await httpRequest({
    hostname: HOST,
    port: PORT,
    path: '/api/accounts',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(accPayload),
      'Authorization': `Bearer ${token}`
    }
  }, accPayload);
  const accountId = JSON.parse(accRes.body).id;

  // 3. Create Category
  const catPayload = JSON.stringify({
    name: `Category_${ts}`
  });
  const catRes = await httpRequest({
    hostname: HOST,
    port: PORT,
    path: '/api/categories',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(catPayload),
      'Authorization': `Bearer ${token}`
    }
  }, catPayload);
  const categoryId = JSON.parse(catRes.body).id;

  console.log(`[Setup] Ready: AccountId=${accountId}, CategoryId=${categoryId}`);
  return { token, accountId, categoryId };
}

// Low-overhead load generator
function runLoad(options) {
  const {
    connections,
    durationSec,
    token,
    accountId,
    categoryId
  } = options;

  return new Promise((resolve) => {
    const startTime = Date.now();
    const endTime = startTime + durationSec * 1000;
    const latencies = [];
    let completedRequests = 0;
    let failedRequests = 0;
    let activeConnections = 0;

    const agent = new http.Agent({
      keepAlive: true,
      maxSockets: connections * 2
    });

    const txPayload = JSON.stringify({
      accountId,
      categoryId,
      type: 'INCOME',
      amount: 100.00,
      description: 'Load test tx'
    });

    // Realistic load distribution
    function getRandomRequest() {
      const rand = Math.random();
      if (rand < 0.50) {
        return {
          path: '/api/accounts',
          method: 'GET'
        };
      } else if (rand < 0.70) {
        return {
          path: '/api/categories',
          method: 'GET'
        };
      } else if (rand < 0.85) {
        return {
          path: '/api/transactions',
          method: 'POST',
          body: txPayload
        };
      } else {
        return {
          path: `/api/transactions/account/${accountId}`,
          method: 'GET'
        };
      }
    }

    function sendWorkerRequest() {
      if (Date.now() >= endTime) {
        activeConnections--;
        if (activeConnections <= 0) {
          finish();
        }
        return;
      }

      const reqSpec = getRandomRequest();
      const reqStart = process.hrtime.bigint();

      const headers = {
        'Authorization': `Bearer ${token}`,
        'Accept': 'application/json'
      };
      if (reqSpec.body) {
        headers['Content-Type'] = 'application/json';
        headers['Content-Length'] = Buffer.byteLength(reqSpec.body);
      }

      const req = http.request({
        hostname: HOST,
        port: PORT,
        path: reqSpec.path,
        method: reqSpec.method,
        headers,
        agent,
        timeout: 8000
      }, (res) => {
        res.on('data', () => {});
        res.on('end', () => {
          const reqEnd = process.hrtime.bigint();
          const latencyMs = Number(reqEnd - reqStart) / 1e6;
          completedRequests++;
          latencies.push(latencyMs);

          if (res.statusCode >= 400 && res.statusCode !== 409) {
            failedRequests++;
          }
          // Worker sends next request
          sendWorkerRequest();
        });
      });

      req.on('error', (err) => {
        completedRequests++;
        failedRequests++;
        sendWorkerRequest();
      });

      req.on('timeout', () => {
        req.destroy();
      });

      if (reqSpec.body) {
        req.write(reqSpec.body);
      }
      req.end();
    }

    function finish() {
      agent.destroy();
      const actualDurationSec = (Date.now() - startTime) / 1000;
      latencies.sort((a, b) => a - b);

      const count = latencies.length;
      const sum = latencies.reduce((acc, v) => acc + v, 0);
      const avg = count > 0 ? sum / count : 0;
      const min = count > 0 ? latencies[0] : 0;
      const max = count > 0 ? latencies[count - 1] : 0;
      const p50 = count > 0 ? latencies[Math.floor(count * 0.50)] : 0;
      const p90 = count > 0 ? latencies[Math.floor(count * 0.90)] : 0;
      const p95 = count > 0 ? latencies[Math.floor(count * 0.95)] : 0;
      const p99 = count > 0 ? latencies[Math.floor(count * 0.99)] : 0;
      const rps = actualDurationSec > 0 ? completedRequests / actualDurationSec : 0;
      const errorRate = completedRequests > 0 ? (failedRequests / completedRequests) * 100 : 0;

      resolve({
        connections,
        durationSec: actualDurationSec,
        totalRequests: completedRequests,
        failedRequests,
        rps: Number(rps.toFixed(1)),
        avg: Number(avg.toFixed(2)),
        min: Number(min.toFixed(2)),
        p50: Number(p50.toFixed(2)),
        p90: Number(p90.toFixed(2)),
        p95: Number(p95.toFixed(2)),
        p99: Number(p99.toFixed(2)),
        max: Number(max.toFixed(2)),
        errorRate: Number(errorRate.toFixed(2))
      });
    }

    // Launch worker connections
    activeConnections = connections;
    for (let i = 0; i < connections; i++) {
      sendWorkerRequest();
    }
  });
}

async function main() {
  console.log('===============================================================');
  console.log('   FINANCE TRACKER API — LOAD & STRESS TESTING SUITE           ');
  console.log('   Verification of NFR-02 (< 500ms at 100 concurrent users)     ');
  console.log('===============================================================\n');

  const testData = await setupTestData();

  const results = {
    timestamp: new Date().toISOString(),
    environment: {
      os: process.platform,
      node: process.version,
      target: BASE_URL
    },
    runs: []
  };

  // Phase 1: Baseline Run (1 VU)
  console.log('\n--- [PHASE 1] Baseline Run (1 Virtual User, 15 seconds) ---');
  const baseline = await runLoad({
    connections: 1,
    durationSec: 15,
    token: testData.token,
    accountId: testData.accountId,
    categoryId: testData.categoryId
  });
  baseline.name = 'Baseline Run';
  console.log(`Baseline Results: RPS=${baseline.rps}, Avg=${baseline.avg}ms, p95=${baseline.p95}ms, Errors=${baseline.errorRate}%`);
  results.runs.push(baseline);

  // Phase 2: Target Load Run (100 VUs - NFR-02)
  console.log('\n--- [PHASE 2] Target Load Test (100 Virtual Users, 25 seconds) ---');
  const targetLoad = await runLoad({
    connections: 100,
    durationSec: 25,
    token: testData.token,
    accountId: testData.accountId,
    categoryId: testData.categoryId
  });
  targetLoad.name = 'Target Load Run (NFR-02)';
  const nfr02Passed = targetLoad.p95 <= 500 && targetLoad.errorRate < 1.0;
  targetLoad.nfr02Passed = nfr02Passed;
  console.log(`Target Load Results: RPS=${targetLoad.rps}, Avg=${targetLoad.avg}ms, p95=${targetLoad.p95}ms, Errors=${targetLoad.errorRate}%`);
  console.log(`NFR-02 Compliance (p95 <= 500ms): ${nfr02Passed ? 'PASSED [OK]' : 'FAILED'}`);
  results.runs.push(targetLoad);

  // Phase 3: Stress Curve (Scaling 50, 150, 200, 300, 400 VUs)
  const stressSteps = [25, 50, 75, 150, 200, 250, 300];
  console.log('\n--- [PHASE 3] Stress & Degradation Curve ---');
  for (const vu of stressSteps) {
    console.log(`Testing with ${vu} concurrent connections (10 sec)...`);
    const stepResult = await runLoad({
      connections: vu,
      durationSec: 10,
      token: testData.token,
      accountId: testData.accountId,
      categoryId: testData.categoryId
    });
    stepResult.name = `Stress Step ${vu} VUs`;
    console.log(`  -> VUs=${vu}: RPS=${stepResult.rps}, Avg=${stepResult.avg}ms, p95=${stepResult.p95}ms, Max=${stepResult.max}ms, Errors=${stepResult.errorRate}%`);
    results.runs.push(stepResult);
  }

  // Phase 4: Recovery Test (Drop to 5 VUs to verify post-stress recovery)
  console.log('\n--- [PHASE 4] Post-Stress Recovery Test (5 VUs, 10 seconds) ---');
  const recovery = await runLoad({
    connections: 5,
    durationSec: 10,
    token: testData.token,
    accountId: testData.accountId,
    categoryId: testData.categoryId
  });
  recovery.name = 'Post-Stress Recovery';
  console.log(`Recovery Results: RPS=${recovery.rps}, Avg=${recovery.avg}ms, p95=${recovery.p95}ms, Errors=${recovery.errorRate}%`);
  results.runs.push(recovery);

  // Save JSON
  const outPath = path.join(__dirname, '..', 'docs', 'load-test-results.json');
  fs.writeFileSync(outPath, JSON.stringify(results, null, 2), 'utf8');
  console.log(`\n[Results Saved] -> ${outPath}`);

  // Print Table
  console.log('\n========================================================================================');
  console.log('VU Count |   RPS   | Avg (ms) | p50 (ms) | p95 (ms) | p99 (ms) | Max (ms) | Errors (%)');
  console.log('---------+---------+----------+----------+----------+----------+----------+-----------');
  for (const r of results.runs) {
    console.log(
      `${String(r.connections).padStart(8)} | ` +
      `${String(r.rps).padStart(7)} | ` +
      `${String(r.avg).padStart(8)} | ` +
      `${String(r.p50).padStart(8)} | ` +
      `${String(r.p95).padStart(8)} | ` +
      `${String(r.p99).padStart(8)} | ` +
      `${String(r.max).padStart(8)} | ` +
      `${String(r.errorRate).padStart(9)}%`
    );
  }
  console.log('========================================================================================\n');
}

main().catch(console.error);
