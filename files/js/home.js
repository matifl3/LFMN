/* LFM Nacional — 01 · Home */
(function () {
  'use strict';
  const L = window.LFM;

  const CTA_ESTADOS = ['PROGRAMADA', 'INSCRIPCIONES_ABIERTAS', 'INSCRIPCIONES_CERRADAS', 'EN_CURSO'];

  function setupCtaInscribir(proximas) {
    const btn = document.getElementById('home-cta-inscribir');
    const panel = document.getElementById('home-cta-panel');
    if (!btn || !panel) return;

    const programadas = proximas.filter(function (r) { return CTA_ESTADOS.indexOf(r.estado) !== -1; });

    if (!programadas.length) {
      btn.setAttribute('aria-disabled', 'true');
      btn.disabled = true;
      btn.style.opacity = '.5';
      panel.innerHTML = '<p class="text-tertiary" style="font-size:var(--fs-sm); padding:var(--sp-3)">No hay carreras próximas publicadas todavía.</p>';
      return;
    }

    panel.innerHTML = programadas.map(function (r) {
      return '<a class="cta-dropdown-item" href="04-race-detail.html?id=' + r.id + '">' +
        L.raceRow(r) +
        '</a>';
    }).join('');

    btn.addEventListener('click', function (e) {
      e.stopPropagation();
      btn.classList.toggle('open');
      panel.classList.toggle('open');
    });

    document.addEventListener('click', function (e) {
      if (!e.target.closest('.cta-dropdown')) {
        btn.classList.remove('open');
        panel.classList.remove('open');
      }
    });

    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') {
        btn.classList.remove('open');
        panel.classList.remove('open');
      }
    });
  }

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
    } else {
      racesBox.innerHTML = '<p class="text-tertiary" style="font-size:var(--fs-sm)">No hay carreras próximas publicadas todavía.</p>';
    }

    setupCtaInscribir(proximas);

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
