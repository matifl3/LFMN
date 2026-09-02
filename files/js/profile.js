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
    if (!nueva) { L.toast('Escribí la nueva contraseña.', 'error'); return; }
    const body = { nuevaPassword: nueva };
    if (user.passwordEstablecida) {
      if (!actual) { L.toast('Escribí tu contraseña actual.', 'error'); return; }
      body.passwordActual = actual;
    }
    try {
      await L.put('/usuarios/' + user.id + '/password', body);
      L.clearSession();
      sessionStorage.setItem('lfm_msg_pass_changed', '1');
      location.href = '02-auth.html';
    } catch (err) { L.toast(err.message, 'error'); }
  });

  function renderPasswordPanel() {
    const sinPass = !user.passwordEstablecida;
    document.getElementById('mp-field-actual').style.display = sinPass ? 'none' : 'block';
    document.getElementById('mp-pass-note').style.display = sinPass ? 'block' : 'none';
    document.getElementById('mp-pass-titulo').textContent = sinPass ? 'Crear contraseña' : 'Cambiar contraseña';
    document.getElementById('mp-cambiar-pass').textContent = sinPass ? 'Crear contraseña' : 'Cambiar contraseña';
  }

  function renderSteam() {
    const u = L.getUser();
    const vinculado = u && u.guidSteam;
    document.getElementById('mp-steam-nombre').textContent = vinculado ? (u.guidSteam + ' conectado') : 'Sin vincular';
    document.getElementById('mp-steam-chip').textContent = vinculado ? 'Verificado' : 'Sin vincular';
    document.getElementById('mp-steam-chip').className = 'chip ' + (vinculado ? 'chip-confirmed' : 'chip-pending');
  }

  document.getElementById('mp-steam-vincular').addEventListener('click', async function () {
    try {
      const data = await L.api('/steam/vincular-url');
      if (data && data.url) {
        window.location.href = data.url;
      } else {
        L.toast('No se pudo iniciar la vinculación con Steam.', 'error');
      }
    } catch (err) { L.toast(err.message, 'error'); }
  });

  const steamResultado = new URLSearchParams(location.search).get('steam');
  if (steamResultado) {
    history.replaceState(null, '', location.pathname);
    if (steamResultado === 'ok') {
      L.toast('Cuenta de Steam vinculada', 'success');
    } else if (steamResultado === 'ocupado') {
      L.toast('Esa cuenta de Steam ya está vinculada a otro usuario', 'error');
    } else if (steamResultado === 'expirado') {
      L.toast('El enlace de vinculación expiró. Intentá de nuevo.', 'error');
    } else {
      L.toast('No se pudo vincular la cuenta de Steam.', 'error');
    }
    L.api('/usuarios/' + user.id).then(function (updated) {
      L.updateUser(updated);
      renderSteam();
    }).catch(function () {});
  }

  document.getElementById('mp-steam-desvincular').addEventListener('click', async function () {
    try {
      await L.del('/usuarios/' + user.id + '/steam');
      const updated = await L.api('/usuarios/' + user.id);
      L.updateUser(updated);
      renderSteam();
      L.toast('Cuenta de Steam desvinculada', 'success');
    } catch (err) { L.toast(err.message, 'error'); }
  });

  function renderInscripciones(list) {
    const box = document.getElementById('mp-inscripciones');
    if (!list.length) {
      box.innerHTML = '<p class="text-tertiary">No tenés inscripciones activas.</p>';
      return;
    }
    box.innerHTML = list.map(function (ins) {
      const chip = ins.estado === 'LISTA_ESPERA'
        ? '<span class="chip chip-pending">En espera</span>'
        : '<span class="chip chip-confirmed">Confirmado</span>';
      return '<div class="race-row" style="grid-template-columns:64px 2fr 1fr auto">' +
        '<div class="race-date"><span class="day">' + L.fmtFecha(ins.carreraFecha).split(' ')[0] + '</span><span class="mon">' + (L.fmtFecha(ins.carreraFecha).split(' ')[1] || '') + '</span></div>' +
        '<div>' +
        '<strong style="font-family:var(--font-display); text-transform:uppercase; font-size:var(--fs-base)"><a href="04-race-detail.html?id=' + ins.carreraId + '" class="link">' + L.esc(ins.carreraNombre || ('Carrera #' + ins.carreraId)) + '</a></strong>' +
        '<div class="text-tertiary" style="font-size:var(--fs-sm)">' + L.esc(ins.categoriaNombre || '') + '</div>' +
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
      const list = await L.api('/inscripciones/usuario/' + user.id).catch(function () { return []; });
      renderInscripciones(list.filter(function (i) { return i.estado !== 'CANCELADA'; }));
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
  renderPasswordPanel();
  renderSteam();
  loadInscripciones();
  loadRecompensas();
})();
