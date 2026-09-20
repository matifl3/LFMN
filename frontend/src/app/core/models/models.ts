export interface PageResponse<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export type Rol = 'USUARIO' | 'ADMIN' | 'COMISARIO';

export interface Usuario {
  id: number;
  email: string;
  nombrePiloto: string;
  fotoPerfil?: string;
  guidSteam?: string;
  elo?: number;
  safetyRating?: number;
  rol: Rol;
  fechaRegistro?: string;
  passwordEstablecida?: boolean;
  habilitado?: boolean;
}

export interface UsuarioBasico {
  id: number;
  nombrePiloto: string;
  fotoPerfil?: string;
  elo?: number;
  safetyRating?: number;
}

export interface LoginResponse {
  token: string;
  usuario: Usuario;
}

export interface StatsResponse {
  carrerasDisputadas: number;
  victorias: number;
  podios: number;
  poles: number;
  vueltasRapidas: number;
  porcentajeFinalizacion: number;
}

export interface HistorialRating {
  id: number;
  cambio: number;
  motivo?: string;
  fecha: string;
  carreraId?: number;
}

export type EstadoCarrera =
  | 'PROGRAMADA'
  | 'INSCRIPCIONES_ABIERTAS'
  | 'INSCRIPCIONES_CERRADAS'
  | 'EN_CURSO'
  | 'FINALIZADA'
  | 'CANCELADA';

export interface Carrera {
  id: number;
  nombre: string;
  fecha: string;
  practicaFecha?: string;
  circuito: string;
  campeonatoId?: number;
  campeonatoNombre?: string;
  categoriaId: number;
  categoriaNombre?: string;
  estado: EstadoCarrera;
  cupoMaximo?: number;
  cuposInscritos?: number;
  servidor?: string;
  archivoId?: number;
  archivoNombre?: string;
  linkPista?: string;
  linkAuto?: string;
}

export interface CarreraAcceso {
  carreraId: number;
  servidor?: string;
  contrasenaServidor?: string;
}

export interface PosicionElo {
  posicion: number;
  deltaElo: number;
}

export interface EloEstimado {
  posicionEsperada: number;
  deltaEsperado: number;
  deltaMejorCaso: number;
  deltaPeorCaso: number;
  detalle: PosicionElo[];
}

export interface Categoria {
  id: number;
  nombre: string;
  descripcion?: string;
  eloMinimo?: number;
  eloMaximo?: number;
  setupAbierto?: boolean;
  setupFijo?: boolean;
}

export interface Campeonato {
  id: number;
  nombre: string;
  temporada?: string;
  categoriaId: number;
  categoriaNombre?: string;
  estado: string;
  sistemaPuntos?: string;
}

export interface TablaPosicion {
  usuarioId: number;
  nombrePiloto: string;
  puntos: number;
  posicion: number;
}

export interface Inscripcion {
  id: number;
  carreraId: number;
  carreraNombre: string;
  categoriaNombre?: string;
  carreraFecha: string;
  usuarioId: number;
  nombrePiloto?: string;
  fotoPerfil?: string;
  elo?: number;
  safetyRating?: number;
  estado: string;
  fechaInscripcion: string;
}

export interface ResultadoCarrera {
  id: number;
  carreraId: number;
  carreraNombre: string;
  categoriaNombre?: string;
  usuarioId: number;
  nombrePiloto?: string;
  posicionFinal?: number;
  tiempoTotal?: number;
  vueltaRapida?: number;
  modeloAuto?: string;
  skinAuto?: string;
  poles?: boolean;
  finalizo?: boolean;
  eloGanado?: number;
  srGanado?: number;
}

export interface SesionClasificacion {
  id: number;
  carreraId: number;
  usuarioId: number;
  nombrePiloto?: string;
  fecha: string;
  tiempo: number;
  diferenciaPole?: number;
  modeloAuto?: string;
  skinAuto?: string;
}

export interface Vuelta {
  id: number;
  carreraId: number;
  usuarioId: number;
  nombrePiloto?: string;
  numeroVuelta: number;
  tiempoMs: number;
  sector1?: number;
  sector2?: number;
  sector3?: number;
  cortes?: number;
  neumatico?: string;
  tipo?: string;
}

export interface VueltaAnalisis extends Vuelta {
  deltaLiderMs?: number;
  posicionEnVuelta?: number;
}

export interface VueltaResumen {
  mejorVueltaMs?: number;
  numeroVueltaMejor?: number;
  mejorS1?: number;
  mejorS2?: number;
  mejorS3?: number;
  teoricaMs?: number;
  potencialMs?: number;
  mejorS1Parrilla?: number;
  mejorS2Parrilla?: number;
  mejorS3Parrilla?: number;
  teoricaParrillaMs?: number;
  vueltasTotales?: number;
  mediaMs?: number;
  desvioMs?: number;
  dentroDe500ms?: number;
  dentroDe1s?: number;
  posicionGrilla?: number;
  posicionFinal?: number;
  posicionesGanadas?: number;
  posicionPico?: number;
}

export interface Anuncio {
  id: number;
  titulo: string;
  contenido?: string;
  urlImagen?: string;
  fecha: string;
  destacado?: boolean;
}

export interface Notificacion {
  id: number;
  usuarioId: number;
  tipo: string;
  mensaje: string;
  leida: boolean;
  fecha: string;
  link?: string;
}

export interface Logro {
  id: number;
  nombre: string;
  descripcion?: string;
  tipoCondicion: string;
  valorCondicion: number;
  icono?: string;
  recompensas?: Recompensa[];
}

export interface Recompensa {
  id: number;
  logroId?: number;
  descripcion: string;
  tipo: string;
}

export interface UsuarioLogro {
  logroId: number;
  nombre: string;
  descripcion?: string;
  tipoCondicion: string;
  valorCondicion: number;
  progreso: number;
  obtenido: boolean;
  fechaObtencion?: string;
}

export interface Setup {
  id: number;
  titulo: string;
  descripcion?: string;
  circuito: string;
  vehiculo: string;
  archivo?: string;
  autorId: number;
  autorNombre: string;
  autorFoto?: string;
  categoriaId?: number;
  categoriaNombre?: string;
  fechaPublicacion: string;
  promedioCalificacion?: number;
}

export interface SetupComentario {
  id: number;
  setupId: number;
  usuarioId: number;
  nombrePiloto: string;
  fotoPerfil?: string;
  texto: string;
  fecha: string;
}

export interface SetupCalificacion {
  id: number;
  setupId: number;
  usuarioId: number;
  puntaje: number;
}

export type EstadoIncidente = 'PENDIENTE' | 'EN_ANALISIS' | 'RESUELTO';

export interface Incidente {
  id: number;
  carreraId: number;
  carreraNombre: string;
  categoriaNombre?: string;
  reportanteId: number;
  reportanteNombre?: string;
  vuelta?: number;
  descripcion?: string;
  videoUrl?: string;
  fecha: string;
  estado: EstadoIncidente;
  quorumRequerido: number;
}

export interface IncidentePiloto {
  id: number;
  incidenteId: number;
  usuarioId: number;
  nombrePiloto?: string;
  rol: string;
}

export interface Voto {
  id: number;
  incidenteId: number;
  comisarioId: number;
  decision: string;
  comentario?: string;
  fecha: string;
}

export interface Resolucion {
  id: number;
  incidenteId: number;
  comisarioId: number;
  explicacion: string;
  fecha: string;
}

export interface DecisionComisario {
  incidenteId: number;
  decision?: string | null;
  comentario?: string;
  fecha: string;
  tipo: string;
}

export interface Sancion {
  id: number;
  usuarioId: number;
  carreraId?: number;
  carreraNombre?: string;
  categoriaNombre?: string;
  resolucionId?: number;
  tipo: string;
  valor?: number;
  motivo?: string;
  origen: string;
  idExterno?: string;
  fecha: string;
}

export interface Apelacion {
  id: number;
  sancionId: number;
  usuarioId: number;
  nombrePiloto?: string;
  motivo: string;
  estado: string;
  respuestaAdmin?: string;
  fecha: string;
}

export interface ArchivoCarrera {
  id: number;
  nombre: string;
  ruta: string;
  tipo: string;
}

export interface UsuarioRecompensa {
  id?: number;
  usuarioId?: number;
  recompensaId?: number;
  reclamada?: boolean;
  fechaReclamada?: string;
}

export interface Estadisticas {
  totalUsuarios: number;
  usuariosActivos: number;
  usuariosAdmin: number;
  usuariosComisario: number;
  totalCarreras: number;
  carrerasProgramadas: number;
  carrerasInscripcionesAbiertas: number;
  carrerasInscripcionesCerradas: number;
  carrerasEnCurso: number;
  carrerasFinalizadas: number;
  carrerasCanceladas: number;
  totalIncidentes: number;
  incidentesPendientes: number;
  incidentesEnAnalisis: number;
  incidentesResueltos: number;
  totalSanciones: number;
  totalInscripciones: number;
  apelacionesPendientes: number;
  setupsPublicados: number;
  anuncios: number;
  campeonatos: number;
  categorias: number;
}