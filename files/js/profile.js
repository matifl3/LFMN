/* LFM Nacional — 09 · Mi perfil */
(function () {
  'use strict';
  const L = window.LFM;

  const user = L.requireAuth();
  if (!user) return;

  function refreshHeader() {
    const u = L.getUser();
    if (u) {
      document.getElementById('mp-nombre').value = u.nombrePiloto || '';
      document.getElementById('mp-mail').value = u.email || '';
      document.getElementById('mp-foto').value = u.fotoPerfil || '';
    }
    document.getElementById('mp-perfil-publico').href = '08-driver-profile.html?id=' + user.id;
  }

  const tabs = document.querySelectorAll('.tabs-pill .tab[data-panel]');
  tabs.forEach(function (tab) {
    tab.addEventListener('click', function () {
      tabs.forEach(function (t) { t.classList.remove('active'); });
      tab.classList.add('active');
      ['datos', 'password', 'steam'].forEach(function (p) {
        document.getElementById('mp-panel-' + p).style.display = (p === tab.dataset.panel) ? 'block' : 'none';
      });
    });
  });

  document.getElementById('mp-guardar').addEventListener('click', async function () {
    try {
      const updated = await L.put('/usuarios/' + user.id + '/perfil', {
        email: document.getElementById('mp-mail').value.trim(),
        nombrePiloto: document.getElementById('mp-nombre').value.trim(),
        fotoPerfil: document.getElementById('mp-foto').value.trim() || null
      });
      L.updateUser(updated);
      refreshHeader();
      L.toast('Perfil actualizado', 'success');
    } catch (err) { L.toast(err.message, 'error'); }
  });

  document.getElementById('mp-cambiar-pass').addEventListener('click', async function () {
    const actual = document.getElementById('mp-pass-actual').value;
    const nueva = document.getElementById('mp-pass-nueva').value;
    if (!actual || !nueva) { L.toast('Completá ambos campos.', 'error'); return; }
    try {
      await L.put('/usuarios/' + user.id + '/password', { passwordActual: actual, nuevaPassword: nueva });
      L.toast('Contraseña actualizada', 'success');
      document.getElementById('mp-pass-actual').value = '';
      document.getElementById('mp-pass-nueva').value = '';
    } catch (err) { L.toast(err.message, 'error'); }
  });

  function renderSteam() {
    const u = L.getUser();
    const vinculado = u && u.guidSteam;
    document.getElementById('mp-steam-nombre').textContent = vinculado ? (u.guidSteam + ' conectado') : 'Sin vincular';
    document.getElementById('mp-steam-chip').textContent = vinculado ? 'Verificado' : 'Sin vincular';
    document.getElementById('mp-steam-chip').className = 'chip ' + (vinculado ? 'chip-confirmed' : 'chip-pending');
  }

  document.getElementById('mp-steam-vincular').addEventListener('click', async function () {
    const guid = document.getElementById('mp-steam-guid').value.trim();
    if (!guid) { L.toast('Ingresá el GUID de Steam.', 'error'); return; }
    try {
      const updated = await L.put('/usuarios/' + user.id + '/steam', { guidSteam: guid });
      L.updateUser(updated);
      renderSteam();
      L.toast('Cuenta de Steam vinculada', 'success');
    } catch (err) { L.toast(err.message, 'error'); }
  });

  document.getElementById('mp-steam-desvincular').addEventListener('click', async function () {
    try {
      const updated = await L.del('/usuarios/' + user.id + '/steam');
      L.updateUser(updated);
      renderSteam();
      L.toast('Cuenta de Steam desvinculada', 'success');
    } catch (err) { L.toast(err.message, 'error'); }
  });

  function renderInscripciones(list, carrerasMap) {
    const box = document.getElementById('mp-inscripciones');
    if (!list.length) {
      box.innerHTML = '<p class="text-tertiary">No tenés inscripciones activas.</p>';
      return;
    }
    box.innerHTML = list.map(function (ins) {
      const cr = carrerasMap[ins.carreraId] || {};
      const chip = ins.estado === 'LISTA_ESPERA'
        ? '<span class="chip chip-pending">En espera</span>'
        : '<span class="chip chip-confirmed">Confirmado</span>';
      return '<div class="race-row" style="grid-template-columns:64px 2fr 1fr auto">' +
        '<div class="race-date"><span class="day">' + L.fmtFecha(cr.fecha).split(' ')[0] + '</span><span class="mon">' + (L.fmtFecha(cr.fecha).split(' ')[1] || '') + '</span></div>' +
        '<div>' +
        '<strong style="font-family:var(--font-display); text-transform:uppercase; font-size:var(--fs-base)"><a href="04-race-detail.html?id=' + ins.carreraId + '" class="link">' + L.esc(cr.nombre || ('Carrera #' + ins.carreraId)) + '</a></strong>' +
        '<div class="text-tertiary" style="font-size:var(--fs-sm)">' + L.esc(cr.categoriaNombre || '') + '</div>' +
        '</div>' +
        chip +
        '<button type="button" class="btn btn-danger btn-sm" data-baja="' + ins.carreraId + '">Cancelar</button>' +
        '</div>';
    }).join('');

    box.querySelectorAll('[data-baja]').forEach(function (btn) {
      btn.addEventListener('click', async function () {
        const carreraId = btn.dataset.baja;
        try {
          await L.del('/inscripciones/carrera/' + carreraId + '/usuario/' + user.id);
          L.toast('Inscripción cancelada', 'success');
          loadInscripciones();
        } catch (err) { L.toast(err.message, 'error'); }
      });
    });
  }

  async function loadInscripciones() {
    try {
      const [list, carreras] = await Promise.all([
        L.api('/inscripciones/usuario/' + user.id).catch(function () { return []; }),
        L.api('/carreras').catch(function () { return []; })
      ]);
      const map = {};
      carreras.forEach(function (c) { map[c.id] = c; });
      renderInscripciones(list.filter(function (i) { return i.estado !== 'CANCELADA'; }), map);
    } catch (err) {
      document.getElementById('mp-inscripciones').innerHTML = '<p class="text-tertiary">No se pudieron cargar las inscripciones.</p>';
    }
  }

  function renderRecompensas(list) {
    const box = document.getElementById('mp-recompensas');
    if (!list.length) {
      box.innerHTML = '<p class="text-tertiary">No tenés recompensas pendientes.</p>';
      return;
    }
    box.innerHTML = list.map(function (r) {
      return '<div class="card card-raised" style="padding:var(--sp-4)">' +
        '<div class="flex-between" style="margin-bottom:var(--sp-2)">' +
        '<strong style="font-size:var(--fs-sm)">' + L.esc(r.descripcion || 'Recompensa') + '</strong>' +
        '<span class="chip chip-confirmed" style="padding:.2em .6em">Lista</span>' +
        '</div>' +
        '<button type="button" class="btn btn-primary btn-sm btn-block" data-reclamo="' + r.recompensaId + '">Reclamar</button>' +
        '</div>';
    }).join('');

    box.querySelectorAll('[data-reclamo]').forEach(function (btn) {
      btn.addEventListener('click', async function () {
        try {
          await L.post('/recompensas/usuario/' + user.id + '/recompensas/' + btn.dataset.reclamo + '/reclamar');
          L.toast('¡Recompensa reclamada!', 'success');
          loadRecompensas();
        } catch (err) { L.toast(err.message, 'error'); }
      });
    });
  }

  async function loadRecompensas() {
    try {
      const list = await L.api('/usuarios/' + user.id + '/recompensas/no-reclamadas').catch(function () { return []; });
      renderRecompensas(list);
    } catch (err) {
      document.getElementById('mp-recompensas').innerHTML = '<p class="text-tertiary">—</p>';
    }
  }

  refreshHeader();
  renderSteam();
  loadInscripciones();
  loadRecompensas();
})();
