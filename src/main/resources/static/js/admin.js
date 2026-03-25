document.addEventListener("DOMContentLoaded", function () {
   loadMeterReadings();  
   setInterval(loadMeterReadings, 15000);
});

function loadDashboardData(){
  fetch("/api/meter/dashboard")
    .then(response => response.json())
    .then(data => {
      document.getElementById("totalPg").innerHTML = `Total PGs: ${data.totalPG}`;
      document.getElementById("totalOwners").innerHTML = `Total Owners: ${data.totalOwners}`;
      document.getElementById("totalTenants").innerHTML = `Total Tenants: ${data.totalTenants}`;
      document.getElementById("totalMeters").innerHTML = `Total Meters: ${data.totalMeters}`;
      document.getElementById("totalRooms").innerHTML = `Total Rooms: ${data.totalRooms}`;
    })
    .catch(error => console.error("Error:", error));
}
loadDashboardData();

function showView(viewId) {
  // Hide all views
  const views = document.querySelectorAll(".content-body > div");
  views.forEach((view) => view.classList.add("hidden"));

  // Show the selected view
  const viewToShow = document.getElementById(viewId + "View");
  if (viewToShow) {
    viewToShow.classList.remove("hidden");
  }

  // Update the page title
  const contentTitle = document.getElementById("contentTitle");
  const view_Id = viewId
    .replace(/([a-z])([A-Z])/g, "$1 $2")
    .replace(/([A-Z])([A-Z][a-z])/g, "$1 $2")
    .replace(/^./, (str) => str.toUpperCase());
  if (viewId === "AddPG") contentTitle.innerText = "Add PG";
  else contentTitle.innerText = view_Id;

  // Highlight the active sidebar nav link
  const navLinks = document.querySelectorAll(".nav-menu .nav-link");
  navLinks.forEach((link) => link.classList.remove("active"));

  // Match the link with correct onclick call
  const activeLink = Array.from(navLinks).find((link) =>
    link.getAttribute("onclick")?.includes(`showView('${viewId}')`)
  );
  if (activeLink) {
    activeLink.classList.add("active");
  }
}

function filterLiveData(filter) {
  const rows = document.querySelectorAll("#liveDataView table tbody tr");
  console.log(rows);
  rows.forEach((row) => {
    row.style.display = "none";

    const isConnected = row.classList.contains("connected");
    const isDisconnected = row.classList.contains("disconnected");
    const isOnline = row.classList.contains("online");
    const isOffline = row.classList.contains("offline");

    if (
      filter === "all" ||
      (filter === "connected" && isConnected) ||
      (filter === "disconnected" && isDisconnected) ||
      (filter === "online" && isOnline) ||
      (filter === "offline" && isOffline)
    ) {
      row.style.display = "";
    }
  });

  updateActiveTab("#liveDataView", filter);
}

function filterRechargeData(filter) {
  const rows = document.querySelectorAll("#rechargeView table tbody tr");
  rows.forEach((row) => {
    row.style.display = "none";
    if (filter === "all" || row.classList.contains(filter)) {
      row.style.display = "";
    }
  }); 

  updateActiveTab("#rechargeView", filter);
} 

function updateActiveTab(viewSelector, filter) {
  const tabs = document.querySelectorAll(`${viewSelector} .filter-tab`);
  tabs.forEach((tab) => {
    tab.classList.remove("active");
    if (tab.innerText.toLowerCase().replace(" meters","") === filter) {
      tab.classList.add("active");
    }
  });
}

// Optional: Submenu toggle for "Add New"
function toggleSubmenu(id) {
  const submenu = document.getElementById(id);
  if (submenu) submenu.classList.toggle("show");
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
  window.location.href = '/'
}

// Modal and Data Management Variables
let currentMeterId = null;
let currentTimeRange = "3months";
let currentView = "infinite";
let currentPage = 1;
let totalPages = 1;
let itemsPerPage = 50;
let allMeterData = [];
let displayedDataCount = 0;
let isLoading = false;

// Update the existing viewMeterDetails function
async function viewMeterDetails(meterId) {
  await loadOneMeterData(meterId);
  currentMeterId = meterId;
  document.getElementById("modalMeterId").textContent = meterId;
  document.getElementById("meterDetailsModal").style.display = "block";

  // Reset view state
  currentPage = 1;
  displayedDataCount = 0;
  //document.getElementById("meterDataBody").innerHTML = "";

  // Load initial data
  loadMeterData();
}

function makeFirstFilterBtnActive(){
  document.querySelectorAll(".filter-btn").forEach(btn => {
      btn.classList.remove("active");
    });

  const allBtn = document.querySelector('.filter-btn[data-filter="all"]');
  allBtn.classList.add("active");
}

// Close modal
function closeMeterModal() {
  document.getElementById("meterDetailsModal").style.display = "none";
  currentMeterId = null;

    makeFirstFilterBtnActive();
}

// Close modal when clicking outside
window.onclick = function (event) {
  const modal = document.getElementById("meterDetailsModal");
  if (event.target === modal) {
    closeMeterModal();
  }
};

// Change time range
// function changeTimeRange(range) {
//   currentTimeRange = range;

//   // Update active button
//   document
//     .querySelectorAll(".filter-btn")
//     .forEach((btn) => btn.classList.remove("active"));
//   event.target.classList.add("active");

//   // Reset and reload data
//   currentPage = 1;
//   displayedDataCount = 0;
//   //document.getElementById("meterDataBody").innerHTML = "";
//   loadMeterData();
// }


// Toggle view mode
function toggleView(view) {
  currentView = view;

  // Update active button
  document
    .querySelectorAll(".toggle-btn")
    .forEach((btn) => btn.classList.remove("active"));
  event.target.classList.add("active");

  // Show/hide appropriate controls
  const paginationControls = document.getElementById("paginationControls");
  const tableContainer = document.querySelector(".modal-table-container");

  if (view === "pagination") {
    paginationControls.classList.remove("hidden");
    tableContainer.style.maxHeight = "400px";
    // Remove scroll event listener
    tableContainer.removeEventListener("scroll", handleInfiniteScroll);
  } else {
    paginationControls.classList.add("hidden");
    tableContainer.style.maxHeight = "500px";
    // Add scroll event listener
    tableContainer.addEventListener("scroll", handleInfiniteScroll);
  }

  // Reset and reload data
  currentPage = 1;
  displayedDataCount = 0;
  document.getElementById("meterDataBody").innerHTML = "";
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
        datetime: readingTime.toLocaleString("en-IN"),
        cumKwh: cumulativeKwh.toFixed(2),
        voltage: (230 + Math.random() * 20).toFixed(2),
        current: (5 + Math.random() * 10).toFixed(2),
        power: (800 + Math.random() * 800).toFixed(0),
        relay: Math.random() > 0.1 ? "ON" : "OFF",
        connection: Math.random() > 0.05 ? "ONLINE" : "OFFLINE",
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
  if (currentView === "infinite") {
    document.getElementById("loadingIndicator").classList.remove("hidden");
  }

  // Simulate API call delay
  setTimeout(() => {
    const months = currentTimeRange === "3months" ? 3 : 6;
    allMeterData = generateMeterData(currentMeterId, months);

    if (currentView === "infinite") {
      loadMoreData();
    } else {
      setupPagination();
      loadPageData();
    }

    isLoading = false;
    document.getElementById("loadingIndicator").classList.add("hidden");
  }, 1000);
}

// Load more data for infinite scroll
function loadMoreData() {
  const startIndex = displayedDataCount;
  const endIndex = Math.min(startIndex + itemsPerPage, allMeterData.length);

  const tbody = document.getElementById("meterDataBody");

  for (let i = startIndex; i < endIndex; i++) {
    const row = allMeterData[i];
    const tr = document.createElement("tr");

    // tr.innerHTML = `
    //         <td>${row.datetime}</td>
    //         <td>${row.cumKwh}</td>
    //         <td>${row.voltage}</td>
    //         <td>${row.current}</td>
    //         <td>${row.power}</td>
    //         <td><span class="status-badge ${
    //           row.relay === "ON" ? "status-on" : "status-off"
    //         }">${row.relay}</span></td>
    //         <td><span class="status-badge ${
    //           row.connection === "ONLINE" ? "status-online" : "status-offline"
    //         }">${row.connection}</span></td>
    //     `;

    tbody.appendChild(tr);
  }

  displayedDataCount = endIndex;
}

function sortByDateDesc(data) {
    return data.sort((a, b) => {
        return new Date(b.createdAt) - new Date(a.createdAt);
    });
}


function getLast3MonthsData(data) {

    const now = new Date();

    const threeMonthsAgo = new Date();
    threeMonthsAgo.setMonth(now.getMonth() - 3);

    const filteredData = data.filter(item => {
        const itemDate = new Date(item.createdAt);
        return itemDate >= threeMonthsAgo && itemDate <= now;
    });

    return sortByDateDesc(filteredData);
}


function getLast6MonthsData(data) {
    const now = new Date();

    const sixMonthsAgo = new Date();
    sixMonthsAgo.setMonth(now.getMonth() - 6);

    const filteredData = data.filter(item => {
        const itemDate = new Date(item.createdAt);
        return itemDate >= sixMonthsAgo && itemDate <= now;
    });

    return sortByDateDesc(filteredData);
}

async function fetchOneMeterData(meterId){
    const response = await fetch(`/api/meter/${meterId}`);
    const oneMeterData = await response.json();

    return oneMeterData;
}

function formatDateTime(isoString) {
    const date = new Date(isoString);

    const day = String(date.getDate()).padStart(2, "0");

    const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

    const month = months[date.getMonth()];
    const year = date.getFullYear();

    let hours = date.getHours();
    const minutes = String(date.getMinutes()).padStart(2, "0");

    const ampm = hours >= 12 ? "pm" : "am";

    hours = hours % 12;
    hours = hours ? hours : 12; // 0 becomes 12
    hours = String(hours).padStart(2, "0");

    return `${day} ${month} ${year}, ${hours}:${minutes} ${ampm}`;
}


document.querySelectorAll(".filter-btn").forEach(button => {

    button.addEventListener("click",async function() {
      console.log("clicked");
       //  Remove active class from all buttons
    document.querySelectorAll(".filter-btn").forEach(btn => {
      btn.classList.remove("active");
    });

    //  Add active class to clicked button
    this.classList.add("active");

      const filter = this.dataset.filter;
      let data = updatedOneMeterData;
      if(filter === "3months"){
        data = getLast3MonthsData(data);
        console.log("Now Can you see it : ->",data);
      }else if(filter === "6months"){
        data = getLast6MonthsData(data);
        console.log("Now Can you see it : ->",data);
      }

    const meterDataBody = document.getElementById("meterDataBody");

    meterDataBody.innerHTML = data.map((row) => {
          return `
            <tr>
                  <td>${formatDateTime(row.createdAt)}</td>
                  <td>${row.reading.kwh1}</td>
                  <td>${row.reading.v1}</td>
                  <td>${row.reading.a1}</td>
                  <td>${row.reading.p1}</td>
                  <td>${row.reading.al_rl === 0 ? "OFF": "ON"}</td>
                  <td>${row.reading.al ===  0 ? "OFFLINE": "ONLINE"}</td>
            </tr>
          `
    }).join("")

        
    });

});



let meter_Id = null;
let updatedOneMeterData = null;
async function loadOneMeterData(meterId, filter){
    meter_Id = meterId;
    const oneMeterData = await fetchOneMeterData(meterId);

     updatedOneMeterData = oneMeterData.map(row => {
      try{
        const parsedReading = JSON.parse(row.reading);
        const liveData = parsedReading.live_data;

        return {
          ...row,
          reading: liveData
        }
      }catch(error){
        console.error("Parsing error:", error)
        return row;
      }
    })

    if(filter === "3months"){
      updatedOneMeterData = getLast3MonthsData(updatedOneMeterData);
    }else if(filter === "6months"){
      updatedOneMeterData = getLast6MonthsData(updatedOneMeterData);
    }else{
     sortByDateDesc(updatedOneMeterData);
    }

    console.log("This Data: => ",updatedOneMeterData);

    const meterDataBody = document.getElementById("meterDataBody");

    meterDataBody.innerHTML = updatedOneMeterData.map((row) => {
          return `
            <tr>
                  <td>${formatDateTime(row.createdAt)}</td>
                  <td>${row.reading.kwh1}</td>
                  <td>${row.reading.v1}</td>
                  <td>${row.reading.a1}</td>
                  <td>${row.reading.p1}</td>
                  <td>${row.reading.al_rl === 0 ? "OFF": "ON"}</td>
                  <td>${row.reading.al ===  0 ? "OFFLINE": "ONLINE"}</td>
            </tr>
          `
    }).join("");

    
}

// Handle infinite scroll
function handleInfiniteScroll() {
  const container = document.querySelector(".modal-table-container");

  if (
    container.scrollTop + container.clientHeight >=
    container.scrollHeight - 10
  ) {
    if (displayedDataCount < allMeterData.length && !isLoading) {
      isLoading = true;
      document.getElementById("loadingIndicator").classList.remove("hidden");

      setTimeout(() => {
        loadMoreData();
        isLoading = false;
        document.getElementById("loadingIndicator").classList.add("hidden");
      }, 500);
    }
  }
}

// Setup pagination
function setupPagination() {
  totalPages = Math.ceil(allMeterData.length / itemsPerPage);
  document.getElementById("totalPages").textContent = totalPages;
  updatePaginationButtons();
}

// Load page data for pagination
function loadPageData() {
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = Math.min(startIndex + itemsPerPage, allMeterData.length);

  const tbody = document.getElementById("meterDataBody");
  tbody.innerHTML = "";

  for (let i = startIndex; i < endIndex; i++) {
    const row = allMeterData[i];
    const tr = document.createElement("tr");

    tr.innerHTML = `
            <td>${row.datetime}</td>
            <td>${row.cumKwh}</td>
            <td>${row.voltage}</td>
            <td>${row.current}</td>
            <td>${row.power}</td>
            <td><span class="status-badge ${
              row.relay === "ON" ? "status-on" : "status-off"
            }">${row.relay}</span></td>
            <td><span class="status-badge ${
              row.connection === "ONLINE" ? "status-online" : "status-offline"
            }">${row.connection}</span></td>
        `;

    tbody.appendChild(tr);
  }

  document.getElementById("currentPage").textContent = currentPage;
  updatePaginationButtons();
}

// Navigate pages
function goToPage(direction) {
  switch (direction) {
    case "first":
      currentPage = 1;
      break;
    case "prev":
      if (currentPage > 1) currentPage--;
      break;
    case "next":
      if (currentPage < totalPages) currentPage++;
      break;
    case "last":
      currentPage = totalPages;
      break;
  }

  loadPageData();
}

// Update pagination button states
function updatePaginationButtons() {
  document.getElementById("firstBtn").disabled = currentPage === 1;
  document.getElementById("prevBtn").disabled = currentPage === 1;
  document.getElementById("nextBtn").disabled = currentPage === totalPages;
  document.getElementById("lastBtn").disabled = currentPage === totalPages;
}

// Initialize infinite scroll listener
// document.addEventListener("DOMContentLoaded", function () {
//   const tableContainer = document.querySelector(".modal-table-container");
//   if (tableContainer) {
//     tableContainer.addEventListener("scroll", handleInfiniteScroll);
//   }
// });

// Toggle submenu visibility
function toggleSubmenu(id) {
  const submenu = document.getElementById(id);
  if (submenu) {
    submenu.classList.toggle("open");
  }
}

document
  .getElementById("addOwnerForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const btn = document.getElementById("addOwnerBtn");
    const loader = document.getElementById("ownerLoading");

    // Disable button & show loading
    btn.disabled = true;
    loader.classList.remove("hidden");

    const data = {
      ownerName: document.getElementById("ownerName").value,
      ownerMobile: document.getElementById("ownerMobile").value,
      ownerEmail: document.getElementById("ownerEmail").value,
      ownerAddress: document.getElementById("ownerAddress").value,
    };

    try {
      const response = await fetch("/api/meter/add_owner", {
        // replace with your API
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      const respObj = await response.json();

      if (response.ok) {
        showOwnerPopup(respObj.message);
        // Clear form
        document.getElementById("addOwnerForm").reset();
      } else {
        showOwnerPopup(respObj.message);
      }
    } catch (err) {
      console.error(err);
      showOwnerPopup("Network error!");
    } finally {
      btn.disabled = false;
      loader.classList.add("hidden");
    }
  });

document
  .getElementById("addPGForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const btn = document.getElementById("addPGBtn");
    const loader = document.getElementById("pgLoading");

    // Disable button & show loading
    btn.disabled = true;
    loader.classList.remove("hidden");

    const data = {
      pgName: document.getElementById("pgName").value,
      pgOwnerId: document.getElementById("pgOwnerId").value,
      pgAddress: document.getElementById("pgAddress").value,
    };

    try {
      const response = await fetch("/api/meter/add_pg", {
        // replace with your API
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      const respObj = await response.json();

      if (response.ok) {
        showOwnerPopup(respObj.message);
        // Clear form
        document.getElementById("addPGForm").reset();
      } else {
        showOwnerPopup(respObj.message);
      }
    } catch (err) {
      console.error(err);
      showOwnerPopup("Network error!");
    } finally {
      btn.disabled = false;
      loader.classList.add("hidden");
    }
  });

document
  .getElementById("addRoomForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const btn = document.getElementById("addRoomBtn");
    const loader = document.getElementById("roomLoading");

    // Disable button & show loading
    btn.disabled = true;
    loader.classList.remove("hidden");

    const data = {
      roomNumber: document.getElementById("roomNumber").value,
      roomPG: document.getElementById("roomPG").value,
    };

    try {
      const response = await fetch("/api/meter/add_room", {
        // replace with your API
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      const respObj = await response.json();

      if (response.ok) {
        showOwnerPopup(respObj.message);
        // Clear form
        document.getElementById("addRoomForm").reset();
      } else {
        showOwnerPopup(respObj.message);
      }
    } catch (err) {
      console.error(err);
      showOwnerPopup("Network error!");
    } finally {
      btn.disabled = false;
      loader.classList.add("hidden");
    }
  });

  document
  .getElementById("addTenantForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const btn = document.getElementById("addTenantBtn");
    const loader = document.getElementById("tenantLoading");

    // Disable button & show loading
    btn.disabled = true;
    loader.classList.remove("hidden");

    const data = {
      tenantRoom: document.getElementById("tenantRoom").value,
      tenantName: document.getElementById("tenantName").value,
      tenantEmail: document.getElementById("tenantEmail").value,
      tenantMobile: document.getElementById("tenantMobile").value,
      tenantAddress: document.getElementById("tenantAddress").value,
    };

    try {
      const response = await fetch("/api/meter/add_tenant", {
        // replace with your API
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      const respObj = await response.json();

      if (response.ok) {
        showOwnerPopup(respObj.message);
        // Clear form
        document.getElementById("addTenantForm").reset();
      } else {
        showOwnerPopup(respObj.message);
      }
    } catch (err) {
      console.error(err);
      showOwnerPopup("Network error!");
    } finally {
      btn.disabled = false;
      loader.classList.add("hidden");
    }
  });

function showOwnerPopup(message) {
  const popup = document.getElementById("ownerPopup");
  const msg = document.getElementById("ownerPopupMessage");
  msg.innerText = message;
  popup.classList.remove("hidden");
  setTimeout(() => {
    popup.classList.add("hidden");
  }, 2500); // auto hide after 2.5 seconds
}

function closeOwnerPopup() {
  document.getElementById("ownerPopup").classList.add("hidden");
}

const meterPG = document.getElementById("meterPG");
meterPG.addEventListener("change", () => {
  const meterPGValue = meterPG.value;

  fetch(`/api/meter/get_rooms?pg_id=${meterPGValue}`)
    .then((response) => response.json())
    .then((data) => {
      const rooms = data.rooms;
      let roomsOptions = "<option value=''>Select Room</option>";

      for (let i = 0; i < rooms.length; i++) {
        roomsOptions += `<option value="${rooms[i].id}">${rooms[i].roomNo}</option>`;
      }

      const roomSelector = document.getElementById("meterRoom");
      roomSelector.innerHTML = roomsOptions;
    })
    .catch((error) => console.error("Error:", error));
});

const tenantPG = document.getElementById("tenantPG");
tenantPG.addEventListener("change", () => {
  const tenantPGValue = tenantPG.value;

  fetch(`/api/meter/get_rooms?pg_id=${tenantPGValue}`)
    .then((response) => response.json())
    .then((data) => {
      const rooms = data.rooms;
      let roomsOptions = "<option value=''>Select Room</option>";

      for (let i = 0; i < rooms.length; i++) {
        roomsOptions += `<option value="${rooms[i].id}">${rooms[i].roomNo}</option>`;
      }

      const roomSelector = document.getElementById("tenantRoom");
      roomSelector.innerHTML = roomsOptions;
    })
    .catch((error) => console.error("Error:", error));
});

let updatedData = null; // gloabal so that other function can also use this data 

function loadMeterReadings() {
  fetch("/api/meter/get_onload_data")
    .then(res => res.json())
    .then(data => {
     updatedData = data.map(row => {
        try {
          const parsed = JSON.parse(row.reading);
          return {
            ...row,
            reading: parsed.live_data,
          };
        } catch {
          return {
            ...row,
            reading: null
          };
        }
    });
      //console.log("Data start:->",updatedData);
      const tbody = document.getElementById("meterTableBody");
      tbody.innerHTML = ""; // clear old rows

      updatedData.forEach(readingData => {
        const row = document.createElement("tr");
        //console.log("Reading start:==>",reading);

        // Example: decide status badges dynamically
        const status = readingData.reading.a1 > 0 ? "ON" : "OFF";
        const connection = Math.random() > 0.5 ? "ONLINE" : "OFFLINE"; // replace with real field
        const health = "OK"; // placeholder (can be derived)

        row.innerHTML = `
          <td>${readingData.reading.sn}</td>
          <td>${readingData.reading.al_rl}</td>
          <td>${formatDateTime(readingData.createdAt)}</td>
          <td>${readingData.reading.kwh1}</td>
          <td>${readingData.reading.v1}</td>
          <td>${readingData.reading.a1}</td>
          <td>${readingData.reading.p1}</td>
          <td>${readingData.reading.pf1}</td>
          <td>${readingData.reading.f1}</td>
          <td>${readingData.reading.kwh2}</td>
          <td>${readingData.reading.v2}</td>
          <td>${readingData.reading.a2}</td>
          <td>${readingData.reading.p2}</td>
          <td>${readingData.reading.pf2}</td>
          <td>${readingData.reading.f2}</td>
          <td>${readingData.reading.al}</td>           

          <td><span class="status-badge ${status === "ON" ? "status-on" : "status-off"}">${status}</span></td>
          <td><span class="status-badge status-online">${health}</span></td>
          <td><span class="status-badge ${connection === "ONLINE" ? "status-online" : "status-offline"}">${connection}</span></td>
        
          <td><button class="view-btn" onclick="viewMeterDetails('${readingData.reading.sn}')">View</button></td>
        `;

        tbody.appendChild(row);
      });
    })
    .catch(err => console.error("Error loading readings:", err));
}

/**
 * Toggles the visibility of different views in the main content area.
 * @param {string} viewId The ID of the view to show (e.g., 'dashboard', 'liveData').
 */
function showView(viewId) {
    const views = ['dashboardView', 'liveDataView', 'rechargeView', 'addOwnerView', 'addPGView', 'addRoomView', 'addMeterView', 'addTenantView'];

    // Hide all views
    views.forEach(id => {
        const view = document.getElementById(id);
        if (view) {
            view.classList.add('hidden');
        }
    });

    // Show the requested view
    const activeView = document.getElementById(viewId);
    if (activeView) {
        activeView.classList.remove('hidden');
    }

    // Update the content title
    const title = document.getElementById('contentTitle');
    if (title) {
        if (viewId === 'dashboardView') title.textContent = 'Dashboard';
        else if (viewId === 'liveDataView') {
        title.textContent = 'Live Meter Data';
        loadMeterReadings()
        }
        else if (viewId === 'rechargeView') title.textContent = 'Recharge Details';
        else if (viewId.startsWith('add')) title.textContent = 'Add New ' + viewId.substring(3).replace(/([A-Z])/g, ' $1').trim();
    }

    // Update active state in sidebar
    document.querySelectorAll('.nav-link').forEach(link => link.classList.remove('active'));
    const activeLink = document.querySelector(`.nav-link[onclick*="showView('${viewId}')"]`);
    if (activeLink) {
        activeLink.classList.add('active');
    }

    // **IMPORTANT:** Initialize charts when dashboard is shown
    if (viewId === 'dashboardView' && typeof Chart !== 'undefined') {
        // We use a small timeout to ensure the canvas elements are fully rendered before Chart.js tries to access them
        setTimeout(initializeCharts, 100);
    }
}

function openSidebar() {
  document.getElementById("sidebar").classList.add("active");
  document.querySelector(".main-content").classList.add("shift");

  // Hide hamburger
  document.querySelector(".hamburger").classList.add("hide");
}

function closeSidebar() {
  document.getElementById("sidebar").classList.remove("active");
  document.querySelector(".main-content").classList.remove("shift");

  // Show hamburger again
  document.querySelector(".hamburger").classList.remove("hide");
}

const sidebar = document.getElementById("sidebar");

// sidebar.addEventListener("mouseleave", function () {
//   closeSidebar();
// }); 

// document.querySelector(".hamburger").addEventListener("mouseenter", function () {
//   openSidebar();
// })

/**
 * Toggles the visibility of a submenu.
 * @param {string} submenuId The ID of the submenu to toggle (e.g., 'addNewSubmenu').
 */
function toggleSubmenu(submenuId) {
    const submenu = document.getElementById(submenuId);
    if (submenu) {
        submenu.classList.toggle('open');
    }
}

// Set initial view to dashboard and initialize charts on load
document.addEventListener('DOMContentLoaded', () => {
    // Ensure the dashboard is the default view on load
    showView('dashboardView');
});


// --- Chart.js Implementation ---

// Sample data for demonstration. In a real application, this would come from an API call.
const mockData = {
    monthly: {
        labels: ['Aug', 'Sep', 'Oct', 'Nov', 'Dec', 'Jan'],
        data: [4500, 3800, 5100, 4200, 5500, 5800]
    },
    status: {
        labels: ['Connected', 'Disconnected', 'Maintenance'],
        data: [100, 20, 5],
        colors: ['#28a745', '#dc3545', '#ffc107']
    },
    consumers: {
        labels: ['Room 101', 'Room 205', 'Room 310', 'Room 102', 'Room 401'],
        data: [120, 115, 95, 80, 75]
    }
};

/**
 * Initializes and renders all Chart.js charts on the dashboard.
 */
function initializeCharts() {
    // 1. Monthly Energy Consumption (Line Chart)
    const monthlyCtx = document.getElementById('monthlyConsumptionChart');
    if (monthlyCtx) {
        // Destroy existing chart if it exists to prevent overlap
        if (monthlyCtx.chart) monthlyCtx.chart.destroy();

        monthlyCtx.chart = new Chart(monthlyCtx, {
            type: 'line',
            data: {
                labels: mockData.monthly.labels,
                datasets: [{
                    label: 'Total kWh Consumed',
                    data: mockData.monthly.data,
                    borderColor: '#667eea',
                    backgroundColor: 'rgba(102, 126, 234, 0.2)',
                    fill: true,
                    tension: 0.4,
                    pointRadius: 5
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Consumption (kWh)'
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    title: {
                        display: false
                    }
                }
            }
        });
    }


    // 2. Meter Connection Status (Doughnut Chart)
    const statusCtx = document.getElementById('meterStatusChart');
    if (statusCtx) {
        // Destroy existing chart if it exists
        if (statusCtx.chart) statusCtx.chart.destroy();

        statusCtx.chart = new Chart(statusCtx, {
            type: 'doughnut',
            data: {
                labels: mockData.status.labels,
                datasets: [{
                    data: mockData.status.data,
                    backgroundColor: mockData.status.colors,
                    hoverOffset: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                    },
                    title: {
                        display: false
                    }
                }
            }
        });
    }


    // 3. Top 5 Consumers (Horizontal Bar Chart)
    const consumersCtx = document.getElementById('topConsumersChart');
    if (consumersCtx) {
        // Destroy existing chart if it exists
        if (consumersCtx.chart) consumersCtx.chart.destroy();

        consumersCtx.chart = new Chart(consumersCtx, {
            type: 'bar',
            data: {
                labels: mockData.consumers.labels,
                datasets: [{
                    label: 'kWh (Last 7 Days)',
                    data: mockData.consumers.data,
                    backgroundColor: '#764ba2',
                }]
            },
            options: {
                indexAxis: 'y', // Makes it a horizontal bar chart
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Consumption (kWh)'
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    title: {
                        display: false
                    }
                }
            }
        });
    }
}