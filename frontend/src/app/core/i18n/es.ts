import { nsEs } from './ns';

/**
 * Textos en español: es el idioma base y la fuente de verdad de las claves.
 * `en.ts` se tipa con `typeof es`, así que si acá se agrega una clave y falta
 * en inglés, `npm run build` falla en vez de mostrar la clave cruda.
 *
 * Los parámetros van con doble llave: `{{ nombre }}`.
 */
export const es = {
  ...nsEs,
  idioma: {
    selector: 'Idioma',
    tituloPagina: 'Late Brake Motorsport — Liga Argentina de Sim Racing',
    es: 'Español',
    en: 'English',
  },
  nav: {
    carreras: 'Carreras',
    campeonato: 'Campeonato',
    categorias: 'Categorías',
    incidentes: 'Incidentes',
    setups: 'Setups',
    logros: 'Logros',
  },
  header: {
    abrirMenu: 'Abrir menú',
    cerrarMenu: 'Cerrar menú',
    menuPrincipal: 'Menú principal',
    menuUsuario: 'Menú de usuario',
    notificaciones: 'Notificaciones',
    miCuenta: 'Mi cuenta',
    miPerfil: 'Mi perfil',
    misCampeonatos: 'Mis campeonatos',
    admin: 'Admin',
    salir: 'Salir',
    ingresar: 'Ingresar',
    sesionCerrada: 'Sesión cerrada',
  },
  auth: {
    titulo: 'Ingresar a LBM',
    subtitulo: 'Accedé con tu cuenta de piloto para inscribirte a carreras y ver tus estadísticas.',
    tabLogin: 'Iniciar sesión',
    tabRegistro: 'Crear cuenta',
    email: 'Email',
    password: 'Contraseña',
    nombrePiloto: 'Nombre de piloto',
    placeholderEmail: 'piloto@ejemplo.com',
    placeholderNombre: 'Ej. Franco Gómez Reta',
    revisarCampos: 'Revisá los campos marcados para continuar.',
    ingresar: 'Ingresar',
    olvidastePassword: '¿Olvidaste tu contraseña?',
    hintPassword: 'Mínimo 6 caracteres. Necesaria para ingresar por email.',
    crearCuenta: 'Crear cuenta',
    steamNoVinculado: 'Tu Steam no está vinculado',
    steamCompletar: 'Completá los datos para crear tu cuenta de piloto y vincular tu Steam.',
    oContinuarCon: 'o continuar con',
    terminos:
      'Al continuar aceptás el Reglamento Deportivo y el Código de Conducta de LBM Nacional.',
    errores: {
      emailRequerido: 'Ingresá tu email.',
      passwordRequerida: 'Ingresá tu contraseña.',
      nombreRequerido: 'Ingresá tu nombre de piloto.',
      obligatorio: 'Este campo es obligatorio.',
      emailInvalido: 'El email no es válido.',
      minimo: 'Mínimo {{ n }} caracteres.',
    },
    toast: {
      passwordActualizada: 'Contraseña actualizada. Ingresá con tu nueva contraseña.',
      completarLogin: 'Completá tu email y contraseña.',
      bienvenido: '¡Bienvenido, {{ nombre }}!',
      piloto: 'piloto',
      steamNoIniciado: 'No se pudo iniciar el ingreso con Steam.',
      completarSteam: 'Completá tu nombre de piloto y email.',
      cuentaCreada: 'Cuenta creada correctamente',
      completarRegistro: 'Completá nombre de piloto, email y contraseña (mínimo 6 caracteres).',
      cuentaCreadaBienvenido: 'Cuenta creada correctamente. ¡Bienvenido, {{ nombre }}!',
      sinSteam:
        'No tenés Steam vinculado: los resultados de carrera no se sumarán a tu perfil. Vinculalo desde Mi perfil',
      steamExpirado: 'El enlace de ingreso con Steam expiró. Intentá de nuevo.',
      steamError: 'No se pudo ingresar con Steam.',
    },
  },
};

export type Traducciones = typeof es;
