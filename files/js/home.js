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
    var statPilotos = document.getElementById('stat-pilotos');
    var statCarreras = document.getElementById('stat-carreras');
    var statCategorias = document.getElementById('stat-categorias');
    var racesBox = document.getElementById('home-next-races');
    statPilotos.textContent = '---';
    statCarreras.textContent = '---';
    statCategorias.textContent = '---';
    racesBox.innerHTML = '<p class="text-tertiary" style="font-size:var(--fs-sm)">Cargando carreras…</p>';

    const [usuarios, pasadas, categorias, proximas, campeonatos] = await Promise.all([
      L.api('/usuarios').catch(function () { L.toast('Error al cargar pilotos', 'error'); return []; }),
      L.api('/carreras/pasadas').catch(function () { L.toast('Error al cargar carreras', 'error'); return []; }),
      L.api('/categorias').catch(function () { L.toast('Error al cargar categorías', 'error'); return []; }),
      L.api('/carreras/proximas').catch(function () { L.toast('Error al cargar carreras próximas', 'error'); return []; }),
      L.api('/campeonatos').catch(function () { L.toast('Error al cargar campeonatos', 'error'); return []; })
    ]);

    document.getElementById('stat-pilotos').textContent = (usuarios.length || 0).toLocaleString('es-AR');
    document.getElementById('stat-carreras').textContent = (pasadas.length || 0).toLocaleString('es-AR');
    document.getElementById('stat-categorias').textContent = (categorias.length || 0).toLocaleString('es-AR');

    // Próximas carreras
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
      }).catch(function () { document.getElementById('home-champ-top').innerHTML = '<p class="text-tertiary" style="font-size:var(--fs-sm)">Error al cargar posiciones.</p>'; });
    }

    // Último anuncio
    L.api('/anuncios/ultimo').then(function (an) {
      if (!an) return;
      var img = document.getElementById('home-anuncio-img');
      if (an.urlImagen) {
        img.src = an.urlImagen;
        img.alt = an.titulo;
        img.style.display = 'block';
      }
      document.getElementById('home-anuncio-fecha').textContent = L.fechaRelativa(an.fecha);
      document.getElementById('home-anuncio-contenido').textContent = an.contenido || an.titulo;
      document.getElementById('home-anuncio-link').textContent = 'Leer completo';

      var modal = document.getElementById('anuncio-modal');
      var modalImg = document.getElementById('anuncio-modal-img');
      var modalTitulo = document.getElementById('anuncio-modal-titulo');
      var modalFecha = document.getElementById('anuncio-modal-fecha');
      var modalContenido = document.getElementById('anuncio-modal-contenido');

      document.getElementById('home-anuncio-link').addEventListener('click', function (e) {
        e.preventDefault();
        modalTitulo.textContent = an.titulo;
        modalFecha.textContent = L.fechaRelativa(an.fecha);
        modalContenido.textContent = an.contenido || an.titulo;
        if (an.urlImagen) {
          modalImg.src = an.urlImagen;
          modalImg.alt = an.titulo;
          modalImg.style.display = 'block';
        } else {
          modalImg.style.display = 'none';
        }
        modal.style.display = 'flex';
      });

      function cerrarModal() { modal.style.display = 'none'; }
      document.getElementById('anuncio-modal-close').addEventListener('click', cerrarModal);
      modal.addEventListener('click', function (e) { if (e.target === modal) cerrarModal(); });
      document.addEventListener('keydown', function (e) { if (e.key === 'Escape') cerrarModal(); });
    }).catch(function () {});
  }

  load();
})();
