import { Rol } from './common.model';

export interface UsuarioResponse {
  id: number;
  email: string;
  nombrePiloto: string | null;
  fotoPerfil: string | null;
  guidSteam: string | null;
  elo: number;
  safetyRating: number;
  rol: Rol;
  fechaRegistro: string;
}

export interface UsuarioRequest {
  email: string;
  password: string;
  nombrePiloto?: string | null;
  fotoPerfil?: string | null;
  guidSteam?: string | null;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  usuario: UsuarioResponse;
}

export interface CambioPasswordRequest {
  passwordActual: string;
  nuevaPassword: string;
}

export interface SteamRequest {
  guidSteam: string;
}

export interface StatsResponse {
  carrerasDisputadas: number;
  victorias: number;
  podios: number;
  poles: number;
  vueltasRapidas: number;
  porcentajeFinalizacion: number;
}

export interface EloHistorialResponse {
  id: number;
  cambio: number;
  motivo: string;
  fecha: string;
  carreraId: number | null;
}

export interface SafetyRatingHistorialResponse {
  id: number;
  cambio: number;
  motivo: string;
  fecha: string;
  carreraId: number | null;
}
