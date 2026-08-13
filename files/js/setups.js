/* LFM Nacional — 07 · Setups */
(function () {
  'use strict';
  const L = window.LFM;

  let setups = [];
  let usuariosMap = {};
  let categoriasMap = {};
  let selectedId = null;

  const listBox = document.getElementById('setup-list');
  const detailBox = document.getElementById('setup-detail');

  function autor(s) {
    return usuariosMap[s.autorId] || { id: s.autorId, nombrePiloto: 'Piloto #' + s.autorId };
  }

  function stars(prom) {
    if (prom === null || prom === undefined) return '<span class="text-tertiary" style="font-size:var(--fs-2xs)">Sin calificar</span>';
    const n = Math.round(prom);
    return '<span class="stars">' + '★'.repeat(n) + '<span class="star-empty">' + '★'.repeat(5 - n) + '</span></span>';
  }

  function filtered() {
    const q = (document.getElementById('sf-search').value || '').toLowerCase().trim();
    const c = document.getElementById('sf-f-circuito').value;
    const v = document.getElementById('sf-f-vehiculo').value;
    const order = document.getElementById('sf-f-order').value;
    let list = setups.filter(function (s) {
      if (c && s.circuito !== c) return false;
      if (v && s.vehiculo !== v) return false;
      if (q && !((s.titulo || '').toLowerCase().includes(q) || (s.vehiculo || '').toLowerCase().includes(q) || (s.circuito || '').toLowerCase().includes(q) || (s.descripcion || '').toLowerCase().includes(q))) return false;
      return true;
    });
    list = list.slice().sort(function (a, b) {
      if (order === 'recientes') return (new Date(b.fechaPublicacion) - new Date(a.fechaPublicacion));
      return (b.promedioCalificacion || 0) - (a.promedioCalificacion || 0);
    });
    return list;
  }

  function renderList() {
    const list = filtered();
    if (!list.length) {
      listBox.innerHTML = '<p class="text-tertiary">No hay setups que coincidan con la búsqueda.</p>';
      return;
    }
    listBox.innerHTML = list.map(function (s) {
      const a = autor(s);
      return '<div class="card setup-card ' + (s.id === selectedId ? 'bracket' : 'card-hover') + '" style="cursor:pointer" data-setup="' + s.id + '">' +
        '<div class="setup-card-cover"><span class="chip chip-category">' + L.esc(s.circuito || '') + '</span></div>' +
        '<h4>' + L.esc(s.titulo) + '</h4>' +
        '<div class="flex-between">' +
        '<span class="author-row">' + L.avatarHtml(a, 24) + L.esc(a.nombrePiloto) + '</span>' +
        stars(s.promedioCalificacion) +
        '</div>' +
        '<div class="text-tertiary mono" style="font-size:var(--fs-2xs)">' + (s.promedioCalificacion != null ? s.promedioCalificacion.toFixed(1) + ' · ' : '') + 'publicado ' + L.fmtRel(s.fechaPublicacion) + '</div>' +
        '</div>';
    }).join('');

    listBox.querySelectorAll('[data-setup]').forEach(function (el) {
      el.addEventListener('click', function () {
        selectedId = Number(el.dataset.setup);
        renderList();
        loadDetail(selectedId);
      });
    });
  }

  function loadDetail(id) {
    const s = setups.find(function (x) { return x.id === id; });
    if (!s) return;
    const a = autor(s);

    detailBox.innerHTML =
      '<div class="card-header">' +
      '<div>' +
      '<span class="eyebrow amber">' + L.esc(s.vehiculo || '') + '</span>' +
      '<h3 style="margin-top:var(--sp-2)">' + L.esc(s.titulo) + '</h3>' +
      '<span class="author-row" style="margin-top:var(--sp-2)">' + L.avatarHtml(a, 24) + 'Subido por ' + L.esc(a.nombrePiloto) + ' · ' + L.fmtRel(s.fechaPublicacion) + '</span>' +
      '</div>' +
      (s.archivo
        ? '<a href="' + L.esc(s.archivo) + '" target="_blank" rel="noopener" class="btn btn-primary">Descargar .json</a>'
        : '<span class="chip chip-closed">Sin archivo</span>') +
      '</div>' +
      '<p class="text-secondary" style="font-size:var(--fs-sm); margin:var(--sp-4) 0 var(--sp-5)">' + L.esc(s.descripcion || 'Sin descripción.') + '</p>' +
      '<div class="grid grid-3" style="margin-bottom:var(--sp-6)">' +
      '<div class="stat-card"><div class="stat-label">Circuito</div><div class="stat-value" style="font-size:var(--fs-md)">' + L.esc(s.circuito || '—') + '</div></div>' +
      '<div class="stat-card"><div class="stat-label">Calificación</div><div class="stat-value" style="font-size:var(--fs-md)">' + (s.promedioCalificacion != null ? s.promedioCalificacion.toFixed(1) : '—') + '</div></div>' +
      '<div class="stat-card"><div class="stat-label">Categoría</div><div class="stat-value" style="font-size:var(--fs-md)">' + L.esc((categoriasMap[s.categoriaId] || {}).nombre || '—') + '</div></div>' +
      '</div>';

    const ratingBox = document.createElement('div');
    ratingBox.innerHTML =
      '<div class="field">' +
      '<label>Tu calificación</label>' +
      '<div class="slider-row">' +
      '<span class="stars" id="setup-my-stars" style="font-size:var(--fs-lg)">★★★</span>' +
      '<input type="range" min="1" max="5" step="1" value="4" aria-label="Calificación en estrellas" id="setup-rate">' +
      '</div></div>';
    detailBox.appendChild(ratingBox);

    const user = L.getUser();
    const rateInput = ratingBox.querySelector('#setup-rate');
    const starsEl = ratingBox.querySelector('#setup-my-stars');
    rateInput.addEventListener('input', function () {
      const v = Number(rateInput.value);
      starsEl.innerHTML = '★'.repeat(v) + '<span class="star-empty">' + '★'.repeat(5 - v) + '</span>';
    });

    if (user) {
      rateInput.addEventListener('change', async function () {
        try {
          await L.post('/setups/' + id + '/calificaciones', { usuarioId: user.id, puntaje: Number(rateInput.value) });
          L.toast('Calificación guardada', 'success');
          reloadRating(id);
        } catch (err) { L.toast(err.message, 'error'); }
      });
    } else {
      rateInput.disabled = true;
      rateInput.title = 'Ingresá para calificar';
    }

    const commentsBox = document.createElement('div');
    commentsBox.id = 'setup-comments';
    detailBox.appendChild(commentsBox);

    L.api('/setups/' + id + '/comentarios').then(function (coments) {
      const inner = document.createElement('div');
      inner.innerHTML = '<h4 style="margin:var(--sp-5) 0 var(--sp-3)">Comentarios <span class="text-tertiary mono" style="font-size:var(--fs-sm)">(' + coments.length + ')</span></h4>' +
        (coments.length ? coments.map(function (c) {
          const cu = usuariosMap[c.usuarioId] || { nombrePiloto: 'Piloto #' + c.usuarioId };
          return '<div class="comment">' +
            L.avatarHtml(cu, 34) +
            '<div style="flex:1">' +
            '<div class="flex-between"><strong style="font-size:var(--fs-sm)">' + L.esc(cu.nombrePiloto) + '</strong><span class="text-tertiary mono" style="font-size:var(--fs-2xs)">' + L.fmtRel(c.fecha) + '</span></div>' +
            '<p class="text-secondary" style="font-size:var(--fs-sm); margin-top:var(--sp-1)">' + L.esc(c.texto) + '</p>' +
            '</div></div>';
        }).join('') : '<p class="text-tertiary" style="font-size:var(--fs-sm)">Todavía no hay comentarios.</p>');

      if (user) {
        inner.insertAdjacentHTML('beforeend',
          '<div class="field" style="margin-top:var(--sp-5); margin-bottom:0">' +
          '<label>Agregar comentario</label>' +
          '<textarea class="textarea" id="setup-comentario" placeholder="Contá cómo te fue con este setup…"></textarea>' +
          '<button type="button" class="btn btn-secondary" style="align-self:flex-end" id="setup-pub-comentario">Publicar comentario</button>' +
          '</div>');
      }
      commentsBox.innerHTML = inner.innerHTML;

      const pubBtn = document.getElementById('setup-pub-comentario');
      if (pubBtn) {
        pubBtn.addEventListener('click', async function () {
          const texto = document.getElementById('setup-comentario').value.trim();
          if (!texto) { L.toast('Escribí un comentario primero.', 'error'); return; }
          try {
            await L.post('/setups/' + id + '/comentarios', { usuarioId: user.id, texto: texto });
            L.toast('Comentario publicado', 'success');
            loadDetail(id);
          } catch (err) { L.toast(err.message, 'error'); }
        });
      }
    }).catch(function () {});

    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function reloadRating(id) {
    L.api('/setups/' + id).then(function (s) {
      const idx = setups.findIndex(function (x) { return x.id === id; });
      if (idx >= 0) setups[idx] = s;
      renderList();
    }).catch(function () {});
  }

  L.api('/categorias').then(function (cats) {
    cats.forEach(function (c) { categoriasMap[c.id] = c; });
    const sel = document.getElementById('sf-categoria');
    sel.innerHTML = '<option value="">—</option>' + cats.map(function (c) {
      return '<option value="' + c.id + '">' + L.esc(c.nombre) + '</option>';
    }).join('');
  }).catch(function () {});

  Promise.all([
    L.api('/setups').catch(function () { return []; }),
    L.api('/usuarios').catch(function () { return []; })
  ]).then(function (res) {
    setups = res[0];
    res[1].forEach(function (u) { usuariosMap[u.id] = u; });

    const circ = new Set();
    const veh = new Set();
    setups.forEach(function (s) {
      if (s.circuito) circ.add(s.circuito);
      if (s.vehiculo) veh.add(s.vehiculo);
    });
    document.getElementById('sf-f-circuito').innerHTML = '<option value="">Todos los circuitos</option>' + Array.from(circ).sort().map(function (c) { return '<option>' + L.esc(c) + '</option>'; }).join('');
    document.getElementById('sf-f-vehiculo').innerHTML = '<option value="">Todos los vehículos</option>' + Array.from(veh).sort().map(function (v) { return '<option>' + L.esc(v) + '</option>'; }).join('');

    if (setups.length) {
      selectedId = setups[0].id;
      loadDetail(selectedId);
    } else {
      listBox.innerHTML = '<p class="text-tertiary">Aún no hay setups publicados.</p>';
    }
    renderList();
  });

  ['sf-search', 'sf-f-circuito', 'sf-f-vehiculo', 'sf-f-order'].forEach(function (id) {
    document.getElementById(id).addEventListener('input', renderList);
    document.getElementById(id).addEventListener('change', renderList);
  });

  const btnSubir = document.getElementById('setup-btn-subir');
  btnSubir.addEventListener('click', function () {
    const form = document.getElementById('setup-form-card');
    form.style.display = form.style.display === 'none' ? 'block' : 'none';
  });

  document.getElementById('sf-publicar').addEventListener('click', async function () {
    const user = L.requireAuth();
    if (!user) return;
    const body = {
      titulo: document.getElementById('sf-titulo').value.trim(),
      descripcion: document.getElementById('sf-descripcion').value.trim(),
      circuito: document.getElementById('sf-circuito').value.trim(),
      vehiculo: document.getElementById('sf-vehiculo').value.trim(),
      archivo: document.getElementById('sf-archivo').value.trim() || null,
      autorId: user.id,
      categoriaId: document.getElementById('sf-categoria').value ? Number(document.getElementById('sf-categoria').value) : null
    };
    if (!body.titulo || !body.circuito || !body.vehiculo) {
      L.toast('Completá título, circuito y vehículo.', 'error');
      return;
    }
    try {
      const nuevo = await L.post('/setups', body);
      setups.unshift(nuevo);
      selectedId = nuevo.id;
      document.getElementById('setup-form-card').style.display = 'none';
      renderList();
      loadDetail(nuevo.id);
      L.toast('Setup publicado', 'success');
    } catch (err) { L.toast(err.message, 'error'); }
  });
})();
