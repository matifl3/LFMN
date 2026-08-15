/* LFM Nacional — 02 · Login / Registro */
(function () {
  'use strict';
  const L = window.LFM;

  const formLogin = document.getElementById('form-login');
  const formReg = document.getElementById('form-register');
  const tabLogin = document.getElementById('tab-login');
  const tabRegister = document.getElementById('tab-register');

  function next() {
    const p = new URLSearchParams(location.search).get('next');
    return p ? decodeURIComponent(p) : '01-home.html';
  }

  function setTab(register) {
    tabLogin.classList.toggle('active', !register);
    tabRegister.classList.toggle('active', register);
    formLogin.style.display = register ? 'none' : 'block';
    formReg.style.display = register ? 'block' : 'none';
  }

  if (location.hash === '#register') setTab(true);

  tabLogin.addEventListener('click', function () { setTab(false); });
  tabRegister.addEventListener('click', function () { setTab(true); });

  formLogin.addEventListener('submit', async function (e) {
    e.preventDefault();
    const btn = document.getElementById('btn-login');
    btn.disabled = true;
    try {
      const data = await L.post('/usuarios/login', {
        email: document.getElementById('login-email').value.trim(),
        password: document.getElementById('login-pass').value
      });
      L.setSession(data.token, data.usuario);
      L.toast('¡Bienvenido, ' + (data.usuario.nombrePiloto || 'piloto') + '!', 'success');
      location.href = next();
    } catch (err) {
      L.toast(err.message, 'error');
      btn.disabled = false;
    }
  });

  formReg.addEventListener('submit', async function (e) {
    e.preventDefault();
    const btn = document.getElementById('btn-register');
    btn.disabled = true;
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-pass').value;
    try {
      await L.post('/usuarios/registro', {
        email: email,
        password: password,
        nombrePiloto: document.getElementById('reg-nombre').value.trim()
      });
      const data = await L.post('/usuarios/login', { email: email, password: password });
      L.setSession(data.token, data.usuario);
      L.toast('Cuenta creada correctamente', 'success');
      location.href = '01-home.html';
    } catch (err) {
      L.toast(err.message, 'error');
      btn.disabled = false;
    }
  });

  document.getElementById('btn-steam').addEventListener('click', async function () {
    const btn = this;
    btn.disabled = true;
    try {
      const data = await L.api('/steam/auth-url', { auth: false });
      if (data && data.url) {
        window.location.href = data.url;
      } else {
        L.toast('No se pudo iniciar el ingreso con Steam.', 'error');
        btn.disabled = false;
      }
    } catch (err) {
      L.toast(err.message, 'error');
      btn.disabled = false;
    }
  });

  /* Retorno del flujo Steam: ?steam=ok&token=<jwt> o ?steam=invalido|expirado */
  (function procesoSteam() {
    const params = new URLSearchParams(location.search);
    const steam = params.get('steam');
    if (!steam) return;
    const token = params.get('token');
    history.replaceState(null, '', location.pathname + location.hash);
    if (steam === 'ok' && token) {
      L.setSession(token, null);
      L.api('/usuarios/me').then(function (usuario) {
        L.updateUser(usuario);
        L.toast('¡Bienvenido, ' + (usuario.nombrePiloto || 'piloto') + '!', 'success');
        location.href = next();
      }).catch(function () {
        L.toast('No se pudo completar el ingreso con Steam.', 'error');
      });
    } else {
      L.toast(steam === 'expirado'
        ? 'El enlace de ingreso con Steam expiró. Intentá de nuevo.'
        : 'No se pudo ingresar con Steam.', 'error');
    }
  })();
})();
