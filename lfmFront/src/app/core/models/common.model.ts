export enum Rol {
  USUARIO = 'USUARIO',
  ADMIN = 'ADMIN',
  COMISARIO = 'COMISARIO',
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
