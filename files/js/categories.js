/* LFM Nacional — 06 · Categorías */
(function () {
  'use strict';
  const L = window.LFM;

  const grid = document.getElementById('categorias-grid');

  const ICONS = {
    default: '<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 17V7l8-4 8 4v10l-8 4-8-4Z"/><path d="M4 7l8 4 8-4M12 11v10"/></svg>'
  };

  Promise.all([
    L.api('/categorias').catch(function () { return []; }),
    L.api('/usuarios').catch(function () { return []; })
  ]).then(function (res) {
    const cats = res[0];
    const users = res[1];
    if (!cats.length) {
      grid.innerHTML = '<p class="text-tertiary">No hay categorías publicadas.</p>';
      return;
    }
    grid.innerHTML = cats.map(function (c) {
      const n = users.filter(function (u) {
        const elo = u.elo;
        if (elo === null || elo === undefined) return false;
        if (c.eloMinimo !== null && c.eloMinimo !== undefined && elo < c.eloMinimo) return false;
        if (c.eloMaximo !== null && c.eloMaximo !== undefined && elo > c.eloMaximo) return false;
        return true;
      }).length;

      const rang = (c.eloMinimo != null && c.eloMaximo != null)
        ? '<div class="elo-range"><span>' + c.eloMinimo + '</span><span class="arrow">→</span><span>' + c.eloMaximo + '</span><span class="text-tertiary" style="font-family:var(--font-body)">Elo</span></div>'
        : '<div class="elo-range"><span>—</span></div>';

      const chip = (c.eloMinimo != null && c.eloMaximo != null)
        ? '<span class="chip chip-confirmed">Abierta</span>'
        : '<span class="chip chip-closed">Próximamente</span>';

      return '<a href="03-races-list.html?cat=' + c.id + '" class="card card-hover category-card" style="text-decoration:none;color:inherit">' +
        '<div class="category-icon">' + (ICONS[c.nombre] || ICONS.default) + '</div>' +
        '<div>' +
        '<h3>' + L.esc(c.nombre) + '</h3>' +
        '<p class="text-secondary" style="font-size:var(--fs-sm); margin-top:var(--sp-2)">' + L.esc(c.descripcion || '') + '</p>' +
        '</div>' +
        '<div style="margin-top:auto">' +
        rang +
        '<div class="flex-between" style="margin-top:var(--sp-3)">' +
        chip +
        '<span class="text-tertiary mono" style="font-size:var(--fs-2xs)">' + n.toLocaleString('es-AR') + ' pilotos</span>' +
        '</div></div></a>';
    }).join('');
  }).catch(function () {});
})();
