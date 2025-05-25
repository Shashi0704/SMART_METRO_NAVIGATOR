// API endpoints
const API_BASE = '/smart-metro/api';
const API = {
    STATIONS: `${API_BASE}/stations`,
    ROUTE: `${API_BASE}/route`,
    STATUS: `${API_BASE}/status`
};

// DOM Elements
const startInput = document.getElementById('start-station');
const endInput = document.getElementById('end-station');
const viaInput = document.getElementById('via-station');
const planButton = document.getElementById('plan-journey');
const resultsDiv = document.getElementById('results');
const loadingDiv = document.getElementById('loading');
const routePath = document.getElementById('route-path');
const totalDistance = document.getElementById('total-distance');
const stationCount = document.getElementById('station-count');
const journeyTime = document.getElementById('journey-time');
const metroList = document.getElementById('metro-list');
const statusList = document.getElementById('status-list');

// Station suggestions
let stations = [];

// Load stations on page load
fetch(API.STATIONS)
    .then(response => response.json())
    .then(data => {
        stations = data;
        setupAutocomplete();
    })
    .catch(error => console.error('Error loading stations:', error));

// Setup autocomplete for station inputs
function setupAutocomplete() {
    [startInput, endInput, viaInput].forEach(input => {
        input.addEventListener('input', (e) => {
            const value = e.target.value.toLowerCase();
            const suggestions = stations
                .filter(station => station.name.toLowerCase().includes(value))
                .slice(0, 5);
            
            showSuggestions(input.id, suggestions);
        });
    });
}

function showSuggestions(inputId, suggestions) {
    const suggestionsDiv = document.getElementById(`${inputId}-suggestions`);
    suggestionsDiv.innerHTML = '';
    
    suggestions.forEach(station => {
        const div = document.createElement('div');
        div.textContent = station.name;
        div.addEventListener('click', () => {
            document.getElementById(inputId).value = station.name;
            suggestionsDiv.innerHTML = '';
        });
        suggestionsDiv.appendChild(div);
    });
}

// Plan journey
planButton.addEventListener('click', async () => {
    const startStation = startInput.value;
    const endStation = endInput.value;
    const viaStation = viaInput.value;

    if (!startStation || !endStation) {
        alert('Please enter both start and end stations');
        return;
    }

    loadingDiv.classList.remove('hidden');
    resultsDiv.classList.add('hidden');

    try {
        const params = new URLSearchParams({
            startStation,
            endStation,
            ...(viaStation && { viaStation })
        });

        const response = await fetch(`${API.ROUTE}?${params}`);
        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Failed to plan journey');
        }

        displayResults(data);
    } catch (error) {
        alert(error.message || 'An error occurred while planning your journey');
    } finally {
        loadingDiv.classList.add('hidden');
    }
});

function displayResults(data) {
    // Display route
    routePath.innerHTML = data.stations
        .map(station => `<div class="station-node">${station.name}</div>`)
        .join('<div class="connection-line"></div>');

    // Display journey details
    totalDistance.textContent = `${data.distance.toFixed(1)} km`;
    stationCount.textContent = data.stations.length;
    journeyTime.textContent = `${data.estimatedTime} min`;

    // Display metro options
    metroList.innerHTML = data.metros
        .map(metro => `
            <div class="metro-card ${metro.recommended ? 'recommended' : ''}">
                <h4>Metro ${metro.id}</h4>
                <p>Time: ${metro.time} min</p>
                <p>Delay: ${metro.delay} min</p>
                ${metro.crowded ? '<p class="crowded">Crowded</p>' : ''}
                ${metro.recommended ? '<span class="recommended-badge">Recommended</span>' : ''}
            </div>
        `)
        .join('');

    // Display status updates
    statusList.innerHTML = data.statusUpdates
        .map(update => `
            <div class="status-update">
                <p class="station-name">${update.station}</p>
                <p class="update-message">${update.message}</p>
            </div>
        `)
        .join('');

    resultsDiv.classList.remove('hidden');
} 