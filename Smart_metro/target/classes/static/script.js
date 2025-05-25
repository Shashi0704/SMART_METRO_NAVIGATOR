// Constants and State Management
const API_ENDPOINTS = {
    STATIONS: '/api/stations',
    ROUTE: '/api/route'
};

const STATE = {
    stations: [],
    isLoading: false
};

// DOM Elements
const DOM = {
    startStation: document.getElementById('startStation'),
    endStation: document.getElementById('endStation'),
    viaStation: document.getElementById('viaStation'),
    findPathButton: document.getElementById('findPathButton'),
    routeResult: document.getElementById('routeResult'),
    loadingIndicator: document.getElementById('loadingIndicator')
};

// Event Listeners
document.addEventListener('DOMContentLoaded', initializeApp);
DOM.findPathButton.addEventListener('click', handleFindPath);

// Station dropdowns change handlers
DOM.startStation.addEventListener('change', validateStations);
DOM.endStation.addEventListener('change', validateStations);

/**
 * Initialize the application
 */
async function initializeApp() {
    try {
        showLoading(true);
        await loadStations();
        populateStationDropdowns();
    } catch (error) {
        handleError('Failed to initialize application', error);
    } finally {
        showLoading(false);
    }
}

/**
 * Load stations from the API
 */
async function loadStations() {
    try {
        const response = await fetch(API_ENDPOINTS.STATIONS);
        if (!response.ok) throw new Error('Failed to fetch stations');
        STATE.stations = await response.json();
    } catch (error) {
        throw new Error('Failed to load stations. Please refresh the page.');
    }
}

/**
 * Populate station dropdowns with options
 */
function populateStationDropdowns() {
    const dropdowns = [
        { element: DOM.startStation, label: 'start' },
        { element: DOM.endStation, label: 'end' },
        { element: DOM.viaStation, label: 'via', optional: true }
    ];

    dropdowns.forEach(({ element, label, optional }) => {
        // Clear existing options
        element.innerHTML = `<option value="">Select ${label} station${optional ? ' (optional)' : ''}</option>`;
        
        // Add station options
        STATE.stations.forEach(station => {
            const option = document.createElement('option');
            option.value = station.name;
            option.textContent = station.name;
            element.appendChild(option);
        });
    });
}

/**
 * Validate station selections
 */
function validateStations() {
    const start = DOM.startStation.value;
    const end = DOM.endStation.value;
    
    if (start && end && start === end) {
        showError('Start and end stations cannot be the same');
        DOM.findPathButton.disabled = true;
        return false;
    }
    
    DOM.findPathButton.disabled = !start || !end;
    return true;
}

/**
 * Handle find path button click
 */
async function handleFindPath() {
    const startStation = DOM.startStation.value;
    const endStation = DOM.endStation.value;
    const viaStation = DOM.viaStation.value;

    if (!validateStations()) return;

    try {
        showLoading(true);
        const route = await findRoute(startStation, endStation, viaStation);
        displayRoute(route);
    } catch (error) {
        handleError('Failed to find route', error);
    } finally {
        showLoading(false);
    }
}

/**
 * Find route from the API
 */
async function findRoute(start, end, via) {
    try {
        const response = await fetch(API_ENDPOINTS.ROUTE, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ start, end, via: via || null })
        });

        if (!response.ok) throw new Error('Failed to find route');
        return await response.json();
    } catch (error) {
        throw new Error('Failed to find route. Please try again.');
    }
}

/**
 * Display the route results
 */
function displayRoute(route) {
    const html = `
        <div class="card">
            <div class="card-body">
                <div class="route-summary">
                    <div class="summary-item">
                        <i class="fas fa-road"></i>
                        <h5>Distance</h5>
                        <p>${route.distance.toFixed(2)} km</p>
                    </div>
                    <div class="summary-item">
                        <i class="fas fa-clock"></i>
                        <h5>Time</h5>
                        <p>${route.estimatedTime} min</p>
                    </div>
                    <div class="summary-item">
                        <i class="fas fa-train"></i>
                        <h5>Stations</h5>
                        <p>${route.stations.length}</p>
                    </div>
                </div>
                
                <div class="route-details">
                    <h5 class="card-title">
                        <i class="fas fa-map-signs"></i> Route Details
                    </h5>
                    <div class="station-list">
                        ${route.stations.map((station, index) => `
                            <div class="station-item" style="border-left-color: ${station.color}">
                                <div class="station-number">${index + 1}</div>
                                <div class="station-name">${station.name}</div>
                            </div>
                        `).join('')}
                    </div>
                </div>
                
                <div class="metro-options">
                    <h5 class="section-title">
                        <i class="fas fa-subway"></i> Metro Options
                    </h5>
                    ${route.metros.map(metro => `
                        <div class="metro-info ${metro.recommended ? 'recommended' : ''}">
                            <div class="metro-header">
                                <h6>Metro ${metro.id}</h6>
                                ${metro.recommended ? '<span class="recommended-badge">Recommended</span>' : ''}
                            </div>
                            <div class="metro-details">
                                <div class="detail-item">
                                    <i class="fas fa-clock"></i>
                                    <span>Time: ${metro.time} min</span>
                                </div>
                                <div class="detail-item">
                                    <i class="fas fa-exclamation-triangle"></i>
                                    <span>Delay: ${metro.delay} min</span>
                                </div>
                                <div class="detail-item">
                                    <i class="fas fa-users"></i>
                                    <span>Crowded: ${metro.crowded ? 'Yes' : 'No'}</span>
                                </div>
                            </div>
                        </div>
                    `).join('')}
                </div>
                
                ${route.statusUpdates.length > 0 ? `
                    <div class="status-updates">
                        <h5 class="section-title">
                            <i class="fas fa-info-circle"></i> Status Updates
                        </h5>
                        ${route.statusUpdates.map(update => `
                            <div class="status-update">
                                <i class="fas fa-bell"></i>
                                <span>${update.message}</span>
                            </div>
                        `).join('')}
                    </div>
                ` : ''}
            </div>
        </div>
    `;
    
    DOM.routeResult.innerHTML = html;
}

/**
 * Show/hide loading indicator
 */
function showLoading(show) {
    DOM.loadingIndicator.classList.toggle('d-none', !show);
    DOM.findPathButton.disabled = show;
}

/**
 * Handle and display errors
 */
function handleError(message, error) {
    console.error(message, error);
    showError(error.message || message);
}

/**
 * Display error message
 */
function showError(message) {
    DOM.routeResult.innerHTML = `
        <div class="error-message">
            <i class="fas fa-exclamation-circle"></i>
            <span>${message}</span>
        </div>
    `;
} 