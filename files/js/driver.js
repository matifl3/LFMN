/* LFM Nacional — 08 · Perfil de piloto */
(function () {
  'use strict';
  const L = window.LFM;

  const params = new URLSearchParams(location.search);
  const id = params.get('id');
  if (!id) { location.href = '03-races-list.html'; return; }

  function chartHtml(values, color) {
    if (!values || values.length < 2) {
      return '<p class="text-tertiary" style="font-size:var(--fs-sm)">No hay datos suficientes para graficar.</p>';
    }
    const W = 720, H = 200, PAD = 24;
    const min = Math.min.apply(null, values);
    const max = Math.max.apply(null, values);
    const rango = (max - min) || 1;
    const step = (W - PAD * 2) / (values.length - 1);
    const pts = values.map(function (v, i) {
      const x = PAD + i * step;
      const y = PAD + (H - PAD * 2) * (1 - (v - min) / rango);
      return [Math.round(x * 100) / 100, Math.round(y * 100) / 100];
    });
    const line = pts.map(function (p) { return p[0] + ',' + p[1]; }).join(' ');
    const area = line + ' ' + W + ',' + H + ' 0,' + H;
    return '<svg viewBox="0 0 ' + W + ' ' + H + '" width="100%" height="200" role="img">' +
      '<line x1="0" y1="' + PAD + '" x2="' + W + '" y2="' + PAD + '" stroke="var(--line-hair)" stroke-width="1"/>' +
      '<line x1="0" y1="' + (H / 2) + '" x2="' + W + '" y2="' + (H / 2) + '" stroke="var(--line-hair)" stroke-width="1"/>' +
      '<line x1="0" y1="' + (H - PAD) + '" x2="' + W + '" y2="' + (H - PAD) + '" stroke="var(--line-hair)" stroke-width="1"/>' +
      '<polyline fill="' + color + '" stroke="none" points="' + area + '"/>' +
      '<polyline fill="none" stroke="' + color + '" stroke-width="2.5" stroke-linejoin="round" stroke-linecap="round" points="' + line + '"/>' +
      '<circle cx="' + pts[pts.length - 1][0] + '" cy="' + pts[pts.length - 1][1] + '" r="5" fill="' + color + '" stroke="var(--bg-panel)" stroke-width="2"/>' +
      '</svg>';
  }

  function cumulative(cambios, actual) {
    if (!cambios || !cambios.length) return [];
    const total = cambios.reduce(function (s, c) { return s + (c.cambio || 0); }, 0);
    let val = actual - total;
    return cambios.map(function (c) {
      val += (c.cambio || 0);
      return val;
    });
  }

  Promise.all([
    L.api('/usuarios/' + id),
    L.api('/usuarios/' + id + '/stats').catch(function () { return null; }),
    L.api('/usuarios/' + id + '/historial-elo').catch(function () { return []; }),
    L.api('/usuarios/' + id + '/historial-safety-rating').catch(function () { return []; }),
    L.api('/resultados/usuario/' + id).catch(function () { return []; }),
    L.api('/usuarios/' + id + '/logros/obtenidos').catch(function () { return []; }),
    L.api('/carreras').catch(function () { return []; }),
    L.api('/categorias').catch(function () { return []; })
  ]).then(function (res) {
    const user = res[0];
    const stats = res[1];
    const histElo = res[2];
    const histSr = res[3];
    const resultados = res[4];
    const logros = res[5];
    const carreras = res[6];
    const categorias = res[7];

    document.title = user.nombrePiloto + ' — Perfil de piloto — LFM Nacional';
    document.getElementById('dp-avatar').innerHTML = L.avatarHtml(user, 108);
    document.getElementById('dp-nombre').textContent = user.nombrePiloto;

    const cat = categorias.find(function (c) {
      if (user.elo === null || user.elo === undefined) return false;
      if (c.eloMinimo != null && user.elo < c.eloMinimo) return false;
      if (c.eloMaximo != null && user.elo > c.eloMaximo) return false;
      return true;
    });
    document.getElementById('dp-eyebrow').textContent = (cat ? cat.nombre.toUpperCase() : 'PILOTO') + ' · LFM NACIONAL';
    document.getElementById('dp-tagline').textContent = (user.email || '') + (user.guidSteam ? ' · Steam: ' + user.guidSteam : '') + (user.fechaRegistro ? ' · miembro desde ' + L.fmtFecha(user.fechaRegistro) : '');
    document.getElementById('dp-elo').textContent = user.elo ?? '—';
    document.getElementById('dp-sr').textContent = user.safetyRating ?? '—';

    if (stats) {
      document.getElementById('dp-st-carreras').textContent = stats.carrerasDisputadas;
      document.getElementById('dp-st-victorias').textContent = stats.victorias;
      document.getElementById('dp-st-podios').textContent = stats.podios;
      document.getElementById('dp-st-poles').textContent = stats.poles;
      document.getElementById('dp-st-vr').textContent = stats.vueltasRapidas;
      document.getElementById('dp-st-fin').textContent = (Math.round(stats.porcentajeFinalizacion * 100) / 100) + '%';
    }

    document.getElementById('dp-elo-actual').textContent = user.elo ?? '—';
    document.getElementById('dp-sr-actual').textContent = user.safetyRating ?? '—';
    document.getElementById('dp-chart-elo').innerHTML = chartHtml(cumulative(histElo, user.elo || 1000), 'var(--amber)');
    document.getElementById('dp-chart-sr').innerHTML = chartHtml(cumulative(histSr, user.safetyRating || 50), 'var(--celeste)');

    const carreraMap = {};
    carreras.forEach(function (c) { carreraMap[c.id] = c; });

    if (resultados.length) {
      const medal = ['gold', 'silver', 'bronze'];
      const order = resultados.slice().sort(function (a, b) { return (b.id || 0) - (a.id || 0); });
      document.getElementById('dp-tabla-resultados').innerHTML = order.map(function (r) {
        const cr = carreraMap[r.carreraId] || {};
        const pos = r.finalizo === false ? '<span class="chip chip-dnf" style="padding:.2em .6em">DNF</span>'
          : '<span class="rank-badge ' + (medal[(r.posicionFinal || 9) - 1] || '') + '" style="width:22px;height:22px">' + (r.posicionFinal ?? '—') + '</span>';
        const elo = r.eloGanado == null ? '—' : '<span class="mono" style="color:' + (r.eloGanado >= 0 ? 'var(--success)' : 'var(--danger)') + '">' + (r.eloGanado >= 0 ? '+' : '') + r.eloGanado + '</span>';
        const sr = r.srGanado == null ? '—' : '<span class="mono" style="color:' + (r.srGanado >= 0 ? 'var(--success)' : 'var(--danger)') + '">' + (r.srGanado >= 0 ? '+' : '') + r.srGanado + '</span>';
        return '<tr class="' + ((r.posicionFinal === 1 && r.finalizo !== false) ? 'podium-1' : (r.posicionFinal === 2 ? 'podium-2' : (r.posicionFinal === 3 ? 'podium-3' : ''))) + '">' +
          '<td class="data"><a href="04-race-detail.html?id=' + r.carreraId + '" class="link">' + L.esc(cr.nombre || ('Carrera #' + r.carreraId)) + '</a></td>' +
          '<td>' + L.esc(cr.categoriaNombre || '—') + '</td>' +
          '<td class="num">' + pos + '</td>' +
          '<td class="num">' + elo + '</td>' +
          '<td class="num">' + sr + '</td>' +
          '</tr>';
      }).join('');
    }

    if (logros.length) {
      document.getElementById('dp-logros').innerHTML = logros.map(function (lg) {
        return '<div class="card achievement-card">' +
          '<div class="achievement-icon" style="background:var(--celeste-soft, rgba(0,180,255,.15))">' + L.esc(lg.icono || '🏆') + '</div>' +
          '<h4 style="font-size:var(--fs-sm)">' + L.esc(lg.nombre) + '</h4>' +
          '<p class="text-secondary" style="font-size:var(--fs-2xs); margin-top:var(--sp-1)">' + L.esc(lg.descripcion || '') + '</p>' +
          '<span class="text-tertiary mono" style="font-size:var(--fs-2xs); margin-top:var(--sp-2)">' + (lg.fechaObtencion ? L.fmtFecha(lg.fechaObtencion) : '') + '</span>' +
          '</div>';
      }).join('');
    }

    document.querySelectorAll('.tabs .tab').forEach(function (tab) {
      tab.addEventListener('click', function () {
        document.querySelectorAll('.tabs .tab').forEach(function (t) { t.classList.remove('active'); });
        tab.classList.add('active');
        ['elo', 'sr', 'resultados', 'logros'].forEach(function (p) {
          document.getElementById('dp-panel-' + p).style.display = (p === tab.dataset.panel) ? 'block' : 'none';
        });
      });
    });

  }).catch(function (err) {
    L.toast(err.message, 'error');
  });
})();
