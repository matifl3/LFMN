export interface CategoriaResponse {
  id: number;
  nombre: string;
  descripcion: string | null;
  eloMinimo: number | null;
  eloMaximo: number | null;
  setupAbierto: boolean | null;
  setupFijo: boolean | null;
}

export interface CategoriaRequest {
  nombre: string;
  descripcion?: string | null;
  eloMinimo?: number | null;
  eloMaximo?: number | null;
  setupAbierto?: boolean | null;
  setupFijo?: boolean | null;
}
