export interface SesionClasificacionResponse {
  id: number;
  carreraId: number;
  usuarioId: number;
  nombrePiloto: string;
  fecha: string;
  tiempo: number;
  diferenciaPole: number | null;
  modeloAuto: string | null;
  skinAuto: string | null;
}
