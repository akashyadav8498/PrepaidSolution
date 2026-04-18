let ownerData;
let unreadCount = 0;

// Entry point → runs when DOM is ready
document.addEventListener("DOMContentLoaded", function () {
  loadOwnerData();
});


// ==========================================
// STEP 1: Load Owner Data (ASYNC CONTROL)
// ==========================================
async function loadOwnerData() {
  try {
    // Using await → ensures code waits until API response comes
    const response = await fetch(`/api/owners/stats`);
    const data = await response.json();

    console.log("Data:", data);

    ownerData = data;

    // ==========================================
    // Now everything runs AFTER data is ready
    // ==========================================

    renderPGDropDown(); // existing function

    dynamicOwnerDataFields(data); // moved DOM updates to separate function

    // Connect WebSocket AFTER we have ownerId
    connectWebSocket(data.ownerId);

    // load unread count from DB
    loadUnreadCount(data.ownerId);

  } catch (error) {
    console.log("Error:", error);
  }
}


// ==========================================
// STEP 2: Update UI Fields (MODULAR)
// ==========================================
function dynamicOwnerDataFields(data) {

  document.getElementById("totalRooms").textContent = `${data.totalRooms}`;
  document.getElementById("totalPgs").textContent = `${data.totalPgs}`;
  document.getElementById("totalTenants").textContent = `${data.totalTenants}`;
  document.getElementById("totalMeters").textContent = `${data.totalMeters}`;

  document.getElementById("welcomeName").textContent = `${data.ownerName.split(" ")[0]}`;

  document.getElementById("ownerProfileName").textContent = `${data.ownerName}`;
  document.getElementById("ownerProfileMobile").textContent = `${data.ownerMobile}`;
  document.getElementById("ownerProfileEmail").textContent = `${data.ownerEmail}`;
}


// ==========================================
// STEP 3: WebSocket Connection (IMPORTANT)
// ==========================================
function connectWebSocket(ownerId) {
  // 🔥 Ensure ownerId exists before connecting
  if (!ownerId) {
    console.error("❌ OwnerId not found, WebSocket not connected");
    return;
  }

  const socket = new SockJS('/ws');
  const stompClient = Stomp.over(socket);

  stompClient.connect({}, function () {

    console.log("✅ WebSocket Connected for owner:", ownerId);

    // Unified topic (your new architecture)
    stompClient.subscribe('/topic/owner/' + ownerId, function (message) {

      // Parse DTO JSON
      const data = JSON.parse(message.body);

      console.log("📩 Notification:", data);

      // Increase unread count
      unreadCount++;

      // Update badge UI
      updateNotificationBadge();

      // Handle based on type
      if (data.type === "LOW_BALANCE") {
        showNotification(
          data.tenantName + " → " + data.message,
          "warning"
        );
      } 
      else if (data.type === "BALANCE_ADDED") {
        showNotification(
          data.tenantName + " → " + data.message,
          "success"
        );
      }
    });
  });
}

async function loadUnreadCount(ownerId) {

  try {
    const response = await fetch(
      `/api/notifications/unread-count?recipientId=${ownerId}&recipientType=OWNER`
    );

    const count = await response.json();

    // set global count
    unreadCount = count;

    updateNotificationBadge();

  } catch (error) {
    console.log("Error loading unread count:", error);
  }
}

async function markNotificationsAsRead() {
  try {

    // call mark-read API
    await fetch(
      `/api/notifications/mark-read?recipientId=${ownerData.ownerId}&recipientType=OWNER`,
      {
        method: "POST"
      }
    );

    // reset badge locally
    unreadCount = 0;
    updateNotificationBadge();

  } catch (error) {
    console.log("Error marking notifications:", error);
  }
}

async function loadNotifications() {
  try {
    const response = await fetch(
      `/api/notifications/latest?recipientId=${ownerData.ownerId}&recipientType=OWNER`
    );

    const notifications = await response.json();

    const container = document.getElementById("notifications-container");

    container.innerHTML = ""; // clear old

    notifications.forEach(n => {

      const card = document.createElement("div");
      card.className = "card";

      // border color based on type
      if (n.type === "LOW_BALANCE") {
        card.style.borderLeft = "4px solid red";
      } else {
        card.style.borderLeft = "4px solid green";
      }

      const date = new Date(n.createdAt);

      card.innerHTML = `
        <p style="font-size:0.7rem;color:var(--muted);margin-bottom:5px">
          ${formatDate(date)}
        </p>
        <p style="font-size:0.85rem">
          ${n.message}
        </p>
      `;

      container.appendChild(card);
    });

  } catch (error) {
    console.log("Error loading notifications:", error);
  }
}

function formatDate(date) {

  const options = {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  };

  return date.toLocaleString("en-IN", options);
}

function handleNotificationClick() {
  markNotificationsAsRead(); // already done
  loadNotifications(); // 
  navTo('view-alerts', null);
}


// ==========================================
// STEP 4: Notification UI
// ==========================================
function showNotification(text, type) {

  const box = document.getElementById("notification");
  const textEl = document.getElementById("notification-text");

  textEl.innerText = text;

  // Dynamic color based on type
  if (type === "warning") {
    box.style.background = "#ff4d4d";
  } else {
    box.style.background = "#28a745";
  }

  box.style.display = "flex";

  setTimeout(() =>{
    box.style.display = "none";
  },3000);
}


// ==========================================
// STEP 5: Close Button
// ==========================================
document.getElementById("close-btn").onclick = function () {
  document.getElementById("notification").style.display = "none";
};


function updateNotificationBadge() {

  const badge = document.getElementById("notification-badge");

  if (!badge) return;

  if (unreadCount > 0) {
    badge.style.display = "flex";
    badge.innerText = unreadCount;
  } else {
    badge.style.display = "none";
  }
}

function resetNotifications() {
  unreadCount = 0;
  updateNotificationBadge();
}

function renderRooms(rooms) {
  const container = document.getElementById("rooms-list-grid");

  container.innerHTML = ""; // clear old

  if (!rooms || rooms.length === 0) {
    container.innerHTML = "<p>No rooms found</p>";
    return;
  }

  rooms.forEach((room) => {
    const isPositive = room.balance >= 0;
    const relayStatus = room.relayStatus === true;
    const connectionStatus = room.connectionStatus === true;
    const eb = room.eb;
    const dg = room.dg;

    const statusText = relayStatus ? "CONNECTED" : "DISCONNECTED";
    const relayColor = relayStatus ? "var(--success)" : "var(--danger)";
    console.log(relayColor, relayStatus,room);
    const connectionText = connectionStatus ? "Online" : "Offline";
    const connectionColor = connectionStatus ? "var(--success)" : "var(--danger)";

    const bgColor = isPositive ? "#ecfdf5" : "#fef2f2";
    const borderColor = isPositive ? "#10b98133" : "#ef444433";
    const textColor = isPositive ? "#064e3b" : "#7f1d1d";
    const labelColor = isPositive ? "#065f46" : "#991b1b";

    const div = document.createElement("div");

    div.setAttribute("onclick", 
    `openRoomDetails('${room.roomNumber}', '${room.tenantName}', '${room.balance}', '${statusText}', '${connectionText}', '${room.meterId}', '${eb}', '${dg}', '${bgColor}', '${borderColor}', '${relayColor}', '${connectionColor}')`);

    div.style = `
      padding: 12px 5px;
      border-radius: 12px;
      background: ${bgColor};
      border: 1px solid ${borderColor};
      transition: transform 0.1s ease;
      cursor: pointer;
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 10px;
    `;

    div.onmousedown = () => (div.style.transform = "scale(0.95)");
    div.onmouseup = () => (div.style.transform = "scale(1)");

    div.innerHTML = `
      <div style="text-align: center">
        <p style="font-size: 0.5rem; color: ${labelColor}; font-weight: 700; text-transform: uppercase; margin-bottom: 2px">Room</p>
        <p style="font-size: 0.9rem; color: ${textColor}; font-weight: 900">${room.roomNumber}</p>
      </div>

      <div style="width: 1px; height: 28px; background: ${borderColor}"></div>

      <div style="text-align: center">
        <p style="font-size: 0.5rem; color: ${labelColor}; font-weight: 700; text-transform: uppercase; margin-bottom: 2px">Balance</p>
        <p style="font-size: 0.9rem; font-weight: 900; color: ${textColor}">
          ₹${room.balance}
        </p>
      </div>
    `;

    container.appendChild(div);
  });
}

function renderPGDropDown() {
  const optionsContainer = document.getElementById("pgOptions");
  const pgs = ownerData.ownerPGs;

  // Clear existing options
  optionsContainer.innerHTML = "";

  // Helper function to handle the selection logic
  const selectPG = (pg) => {
    document.getElementById("selectedPg").textContent = pg.name;
    optionsContainer.style.display = "none";

    fetch(`/api/owners/pg/${pg.id}/rooms`)
      .then((res) => res.json())
      .then((data) => {
        renderRooms(data);
      })
      .catch((err) => console.error("Error fetching rooms:", err));
  };

  // Case 1: Only one PG - Select and render automatically
  if (pgs.length === 1) {
    selectPG(pgs[0]);
    return; // Exit early
  }

  // Case 2: Multiple PGs - Build the dropdown
  pgs.forEach((pg) => {
    const option = document.createElement("div");
    option.textContent = pg.name;
    option.dataset.id = pg.id;

    option.addEventListener("click", () => selectPG(pg));
    optionsContainer.appendChild(option);
  });
}

const selected = document.getElementById("selectedPg");
const options = document.getElementById("pgOptions");

selected.addEventListener("click", () => {
  options.style.display = options.style.display === "block" ? "none" : "block";
});

document.addEventListener("click", (e) => {
  if (!e.target.closest(".custom-dropdown")) {
    options.style.display = "none";
  }
});

function navTo(viewId, btn) {
  // 1. Switch Views
  document.querySelectorAll(".view").forEach((v) => v.classList.remove("active"));
  const target = document.getElementById(viewId);
  if (target) target.classList.add("active");

  // 2. Update Button State
  document.querySelectorAll(".nav-btn").forEach((b) => b.classList.remove("active"));
  if (btn) {
    btn.classList.add("active");

    // 3. Smoothly center the button in the scroll view
    btn.scrollIntoView({
      behavior: "smooth",
      inline: "center",
      block: "nearest",
    });
  }
  wrapper.scrollLeft = 0; // Scroll to left automatically
  wrapper.style.scrollBehavior = "auto";
}

// Toggle Profile Dropdown
function toggleDropdown() {
  document.getElementById("profileMenu").classList.toggle("show");
}

// Close dropdown when clicking outside
window.onclick = function (event) {
  if (!event.target.closest(".profile-dropdown")) {
    var dropdowns = document.getElementsByClassName("dropdown-menu");
    for (var i = 0; i < dropdowns.length; i++) {
      dropdowns[i].classList.remove("show");
    }
  }
};

// Logout Modal Logic
function showLogoutModal() {
  document.getElementById("logoutModal").style.display = "flex";
}

function hideLogoutModal() {
  document.getElementById("logoutModal").style.display = "none";
}

function logout() {
  window.location.href = "/logout";
}

// Small tweak to existing navTo to close dropdown
const originalNavTo = navTo;
navTo = function (viewId, btn) {
  originalNavTo(viewId, btn);
  document.getElementById("profileMenu").classList.remove("show");
};

const wrapper = document.getElementById("main-scroll-wrapper");

function openRoomDetails(roomNo, tenant, balance, relay, connection, meter, eb, dg, bgColor, borderColor, relayColor, connectionColor) {
  // 1. Fill Data
  console.log("first",relayColor);
  document.getElementById("det-room-no").innerText = "Room No: " + roomNo;
  document.getElementById("det-tenant").innerText = "Tenant: " + tenant;
  document.getElementById("det-balance").innerText = "₹" + balance;
  document.getElementById("det-meter-id").innerText = meter;
  document.getElementById("det-eb").innerText = eb;
  document.getElementById("det-dg").innerText = dg;
  document.getElementById("det-relay-status").innerHTML = relay;
  document.getElementById("det-connection-status").innerHTML = connection;

  const relayBadge = document.getElementById("det-relay-status");
  relayBadge.style.background = relayColor;
  console.log(relayColor);

  const connectionBadge = document.getElementById("det-connection-status");
  connectionBadge.style.color = connectionColor;

  const details = document.getElementById("room-details-card");
  details.style.background = bgColor;
  details.style.border = "1px solid " + borderColor;

  // 2. Unlock horizontal scroll and jump to details
  wrapper.style.overflowX = "auto";
  wrapper.scrollTo({
    left: wrapper.offsetWidth,
    behavior: "smooth",
  });
}

// Listen for the scroll event to lock the screen when back at the start
wrapper.addEventListener("scroll", () => {
  // If the user has scrolled back to the list (position 0)
  if (wrapper.scrollLeft === 0) {
    wrapper.style.overflowX = "hidden";
  }
});

function openSpecificHistory() {
  const roomNo = document.getElementById("det-room-no").innerText.replace("Room No: ", "");
  document.getElementById("hist-room-no").innerText = "Room " + roomNo + " History";

  // Slide to the 3rd page (index 2)
  wrapper.scrollTo({
    left: wrapper.offsetWidth * 2,
    behavior: "smooth",
  });
}

function backToDetails() {
  // Slide back to the 2nd page (index 1)
  wrapper.scrollTo({
    left: wrapper.offsetWidth,
    behavior: "smooth",
  });
}

// Update your existing scroll listener to handle the 3-page lock logic
wrapper.addEventListener("scroll", () => {
  // Lock scroll only if at the very first page (Room List)
  if (wrapper.scrollLeft === 0) {
    wrapper.style.overflowX = "hidden";
  } else {
    wrapper.style.overflowX = "auto";
  }
});
if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("/service-worker.js").catch((error) => console.error("Service worker registration failed", error));
  });
}
