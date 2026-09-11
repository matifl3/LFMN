/* ============================================================
   LFM Nacional — Shell: avatar, logout, notificaciones, nav
   + nav móvil (drawer) + footer enriquecido
   ============================================================ */
(function () {
  'use strict';
  const L = window.LFM;

  function setup() {
    const user = L.getUser();
    const actions = document.querySelector('.header-actions');
    const avatarLink = actions ? actions.querySelector('a[aria-label="Mi perfil"], a.avatar') : null;
    const dot = actions ? actions.querySelector('.dot') : null;

    if (user) {
      // Avatar -> perfil propio
      if (avatarLink) {
        avatarLink.href = '09-my-profile.html';
        avatarLink.innerHTML = L.avatarHtml(user, 38);
      } else if (actions) {
        const a = document.createElement('a');
        a.href = '09-my-profile.html';
        a.setAttribute('aria-label', 'Mi perfil');
        a.innerHTML = L.avatarHtml(user, 38);
        actions.appendChild(a);
      }

      // Nav: link Admin para admins y comisarios
      if (user.rol === 'ADMIN' || user.rol === 'COMISARIO') {
        const nav = document.querySelector('.main-nav');
        if (nav && !nav.querySelector('a[href="13-admin.html"]')) {
          const a = document.createElement('a');
          a.href = '13-admin.html';
          a.textContent = 'Admin';
          nav.appendChild(a);
        }
      }

      // Botón salir
      const salir = document.createElement('a');
      salir.href = '#';
      salir.className = 'btn btn-ghost btn-sm';
      salir.textContent = 'Salir';
      salir.style.fontSize = 'var(--fs-2xs)';
      salir.addEventListener('click', function (e) {
        e.preventDefault();
        L.clearSession();
        L.toast('Sesión cerrada', 'success');
        location.href = '01-home.html';
      });
      if (actions) actions.appendChild(salir);

      // Badge de notificaciones
      if (dot) {
        L.api('/notificaciones/me/no-leidas/contar')
          .then(function (n) {
            dot.style.display = n > 0 ? 'block' : 'none';
            if (n > 0) dot.textContent = n > 9 ? '9+' : n;
          })
          .catch(function () { dot.style.display = 'none'; });
      }
    } else {
      if (dot) dot.style.display = 'none';
      if (avatarLink) {
        const a = document.createElement('a');
        a.href = '02-auth.html';
        a.className = 'btn btn-primary btn-sm';
        a.textContent = 'Ingresar';
        avatarLink.replaceWith(a);
      }
    }

    buildMobileNav();
    buildFooter();
  }

  /* ---------- Nav móvil: toggle + drawer ---------- */

  function buildMobileNav() {
    const actions = document.querySelector('.header-actions');
    const nav = document.querySelector('.main-nav');
    if (!actions || !nav || document.querySelector('.nav-toggle')) return;

    const toggle = document.createElement('button');
    toggle.type = 'button';
    toggle.className = 'nav-toggle';
    toggle.setAttribute('aria-label', 'Abrir menú');
    toggle.setAttribute('aria-expanded', 'false');
    toggle.innerHTML = '<span class="hamb"><span></span></span>';
    actions.insertBefore(toggle, actions.firstChild);

    const overlay = document.createElement('div');
    overlay.className = 'nav-overlay';
    document.body.appendChild(overlay);

    const drawer = document.createElement('aside');
    drawer.className = 'nav-drawer';
    drawer.setAttribute('aria-hidden', 'true');
    document.body.appendChild(drawer);

    function sync() {
      const links = Array.prototype.map.call(document.querySelectorAll('.main-nav a'), function (a) {
        return { href: a.getAttribute('href'), text: a.textContent, active: a.classList.contains('active') };
      });
      const brand = document.querySelector('.brand');
      const brandHtml = brand ? brand.innerHTML : '';
      let inner = '<div class="drawer-brand">' + brandHtml + '</div>';
      inner += links.map(function (l) {
        return '<a href="' + l.href + '"' + (l.active ? ' class="active"' : '') + '>' + L.esc(l.text) + '</a>';
      }).join('');
      const user = L.getUser();
      inner += '<div class="drawer-foot">';
      if (user) {
        inner += '<span class="text-tertiary" style="font-size:var(--fs-xs); padding:0 var(--sp-3)">' + L.esc(user.nombrePiloto || user.email || '') + '</span>';
        inner += '<a href="09-my-profile.html">Mi perfil</a>';
        inner += '<a href="#" id="drawer-salir">Salir</a>';
      } else {
        inner += '<a href="02-auth.html" class="btn btn-primary btn-sm btn-block" style="text-transform:none">Ingresar</a>';
      }
      inner += '</div>';
      drawer.innerHTML = inner;
      const salir = drawer.querySelector('#drawer-salir');
      if (salir) salir.addEventListener('click', function (e) {
        e.preventDefault();
        L.clearSession();
        L.toast('Sesión cerrada', 'success');
        location.href = '01-home.html';
      });
    }

    function setOpen(open) {
      toggle.classList.toggle('open', open);
      overlay.classList.toggle('open', open);
      drawer.classList.toggle('open', open);
      toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
      drawer.setAttribute('aria-hidden', open ? 'false' : 'true');
      document.body.style.overflow = open ? 'hidden' : '';
      if (open) sync();
    }

    toggle.addEventListener('click', function () { setOpen(!drawer.classList.contains('open')); });
    overlay.addEventListener('click', function () { setOpen(false); });
    window.addEventListener('resize', function () { if (window.innerWidth > 980) setOpen(false); });
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape') setOpen(false); });
  }

  /* ---------- Footer enriquecido ---------- */

  function buildFooter() {
    const grid = document.querySelector('.site-footer .footer-grid');
    if (!grid) return;
    const navLinks = Array.prototype.map.call(document.querySelectorAll('.main-nav a'), function (a) {
      return { href: a.getAttribute('href'), text: a.textContent };
    });
    const nav = navLinks.map(function (l) {
      return '<a class="foot-link" href="' + l.href + '">' + L.esc(l.text) + '</a>';
    }).join('');
    grid.innerHTML =
      '<div class="foot-col">' +
        '<div class="brand">' +
          '<span class="brand-mark">L</span>' +
          '<span class="brand-name">LFM Nacional<small>Low Fuel Motorsport</small></span>' +
        '</div>' +
        '<p class="text-tertiary" style="font-size:var(--fs-sm); max-width:30ch">Liga argentina de sim racing. Carreras semanales, categorías por Elo y Safety Rating.</p>' +
      '</div>' +
      '<div class="foot-col">' +
        '<span class="foot-title">Navegación</span>' + nav +
      '</div>' +
      '<div class="foot-col">' +
        '<span class="foot-title">Cuenta</span>' +
        '<a class="foot-link" href="09-my-profile.html">Mi perfil</a>' +
        '<a class="foot-link" href="10-notifications.html">Notificaciones</a>' +
      '</div>' +
      '<div class="foot-bar">' +
        '<span>© 2026 LFM Nacional — Low Fuel Motorsport</span>' +
        '<span>Ronda 8 de 16 · Temporada 2026</span>' +
      '</div>';
  }

  document.addEventListener('DOMContentLoaded', setup);
})();