/* LFM Nacional — 13 · Panel de administración */
(function () {
  'use strict';
  const L = window.LFM;

  const ESTADO_LABEL = {
    PROGRAMADA: 'Programada',
    INSCRIPCIONES_ABIERTAS: 'Inscripciones abiertas',
    INSCRIPCIONES_CERRADAS: 'Inscripciones cerradas',
    EN_CURSO: 'En curso',
    FINALIZADA: 'Finalizada',
    CANCELADA: 'Cancelada'
  };

  let categorias = [];
  let editCarreraId = null;
  let isAdmin = false;
  let isComisario = false;

  const $ = function (id) { return document.getElementById(id); };

  function checkAccess() {
    const user = L.requireAuth();
    if (!user) return false;
    isAdmin = user.rol === 'ADMIN';
    isComisario = user.rol === 'COMISARIO';
    if (!isAdmin && !isComisario) {
      $('adm-denied').style.display = 'block';
      $('adm-panel').style.display = 'none';
      return false;
    }
    return true;
  }

  function setTab(name) {
    document.querySelectorAll('.tabs-pill .tab').forEach(function (t) {
      t.classList.toggle('active', t.getAttribute('data-tab') === name);
    });
    ['carreras', 'campeonatos', 'categorias', 'pilotos', 'anuncios', 'importar'].forEach(function (p) {
      $('pane-' + p).style.display = p === name ? 'block' : 'none';
    });
    const aside = document.querySelector('.panel-aside');
    aside.style.display = (name === 'importar' || name === 'pilotos') ? 'none' : 'block';
    $('form-carrera').style.display = name === 'carreras' ? 'block' : 'none';
    $('form-campeonato').style.display = name === 'campeonatos' ? 'block' : 'none';
    $('form-categoria').style.display = name === 'categorias' ? 'block' : 'none';
    $('form-anuncio').style.display = name === 'anuncios' ? 'block' : 'none';
  }

  /* ---------- Carreras ---------- */

  function estadoSelect(id, estado) {
    const opts = Object.keys(ESTADO_LABEL).map(function (k) {
      return '<option value="' + k + '"' + (k === estado ? ' selected' : '') + '>' + ESTADO_LABEL[k] + '</option>';
    }).join('');
    return '<select class="select" data-race="' + id + '" style="font-size:var(--fs-2xs);padding:4px 6px">' + opts + '</select>';
  }

  function icono(accion) {
    if (accion === 'edit') {
      return '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>';
    }
    return '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2m3 0-1 14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2L4 6"/></svg>';
  }

  function cargarCarreras() {
    const tbody = $('adm-tabla-carreras');
    tbody.innerHTML = '<tr><td colspan="6" class="text-tertiary">Cargando…</td></tr>';
    L.get('/carreras').then(function (carreras) {
      Promise.all(carreras.map(function (c) {
        return L.get('/inscripciones/carrera/' + c.id + '/count')
          .then(function (n) { c._count = n; })
          .catch(function () { c._count = null; });
      })).then(function () {
        tbody.innerHTML = carreras.length ? carreras.map(function (c) {
          const cupo = c.cupoMaximo ? (c._count != null ? c._count + ' / ' + c.cupoMaximo : c.cupoMaximo) : (c._count != null ? String(c._count) : '—');
          return '<tr>' +
            '<td class="data">' + L.esc(c.nombre) + '</td>' +
            '<td><span class="chip chip-category">' + L.esc(c.categoriaNombre || '—') + '</span></td>' +
            '<td class="mono" style="font-size:var(--fs-2xs)">' + L.fmtFechaHora(c.fecha) + '</td>' +
            '<td class="num mono">' + cupo + '</td>' +
            '<td>' + (isAdmin ? estadoSelect(c.id, c.estado) : L.chipCarrera(c.estado)) + '</td>' +
            '<td>' + (isAdmin ?
              '<div class="row-actions">' +
                '<button class="btn btn-ghost btn-sm btn-icon" data-edit-race="' + c.id + '" aria-label="Editar">' + icono('edit') + '</button>' +
                '<button class="btn btn-danger btn-sm btn-icon" data-del-race="' + c.id + '" aria-label="Eliminar">' + icono('del') + '</button>' +
              '</div>' : '') +
            '</td></tr>';
        }).join('') : '<tr><td colspan="6" class="text-tertiary">No hay carreras.</td></tr>';
        tbody.querySelectorAll('[data-edit-race]').forEach(function (b) {
          b.addEventListener('click', function () {
            const c = carreras.find(function (x) { return x.id === Number(b.getAttribute('data-edit-race')); });
            if (c) editarCarrera(c);
          });
        });
        tbody.querySelectorAll('[data-del-race]').forEach(function (b) {
          b.addEventListener('click', function () {
            const id = Number(b.getAttribute('data-del-race'));
            if (!confirm('¿Eliminar esta carrera?')) return;
            L.del('/carreras/' + id).then(function () {
              L.toast('Carrera eliminada', 'success');
              cargarCarreras();
            }).catch(function (e) { L.toast(e.message, 'error'); });
          });
        });
        tbody.querySelectorAll('select[data-race]').forEach(function (sel) {
          sel.addEventListener('change', function () {
            L.put('/carreras/' + sel.getAttribute('data-race') + '/estado?estado=' + sel.value)
              .then(function () { L.toast('Estado actualizado', 'success'); })
              .catch(function (e) { L.toast(e.message, 'error'); });
          });
        });
      });
    }).catch(function (e) {
      tbody.innerHTML = '<tr><td colspan="6" class="text-tertiary">' + L.esc(e.message) + '</td></tr>';
    });
  }

  function cargarCampeonatosSelect(categoriaId, selectedId) {
    const sel = $('a-camp');
    if (!categoriaId) {
      sel.innerHTML = '<option value="">Elegí categoría primero…</option>';
      return;
    }
    L.get('/campeonatos/categoria/' + categoriaId).then(function (cams) {
      sel.innerHTML = cams.length
        ? '<option value="">Elegí campeonato…</option>' + cams.map(function (c) {
            return '<option value="' + c.id + '"' + (selectedId && c.id === selectedId ? ' selected' : '') + '>' + L.esc(c.nombre) + '</option>';
          }).join('')
        : '<option value="">Sin campeonatos</option>';
    }).catch(function () {
      sel.innerHTML = '<option value="">Error al cargar</option>';
    });
  }

  function limpiarFormCarrera() {
    editCarreraId = null;
    $('adm-form-titulo').textContent = 'Nueva carrera';
    $('a-guardar').textContent = 'Crear carrera';
    $('a-id').value = '';
    $('a-nombre').value = '';
    $('a-fecha').value = '';
    $('a-hora').value = '';
    $('a-circuito').value = '';
    $('a-cupo').value = '32';
    $('a-servidor').value = '';
    $('a-camp').innerHTML = '<option value="">Elegí categoría primero…</option>';
  }

  function editarCarrera(c) {
    editCarreraId = c.id;
    $('adm-form-titulo').textContent = 'Editar carrera';
    $('a-guardar').textContent = 'Guardar cambios';
    $('a-id').value = c.id;
    $('a-nombre').value = c.nombre;
    const d = L.parseIso ? L.parseIso(c.fecha) : new Date(c.fecha);
    if (d && !isNaN(d)) {
      $('a-fecha').value = d.toISOString().slice(0, 10);
      $('a-hora').value = d.toISOString().slice(11, 16);
    }
    $('a-circuito').value = c.circuito || '';
    $('a-cupo').value = c.cupoMaximo != null ? c.cupoMaximo : '';
    $('a-servidor').value = c.servidor || '';
    const sel = $('a-cat');
    if (c.categoriaId) sel.value = String(c.categoriaId);
    cargarCampeonatosSelect(c.categoriaId, c.campeonatoId);
    sel.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  function guardarCarrera() {
    const fecha = $('a-fecha').value;
    const hora = $('a-hora').value;
    if (!$('a-nombre').value.trim()) { L.toast('Ingresá el nombre.', 'error'); return; }
    if (!$('a-cat').value) { L.toast('Elegí una categoría.', 'error'); return; }
    if (!$('a-camp').value) { L.toast('Elegí un campeonato.', 'error'); return; }
    if (!fecha) { L.toast('Ingresá la fecha.', 'error'); return; }
    const body = {
      nombre: $('a-nombre').value.trim(),
      fecha: fecha + (hora ? 'T' + hora : 'T20:00') + ':00',
      circuito: $('a-circuito').value.trim() || 'Por definir',
      campeonatoId: Number($('a-camp').value),
      cupoMaximo: Number($('a-cupo').value) || 32,
      servidor: $('a-servidor').value.trim() || null
    };
    const req = editCarreraId ? L.put('/carreras/' + editCarreraId, body) : L.post('/carreras', body);
    req.then(function () {
      L.toast(editCarreraId ? 'Carrera actualizada' : 'Carrera creada', 'success');
      limpiarFormCarrera();
      cargarCarreras();
    }).catch(function (e) { L.toast(e.message, 'error'); });
  }

  /* ---------- Campeonatos ---------- */

  let editCampeonatoId = null;

  function cargarCampeonatos() {
    const tbody = $('adm-tabla-campeonatos');
    tbody.innerHTML = '<tr><td colspan="6" class="text-tertiary">Cargando…</td></tr>';
    L.get('/campeonatos').then(function (cams) {
      tbody.innerHTML = cams.length ? cams.map(function (c) {
        return '<tr>' +
          '<td class="data">' + L.esc(c.nombre) + '</td>' +
          '<td class="mono" style="font-size:var(--fs-xs)">' + L.esc(c.temporada || '—') + '</td>' +
          '<td><span class="chip chip-category">' + L.esc(c.categoriaNombre || '—') + '</span></td>' +
          '<td class="mono" style="font-size:var(--fs-xs)">' + L.esc(c.sistemaPuntos || '—') + '</td>' +
          '<td>' + (c.estado === 'CERRADO'
            ? '<span class="chip chip-resolved">Cerrado</span>'
            : '<span class="chip chip-upcoming">Activo</span>') + '</td>' +
          '<td>' + (isAdmin ?
            '<div class="row-actions">' +
              (c.estado !== 'CERRADO' ? '<button class="btn btn-ghost btn-sm" data-cerrar-cam="' + c.id + '" style="font-size:var(--fs-2xs)">Cerrar</button>' : '') +
              '<button class="btn btn-ghost btn-sm btn-icon" data-edit-cam="' + c.id + '" aria-label="Editar">' + icono('edit') + '</button>' +
              '<button class="btn btn-danger btn-sm btn-icon" data-del-cam="' + c.id + '" aria-label="Eliminar">' + icono('del') + '</button>' +
            '</div>' : '') +
          '</td></tr>';
      }).join('') : '<tr><td colspan="6" class="text-tertiary">No hay campeonatos.</td></tr>';

      tbody.querySelectorAll('[data-edit-cam]').forEach(function (b) {
        b.addEventListener('click', function () {
          const c = cams.find(function (x) { return x.id === Number(b.getAttribute('data-edit-cam')); });
          if (c) editarCampeonato(c);
        });
      });
      tbody.querySelectorAll('[data-cerrar-cam]').forEach(function (b) {
        b.addEventListener('click', function () {
          const id = Number(b.getAttribute('data-cerrar-cam'));
          if (!confirm('¿Cerrar este campeonato? No se podrá editar después.')) return;
          L.put('/campeonatos/' + id + '/cerrar').then(function () {
            L.toast('Campeonato cerrado', 'success');
            cargarCampeonatos();
          }).catch(function (e) { L.toast(e.message, 'error'); });
        });
      });
      tbody.querySelectorAll('[data-del-cam]').forEach(function (b) {
        b.addEventListener('click', function () {
          const id = Number(b.getAttribute('data-del-cam'));
          if (!confirm('¿Eliminar este campeonato?')) return;
          L.del('/campeonatos/' + id).then(function () {
            L.toast('Campeonato eliminado', 'success');
            cargarCampeonatos();
          }).catch(function (e) { L.toast(e.message, 'error'); });
        });
      });
    }).catch(function (e) {
      tbody.innerHTML = '<tr><td colspan="6" class="text-tertiary">' + L.esc(e.message) + '</td></tr>';
    });
  }

  function limpiarFormCampeonato() {
    editCampeonatoId = null;
    $('cam-form-titulo').textContent = 'Nuevo campeonato';
    $('c-guardar').textContent = 'Crear campeonato';
    $('c-id').value = '';
    $('c-nombre').value = '';
    $('c-temporada').value = '';
    $('c-sistema').value = '';
  }

  function editarCampeonato(c) {
    editCampeonatoId = c.id;
    $('cam-form-titulo').textContent = 'Editar campeonato';
    $('c-guardar').textContent = 'Guardar cambios';
    $('c-id').value = c.id;
    $('c-nombre').value = c.nombre;
    $('c-temporada').value = c.temporada || '';
    if (c.sistemaPuntos) $('c-sistema').value = c.sistemaPuntos;
    if (c.categoriaId) $('c-cat').value = String(c.categoriaId);
    setTab('campeonatos');
    $('c-nombre').scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  function guardarCampeonato() {
    if (!$('c-nombre').value.trim()) { L.toast('Ingresá el nombre.', 'error'); return; }
    if (!$('c-cat').value) { L.toast('Elegí una categoría.', 'error'); return; }
    const body = {
      nombre: $('c-nombre').value.trim(),
      temporada: $('c-temporada').value.trim() || null,
      categoriaId: Number($('c-cat').value),
      sistemaPuntos: $('c-sistema').value || null
    };
    const req = editCampeonatoId ? L.put('/campeonatos/' + editCampeonatoId, body) : L.post('/campeonatos', body);
    req.then(function () {
      L.toast(editCampeonatoId ? 'Campeonato actualizado' : 'Campeonato creado', 'success');
      limpiarFormCampeonato();
      cargarCampeonatos();
    }).catch(function (e) { L.toast(e.message, 'error'); });
  }

  /* ---------- Categorías ---------- */

  let editCategoriaId = null;

  function cargarCategorias() {
    const tbody = $('adm-tabla-categorias');
    tbody.innerHTML = '<tr><td colspan="5" class="text-tertiary">Cargando…</td></tr>';
    L.get('/categorias').then(function (cats) {
      categorias = cats;
      tbody.innerHTML = cats.length ? cats.map(function (c) {
        return '<tr>' +
          '<td class="data">' + L.esc(c.nombre) + '</td>' +
          '<td class="text-tertiary" style="font-size:var(--fs-xs)">' + L.esc(c.descripcion || '—') + '</td>' +
          '<td class="num mono" style="font-size:var(--fs-xs)">' + (c.eloMinimo != null ? c.eloMinimo : '—') + ' — ' + (c.eloMaximo != null ? c.eloMaximo : '∞') + '</td>' +
          '<td>' + (c.setupAbierto ? '<span class="chip chip-category">Abierto</span>' : (c.setupFijo ? '<span class="chip chip-closed">Fijo</span>' : '<span class="chip chip-pending">—</span>')) + '</td>' +
          '<td>' + (isAdmin ?
            '<div class="row-actions">' +
              '<button class="btn btn-ghost btn-sm btn-icon" data-edit-cat="' + c.id + '" aria-label="Editar">' + icono('edit') + '</button>' +
              '<button class="btn btn-danger btn-sm btn-icon" data-del-cat="' + c.id + '" aria-label="Eliminar">' + icono('del') + '</button>' +
            '</div>' : '') +
          '</td></tr>';
      }).join('') : '<tr><td colspan="5" class="text-tertiary">No hay categorías.</td></tr>';

      tbody.querySelectorAll('[data-edit-cat]').forEach(function (b) {
        b.addEventListener('click', function () {
          const c = cats.find(function (x) { return x.id === Number(b.getAttribute('data-edit-cat')); });
          if (c) editarCategoria(c);
        });
      });
      tbody.querySelectorAll('[data-del-cat]').forEach(function (b) {
        b.addEventListener('click', function () {
          const id = Number(b.getAttribute('data-del-cat'));
          if (!confirm('¿Eliminar esta categoría?')) return;
          L.del('/categorias/' + id).then(function () {
            L.toast('Categoría eliminada', 'success');
            cargarCategorias();
          }).catch(function (e) { L.toast(e.message, 'error'); });
        });
      });
    }).catch(function (e) {
      tbody.innerHTML = '<tr><td colspan="5" class="text-tertiary">' + L.esc(e.message) + '</td></tr>';
    });
  }

  function limpiarFormCategoria() {
    editCategoriaId = null;
    $('cat-form-titulo').textContent = 'Nueva categoría';
    $('cat-guardar').textContent = 'Crear categoría';
    $('cat-id').value = '';
    $('cat-nombre').value = '';
    $('cat-desc').value = '';
    $('cat-elomin').value = '';
    $('cat-elomax').value = '';
    $('cat-setup').value = 'LIBRE';
  }

  function editarCategoria(c) {
    editCategoriaId = c.id;
    $('cat-form-titulo').textContent = 'Editar categoría';
    $('cat-guardar').textContent = 'Guardar cambios';
    $('cat-id').value = c.id;
    $('cat-nombre').value = c.nombre;
    $('cat-desc').value = c.descripcion || '';
    $('cat-elomin').value = c.eloMinimo != null ? c.eloMinimo : '';
    $('cat-elomax').value = c.eloMaximo != null ? c.eloMaximo : '';
    $('cat-setup').value = c.setupAbierto ? 'ABIERTO' : (c.setupFijo ? 'FIJO' : 'LIBRE');
    setTab('categorias');
    $('cat-nombre').scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  function guardarCategoria() {
    if (!$('cat-nombre').value.trim()) { L.toast('Ingresá el nombre.', 'error'); return; }
    const body = {
      nombre: $('cat-nombre').value.trim(),
      descripcion: $('cat-desc').value.trim() || null,
      eloMinimo: $('cat-elomin').value ? Number($('cat-elomin').value) : null,
      eloMaximo: $('cat-elomax').value ? Number($('cat-elomax').value) : null,
      setupAbierto: $('cat-setup').value === 'ABIERTO',
      setupFijo: $('cat-setup').value === 'FIJO'
    };
    const req = editCategoriaId ? L.put('/categorias/' + editCategoriaId, body) : L.post('/categorias', body);
    req.then(function () {
      L.toast(editCategoriaId ? 'Categoría actualizada' : 'Categoría creada', 'success');
      limpiarFormCategoria();
      cargarCategorias();
      cargarSelectoresCategorias();
    }).catch(function (e) { L.toast(e.message, 'error'); });
  }

  /* ---------- Pilotos ---------- */

  function cargarPilotos() {
    const tbody = $('adm-tabla-pilotos');
    const me = L.getUser();
    tbody.innerHTML = '<tr><td colspan="6" class="text-tertiary">Cargando…</td></tr>';
    L.get('/usuarios').then(function (usuarios) {
      tbody.innerHTML = usuarios.length ? usuarios.map(function (u) {
        const sel = '<select class="select" data-piloto="' + u.id + '" style="font-size:var(--fs-2xs);padding:4px 6px">' +
          ['USUARIO', 'COMISARIO', 'ADMIN'].map(function (r) {
            return '<option value="' + r + '"' + (u.rol === r ? ' selected' : '') + '>' + r + '</option>';
          }).join('') + '</select>';
        const del = u.id === me.id ? '<span></span>' :
          '<button class="btn btn-danger btn-sm btn-icon" data-del-piloto="' + u.id + '" aria-label="Eliminar">' + icono('del') + '</button>';
        const ratingCell = function (campo) {
          if (!isAdmin) {
            return '<td class="num mono">' + (u[campo] != null ? u[campo] : '—') + '</td>';
          }
          return '<td><input class="input" type="number" min="0" data-rating-piloto="' + u.id + '" data-campo="' + campo + '" value="' + (u[campo] != null ? u[campo] : '') + '" style="width:70px;padding:4px 6px;font-size:var(--fs-2xs)"></td>';
        };
        return '<tr>' +
          '<td class="data">' + L.avatarHtml(u, 26) + ' ' + L.esc(u.nombrePiloto || u.email) + '</td>' +
          '<td class="text-tertiary" style="font-size:var(--fs-xs)">' + L.esc(u.email || '—') + '</td>' +
          ratingCell('elo') +
          ratingCell('safetyRating') +
          '<td>' + (isAdmin ? sel : '<span class="chip chip-pending">' + u.rol + '</span>') + '</td>' +
          '<td>' + (isAdmin ? del : '<span></span>') + '</td></tr>';
      }).join('') : '<tr><td colspan="6" class="text-tertiary">No hay pilotos.</td></tr>';

      tbody.querySelectorAll('input[data-rating-piloto]').forEach(function (inp) {
        inp.addEventListener('change', function () {
          const id = inp.getAttribute('data-rating-piloto');
          const body = {};
          ['elo', 'safetyRating'].forEach(function (campo) {
            const el = tbody.querySelector('input[data-rating-piloto="' + id + '"][data-campo="' + campo + '"]');
            if (el) {
              const v = el.value.trim() === '' ? null : Number(el.value);
              if (v !== null && !isNaN(v) && v >= 0) body[campo] = v;
            }
          });
          if (!Object.keys(body).length) {
            L.toast('Ingresá un valor mayor o igual a 0.', 'error');
            cargarPilotos();
            return;
          }
          L.put('/usuarios/' + id + '/rating', body)
            .then(function (u) {
              L.toast('Rating actualizado: ' + (u.nombrePiloto || 'piloto') + ' · Elo ' + u.elo + ' · SR ' + u.safetyRating, 'success');
            })
            .catch(function (e) {
              L.toast(e.message, 'error');
              cargarPilotos();
            });
        });
      });

      tbody.querySelectorAll('select[data-piloto]').forEach(function (sel) {
        sel.addEventListener('change', function () {
          L.put('/usuarios/' + sel.getAttribute('data-piloto') + '/rol?rol=' + sel.value)
            .then(function (u) {
              L.toast('Rol de ' + (u.nombrePiloto || 'piloto') + ' → ' + u.rol, 'success');
            })
            .catch(function (e) {
              L.toast(e.message, 'error');
              cargarPilotos();
            });
        });
      });
      tbody.querySelectorAll('[data-del-piloto]').forEach(function (b) {
        b.addEventListener('click', function () {
          const id = Number(b.getAttribute('data-del-piloto'));
          if (!confirm('¿Eliminar este usuario? Esta acción no se puede deshacer.')) return;
          L.del('/usuarios/' + id).then(function () {
            L.toast('Usuario eliminado', 'success');
            cargarPilotos();
          }).catch(function (e) { L.toast(e.message, 'error'); });
        });
      });
    }).catch(function (e) {
      tbody.innerHTML = '<tr><td colspan="6" class="text-tertiary">' + L.esc(e.message) + '</td></tr>';
    });
  }

  /* ---------- Anuncios ---------- */

  function limpiarFormAnuncio() {
    $('an-form-titulo').textContent = 'Nuevo anuncio';
    $('an-titulo').value = '';
    $('an-contenido').value = '';
    $('an-imagen').value = null;
  }

  function cargarAnuncios() {
    const tbody = $('adm-tabla-anuncios');
    tbody.innerHTML = '<tr><td colspan="5" class="text-tertiary">Cargando…</td></tr>';
    L.get('/anuncios').then(function (anuncios) {
      tbody.innerHTML = anuncios.length ? anuncios.map(function (a) {
        const img = a.urlImagen
          ? '<img src="' + L.esc(a.urlImagen) + '" alt="" style="width:56px;height:36px;object-fit:cover;border-radius:var(--r-sm);border:1px solid var(--line-bright)">'
          : '<span class="text-tertiary">—</span>';
        return '<tr>' +
          '<td class="mono" style="font-size:var(--fs-2xs)">' + L.fmtFechaHora(a.fecha) + '</td>' +
          '<td class="data">' + L.esc(a.titulo) + '</td>' +
          '<td class="text-secondary" style="font-size:var(--fs-xs); max-width:340px">' + L.esc(a.contenido) + '</td>' +
          '<td>' + img + '</td>' +
          '<td>' + (isAdmin ?
            '<div class="row-actions">' +
              '<button class="btn btn-danger btn-sm btn-icon" data-del-anuncio="' + a.id + '" aria-label="Eliminar">' + icono('del') + '</button>' +
            '</div>' : '') +
          '</td></tr>';
      }).join('') : '<tr><td colspan="5" class="text-tertiary">No hay anuncios publicados.</td></tr>';

      tbody.querySelectorAll('[data-del-anuncio]').forEach(function (b) {
        b.addEventListener('click', function () {
          const id = Number(b.getAttribute('data-del-anuncio'));
          if (!confirm('¿Eliminar este anuncio?')) return;
          L.del('/anuncios/' + id).then(function () {
            L.toast('Anuncio eliminado', 'success');
            cargarAnuncios();
          }).catch(function (e) { L.toast(e.message, 'error'); });
        });
      });
    }).catch(function (e) {
      tbody.innerHTML = '<tr><td colspan="5" class="text-tertiary">' + L.esc(e.message) + '</td></tr>';
    });
  }

  function guardarAnuncio() {
    if (!$('an-titulo').value.trim()) { L.toast('Ingresá el título.', 'error'); return; }
    if (!$('an-contenido').value.trim()) { L.toast('Ingresá el contenido.', 'error'); return; }
    const archivo = $('an-imagen').files[0];
    const crearAnuncio = function (urlImagen) {
      const body = {
        titulo: $('an-titulo').value.trim(),
        contenido: $('an-contenido').value.trim(),
        urlImagen: urlImagen || null
      };
      L.post('/anuncios', body).then(function () {
        L.toast('Anuncio publicado', 'success');
        limpiarFormAnuncio();
        cargarAnuncios();
      }).catch(function (e) { L.toast(e.message, 'error'); });
    };
    if (archivo) {
      var fd = new FormData();
      fd.append('archivo', archivo);
      L.post('/imagenes', fd).then(function (res) {
        crearAnuncio(res.url);
      }).catch(function (e) { L.toast('Error al subir imagen: ' + e.message, 'error'); });
    } else {
      crearAnuncio(null);
    }
  }

  /* ---------- Importar sesión ---------- */

  function cargarCarrerasImport() {
    const sel = $('adm-import-carrera');
    L.get('/carreras').then(function (carreras) {
      sel.innerHTML = carreras.length
        ? '<option value="">Seleccioná una carrera…</option>' + carreras.map(function (c) {
            return '<option value="' + c.id + '">' + L.esc(c.nombre + ' — ' + (c.categoriaNombre || '')) + '</option>';
          }).join('')
        : '<option value="">No hay carreras</option>';
    }).catch(function () {
      sel.innerHTML = '<option value="">No hay carreras</option>';
    });
  }

  $('adm-import-validar').addEventListener('click', function () {
    const txt = $('adm-import-json').value.trim();
    if (!txt) { L.toast('Pegá un JSON primero.', 'error'); return; }
    try {
      const obj = JSON.parse(txt);
      const keys = Object.keys(obj).join(', ');
      L.toast('JSON válido (' + keys + ')', 'success');
    } catch (e) {
      L.toast('JSON inválido: ' + e.message, 'error');
    }
  });

  $('adm-import-enviar').addEventListener('click', function () {
    const carreraId = $('adm-import-carrera').value;
    const txt = $('adm-import-json').value.trim();
    if (!carreraId) { L.toast('Seleccioná una carrera.', 'error'); return; }
    if (!txt) { L.toast('Pegá el JSON de la sesión.', 'error'); return; }
    let body;
    try { body = JSON.parse(txt); } catch (e) { L.toast('JSON inválido: ' + e.message, 'error'); return; }
    L.post('/sesiones/importar?carreraId=' + carreraId, body).then(function (res) {
      L.toast('Sesión importada: ' + (res.tipo || '') + ' (' + res.estado + ')', 'success');
      $('adm-import-json').value = '';
    }).catch(function (e) { L.toast(e.message, 'error'); });
  });

  function cargarSelectoresCategorias() {
    L.get('/categorias').then(function (cats) {
      categorias = cats;
      ['a-cat', 'c-cat'].forEach(function (selId) {
        const sel = $(selId);
        sel.innerHTML = cats.length
          ? '<option value="">Elegí categoría…</option>' + cats.map(function (c) {
              return '<option value="' + c.id + '">' + L.esc(c.nombre) + '</option>';
            }).join('')
          : '<option value="">Sin categorías</option>';
      });
    });
  }

  /* ---------- Wire up ---------- */

  document.querySelectorAll('.tabs-pill .tab').forEach(function (t) {
    t.addEventListener('click', function () { setTab(t.getAttribute('data-tab')); });
  });

  $('a-guardar').addEventListener('click', guardarCarrera);
  $('a-cancelar').addEventListener('click', limpiarFormCarrera);
  $('a-cat').addEventListener('change', function () {
    cargarCampeonatosSelect($('a-cat').value);
  });
  $('c-guardar').addEventListener('click', guardarCampeonato);
  $('c-cancelar').addEventListener('click', limpiarFormCampeonato);
  $('cat-guardar').addEventListener('click', guardarCategoria);
  $('cat-cancelar').addEventListener('click', limpiarFormCategoria);
  $('an-guardar').addEventListener('click', guardarAnuncio);
  $('an-cancelar').addEventListener('click', limpiarFormAnuncio);

  if (!checkAccess()) return;

  cargarSelectoresCategorias();
  cargarCarreras();
  cargarCampeonatos();
  cargarCategorias();
  cargarPilotos();
  cargarAnuncios();
  cargarCarrerasImport();
})();
