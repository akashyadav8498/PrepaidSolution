let unreadCount = 0;
let tenantData;

async function loadTenantData() {
  try {
    const response = await fetch("/api/tenants/tenant-data", {
      method: "GET",
      credentials: "include",
    });

    if (!response.ok) {
      throw new Error("Failed to fetch tenant data");
    }

    const data = await response.json();
    console.log("Data:", data);

    tenantData = data;
    return data;
  } catch (error) {
    console.error("Error", error.message);
  }
}

function updateGauge(value, maxlimit) {
  const arc = document.getElementById("progressArc");
  const valueText = document.getElementById("gaugeValue");
  const circumference = 252;

  const offset = circumference - (value / maxlimit) * circumference;

  arc.style.strokeDashoffset = offset;

  valueText.textContent = value + " W";
}

async function loadEnergyData() {
  try {
    let meterSerialNumber = "801235"; // for testing purpose only, later change it.
    if (tenantData.tenantMeterSerialNumber != null) {
      meterSerialNumber = tenantData.tenantMeterSerialNumber;
    }

    const response = await fetch(`/api/tenants/reading/${meterSerialNumber}`, {
      method: "GET",
      credentials: "include",
    });

    if (!response.ok) {
      throw new Error("Failed to fetch tenant enegry data");
    }

    const data = await response.json();
    console.log("Energy Data:", data);

    const energySource = data.dg ? "DG" : "EB";
    document.getElementById("energy-source").textContent = energySource;
    const connection = data.connectionStatus ? "ONLINE" : "OFFLINE";
    const relayStatus = data.relayStatus ? "ON" : "OFF";

    document.getElementById("connection-status").textContent = connection;
    document.getElementById("relay-status").textContent = relayStatus;
    document.getElementById("today-eb-reading").textContent = data.todayEbUsage + " kwh";
    document.getElementById("today-dg-reading").textContent = data.totalDgReading + " kwh";
    document.getElementById("total-eb-reading").textContent = data.totalEbReading + " kwh";
    document.getElementById("total-dg-reading").textContent = data.totalDgReading + " kwh";

    let value = data.lastReading.p1 > data.lastReading.p2 ? data.lastReading.p1 : data.lastReading.p2;

    updateGauge(value, 500); // 500w is max limit. later sir will send it inside reading data.

    const lastUpdated = new Date(data.lastUpdatedTime);

    document.getElementById("last-updated-time").textContent = "Last Updated : " + lastUpdated.toLocaleString();
  } catch (error) {
    console.error("Error", error.message);
  }
}

async function logout() {
  try {
    await fetch("/api/tenants/logout", {
      method: "POST",
      credentials: "include",
    });

    // redirect after logout
    window.location.href = "/";
  } catch (error) {
    console.log("Logout failed", error);
  }
}

function dynamicTenantData(data) {
  document.getElementById("section-title-name").textContent = data.tenantName;
  document.getElementById("current-balance").textContent = "₹" + data.tenantCurrentBalance;

  const meterSerialElement = document.getElementById("meter-serialNo");
  data.tenantMeterSerialNumber === null ? (meterSerialElement.textContent = "NA") : (meterSerialElement.textContent = data.tenantMeterSerialNumber);

  document.getElementById("room-no").textContent = data.tenantRoomNumber;
  document.getElementById("tenant-name").textContent = data.tenantName;
  document.getElementById("tenant-phoneNo").textContent = data.tenantPhoneNumber;
  document.getElementById("tenant-email").textContent = data.tenantEmail;
}

async function loadUnreadCount(tenantId) {
  try {
    const response = await fetch(`/api/notifications/unread-count?recipientId=${tenantId}&recipientType=TENANT`);

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
    await fetch(`/api/notifications/mark-read?recipientId=${tenantData.tenantId}&recipientType=TENANT`, {
      method: "POST",
    });

    // reset badge locally
    resetNotifications();
  } catch (error) {
    console.log("Error marking notifications:", error);
  }
}

async function loadNotifications() {
  try {
    const response = await fetch(`/api/notifications/latest?recipientId=${tenantData.tenantId}&recipientType=TENANT`);

    const notifications = await response.json();

    const container = document.getElementById("notifications-container");

    container.innerHTML = ""; // clear old

    notifications.forEach((n) => {
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
    minute: "2-digit",
  };

  return date.toLocaleString("en-IN", options);
}

function handleNotificationClick() {
  markNotificationsAsRead();
  loadNotifications();
  navTo("view-alerts", null);
}

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

  setTimeout(() => {
    box.style.display = "none";
  }, 3000);
}

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

// add script tag in head <------------------------------------------------------------------------------------------------
// Websocket Connection (IMPORTANT)
function connectWebSocket(tenantId) {
  if (!tenantId) {
    console.error("❌ TenantId not found, WebSocket not connected");
    return;
  }

  const socket = new SockJS("/ws");
  const stompClient = Stomp.over(socket);

  stompClient.connect({}, function () {
    console.log("✅ WebSocket Connected for Tenant:", tenantId);

    stompClient.subscribe("/topic/tenant/" + tenantId, function (message) {
      // Parse DTO JSON
      const data = JSON.parse(message.body);
      console.log("Notification Data:", data);

      // Increase unread count
      unreadCount++;

      // Update badge UI
      updateNotificationBadge();

      if (data.type === "LOW_BALANCE") {
        showNotification(data.tenantName + " → " + data.message, "warning");
      } else if (data.type === "BALANCE_ADDED") {
        showNotification(data.tenantName + " → " + data.message, "success");
      }
    });
  });
}

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
}

// Toggle Profile Dropdown
function toggleDropdown() {
  document.getElementById("profileMenu").classList.toggle("show");
}

// Close dropdown when clicking outside
document.addEventListener("click", function (event) {
  const dropdown = document.querySelector(".profile-dropdown");

  if (!dropdown.contains(event.target)) {
    document.getElementById("profileMenu").classList.remove("show");
  }
});

// Logout Modal Logic
function showLogoutModal() {
  document.getElementById("logoutModal").style.display = "flex";
}

function hideLogoutModal() {
  document.getElementById("logoutModal").style.display = "none";
}

// function logout() {
//   window.location.href = "/";
// }

// Small tweak to existing navTo to close dropdown
let originalNavTo = navTo;
navTo = function (viewId, btn) {
  originalNavTo(viewId, btn);
  document.getElementById("profileMenu").classList.remove("show");
};
if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("/service-worker.js").catch((error) => console.error("Service worker registration failed", error));
  });
}

async function firstFunction() {
  const data = await loadTenantData();
  dynamicTenantData(data);

  // Connect WebSocket AFTER we have ownerId
  connectWebSocket(data.tenantId);

  loadUnreadCount(data.tenantId);

  await loadEnergyData();
}

function openRechargeModal() {
  const modal = document.getElementById("rechargeModal");
  modal.style.display = "block";

  setTimeout(() => {
    modal.classList.add("active");
  }, 10);
}

function closeRechargeModal() {
  const modal = document.getElementById("rechargeModal");
  modal.classList.remove("active");

  setTimeout(() => {
    modal.style.display = "none";
  }, 300);
}

function increaseAmount() {
  const input = document.getElementById("amountInput");
  input.value = parseInt(input.value || 0) + 10;
}

function decreaseAmount() {
  const input = document.getElementById("amountInput");
  let val = parseInt(input.value || 0);
  if (val > 0) input.value = val - 10;
}

async function recharge() {
  const amount = document.getElementById("amountInput").value;

  // Basic validation
  if (!amount || amount <= 0) {
    alert("Enter a valid amount");
    return;
  }

  try {
    const response = await fetch("/balance/add", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        tenantId: tenantData.tenantId,
        amount: amount,
      }),
    });

    if (!response.ok) {
      throw new Error("Failed to recharge");
    }

    const data = await response.json();
    console.log("Recharge sucess:", data);

    document.getElementById("current-balance").innerText = "₹" + data.balance;

    closeRechargeModal();
  } catch (error) {
    console.log("Error:", error);
    alert("Recharge failed");
  }
}

/* Close when clicking outside */
window.onclick = function (event) {
  const modal = document.getElementById("rechargeModal");
  if (event.target === modal) {
    closeRechargeModal();
  }
};

document.addEventListener("DOMContentLoaded", function () {
  firstFunction();
});

// -----------------------------------------------------------------------------------------------

// // Tab functionality
// document.addEventListener("DOMContentLoaded", function () {
//   const tabs = document.querySelectorAll(".nav-tab");
//   const tabContents = document.querySelectorAll(".tab-content");

//   tabs.forEach((tab) => {
//     tab.addEventListener("click", function () {
//       const targetTab = this.getAttribute("data-tab");

//       // Remove active class from all tabs and contents
//       tabs.forEach((t) => t.classList.remove("active"));
//       tabContents.forEach((content) => content.classList.remove("active"));

//       // Add active class to clicked tab and corresponding content
//       this.classList.add("active");
//       document.getElementById(targetTab).classList.add("active");
//     });
//   });

//   console.log("control is here");

//   // PWA functionality
//   let deferredPrompt;

//   window.addEventListener("beforeinstallprompt", (e) => {
//     e.preventDefault();
//     deferredPrompt = e;
//     showInstallButton();
//   });

//   function showInstallButton() {
//     // You can add an install button here if needed
//     console.log("PWA can be installed");
//   }

//   // Service Worker registration
//   if ("serviceWorker" in navigator) {
//     navigator.serviceWorker
//       .register(
//         "data:application/javascript;base64,Ly8gU2ltcGxlIHNlcnZpY2Ugd29ya2VyCmNvbnN0IENBQ0hFX05BTUUgPSAnYXJpb3QtcHdhLXYxJzsKY29uc3QgdXJsc1RvQ2FjaGUgPSBbCiAgJy8nCl07CgpzZWxmLmFkZEV2ZW50TGlzdGVuZXIoJ2luc3RhbGwnLCAoZXZlbnQpID0+IHsKICBldmVudC53YWl0VW50aWwoCiAgICBjYWNoZXMub3BlbihDQUNIRV9OQU1FKQogICAgICAudGhlbigoY2FjaGUpID0+IGNhY2hlLmFkZEFsbCh1cmxzVG9DYWNoZSkpCiAgKTsKfSk7CgpzZWxmLmFkZEV2ZW50TGlzdGVuZXIoJ2ZldGNoJywgKGV2ZW50KSA9PiB7CiAgZXZlbnQucmVzcG9uZFdpdGgoCiAgICBjYWNoZXMubWF0Y2goZXZlbnQucmVxdWVzdCkKICAgICAgLnRoZW4oKHJlc3BvbnNlKSA9PiB7CiAgICAgICAgcmV0dXJuIHJlc3BvbnNlIHx8IGZldGNoKGV2ZW50LnJlcXVlc3QpOwogICAgICB9KQogICk7Cn0pOw==",
//       )
//       .then(function (registration) {
//         console.log("Service Worker registered successfully");
//       })
//       .catch(function (error) {
//         console.log("Service Worker registration failed");
//       });
//   }

//   // Simulate real-time updates
//   setInterval(updateLiveData, 30000); // Update every 30 seconds
// });

// function updateLiveData() {
//   // Simulate live data updates
//   const voltElement = document.querySelector(".metric-item:nth-child(1) .metric-value");
//   const ampElement = document.querySelector(".metric-item:nth-child(2) .metric-value");
//   const powerElement = document.querySelector(".metric-item:nth-child(3) .metric-value");

//   if (voltElement) {
//     const currentVolt = parseFloat(voltElement.textContent);
//     const newVolt = (currentVolt + (Math.random() - 0.5) * 2).toFixed(2);
//     voltElement.textContent = Math.max(220, Math.min(250, newVolt));
//   }

//   if (ampElement) {
//     const currentAmp = parseFloat(ampElement.textContent);
//     const newAmp = (currentAmp + (Math.random() - 0.5) * 0.1).toFixed(2);
//     ampElement.textContent = Math.max(0.5, Math.min(3, newAmp));
//   }

//   if (powerElement) {
//     const volt = parseFloat(voltElement.textContent);
//     const amp = parseFloat(ampElement.textContent);
//     powerElement.textContent = Math.round(volt * amp);
//   }
// }

// // Add touch feedback for mobile
// document.addEventListener("touchstart", function () {}, { passive: true });
