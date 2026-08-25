/* ============================================================
   LFM Nacional — Cliente API compartido
   ============================================================ */

(function () {
  'use strict';

  const SESSION_KEY = 'lfm_session';
  const API_BASE = location.protocol === 'file:' ? 'http://localhost:8080' : '';

  /* ---------- Sesión ---------- */

  function getSession() {
    try {
      return JSON.parse(localStorage.getItem(SESSION_KEY)) || null;
    } catch (e) {
      return null;
    }
  }

  function getUser() {
    const s = getSession();
    return s ? s.usuario : null;
  }

  function getToken() {
    const s = getSession();
    return s ? s.token : null;
  }

  function setSession(token, usuario) {
    localStorage.setItem(SESSION_KEY, JSON.stringify({ token, usuario }));
  }

  function updateUser(usuario) {
    const s = getSession();
    if (s) {
      s.usuario = usuario;
      localStorage.setItem(SESSION_KEY, JSON.stringify(s));
    }
  }

  function clearSession() {
    localStorage.removeItem(SESSION_KEY);
  }

  /* Si no hay sesión o el token expiró, redirige a login y devuelve null. */
  function requireAuth() {
    const u = getUser();
    const t = getToken();
    if (!u || !t) {
      location.href = '02-auth.html?next=' + encodeURIComponent(location.pathname.split('/').pop() + location.search);
      return null;
    }
    try {
      var parts = t.split('.');
      if (parts.length === 3) {
        var payload = JSON.parse(atob(parts[1]));
        if (payload.exp && payload.exp < Date.now() / 1000) {
          clearSession();
          location.href = '02-auth.html?next=' + encodeURIComponent(location.pathname.split('/').pop() + location.search);
          return null;
        }
      }
    } catch (e) { /* token malformado, limpiar sesión */ clearSession(); location.href = '02-auth.html'; return null; }
    return u;
  }

  /* ---------- Fetch ---------- */

  async function api(path, options = {}) {
    const { method = 'GET', body, auth = true, raw = false } = options;

    const headers = {};
    if (body !== undefined && !(body instanceof FormData)) {
      headers['Content-Type'] = 'application/json';
    }
    const token = getToken();
    if (auth && token) {
      headers['Authorization'] = 'Bearer ' + token;
    }

    const isAuthEndpoint = path.includes('/login') || path.includes('/registro');

    let res;
    try {
      res = await fetch(API_BASE + '/api' + path, {
        method,
        headers,
        body: body instanceof FormData ? body : (body !== undefined ? JSON.stringify(body) : undefined)
      });
    } catch (e) {
      throw new Error('No se pudo conectar con el servidor. ¿Está levantado el backend en :8080?');
    }

    if (res.status === 401 && !isAuthEndpoint && auth) {
      clearSession();
      const next = encodeURIComponent(location.pathname.split('/').pop() + location.search);
      location.href = '02-auth.html?next=' + next;
      throw new Error('Sesión expirada');
    }

    if (res.status === 204) return null;

    const text = await res.text();
    let data = null;
    try { data = text ? JSON.parse(text) : null; } catch (e) { data = text; }

    if (!res.ok) {
      const msg = (data && (data.mensaje || data.error || data.message))
        ? data.mensaje || data.error || data.message
        : ('Error ' + res.status);
      const err = new Error(msg);
      err.status = res.status;
      throw err;
    }

    return raw ? res : data;
  }

  function get(path) { return api(path); }
  function post(path, body) { return api(path, { method: 'POST', body }); }
  function put(path, body) { return api(path, { method: 'PUT', body }); }
  function del(path) { return api(path, { method: 'DELETE' }); }

  /* ---------- Utilidades ---------- */

  const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

  function parseIso(iso) {
    if (!iso) return null;
    if (iso instanceof Date) return iso;
    const d = new Date(iso);
    return isNaN(d.getTime()) ? null : d;
  }

  function fmtFecha(iso) {
    const d = parseIso(iso);
    if (!d) return '—';
    return d.getDate() + ' ' + MESES[d.getMonth()].toUpperCase();
  }

  function fmtFechaHora(iso) {
    const d = parseIso(iso);
    if (!d) return '—';
    const hh = String(d.getHours()).padStart(2, '0');
    const mm = String(d.getMinutes()).padStart(2, '0');
    return d.getDate() + ' ' + MESES[d.getMonth()].toUpperCase() + ' · ' + hh + ':' + mm;
  }

  function fmtHora(iso) {
    const d = parseIso(iso);
    if (!d) return '—';
    const hh = String(d.getHours()).padStart(2, '0');
    const mm = String(d.getMinutes()).padStart(2, '0');
    return hh + ':' + mm + ' hs';
  }

  function fmtRel(iso) {
    const d = parseIso(iso);
    if (!d) return '';
    const seg = Math.floor((Date.now() - d.getTime()) / 1000);
    if (seg < 60) return 'ahora';
    const min = Math.floor(seg / 60);
    if (min < 60) return 'hace ' + min + ' min';
    const hs = Math.floor(min / 60);
    if (hs < 24) return 'hace ' + hs + ' hs';
    const dias = Math.floor(hs / 24);
    if (dias < 7) return 'hace ' + dias + ' días';
    return d.getDate() + ' ' + MESES[d.getMonth()];
  }

  function esc(str) {
    if (str === null || str === undefined) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function sanitizeUrl(url) {
    if (!url) return '';
    var s = String(url).trim().toLowerCase();
    if (s.startsWith('javascript:') || s.startsWith('data:') || s.startsWith('vbscript:')) return '';
    return url;
  }

  /* Formatea milisegundos como tiempo de vuelta (mm:ss.mmm) */
  function fmtLap(ms) {
    if (ms === null || ms === undefined || isNaN(ms)) return '—';
    const total = ms / 1000;
    const m = Math.floor(total / 60);
    const s = Math.floor(total % 60);
    const ml = Math.floor((total * 1000) % 1000);
    return (m > 0 ? m + ':' : '') + String(s).padStart(2, '0') + '.' + String(ml).padStart(3, '0');
  }

  /* -------- Chips / estados -------- */

  const CARRERA_CHIPS = {
    PROGRAMADA: ['chip-upcoming', 'Programada'],
    INSCRIPCIONES_ABIERTAS: ['chip-upcoming', 'Inscripciones abiertas'],
    INSCRIPCIONES_CERRADAS: ['chip-closed', 'Cerrada'],
    EN_CURSO: ['chip-review', 'En curso'],
    FINALIZADA: ['chip-resolved', 'Finalizada'],
    CANCELADA: ['chip-rejected', 'Cancelada']
  };

  function chipCarrera(estado) {
    const def = CARRERA_CHIPS[estado] || ['chip-pending', estado || '—'];
    return '<span class="chip ' + def[0] + '">' + esc(def[1]) + '</span>';
  }

  function chipEstado(estado, mapa) {
    if (mapa && mapa[estado]) {
      return '<span class="chip ' + mapa[estado][0] + '">' + esc(mapa[estado][1]) + '</span>';
    }
    return '<span class="chip chip-pending">' + esc(estado) + '</span>';
  }

  /* -------- Fila de carrera (lista / próximas) -------- */

  function raceRow(r) {
    const d = parseIso(r.fecha);
    const dia = d ? d.getDate() : '—';
    const mes = d ? MESES[d.getMonth()].toUpperCase() : '';
    return '<div class="flex gap-3">' +
      '<div class="race-date"><span class="day">' + dia + '</span><span class="mon">' + mes + '</span></div>' +
      '<div class="race-meta">' +
      '<strong style="color:var(--text-primary);font-family:var(--font-display);text-transform:uppercase;font-size:var(--fs-sm)">' + esc(r.nombre || 'Carrera') + (r.categoriaNombre ? ' — ' + esc(r.categoriaNombre) : '') + '</strong>' +
      '<span>' + esc(r.circuito || '') + (d ? ' · ' + fmtHora(r.fecha) : '') + '</span>' +
      '</div></div>';
  }

  /* -------- Avatar ---------- */
  function iniciales(user) {
    if (!user) return '?';
    const n = user.nombrePiloto || user.email || '?';
    const parts = n.trim().split(/\s+/);
    return ((parts[0][0] || '') + (parts.length > 1 ? parts[parts.length - 1][0] : '')).toUpperCase();
  }

  function avatarHtml(user, size) {
    const s = size || 38;
    const src = user && user.fotoPerfil ? 'src="' + esc(sanitizeUrl(user.fotoPerfil)) + '"' : '';
    if (src) {
      return '<img class="avatar" width="' + s + '" height="' + s + '" ' + src + ' alt="">';
    }
    const ini = iniciales(user);
    return '<span class="avatar avatar-initials" style="width:' + s + 'px;height:' + s + 'px">' + ini + '</span>';
  }

  /* -------- Toast -------- */

  function toast(msg, tipo) {
    const color = tipo === 'error' ? 'var(--danger)' : (tipo === 'success' ? 'var(--success)' : 'var(--amber)');
    let el = document.getElementById('lfm-toast');
    if (!el) {
      el = document.createElement('div');
      el.id = 'lfm-toast';
      el.style.cssText = 'position:fixed;bottom:20px;right:20px;z-index:9999;max-width:340px;padding:12px 16px;border-radius:var(--radius,8px);color:#fff;font-family:var(--font-body);font-size:var(--fs-sm);box-shadow:0 6px 24px rgba(0,0,0,.45);opacity:0;transition:opacity .25s;background:' + color + ';';
      document.body.appendChild(el);
    }
    el.style.background = color;
    el.textContent = msg;
    el.style.opacity = '1';
    clearTimeout(el._t);
    el._t = setTimeout(function () { el.style.opacity = '0'; }, 3500);
  }

  /* -------- Export ---------- */

  window.LFM = {
    API_BASE,
    api, get, post, put, del,
    getUser, getToken, setSession, updateUser, clearSession, requireAuth,
    fmtFecha, fmtFechaHora, fmtHora, fmtRel, fechaRelativa: fmtRel, fmtLap, esc, sanitizeUrl,
    chipCarrera, chipEstado, raceRow, avatarHtml, toast
  };
})();
