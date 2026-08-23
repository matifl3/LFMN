/* ============================================================
   LFM Nacional — Shell: avatar, logout, notificaciones, nav
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
  }

  document.addEventListener('DOMContentLoaded', setup);
})();
