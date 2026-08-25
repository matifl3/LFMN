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
      const u = { nombrePiloto: i.nombrePiloto || ('Piloto #' + i.usuarioId), fotoPerfil: i.fotoPerfil };
      const chip = i.estado === 'LISTA_ESPERA' ? '<span class="chip chip-pending">En espera</span>' : '<span class="chip chip-confirmed">Confirmado</span>';
      return '<div class="entrant-row">' +
        L.avatarHtml(u, 36) +
        '<div style="flex:1">' +
        '<strong style="font-size:var(--fs-sm)">' + L.esc(u.nombrePiloto) + '</strong>' +
        '<div class="text-tertiary mono" style="font-size:var(--fs-2xs)">Elo ' + (i.elo ?? '—') + ' · SR ' + (i.safetyRating ?? '—') + '</div>' +
        '</div>' +
        chip +
        '</div>';
    }).join('');
  }

  function renderArchivos() {
    const box = document.getElementById('race-archivos');
    const hasPista = carrera.linkPista && carrera.linkPista.trim();
    const hasAuto = carrera.linkAuto && carrera.linkAuto.trim();
    if (!hasPista && !hasAuto) {
      box.innerHTML = '<p class="text-tertiary" style="font-size:var(--fs-sm)">No hay archivos publicados para esta carrera.</p>';
      return;
    }
    var html = '';
    if (hasPista) {
      html += '<a href="' + L.esc(L.sanitizeUrl(carrera.linkPista)) + '" target="_blank" rel="noopener" class="btn btn-ghost btn-sm">Descargar Pista</a>';
    }
    if (hasAuto) {
      html += '<a href="' + L.esc(L.sanitizeUrl(carrera.linkAuto)) + '" target="_blank" rel="noopener" class="btn btn-ghost btn-sm">Descargar Auto</a>';
    }
    box.innerHTML = html;
  }

  function fmtDif(ms) {
    if (ms === null || ms === undefined || isNaN(ms)) return '—';
    const sign = ms >= 0 ? '+' : '−';
    const total = Math.abs(ms) / 1000;
    const m = Math.floor(total / 60);
    const s = Math.floor(total % 60);
    const ml = Math.floor((total * 1000) % 1000);
    return sign + (m > 0 ? m + ':' : '') + String(s).padStart(2, '0') + '.' + String(ml).padStart(3, '0');
  }

  function autoHtml(auto, skin) {
    if (!auto) return '—';
    return L.esc(auto) + (skin ? ' <span class="text-tertiary">' + L.esc(skin) + '</span>' : '');
  }

  function renderResultados() {
    const tbody = document.getElementById('race-resultados');
    L.api('/resultados/carrera/' + id).then(function (res) {
      if (!res.length) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-tertiary">Sin resultados publicados.</td></tr>';
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
        const pole = r.poles === true ? '<span class="chip chip-pole">POLE</span>' : '—';
        return '<tr class="' + rowClass + '">' +
          '<td>' + pos + '</td>' +
          '<td class="data"><a href="08-driver-profile.html?id=' + r.usuarioId + '" class="link">' + L.esc(r.nombrePiloto || ('Piloto #' + r.usuarioId)) + '</a></td>' +
          '<td class="num mono">' + L.fmtLap(r.tiempoTotal) + '</td>' +
          '<td class="num mono">' + L.fmtLap(r.vueltaRapida) + '</td>' +
          '<td class="num">' + pole + '</td>' +
          '<td class="num">' + autoHtml(r.modeloAuto, r.skinAuto) + '</td>' +
          '<td class="num">' + eloHtml + '</td>' +
          '<td class="num">' + srHtml + '</td>' +
          '</tr>';
      }).join('');
    }).catch(function () { tbody.innerHTML = '<tr><td colspan="8" class="text-tertiary">Error al cargar resultados.</td></tr>'; });
  }

  function renderClasificacion() {
    const tbody = document.getElementById('race-clasificacion');
    L.api('/clasificaciones/carrera/' + id).then(function (res) {
      if (!res.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-tertiary">Sin tiempos registrados.</td></tr>';
        return;
      }
      const medal = ['gold', 'silver', 'bronze'];
      tbody.innerHTML = res.slice().sort(function (a, b) { return (a.tiempo || 0) - (b.tiempo || 0); }).map(function (c, idx) {
        const rowClass = idx === 0 ? 'podium-1' : (idx === 1 ? 'podium-2' : (idx === 2 ? 'podium-3' : ''));
        const dif = c.diferenciaPole === 0 ? '<span class="chip chip-pole">POLE</span>' : fmtDif(c.diferenciaPole);
        return '<tr class="' + rowClass + '">' +
          '<td><span class="rank-badge ' + (medal[idx] || '') + '">' + (idx + 1) + '</span></td>' +
          '<td class="data"><a href="08-driver-profile.html?id=' + c.usuarioId + '" class="link">' + L.esc(c.nombrePiloto || ('Piloto #' + c.usuarioId)) + '</a></td>' +
          '<td class="num mono">' + L.fmtLap(c.tiempo) + '</td>' +
          '<td class="num mono">' + dif + '</td>' +
          '<td class="num">' + autoHtml(c.modeloAuto, c.skinAuto) + '</td>' +
          '</tr>';
      }).join('');
    }).catch(function () { tbody.innerHTML = '<tr><td colspan="5" class="text-tertiary">Error al cargar clasificación.</td></tr>'; });
  }

  function renderAnalisis() {
    const user = L.getUser();
    const tbody = document.getElementById('race-analisis');
    if (!user) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-tertiary">Ingresá para ver tu análisis por vuelta.</td></tr>';
      return;
    }
    L.api('/vueltas/carrera/' + id + '/usuario/' + user.id).then(function (res) {
      if (!res.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-tertiary">Sin vueltas registradas.</td></tr>';
        return;
      }
      tbody.innerHTML = res.map(function (v) {
        return '<tr>' +
          '<td class="num mono">' + v.numeroVuelta + '</td>' +
          '<td class="num mono">' + L.fmtLap(v.tiempoMs) + '</td>' +
          '<td class="num mono">' + L.fmtLap(v.sector1) + '</td>' +
          '<td class="num mono">' + L.fmtLap(v.sector2) + '</td>' +
          '<td class="num mono">' + L.fmtLap(v.sector3) + '</td>' +
          '<td class="num">' + (v.cortes || 0) + '</td>' +
          '<td class="mono">' + L.esc(v.neumatico || '—') + '</td>' +
          '</tr>';
      }).join('');
    }).catch(function () { tbody.innerHTML = '<tr><td colspan="7" class="text-tertiary">Error al cargar análisis de vueltas.</td></tr>'; });
  }

  function setupInscribir() {
    const btn = document.getElementById('race-inscribir');
    const user = L.getUser();
    const newBtn = btn.cloneNode(true);
    btn.parentNode.replaceChild(newBtn, btn);

    if (!user) {
      newBtn.textContent = 'Ingresar para inscribirme';
      newBtn.href = '02-auth.html?next=' + encodeURIComponent('04-race-detail.html?id=' + id);
      return;
    }

    const yaInscripto = inscriptos.some(function (i) { return i.usuarioId === user.id && i.estado !== 'CANCELADA'; });

    if (carrera.estado !== 'PROGRAMADA' && carrera.estado !== 'INSCRIPCIONES_ABIERTAS') {
      newBtn.textContent = 'Inscripción cerrada';
      newBtn.setAttribute('aria-disabled', 'true');
      newBtn.style.pointerEvents = 'none';
      newBtn.style.opacity = '.5';
      return;
    }

    if (yaInscripto) {
      newBtn.textContent = 'Darme de baja';
      newBtn.href = '#';
      newBtn.addEventListener('click', async function (e) {
        e.preventDefault();
        newBtn.disabled = true;
        newBtn.textContent = 'Procesando...';
        try {
          await L.del('/inscripciones/carrera/' + id + '/usuario/' + user.id);
          L.toast('Te diste de baja de la carrera.', 'success');
          inscriptos = inscriptos.filter(function (i) { return !(i.usuarioId === user.id && i.estado !== 'CANCELADA'); });
          renderInscriptos();
          renderCarrera();
          setupInscribir();
        } catch (err) { L.toast(err.message, 'error'); newBtn.disabled = false; newBtn.textContent = 'Darme de baja'; }
      });
    } else {
      newBtn.textContent = 'Inscribirme';
      newBtn.href = '#';
      newBtn.addEventListener('click', async function (e) {
        e.preventDefault();
        newBtn.disabled = true;
        newBtn.textContent = 'Procesando...';
        try {
          await L.post('/inscripciones', { carreraId: Number(id) });
          L.toast('¡Te inscribiste correctamente!', 'success');
          const nueva = await L.api('/inscripciones/carrera/' + id).catch(function () { return []; });
          inscriptos = nueva;
          renderInscriptos();
          renderCarrera();
          setupInscribir();
        } catch (err) { L.toast(err.message, 'error'); newBtn.disabled = false; newBtn.textContent = 'Inscribirme'; }
      });
    }
  }

  Promise.all([
    L.api('/carreras/' + id),
    L.api('/inscripciones/carrera/' + id).catch(function () { L.toast('Error al cargar inscripciones', 'error'); return []; })
  ]).then(function (res) {
    carrera = res[0];
    inscriptos = res[1];
    renderCarrera();
    renderInscriptos();
    renderArchivos();
    setupInscribir();
    renderResultados();
    renderClasificacion();
    renderAnalisis();
  }).catch(function (err) {
    L.toast(err.message, 'error');
    document.getElementById('race-titulo').textContent = 'Carrera no encontrada';
  });
})();
