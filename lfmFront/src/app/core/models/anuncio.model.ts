export interface AnuncioResponse {
  id: number;
  titulo: string;
  contenido: string;
  urlImagen: string | null;
  fecha: string;
}

export interface AnuncioRequest {
  titulo: string;
  contenido: string;
  urlImagen?: string | null;
}
