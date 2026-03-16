import axios from 'axios';
import type { 
  Proyecto, 
  Actividad, 
  Barra, 
  StockBarra, 
  Usuario, 
  Rol, 
  Evento, 
  ApiStatus
} from '../types';

const API_BASE = '/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ==================== PROYECTOS ====================

export const proyectosApi = {
  getAll: () => api.get<Proyecto[]>('/proyectos'),
  
  getById: (id: number) => api.get<Proyecto>(`/proyectos/${id}`),
  
  getByReferencia: (ref: string) => api.get<Proyecto>(`/proyectos/navision/${ref}`),
  
  getEnCurso: () => api.get<Proyecto[]>('/proyectos/encurso'),
  
  create: (proyecto: Partial<Proyecto>) => api.post<ApiStatus>('/proyectos', proyecto),
  
  update: (proyecto: Partial<Proyecto>) => api.put<ApiStatus>('/proyectos', proyecto),
  
  // Acciones sobre proyectos
  mochilificar: (referencia: string, emisor?: number) => {
    const url = emisor !== undefined 
      ? `/proyectos/${referencia}/mochilificar/${emisor}`
      : `/proyectos/${referencia}/mochilificar/`;
    return api.get<ApiStatus>(url);
  },
  
  liberarBarras: (referencia: string) => 
    api.post<ApiStatus>(`/proyectos/${referencia}/liberabarras/`),
  
  preparaBarras: (referencia: string) => 
    api.post<ApiStatus>(`/proyectos/${referencia}/preparaBarras/`),
  
  deleteBarras: (referencia: string) => 
    api.post<ApiStatus>(`/proyectos/${referencia}/barras/delete/`),
  
  generaFicheroCorte: (referencia: string) => 
    api.get<ApiStatus>(`/proyectos/${referencia}/generaFicheroCorte/`),
  
  agregaRestos: (referencia: string, codigoActividad: number) => 
    api.get<ApiStatus>(`/proyectos/${referencia}/agregarestos/${codigoActividad}`),
};

// ==================== ACTIVIDADES ====================

export const actividadesApi = {
  getAll: () => api.get<Actividad[]>('/proyectos/actividades'),
  
  getByProyecto: (referencia: string) => 
    api.get<Actividad[]>(`/proyectos/${referencia}/actividades`),
  
  getById: (id: number) => api.get<Actividad>(`/proyectos/actividades/${id}`),
  
  getByRefYCod: (referencia: string, codigo: number) => 
    api.get<Actividad>(`/proyectos/${referencia}/actividades/${codigo}`),
  
  create: (actividad: Partial<Actividad>) => 
    api.post<ApiStatus>('/proyectos/actividades/', actividad),
  
  update: (actividad: Partial<Actividad>) => 
    api.put<ApiStatus>('/proyectos/actividades/', actividad),
  
  delete: (referencia: string, codigo: number) => 
    api.delete<ApiStatus>(`/proyectos/${referencia}/actividades/${codigo}`),
};

// ==================== BARRAS ====================

export const barrasApi = {
  getByProyecto: (referencia: string) => 
    api.get<Barra[]>(`/proyectos/${referencia}/barras`),
  
  create: (barra: Partial<Barra>) => 
    api.post<ApiStatus>('/proyectos/barras/', barra),
};

// ==================== STOCK BARRAS ====================

export const stockBarrasApi = {
  getAll: () => api.get<StockBarra[]>('/stockbarras'),
  
  getById: (id: number) => api.get<StockBarra>(`/stockbarras/${id}`),
  
  // Gestor: migración, agregar, eliminar barras de stock
  gestor: (data: {
    color: string;
    tipoSistema: string;
    tipoPerfil: string;
    longitudOrigen: number;
    longitudDestino: number;
    unidades: number;
  }) => api.post<ApiStatus>('/stockbarras/gestor/', data),
  
  getInventario: () => api.get<string>('/inventario/stockbarras/'),
};

// ==================== USUARIOS ====================

export const usuariosApi = {
  getAll: () => api.get<Usuario[]>('/usuarios'),
  
  getByNombre: (nombre: string) => api.get<Usuario>(`/usuarios/${nombre}`),
  
  create: (usuario: Partial<Usuario>) => api.post<ApiStatus>('/usuarios', usuario),
};

// ==================== ROLES ====================

export const rolesApi = {
  getAll: () => api.get<Rol[]>('/roles'),
  
  getById: (id: number) => api.get<Rol>(`/roles/${id}`),
  
  create: (rol: Partial<Rol>) => api.post<ApiStatus>('/roles/', rol),
};

// ==================== EVENTOS ====================

export const eventosApi = {
  getById: (id: number) => api.get<Evento>(`/eventos/${id}`),
  
  getByActividad: (idActividad: number) => 
    api.get<Evento[]>(`/eventos/actividades/${idActividad}`),
  
  getByActividadYUsuario: (idActividad: number, usuario: string) => 
    api.get<Evento[]>(`/eventos/actividades/${idActividad}/usuarios/${usuario}`),
  
  getByUsuario: (idUsuario: number) => 
    api.get<Evento[]>(`/eventos/usuarios/${idUsuario}`),
  
  create: (evento: Partial<Evento>) => api.post<ApiStatus>('/eventos/', evento),
  
  update: (evento: Partial<Evento>) => api.put<ApiStatus>('/eventos/', evento),
};

// ==================== LOG BARRAS ====================

// No hay endpoint específico en el controller, pero queda disponible si se necesita

export default api;
