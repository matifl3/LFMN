export enum TipoArchivo {
  SETUP = 'SETUP',
  PAQUETE = 'PAQUETE',
  CARGA = 'CARGA',
  OTRO = 'OTRO',
}

export interface ArchivoCarreraResponse {
  id: number;
  nombre: string;
  ruta: string;
  tipo: TipoArchivo;
}
