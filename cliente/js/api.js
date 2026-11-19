/**
 * Cliente independiente para la API REST de EmpleaSV.
 * Módulo de comunicación HTTP: maneja sesión (cookies), JSON,
 * respuestas exitosas y errores del servidor según contrato_api.md.
 */
const ApiCliente = (() => {
  let baseUrl = localStorage.getItem('empleasv.baseUrl') || 'http://localhost:8080/EmpleaSV/api';
  const listeners = [];

  const onLog = (fn) => listeners.push(fn);
  const log = (registro) => listeners.forEach((fn) => fn(registro));

  function setBaseUrl(url) {
    baseUrl = url.replace(/\/+$/, '');
    localStorage.setItem('empleasv.baseUrl', baseUrl);
  }

  function getBaseUrl() { return baseUrl; }

  class ApiError extends Error {
    constructor(status, codigo, mensaje, detalles) {
      super(mensaje);
      this.status = status;
      this.codigo = codigo || 'ERROR';
      this.detalles = detalles || null;
    }
  }

  /**
   * Ejecuta una petición HTTP contra la API.
   * @param {string} metodo GET|POST|PUT|PATCH|DELETE
   * @param {string} ruta  ruta relativa, ej. "/ofertas?titulo=java"
   * @param {object} op    { body: objeto JSON, formData: FormData, params: object }
   * @returns {Promise<{status:number, datos:any, mensaje:string}>}
   */
  async function solicitud(metodo, ruta, op = {}) {
    const t0 = performance.now();
    let url = baseUrl + ruta;

    if (op.params) {
      const q = new URLSearchParams();
      Object.entries(op.params).forEach(([k, v]) => {
        if (v !== null && v !== undefined && v !== '') q.append(k, v);
      });
      const qs = q.toString();
      if (qs) url += (url.includes('?') ? '&' : '?') + qs;
    }

    const headers = { 'Accept': 'application/json' };
    const init = { method: metodo, headers, credentials: 'include' };

    if (op.body !== undefined && op.body !== null) {
      headers['Content-Type'] = 'application/json; charset=UTF-8';
      init.body = JSON.stringify(op.body);
    }
    if (op.formData) init.body = op.formData;

    let res, json = null, texto = '';
    try {
      res = await fetch(url, init);
      texto = await res.text();
      if (texto) { try { json = JSON.parse(texto); } catch { json = null; } }
    } catch (e) {
      const err = new ApiError(0, 'SIN_CONEXION', 'No se pudo conectar con la API. ¿Está Tomcat corriendo en ' + baseUrl + '?');
      log({ metodo, ruta, status: 0, codigo: 'SIN_CONEXION', ms: Math.round(performance.now() - t0), mensaje: err.message });
      throw err;
    }

    const ms = Math.round(performance.now() - t0);

    if (!res.ok) {
      const error = (json && json.error) || {};
      const err = new ApiError(res.status, error.codigo || `HTTP_${res.status}`,
        error.mensaje || `Error HTTP ${res.status}`, error.detalles || null);
      log({ metodo, ruta, status: res.status, codigo: err.codigo, ms, mensaje: err.mensaje });
      throw err;
    }

    const resultado = {
      status: res.status,
      datos: json ? json.datos : null,
      mensaje: json ? json.mensaje : (res.status === 204 ? 'Sin contenido' : 'OK'),
      location: res.headers.get('Location'),
    };
    log({ metodo, ruta, status: res.status, codigo: 'OK', ms, mensaje: resultado.mensaje });
    return resultado;
  }

  return {
    solicitud, onLog, setBaseUrl, getBaseUrl, ApiError,
    get: (ruta, params) => solicitud('GET', ruta, { params }),
    post: (ruta, body) => solicitud('POST', ruta, { body }),
    put: (ruta, body) => solicitud('PUT', ruta, { body }),
    patch: (ruta, body) => solicitud('PATCH', ruta, { body }),
    del: (ruta) => solicitud('DELETE', ruta),
    postFormData: (ruta, formData) => solicitud('POST', ruta, { formData }),
  };
})();
