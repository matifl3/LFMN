/* LFM Nacional — 10 · Notificaciones */
(function () {
  'use strict';
  const L = window.LFM;

  const user = L.requireAuth();
  if (!user) return;

  const feed = document.getElementById('nt-feed');
  const eyebrow = document.getElementById('nt-cta');

  const ICONS = {
    CARRERA_INICIO: ['var(--celeste)', '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 17V7l8-4 8 4v10l-8 4-8-4Z"/></svg>'],
    PENALIZACION: ['var(--danger)', '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0Z"/><path d="M12 9v4M12 17h.01"/></svg>'],
    LOGRO: ['var(--amber)', '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m12 2 3 7h7l-5.5 4.5L18 21l-6-4-6 4 1.5-7.5L2 9h7z"/></svg>'],
    RECOMPENSA: ['var(--amber)', '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="8" r="6"/><path d="M15.5 13 17 22l-5-3-5 3 1.5-9"/></svg>'],
    ANUNCIO: ['var(--celeste)', '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 11v2a1 1 0 0 0 1 1h2l9 5V5L6 10H4a1 1 0 0 0-1 1Z"/><path d="M19 8a5 5 0 0 1 0 8"/></svg>'],
    INCIDENTE: ['var(--danger)', '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 3"/></svg>'],
    APELACION: ['var(--celeste)', '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 11.5a8.38 8.38 0 0 1-8.5 8.5 8.5 8.5 0 0 1-6.13-2.5L3 19l1-3.6A8.5 8.5 0 1 1 21 11.5Z"/></svg>']
  };

  function linkDe(n) {
    if (n.link && n.link.indexOf('http') === 0) return n.link;
    if (!n.link) return null;
    return n.link;
  }

  function render(list) {
    const noLeidas = list.filter(function (n) { return !n.leida; }).length;
    eyebrow.textContent = noLeidas + ' SIN LEER';
    if (!list.length) {
      feed.innerHTML = '<p class="text-tertiary">No tenés notificaciones.</p>';
      return;
    }
    feed.innerHTML = list.map(function (n) {
      const cfg = ICONS[n.tipo] || ['var(--celeste)', '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"/></svg>'];
      const leidaCls = n.leida ? '' : 'unread';
      const accion = [];
      const link = linkDe(n);
      if (link) {
        accion.push('<a href="' + L.esc(link) + '" class="btn btn-ghost btn-sm">Ver</a>');
      }
      if (!n.leida) {
        accion.push('<a href="#" class="btn btn-ghost btn-sm" data-leer="' + n.id + '">Marcar como leída</a>');
      } else {
        accion.push('<a href="#" class="btn btn-ghost btn-sm" data-borrar="' + n.id + '" style="color:var(--danger)">Eliminar</a>');
      }
      return '<div class="feed-item ' + leidaCls + '">' +
        '<div class="feed-icon" style="color:' + cfg[0] + '">' + cfg[1] + '</div>' +
        '<div style="flex:1">' +
        '<div class="flex-between">' +
        '<strong style="font-size:var(--fs-sm)' + (n.leida ? ';color:var(--text-secondary)' : '') + '">' + (n.tipo || '').replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, function (c) { return c.toUpperCase(); }) + '</strong>' +
        '<span class="text-tertiary mono" style="font-size:var(--fs-2xs)">' + L.fmtRel(n.fecha) + '</span>' +
        '</div>' +
        '<p class="text-secondary" style="font-size:var(--fs-sm); margin-top:var(--sp-1)">' + L.esc(n.mensaje) + '</p>' +
        (accion.length ? '<div class="flex gap-2" style="margin-top:var(--sp-3)">' + accion.join('') + '</div>' : '') +
        '</div></div>';
    }).join('');

    feed.querySelectorAll('[data-leer]').forEach(function (a) {
      a.addEventListener('click', async function (e) {
        e.preventDefault();
        try {
          await L.put('/notificaciones/' + a.dataset.leer + '/leida');
          load();
        } catch (err) { L.toast(err.message, 'error'); }
      });
    });
    feed.querySelectorAll('[data-borrar]').forEach(function (a) {
      a.addEventListener('click', async function (e) {
        e.preventDefault();
        try {
          await L.del('/notificaciones/' + a.dataset.borrar);
          load();
        } catch (err) { L.toast(err.message, 'error'); }
      });
    });
  }

  function load() {
    L.api('/notificaciones/usuario/' + user.id).then(render).catch(function (err) {
      feed.innerHTML = '<p class="text-tertiary">' + L.esc(err.message) + '</p>';
    });
  }

  document.getElementById('nt-marcar-todas').addEventListener('click', async function (e) {
    e.preventDefault();
    try {
      await L.put('/notificaciones/usuario/' + user.id + '/leidas');
      L.toast('Todas marcadas como leídas', 'success');
      load();
    } catch (err) { L.toast(err.message, 'error'); }
  });

  load();
})();
