/* LFM Nacional — 03 · Lista de carreras */
(function () {
  'use strict';
  const L = window.LFM;

  const box = document.getElementById('races-list');
  const selectCat = document.getElementById('races-categoria');
  const tabProx = document.getElementById('tab-proximas');
  const tabPasadas = document.getElementById('tab-pasadas');
  let proximas = [];
  let pasadas = [];
  let view = 'proximas';
  let filtroCat = '';

  function render() {
    const lista = view === 'proximas' ? proximas : pasadas;
    const filtradas = filtroCat ? lista.filter(function (r) { return String(r.categoriaId) === filtroCat; }) : lista;
    const tabCount = view === 'proximas' ? tabProx : tabPasadas;
    tabCount.querySelector('.count').textContent = filtradas.length;

    if (!filtradas.length) {
      box.innerHTML = '<p class="text-tertiary">No hay carreras para mostrar.</p>';
      return;
    }

    box.innerHTML = filtradas.map(function (r) {
      const dias = Math.floor((new Date(r.fecha) - Date.now()) / 86400000);
      const n = r.cuposInscritos || 0;
      const cupo = r.cupoMaximo || 0;
      const pct = cupo ? Math.round((n / cupo) * 100) : 0;
      return '<div class="race-row"' + (view === 'pasadas' ? ' style="opacity:.75"' : '') + '>' +
        '<div class="race-date"><span class="day">' + L.fmtFecha(r.fecha).split(' ')[0] + '</span><span class="mon">' + (L.fmtFecha(r.fecha).split(' ')[1] || '') + '</span></div>' +
        '<div>' +
        '<strong style="font-family:var(--font-display);text-transform:uppercase;font-size:var(--fs-base)">' + L.esc(r.nombre) + '</strong>' +
        '<div class="text-tertiary" style="font-size:var(--fs-sm)">' + L.esc(r.circuito || '') + (r.fecha ? ' · ' + L.fmtHora(r.fecha) : '') + '</div>' +
        '</div>' +
        '<div><span class="chip chip-category">' + L.esc(r.categoriaNombre || '') + '</span></div>' +
        '<div class="race-ocup" style="min-width:110px">' +
        '<div class="progress-label" style="margin-top:0"><span>Inscriptos</span><span class="mono">' + n + ' / ' + (cupo || '—') + '</span></div>' +
        '<div class="progress"><div class="progress-bar' + (n >= cupo && cupo > 0 ? ' full' : '') + '" style="width:' + pct + '%"></div></div>' +
        '</div>' +
        '<span class="chip chip-pending">' + (function () {
          if (r.estado === 'CANCELADA') return L.chipCarrera('CANCELADA');
          const lleno = n >= cupo && cupo > 0;
          if (r.estado === 'PROGRAMADA' || r.estado === 'INSCRIPCIONES_ABIERTAS') {
            return lleno ? '<span class="chip chip-closed">Cupo lleno</span>' : L.chipCarrera('INSCRIPCIONES_ABIERTAS');
          }
          return L.chipCarrera(r.estado);
        })() + '</span>' +
        '<a href="04-race-detail.html?id=' + r.id + '" class="btn btn-primary btn-sm">' + (view === 'pasadas' ? 'Resultados' : 'Ver carrera') + '</a>' +
        '</div>';
    }).join('');
  }

  L.api('/categorias').then(function (cats) {
    selectCat.innerHTML = '<option value="">Todas las categorías</option>' + cats.map(function (c) {
      return '<option value="' + c.id + '">' + L.esc(c.nombre) + '</option>';
    }).join('');

    const catParam = new URLSearchParams(location.search).get('cat');
    if (catParam && selectCat.querySelector('option[value="' + catParam + '"]')) {
      selectCat.value = catParam;
      filtroCat = catParam;
      render();
    }
  }).catch(function () {});

  selectCat.addEventListener('change', function () { filtroCat = selectCat.value; render(); });
  tabProx.addEventListener('click', function () {
    view = 'proximas';
    tabProx.classList.add('active');
    tabPasadas.classList.remove('active');
    render();
  });
  tabPasadas.addEventListener('click', function () {
    view = 'pasadas';
    tabPasadas.classList.add('active');
    tabProx.classList.remove('active');
    render();
  });

  Promise.all([
    L.api('/carreras/proximas').catch(function () { return []; }),
    L.api('/carreras/pasadas').catch(function () { return []; })
  ]).then(function (res) {
    proximas = res[0];
    pasadas = res[1];
    render();
  });
})();
