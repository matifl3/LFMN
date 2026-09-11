const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

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
  return d.getDate() + ' ' + MESES[d.getMonth()].toUpperCase();
}

/** "17 AGO · 20:30" */
export function fmtFechaHora(iso: string | number | Date | null | undefined): string {
  const d = parseIso(iso);
  if (!d) return '—';
  return d.getDate() + ' ' + MESES[d.getMonth()].toUpperCase() + ' · ' + two(d.getHours()) + ':' + two(d.getMinutes());
}

/** "20:30 hs" */
export function fmtHora(iso: string | number | Date | null | undefined): string {
  const d = parseIso(iso);
  if (!d) return '—';
  return two(d.getHours()) + ':' + two(d.getMinutes()) + ' hs';
}

/** "hace 5 min" / "hace 2 hs" / "hace 3 días" / "17 AGO" */
export function fmtRel(iso: string | number | Date | null | undefined): string {
  const d = parseIso(iso);
  if (!d) return '';
  const seg = Math.floor((Date.now() - d.getTime()) / 1000);
  if (seg < 60) return 'ahora';
  const min = Math.floor(seg / 60);
  if (min < 60) return 'hace ' + min + ' min';
  const hs = Math.floor(min / 60);
  if (hs < 24) return 'hace ' + hs + ' hs';
  const dias = Math.floor(hs / 24);
  if (dias < 7) return 'hace ' + dias + ' días';
  return d.getDate() + ' ' + MESES[d.getMonth()];
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
export function iniciales(user: { nombrePiloto?: string; email?: string } | null | undefined): string {
  if (!user) return '?';
  const n = user.nombrePiloto || user.email || '?';
  const parts = n.trim().split(/\s+/);
  const first = parts[0]?.[0] ?? '';
  const last = parts.length > 1 ? parts[parts.length - 1]?.[0] ?? '' : '';
  return (first + last).toUpperCase() || '?';
}

export function sanitizeUrl(url: string | null | undefined): string {
  if (!url) return '';
  const s = String(url).trim().toLowerCase();
  if (s.startsWith('javascript:') || s.startsWith('data:') || s.startsWith('vbscript:')) return '';
  return url;
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