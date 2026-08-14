/* LFM Nacional — 04 · Detalle de carrera */
(function () {
  'use strict';
  const L = window.LFM;

  const params = new URLSearchParams(location.search);
  const id = params.get('id');
  if (!id) {
    location.href = '03-races-list.html';
    return;
  }

  let carrera = null;
  let inscriptos = [];
  let usuariosMap = {};

  function usuarioDe(u) {
    return usuariosMap[u] || { id: u, nombrePiloto: 'Piloto #' + u };
  }

  function estadoChip() {
    if (!carrera) return L.chipEstado('', null);
    if (carrera.estado === 'CANCELADA') return L.chipCarrera('CANCELADA');
    const cupo = carrera.cupoMaximo || 0;
    const n = inscriptos.filter(function (i) { return i.estado !== 'CANCELADA'; }).length;
    if (carrera.estado === 'PROGRAMADA' || carrera.estado === 'INSCRIPCIONES_ABIERTAS') {
      if (n >= cupo && cupo > 0) return L.chipEstado('INSCRIPCIONES_CERRADAS', { INSCRIPCIONES_CERRADAS: ['chip-closed', 'Cupo lleno'] });
      return L.chipEstado('INSCRIPCIONES_ABIERTAS', { INSCRIPCIONES_ABIERTAS: ['chip-upcoming', 'Inscripciones abiertas'] });
    }
    return L.chipCarrera(carrera.estado);
  }

  function renderCarrera() {
    document.title = carrera.nombre + ' — LFM Nacional';
    document.getElementById('race-eyebrow').textContent = (carrera.categoriaNombre || 'CARRERA') + ' · RONDA';
    document.getElementById('race-titulo').textContent = carrera.nombre;
    document.getElementById('race-subtitulo').textContent = (carrera.circuito || '') + (carrera.fecha ? ' — ' + L.fmtFechaHora(carrera.fecha) : '');
    document.getElementById('race-estado-chip').innerHTML = estadoChip();
    document.getElementById('race-fecha').textContent = L.fmtFechaHora(carrera.fecha);
    document.getElementById('race-circuito').textContent = carrera.circuito || '—';
    document.getElementById('race-categoria').textContent = carrera.categoriaNombre || '—';
    document.getElementById('race-servidor').textContent = carrera.servidor || '—';

    const activas = inscriptos.filter(function (i) { return i.estado !== 'CANCELADA'; }).length;
    const cupo = carrera.cupoMaximo;
    document.getElementById('race-cupo').textContent = activas + ' / ' + (cupo || '—');
    const pct = cupo ? Math.round((activas / cupo) * 100) : 0;
    const bar = document.getElementById('race-cupo-bar');
    bar.style.width = pct + '%';
    if (activas >= cupo && cupo > 0) bar.classList.add('full');
  }

  function renderInscriptos() {
    const activas = inscriptos.filter(function (i) { return i.estado !== 'CANCELADA'; });
    document.getElementById('race-inscriptos-count').textContent = activas.length + ' / ' + (carrera.cupoMaximo || '—');

    const box = document.getElementById('race-inscriptos-list');
    if (!activas.length) {
      box.innerHTML = '<p class="text-tertiary" style="font-size:var(--fs-sm)">Todavía no hay inscriptos. ¡Sé el primero!</p>';
      return;
    }
    box.innerHTML = activas.map(function (i) {
      const u = usuarioDe(i.usuarioId);
      const chip = i.estado === 'LISTA_ESPERA' ? '<span class="chip chip-pending">En espera</span>' : '<span class="chip chip-confirmed">Confirmado</span>';
      return '<div class="entrant-row">' +
        L.avatarHtml(u, 36) +
        '<div style="flex:1">' +
        '<strong style="font-size:var(--fs-sm)">' + L.esc(u.nombrePiloto) + '</strong>' +
        '<div class="text-tertiary mono" style="font-size:var(--fs-2xs)">Elo ' + (u.elo ?? '—') + ' · SR ' + (u.safetyRating ?? '—') + '</div>' +
        '</div>' +
        chip +
        '</div>';
    }).join('');
  }

  function renderArchivos() {
    const box = document.getElementById('race-archivos');
    const files = [];
    if (carrera.archivoId) {
      files.push({ id: carrera.archivoId, nombre: carrera.archivoNombre || ('Archivo #' + carrera.archivoId), tipo: 'reglamento' });
    }
    if (!files.length) {
      box.innerHTML = '<p class="text-tertiary" style="font-size:var(--fs-sm)">No hay archivos publicados para esta carrera.</p>';
      return;
    }
    box.innerHTML = files.map(function (f) {
      const color = f.tipo === 'setup' ? 'var(--amber)' : 'var(--celeste)';
      return '<div class="file-row">' +
        '<span class="flex gap-3" style="align-items:center">' +
        '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color:' + color + '"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6"/></svg>' +
        '<span>' + L.esc(f.nombre) + '</span></span>' +
        '<a href="' + L.API_BASE + '/api/archivos/' + f.id + '/descargar" class="btn btn-ghost btn-sm">Descargar</a>' +
        '</div>';
    }).join('');
  }

  function renderResultados() {
    L.api('/resultados/carrera/' + id).then(function (res) {
      const tbody = document.getElementById('race-resultados');
      if (!res.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-tertiary">Sin resultados publicados.</td></tr>';
        return;
      }
      const medal = ['gold', 'silver', 'bronze'];
      const order = res.slice().sort(function (a, b) { return (a.posicionFinal || 999) - (b.posicionFinal || 999); });
      tbody.innerHTML = order.map(function (r, idx) {
        const rowClass = idx === 0 ? 'podium-1' : (idx === 1 ? 'podium-2' : (idx === 2 ? 'podium-3' : ''));
        const elo = r.eloGanado;
        const sr = r.srGanado;
        const eloHtml = elo === null || elo === undefined ? '—'
          : '<span class="mono" style="color:' + (elo >= 0 ? 'var(--success)' : 'var(--danger)') + '">' + (elo >= 0 ? '+' : '') + elo + '</span>';
        const srHtml = sr === null || sr === undefined ? '—'
          : '<span class="mono" style="color:' + (sr >= 0 ? 'var(--success)' : 'var(--danger)') + '">' + (sr >= 0 ? '+' : '') + sr + '</span>';
        const pos = r.finalizo === false ? '<span class="chip chip-dnf" style="padding:.2em .6em">DNF</span>'
          : '<span class="rank-badge ' + (medal[idx] || '') + '">' + (r.posicionFinal ?? idx + 1) + '</span>';
        return '<tr class="' + rowClass + '">' +
          '<td>' + pos + '</td>' +
          '<td class="data"><a href="08-driver-profile.html?id=' + r.usuarioId + '" class="link">' + L.esc(usuarioDe(r.usuarioId).nombrePiloto) + '</a></td>' +
          '<td class="num mono">' + L.fmtLap(r.vueltaRapida) + '</td>' +
          '<td class="num">' + eloHtml + '</td>' +
          '<td class="num">' + srHtml + '</td>' +
          '</tr>';
      }).join('');
    }).catch(function () {});
  }

  function renderClasificacion() {
    L.api('/clasificaciones/carrera/' + id).then(function (res) {
      const tbody = document.getElementById('race-clasificacion');
      if (!res.length) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-tertiary">Sin tiempos registrados.</td></tr>';
        return;
      }
      const medal = ['gold', 'silver', 'bronze'];
      tbody.innerHTML = res.slice().sort(function (a, b) { return (a.tiempo || 0) - (b.tiempo || 0); }).map(function (c, idx) {
        const rowClass = idx === 0 ? 'podium-1' : (idx === 1 ? 'podium-2' : (idx === 2 ? 'podium-3' : ''));
        return '<tr class="' + rowClass + '">' +
          '<td><span class="rank-badge ' + (medal[idx] || '') + '">' + (idx + 1) + '</span></td>' +
          '<td class="data"><a href="08-driver-profile.html?id=' + c.usuarioId + '" class="link">' + L.esc(usuarioDe(c.usuarioId).nombrePiloto) + '</a></td>' +
          '<td class="num mono">' + L.fmtLap(c.tiempo) + '</td>' +
          '</tr>';
      }).join('');
    }).catch(function () {});
  }

  function setupInscribir() {
    const btn = document.getElementById('race-inscribir');
    const user = L.getUser();

    if (!user) {
      btn.textContent = 'Ingresar para inscribirme';
      btn.href = '02-auth.html?next=' + encodeURIComponent('04-race-detail.html?id=' + id);
      return;
    }

    const yaInscripto = inscriptos.some(function (i) { return i.usuarioId === user.id && i.estado !== 'CANCELADA'; });

    if (carrera.estado !== 'PROGRAMADA' && carrera.estado !== 'INSCRIPCIONES_ABIERTAS') {
      btn.textContent = 'Inscripción cerrada';
      btn.setAttribute('aria-disabled', 'true');
      btn.style.pointerEvents = 'none';
      btn.style.opacity = '.5';
      return;
    }

    if (yaInscripto) {
      btn.textContent = 'Darme de baja';
      btn.href = '#';
      btn.addEventListener('click', async function (e) {
        e.preventDefault();
        try {
          await L.del('/inscripciones/carrera/' + id + '/usuario/' + user.id);
          L.toast('Te diste de baja de la carrera.', 'success');
          location.reload();
        } catch (err) { L.toast(err.message, 'error'); }
      });
    } else {
      btn.textContent = 'Inscribirme';
      btn.href = '#';
      btn.addEventListener('click', async function (e) {
        e.preventDefault();
        try {
          await L.post('/inscripciones', { carreraId: Number(id), usuarioId: user.id });
          L.toast('¡Te inscribiste correctamente!', 'success');
          location.reload();
        } catch (err) { L.toast(err.message, 'error'); }
      });
    }
  }

  Promise.all([
    L.api('/carreras/' + id),
    L.api('/inscripciones/carrera/' + id).catch(function () { return []; }),
    L.api('/usuarios').catch(function () { return []; })
  ]).then(function (res) {
    carrera = res[0];
    inscriptos = res[1];
    res[2].forEach(function (u) { usuariosMap[u.id] = u; });
    renderCarrera();
    renderInscriptos();
    renderArchivos();
    setupInscribir();
    renderResultados();
    renderClasificacion();
  }).catch(function (err) {
    L.toast(err.message, 'error');
    document.getElementById('race-titulo').textContent = 'Carrera no encontrada';
  });
})();
