export interface IncidenteResponse {
  id: number;
  carreraId: number;
  carreraNombre: string;
  categoriaNombre: string;
  reportanteId: number;
  reportanteNombre: string;
  vuelta: number | null;
  descripcion: string | null;
  videoUrl: string | null;
  estado: string;
}
