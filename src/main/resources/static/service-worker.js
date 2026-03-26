
const STATIC_CACHE = "prepaid-app-static-v3";
const PAGE_CACHE = "prepaid-app-pages-v3";
const ASSET_CACHE = "prepaid-app-assets-v3";

const APP_SHELL = [
  "/",
  "/manifest.json",
  "/images/ariot_logo.jpeg",
  "/images/icon-72.png",
  "/images/icon-96.png",
  "/images/icon-128.png",
  "/images/icon-144.png",
  "/images/icon-152.png",
  "/images/icon-192.png",
  "/images/icon-384.png",
  "/images/icon-512.png",
  "/css/index.css",
  "/css/styles.css",
  "/css/meter_management_mobile.css",
  "/css/tenant.css"
];

// ✅ INSTALL
self.addEventListener("install", (event) => {
  self.skipWaiting();

  event.waitUntil(
    caches.open(STATIC_CACHE).then((cache) => {
      return cache.addAll(APP_SHELL);
    })
  );
});

// ✅ ACTIVATE
self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.map((key) => {
          if (![STATIC_CACHE, PAGE_CACHE, ASSET_CACHE].includes(key)) {
            return caches.delete(key);
          }
        })
      );
    }).then(() => self.clients.claim())
  );
});

// ✅ FETCH
self.addEventListener("fetch", (event) => {
  if (event.request.method !== "GET") return;

  const requestUrl = new URL(event.request.url);

  // 🚨 NEVER CACHE APIs 
  if (requestUrl.pathname.startsWith("/api/")) {
    event.respondWith(fetch(event.request));
    return;
  }

  // Skip external requests
  if (requestUrl.origin !== self.location.origin) return;

  // Handle navigation (HTML pages)
  if (event.request.mode === "navigate") {
    event.respondWith(handleNavigation(event.request));
    return;
  }

  // Handle static assets
  event.respondWith(handleAsset(event.request));
});

// ✅ PAGE HANDLER (network-first)
async function handleNavigation(request) {
  const cache = await caches.open(PAGE_CACHE);

  try {
    const response = await fetch(request);
    cache.put(request, response.clone());
    return response;
  } catch (error) {
    return (await cache.match(request)) || (await caches.match("/"));
  }
}

// ✅ ASSET HANDLER (cache-first, but SAFE)
async function handleAsset(request) {
  const cache = await caches.open(ASSET_CACHE);

  const cachedResponse = await cache.match(request);
  if (cachedResponse) {
    return cachedResponse;
  }

  const response = await fetch(request);

  // DO NOT CACHE API 
  if (response.ok && !request.url.includes("/api/")) {
    cache.put(request, response.clone());
  }

  return response;
}