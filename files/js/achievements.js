/* LFM Nacional — 11 · Logros */
(function () {
  'use strict';
  const L = window.LFM;

  const grid = document.getElementById('ach-grid');
  const eyebrow = document.getElementById('ach-eyebrow');
  const progress = document.getElementById('ach-progress');

  function cardHtml(lg, idx) {
    const icon = lg.icono
      ? L.esc(lg.icono)
      : '<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m12 2 3 7h7l-5.5 4.5L18 21l-6-4-6 4 1.5-7.5L2 9h7z"/></svg>';

    const obtenido = lg.obtenido === true;
    const cls = obtenido ? ' unlocked' : ' locked';
    const check = obtenido ? '<div class="achievement-check">✓</div>' : '';

    let extra = '';
    if (!obtenido && lg.progreso !== null && lg.progreso !== undefined && lg.valorCondicion != null) {
      const pct = lg.valorCondicion ? Math.round((lg.progreso / lg.valorCondicion) * 100) : 0;
      extra = '<div class="progress" style="margin-top:var(--sp-2)"><div class="progress-bar" style="width:' + pct + '%"></div></div>' +
        '<span class="text-tertiary mono" style="font-size:var(--fs-2xs)">' + lg.progreso + ' / ' + lg.valorCondicion + '</span>';
    } else if (!obtenido && lg.progreso === undefined) {
      extra = '<span class="chip chip-pending" style="margin-top:var(--sp-2);padding:.2em .6em">Sin desbloquear</span>';
    }

    return '<div class="card achievement-card' + cls + (obtenido ? ' bracket' : '') + '" style="' + (idx >= 0 ? '' : '') + '">' +
      check +
      '<div class="achievement-icon">' + icon + '</div>' +
      '<h4>' + L.esc(lg.nombre || 'Logro') + '</h4>' +
      '<p class="text-tertiary" style="font-size:var(--fs-xs)">' + L.esc(lg.descripcion || '') + '</p>' +
      extra +
      '</div>';
  }

  const user = L.getUser();
  const url = user ? '/logros/usuario/' + user.id : '/logros';

  L.api(url).then(function (list) {
    if (!list.length) {
      grid.innerHTML = '<p class="text-tertiary">No hay logros publicados.</p>';
      return;
    }
    const obtenidos = user ? list.filter(function (lg) { return lg.obtenido; }).length : 0;
    eyebrow.textContent = (user ? obtenidos + ' DE ' + list.length + ' DESBLOQUEADOS' : 'INICIÁ SESIÓN PARA VER TU PROGRESO');
    if (user && list.length) progress.style.width = Math.round((obtenidos / list.length) * 100) + '%';

    grid.innerHTML = list.map(function (lg) { return cardHtml(lg); }).join('');
  }).catch(function (err) {
    grid.innerHTML = '<p class="text-tertiary">' + L.esc(err.message) + '</p>';
  });
})();
