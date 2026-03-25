
document.addEventListener("DOMContentLoaded", function () {
   loadMeterReadings();  
   console.log("It is working")
   setInterval(loadMeterReadings, 15000);
});

function loadMeterReadings() {
  fetch("/api/meter/get_onload_data")
    .then(res => res.json())
    .then(data => {

      const updatedData = data.map(row => {
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

      const container = document.getElementById("meter-table-mobile");
      container.innerHTML = ""; // clear old cards

      updatedData.forEach(readingData => {

        if (!readingData.reading) return;

        const r = readingData.reading;

        // Dynamic Status Logic
        const relayStatus = r.a1 > 0 ? "connected" : "disconnected";
        const relayClass = r.a1 > 0 ? "status-on" : "status-off";
        const relayTitle = r.a1 > 0 ? "Relay ON" : "Relay OFF";

        const connectionOnline = r.al === 1;  // change according to your real field
        const connectionClass = connectionOnline ? "online" : "offline";
        const connectionDotClass = connectionOnline ? "status-online" : "status-offline";
        const connectionTitle = connectionOnline ? "Online" : "Offline";

        const card = document.createElement("div");
        card.className = `table-row-mobile ${relayStatus} ${connectionClass}`;
        card.setAttribute("onclick", `viewMeterDetails('${r.sn}')`);

        card.innerHTML = `
          <div class="row-header">
              <span class="meter-id">Meter ${r.sn}</span>
              <div class="meter-status">
                  <div class="status-dot ${relayClass}" title="${relayTitle}"></div>
                  <div class="status-dot ${connectionDotClass}" title="${connectionTitle}"></div>
              </div>
          </div>

          <div class="row-details">
              <div class="detail-item">
                  <span class="detail-label">kWh:</span>
                  <span class="detail-value">${r.kwh1 ?? "-"}</span>
              </div>
              <div class="detail-item">
                  <span class="detail-label">Voltage:</span>
                  <span class="detail-value">${r.v1 ?? "-"}V</span>
              </div>
              <div class="detail-item">
                  <span class="detail-label">Current:</span>
                  <span class="detail-value">${r.a1 ?? "-"}A</span>
              </div>
              <div class="detail-item">
                  <span class="detail-label">Power:</span>
                  <span class="detail-value">${r.p1 ?? "-"}W</span>
              </div>
          </div>
        `;

        container.appendChild(card);
      });
    })
    .catch(err => console.error("Error loading readings:", err));
}

    // Service Worker Registration
    if ('serviceWorker' in navigator) {
        window.addEventListener('load', () => {
            navigator.serviceWorker.register('sw.js')
                .then(reg => console.log('✅ Service Worker registered:', reg))
                .catch(err => console.error('❌ Service Worker registration failed:', err));
        });
    }

    // PWA Install Functionality
    let deferredPrompt;
    let installBannerDismissed = false;

    window.addEventListener('beforeinstallprompt', (e) => {
        console.log('💡 beforeinstallprompt event fired');
        console.log('User agent:', navigator.userAgent);
        console.log('Platform:', navigator.platform);

        // Prevent Chrome 67 and earlier from automatically showing the prompt
        e.preventDefault();

        // Stash the event so it can be triggered later
        deferredPrompt = e;

        // Show custom install banner after a delay
        if (!installBannerDismissed) {
            setTimeout(() => {
                showInstallBanner();
            }, 3000); // Show after 3 seconds
        }
    });

    // Additional detection for different mobile browsers
    window.addEventListener('load', () => {
        // Check if we're on mobile and show banner even without beforeinstallprompt
        const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
        const isStandalone = window.matchMedia('(display-mode: standalone)').matches;
        const isInstallable = window.navigator.standalone !== undefined || deferredPrompt;

        console.log('Mobile check:', {
            isMobile,
            isStandalone,
            isInstallable,
            userAgent: navigator.userAgent,
            installBannerDismissed
        });

        // For mobile browsers that don't fire beforeinstallprompt
        if (isMobile && !isStandalone && !installBannerDismissed && !deferredPrompt) {
            setTimeout(() => {
                console.log('Showing fallback install banner for mobile');
                showInstallBanner(true); // true = fallback mode
            }, 5000);
        }
    });

    // Handle successful app installation
    window.addEventListener('appinstalled', (evt) => {
        console.log('✅ App was installed successfully');
        hideInstallBanner();
        showToast('App installed successfully!');
    });

    function loadDashboardData(){
    fetch("/api/meter/dashboard")
        .then(response => response.json())
        .then(data => {
            console.log("Data is here ->", data)
        document.getElementById("totalPg").innerHTML = `${data.totalPG}`;
        document.getElementById("totalOwners").innerHTML = `${data.totalOwners}`;
        document.getElementById("totalTenants").innerHTML = `${data.totalTenants}`;
        document.getElementById("totalMeters").innerHTML = `${data.totalMeters}`;
        document.getElementById("totalRooms").innerHTML = `${data.totalRooms}`;
        })
        .catch(error => console.error("Error:", error));
    }
    

    function showInstallBanner(fallbackMode = false) {
        const installBanner = document.getElementById('installBanner');
        if (installBanner && !installBannerDismissed) {
            if (fallbackMode) {
                // For browsers that don't support native install, hide the banner
                // since we want native app installation only
                return;
            }
            installBanner.classList.add('show');
        }
    }

    function hideInstallBanner() {
        const installBanner = document.getElementById('installBanner');
        if (installBanner) {
            installBanner.classList.remove('show');
        }
    }

    function installApp() {
        // Only proceed if we have the native install prompt
        if (!deferredPrompt) {
            console.log('Native install not available');
            hideInstallBanner();
            showToast('App installation not supported in this browser. Please use Chrome or Edge.', 4000);
            return;
        }

        hideInstallBanner();

        // Show the browser's native install prompt
        deferredPrompt.prompt();

        // Wait for the user to respond to the prompt
        deferredPrompt.userChoice.then((choiceResult) => {
            if (choiceResult.outcome === 'accepted') {
                console.log('User accepted the install prompt - app will install as native PWA');
                showToast('App installing... Check your home screen!');
            } else {
                console.log('User dismissed the install prompt');
                showToast('Installation cancelled');
            }
            deferredPrompt = null;
        }).catch((error) => {
            console.log('Error with install prompt:', error);
            showToast('Installation failed. Please try again.');
        });
    }

    function showInstallInstructions() {
        const isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent);
        const isAndroid = /Android/.test(navigator.userAgent);
        const isSamsungBrowser = /SamsungBrowser/.test(navigator.userAgent);
        const isChrome = /Chrome/.test(navigator.userAgent) && !/Edge/.test(navigator.userAgent);

        let instructions = '';
        let duration = 6000;

        if (isIOS) {
            instructions = 'iPhone/iPad: Tap the Share button (□↗) at the bottom, then scroll down and tap "Add to Home Screen"';
            duration = 8000;
        } else if (isSamsungBrowser) {
            instructions = 'Samsung Browser: Tap the menu (☰), then tap "Add page to" → "Home screen"';
        } else if (isAndroid && isChrome) {
            instructions = 'Android Chrome: Tap the menu (⋮) at the top right, then tap "Add to Home screen" or "Install app"';
        } else if (isAndroid) {
            instructions = 'Android: Look for "Add to Home screen" or "Install" in your browser menu (usually ⋮ or ☰)';
        } else {
            instructions = 'Desktop: Look for the install icon (⬇) in your address bar, or check browser menu for "Install" option';
        }

        showToast(instructions, duration);

        // Also show a more persistent modal for mobile users
        if (isIOS || isAndroid) {
            setTimeout(() => showInstallModal(instructions), 1000);
        }
    }

    function showInstallModal(instructions) {
        // Create a more detailed install modal
        const modal = document.createElement('div');
        modal.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0, 0, 0, 0.5);
            backdrop-filter: blur(4px);
            z-index: 10001;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        `;

        const content = document.createElement('div');
        content.style.cssText = `
            background: white;
            border-radius: 16px;
            padding: 24px;
            max-width: 90%;
            width: 400px;
            text-align: center;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
        `;

        content.innerHTML = `
            <div style="font-size: 48px; margin-bottom: 16px;">📱</div>
            <h3 style="color: #333; margin-bottom: 12px; font-size: 18px;">Install This App</h3>
            <p style="color: #666; line-height: 1.4; margin-bottom: 20px; font-size: 14px;">${instructions}</p>
            <button onclick="this.parentElement.parentElement.remove()"
                    style="background: #667eea; color: white; border: none; padding: 12px 24px; border-radius: 8px; font-weight: 600; cursor: pointer;">
                Got It
            </button>
        `;

        modal.appendChild(content);
        document.body.appendChild(modal);

        // Remove modal when clicking outside
        modal.onclick = (e) => {
            if (e.target === modal) {
                modal.remove();
            }
        };
    }

    function showToast(message, duration = 3000) {
        // Create toast element
        const toast = document.createElement('div');
        toast.style.cssText = `
            position: fixed;
            bottom: 100px;
            left: 50%;
            transform: translateX(-50%);
            background: rgba(0, 0, 0, 0.8);
            color: white;
            padding: 12px 20px;
            border-radius: 25px;
            font-size: 14px;
            z-index: 10000;
            max-width: 90%;
            text-align: center;
            backdrop-filter: blur(10px);
            animation: slideInUp 0.3s ease-out;
        `;

        // Add animation keyframes
        if (!document.querySelector('#toast-styles')) {
            const style = document.createElement('style');
            style.id = 'toast-styles';
            style.textContent = `
                @keyframes slideInUp {
                    from { transform: translate(-50%, 100px); opacity: 0; }
                    to { transform: translate(-50%, 0); opacity: 1; }
                }
            `;
            document.head.appendChild(style);
        }

        toast.textContent = message;
        document.body.appendChild(toast);

        // Remove toast after duration
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translate(-50%, 20px)';
            setTimeout(() => {
                document.body.removeChild(toast);
            }, 300);
        }, duration);
    }

    // Check if install banner was previously dismissed
    window.addEventListener('load', () => {
        installBannerDismissed = localStorage.getItem('installBannerDismissed') === 'true';

        // Check if app is already installed
        const isStandalone = window.matchMedia('(display-mode: standalone)').matches;
        if (isStandalone || window.navigator.standalone) {
            console.log('App is running in standalone mode (installed)');
            hideInstallBanner();
            return;
        }

        // Debug info
        const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);

        console.log('PWA Install Debug Info:', {
            isMobile,
            isStandalone,
            installBannerDismissed,
            userAgent: navigator.userAgent,
            platform: navigator.platform,
            hasServiceWorker: 'serviceWorker' in navigator,
            hasManifest: document.querySelector('link[rel="manifest"]') !== null,
            protocol: location.protocol,
            origin: location.origin,
            deferredPromptAvailable: !!deferredPrompt
        });

        // Only show banner if we have native install capability
        // The banner will only appear when beforeinstallprompt fires
        console.log('Waiting for beforeinstallprompt event for native PWA installation');

        // Add a manual test button for debugging (remove in production)
        if (location.hostname === 'localhost' || location.hostname === '127.0.0.1') {
            const debugBtn = document.createElement('button');
// <!--            debugBtn.textContent = 'Reset Install';-->
// <!--            debugBtn.style.cssText = `-->
// <!--                position: fixed;-->
// <!--                top: 200px;-->
// <!--                right: 20px;-->
// <!--                z-index: 9999;-->
// <!--                background: red;-->
// <!--                color: white;-->
// <!--                border: none;-->
// <!--                padding: 10px;-->
// <!--                border-radius: 5px;-->
// <!--                font-size: 12px;-->
// <!--            `;-->
            debugBtn.onclick = () => {
                localStorage.removeItem('installBannerDismissed');
                installBannerDismissed = false;
                if (deferredPrompt) {
                    showInstallBanner();
                } else {
                    showToast('No native install prompt available. Try refreshing or using Chrome.');
                }
            };
            document.body.appendChild(debugBtn);
        }
    });

    // View Management
    function showView(viewId) {
        // Hide all views
        const views = document.querySelectorAll('[id$="View"]');
        views.forEach(view => view.classList.add('hidden'));

        // Show selected view
        document.getElementById(viewId + 'View').classList.remove('hidden');

        // Update navigation
        document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));
        event.target.closest('.nav-item').classList.add('active');

        // Close any open modals
        closeAllModals();
    }

    // Modal Management
    function openModal(modalId) {
        document.getElementById(modalId).style.display = 'block';
        document.body.style.overflow = 'hidden';
    }

    function closeModal(modalId) {
        document.getElementById(modalId).style.display = 'none';
        document.body.style.overflow = 'auto';
    }

    function closeAllModals() {
        document.querySelectorAll('.modal').forEach(modal => {
            modal.style.display = 'none';
        });
        document.body.style.overflow = 'auto';
    }

// Close modals when clicking outside
window.onclick = function(event) {
        if (event.target.classList.contains('modal')) {
            closeAllModals();
        }
}

function parseMeterReadings(oneMeterData) {
    return oneMeterData.map(row => {
        try {
            const parsedReading = JSON.parse(row.reading);
            const liveData = parsedReading.live_data;

            return {
                ...row,
                reading: liveData   // store parsed data separately
            };

        } catch (error) {
            console.error("Parsing error:", error);
            return {
                ...row,
                reading: null
            };
        }
    });
}

async function getOneMeterData(meterId) {
    try {
        const response = await fetch(`/api/meter/${meterId}`);

        if (!response.ok) {
            throw new Error("Failed to fetch meter data");
        }

        const oneMeterData = await response.json();

        // Parse all readings
        const parsedData = parseMeterReadings(oneMeterData);
        return parsedData;

    } catch (error) {
        console.error("Error in getParsedMeterReadings:", error);
        return [];
    }
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

// Meter Details
async function viewMeterDetails(meterId) {
    document.getElementById('modalMeterId').textContent = `Meter ID: ${meterId}`;
    const modalBody = document.getElementById("modalBodyMobile");
    modalBody.innerHTML = "";

            const parsedData = await getOneMeterData(meterId);

            if (!parsedData || parsedData.length === 0) {
                modalBody.innerHTML = "<div>No data available</div>";
                return;
            }

            // Latest reading
            const latest = parsedData[0].reading;

            
            // Current Status Card
            const currentStatusHTML = `
                <div class="mobile-card">
                    <h4 style="margin-bottom: 16px; color: #333;">Current Status</h4>
                    <div class="row-details">
                        <div class="detail-item">
                            <span class="detail-label">Cum kWh:</span>
                            <span class="detail-value">${latest.kwh1}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Voltage:</span>
                            <span class="detail-value">${latest.v1}V</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Current:</span>
                            <span class="detail-value">${latest.a1}A</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Power:</span>
                            <span class="detail-value">${latest.p1}W</span>
                        </div>
                    </div>
                </div>
            `;

            
            // Recent Readings (Last 5)
            
            let recentHTML = `
                <div class="mobile-card">
                    <h4 style="margin-bottom: 16px; color: #333;">Recent Readings</h4>
                    <div style="font-size: 12px; color: #666; line-height: 1.4;">
            `;

            const recentFive = parsedData.slice(0, 5);

            recentFive.forEach((row, index) => {
                if (row.reading) {
                    recentHTML += `
                        <div style="padding: 8px 0; ${index !== recentFive.length - 1 ? "border-bottom: 1px solid #eee;" : ""}">
                            <strong>${formatDateTime(row.createdAt)}:</strong> 
                            ${row.reading.kwh1} kWh, 
                            ${row.reading.v1}V, 
                            ${row.reading.a1}A
                        </div>
                    `;
                }
            });

            recentHTML += `
                    </div>
                </div>
            `;

            
            // Button
            
            const buttonHTML = `
                <div>
                    <button class="btn-mobile btn-primary-mobile" onclick="showFullHistory('${meterId}')">
                        View Full History
                    </button>
                </div>
            `;

            // modalBody.innerHTML = currentStatusHTML + recentHTML + buttonHTML;
            modalBody.innerHTML = currentStatusHTML + recentHTML;

            openModal('meterModal');
}

    // Profile
    function openProfile() {
        openModal('profileModal');
    }

    // Menu
    function openMenu() {
        openModal('menuModal');
    }

    function openProfileFromMenu() {
        closeModal('menuModal');
        setTimeout(() => openModal('profileModal'), 300);
    }

    function openCustomerCare() {
        closeModal('menuModal');
        //showToast('Customer Care: +91-9990160416');
        setTimeout(() => {
            const callModal = document.createElement('div');
            callModal.style.cssText = `
                position: fixed;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: rgba(0, 0, 0, 0.5);
                backdrop-filter: blur(4px);
                z-index: 10001;
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 20px;
            `;

            const content = document.createElement('div');
            content.style.cssText = `
                background: white;
                border-radius: 16px;
                padding: 24px;
                max-width: 90%;
                width: 350px;
                text-align: center;
                box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
            `;

            content.innerHTML = `
                <div style="font-size: 48px; margin-bottom: 16px;">📞</div>
                <h3 style="color: #333; margin-bottom: 12px; font-size: 18px;">Customer Care</h3>
                <p style="color: #666; line-height: 1.4; margin-bottom: 20px; font-size: 14px;">
                    Need help? Contact our support team:
                </p>
                <div style="background: #f3f4f6; padding: 16px; border-radius: 12px; margin-bottom: 20px;">
                    <div style="font-weight: 600; color: #333; margin-bottom: 8px;">Phone</div>
                    <a href="tel:+911234567890" style="color: #667eea; font-size: 18px; text-decoration: none; font-weight: 600;">+91 9990160416</a>
                    <div style="font-weight: 600; color: #333; margin: 16px 0 8px;">Email</div>
                    <a href="mailto:support@ariot.com" style="color: #667eea; text-decoration: none;">info@ariotsolutions.com</a>
                    <div style="font-weight: 600; color: #333; margin: 16px 0 8px;">Hours</div>
                    <div style="color: #666; font-size: 14px;">Mon-Sat: 9:00 AM - 6:00 PM</div>
                </div>
                <button onclick="this.parentElement.parentElement.remove()"
                        style="background: #667eea; color: white; border: none; padding: 12px 24px; border-radius: 8px; font-weight: 600; cursor: pointer; width: 100%;">
                    Close
                </button>
            `;

            callModal.appendChild(content);
            document.body.appendChild(callModal);

            callModal.onclick = (e) => {
                if (e.target === callModal) {
                    callModal.remove();
                }
            };
        }, 100);
    }

    function openSettings() {
        closeModal('menuModal');
        showToast('Settings feature coming soon!');
    }

    function openNotifications() {
        closeModal('menuModal');
        showToast('Notifications settings coming soon!');
    }

    function openReports() {
        closeModal('menuModal');
        showToast('Reports feature coming soon!');
    }

    function openAbout() {
        closeModal('menuModal');
        setTimeout(() => {
            const aboutModal = document.createElement('div');
            aboutModal.style.cssText = `
                position: fixed;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: rgba(0, 0, 0, 0.5);
                backdrop-filter: blur(4px);
                z-index: 10001;
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 20px;
            `;

            const content = document.createElement('div');
            content.style.cssText = `
                background: white;
                border-radius: 16px;
                padding: 24px;
                max-width: 90%;
                width: 350px;
                text-align: center;
                box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
            `;

            content.innerHTML = `
                <div style="font-size: 48px; margin-bottom: 16px;">⚡</div>
                <h3 style="color: #333; margin-bottom: 12px; font-size: 18px;">PAY AS YOU GO</h3>
                <p style="color: #667eea; margin-bottom: 20px; font-weight: 600;">AR IoT Solutions</p>
                <div style="background: #f3f4f6; padding: 16px; border-radius: 12px; margin-bottom: 20px; text-align: left;">
                    <div style="margin-bottom: 12px;">
                        <div style="font-size: 12px; color: #666;">Version</div>
                        <div style="font-weight: 600; color: #333;">1.0.0</div>
                    </div>
                    <div style="margin-bottom: 12px;">
                        <div style="font-size: 12px; color: #666;">Last Updated</div>
                        <div style="font-weight: 600; color: #333;">February 2026</div>
                    </div>
                    <div>
                        <div style="font-size: 12px; color: #666;">Type</div>
                        <div style="font-weight: 600; color: #333;">Progressive Web App</div>
                    </div>
                </div>
                <p style="color: #666; font-size: 13px; line-height: 1.5; margin-bottom: 20px;">
                    Smart energy management system for PG accommodations with real-time monitoring and prepaid billing.
                </p>
                <button onclick="this.parentElement.parentElement.remove()"
                        style="background: #667eea; color: white; border: none; padding: 12px 24px; border-radius: 8px; font-weight: 600; cursor: pointer; width: 100%;">
                    Close
                </button>
            `;

            aboutModal.appendChild(content);
            document.body.appendChild(aboutModal);

            aboutModal.onclick = (e) => {
                if (e.target === aboutModal) {
                    aboutModal.remove();
                }
            };
        }, 100);
    }

    function logoutFromMenu() {
        //closeModal('menuModal');
        logout();
    }

    // Add Menu
    function openAddMenu() {
        openModal('addMenuModal');
    }

    function openAddForm(type) {
        // Hide add menu
        closeModal('addMenuModal');

        // Show form modal
        document.getElementById('addFormTitle').textContent = `Add New ${type.charAt(0).toUpperCase() + type.slice(1)}`;

        // Hide all forms
        document.querySelectorAll('[id$="Form"]').forEach(form => form.classList.add('hidden'));

        // Show specific form
        document.getElementById(type + 'Form').classList.remove('hidden');

        openModal('addFormModal');
    }

document
  .getElementById("ownerForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const loader = document.getElementById("ownerLoading");
    
     loader.classList.remove("hidden");

    const data = {
      ownerName: document.getElementById("ownerName").value,
      ownerMobile: document.getElementById("ownerMobile").value,
      ownerEmail: document.getElementById("ownerEmail").value,
      ownerAddress: document.getElementById("ownerAddress").value,
    };
    
    try {
      const response = await fetch("/api/meter/add_owner", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      const respObj = await response.json();
      if (response.ok) {
        document.getElementById("ownerForm").reset();
      } else {
        console.log(respObj.message)
      }
    } catch (err) {
      console.error(err);
    } finally {
       loader.classList.add("hidden");
    }
  });

document
  .getElementById("pgForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const loader = document.getElementById("pgLoading");

    // Show loading...
    loader.classList.remove("hidden");

    const data = {
      pgName: document.getElementById("pgName").value,
      pgOwnerId: document.getElementById("pgOwnerId").value,
      pgAddress: document.getElementById("pgAddress").value,
    };

    try {
      const response = await fetch("/api/meter/add_pg", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      const respObj = await response.json();
      console.log(respObj.message);
      if (response.ok) {
        // Clear form
        document.getElementById("pgForm").reset();
      } else {
        console.log(respObj.message);
      }
    } catch (err) {
      console.error(err);
    } finally {
      loader.classList.add("hidden");
    }
});

function addEventListenerToADDROOMDropDown(){
    const ownerDropdown = document.getElementById("ownerId");
    const pgDropdown = document.getElementById("roomPG");

    ownerDropdown.addEventListener("change", function () {
        console.log("ownerDropdown", ownerDropdown);
        console.log("Changed");
        const ownerId = this.value;
        console.log("Owner Id: => ",ownerId);
        // Reset PG dropdown
        pgDropdown.innerHTML = '<option value="">Select PG</option>';

        if (!ownerId) return;

        // Show loading
        pgDropdown.innerHTML = '<option value="">Loading...</option>';

        fetch(`/api/meter/pg/by-owner/${ownerId}`)
            .then(response => {
                if (!response.ok) {
                    console.log(response)
                    throw new Error("Failed to fetch PGs");
                }
                return response.json();
            })
            .then(data => {
                pgDropdown.innerHTML = '<option value="">Select PG</option>';

                if (data.length === 0) {
                    pgDropdown.disabled = true;
                    pgDropdown.innerHTML = 
                        '<option>No PG found</option>';
                    return;
                }

                data.forEach(pg => {
                    const option = document.createElement("option");
                    option.value = pg.id;
                    option.textContent = pg.name;
                    pgDropdown.appendChild(option);
                });

                pgDropdown.disabled = false;
            })
            .catch(error => {
                console.error("Error:", error);
                pgDropdown.innerHTML =
                    '<option disabled>Error loading PGs</option>';
            });

    });

}

function showPGsAsPerOwner(id1, id2){
    const ownerDropdown = document.getElementById(id1);
    const pgDropdown = document.getElementById(id2);

    ownerDropdown.addEventListener("change", function () {
        console.log("ownerDropdown", ownerDropdown);
        console.log("Changed");
        const ownerId = this.value;
        console.log("Owner Id: => ",ownerId);
        // Reset PG dropdown
        pgDropdown.innerHTML = '<option value="">Select PG</option>';

        if (!ownerId) return;

        // Show loading
        pgDropdown.innerHTML = '<option value="">Loading...</option>';

        fetch(`/api/meter/pg/by-owner/${ownerId}`)
            .then(response => {
                if (!response.ok) {
                    console.log(response)
                    throw new Error("Failed to fetch PGs");
                }
                return response.json();
            })
            .then(data => {
                pgDropdown.innerHTML = '<option value="">Select PG</option>';

                if (data.length === 0) {
                    pgDropdown.disabled = true;
                    pgDropdown.innerHTML = 
                        '<option>No PG found</option>';
                    return;
                }

                data.forEach(pg => {
                    const option = document.createElement("option");
                    option.value = pg.id;
                    option.textContent = pg.name;
                    pgDropdown.appendChild(option);
                });

                pgDropdown.disabled = false;
            })
            .catch(error => {
                console.error("Error:", error);
                pgDropdown.innerHTML =
                    '<option disabled>Error loading PGs</option>';
            });

    });

}

// this function is fetching rooms using api by using PG Id and showing options while adding tenant.
function showRoomsAsPerPG(id1, id2){
    const pgDropdown = document.getElementById(id1);
    const roomDropdown = document.getElementById(id2);

    pgDropdown.addEventListener("change", function () {
        console.log("pgDropdown->", pgDropdown);
        console.log("Changed");
        const pgId = this.value;
        console.log("PG Id: => ",pgId);
        // Reset PG dropdown
        roomDropdown.innerHTML = '<option value="">Select PG</option>';

        if (!pgId){
            console.log("PG Id not found!");
            return;
        }

        // Show loading
        roomDropdown.innerHTML = '<option value="">Loading... is</option>';

        fetch(`/api/meter/rooms/by-pg/${pgId}`)
            .then(response => {
                if (!response.ok) {
                    console.log(response)
                    throw new Error("Failed to fetch Rooms");
                }
                return response.json();
            })
            .then(data => {
                console.log("This is Data", data);
                roomDropdown.innerHTML = '<option value="">Select Room</option>';
                if (data.length === 0) {
                    roomDropdown.disabled = true;
                    roomDropdown.innerHTML = 
                        '<option>No Room found</option>';
                    return;
                }

                data.forEach(room => {
                    console.log("Room:",room);
                    const option = document.createElement("option");
                    option.value = room.id;
                    option.textContent = room.roomNo + "  (" + room.status + ")";
                    roomDropdown.appendChild(option);
                });

                roomDropdown.disabled = false;
            })
            .catch(error => {
                console.error("Error:", error);
                roomDropdown.innerHTML =
                    '<option disabled>Error loading Rooms</option>';
            });

    });
}

document.addEventListener("DOMContentLoaded", function () {
    loadDashboardData();
    addEventListenerToADDROOMDropDown();
    showPGsAsPerOwner("tenantOwnerId", "tenantPG");
    showPGsAsPerOwner("meterOwnerId", "meterPG");
    showRoomsAsPerPG("tenantPG", "tenantRoom");
    showRoomsAsPerPG("meterPG", "meterRoom");
});

document
  .getElementById("roomForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const loader = document.getElementById("roomLoading");

    // show loading
    loader.classList.remove("hidden");

    const data = {
      roomNumber: document.getElementById("roomNumber").value,
      roomPG: document.getElementById("roomPG").value,
    };

    try {
      const response = await fetch("/api/meter/add_room", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      const respObj = await response.json();
      if (response.ok) {

        document.getElementById("roomForm").reset();
        const afterMsg = document.getElementById("afterMessage");
        afterMsg.textContent = respObj.message;

        setTimeout(() => {
            afterMsg.innerText = "";
        }, 3000);

      } else {
        console.log(respObj.message);
      }
    } catch (err) {
      console.error(err);
    } finally {
      loader.classList.add("hidden");
    }
});

document
  .getElementById("tenantForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const loader = document.getElementById("tenantLoading");

    // show loading
    loader.classList.remove("hidden");

    const data = {
      tenantName: document.getElementById("tenantName").value,
      tenantMobile: document.getElementById("tenantPhone").value,
      tenantEmail: document.getElementById("tenantEmail").value,
      tenantAddress: document.getElementById("tenantAddress").value,
      tenantRoom: document.getElementById("tenantRoom").value,
    };
    console.log("This is tenant Data: =>",data);
    try {
      const response = await fetch("/api/meter/add_tenant", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      const respObj = await response.json();
      const afterMsg = document.getElementById("tenantMessage");
      if (response.ok) {

        document.getElementById("tenantForm").reset();
        afterMsg.textContent = respObj.message;

        setTimeout(() => {
            afterMsg.innerText = "";
        }, 3000);

      } else {
        console.log(respObj.message);
        afterMsg.textContent = respObj.message;
        setTimeout(() => {
            afterMsg.innerText = "";
        }, 3000);
      }
    } catch (err) {
      console.error(err);
    } finally {
      loader.classList.add("hidden");
    }
});

document
  .getElementById("meterForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const loader = document.getElementById("meterLoading");
    const  roomId = document.getElementById("meterRoom").value;
    // show loading
    loader.classList.remove("hidden");

    const data = {
      serialNo: document.getElementById("meterSerialNo").value,
      installationDate: document.getElementById("installationDate").value,
      type: parseInt(document.getElementById("meterType").value)
    };
    console.log("This is meterForm Data: =>",data);
    try {
      const response = await fetch(`api/meter/add_meter/${roomId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      const respObj = await response.json();
      if (response.ok) {

        document.getElementById("meterForm").reset();
        const afterMsg = document.getElementById("meterMessage");
        afterMsg.textContent = respObj.message;

        setTimeout(() => {
            afterMsg.innerText = "";
        }, 3000);

      } else {
        console.log(respObj.message);
      }
    } catch (err) {
      console.error(err);
    } finally {
      loader.classList.add("hidden");
    }
});


    
    // Transaction Details
    function viewTransaction(txnId) {
        alert(`Viewing transaction: ${txnId}`);
    }

    // Filtering
    function filterLiveData(filter) {
        const rows = document.querySelectorAll('#liveDataView .table-row-mobile');
        rows.forEach(row => {
            row.style.display = 'none';

            if (filter === 'all' ||
                (filter === 'connected' && row.classList.contains('connected')) ||
                (filter === 'disconnected' && row.classList.contains('disconnected')) ||
                (filter === 'online' && row.classList.contains('online')) ||
                (filter === 'offline' && row.classList.contains('offline'))) {
                row.style.display = 'block';
            }
        });

        // Update active tab
        updateActiveFilterTab('#liveDataView', filter);
    }

    function filterRechargeData(filter) {
        const rows = document.querySelectorAll('#rechargeView .table-row-mobile');
        rows.forEach(row => {
            row.style.display = 'none';
            if (filter === 'all' || row.classList.contains(filter)) {
                row.style.display = 'block';
            }
        });

        // Update active tab
        updateActiveFilterTab('#rechargeView', filter);
    }

    function updateActiveFilterTab(viewSelector, filter) {
        const tabs = document.querySelectorAll(`${viewSelector} .filter-tab-mobile`);
        tabs.forEach(tab => {
            tab.classList.remove('active');
            if (tab.textContent.toLowerCase().includes(filter) ||
                (filter === 'all' && tab.textContent.toLowerCase() === 'all')) {
                tab.classList.add('active');
            }
        });
    }

    // Additional Functions
    function showFullHistory() {
        alert('Opening full meter history...');
        closeModal('meterModal');
    }

function logout() {

    const message = document.createElement("div");
    message.innerText = "Logging out...";

    message.style.position = "fixed";
    message.style.top = "20px";
    message.style.right = "20px";
    message.style.padding = "12px 24px";
    message.style.backgroundColor = "#ffffff";         
    message.style.color = "#EF4444";                      
    message.style.borderRadius = "10px";                
    message.style.boxShadow = "0 4px 12px rgba(0,0,0,0.1)";
    message.style.zIndex = "9999";

    document.body.appendChild(message);

    window.location.href = '/';
    
}

    // // Pull to Refresh
    // let startY = 0;
    // let currentY = 0;
    // let isRefreshing = false;

    document.addEventListener('touchstart', function(e) {
        if (window.scrollY === 0 && !isRefreshing) {
            startY = e.touches[0].pageY;
        }
    });

    document.addEventListener('touchmove', function(e) {
        if (window.scrollY === 0 && !isRefreshing) {
            currentY = e.touches[0].pageY;
            const diff = currentY - startY;

            if (diff > 0 && diff < 100) {
                const pullToRefresh = document.getElementById('pullToRefresh');
                pullToRefresh.style.transform = `translateY(${diff}px)`;
                pullToRefresh.style.opacity = diff / 60;

                if (diff > 60) {
                    pullToRefresh.textContent = '↑ Release to refresh';
                } else {
                    pullToRefresh.textContent = '↓ Pull to refresh';
                }
            }
        }
    });

    document.addEventListener('touchend', function(e) {
        if (window.scrollY === 0 && !isRefreshing) {
            const diff = currentY - startY;
            const pullToRefresh = document.getElementById('pullToRefresh');

            if (diff > 60) {
                isRefreshing = true;
                pullToRefresh.textContent = '🔄 Refreshing...';
                pullToRefresh.classList.add('show');

                setTimeout(() => {
                    pullToRefresh.classList.remove('show');
                    pullToRefresh.style.transform = 'translateY(0)';
                    pullToRefresh.style.opacity = '0';
                    isRefreshing = false;
                    alert('Data refreshed!');
                }, 2000);
            } else {
                pullToRefresh.style.transform = 'translateY(0)';
                pullToRefresh.style.opacity = '0';
            }
        }
    });

    // Prevent zoom on double tap
    // let lastTouchEnd = 0;
    // document.addEventListener('touchend', function(event) {
    //     const now = (new Date()).getTime();
    //     if (now - lastTouchEnd <= 300) {
    //         event.preventDefault();
    //     }
    //     lastTouchEnd = now;
    // }, false);

    // Initialize app
    document.addEventListener('DOMContentLoaded', function() {
        console.log('Mobile Admin PWA initialized');
    });


    // Service Worker Registration
    if ('serviceWorker' in navigator) {
        window.addEventListener('load', () => {
            navigator.serviceWorker.register('/service-worker.js')
                .then(reg => console.log('ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ Service Worker registered:', reg))
                .catch(err => console.error('ÃƒÂ¢Ã‚ÂÃ…â€™ Service Worker registration failed:', err));
        });
    }
    // PWA Install Functionality
    // let deferredPrompt;
    // let installBannerDismissed = false;
    function isStandaloneMode() {
        return window.matchMedia('(display-mode: standalone)').matches || window.navigator.standalone === true;
    }
    function isAppInstalled() {
        return localStorage.getItem('pwaInstalled') === 'true' || isStandaloneMode();
    }
    function markAppInstalled(installed) {
        localStorage.setItem('pwaInstalled', installed ? 'true' : 'false');
    }
    window.addEventListener('beforeinstallprompt', (e) => {
        console.log('beforeinstallprompt event fired');
        console.log('User agent:', navigator.userAgent);
        console.log('Platform:', navigator.platform);
        e.preventDefault();
        deferredPrompt = e;
        markAppInstalled(false);
        if (!installBannerDismissed && !isAppInstalled()) {
            setTimeout(() => {
                showInstallBanner();
            }, 3000);
        }
    });
    window.addEventListener('appinstalled', () => {
        console.log('App was installed successfully');
        deferredPrompt = null;
        markAppInstalled(true);
        localStorage.removeItem('installBannerDismissed');
        installBannerDismissed = false;
        hideInstallBanner();
        showToast('App installed successfully!');
    });
    function showInstallBanner(fallbackMode = false) {
        const installBanner = document.getElementById('installBanner');
        if (!installBanner || installBannerDismissed || isAppInstalled()) {
            return;
        }
        const subtitle = installBanner.querySelector('.install-subtitle');
        if (subtitle) {
            subtitle.textContent = fallbackMode
                ? 'Open browser menu and choose "Add to Home screen"'
                : 'Add to your home screen for better experience';
        }
        installBanner.classList.add('show');
    }
    function hideInstallBanner() {
        const installBanner = document.getElementById('installBanner');
        if (installBanner) {
            installBanner.classList.remove('show');
        }
    }
    function dismissInstallBanner() {
        installBannerDismissed = true;
        localStorage.setItem('installBannerDismissed', 'true');
        hideInstallBanner();
    }
    function installApp() {
        if (!deferredPrompt) {
            console.log('Native install not available');
            hideInstallBanner();
            if (!isAppInstalled()) {
                showToast('App installation not supported in this browser. Please use Chrome or Edge.', 4000);
            }
            return;
        }
        hideInstallBanner();
        deferredPrompt.prompt();
        deferredPrompt.userChoice.then((choiceResult) => {
            if (choiceResult.outcome === 'accepted') {
                console.log('User accepted the install prompt - app will install as native PWA');
                markAppInstalled(true);
                showToast('App installing... Check your home screen!');
            } else {
                console.log('User dismissed the install prompt');
                markAppInstalled(false);
                showToast('Installation cancelled');
                showInstallBanner();
            }
            deferredPrompt = null;
        }).catch((error) => {
            console.log('Error with install prompt:', error);
            markAppInstalled(false);
            showToast('Installation failed. Please try again.');
        });
    }
    function showInstallInstructions() {
        const isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent);
        const isAndroid = /Android/.test(navigator.userAgent);
        const isSamsungBrowser = /SamsungBrowser/.test(navigator.userAgent);
        const isChrome = /Chrome/.test(navigator.userAgent) && !/Edge/.test(navigator.userAgent);

        let instructions = '';
        let duration = 6000;

        if (isIOS) {
            instructions = 'iPhone/iPad: Tap the Share button (ÃƒÂ¢Ã¢â‚¬â€œÃ‚Â¡ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â€) at the bottom, then scroll down and tap "Add to Home Screen"';
            duration = 8000;
        } else if (isSamsungBrowser) {
            instructions = 'Samsung Browser: Tap the menu (ÃƒÂ¢Ã‹Å“Ã‚Â°), then tap "Add page to" ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ "Home screen"';
        } else if (isAndroid && isChrome) {
            instructions = 'Android Chrome: Tap the menu (ÃƒÂ¢Ã¢â‚¬Â¹Ã‚Â®) at the top right, then tap "Add to Home screen" or "Install app"';
        } else if (isAndroid) {
            instructions = 'Android: Look for "Add to Home screen" or "Install" in your browser menu (usually ÃƒÂ¢Ã¢â‚¬Â¹Ã‚Â® or ÃƒÂ¢Ã‹Å“Ã‚Â°)';
        } else {
            instructions = 'Desktop: Look for the install icon (ÃƒÂ¢Ã‚Â¬Ã¢â‚¬Â¡) in your address bar, or check browser menu for "Install" option';
        }

        showToast(instructions, duration);

        // Also show a more persistent modal for mobile users
        if (isIOS || isAndroid) {
            setTimeout(() => showInstallModal(instructions), 1000);
        }
    }

    function showInstallModal(instructions) {
        // Create a more detailed install modal
        const modal = document.createElement('div');
        modal.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0, 0, 0, 0.5);
            backdrop-filter: blur(4px);
            z-index: 10001;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        `;

        const content = document.createElement('div');
        content.style.cssText = `
            background: white;
            border-radius: 16px;
            padding: 24px;
            max-width: 90%;
            width: 400px;
            text-align: center;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
        `;

        content.innerHTML = `
            <div style="font-size: 48px; margin-bottom: 16px;">ÃƒÂ°Ã…Â¸Ã¢â‚¬Å“Ã‚Â±</div>
            <h3 style="color: #333; margin-bottom: 12px; font-size: 18px;">Install This App</h3>
            <p style="color: #666; line-height: 1.4; margin-bottom: 20px; font-size: 14px;">${instructions}</p>
            <button onclick="this.parentElement.parentElement.remove()"
                    style="background: #667eea; color: white; border: none; padding: 12px 24px; border-radius: 8px; font-weight: 600; cursor: pointer;">
                Got It
            </button>
        `;

        modal.appendChild(content);
        document.body.appendChild(modal);

        // Remove modal when clicking outside
        modal.onclick = (e) => {
            if (e.target === modal) {
                modal.remove();
            }
        };
    }

    function showToast(message, duration = 3000) {
        // Create toast element
        const toast = document.createElement('div');
        toast.style.cssText = `
            position: fixed;
            bottom: 100px;
            left: 50%;
            transform: translateX(-50%);
            background: rgba(0, 0, 0, 0.8);
            color: white;
            padding: 12px 20px;
            border-radius: 25px;
            font-size: 14px;
            z-index: 10000;
            max-width: 90%;
            text-align: center;
            backdrop-filter: blur(10px);
            animation: slideInUp 0.3s ease-out;
        `;

        // Add animation keyframes
        if (!document.querySelector('#toast-styles')) {
            const style = document.createElement('style');
            style.id = 'toast-styles';
            style.textContent = `
                @keyframes slideInUp {
                    from { transform: translate(-50%, 100px); opacity: 0; }
                    to { transform: translate(-50%, 0); opacity: 1; }
                }
            `;
            document.head.appendChild(style);
        }

        toast.textContent = message;
        document.body.appendChild(toast);

        // Remove toast after duration
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translate(-50%, 20px)';
            setTimeout(() => {
                document.body.removeChild(toast);
            }, 300);
        }, duration);
    }
    // Check if install banner was previously dismissed
    window.addEventListener('load', () => {
        installBannerDismissed = localStorage.getItem('installBannerDismissed') === 'true';
        const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
        const isStandalone = isStandaloneMode();
        const isInstallable = window.navigator.standalone !== undefined || deferredPrompt;
        if (isStandalone) {
            console.log('App is running in standalone mode (installed)');
            markAppInstalled(true);
            hideInstallBanner();
            return;
        }
        console.log('PWA Install Debug Info:', {
            isMobile,
            isStandalone,
            isInstallable,
            installBannerDismissed,
            pwaInstalled: localStorage.getItem('pwaInstalled') === 'true',
            userAgent: navigator.userAgent,
            platform: navigator.platform,
            hasServiceWorker: 'serviceWorker' in navigator,
            hasManifest: document.querySelector('link[rel="manifest"]') !== null,
            protocol: location.protocol,
            origin: location.origin,
            deferredPromptAvailable: !!deferredPrompt
        });
        console.log('Waiting for beforeinstallprompt event for native PWA installation');
        if (isMobile && !installBannerDismissed && !deferredPrompt && !isAppInstalled()) {
            setTimeout(() => {
                console.log('Showing fallback install banner for mobile');
                showInstallBanner(true);
            }, 5000);
        }
        if (location.hostname === 'localhost' || location.hostname === '127.0.0.1') {
            const debugBtn = document.createElement('button');
            debugBtn.onclick = () => {
                localStorage.removeItem('installBannerDismissed');
                localStorage.removeItem('pwaInstalled');
                installBannerDismissed = false;
                if (deferredPrompt) {
                    showInstallBanner();
                } else {
                    showToast('No native install prompt available. Try refreshing or using Chrome.');
                }
            };
            document.body.appendChild(debugBtn);
        }
    });
    // View Management
    function showView(viewId) {
        // Hide all views
        const views = document.querySelectorAll('[id$="View"]');
        views.forEach(view => view.classList.add('hidden'));

        // Show selected view
        document.getElementById(viewId + 'View').classList.remove('hidden');

        // Update navigation
        document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));
        event.target.closest('.nav-item').classList.add('active');

        // Close any open modals
        closeAllModals();
    }

    // Modal Management
    function openModal(modalId) {
        document.getElementById(modalId).style.display = 'block';
        document.body.style.overflow = 'hidden';
    }

    function closeModal(modalId) {
        document.getElementById(modalId).style.display = 'none';
        document.body.style.overflow = 'auto';
    }

    function closeAllModals() {
        document.querySelectorAll('.modal').forEach(modal => {
            modal.style.display = 'none';
        });
        document.body.style.overflow = 'auto';
    }

    // Close modals when clicking outside
    window.onclick = function(event) {
        if (event.target.classList.contains('modal')) {
            closeAllModals();
        }
    }

    // Meter Details
    function viewMeterDetails(meterId) {
        document.getElementById('modalMeterId').textContent = `Meter ID: ${meterId}`;
        openModal('meterModal');
    }

    // Profile
    function openProfile() {
        openModal('profileModal');
    }

    // Menu
    function openMenu() {
        openModal('menuModal');
    }

    function openProfileFromMenu() {
        closeModal('menuModal');
        setTimeout(() => openModal('profileModal'), 300);
    }

    function openCustomerCare() {
        closeModal('menuModal');
        showToast('Customer Care: +91-1234567890');
        setTimeout(() => {
            const callModal = document.createElement('div');
            callModal.style.cssText = `
                position: fixed;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: rgba(0, 0, 0, 0.5);
                backdrop-filter: blur(4px);
                z-index: 10001;
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 20px;
            `;

            const content = document.createElement('div');
            content.style.cssText = `
                background: white;
                border-radius: 16px;
                padding: 24px;
                max-width: 90%;
                width: 350px;
                text-align: center;
                box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
            `;

            content.innerHTML = `
                <div style="font-size: 48px; margin-bottom: 16px;">ÃƒÂ°Ã…Â¸Ã¢â‚¬Å“Ã…Â¾</div>
                <h3 style="color: #333; margin-bottom: 12px; font-size: 18px;">Customer Care</h3>
                <p style="color: #666; line-height: 1.4; margin-bottom: 20px; font-size: 14px;">
                    Need help? Contact our support team:
                </p>
                <div style="background: #f3f4f6; padding: 16px; border-radius: 12px; margin-bottom: 20px;">
                    <div style="font-weight: 600; color: #333; margin-bottom: 8px;">Phone</div>
                    <a href="tel:+911234567890" style="color: #667eea; font-size: 18px; text-decoration: none; font-weight: 600;">+91-1234567890</a>
                    <div style="font-weight: 600; color: #333; margin: 16px 0 8px;">Email</div>
                    <a href="mailto:support@ariot.com" style="color: #667eea; text-decoration: none;">support@ariot.com</a>
                    <div style="font-weight: 600; color: #333; margin: 16px 0 8px;">Hours</div>
                    <div style="color: #666; font-size: 14px;">Mon-Sat: 9:00 AM - 6:00 PM</div>
                </div>
                <button onclick="this.parentElement.parentElement.remove()"
                        style="background: #667eea; color: white; border: none; padding: 12px 24px; border-radius: 8px; font-weight: 600; cursor: pointer; width: 100%;">
                    Close
                </button>
            `;

            callModal.appendChild(content);
            document.body.appendChild(callModal);

            callModal.onclick = (e) => {
                if (e.target === callModal) {
                    callModal.remove();
                }
            };
        }, 100);
    }

    function openSettings() {
        closeModal('menuModal');
        showToast('Settings feature coming soon!');
    }

    function openNotifications() {
        closeModal('menuModal');
        showToast('Notifications settings coming soon!');
    }

    function openReports() {
        closeModal('menuModal');
        showToast('Reports feature coming soon!');
    }

    function openAbout() {
        closeModal('menuModal');
        setTimeout(() => {
            const aboutModal = document.createElement('div');
            aboutModal.style.cssText = `
                position: fixed;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: rgba(0, 0, 0, 0.5);
                backdrop-filter: blur(4px);
                z-index: 10001;
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 20px;
            `;

            const content = document.createElement('div');
            content.style.cssText = `
                background: white;
                border-radius: 16px;
                padding: 24px;
                max-width: 90%;
                width: 350px;
                text-align: center;
                box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
            `;

            content.innerHTML = `
                <div style="font-size: 48px; margin-bottom: 16px;">ÃƒÂ¢Ã…Â¡Ã‚Â¡</div>
                <h3 style="color: #333; margin-bottom: 12px; font-size: 18px;">PAY AS YOU GO</h3>
                <p style="color: #667eea; margin-bottom: 20px; font-weight: 600;">AR IoT Solutions</p>
                <div style="background: #f3f4f6; padding: 16px; border-radius: 12px; margin-bottom: 20px; text-align: left;">
                    <div style="margin-bottom: 12px;">
                        <div style="font-size: 12px; color: #666;">Version</div>
                        <div style="font-weight: 600; color: #333;">1.0.0</div>
                    </div>
                    <div style="margin-bottom: 12px;">
                        <div style="font-size: 12px; color: #666;">Last Updated</div>
                        <div style="font-weight: 600; color: #333;">February 2026</div>
                    </div>
                    <div>
                        <div style="font-size: 12px; color: #666;">Type</div>
                        <div style="font-weight: 600; color: #333;">Progressive Web App</div>
                    </div>
                </div>
                <p style="color: #666; font-size: 13px; line-height: 1.5; margin-bottom: 20px;">
                    Smart energy management system for PG accommodations with real-time monitoring and prepaid billing.
                </p>
                <button onclick="this.parentElement.parentElement.remove()"
                        style="background: #667eea; color: white; border: none; padding: 12px 24px; border-radius: 8px; font-weight: 600; cursor: pointer; width: 100%;">
                    Close
                </button>
            `;

            aboutModal.appendChild(content);
            document.body.appendChild(aboutModal);

            aboutModal.onclick = (e) => {
                if (e.target === aboutModal) {
                    aboutModal.remove();
                }
            };
        }, 100);
    }

    function logoutFromMenu() {
        closeModal('menuModal');
        logout();
    }

    // Add Menu
    function openAddMenu() {
        openModal('addMenuModal');
    }

    function openAddForm(type) {
        // Hide add menu
        closeModal('addMenuModal');

        // Show form modal
        document.getElementById('addFormTitle').textContent = `Add New ${type.charAt(0).toUpperCase() + type.slice(1)}`;

        // Hide all forms
        document.querySelectorAll('[id$="Form"]').forEach(form => form.classList.add('hidden'));

        // Show specific form
        document.getElementById(type + 'Form').classList.remove('hidden');

        openModal('addFormModal');
    }

    function submitForm(type) {
        alert(`${type.charAt(0).toUpperCase() + type.slice(1)} added successfully!`);
        closeModal('addFormModal');
    }

    // Transaction Details
    function viewTransaction(txnId) {
        alert(`Viewing transaction: ${txnId}`);
    }

    // Filtering
    function filterLiveData(filter) {
        const rows = document.querySelectorAll('#liveDataView .table-row-mobile');
        rows.forEach(row => {
            row.style.display = 'none';

            if (filter === 'all' ||
                (filter === 'connected' && row.classList.contains('connected')) ||
                (filter === 'disconnected' && row.classList.contains('disconnected')) ||
                (filter === 'online' && row.classList.contains('online')) ||
                (filter === 'offline' && row.classList.contains('offline'))) {
                row.style.display = 'block';
            }
        });

        // Update active tab
        updateActiveFilterTab('#liveDataView', filter);
    }

    function filterRechargeData(filter) {
        const rows = document.querySelectorAll('#rechargeView .table-row-mobile');
        rows.forEach(row => {
            row.style.display = 'none';
            if (filter === 'all' || row.classList.contains(filter)) {
                row.style.display = 'block';
            }
        });

        // Update active tab
        updateActiveFilterTab('#rechargeView', filter);
    }

    function updateActiveFilterTab(viewSelector, filter) {
        const tabs = document.querySelectorAll(`${viewSelector} .filter-tab-mobile`);
        tabs.forEach(tab => {
            tab.classList.remove('active');
            if (tab.textContent.toLowerCase().includes(filter) ||
                (filter === 'all' && tab.textContent.toLowerCase() === 'all')) {
                tab.classList.add('active');
            }
        });
    }

    // Additional Functions
    function showFullHistory() {
        alert('Opening full meter history...');
        closeModal('meterModal');
    }

    function logout() {
        if (confirm('Are you sure you want to logout?')) {
            alert('Logging out...');
            closeAllModals();
        }
    }

    // Pull to Refresh
    let startY = 0;
    let currentY = 0;
    let isRefreshing = false;

    document.addEventListener('touchstart', function(e) {
        if (window.scrollY === 0 && !isRefreshing) {
            startY = e.touches[0].pageY;
        }
    });

    document.addEventListener('touchmove', function(e) {
        if (window.scrollY === 0 && !isRefreshing) {
            currentY = e.touches[0].pageY;
            const diff = currentY - startY;

            if (diff > 0 && diff < 100) {
                const pullToRefresh = document.getElementById('pullToRefresh');
                pullToRefresh.style.transform = `translateY(${diff}px)`;
                pullToRefresh.style.opacity = diff / 60;

                if (diff > 60) {
                    pullToRefresh.textContent = 'ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Ëœ Release to refresh';
                } else {
                    pullToRefresh.textContent = 'ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Å“ Pull to refresh';
                }
            }
        }
    });

    document.addEventListener('touchend', function(e) {
        if (window.scrollY === 0 && !isRefreshing) {
            const diff = currentY - startY;
            const pullToRefresh = document.getElementById('pullToRefresh');

            if (diff > 60) {
                isRefreshing = true;
                pullToRefresh.textContent = 'ÃƒÂ°Ã…Â¸Ã¢â‚¬ÂÃ¢â‚¬Å¾ Refreshing...';
                pullToRefresh.classList.add('show');

                setTimeout(() => {
                    pullToRefresh.classList.remove('show');
                    pullToRefresh.style.transform = 'translateY(0)';
                    pullToRefresh.style.opacity = '0';
                    isRefreshing = false;
                    alert('Data refreshed!');
                }, 2000);
            } else {
                pullToRefresh.style.transform = 'translateY(0)';
                pullToRefresh.style.opacity = '0';
            }
        }
    });

    // Prevent zoom on double tap
    let lastTouchEnd = 0;
    document.addEventListener('touchend', function(event) {
        const now = (new Date()).getTime();
        if (now - lastTouchEnd <= 300) {
            event.preventDefault();
        }
        lastTouchEnd = now;
    }, false);

    // Initialize app
    document.addEventListener('DOMContentLoaded', function() {
        console.log('Mobile Admin PWA initialized');
    });










    // There was already code for the meter_management_mobile written in this file, 
    // but the actual working code was in the script tag in the html file, so
    // I commented this below code and move that code from script tag to this js file.





















// ✅ Ensure Service Worker is registered if script is loaded directly
// if ("serviceWorker" in navigator) {
//     navigator.serviceWorker.register("sw.js").catch(err =>
//         console.error("Service Worker registration failed:", err)
//     );
// }

// function showView(viewId) {
//     // Hide all views
//     const views = document.querySelectorAll('.content-body > div');
//     views.forEach(view => view.classList.add('hidden'));

//     // Show the selected view
//     const viewToShow = document.getElementById(viewId + 'View');
//     if (viewToShow) {
//         viewToShow.classList.remove('hidden');
//     }

//     // Update the page title
//     const contentTitle = document.getElementById('contentTitle');
//     contentTitle.innerText = viewId
//         .replace(/([A-Z])/g, ' $1')
//         .replace(/^./, str => str.toUpperCase());

//     // Highlight the active sidebar nav link
//     const navLinks = document.querySelectorAll('.nav-menu .nav-link');
//     navLinks.forEach(link => link.classList.remove('active'));

//     // Match the link with correct onclick call
//     const activeLink = Array.from(navLinks).find(link => link.getAttribute('onclick')?.includes(`showView('${viewId}')`));
//     if (activeLink) {
//         activeLink.classList.add('active');
//     }
// }


// function filterLiveData(filter) {
//     const rows = document.querySelectorAll('#liveDataView table tbody tr');
//     rows.forEach(row => {
//         row.style.display = 'none';

//         const isConnected = row.classList.contains('connected');
//         const isDisconnected = row.classList.contains('disconnected');
//         const isOnline = row.classList.contains('online');
//         const isOffline = row.classList.contains('offline');

//         if (filter === 'all' ||
//             (filter === 'connected' && isConnected) ||
//             (filter === 'disconnected' && isDisconnected) ||
//             (filter === 'online' && isOnline) ||
//             (filter === 'offline' && isOffline)) {
//             row.style.display = '';
//         }
//     });

//     updateActiveTab('#liveDataView', filter);
// }

// function filterRechargeData(filter) {
//     const rows = document.querySelectorAll('#rechargeView table tbody tr');
//     rows.forEach(row => {
//         row.style.display = 'none';
//         if (filter === 'all' || row.classList.contains(filter)) {
//             row.style.display = '';
//         }
//     });

//     updateActiveTab('#rechargeView', filter);
// }

// function updateActiveTab(viewSelector, filter) {
//     const tabs = document.querySelectorAll(`${viewSelector} .filter-tab`);
//     tabs.forEach(tab => {
//         tab.classList.remove('active');
//         if (tab.innerText.toLowerCase().includes(filter)) {
//             tab.classList.add('active');
//         }
//     });
// }

// // Optional: Submenu toggle for "Add New"
// function toggleSubmenu(id) {
//     const submenu = document.getElementById(id);
//     if (submenu) submenu.classList.toggle('show');
// }

// // Dummy handlers
// function viewMeterDetails(id) {
//     alert("Viewing meter details for ID: " + id);
// }

// function viewTransactionDetails(txnId) {
//     alert("Viewing transaction: " + txnId);
// }

// function retryTransaction(txnId) {
//     alert("Retrying transaction: " + txnId);
// }

// function processRefund(txnId) {
//     alert("Processing refund for transaction: " + txnId);
// }

// function logout() {
//     alert("Logging out...");
// }


// // Modal and Data Management Variables
// let currentMeterId = null;
// let currentTimeRange = '3months';
// let currentView = 'infinite';
// let currentPage = 1;
// let totalPages = 1;
// let itemsPerPage = 50;
// let allMeterData = [];
// let displayedDataCount = 0;
// let isLoading = false;

// // Update the existing viewMeterDetails function
// function viewMeterDetails(meterId) {
//     currentMeterId = meterId;
//     document.getElementById('modalMeterId').textContent = meterId;
//     document.getElementById('meterDetailsModal').style.display = 'block';
    
//     // Reset view state
//     currentPage = 1;
//     displayedDataCount = 0;
//     document.getElementById('meterDataBody').innerHTML = '';
    
//     // Load initial data
//     loadMeterData();
// }

// // Close modal
// function closeMeterModal() {
//     document.getElementById('meterDetailsModal').style.display = 'none';
//     currentMeterId = null;
// }

// // Close modal when clicking outside
// window.onclick = function(event) {
//     const modal = document.getElementById('meterDetailsModal');
//     if (event.target === modal) {
//         closeMeterModal();
//     }
// }

// // Change time range
// function changeTimeRange(range) {
//     currentTimeRange = range;
    
//     // Update active button
//     document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active'));
//     event.target.classList.add('active');
    
//     // Reset and reload data
//     currentPage = 1;
//     displayedDataCount = 0;
//     document.getElementById('meterDataBody').innerHTML = '';
//     loadMeterData();
// }

// // Toggle view mode
// function toggleView(view) {
//     currentView = view;
    
//     // Update active button
//     document.querySelectorAll('.toggle-btn').forEach(btn => btn.classList.remove('active'));
//     event.target.classList.add('active');
    
//     // Show/hide appropriate controls
//     const paginationControls = document.getElementById('paginationControls');
//     const tableContainer = document.querySelector('.modal-table-container');
    
//     if (view === 'pagination') {
//         paginationControls.classList.remove('hidden');
//         tableContainer.style.maxHeight = '400px';
//         // Remove scroll event listener
//         tableContainer.removeEventListener('scroll', handleInfiniteScroll);
//     } else {
//         paginationControls.classList.add('hidden');
//         tableContainer.style.maxHeight = '500px';
//         // Add scroll event listener
//         tableContainer.addEventListener('scroll', handleInfiniteScroll);
//     }
    
//     // Reset and reload data
//     currentPage = 1;
//     displayedDataCount = 0;
//     document.getElementById('meterDataBody').innerHTML = '';
//     loadMeterData();
// }

// // Generate sample meter data
// function generateMeterData(meterId, months) {
//     const data = [];
//     const endDate = new Date();
//     const startDate = new Date();
//     startDate.setMonth(startDate.getMonth() - months);
    
//     let currentDate = new Date(startDate);
//     let cumulativeKwh = 100 + Math.random() * 50;
    
//     while (currentDate <= endDate) {
//         // Generate 24 readings per day (hourly)
//         for (let hour = 0; hour < 24; hour++) {
//             const readingTime = new Date(currentDate);
//             readingTime.setHours(hour, Math.floor(Math.random() * 60), 0);
            
//             cumulativeKwh += Math.random() * 2;
            
//             data.push({
//                 datetime: readingTime.toLocaleString('en-IN'),
//                 cumKwh: cumulativeKwh.toFixed(2),
//                 voltage: (230 + Math.random() * 20).toFixed(2),
//                 current: (5 + Math.random() * 10).toFixed(2),
//                 power: (800 + Math.random() * 800).toFixed(0),
//                 relay: Math.random() > 0.1 ? 'ON' : 'OFF',
//                 connection: Math.random() > 0.05 ? 'ONLINE' : 'OFFLINE'
//             });
//         }
//         currentDate.setDate(currentDate.getDate() + 1);
//     }
    
//     return data.reverse(); // Latest first
// }

// // Load meter data
// function loadMeterData() {
//     if (isLoading) return;
//     isLoading = true;
    
//     // Show loading indicator for infinite scroll
//     if (currentView === 'infinite') {
//         document.getElementById('loadingIndicator').classList.remove('hidden');
//     }
    
//     // Simulate API call delay
//     setTimeout(() => {
//         const months = currentTimeRange === '3months' ? 3 : 6;
//         allMeterData = generateMeterData(currentMeterId, months);
        
//         if (currentView === 'infinite') {
//             loadMoreData();
//         } else {
//             setupPagination();
//             loadPageData();
//         }
        
//         isLoading = false;
//         document.getElementById('loadingIndicator').classList.add('hidden');
//     }, 1000);
// }

// // Load more data for infinite scroll
// function loadMoreData() {
//     const startIndex = displayedDataCount;
//     const endIndex = Math.min(startIndex + itemsPerPage, allMeterData.length);
    
//     const tbody = document.getElementById('meterDataBody');
    
//     for (let i = startIndex; i < endIndex; i++) {
//         const row = allMeterData[i];
//         const tr = document.createElement('tr');
        
//         tr.innerHTML = `
//             <td>${row.datetime}</td>
//             <td>${row.cumKwh}</td>
//             <td>${row.voltage}</td>
//             <td>${row.current}</td>
//             <td>${row.power}</td>
//             <td><span class="status-badge ${row.relay === 'ON' ? 'status-on' : 'status-off'}">${row.relay}</span></td>
//             <td><span class="status-badge ${row.connection === 'ONLINE' ? 'status-online' : 'status-offline'}">${row.connection}</span></td>
//         `;
        
//         tbody.appendChild(tr);
//     }
    
//     displayedDataCount = endIndex;
// }

// // Handle infinite scroll
// function handleInfiniteScroll() {
//     const container = document.querySelector('.modal-table-container');
    
//     if (container.scrollTop + container.clientHeight >= container.scrollHeight - 10) {
//         if (displayedDataCount < allMeterData.length && !isLoading) {
//             isLoading = true;
//             document.getElementById('loadingIndicator').classList.remove('hidden');
            
//             setTimeout(() => {
//                 loadMoreData();
//                 isLoading = false;
//                 document.getElementById('loadingIndicator').classList.add('hidden');
//             }, 500);
//         }
//     }
// }

// // Setup pagination
// function setupPagination() {
//     totalPages = Math.ceil(allMeterData.length / itemsPerPage);
//     document.getElementById('totalPages').textContent = totalPages;
//     updatePaginationButtons();
// }

// // Load page data for pagination
// function loadPageData() {
//     const startIndex = (currentPage - 1) * itemsPerPage;
//     const endIndex = Math.min(startIndex + itemsPerPage, allMeterData.length);
    
//     const tbody = document.getElementById('meterDataBody');
//     tbody.innerHTML = '';
    
//     for (let i = startIndex; i < endIndex; i++) {
//         const row = allMeterData[i];
//         const tr = document.createElement('tr');
        
//         tr.innerHTML = `
//             <td>${row.datetime}</td>
//             <td>${row.cumKwh}</td>
//             <td>${row.voltage}</td>
//             <td>${row.current}</td>
//             <td>${row.power}</td>
//             <td><span class="status-badge ${row.relay === 'ON' ? 'status-on' : 'status-off'}">${row.relay}</span></td>
//             <td><span class="status-badge ${row.connection === 'ONLINE' ? 'status-online' : 'status-offline'}">${row.connection}</span></td>
//         `;
        
//         tbody.appendChild(tr);
//     }
    
//     document.getElementById('currentPage').textContent = currentPage;
//     updatePaginationButtons();
// }

// // Navigate pages
// function goToPage(direction) {
//     switch(direction) {
//         case 'first':
//             currentPage = 1;
//             break;
//         case 'prev':
//             if (currentPage > 1) currentPage--;
//             break;
//         case 'next':
//             if (currentPage < totalPages) currentPage++;
//             break;
//         case 'last':
//             currentPage = totalPages;
//             break;
//     }
    
//     loadPageData();
// }

// // Update pagination button states
// function updatePaginationButtons() {
//     document.getElementById('firstBtn').disabled = currentPage === 1;
//     document.getElementById('prevBtn').disabled = currentPage === 1;
//     document.getElementById('nextBtn').disabled = currentPage === totalPages;
//     document.getElementById('lastBtn').disabled = currentPage === totalPages;
// }

// // Initialize infinite scroll listener
// document.addEventListener('DOMContentLoaded', function() {
//     const tableContainer = document.querySelector('.modal-table-container');
//     if (tableContainer) {
//         tableContainer.addEventListener('scroll', handleInfiniteScroll);
//     }
// });
