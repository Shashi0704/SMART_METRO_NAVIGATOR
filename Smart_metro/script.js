// Cache DOM elements
const elements = {
    startStation: document.getElementById('start-station'),
    endStation: document.getElementById('end-station'),
    viaStation: document.getElementById('via-station'),
    startSuggestions: document.getElementById('start-suggestions'),
    endSuggestions: document.getElementById('end-suggestions'),
    viaSuggestions: document.getElementById('via-suggestions'),
    planJourneyBtn: document.getElementById('plan-journey'),
    results: document.getElementById('results'),
    routePath: document.getElementById('route-path'),
    totalDistance: document.getElementById('total-distance'),
    stationCount: document.getElementById('station-count'),
    journeyTime: document.getElementById('journey-time'),
    metroList: document.getElementById('metro-list'),
    statusList: document.getElementById('status-list'),
    loading: document.getElementById('loading')
};

// API endpoints
const API_BASE_URL = 'http://localhost:8080/api';
const ENDPOINTS = {
    stations: `${API_BASE_URL}/stations`,
    route: `${API_BASE_URL}/route`,
    status: `${API_BASE_URL}/status`
};

// Store station data
let stations = [];

// Fetch stations on page load
async function fetchStations() {
    try {
        const response = await fetch(ENDPOINTS.stations);
        stations = await response.json();
    } catch (error) {
        console.error('Error fetching stations:', error);
    }
}

// Handle station input and suggestions
function setupStationInput(input, suggestionsElement) {
    let currentFocus = -1;

    input.addEventListener('input', () => {
        const value = input.value.toUpperCase();
        suggestionsElement.innerHTML = '';
        suggestionsElement.style.display = 'none';

        if (!value) return;

        const matchingStations = stations.filter(station => 
            station.name.toUpperCase().includes(value)
        ).slice(0, 5);

        if (matchingStations.length > 0) {
            suggestionsElement.style.display = 'block';
            matchingStations.forEach(station => {
                const div = document.createElement('div');
                div.textContent = station.name;
                div.className = 'suggestion-item';
                div.addEventListener('click', () => {
                    input.value = station.name;
                    suggestionsElement.style.display = 'none';
                });
                suggestionsElement.appendChild(div);
            });
        }
    });

    // Handle keyboard navigation
    input.addEventListener('keydown', (e) => {
        const items = suggestionsElement.getElementsByClassName('suggestion-item');
        
        if (e.key === 'ArrowDown') {
            currentFocus++;
            addActive(items);
        } else if (e.key === 'ArrowUp') {
            currentFocus--;
            addActive(items);
        } else if (e.key === 'Enter') {
            e.preventDefault();
            if (currentFocus > -1) {
                if (items[currentFocus]) {
                    items[currentFocus].click();
                }
            }
        }
    });

    function addActive(items) {
        if (!items) return;
        removeActive(items);
        if (currentFocus >= items.length) currentFocus = 0;
        if (currentFocus < 0) currentFocus = items.length - 1;
        items[currentFocus].classList.add('active');
    }

    function removeActive(items) {
        for (let item of items) {
            item.classList.remove('active');
        }
    }

    // Close suggestions when clicking outside
    document.addEventListener('click', (e) => {
        if (e.target !== input) {
            suggestionsElement.style.display = 'none';
        }
    });
}

// Setup station inputs
function initializeStationInputs() {
    setupStationInput(elements.startStation, elements.startSuggestions);
    setupStationInput(elements.endStation, elements.endSuggestions);
    setupStationInput(elements.viaStation, elements.viaSuggestions);
}

// Display route on the UI
function displayRoute(route) {
    elements.routePath.innerHTML = '';
    
    route.stations.forEach((station, index) => {
        // Add station node
        const stationNode = document.createElement('div');
        stationNode.className = 'station-node';
        stationNode.style.borderLeft = `4px solid ${station.color}`;
        stationNode.textContent = station.name;
        
        elements.routePath.appendChild(stationNode);

        // Add arrow if not last station
        if (index < route.stations.length - 1) {
            const arrow = document.createElement('div');
            arrow.className = 'route-arrow';
            arrow.textContent = '→';
            elements.routePath.appendChild(arrow);
        }
    });

    // Update journey details
    elements.totalDistance.textContent = `${route.distance.toFixed(2)} km`;
    elements.stationCount.textContent = route.stations.length - 1;
    elements.journeyTime.textContent = `${route.estimatedTime} min`;

    // Display metro options
    elements.metroList.innerHTML = '';
    route.metros.forEach(metro => {
        const metroCard = document.createElement('div');
        metroCard.className = `metro-card ${metro.recommended ? 'recommended' : ''}`;
        metroCard.innerHTML = `
            <div class="metro-info">
                <h4>Metro ${metro.id}</h4>
                <p>Time: ${metro.time} min</p>
            </div>
            <div class="metro-status">
                ${metro.crowded ? '<span class="crowd-badge">Crowded</span>' : ''}
                ${metro.delay ? `<span class="delay-badge">+${metro.delay} min delay</span>` : ''}
            </div>
        `;
        elements.metroList.appendChild(metroCard);
    });

    // Display status updates
    elements.statusList.innerHTML = '';
    route.statusUpdates.forEach(status => {
        const statusItem = document.createElement('div');
        statusItem.className = 'status-item';
        statusItem.innerHTML = `
            <strong>${status.station}</strong>
            <p>${status.message}</p>
        `;
        elements.statusList.appendChild(statusItem);
    });

    elements.results.classList.remove('hidden');
}

// Plan journey
async function planJourney() {
    const startStation = elements.startStation.value;
    const endStation = elements.endStation.value;
    const viaStation = elements.viaStation.value;

    if (!startStation || !endStation) {
        alert('Please enter both start and destination stations');
        return;
    }

    elements.loading.classList.remove('hidden');

    try {
        const response = await fetch(ENDPOINTS.route, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                start: startStation,
                end: endStation,
                via: viaStation || null
            })
        });

        const data = await response.json();
        if (response.ok) {
            displayRoute(data);
        } else {
            alert(data.message || 'Error planning journey');
        }
    } catch (error) {
        console.error('Error planning journey:', error);
        alert('Error planning journey. Please try again.');
    } finally {
        elements.loading.classList.add('hidden');
    }
}

// Initialize the application
async function init() {
    await fetchStations();
    initializeStationInputs();
    elements.planJourneyBtn.addEventListener('click', planJourney);
}

// Start the application
init();