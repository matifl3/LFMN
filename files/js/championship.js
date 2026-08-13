/* LFM Nacional — 05 · Campeonato (tabla de pilotos) */
(function () {
  'use strict';
  const L = window.LFM;

  const select = document.getElementById('champ-select');
  const tbody = document.getElementById('champ-tabla');
  const info = document.getElementById('champ-info');
  let campeonatos = [];
  let selected = null;

  const params = new URLSearchParams(location.search);
  const idParam = params.get('id') ? Number(params.get('id')) : null;

  function loadTabla(id) {
    tbody.innerHTML = '<tr><td colspan="3" class="text-tertiary">Cargando posiciones…</td></tr>';
    L.api('/campeonatos/' + id + '/tabla').then(function (tabla) {
      const champ = campeonatos.find(function (c) { return c.id === id; });
      info.textContent = (champ ? (champ.temporada || 'Temporada 2026') : '') + ' · ' + (champ ? champ.sistemaPuntos : '') + ' · ' + (champ ? champ.estado : '');

      if (!tabla.length) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-tertiary">Aún no hay posiciones registradas.</td></tr>';
        return;
      }
      const medal = ['gold', 'silver', 'bronze'];
      tbody.innerHTML = tabla.slice().sort(function (a, b) { return (a.posicion || 999) - (b.posicion || 999); }).map(function (t, idx) {
        const rowClass = idx === 0 ? 'podium-1' : (idx === 1 ? 'podium-2' : (idx === 2 ? 'podium-3' : ''));
        const pos = t.posicion || (idx + 1);
        return '<tr class="' + rowClass + '">' +
          '<td><span class="rank-badge ' + (medal[idx] || '') + '">' + pos + '</span></td>' +
          '<td class="data"><a href="08-driver-profile.html?id=' + t.usuarioId + '" class="link">' + L.esc(t.nombrePiloto) + '</a></td>' +
          '<td class="num mono glow-amber" style="font-weight:700">' + t.puntos + '</td>' +
          '</tr>';
      }).join('');
    }).catch(function (err) {
      tbody.innerHTML = '<tr><td colspan="3" class="text-tertiary">' + L.esc(err.message) + '</td></tr>';
    });
  }

  L.api('/campeonatos').then(function (list) {
    campeonatos = list;
    if (!campeonatos.length) {
      select.innerHTML = '<option value="">Sin campeonatos</option>';
      tbody.innerHTML = '<tr><td colspan="3" class="text-tertiary">No hay campeonatos publicados.</td></tr>';
      return;
    }
    select.innerHTML = campeonatos.map(function (c) {
      return '<option value="' + c.id + '">' + L.esc(c.nombre) + ' (' + L.esc(c.temporada || '') + ')</option>';
    }).join('');

    const inicial = idParam && campeonatos.some(function (c) { return c.id === idParam; })
      ? idParam
      : campeonatos[0].id;
    select.value = String(inicial);
    selected = inicial;
    loadTabla(inicial);
  }).catch(function (err) {
    tbody.innerHTML = '<tr><td colspan="3" class="text-tertiary">' + L.esc(err.message) + '</td></tr>';
  });

  select.addEventListener('change', function () {
    selected = Number(select.value);
    if (selected) loadTabla(selected);
  });
})();
