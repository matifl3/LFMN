/* LFM Nacional — 12 · Incidentes, sanciones y apelaciones */
(function () {
  'use strict';
  const L = window.LFM;

  const listBox = document.getElementById('inc-list');
  let view = 'incidentes';
  let carreras = [];
  let incidentes = [];
  let sanciones = [];
  let apelaciones = [];
  let usuarios = [];
  let allSanciones = [];

  const ESTADO_CHIP = {
    PENDIENTE: ['chip-pending', 'Pendiente'],
    EN_ANALISIS: ['chip-review', 'En análisis'],
    RESUELTO: ['chip-resolved', 'Resuelto']
  };

  const ROL_COLORS = {
    CAUSANTE: '#ffb0b8',
    AFECTADO: '#a6f2cf'
  };

  const DECISION_CHIP = {
    A_FAVOR: ['chip-resolved', 'A favor'],
    EN_CONTRA: ['chip-rejected', 'En contra'],
    ABSTENCION: ['chip-closed', 'Abstención']
  };

  function fmtCarrera(nombre, categoria) {
    return (nombre || 'Carrera') + (categoria ? ' — ' + categoria : '');
  }

  function isComisarioOrAdmin() {
    const u = L.getUser();
    return u && (u.rol === 'COMISARIO' || u.rol === 'ADMIN');
  }

  function findUsuario(id) {
    return usuarios.find(function (u) { return u.id === id; });
  }

  function findSancion(id) {
    return allSanciones.find(function (s) { return s.id === id; });
  }

  /* ====== Tabs ====== */

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

  /* ====== Render: Incidentes ====== */

  function renderIncidentes() {
    document.getElementById('tab-incidentes').querySelector('.count').textContent = incidentes.length;
    const reportForm = document.querySelector('.report-form-panel');
    if (reportForm) reportForm.style.display = '';

    if (!incidentes.length) {
      listBox.innerHTML = '<p class="text-tertiary">No hay incidentes reportados.</p>';
      return;
    }
    listBox.innerHTML = incidentes.map(function (i) {
      const chip = L.chipEstado(i.estado, ESTADO_CHIP);
      return '<div class="race-row incidente-row" data-id="' + i.id + '" style="grid-template-columns:auto 2fr 1fr auto;cursor:pointer">' +
        chip +
        '<div>' +
        '<strong style="font-family:var(--font-display); text-transform:uppercase; font-size:var(--fs-base)">' + L.esc(i.descripcion || 'Incidente') + '</strong>' +
        '<div class="text-tertiary" style="font-size:var(--fs-sm)">' + L.esc(fmtCarrera(i.carreraNombre, i.categoriaNombre)) + ' · reportado por ' + L.esc(i.reportanteNombre || ('Piloto #' + i.reportanteId)) + '</div>' +
        '</div>' +
        '<span class="text-tertiary mono" style="font-size:var(--fs-xs)">' + (i.vuelta ? 'Vuelta ' + i.vuelta : '—') + '</span>' +
        (i.videoUrl ? '<a href="' + L.esc(L.sanitizeUrl(i.videoUrl)) + '" target="_blank" rel="noopener" class="btn btn-ghost btn-sm" onclick="event.stopPropagation()">Evidencia</a>' : '<span></span>') +
        '</div>';
    }).join('');

    listBox.querySelectorAll('.incidente-row').forEach(function (row) {
      row.addEventListener('click', function () {
        openIncidenteDetail(Number(row.dataset.id));
      });
    });
  }

  /* ====== Detalle de incidente ====== */

  async function openIncidenteDetail(incidenteId) {
    const modal = document.getElementById('modal-incidente');
    const body = document.getElementById('modal-incidente-body');
    modal.style.display = 'flex';
    body.innerHTML = '<p class="text-tertiary">Cargando…</p>';

    try {
      const inc = await L.api('/incidentes/' + incidenteId);
      const pilotos = await L.api('/incidentes/' + incidenteId + '/pilotos').catch(function () { return []; });
      const votos = await L.api('/votos/incidente/' + incidenteId).catch(function () { return []; });
      let resolucion = null;
      try { resolucion = await L.api('/incidentes/' + incidenteId + '/resolucion'); } catch (e) { /* no tiene resolución */ }

      const chip = L.chipEstado(inc.estado, ESTADO_CHIP);
      const esResuelto = inc.estado === 'RESUELTO';
      const esPendiente = inc.estado !== 'RESUELTO';

      let html = '';

      html += '<div style="margin-bottom:var(--sp-4)">' + chip + '</div>';
      html += '<div style="margin-bottom:var(--sp-3)"><strong style="font-size:var(--fs-base)">' + L.esc(inc.descripcion || 'Sin descripción') + '</strong></div>';
      html += '<div class="text-tertiary" style="font-size:var(--fs-sm);margin-bottom:var(--sp-2)">' + L.esc(fmtCarrera(inc.carreraNombre, inc.categoriaNombre)) + '</div>';
      html += '<div class="text-tertiary" style="font-size:var(--fs-sm);margin-bottom:var(--sp-2)">Reportado por: ' + L.esc(inc.reportanteNombre || ('Piloto #' + inc.reportanteId)) + (inc.vuelta ? ' · Vuelta ' + inc.vuelta : '') + '</div>';
      if (inc.videoUrl) {
        html += '<div style="margin-bottom:var(--sp-4)"><a href="' + L.esc(L.sanitizeUrl(inc.videoUrl)) + '" target="_blank" rel="noopener" class="btn btn-ghost btn-sm">Ver evidencia</a></div>';
      }

      /* Pilotos involucrados */
      html += '<h4 style="margin:var(--sp-5) 0 var(--sp-3)">Pilotos involucrados</h4>';
      if (pilotos.length) {
        html += '<div style="display:flex;flex-wrap:wrap;gap:var(--sp-2);margin-bottom:var(--sp-3)">';
        pilotos.forEach(function (p) {
          const color = ROL_COLORS[p.rol] || 'var(--text-secondary)';
          const nombre = p.nombrePiloto || ('Piloto #' + p.usuarioId);
          html += '<span style="display:inline-flex;align-items:center;gap:6px;padding:4px 10px;border-radius:var(--radius);border:1px solid ' + color + ';color:' + color + ';font-size:var(--fs-xs)">' +
            L.esc(nombre) +
            '<span style="opacity:.7;font-size:var(--fs-2xs)">' + L.esc(p.rol) + '</span></span>';
        });
        html += '</div>';
      } else {
        html += '<p class="text-tertiary" style="font-size:var(--fs-sm)">Sin pilotos asignados.</p>';
      }

      if (isComisarioOrAdmin() && esPendiente) {
        html += '<div style="margin:var(--sp-3) 0">';
        html += '<div class="input-group" style="margin-bottom:var(--sp-2)">';
        html += '<div class="field" style="flex:2"><label style="font-size:var(--fs-xs)">Piloto</label><select class="select" id="det-asignar-piloto" style="font-size:var(--fs-sm)"><option value="">Elegir…</option></select></div>';
        html += '<div class="field" style="flex:1"><label style="font-size:var(--fs-xs)">Rol</label><select class="select" id="det-asignar-rol" style="font-size:var(--fs-sm)"><option value="CAUSANTE">Causante</option><option value="AFECTADO">Afectado</option></select></div>';
        html += '</div>';
        html += '<button type="button" class="btn btn-ghost btn-sm" id="det-asignar-btn">Asignar piloto</button>';
        html += '</div>';
      }

      /* Votos */
      html += '<h4 style="margin:var(--sp-5) 0 var(--sp-3)">Votos de comisarios (' + votos.length + ')</h4>';
      if (votos.length) {
        votos.forEach(function (v) {
          const dec = DECISION_CHIP[v.decision] || ['chip-pending', v.decision];
          const comisario = findUsuario(v.comisarioId);
          const nombre = comisario ? comisario.nombrePiloto : ('Comisario #' + v.comisarioId);
          html += '<div style="display:flex;align-items:center;gap:var(--sp-2);margin-bottom:var(--sp-2);font-size:var(--fs-sm)">' +
            '<span class="chip ' + dec[0] + '">' + L.esc(dec[1]) + '</span>' +
            '<span>' + L.esc(nombre) + '</span>' +
            (v.comentario ? '<span class="text-tertiary" style="font-size:var(--fs-xs)"> — ' + L.esc(v.comentario) + '</span>' : '') +
            '</div>';
        });
      } else {
        html += '<p class="text-tertiary" style="font-size:var(--fs-sm)">Sin votos todavía.</p>';
      }

      if (isComisarioOrAdmin() && esPendiente) {
        html += '<div style="margin:var(--sp-3) 0">';
        html += '<div class="flex gap-2" style="margin-bottom:var(--sp-2)">';
        html += '<button type="button" class="btn btn-sm btn-voto" data-decision="A_FAVOR" style="flex:1;border-color:var(--success);color:var(--success)">A favor</button>';
        html += '<button type="button" class="btn btn-sm btn-voto" data-decision="EN_CONTRA" style="flex:1;border-color:var(--danger);color:var(--danger)">En contra</button>';
        html += '<button type="button" class="btn btn-sm btn-voto" data-decision="ABSTENCION" style="flex:1">Abstención</button>';
        html += '</div>';
        html += '<div class="field"><label style="font-size:var(--fs-xs)">Comentario (opcional)</label><input class="input" id="det-voto-comentario" style="font-size:var(--fs-sm)" placeholder="Comentario…"></div>';
        html += '</div>';
      }

      /* Resolución */
      if (resolucion) {
        html += '<h4 style="margin:var(--sp-5) 0 var(--sp-3)">Resolución</h4>';
        const resComisario = findUsuario(resolucion.comisarioId);
        html += '<div class="card" style="padding:var(--sp-3)">';
        html += '<div class="text-tertiary" style="font-size:var(--fs-sm);margin-bottom:var(--sp-2)">Resuelto por: ' + L.esc(resComisario ? resComisario.nombrePiloto : ('Comisario #' + resolucion.comisarioId)) + '</div>';
        html += '<p style="margin:0;font-size:var(--fs-sm)">' + L.esc(resolucion.explicacion) + '</p>';
        html += '</div>';
      }

      /* Resolver incidente */
      if (isComisarioOrAdmin() && esPendiente && votos.length >= (inc.quorumRequerido || 2)) {
        html += '<h4 style="margin:var(--sp-5) 0 var(--sp-3)">Resolver incidente</h4>';
        html += '<div class="field"><label style="font-size:var(--fs-xs)">Explicación</label><textarea class="textarea" id="det-resolucion-explicacion" rows="3" placeholder="Explicá la resolución del incidente…"></textarea></div>';
        html += '<div id="det-sanciones-list" style="margin-bottom:var(--sp-3)"></div>';
        html += '<button type="button" class="btn btn-ghost btn-sm" id="det-agregar-sancion-btn" style="margin-bottom:var(--sp-3)">+ Agregar sanción</button>';
        html += '<div id="det-sanciones-form" style="display:none;margin-bottom:var(--sp-3);padding:var(--sp-3);border:1px solid var(--line);border-radius:var(--radius)">';
        html += '<div class="input-group">';
        html += '<div class="field"><label style="font-size:var(--fs-xs)">Piloto</label><select class="select" id="det-san-piloto" style="font-size:var(--fs-sm)"><option value="">Elegir…</option></select></div>';
        html += '<div class="field"><label style="font-size:var(--fs-xs)">Tipo</label><select class="select" id="det-san-tipo" style="font-size:var(--fs-sm)">';
        html += '<option value="ELO">Elo</option><option value="SAFETY_RATING">Safety Rating</option><option value="PUESTOS">Puestos</option><option value="SEGUNDOS">Segundos</option><option value="DRIVE_THROUGH">Drive Through</option><option value="STOP_AND_GO">Stop & Go</option><option value="DESCALIFICACION">Descalificación</option>';
        html += '</select></div>';
        html += '</div>';
        html += '<div class="input-group">';
        html += '<div class="field"><label style="font-size:var(--fs-xs)">Valor</label><input class="input" id="det-san-valor" type="number" style="font-size:var(--fs-sm)" placeholder="-50"></div>';
        html += '<div class="field"><label style="font-size:var(--fs-xs)">Motivo</label><input class="input" id="det-san-motivo" style="font-size:var(--fs-sm)" placeholder="Motivo…"></div>';
        html += '</div>';
        html += '<div class="flex gap-2" style="justify-content:flex-end;margin-top:var(--sp-2)">';
        html += '<button type="button" class="btn btn-ghost btn-sm" id="det-san-cancelar">Cancelar</button>';
        html += '<button type="button" class="btn btn-primary btn-sm" id="det-san-guardar">Agregar</button>';
        html += '</div>';
        html += '</div>';
        html += '<button type="button" class="btn btn-primary btn-block" id="det-resolver-btn">Resolver incidente</button>';
      }

      body.innerHTML = html;

      /* Poblar selects de pilotos */
      populatePilotoSelect('det-asignar-piloto');
      populatePilotoSelect('det-san-piloto');

      /* Event listeners */
      if (isComisarioOrAdmin() && esPendiente) {
        const asignarBtn = document.getElementById('det-asignar-btn');
        if (asignarBtn) {
          asignarBtn.addEventListener('click', function () {
            asignarPilotoIncidente(incidenteId);
          });
        }

        body.querySelectorAll('.btn-voto').forEach(function (btn) {
          btn.addEventListener('click', function () {
            votarIncidente(incidenteId, btn.dataset.decision);
          });
        });
      }

      if (isComisarioOrAdmin() && esPendiente && votos.length >= (inc.quorumRequerido || 2)) {
        setupResolucionForm(incidenteId);
      }

    } catch (err) {
      body.innerHTML = '<p class="text-tertiary">Error: ' + L.esc(err.message) + '</p>';
    }
  }

  function populatePilotoSelect(selectId) {
    const sel = document.getElementById(selectId);
    if (!sel) return;
    sel.innerHTML = '<option value="">Elegir piloto…</option>' +
      usuarios.map(function (u) {
        return '<option value="' + u.id + '">' + L.esc(u.nombrePiloto || u.email) + ' (Elo ' + (u.elo || 0) + ')</option>';
      }).join('');
  }

  async function asignarPilotoIncidente(incidenteId) {
    const usuarioId = document.getElementById('det-asignar-piloto').value;
    const rol = document.getElementById('det-asignar-rol').value;
    if (!usuarioId) { L.toast('Elegí un piloto.', 'error'); return; }
    try {
      await L.post('/incidentes/' + incidenteId + '/pilotos', [
        { usuarioId: Number(usuarioId), rol: rol }
      ]);
      L.toast('Piloto asignado.', 'success');
      openIncidenteDetail(incidenteId);
    } catch (err) { L.toast(err.message, 'error'); }
  }

  async function votarIncidente(incidenteId, decision) {
    const user = L.requireAuth();
    if (!user) return;
    const comentario = (document.getElementById('det-voto-comentario') || {}).value || '';
    try {
      await L.post('/incidentes/' + incidenteId + '/votos', {
        comisarioId: user.id,
        decision: decision,
        comentario: comentario.trim() || null
      });
      L.toast('Voto registrado.', 'success');
      openIncidenteDetail(incidenteId);
    } catch (err) { L.toast(err.message, 'error'); }
  }

  let sancionesResolver = [];

  function setupResolucionForm(incidenteId) {
    sancionesResolver = [];
    const agregarBtn = document.getElementById('det-agregar-sancion-btn');
    const form = document.getElementById('det-sanciones-form');
    const guardarBtn = document.getElementById('det-san-guardar');
    const cancelarBtn = document.getElementById('det-san-cancelar');
    const resolverBtn = document.getElementById('det-resolver-btn');

    if (agregarBtn) {
      agregarBtn.addEventListener('click', function () {
        form.style.display = form.style.display === 'none' ? 'block' : 'none';
      });
    }
    if (cancelarBtn) {
      cancelarBtn.addEventListener('click', function () {
        form.style.display = 'none';
      });
    }
    if (guardarBtn) {
      guardarBtn.addEventListener('click', function () {
        const pilotoId = document.getElementById('det-san-piloto').value;
        const tipo = document.getElementById('det-san-tipo').value;
        const valor = document.getElementById('det-san-valor').value;
        const motivo = document.getElementById('det-san-motivo').value.trim();
        if (!pilotoId) { L.toast('Elegí un piloto para la sanción.', 'error'); return; }
        if (!motivo) { L.toast('Escribí el motivo.', 'error'); return; }
        sancionesResolver.push({
          usuarioId: Number(pilotoId),
          tipo: tipo,
          valor: valor ? Number(valor) : null,
          motivo: motivo,
          origen: 'COMISARIO'
        });
        form.style.display = 'none';
        renderSancionesResolverList();
        document.getElementById('det-san-piloto').value = '';
        document.getElementById('det-san-valor').value = '';
        document.getElementById('det-san-motivo').value = '';
      });
    }
    if (resolverBtn) {
      resolverBtn.addEventListener('click', function () {
        resolverIncidente(incidenteId);
      });
    }
  }

  function renderSancionesResolverList() {
    const container = document.getElementById('det-sanciones-list');
    if (!container) return;
    if (!sancionesResolver.length) { container.innerHTML = ''; return; }
    let html = '<div style="margin-bottom:var(--sp-2)">';
    sancionesResolver.forEach(function (s, idx) {
      const nombre = findUsuario(s.usuarioId);
      html += '<div style="display:flex;align-items:center;gap:var(--sp-2);margin-bottom:var(--sp-1);font-size:var(--fs-sm)">' +
        '<span class="chip chip-sanctioned">' + L.esc(s.tipo) + '</span>' +
        '<span>' + L.esc(nombre ? nombre.nombrePiloto : 'Piloto #' + s.usuarioId) + '</span>' +
        '<span class="text-tertiary">' + L.esc(s.motivo) + '</span>' +
        '<button type="button" class="btn btn-ghost btn-sm det-san-quitar" data-idx="' + idx + '" style="margin-left:auto;font-size:var(--fs-xs)">&times;</button>' +
        '</div>';
    });
    html += '</div>';
    container.innerHTML = html;
    container.querySelectorAll('.det-san-quitar').forEach(function (btn) {
      btn.addEventListener('click', function () {
        sancionesResolver.splice(Number(btn.dataset.idx), 1);
        renderSancionesResolverList();
      });
    });
  }

  async function resolverIncidente(incidenteId) {
    const user = L.requireAuth();
    if (!user) return;
    const explicacion = (document.getElementById('det-resolucion-explicacion') || {}).value || '';
    if (!explicacion.trim()) { L.toast('Escribí la explicación.', 'error'); return; }
    try {
      await L.post('/incidentes/' + incidenteId + '/resolucion', {
        comisarioId: user.id,
        explicacion: explicacion.trim(),
        sanciones: sancionesResolver.length ? sancionesResolver : null
      });
      L.toast('Incidente resuelto.', 'success');
      closeModal('modal-incidente');
      load();
    } catch (err) { L.toast(err.message, 'error'); }
  }

  /* ====== Render: Sanciones ====== */

  function renderSanciones() {
    document.getElementById('tab-sanciones').querySelector('.count').textContent = sanciones.length;
    const reportForm = document.querySelector('.report-form-panel');
    if (reportForm) reportForm.style.display = 'none';

    let html = '';

    if (isComisarioOrAdmin()) {
      html += '<div style="margin-bottom:var(--sp-4)"><button type="button" class="btn btn-primary btn-sm" id="btn-nueva-sancion">+ Nueva sanción</button></div>';
    }

    if (!sanciones.length) {
      html += '<p class="text-tertiary">' + (L.getUser() ? 'No tenés sanciones registradas.' : 'Iniciá sesión para ver tus sanciones.') + '</p>';
    } else {
      html += sanciones.map(function (s) {
        let detalle = '';
        if (s.valor != null) {
          detalle = s.tipo === 'SEGUNDOS' ? '+' + s.valor + ' seg'
            : s.tipo === 'PUESTOS' ? '+' + s.valor + ' puestos'
            : s.tipo === 'ELO' ? (s.valor > 0 ? '+' : '') + s.valor + ' elo'
            : s.tipo === 'SAFETY_RATING' ? (s.valor > 0 ? '+' : '') + s.valor + ' SR'
            : String(s.tipo || '').replace(/_/g, ' ');
        }
        const user = L.getUser();
        const puedeApelar = user && s.usuarioId === user.id && !apelacionExiste(s.id);
        return '<div class="race-row" style="grid-template-columns:auto 2fr 1fr auto">' +
          '<span class="chip chip-sanctioned">Sanción</span>' +
          '<div>' +
          '<strong style="font-family:var(--font-display); text-transform:uppercase; font-size:var(--fs-base)">' + L.esc(s.motivo || 'Sanción') + '</strong>' +
          '<div class="text-tertiary" style="font-size:var(--fs-sm)">' + L.esc(fmtCarrera(s.carreraNombre, s.categoriaNombre)) + '</div>' +
          '</div>' +
          '<span class="text-tertiary mono" style="font-size:var(--fs-xs)">' + detalle + '</span>' +
          '<div>' +
          (puedeApelar ? '<button type="button" class="btn btn-ghost btn-sm btn-apelar-sancion" data-sancion-id="' + s.id + '" data-sancion-motivo="' + L.esc(s.motivo || '') + '" data-sancion-carrera="' + L.esc(fmtCarrera(s.carreraNombre, s.categoriaNombre)) + '">Apelar</button>' : '') +
          '<span class="text-tertiary mono" style="font-size:var(--fs-2xs)">' + L.fmtFecha(s.fecha) + '</span>' +
          '</div>' +
          '</div>';
      }).join('');
    }

    listBox.innerHTML = html;

    if (isComisarioOrAdmin()) {
      const btn = document.getElementById('btn-nueva-sancion');
      if (btn) btn.addEventListener('click', openModalSancion);
    }

    listBox.querySelectorAll('.btn-apelar-sancion').forEach(function (btn) {
      btn.addEventListener('click', function () {
        openModalApelar(Number(btn.dataset.sancionId), btn.dataset.sancionMotivo, btn.dataset.sancionCarrera);
      });
    });
  }

  function apelacionExiste(sancionId) {
    const user = L.getUser();
    if (!user) return false;
    return apelaciones.some(function (a) { return a.sancionId === sancionId && a.usuarioId === user.id; });
  }

  /* ====== Render: Apelaciones ====== */

  function renderApelaciones() {
    document.getElementById('tab-apelaciones').querySelector('.count').textContent = apelaciones.length;
    const reportForm = document.querySelector('.report-form-panel');
    if (reportForm) reportForm.style.display = 'none';

    if (!apelaciones.length) {
      listBox.innerHTML = '<p class="text-tertiary">No hay apelaciones.</p>';
      return;
    }
    listBox.innerHTML = apelaciones.map(function (a) {
      const chip = a.estado === 'APROBADA'
        ? '<span class="chip chip-resolved">Aprobada</span>'
        : (a.estado === 'RECHAZADA' ? '<span class="chip chip-rejected">Rechazada</span>' : '<span class="chip chip-review">Pendiente</span>');
      const puedeResolver = isComisarioOrAdmin() && a.estado === 'PENDIENTE';
      return '<div class="race-row" style="grid-template-columns:auto 2fr 1fr auto">' +
        chip +
        '<div>' +
        '<strong style="font-family:var(--font-display); text-transform:uppercase; font-size:var(--fs-base)">' + L.esc(a.motivo || 'Apelación') + '</strong>' +
        '<div class="text-tertiary" style="font-size:var(--fs-sm)">' + L.esc(a.nombrePiloto || ('Piloto #' + a.usuarioId)) + '</div>' +
        (a.respuestaAdmin ? '<div class="text-tertiary" style="font-size:var(--fs-xs);margin-top:2px">Respuesta: ' + L.esc(a.respuestaAdmin) + '</div>' : '') +
        '</div>' +
        '<span class="text-tertiary mono" style="font-size:var(--fs-xs)">' + L.fmtFecha(a.fecha) + '</span>' +
        (puedeResolver ? '<button type="button" class="btn btn-ghost btn-sm btn-resolver-apelacion" data-id="' + a.id + '" data-piloto="' + L.esc(a.nombrePiloto || '') + '" data-motivo="' + L.esc(a.motivo || '') + '">Resolver</button>' : '<span></span>') +
        '</div>';
    }).join('');

    listBox.querySelectorAll('.btn-resolver-apelacion').forEach(function (btn) {
      btn.addEventListener('click', function () {
        openModalResolverApelacion(Number(btn.dataset.id), btn.dataset.piloto, btn.dataset.motivo);
      });
    });
  }

  /* ====== Modales ====== */

  function closeModal(id) {
    document.getElementById(id).style.display = 'none';
  }

  /* Modal: Apelar */
  function openModalApelar(sancionId, sancionMotivo, sancionCarrera) {
    document.getElementById('apelar-sancion-id').value = sancionId;
    document.getElementById('modal-apelar-sancion-info').textContent = 'Sanción: ' + (sancionMotivo || 'Sin motivo') + (sancionCarrera ? ' — ' + sancionCarrera : '');
    document.getElementById('apelar-motivo').value = '';
    document.getElementById('modal-apelar').style.display = 'flex';
  }

  /* Modal: Resolver apelación */
  function openModalResolverApelacion(id, piloto, motivo) {
    document.getElementById('resolver-apelacion-id').value = id;
    document.getElementById('modal-resolver-apelacion-info').textContent = 'Apelación de ' + (piloto || 'Piloto') + ': ' + (motivo || 'Sin motivo');
    document.getElementById('resolver-apelacion-respuesta').value = '';
    document.getElementById('resolver-apelacion-aprobar').style.borderColor = '';
    document.getElementById('resolver-apelacion-rechazar').style.borderColor = '';
    document.getElementById('resolver-apelacion-aprobar').dataset.selected = '';
    document.getElementById('resolver-apelacion-rechazar').dataset.selected = '';
    document.getElementById('modal-resolver-apelacion').style.display = 'flex';
  }

  /* Modal: Nueva sanción */
  function openModalSancion() {
    const selPiloto = document.getElementById('sancion-piloto');
    const selCarrera = document.getElementById('sancion-carrera');
    selPiloto.innerHTML = '<option value="">Seleccionar piloto…</option>' +
      usuarios.map(function (u) {
        return '<option value="' + u.id + '">' + L.esc(u.nombrePiloto || u.email) + '</option>';
      }).join('');
    selCarrera.innerHTML = '<option value="">Seleccionar carrera…</option>' +
      carreras.map(function (c) {
        return '<option value="' + c.id + '">' + L.esc(c.nombre + ' — ' + (c.categoriaNombre || '')) + '</option>';
      }).join('');
    document.getElementById('sancion-valor').value = '';
    document.getElementById('sancion-motivo').value = '';
    document.getElementById('modal-sancion').style.display = 'flex';
  }

  /* ====== Event Listeners: Modales ====== */

  document.getElementById('modal-incidente-cerrar').addEventListener('click', function () { closeModal('modal-incidente'); });
  document.getElementById('modal-apelar-cerrar').addEventListener('click', function () { closeModal('modal-apelar'); });
  document.getElementById('modal-resolver-apelacion-cerrar').addEventListener('click', function () { closeModal('modal-resolver-apelacion'); });
  document.getElementById('modal-sancion-cerrar').addEventListener('click', function () { closeModal('modal-sancion'); });

  document.getElementById('apelar-cancelar').addEventListener('click', function () { closeModal('modal-apelar'); });
  document.getElementById('resolver-apelacion-cancelar').addEventListener('click', function () { closeModal('modal-resolver-apelacion'); });
  document.getElementById('sancion-cancelar').addEventListener('click', function () { closeModal('modal-sancion'); });

  /* Click fuera del modal para cerrar */
  ['modal-incidente', 'modal-apelar', 'modal-resolver-apelacion', 'modal-sancion'].forEach(function (id) {
    document.getElementById(id).addEventListener('click', function (e) {
      if (e.target === e.currentTarget) closeModal(id);
    });
  });

  /* ====== Acciones: Apelar ====== */

  document.getElementById('apelar-enviar').addEventListener('click', async function () {
    const user = L.requireAuth();
    if (!user) return;
    const sancionId = Number(document.getElementById('apelar-sancion-id').value);
    const motivo = document.getElementById('apelar-motivo').value.trim();
    if (!motivo) { L.toast('Escribí el motivo de la apelación.', 'error'); return; }
    try {
      await L.post('/apelaciones', {
        sancionId: sancionId,
        motivo: motivo
      });
      L.toast('Apelación enviada. El panel de administración la va a revisar.', 'success');
      closeModal('modal-apelar');
      load();
    } catch (err) { L.toast(err.message, 'error'); }
  });

  /* ====== Acciones: Resolver apelación ====== */

  let resolucionApelacionEstado = '';

  document.getElementById('resolver-apelacion-aprobar').addEventListener('click', function () {
    resolucionApelacionEstado = 'APROBADA';
    this.style.borderColor = 'var(--success)';
    document.getElementById('resolver-apelacion-rechazar').style.borderColor = '';
  });
  document.getElementById('resolver-apelacion-rechazar').addEventListener('click', function () {
    resolucionApelacionEstado = 'RECHAZADA';
    this.style.borderColor = 'var(--danger)';
    document.getElementById('resolver-apelacion-aprobar').style.borderColor = '';
  });

  document.getElementById('resolver-apelacion-enviar').addEventListener('click', async function () {
    if (!resolucionApelacionEstado) { L.toast('Elegí aprobar o rechazar.', 'error'); return; }
    const id = Number(document.getElementById('resolver-apelacion-id').value);
    const respuesta = document.getElementById('resolver-apelacion-respuesta').value.trim();
    if (!respuesta) { L.toast('Escribí una respuesta al piloto.', 'error'); return; }
    try {
      await L.put('/apelaciones/' + id + '/resolucion', {
        estado: resolucionApelacionEstado,
        respuestaAdmin: respuesta
      });
      L.toast('Apelación resuelta.', 'success');
      closeModal('modal-resolver-apelacion');
      load();
    } catch (err) { L.toast(err.message, 'error'); }
  });

  /* ====== Acciones: Crear sanción manual ====== */

  document.getElementById('sancion-enviar').addEventListener('click', async function () {
    const user = L.requireAuth();
    if (!user) return;
    const pilotoId = document.getElementById('sancion-piloto').value;
    const carreraId = document.getElementById('sancion-carrera').value;
    const tipo = document.getElementById('sancion-tipo').value;
    const valor = document.getElementById('sancion-valor').value;
    const motivo = document.getElementById('sancion-motivo').value.trim();
    if (!pilotoId) { L.toast('Elegí un piloto.', 'error'); return; }
    if (!motivo) { L.toast('Escribí el motivo.', 'error'); return; }
    try {
      await L.post('/sanciones', {
        usuarioId: Number(pilotoId),
        carreraId: carreraId ? Number(carreraId) : null,
        tipo: tipo,
        valor: valor ? Number(valor) : null,
        motivo: motivo,
        origen: 'ADMIN'
      });
      L.toast('Sanción creada.', 'success');
      closeModal('modal-sancion');
      load();
    } catch (err) { L.toast(err.message, 'error'); }
  });

  /* ====== Tabs click ====== */

  document.getElementById('tab-incidentes').addEventListener('click', function () { setView('incidentes'); });
  document.getElementById('tab-sanciones').addEventListener('click', function () { setView('sanciones'); });
  document.getElementById('tab-apelaciones').addEventListener('click', function () { setView('apelaciones'); });

  /* ====== Reportar incidente (original) ====== */

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

  /* ====== Carga de datos ====== */

  function load() {
    const user = L.getUser();
    Promise.all([
      L.api('/incidentes').then(function (r) { return r.content || r; }).catch(function () { return []; }),
      L.api('/carreras').then(function (r) { return r.content || r; }).catch(function () { return []; }),
      L.api('/apelaciones').catch(function () { return []; }),
      user ? L.api('/sanciones/usuario/' + user.id).catch(function () { return []; }) : Promise.resolve([]),
      L.api('/usuarios/basico').catch(function () { return []; }),
      isComisarioOrAdmin() ? L.api('/sanciones').then(function (r) { return r.content || r; }).catch(function () { return []; }) : Promise.resolve([])
    ]).then(function (res) {
      incidentes = res[0];
      carreras = res[1];
      apelaciones = res[2];
      sanciones = res[3];
      usuarios = res[4];
      allSanciones = res[5];
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

  load();
})();
