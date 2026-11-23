// Corporation Client API - Vanilla JS replacement for Angular CorporationService
// All endpoints under /api/v1/corporate/office/{id}

const CorporationClient = (function() {
  const API_BASE = '/api/v1/corporate/office';
  const AI_BASE = '/api/v1/corporate/ai';
  const CREW_BASE = '/api/v1/corporate/crew';
  const UNIFIED_BASE = '/api/v1/unified/characters';

  // ---- State Management ----

  function getCorporationId() {
    // Check URL params first
    const urlParams = new URLSearchParams(window.location.search);
    const urlCorpId = urlParams.get('corporationId');
    if (urlCorpId) {
      const id = parseInt(urlCorpId, 10);
      if (!isNaN(id)) {
        localStorage.setItem('corporationId', id.toString());
        return id;
      }
    }
    // Check localStorage
    const stored = localStorage.getItem('corporationId');
    if (stored) return parseInt(stored, 10);
    // Default
    return 1;
  }

  function getCharacterId() {
    const urlParams = new URLSearchParams(window.location.search);
    const urlCharId = urlParams.get('characterId');
    if (urlCharId) {
      const id = parseInt(urlCharId, 10);
      if (!isNaN(id)) {
        localStorage.setItem('currentCharacterId', id.toString());
        return id;
      }
    }
    let stored = localStorage.getItem('currentCharacterId');
    if (!stored) stored = localStorage.getItem('selected_char_id');
    if (stored) return parseInt(stored, 10);
    return 1;
  }

  function setCorporationId(id) {
    localStorage.setItem('corporationId', id.toString());
  }

  function setCharacterId(id) {
    localStorage.setItem('currentCharacterId', id.toString());
  }

  // ---- HTTP Helper ----

  async function request(path, options = {}) {
    const apiUrl = window.apiUrl || window.location.origin;
    const token = localStorage.getItem('doramcu_auth_token');
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    const response = await fetch(`${apiUrl}${path}`, {
      ...options,
      headers,
    });
    if (!response.ok) {
      const errorBody = await response.text().catch(() => '');
      throw new Error(`HTTP ${response.status}: ${errorBody || response.statusText}`);
    }
    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
      return await response.json();
    }
    return await response.text();
  }

  // ---- Office State ----

  async function getOfficeState() {
    const corpId = getCorporationId();
    return request(`${API_BASE}/${corpId}`);
  }

  // ---- Contracts ----

  async function acceptContract(contractId) {
    const corpId = getCorporationId();
    return request(`${API_BASE}/${corpId}/contracts/${contractId}/accept`, { method: 'POST' });
  }

  async function processContractTurn(contractId) {
    return request(`${API_BASE}/contracts/${contractId}/process-turn`, { method: 'POST' });
  }

  async function getContractStatus(contractId) {
    return request(`${API_BASE}/contracts/${contractId}/status`);
  }

  async function getContractCrew(contractId) {
    return request(`${API_BASE}/contracts/${contractId}/crew`);
  }

  async function generateContracts() {
    const corpId = getCorporationId();
    return request(`${API_BASE}/${corpId}/contracts/generate`, { method: 'POST' });
  }

  async function completeContract(contractId) {
    return request(`${API_BASE}/contracts/${contractId}/complete`, { method: 'POST' });
  }

  // ---- Crew / Hiring ----

  async function hireAiCharacter(aiCharacterId, position, dailySalary) {
    const corpId = getCorporationId();
    return request(`${API_BASE}/${corpId}/hiring`, {
      method: 'POST',
      body: JSON.stringify({ aiCharacterId, position, dailySalary }),
    });
  }

  async function fireAiCharacter(hiringId) {
    const corpId = getCorporationId();
    return request(`${API_BASE}/${corpId}/hiring/${hiringId}`, { method: 'DELETE' });
  }

  async function getCrewCareer(hiringId) {
    return request(`${CREW_BASE}/${hiringId}/career`);
  }

  async function promoteCrew(hiringId) {
    return request(`${CREW_BASE}/${hiringId}/promote`, { method: 'POST' });
  }

  async function demoteCrew(hiringId) {
    return request(`${CREW_BASE}/${hiringId}/demote`, { method: 'POST' });
  }

  async function getAvailableCharacters() {
    const corpId = getCorporationId();
    return request(`${API_BASE}/${corpId}/hiring`);
  }

  // ---- Finance ----

  async function getFinancialReport() {
    const corpId = getCorporationId();
    return request(`${API_BASE}/${corpId}/finance`);
  }

  async function getTransactions(page = 0, size = 50) {
    const corpId = getCorporationId();
    return request(`${API_BASE}/${corpId}/finance/transactions?page=${page}&size=${size}`);
  }

  // ---- AI Assistant ----

  async function triggerAiTurn() {
    const corpId = getCorporationId();
    return request(`${AI_BASE}/turn`, {
      method: 'POST',
      body: JSON.stringify({ playerId: corpId }),
    });
  }

  async function getAiActions(limit = 20) {
    const corpId = getCorporationId();
    return request(`${AI_BASE}/actions?playerId=${corpId}&limit=${limit}`);
  }

  async function getAiStatus() {
    const corpId = getCorporationId();
    return request(`${AI_BASE}/status?playerId=${corpId}`);
  }

  // ---- Role Switching ----

  async function switchRole(role) {
    const charId = getCharacterId();
    return request(`${UNIFIED_BASE}/${charId}/role/switch`, {
      method: 'POST',
      body: JSON.stringify({ role }),
    });
  }

  // ---- Character Integration ----

  async function getCharacterStats(characterId) {
    return request(`${API_BASE}/character/${characterId}/stats`);
  }

  async function getCharacterAbilities(characterId) {
    return request(`${API_BASE}/character/${characterId}/abilities`);
  }

  async function getCharacterSkills(characterId) {
    return request(`${API_BASE}/character/${characterId}/skills`);
  }

  async function getCharacterCareer(characterId) {
    return request(`${API_BASE}/character/${characterId}/career`);
  }

  async function getCharacterAssignments(characterId) {
    return request(`${API_BASE}/character/${characterId}/assignments`);
  }

  async function getQualifiedContracts(characterId) {
    return request(`${API_BASE}/character/qualified?characterId=${characterId}`);
  }

  async function assignCharacterToContract(characterId, contractId) {
    return request(`${API_BASE}/character/${characterId}/assign?contractId=${contractId}`, {
      method: 'POST',
    });
  }

  // ---- Building ----

  async function buildOffice(buildingType) {
    const corpId = getCorporationId();
    return request(`${API_BASE}/${corpId}/building`, {
      method: 'POST',
      body: JSON.stringify({ buildingType }),
    });
  }

  // ---- SSE Events ----

  function subscribeToEvents(callback) {
    const apiUrl = window.apiUrl || window.location.origin;
    const eventSource = new EventSource(`${apiUrl}/api/v1/game/events?corporation=true`);

    eventSource.onmessage = (event) => {
      try {
        const corpEvent = JSON.parse(event.data);
        callback(corpEvent);

        // Auto-refresh on important events
        if (corpEvent.eventType === 'CORPORATION_CONTRACT_COMPLETED' ||
            corpEvent.eventType === 'CORPORATION_EMERGENCY') {
          if (window.corporationRefresh) {
            window.corporationRefresh();
          }
        }
      } catch (e) {
        console.error('Failed to parse corporation SSE event', e);
      }
    };

    eventSource.onerror = () => {
      console.warn('Corporation SSE connection lost, reconnecting...');
      eventSource.close();
      setTimeout(() => subscribeToEvents(callback), 5000);
    };

    return eventSource;
  }

  // ---- Public API ----
  return {
    getCorporationId,
    getCharacterId,
    setCorporationId,
    setCharacterId,
    getOfficeState,
    acceptContract,
    processContractTurn,
    getContractStatus,
    getContractCrew,
    generateContracts,
    completeContract,
    hireAiCharacter,
    fireAiCharacter,
    getCrewCareer,
    promoteCrew,
    demoteCrew,
    getAvailableCharacters,
    getFinancialReport,
    getTransactions,
    triggerAiTurn,
    getAiActions,
    getAiStatus,
    switchRole,
    getCharacterStats,
    getCharacterAbilities,
    getCharacterSkills,
    getCharacterCareer,
    getCharacterAssignments,
    getQualifiedContracts,
    assignCharacterToContract,
    buildOffice,
    subscribeToEvents,
  };
})();
