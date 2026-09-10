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
    let el = document.getElementById('lfm-toast');
    if (!el) {
      el = document.createElement('div');
      el.id = 'lfm-toast';
      document.body.appendChild(el);
    }
    el.className = tipo === 'error' ? 'error' : (tipo === 'success' ? 'success' : '');
    el.textContent = msg;
    let bar = el.querySelector('.toast-bar');
    if (bar) bar.remove();
    bar = document.createElement('span');
    bar.className = 'toast-bar';
    el.appendChild(bar);
    el.classList.add('show');
    clearTimeout(el._t);
    el._t = setTimeout(function () { el.classList.remove('show'); }, 3400);
  }

  /* -------- Botón en carga (busy) -------- */

  function busy(btn, on) {
    if (!btn) return;
    if (on) {
      btn.classList.add('is-loading');
      btn.setAttribute('aria-busy', 'true');
    } else {
      btn.classList.remove('is-loading');
      btn.removeAttribute('aria-busy');
    }
  }

  /* -------- Estados de campo inválido -------- */

  function setFieldInvalid(input, on, msg) {
    const field = input ? input.closest('.field') : null;
    if (!field) return;
    field.classList.toggle('invalid', !!on);
    if (msg !== undefined) {
      let el = field.querySelector('.error-msg');
      if (!el) {
        el = document.createElement('span');
        el.className = 'error-msg';
        field.appendChild(el);
      }
      el.textContent = msg || '';
    }
  }

  function clearFieldErrors(root) {
    (root || document).querySelectorAll('.field.invalid').forEach(function (f) { f.classList.remove('invalid'); });
  }

  /* -------- Skeletons en zonas de carga -------- */

  function skeletonHtml(zone) {
    const table = zone.closest ? zone.closest('.table-wrap table') : null;
    if (zone.tagName === 'TBODY' || table) {
      const t = zone.tagName === 'TBODY' ? zone : table;
      const cols = Math.max(1, t.closest('.table-wrap').querySelectorAll('thead th, thead td').length);
      let rows = '';
      for (let i = 0; i < 5; i++) {
        rows += '<tr><td colspan="' + cols + '"><span class="skeleton"></span></td></tr>';
      }
      return rows;
    }
    const cards = [];
    for (let i = 0; i < 3; i++) {
      cards.push(
        '<div class="skeleton-card">' +
        '<span class="skeleton" style="width:38%"></span>' +
        '<span class="skeleton" style="width:72%"></span>' +
        '<span class="skeleton" style="width:52%"></span>' +
        '</div>'
      );
    }
    return cards.join('');
  }

  function initSkeletons(root) {
    (root || document).querySelectorAll('.skeleton-zone').forEach(function (zone) {
      if (!/^cargando/i.test((zone.textContent || '').trim())) return;
      zone.innerHTML = skeletonHtml(zone);
    });
  }

  /* -------- Modales: cierre con Esc -------- */

  function closeModalsOnEscape() {
    document.addEventListener('keydown', function (e) {
      if (e.key !== 'Escape') return;
      document.querySelectorAll('.cta-dropdown-panel.open').forEach(function (p) { p.classList.remove('open'); });
      document.querySelectorAll('.modal-overlay').forEach(function (ov) {
        if (ov.style.display === 'none') return;
        ov.style.display = 'none';
        let closeBtn = null;
        ov.querySelectorAll('button').forEach(function (b) {
          if (!closeBtn && (b.matches('.modal-close') || /cerrar|close/i.test(b.id || ''))) closeBtn = b;
        });
        if (closeBtn) closeBtn.click();
      });
    });
  }

  /* -------- Export ---------- */

  window.LFM = {
    API_BASE,
    api, get, post, put, del,
    getUser, getToken, setSession, updateUser, clearSession, requireAuth,
    fmtFecha, fmtFechaHora, fmtHora, fmtRel, fechaRelativa: fmtRel, fmtLap, esc, sanitizeUrl,
    chipCarrera, chipEstado, raceRow, avatarHtml, toast,
    busy, setFieldInvalid, clearFieldErrors, initSkeletons
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () {
      initSkeletons(document);
      closeModalsOnEscape();
    });
  } else {
    initSkeletons(document);
    closeModalsOnEscape();
  }
})();
