function showView(viewId) {
    // Hide all views
    const views = document.querySelectorAll('.content-body > div');
    views.forEach(view => view.classList.add('hidden'));

    // Show the selected view
    const viewToShow = document.getElementById(viewId + 'View');
    if (viewToShow) {
        viewToShow.classList.remove('hidden');
    }

    // Update the page title
    const contentTitle = document.getElementById('contentTitle');
    contentTitle.innerText = viewId
        .replace(/([A-Z])/g, ' $1')
        .replace(/^./, str => str.toUpperCase());

    // Highlight the active sidebar nav link
    const navLinks = document.querySelectorAll('.nav-menu .nav-link');
    navLinks.forEach(link => link.classList.remove('active'));

    // Match the link with correct onclick call
    const activeLink = Array.from(navLinks).find(link => link.getAttribute('onclick')?.includes(`showView('${viewId}')`));
    if (activeLink) {
        activeLink.classList.add('active');
    }
}


function filterLiveData(filter) {
    const rows = document.querySelectorAll('#liveDataView table tbody tr');
    rows.forEach(row => {
        row.style.display = 'none';

        const isConnected = row.classList.contains('connected');
        const isDisconnected = row.classList.contains('disconnected');
        const isOnline = row.classList.contains('online');
        const isOffline = row.classList.contains('offline');

        if (filter === 'all' ||
            (filter === 'connected' && isConnected) ||
            (filter === 'disconnected' && isDisconnected) ||
            (filter === 'online' && isOnline) ||
            (filter === 'offline' && isOffline)) {
            row.style.display = '';
        }
    });

    updateActiveTab('#liveDataView', filter);
}

function filterRechargeData(filter) {
    const rows = document.querySelectorAll('#rechargeView table tbody tr');
    rows.forEach(row => {
        row.style.display = 'none';
        if (filter === 'all' || row.classList.contains(filter)) {
            row.style.display = '';
        }
    });

    updateActiveTab('#rechargeView', filter);
}

function updateActiveTab(viewSelector, filter) {
    const tabs = document.querySelectorAll(`${viewSelector} .filter-tab`);
    tabs.forEach(tab => {
        tab.classList.remove('active');
        if (tab.innerText.toLowerCase().includes(filter)) {
            tab.classList.add('active');
        }
    });
}

// Optional: Submenu toggle for "Add New"
function toggleSubmenu(id) {
    const submenu = document.getElementById(id);
    if (submenu) submenu.classList.toggle('show');
}

// Dummy handlers
function viewMeterDetails(id) {
    alert("Viewing meter details for ID: " + id);
}

function viewTransactionDetails(txnId) {
    alert("Viewing transaction: " + txnId);
}

function retryTransaction(txnId) {
    alert("Retrying transaction: " + txnId);
}

function processRefund(txnId) {
    alert("Processing refund for transaction: " + txnId);
}

function logout() {
    alert("Logging out...");
}


// Modal and Data Management Variables
let currentMeterId = null;
let currentTimeRange = '3months';
let currentView = 'infinite';
let currentPage = 1;
let totalPages = 1;
let itemsPerPage = 50;
let allMeterData = [];
let displayedDataCount = 0;
let isLoading = false;

// Update the existing viewMeterDetails function
function viewMeterDetails(meterId) {
    currentMeterId = meterId;
    document.getElementById('modalMeterId').textContent = meterId;
    document.getElementById('meterDetailsModal').style.display = 'block';
    
    // Reset view state
    currentPage = 1;
    displayedDataCount = 0;
    document.getElementById('meterDataBody').innerHTML = '';
    
    // Load initial data
    loadMeterData();
}

// Close modal
function closeMeterModal() {
    document.getElementById('meterDetailsModal').style.display = 'none';
    currentMeterId = null;
}

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = document.getElementById('meterDetailsModal');
    if (event.target === modal) {
        closeMeterModal();
    }
}

// Change time range
function changeTimeRange(range) {
    currentTimeRange = range;
    
    // Update active button
    document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');
    
    // Reset and reload data
    currentPage = 1;
    displayedDataCount = 0;
    document.getElementById('meterDataBody').innerHTML = '';
    loadMeterData();
}

// Toggle view mode
function toggleView(view) {
    currentView = view;
    
    // Update active button
    document.querySelectorAll('.toggle-btn').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');
    
    // Show/hide appropriate controls
    const paginationControls = document.getElementById('paginationControls');
    const tableContainer = document.querySelector('.modal-table-container');
    
    if (view === 'pagination') {
        paginationControls.classList.remove('hidden');
        tableContainer.style.maxHeight = '400px';
        // Remove scroll event listener
        tableContainer.removeEventListener('scroll', handleInfiniteScroll);
    } else {
        paginationControls.classList.add('hidden');
        tableContainer.style.maxHeight = '500px';
        // Add scroll event listener
        tableContainer.addEventListener('scroll', handleInfiniteScroll);
    }
    
    // Reset and reload data
    currentPage = 1;
    displayedDataCount = 0;
    document.getElementById('meterDataBody').innerHTML = '';
    loadMeterData();
}

// Generate sample meter data
function generateMeterData(meterId, months) {
    const data = [];
    const endDate = new Date();
    const startDate = new Date();
    startDate.setMonth(startDate.getMonth() - months);
    
    let currentDate = new Date(startDate);
    let cumulativeKwh = 100 + Math.random() * 50;
    
    while (currentDate <= endDate) {
        // Generate 24 readings per day (hourly)
        for (let hour = 0; hour < 24; hour++) {
            const readingTime = new Date(currentDate);
            readingTime.setHours(hour, Math.floor(Math.random() * 60), 0);
            
            cumulativeKwh += Math.random() * 2;
            
            data.push({
                datetime: readingTime.toLocaleString('en-IN'),
                cumKwh: cumulativeKwh.toFixed(2),
                voltage: (230 + Math.random() * 20).toFixed(2),
                current: (5 + Math.random() * 10).toFixed(2),
                power: (800 + Math.random() * 800).toFixed(0),
                relay: Math.random() > 0.1 ? 'ON' : 'OFF',
                connection: Math.random() > 0.05 ? 'ONLINE' : 'OFFLINE'
            });
        }
        currentDate.setDate(currentDate.getDate() + 1);
    }
    
    return data.reverse(); // Latest first
}

// Load meter data
function loadMeterData() {
    if (isLoading) return;
    isLoading = true;
    
    // Show loading indicator for infinite scroll
    if (currentView === 'infinite') {
        document.getElementById('loadingIndicator').classList.remove('hidden');
    }
    
    // Simulate API call delay
    setTimeout(() => {
        const months = currentTimeRange === '3months' ? 3 : 6;
        allMeterData = generateMeterData(currentMeterId, months);
        
        if (currentView === 'infinite') {
            loadMoreData();
        } else {
            setupPagination();
            loadPageData();
        }
        
        isLoading = false;
        document.getElementById('loadingIndicator').classList.add('hidden');
    }, 1000);
}

// Load more data for infinite scroll
function loadMoreData() {
    const startIndex = displayedDataCount;
    const endIndex = Math.min(startIndex + itemsPerPage, allMeterData.length);
    
    const tbody = document.getElementById('meterDataBody');
    
    for (let i = startIndex; i < endIndex; i++) {
        const row = allMeterData[i];
        const tr = document.createElement('tr');
        
        tr.innerHTML = `
            <td>${row.datetime}</td>
            <td>${row.cumKwh}</td>
            <td>${row.voltage}</td>
            <td>${row.current}</td>
            <td>${row.power}</td>
            <td><span class="status-badge ${row.relay === 'ON' ? 'status-on' : 'status-off'}">${row.relay}</span></td>
            <td><span class="status-badge ${row.connection === 'ONLINE' ? 'status-online' : 'status-offline'}">${row.connection}</span></td>
        `;
        
        tbody.appendChild(tr);
    }
    
    displayedDataCount = endIndex;
}

// Handle infinite scroll
function handleInfiniteScroll() {
    const container = document.querySelector('.modal-table-container');
    
    if (container.scrollTop + container.clientHeight >= container.scrollHeight - 10) {
        if (displayedDataCount < allMeterData.length && !isLoading) {
            isLoading = true;
            document.getElementById('loadingIndicator').classList.remove('hidden');
            
            setTimeout(() => {
                loadMoreData();
                isLoading = false;
                document.getElementById('loadingIndicator').classList.add('hidden');
            }, 500);
        }
    }
}

// Setup pagination
function setupPagination() {
    totalPages = Math.ceil(allMeterData.length / itemsPerPage);
    document.getElementById('totalPages').textContent = totalPages;
    updatePaginationButtons();
}

// Load page data for pagination
function loadPageData() {
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = Math.min(startIndex + itemsPerPage, allMeterData.length);
    
    const tbody = document.getElementById('meterDataBody');
    tbody.innerHTML = '';
    
    for (let i = startIndex; i < endIndex; i++) {
        const row = allMeterData[i];
        const tr = document.createElement('tr');
        
        tr.innerHTML = `
            <td>${row.datetime}</td>
            <td>${row.cumKwh}</td>
            <td>${row.voltage}</td>
            <td>${row.current}</td>
            <td>${row.power}</td>
            <td><span class="status-badge ${row.relay === 'ON' ? 'status-on' : 'status-off'}">${row.relay}</span></td>
            <td><span class="status-badge ${row.connection === 'ONLINE' ? 'status-online' : 'status-offline'}">${row.connection}</span></td>
        `;
        
        tbody.appendChild(tr);
    }
    
    document.getElementById('currentPage').textContent = currentPage;
    updatePaginationButtons();
}

// Navigate pages
function goToPage(direction) {
    switch(direction) {
        case 'first':
            currentPage = 1;
            break;
        case 'prev':
            if (currentPage > 1) currentPage--;
            break;
        case 'next':
            if (currentPage < totalPages) currentPage++;
            break;
        case 'last':
            currentPage = totalPages;
            break;
    }
    
    loadPageData();
}

// Update pagination button states
function updatePaginationButtons() {
    document.getElementById('firstBtn').disabled = currentPage === 1;
    document.getElementById('prevBtn').disabled = currentPage === 1;
    document.getElementById('nextBtn').disabled = currentPage === totalPages;
    document.getElementById('lastBtn').disabled = currentPage === totalPages;
}

// Initialize infinite scroll listener
document.addEventListener('DOMContentLoaded', function() {
    const tableContainer = document.querySelector('.modal-table-container');
    if (tableContainer) {
        tableContainer.addEventListener('scroll', handleInfiniteScroll);
    }
});
