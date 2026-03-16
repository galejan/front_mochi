// Generated from Java models - Proyectos.java
export interface Proyecto {
  id: number;
  referencia: string;
  nombre: string;
  fecha: string; // Date serialized as string
  fecha_compromiso: string | null;
  email_responsable: string;
  metrosLineales: number;
  totalPaneles: number;
  tipoProyecto: string;
  tipoProduccion: string;
  version: number;
  nombre_cliente: string;
  email_gestor: string;
  ral: string;
  pais: string;
  idCielo: number;
  disenyado: boolean;
  fecha_corte: string | null;
  tipo_producto: string;
  otros_lacables: string | null;
  actividades?: Actividad[];
  barras?: Barra[];
}

// Generated from Java models - Actividades.java
export interface Actividad {
  id: number;
  codigo: number;
  descripcion: string;
  proyecto?: Proyecto;
  eventos?: Evento[];
}

// Generated from Java models - Barras.java
export interface Barra {
  id: number;
  longitud: number;
  proyecto?: Proyecto;
  tipoPerfil: string;
  posicion: string;
  tipoSistema: string;
  identificadorUnicoBarra: string;
  color: string;
  grosor: number;
  unidades: number;
  perteneceASeccion: number | null;
  invertir: boolean;
  anguloDerecho: number | null;
  anguloIzquierdo: number | null;
  longitudInterior: number | null;
  longitudExterior: number | null;
  tagA: string | null;
  tagB: string | null;
  tagC: string | null;
  tagD: string | null;
  editada: boolean;
  mochilificada: number;
  grupoStock: number | null;
  identificador_stock: number | null;
  merma: number | null;
  resto: number | null;
}

// Generated from Java models - StockBarras.java
export interface StockBarra {
  id: number;
  tipoPerfil: string;
  tipoSistema: string;
  color: string;
  longitud: number;
  unidades: number;
}

// Generated from Java models - Usuarios.java
export interface Usuario {
  id: number;
  usuario: string;
  password?: string;
  nombre: string;
  activado: boolean;
  email: string;
}

// Generated from Java models - Roles.java
export interface Rol {
  id: number;
  nombre: string;
  descripcion: string;
}

// Generated from Java models - Eventos.java
export interface Evento {
  id: number;
  registro: string;
  accion?: Accion;
  actividad?: Actividad;
  usuario?: Usuario;
}

// Generated from Java models - Acciones.java
export interface Accion {
  id: number;
  nombre: string;
  descripcion: string;
}

// Generated from Java models - Status.java (API response)
export interface ApiStatus {
  code: number;
  message: string;
}

// Generated from Java models - LogBarra.java
export interface LogBarra {
  id: number;
  perfil: string;
  color: string;
  longitud: number;
  longitudMochilificada: number;
  unidadesBarrasMochi: number;
  merma: number;
  resto: number;
  obligadaStock: boolean;
  registro: string;
  procesado: string;
  proyecto: string;
}
