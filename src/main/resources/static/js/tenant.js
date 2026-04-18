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
    console.log(data);
    return data;
  } catch (error) {
    console.error("Error", error.message);
  }
}

async function logout(){
    try{

        await fetch("/api/tenants/logout",{
            method: "POST",
            credentials: "include"
        });

        // redirect after logout
        window.location.href = "/";
    }catch(error){
        console.log("Logout failed", error);
    }
}

function dynamicTenantData(data) {
  document.getElementById("section-title-name").textContent = data.tenantName;
  document.getElementById("current-balance").textContent ="₹" + data.tenantCurrentBalance;

  const meterSerialElement = document.getElementById("meter-serialNo");
  data.tenantMeterSerialNumber === null ? 
  (meterSerialElement.textContent = "NA") :
   (meterSerialElement.textContent = data.tenantMeterSerialNumber);

  document.getElementById("room-no").textContent = data.tenantRoomNumber;
  document.getElementById("tenant-name").textContent = data.tenantName;
  document.getElementById("tenant-phoneNo").textContent = data.tenantPhoneNumber;
  document.getElementById("tenant-email").textContent = data.tenantEmail;
}

async function firstFunction() {
  const data = await loadTenantData();
  dynamicTenantData(data);
}

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
