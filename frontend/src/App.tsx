import { useState, useEffect } from 'react';
import { proyectosApi, barrasApi, stockBarrasApi } from './api';
import type { Proyecto, Barra } from './types';
import './App.css';

interface LogEntry {
  id: number;
  action: string;
  result: string;
  timestamp: Date;
  success: boolean;
}

function App() {
  const [referenciaInput, setReferenciaInput] = useState('');
  const [proyectoSeleccionado, setProyectoSeleccionado] = useState<Proyecto | null>(null);
  const [barras, setBarras] = useState<Barra[]>([]);
  const [loading, setLoading] = useState(false);
  const [loadingBarras, setLoadingBarras] = useState(false);
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [stockBarras, setStockBarras] = useState<number>(0);
  const [resumenMochilificacion, setResumenMochilificacion] = useState<{
    totalBarras: number;
    barrasProcesadas: number;
    longitudTotal: number;
    longitudUsada: number;
    mermaTotal: number;
    restoTotal: number;
    porcentajeMerma: number;
  } | null>(null);

  let logId = 0;

  const addLog = (action: string, result: string, success: boolean) => {
    setLogs(prev => [{ id: logId++, action, result, timestamp: new Date(), success }, ...prev]);
  };

  // Calcular resumen de merma teórica
  const calcularResumenMerma = (barrasData: Barra[]) => {
    const totalBarras = barrasData.length;
    const barrasProcesadas = barrasData.filter(b => b.mochilificada && b.mochilificada > 0).length;
    
    const longitudTotal = barrasData.reduce((acc, b) => acc + (b.longitud || 0) * (b.unidades || 0), 0);
    
    // Longitud usada = longitud total - mermas
    const mermaTotal = barrasData.reduce((acc, b) => acc + ((b.merma || 0) * (b.unidades || 0)), 0);
    const restoTotal = barrasData.reduce((acc, b) => acc + ((b.resto || 0) * (b.unidades || 0)), 0);
    
    const longitudUsada = longitudTotal - mermaTotal + restoTotal;
    const porcentajeMerma = longitudTotal > 0 ? (mermaTotal / longitudTotal) * 100 : 0;

    setResumenMochilificacion({
      totalBarras,
      barrasProcesadas,
      longitudTotal: Math.round(longitudTotal * 100) / 100,
      longitudUsada: Math.round(longitudUsada * 100) / 100,
      mermaTotal: Math.round(mermaTotal * 100) / 100,
      restoTotal: Math.round(restoTotal * 100) / 100,
      porcentajeMerma: Math.round(porcentajeMerma * 100) / 100,
    });
  };

  // Cargar stock al iniciar
  useEffect(() => {
    cargarStock();
  }, []);

  const cargarStock = async () => {
    try {
      const res = await stockBarrasApi.getAll();
      const total = res.data.reduce((acc, b) => acc + b.unidades, 0);
      setStockBarras(total);
    } catch (err: any) {
      console.error('Error cargando stock:', err);
    }
  };

  const seleccionarProyecto = async (ref: string) => {
    if (!ref.trim()) {
      addLog('ERROR', 'Ingresa una referencia', false);
      return;
    }
    setLoading(true);
    setBarras([]);
    try {
      const res = await proyectosApi.getByReferencia(ref.trim());
      if (res.data && res.data.referencia) {
        setProyectoSeleccionado(res.data);
        addLog('SELECT', `Proyecto cargado: ${res.data.referencia} - ${res.data.nombre}`, true);
        await cargarBarras(ref.trim());
      } else {
        setProyectoSeleccionado(null);
        addLog('ERROR', `No se encontró el proyecto: ${ref}`, false);
      }
    } catch (err: any) {
      setProyectoSeleccionado(null);
      addLog('ERROR', `Error buscando proyecto: ${err.message}`, false);
    } finally {
      setLoading(false);
    }
  };

  const handleBuscar = (e: React.FormEvent) => {
    e.preventDefault();
    seleccionarProyecto(referenciaInput);
  };

  const cargarBarras = async (referencia: string) => {
    setLoadingBarras(true);
    setResumenMochilificacion(null);
    try {
      const res = await barrasApi.getByProyecto(referencia);
      setBarras(res.data);
      calcularResumenMerma(res.data);
      addLog('BARRAS', `Cargadas ${res.data.length} barras del proyecto`, true);
    } catch (err: any) {
      addLog('ERROR', `Error cargando barras: ${err.message}`, false);
    } finally {
      setLoadingBarras(false);
    }
  };

  // ==================== ACCIONES ====================

  const handleMochilificar = async (emisor?: number) => {
    if (!proyectoSeleccionado) {
      addLog('ERROR', 'Selecciona un proyecto primero', false);
      return;
    }
    setLoading(true);
    try {
      const emisorText = emisor === 0 ? 'Diseño' : emisor === 1 ? 'Excell' : emisor === 2 ? 'Forzado' : 'Normal';
      addLog('MOCHILIFICAR', `Iniciando mochilificación (${emisorText})...`, true);
      const res = await proyectosApi.mochilificar(proyectoSeleccionado.referencia, emisor);
      addLog('MOCHILIFICAR', res.data.message, res.data.code === 1);
      if (res.data.code === 1) {
        await cargarBarras(proyectoSeleccionado.referencia);
      }
    } catch (err: any) {
      addLog('ERROR', `Error: ${err.message}`, false);
    } finally {
      setLoading(false);
    }
  };

  const handleLiberarBarras = async () => {
    if (!proyectoSeleccionado) {
      addLog('ERROR', 'Selecciona un proyecto primero', false);
      return;
    }
    setLoading(true);
    try {
      addLog('LIBERAR', 'Liberando barras...', true);
      const res = await proyectosApi.liberarBarras(proyectoSeleccionado.referencia);
      addLog('LIBERAR', res.data.message, res.data.code === 1);
      await cargarBarras(proyectoSeleccionado.referencia);
      await cargarStock();
    } catch (err: any) {
      addLog('ERROR', `Error: ${err.message}`, false);
    } finally {
      setLoading(false);
    }
  };

  const handlePreparaBarras = async () => {
    if (!proyectoSeleccionado) {
      addLog('ERROR', 'Selecciona un proyecto primero', false);
      return;
    }
    setLoading(true);
    try {
      addLog('PREPARAR', 'Preparando barras para re-mochilificar...', true);
      const res = await proyectosApi.preparaBarras(proyectoSeleccionado.referencia);
      addLog('PREPARAR', res.data.message, res.data.code === 1);
      await cargarBarras(proyectoSeleccionado.referencia);
    } catch (err: any) {
      addLog('ERROR', `Error: ${err.message}`, false);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteBarras = async () => {
    if (!proyectoSeleccionado) {
      addLog('ERROR', 'Selecciona un proyecto primero', false);
      return;
    }
    setLoading(true);
    try {
      addLog('DELETE', 'Eliminando barras del proyecto...', true);
      const res = await proyectosApi.deleteBarras(proyectoSeleccionado.referencia);
      addLog('DELETE', res.data.message, res.data.code === 1);
      await cargarBarras(proyectoSeleccionado.referencia);
    } catch (err: any) {
      addLog('ERROR', `Error: ${err.message}`, false);
    } finally {
      setLoading(false);
    }
  };

  const handleGeneraFicheroCorte = async () => {
    if (!proyectoSeleccionado) {
      addLog('ERROR', 'Selecciona un proyecto primero', false);
      return;
    }
    setLoading(true);
    try {
      addLog('FICHERO', 'Generando ficheo de corte...', true);
      const res = await proyectosApi.generaFicheroCorte(proyectoSeleccionado.referencia);
      addLog('FICHERO', res.data.message, res.data.code === 1);
    } catch (err: any) {
      addLog('ERROR', `Error: ${err.message}`, false);
    } finally {
      setLoading(false);
    }
  };

  const handleAgregaRestos = async () => {
    if (!proyectoSeleccionado) {
      addLog('ERROR', 'Selecciona un proyecto primero', false);
      return;
    }
    setLoading(true);
    try {
      // Código de actividad por defecto 0
      addLog('RESTOS', 'Agregando restos...', true);
      const res = await proyectosApi.agregaRestos(proyectoSeleccionado.referencia, 0);
      addLog('RESTOS', res.data.message, res.data.code === 1);
    } catch (err: any) {
      addLog('ERROR', `Error: ${err.message}`, false);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-container">
      <header className="header">
        <h1>🛠️ Mochi - Panel de Control</h1>
        <div className="stock-badge">
          📦 Stock Total: <strong>{stockBarras}</strong> barras
        </div>
      </header>

      <main className="main-content">
        {/* Buscar Proyecto por Referencia */}
        <section className="panel">
          <h2>🔍 Buscar Proyecto</h2>
          <form onSubmit={handleBuscar} className="buscar-form">
            <input 
              type="text"
              placeholder="Ingresa referencia (ej: 12345)"
              value={referenciaInput}
              onChange={(e) => setReferenciaInput(e.target.value)}
              disabled={loading}
              className="buscar-input"
            />
            <button type="submit" disabled={loading} className="buscar-btn">
              {loading ? '⏳' : '🔍'} Buscar
            </button>
          </form>

          {proyectoSeleccionado && (
            <div className="proyecto-info">
              <h3>Detalles del Proyecto</h3>
              <div className="info-grid">
                <div><strong>Referencia:</strong> {proyectoSeleccionado.referencia}</div>
                <div><strong>Nombre:</strong> {proyectoSeleccionado.nombre}</div>
                <div><strong>Cliente:</strong> {proyectoSeleccionado.nombre_cliente}</div>
                <div><strong>Tipo:</strong> {proyectoSeleccionado.tipoProyecto}</div>
                <div><strong> metros lineales:</strong> {proyectoSeleccionado.metrosLineales}</div>
                <div><strong>Paneles:</strong> {proyectoSeleccionado.totalPaneles}</div>
                <div><strong>RAL:</strong> {proyectoSeleccionado.ral}</div>
                <div><strong>Estado:</strong> {proyectoSeleccionado.disenyado ? '✓ Diseñado' : '⏳ Pendiente'}</div>
              </div>
            </div>
          )}

          {/* Barras del Proyecto */}
          {proyectoSeleccionado && (
            <div className="barras-section">
              {/* Resumen de Merma */}
              {resumenMochilificacion && (
                <div className="resumen-merma">
                  <h4>📐 Resumen de Mochilificación</h4>
                  <div className="resumen-grid">
                    <div className="resumen-item">
                      <span className="resumen-label">Total Barras</span>
                      <span className="resumen-value">{resumenMochilificacion.totalBarras}</span>
                    </div>
                    <div className="resumen-item">
                      <span className="resumen-label">Procesadas</span>
                      <span className="resumen-value">{resumenMochilificacion.barrasProcesadas}</span>
                    </div>
                    <div className="resumen-item">
                      <span className="resumen-label">Long. Total (mm)</span>
                      <span className="resumen-value">{resumenMochilificacion.longitudTotal}</span>
                    </div>
                    <div className="resumen-item">
                      <span className="resumen-label">Usada (mm)</span>
                      <span className="resumen-value">{resumenMochilificacion.longitudUsada}</span>
                    </div>
                    <div className="resumen-item">
                      <span className="resumen-label">Merma (mm)</span>
                      <span className="resumen-value warning">{resumenMochilificacion.mermaTotal}</span>
                    </div>
                    <div className="resumen-item">
                      <span className="resumen-label">Resto (mm)</span>
                      <span className="resumen-value success">{resumenMochilificacion.restoTotal}</span>
                    </div>
                    <div className="resumen-item resumen-highlight">
                      <span className="resumen-label">% Merma</span>
                      <span className="resumen-value">{resumenMochilificacion.porcentajeMerma}%</span>
                    </div>
                  </div>
                </div>
              )}

              <div className="barras-header">
                <h3>📊 Barras del Proyecto ({barras.length})</h3>
                <button 
                  className="btn-reload-barras"
                  onClick={() => cargarBarras(proyectoSeleccionado.referencia)}
                  disabled={loadingBarras}
                >
                  {loadingBarras ? '⏳' : '🔄'}
                </button>
              </div>
              
              {loadingBarras ? (
                <p className="loading-text">Cargando barras...</p>
              ) : barras.length === 0 ? (
                <p className="empty-text">No hay barras en este proyecto</p>
              ) : (
                <div className="barras-table-container">
                  <table className="barras-table">
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>Perfil</th>
                        <th>Tipo Sistema</th>
                        <th>Color</th>
                        <th>Longitud</th>
                        <th>Unidades</th>
                        <th>Mochilif.</th>
                        <th>Sección</th>
                        <th>Posición</th>
                      </tr>
                    </thead>
                    <tbody>
                      {barras.map((barra) => (
                        <tr key={barra.id} className={barra.mochilificada === 0 ? 'row-pending' : 'row-ready'}>
                          <td>{barra.id}</td>
                          <td>{barra.tipoPerfil}</td>
                          <td>{barra.tipoSistema}</td>
                          <td>{barra.color}</td>
                          <td>{barra.longitud?.toFixed(1)}</td>
                          <td>{barra.unidades}</td>
                          <td>
                            <span className={`badge ${barra.mochilificada === 0 ? 'badge-pending' : 'badge-ready'}`}>
                              {barra.mochilificada === 0 ? '⏳' : '✓'}
                            </span>
                          </td>
                          <td>{barra.perteneceASeccion || '-'}</td>
                          <td>{barra.posicion || '-'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}
        </section>

        {/* Acciones */}
        <section className="panel acciones-panel">
          <h2>⚡ Acciones</h2>
          <div className="acciones-grid">
            <button 
              className="btn btn-primary"
              onClick={() => handleMochilificar()}
              disabled={loading || !proyectoSeleccionado}
            >
              🎒 Mochilificar
            </button>
            
            <button 
              className="btn btn-secondary"
              onClick={() => handleMochilificar(1)}
              disabled={loading || !proyectoSeleccionado}
            >
              🎒 Mochilificar (Excell)
            </button>

            <button 
              className="btn btn-warning"
              onClick={handleLiberarBarras}
              disabled={loading || !proyectoSeleccionado}
            >
              🔓 Liberar Barras
            </button>

            <button 
              className="btn btn-info"
              onClick={handlePreparaBarras}
              disabled={loading || !proyectoSeleccionado}
            >
              🔄 Preparar Barras
            </button>

            <button 
              className="btn btn-danger"
              onClick={handleDeleteBarras}
              disabled={loading || !proyectoSeleccionado}
            >
              🗑️ Eliminar Barras
            </button>

            <button 
              className="btn btn-secondary"
              onClick={handleGeneraFicheroCorte}
              disabled={loading || !proyectoSeleccionado}
            >
              📄 Generar Fichero Corte
            </button>

            <button 
              className="btn btn-outline"
              onClick={handleAgregaRestos}
              disabled={loading || !proyectoSeleccionado}
            >
              ♻️ Agregar Restos
            </button>
          </div>
        </section>

        {/* Logs / Resultado API */}
        <section className="panel logs-panel">
          <h2>📋 Log de Operaciones</h2>
          <div className="logs-container">
            {logs.length === 0 ? (
              <p className="logs-empty">No hay operaciones aún...</p>
            ) : (
              logs.map(log => (
                <div key={log.id} className={`log-entry ${log.success ? 'success' : 'error'}`}>
                  <span className="log-time">
                    {log.timestamp.toLocaleTimeString()}
                  </span>
                  <span className="log-action">[{log.action}]</span>
                  <span className="log-result">{log.result}</span>
                </div>
              ))
            )}
          </div>
        </section>
      </main>
    </div>
  );
}

export default App;
