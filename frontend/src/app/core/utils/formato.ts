import { t } from '../i18n/t';

/** Abreviatura del mes (0-11) en el idioma activo. */
function mes(i: number): string {
  return t('comun.meses').split(',')[i] ?? '';
}

export function parseIso(iso: string | number | Date | null | undefined): Date | null {
  if (!iso) return null;
  if (iso instanceof Date) return isNaN(iso.getTime()) ? null : iso;
  const d = new Date(iso);
  return isNaN(d.getTime()) ? null : d;
}

function two(n: number): string {
  return String(n).padStart(2, '0');
}

/** "17 AGO" */
export function fmtFecha(iso: string | number | Date | null | undefined): string {
  const d = parseIso(iso);
  if (!d) return '—';
  return d.getDate() + ' ' + mes(d.getMonth()).toUpperCase();
}

/** "17" (día del mes); sin fecha → "—". Para el bloque `.race-date`. */
export function fmtDia(iso: string | number | Date | null | undefined): string {
  const d = parseIso(iso);
  return d ? String(d.getDate()) : '—';
}

/** "AGO"; sin fecha → "". Para el bloque `.race-date`. */
export function fmtMes(iso: string | number | Date | null | undefined): string {
  const d = parseIso(iso);
  return d ? mes(d.getMonth()).toUpperCase() : '';
}

/**
 * Cuenta regresiva a una carrera: "Faltan 2 d 4 h", "Faltan 3 h 20 min",
 * "Faltan 5 min". Una carrera EN_CURSO devuelve "En curso ahora"; sin fecha, "".
 */
export function fmtCuentaRegresiva(
  iso: string | number | Date | null | undefined,
  ahora: number,
  estado?: string,
): string {
  if (estado === 'EN_CURSO') return t('comun.tiempo.enCursoAhora');
  const d = parseIso(iso);
  if (!d) return '';
  const diff = d.getTime() - ahora;
  if (diff <= 0) return t('comun.tiempo.arrancaEnInstantes');
  const MIN = 60_000;
  const HORA = 60 * MIN;
  const DIA = 24 * HORA;
  const dias = Math.floor(diff / DIA);
  const hs = Math.floor((diff % DIA) / HORA);
  const min = Math.floor((diff % HORA) / MIN);
  if (dias > 0) return t('comun.tiempo.faltanDiasHoras', { d: dias, h: hs });
  if (hs > 0) return t('comun.tiempo.faltanHorasMin', { h: hs, m: min });
  return t('comun.tiempo.faltanMin', { m: Math.max(min, 1) });
}

/** "17 AGO · 20:30" */
export function fmtFechaHora(iso: string | number | Date | null | undefined): string {
  const d = parseIso(iso);
  if (!d) return '—';
  return (
    d.getDate() +
    ' ' +
    mes(d.getMonth()).toUpperCase() +
    ' · ' +
    two(d.getHours()) +
    ':' +
    two(d.getMinutes())
  );
}

/** "20:30 hs" */
export function fmtHora(iso: string | number | Date | null | undefined): string {
  const d = parseIso(iso);
  if (!d) return '—';
  return t('comun.tiempo.horaSufijo', { hora: two(d.getHours()) + ':' + two(d.getMinutes()) });
}

/** "hace 5 min" / "hace 2 hs" / "hace 3 días" / "17 AGO" */
export function fmtRel(iso: string | number | Date | null | undefined): string {
  const d = parseIso(iso);
  if (!d) return '';
  const seg = Math.floor((Date.now() - d.getTime()) / 1000);
  if (seg < 60) return t('comun.tiempo.ahora');
  const min = Math.floor(seg / 60);
  if (min < 60) return t('comun.tiempo.haceMin', { n: min });
  const hs = Math.floor(min / 60);
  if (hs < 24) return t('comun.tiempo.haceHs', { n: hs });
  const dias = Math.floor(hs / 24);
  if (dias < 7) return t('comun.tiempo.haceDias', { n: dias });
  return d.getDate() + ' ' + mes(d.getMonth());
}

/** ms → "m:ss.mmm"; null/NaN → "—" */
export function fmtLap(ms: number | null | undefined): string {
  if (ms === null || ms === undefined || isNaN(ms)) return '—';
  const total = ms / 1000;
  const m = Math.floor(total / 60);
  const s = Math.floor(total % 60);
  const ml = Math.floor((total * 1000) % 1000);
  return (m > 0 ? m + ':' : '') + two(s) + '.' + String(ml).padStart(3, '0');
}

/** Diferencia respecto a pole: "+2:34.123" */
export function fmtDif(ms: number | null | undefined): string {
  if (ms === null || ms === undefined || isNaN(ms)) return '—';
  const sign = ms < 0 ? '−' : '+';
  const abs = Math.abs(ms);
  return sign + fmtLap(abs);
}

/** Iniciales de un piloto */
export function iniciales(
  user: { nombrePiloto?: string; email?: string } | null | undefined,
): string {
  if (!user) return '?';
  const n = user.nombrePiloto || user.email || '?';
  const parts = n.trim().split(/\s+/);
  const first = parts[0]?.[0] ?? '';
  const last = parts.length > 1 ? (parts[parts.length - 1]?.[0] ?? '') : '';
  return (first + last).toUpperCase() || '?';
}

/** Lista de permitidos: rutas internas (`/x`, no `//host`) y http(s). Todo lo demas se descarta. */
export function sanitizeUrl(url: string | null | undefined): string {
  if (!url) return '';
  const s = String(url).trim();
  if (s.startsWith('/') && !s.startsWith('//')) return s;
  if (/^https?:\/\//i.test(s)) return s;
  return '';
}

/** Paginación inconsistente del backend: `.content || r` tipado */
export function normalizeList<T>(r: unknown): T[] {
  if (r === null || r === undefined) return [];
  if (Array.isArray(r)) return r as T[];
  const content = (r as { content?: unknown }).content;
  if (Array.isArray(content)) return content as T[];
  return [];
}

export function toNumber(v: unknown, fallback: number | null = null): number | null {
  if (v === null || v === undefined || v === '') return fallback;
  const n = Number(v);
  return isNaN(n) ? fallback : n;
}

/**
 * Devuelve una URL navegable o `null` si el valor no es un enlace. Acepta lo que
 * suele pegar la gente: con o sin `http://`, o solo `host:puerto`, que es como
 * se ve un server de Assetto Corsa.
 */
export function normalizeUrl(valor: string | null | undefined): string | null {
  const v = (valor ?? '').trim();
  if (!v) return null;
  if (/^https?:\/\//i.test(v)) return v;
  // host:puerto o un dominio suelto. Excluye "Servidor 1" y frases sueltas.
  if (/^[a-z0-9-]+(\.[a-z0-9-]+)+(:\d+)?(\/\S*)?$/i.test(v)) return 'http://' + v;
  return null;
}
