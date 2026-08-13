/* LFM Nacional — 01 · Home */
(function () {
  'use strict';
  const L = window.LFM;

  async function load() {
    const [usuarios, pasadas, categorias, proximas, campeonatos] = await Promise.all([
      L.api('/usuarios').catch(() => []),
      L.api('/carreras/pasadas').catch(() => []),
      L.api('/categorias').catch(() => []),
      L.api('/carreras/proximas').catch(() => []),
      L.api('/campeonatos').catch(() => [])
    ]);

    document.getElementById('stat-pilotos').textContent = (usuarios.length || 0).toLocaleString('es-AR');
    document.getElementById('stat-carreras').textContent = (pasadas.length || 0).toLocaleString('es-AR');
    document.getElementById('stat-categorias').textContent = (categorias.length || 0).toLocaleString('es-AR');

    // Próximas carreras
    const racesBox = document.getElementById('home-next-races');
    if (proximas.length) {
      racesBox.innerHTML = proximas.slice(0, 2).map(function (r) {
        return L.raceRow(r);
      }).join('');

      const primera = proximas[0];
      const cta = document.getElementById('home-cta-inscribir');
      cta.href = '04-race-detail.html?id=' + primera.id;
    } else {
      racesBox.innerHTML = '<p class="text-tertiary" style="font-size:var(--fs-sm)">No hay carreras próximas publicadas todavía.</p>';
      const cta = document.getElementById('home-cta-inscribir');
      cta.setAttribute('aria-disabled', 'true');
      cta.style.pointerEvents = 'none';
      cta.style.opacity = '.5';
    }

    // Campeonato
    if (campeonatos.length) {
      const campeonato = campeonatos[0];
      document.getElementById('home-champ-title').textContent = campeonato.nombre;
      const chip = document.getElementById('home-champ-chip');
      chip.textContent = campeonato.temporada || 'Temp. 2026';
      chip.href = '05-championship.html?id=' + campeonato.id;
      L.api('/campeonatos/' + campeonato.id + '/tabla').then(function (tabla) {
        const box = document.getElementById('home-champ-top');
        if (!tabla.length) {
          box.innerHTML = '<p class="text-tertiary" style="font-size:var(--fs-sm)">Aún no hay posiciones registradas.</p>';
          return;
        }
        const medal = ['gold', 'silver', 'bronze'];
        box.innerHTML = tabla.slice(0, 3).map(function (t, i) {
          return '<div class="flex-between">' +
            '<span class="flex gap-2" style="align-items:center"><span class="rank-badge ' + (medal[i] || '') + '">' + t.posicion + '</span> ' + L.esc(t.nombrePiloto) + '</span>' +
            '<span class="mono data">' + t.puntos + ' pts</span></div>';
        }).join('');
      }).catch(function () {});
    }

    // Último anuncio
    L.api('/anuncios/ultimo').then(function (an) {
      if (!an) return;
      document.getElementById('home-anuncio-fecha').textContent = L.fechaRelativa(an.fecha);
      document.getElementById('home-anuncio-contenido').textContent = an.contenido || an.titulo;
      document.getElementById('home-anuncio-link').textContent = 'Leer completo';
      document.getElementById('home-anuncio-link').href = '03-races-list.html';
    }).catch(function () {});
  }

  load();
})();
