function loadOwnerData() {
  let ownerId = 2;
  fetch(`/api/owners/${ownerId}/stats`)
    .then((response) => response.json())
    .then((data) => {
      console.log("Data:", data);
      document.getElementById("totalRooms").textContent = `${data.totalRooms}`;
      document.getElementById("totalPgs").textContent = `${data.totalPgs}`;
      document.getElementById("totalTenants").textContent = `${data.totalTenants}`;
      document.getElementById("totalMeters").textContent = `${data.totalMeters}`;
      document.getElementById("welcomeName").textContent = `${data.ownerName.split(" ")[0]}`;
      document.getElementById("ownerProfileName").textContent = `${data.ownerName}`;
      document.getElementById("ownerProfileMobile").textContent = `${data.ownerMobile}`;
      document.getElementById("ownerProfileEmail").textContent = `${data.ownerEmail}`;
    })
    .catch((error) => console.error("Error:", error));
}

async function loadPGs() {
  const ownerId = 2;

  const response = await fetch(`/api/owners/${ownerId}/pgs`);
  const pgs = await response.json();

  const optionsContainer = document.getElementById("pgOptions");

  optionsContainer.innerHTML = "";

  pgs.forEach((pg) => {
    const option = document.createElement("div");
    option.textContent = pg.name;
    option.dataset.id = pg.id;

    option.addEventListener("click", () => {
      document.getElementById("selectedPg").textContent = pg.name;

      optionsContainer.style.display = "none";

      console.log("Selected PG ID:", pg.id);
    });

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

document.addEventListener("DOMContentLoaded", function () {
  loadOwnerData();
  loadPGs();
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
  window.location.href = "/";
}

// Small tweak to existing navTo to close dropdown
const originalNavTo = navTo;
navTo = function (viewId, btn) {
  originalNavTo(viewId, btn);
  document.getElementById("profileMenu").classList.remove("show");
};

const wrapper = document.getElementById("main-scroll-wrapper");

function openRoomDetails(roomNo, tenant, balance, conn, power, meter, eb, dg, bgColor, borderColor, statusColor) {
  // 1. Fill Data
  document.getElementById("det-room-no").innerText = "Room No: " + roomNo;
  document.getElementById("det-tenant").innerText = "Tenant: " + tenant;
  document.getElementById("det-balance").innerText = balance;
  document.getElementById("det-meter-id").innerText = meter;
  document.getElementById("det-eb").innerText = eb;
  document.getElementById("det-dg").innerText = dg;

  const connBadge = document.getElementById("det-conn-status");
  connBadge.innerText = conn;
  connBadge.style.background = statusColor;

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
