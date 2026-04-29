/**
 * ForgeWebMap - app.js
 *
 * CRS Math:
 *   Custom MinecraftCRS: scale(zoom) = 2^zoom
 *   lat = -blockZ, lng = blockX
 *   At zoom=0: 1 block = 1 pixel, 1 tile = 256px
 *   minNativeZoom = maxNativeZoom = 0 → always fetch zoom-0 tiles, scale visually
 */

(function () {
    'use strict';

    const TILE_SIZE = 256;
    const PLAYER_REFRESH_MS = 2000;
    const STATUS_REFRESH_MS = 5000;

    // Transparent 1×1 GIF for 404 tiles
    const TRANSPARENT = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7';

    let currentDimension = 'overworld';
    let map, tileLayer;
    let playerMarkers = {};

    // ── Custom CRS ─────────────────────────────────────────────────────────────

    var MinecraftCRS = L.extend({}, L.CRS.Simple, {
        scale: function (zoom) { return Math.pow(2, zoom); },
        zoom:  function (scale) { return Math.log(scale) / Math.LN2; }
    });

    // ── Coordinate helpers ─────────────────────────────────────────────────────

    function blockToLatLng(bx, bz) {
        return L.latLng(-bz, bx);
    }

    function latLngToBlock(latlng) {
        return { x: Math.floor(latlng.lng), z: Math.floor(-latlng.lat) };
    }

    // ── Coordinate display ─────────────────────────────────────────────────────

    var coordsEl       = document.getElementById('coords');
    var mobileCoordsEl = document.getElementById('mobile-coords');

    function showCoords(latlng) {
        var blk = latLngToBlock(latlng);
        var text = 'X: ' + blk.x + '  Z: ' + blk.z;
        if (coordsEl)       coordsEl.textContent       = text;
        if (mobileCoordsEl) mobileCoordsEl.textContent = text;
    }

    // ── Tile layer ─────────────────────────────────────────────────────────────

    var MinecraftTileLayer = L.TileLayer.extend({
        initialize: function (dimension, options) {
            L.TileLayer.prototype.initialize.call(this, '', L.setOptions(this, options));
            this._mcDim = dimension;
        },

        getTileUrl: function (coords) {
            return '/tiles/' + this._mcDim + '/0/' + coords.x + '/' + coords.y + '.png';
        },

        setDimension: function (dim) {
            this._mcDim = dim;
            this.redraw();
        }
    });

    // ── Map init ───────────────────────────────────────────────────────────────

    function initMap() {
        map = L.map('map', {
            crs: MinecraftCRS,
            center: blockToLatLng(0, 0),
            zoom: 0,
            minZoom: -4,
            maxZoom: 4,
            zoomSnap: 1,
            zoomDelta: 1,
            zoomControl: false,
            attributionControl: false,
            // Mobile touch options
            tapHold: false,
            touchZoom: true,
            bounceAtZoomLimits: false,
        });

        // Zoom control bottom-left (CSS moves it above mobile bar on small screens)
        L.control.zoom({ position: 'bottomleft' }).addTo(map);

        tileLayer = new MinecraftTileLayer(currentDimension, {
            tileSize: TILE_SIZE,
            noWrap: true,
            minNativeZoom: 0,
            maxNativeZoom: 0,
            minZoom: -4,
            maxZoom: 4,
            errorTileUrl: TRANSPARENT,
            keepBuffer: 2,
        });
        tileLayer.addTo(map);

        // Mouse move (desktop)
        map.on('mousemove', function (e) {
            showCoords(e.latlng);
        });

        // Touch move (mobile) — fires during panning
        map.on('move', function () {
            var center = map.getCenter();
            showCoords(center);
        });
    }

    // ── Dimension switcher (handles both desktop and mobile buttons) ──────────

    function setDimension(dim) {
        // Update all dim-btn elements on the page
        document.querySelectorAll('.dim-btn').forEach(function (b) {
            b.classList.toggle('active', b.dataset.dim === dim);
        });
        currentDimension = dim;
        if (tileLayer) tileLayer.setDimension(dim);
        clearPlayerMarkers();
    }

    document.querySelectorAll('.dim-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            setDimension(this.dataset.dim);
        });
    });

    // ── Player markers ─────────────────────────────────────────────────────────

    function clearPlayerMarkers() {
        Object.values(playerMarkers).forEach(function (m) { map.removeLayer(m); });
        playerMarkers = {};
    }

    var playerIcon = L.divIcon({
        className:  'player-marker-icon',
        iconSize:   [10, 10],
        iconAnchor: [5, 5],
    });

    function updatePlayers(players) {
        var seen = {};
        players.forEach(function (p) {
            if (p.dimension !== currentDimension) return;
            seen[p.name] = true;
            var latlng = blockToLatLng(p.x, p.z);
            if (playerMarkers[p.name]) {
                playerMarkers[p.name].setLatLng(latlng);
            } else {
                playerMarkers[p.name] = L.marker(latlng, { icon: playerIcon })
                    .bindTooltip(p.name, {
                        className: 'player-marker-label',
                        permanent: true,
                        direction: 'top',
                        offset: [0, -8],
                    })
                    .addTo(map);
            }
        });
        Object.keys(playerMarkers).forEach(function (name) {
            if (!seen[name]) {
                map.removeLayer(playerMarkers[name]);
                delete playerMarkers[name];
            }
        });
    }

    function fetchPlayers() {
        fetch('/api/players')
            .then(function (r) { return r.ok ? r.json() : []; })
            .then(updatePlayers)
            .catch(function () {});
    }

    // ── Status indicator ───────────────────────────────────────────────────────

    var statusDots = [
        document.getElementById('status-indicator'),
        document.getElementById('mobile-status-indicator')
    ].filter(Boolean);

    function setStatus(online) {
        statusDots.forEach(function (dot) {
            dot.classList.toggle('online', online);
            dot.classList.toggle('offline', !online);
            dot.title = online ? 'Server online' : 'Server offline';
        });
    }

    function fetchStatus() {
        fetch('/api/status')
            .then(function (r) { setStatus(r.ok); })
            .catch(function () { setStatus(false); });
    }

    // ── Bootstrap ──────────────────────────────────────────────────────────────

    initMap();
    fetchPlayers();
    fetchStatus();
    setInterval(fetchPlayers, PLAYER_REFRESH_MS);
    setInterval(fetchStatus, STATUS_REFRESH_MS);

}());
