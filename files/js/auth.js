/* LFM Nacional — 02 · Login / Registro */
(function () {
  'use strict';
  const L = window.LFM;

  const formLogin = document.getElementById('form-login');
  const panelReg = document.getElementById('panel-register');
  const tabLogin = document.getElementById('tab-login');
  const tabRegister = document.getElementById('tab-register');

  if (sessionStorage.getItem('lfm_msg_pass_changed')) {
    sessionStorage.removeItem('lfm_msg_pass_changed');
    L.toast('Contraseña actualizada. Ingresá con tu nueva contraseña.', 'success');
  }

  function next() {
    const p = new URLSearchParams(location.search).get('next');
    if (!p) return '01-home.html';
    const decoded = decodeURIComponent(p);
    if (decoded.startsWith('//') || decoded.startsWith('http://') || decoded.startsWith('https://') || decoded.startsWith('javascript:')) return '01-home.html';
    if (!decoded.startsWith('/')) return '01-home.html';
    return decoded;
  }

  function setTab(register) {
    tabLogin.classList.toggle('active', !register);
    tabRegister.classList.toggle('active', register);
    formLogin.style.display = register ? 'none' : 'block';
    panelReg.style.display = register ? 'block' : 'none';
  }

  function iniciarConSteam() {
    return L.api('/steam/auth-url', { auth: false }).then(function (data) {
      if (data && data.url) {
        window.location.href = data.url;
      } else {
        L.toast('No se pudo iniciar el ingreso con Steam.', 'error');
      }
    }).catch(function (err) {
      L.toast(err.message, 'error');
    });
  }

  if (location.hash === '#register') setTab(true);

  tabLogin.addEventListener('click', function () { setTab(false); });
  tabRegister.addEventListener('click', function () { setTab(true); });

  formLogin.addEventListener('submit', async function (e) {
    e.preventDefault();
    const btn = document.getElementById('btn-login');
    const passInput = document.getElementById('login-pass');
    L.clearFieldErrors();
    L.setFieldInvalid(passInput, false);
    L.busy(btn, true);
    try {
      const data = await L.post('/usuarios/login', {
        email: document.getElementById('login-email').value.trim(),
        password: passInput.value
      });
      L.setSession(data.token, data.usuario);
      L.toast('¡Bienvenido, ' + (data.usuario.nombrePiloto || 'piloto') + '!', 'success');
      location.href = next();
    } catch (err) {
      L.setFieldInvalid(passInput, true, 'Email o contraseña incorrectos.');
      L.busy(btn, false);
      L.toast(err.message, 'error');
    }
  });

  document.getElementById('btn-steam').addEventListener('click', function () {
    const btn = this;
    btn.disabled = true;
    iniciarConSteam().finally(function () { btn.disabled = false; });
  });

  document.getElementById('btn-register-steam').addEventListener('click', function () {
    const btn = this;
    btn.disabled = true;
    iniciarConSteam().finally(function () { btn.disabled = false; });
  });

  /* Retorno del flujo Steam: ?steam=ok&codigo=<un-solo-uso> o ?steam=nuevo&guid=<guid> o ?steam=invalido|expirado */
  (function procesoSteam() {
    const params = new URLSearchParams(location.search);
    const steam = params.get('steam');
    if (!steam) return;
    const codigo = params.get('codigo');
    const guid = params.get('guid');
    history.replaceState(null, '', location.pathname + location.hash);
    if (steam === 'ok' && codigo) {
      L.post('/steam/completar', { codigo: codigo }).then(function (data) {
        L.setSession(data.token, null);
        return L.api('/usuarios/me');
      }).then(function (usuario) {
        L.updateUser(usuario);
        L.toast('¡Bienvenido, ' + (usuario.nombrePiloto || 'piloto') + '!', 'success');
        location.href = next();
      }).catch(function (err) {
        L.toast((err && err.message) || 'No se pudo completar el ingreso con Steam.', 'error');
      });
    } else if (steam === 'nuevo' && guid) {
      document.getElementById('form-login').style.display = 'none';
      panelReg.style.display = 'none';
      document.getElementById('tab-login').style.display = 'none';
      document.getElementById('tab-register').style.display = 'none';
      document.querySelector('.divider-or').style.display = 'none';
      document.getElementById('btn-steam').style.display = 'none';
      document.getElementById('steam-guid').value = guid;
      document.getElementById('form-steam-setup').style.display = 'block';
    } else {
      L.toast(steam === 'expirado'
        ? 'El enlace de ingreso con Steam expiró. Intentá de nuevo.'
        : 'No se pudo ingresar con Steam.', 'error');
    }
  })();

  document.getElementById('form-steam-setup').addEventListener('submit', async function (e) {
    e.preventDefault();
    const btn = document.getElementById('btn-steam-setup');
    L.clearFieldErrors();
    L.busy(btn, true);
    try {
      const data = await L.post('/usuarios/registro-steam', {
        email: document.getElementById('steam-email').value.trim(),
        nombrePiloto: document.getElementById('steam-nombre').value.trim(),
        guidSteam: document.getElementById('steam-guid').value
      });
      L.setSession(data.token, data.usuario);
      L.toast('Cuenta creada correctamente', 'success');
      location.href = next();
    } catch (err) {
      L.busy(btn, false);
      L.toast(err.message, 'error');
    }
  });
})();
