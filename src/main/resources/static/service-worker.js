const STATIC_CACHE = "prepaid-app-static-v2";
const PAGE_CACHE = "prepaid-app-pages-v2";
const ASSET_CACHE = "prepaid-app-assets-v2";
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
self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(STATIC_CACHE).then((cache) => cache.addAll(APP_SHELL))
  );
  self.skipWaiting();
});
self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(
        keys
          .filter((key) => ![STATIC_CACHE, PAGE_CACHE, ASSET_CACHE].includes(key))
          .map((key) => caches.delete(key))
      )
    )
  );
  self.clients.claim();
});
self.addEventListener("fetch", (event) => {
  if (event.request.method !== "GET") {
    return;
  }
  const requestUrl = new URL(event.request.url);
  if (requestUrl.origin !== self.location.origin) {
    return;
  }
  if (event.request.mode === "navigate") {
    event.respondWith(handleNavigation(event.request));
    return;
  }
  event.respondWith(handleAsset(event.request));
});
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
async function handleAsset(request) {
  const cache = await caches.open(ASSET_CACHE);
  const cachedResponse = await cache.match(request);
  if (cachedResponse) {
    return cachedResponse;
  }
  const response = await fetch(request);
  if (response.ok) {
    cache.put(request, response.clone());
  }
  return response;
}
