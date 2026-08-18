export interface VueltaResponse {
  id: number;
  carreraId: number;
  usuarioId: number;
  nombrePiloto: string;
  numeroVuelta: number;
  tiempoMs: number;
  sector1: number | null;
  sector2: number | null;
  sector3: number | null;
  cortes: number | null;
  neumatico: string | null;
  tipo: string;
}
