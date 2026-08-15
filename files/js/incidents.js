/* LFM Nacional — 12 · Incidentes y sanciones */
(function () {
  'use strict';
  const L = window.LFM;

  const listBox = document.getElementById('inc-list');
  let view = 'incidentes';
  let carreras = [];
  let incidentes = [];
  let sanciones = [];
  let apelaciones = [];

  const ESTADO_CHIP = {
    PENDIENTE: ['chip-pending', 'Pendiente'],
    EN_ANALISIS: ['chip-review', 'En análisis'],
    RESUELTO: ['chip-resolved', 'Resuelto']
  };

  function fmtCarrera(nombre, categoria) {
    return (nombre || 'Carrera') + (categoria ? ' — ' + categoria : '');
  }

  function setView(v) {
    view = v;
    document.getElementById('tab-incidentes').classList.toggle('active', v === 'incidentes');
    document.getElementById('tab-sanciones').classList.toggle('active', v === 'sanciones');
    document.getElementById('tab-apelaciones').classList.toggle('active', v === 'apelaciones');
    render();
  }

  function render() {
    if (view === 'incidentes') renderIncidentes();
    else if (view === 'sanciones') renderSanciones();
    else renderApelaciones();
  }

  function renderIncidentes() {
    document.getElementById('tab-incidentes').querySelector('.count').textContent = incidentes.length;
    if (!incidentes.length) {
      listBox.innerHTML = '<p class="text-tertiary">No hay incidentes reportados.</p>';
      return;
    }
    listBox.innerHTML = incidentes.map(function (i) {
      const chip = L.chipEstado(i.estado, ESTADO_CHIP);
      return '<div class="race-row" style="grid-template-columns:auto 2fr 1fr auto">' +
        chip +
        '<div>' +
        '<strong style="font-family:var(--font-display); text-transform:uppercase; font-size:var(--fs-base)">' + L.esc(i.descripcion || 'Incidente') + '</strong>' +
        '<div class="text-tertiary" style="font-size:var(--fs-sm)">' + L.esc(fmtCarrera(i.carreraNombre, i.categoriaNombre)) + ' · reportado por ' + L.esc(i.reportanteNombre || ('Piloto #' + i.reportanteId)) + '</div>' +
        '</div>' +
        '<span class="text-tertiary mono" style="font-size:var(--fs-xs)">' + (i.vuelta ? 'Vuelta ' + i.vuelta : '—') + '</span>' +
        (i.videoUrl ? '<a href="' + L.esc(i.videoUrl) + '" target="_blank" rel="noopener" class="btn btn-ghost btn-sm">Evidencia</a>' : '<span></span>') +
        '</div>';
    }).join('');
  }

  function renderSanciones() {
    document.getElementById('tab-sanciones').querySelector('.count').textContent = sanciones.length;
    if (!sanciones.length) {
      listBox.innerHTML = '<p class="text-tertiary">' + (L.getUser() ? 'No tenés sanciones registradas.' : 'Iniciá sesión para ver tus sanciones.') + '</p>';
      return;
    }
    listBox.innerHTML = sanciones.map(function (s) {
      let detalle = '';
      if (s.valor != null) {
        detalle = s.tipo === 'SEGUNDOS' ? '+' + s.valor + ' seg'
          : s.tipo === 'PUESTOS' ? '+' + s.valor + ' puestos'
          : s.tipo === 'ELO' ? (s.valor > 0 ? '+' : '') + s.valor + ' elo'
          : s.tipo === 'SAFETY_RATING' ? (s.valor > 0 ? '+' : '') + s.valor + ' SR'
          : String(s.tipo || '').replace(/_/g, ' ');
      }
      return '<div class="race-row" style="grid-template-columns:auto 2fr 1fr auto">' +
        '<span class="chip chip-sanctioned">Sanción</span>' +
        '<div>' +
        '<strong style="font-family:var(--font-display); text-transform:uppercase; font-size:var(--fs-base)">' + L.esc(s.motivo || 'Sanción') + '</strong>' +
        '<div class="text-tertiary" style="font-size:var(--fs-sm)">' + L.esc(fmtCarrera(s.carreraNombre, s.categoriaNombre)) + '</div>' +
        '</div>' +
        '<span class="text-tertiary mono" style="font-size:var(--fs-xs)">' + detalle + '</span>' +
        '<span class="text-tertiary mono" style="font-size:var(--fs-2xs)">' + L.fmtFecha(s.fecha) + '</span>' +
        '</div>';
    }).join('');
  }

  function renderApelaciones() {
    document.getElementById('tab-apelaciones').querySelector('.count').textContent = apelaciones.length;
    if (!apelaciones.length) {
      listBox.innerHTML = '<p class="text-tertiary">No hay apelaciones.</p>';
      return;
    }
    listBox.innerHTML = apelaciones.map(function (a) {
      const chip = a.estado === 'APROBADA'
        ? '<span class="chip chip-resolved">Aprobada</span>'
        : (a.estado === 'RECHAZADA' ? '<span class="chip chip-rejected">Rechazada</span>' : '<span class="chip chip-review">Pendiente</span>');
      return '<div class="race-row" style="grid-template-columns:auto 2fr 1fr auto">' +
        chip +
        '<div>' +
        '<strong style="font-family:var(--font-display); text-transform:uppercase; font-size:var(--fs-base)">' + L.esc(a.motivo || 'Apelación') + '</strong>' +
        '<div class="text-tertiary" style="font-size:var(--fs-sm)">' + L.esc(a.nombrePiloto || ('Piloto #' + a.usuarioId)) + '</div>' +
        '</div>' +
        '<span class="text-tertiary mono" style="font-size:var(--fs-xs)">' + L.fmtFecha(a.fecha) + '</span>' +
        '<span></span>' +
        '</div>';
    }).join('');
  }

  function load() {
    const user = L.getUser();
    Promise.all([
      L.api('/incidentes').catch(function () { return []; }),
      L.api('/carreras').catch(function () { return []; }),
      L.api('/apelaciones').catch(function () { return []; }),
      user ? L.api('/sanciones/usuario/' + user.id).catch(function () { return []; }) : Promise.resolve([])
    ]).then(function (res) {
      incidentes = res[0];
      carreras = res[1];
      apelaciones = res[2];
      sanciones = res[3];
      render();

      const sel = document.getElementById('inc-carrera');
      if (carreras.length) {
        sel.innerHTML = carreras.map(function (c) {
          return '<option value="' + c.id + '">' + L.esc(c.nombre + ' — ' + (c.categoriaNombre || '')) + '</option>';
        }).join('');
      } else {
        sel.innerHTML = '<option value="">Sin carreras disponibles</option>';
      }
    });
  }

  document.getElementById('tab-incidentes').addEventListener('click', function () { setView('incidentes'); });
  document.getElementById('tab-sanciones').addEventListener('click', function () { setView('sanciones'); });
  document.getElementById('tab-apelaciones').addEventListener('click', function () { setView('apelaciones'); });

  document.getElementById('inc-enviar').addEventListener('click', async function () {
    const user = L.requireAuth();
    if (!user) return;
    const carreraId = document.getElementById('inc-carrera').value;
    const descripcion = document.getElementById('inc-desc').value.trim();
    if (!carreraId) { L.toast('Seleccioná una carrera.', 'error'); return; }
    if (!descripcion) { L.toast('Describí el incidente.', 'error'); return; }
    const vuelta = document.getElementById('inc-vuelta').value ? Number(document.getElementById('inc-vuelta').value) : null;
    const videoUrl = document.getElementById('inc-video').value.trim() || null;
    try {
      await L.post('/incidentes', {
        carreraId: Number(carreraId),
        reportanteId: user.id,
        vuelta: vuelta,
        descripcion: descripcion,
        videoUrl: videoUrl
      });
      L.toast('Incidente reportado. El panel de comisarios lo va a revisar.', 'success');
      document.getElementById('inc-desc').value = '';
      document.getElementById('inc-vuelta').value = '';
      document.getElementById('inc-video').value = '';
      load();
    } catch (err) { L.toast(err.message, 'error'); }
  });

  load();
})();
