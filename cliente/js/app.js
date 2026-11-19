/**
 * Lógica de interfaz del cliente EmpleaSV.
 * Consume el módulo ApiCliente (js/api.js).
 */
(() => {
  const $ = (sel) => document.querySelector(sel);

  // ---------- Utilidades de UI ----------
  function toast(mensaje, tipo = '') {
    const cont = $('#toasts');
    const el = document.createElement('div');
    el.className = `toast ${tipo}`;
    el.textContent = mensaje;
    cont.appendChild(el);
    setTimeout(() => el.remove(), 4500);
  }

  function mostrarEstado(sel, texto, exito) {
    const el = $(sel);
    el.textContent = texto;
    el.classList.add('visible');
    el.classList.toggle('exito', !!exito);
    el.classList.toggle('error', !exito);
  }

  function fmtSalario(s) {
    return (s === null || s === undefined) ? '—'
      : '$' + Number(s).toLocaleString('es-SV', { minimumFractionDigits: 2 });
  }

  function escapar(t) {
    return String(t ?? '').replace(/[&<>"]/g, c =>
      ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));
  }

  // ---------- Consola de peticiones ----------
  ApiCliente.onLog((r) => {
    const log = $('#logPeticiones');
    const fila = document.createElement('div');
    fila.className = 'fila';
    const clase = r.status >= 200 && r.status < 400 ? 'ok' : 'mal';
    fila.innerHTML = `<span>${escapar(r.metodo)}</span>
      <span class="${clase}">${r.status || 'ERR'}</span>
      <span>${escapar(r.ruta)}</span>
      <span>${r.ms} ms</span>
      <span class="${clase}">${escapar(r.codigo)}</span>
      <span>${escapar(r.mensaje || '')}</span>`;
    log.prepend(fila);
  });

  // ---------- Pestañas ----------
  document.querySelectorAll('.pestana').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.pestana').forEach(b => b.classList.remove('activa'));
      document.querySelectorAll('.vista').forEach(v => v.classList.remove('activa'));
      btn.classList.add('activa');
      $('#vista-' + btn.dataset.vista).classList.add('activa');
    });
  });

  // ---------- Configuración de URL ----------
  $('#baseUrl').value = ApiCliente.getBaseUrl();
  $('#btnGuardarUrl').addEventListener('click', () => {
    ApiCliente.setBaseUrl($('#baseUrl').value.trim());
    toast('Base URL guardada', 'exito');
  });
  $('#btnProbarApi').addEventListener('click', async () => {
    try {
      const r = await ApiCliente.get('/ofertas?titulo=');
      toast(`Conexión OK — ${r.datos.length} ofertas disponibles`, 'exito');
    } catch (e) { toast(e.message, 'error'); }
  });

  // ---------- Sesión ----------
  let sesion = null;

  function pintarSesion() {
    const badge = $('#sesionBadge');
    if (sesion) {
      badge.textContent = `${sesion.nombre} · ${sesion.rol}`;
      badge.classList.add('activo');
      $('#btnLogout').hidden = false;
      $('#estadoSesion').className = 'estado visible exito';
      $('#estadoSesion').textContent = JSON.stringify(sesion, null, 2);
    } else {
      badge.textContent = 'Sin sesión';
      badge.classList.remove('activo');
      $('#btnLogout').hidden = true;
      $('#estadoSesion').className = 'estado';
      $('#estadoSesion').textContent = '';
    }
  }

  async function refrescarSesion() {
    try {
      const r = await ApiCliente.get('/auth/sesion');
      sesion = r.datos.usuario;
    } catch { sesion = null; }
    pintarSesion();
    return sesion;
  }

  $('#formLogin').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    try {
      const r = await ApiCliente.post('/auth/login', {
        email: $('#loginEmail').value.trim(),
        password: $('#loginPassword').value,
      });
      sesion = r.datos.usuario;
      pintarSesion();
      toast(`Bienvenido/a, ${sesion.nombre}`, 'exito');
    } catch (e) {
      const det = e.detalles ? '\n' + e.detalles.map(d => `${d.campo}: ${d.mensaje}`).join('\n') : '';
      toast(`${e.codigo}: ${e.mensaje}${det}`, 'error');
    }
  });

  $('#btnLogout').addEventListener('click', async () => {
    try { await ApiCliente.post('/auth/logout'); } catch { /* 204 o error */ }
    sesion = null;
    pintarSesion();
    toast('Sesión cerrada', 'exito');
  });

  $('#formRegistro').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    const rol = $('#regRol').value;
    const datos = {
      rol,
      nombre: $('#regNombre').value.trim(),
      email: $('#regEmail').value.trim(),
      password: $('#regPassword').value,
      password2: $('#regPassword').value,
    };
    if (rol === 'USUARIO') {
      Object.assign(datos, { telefono: '', direccion: '', municipio: 'San Salvador', departamento: 'San Salvador' });
    } else {
      Object.assign(datos, {
        nombreResponsable: datos.nombre, nombreEmpresa: datos.nombre,
        telefonoEmpresa: '', direccionEmpresa: '', municipio: 'San Salvador',
        departamento: 'San Salvador', sector: 'General',
      });
    }
    try {
      const r = await ApiCliente.post('/usuarios', datos);
      mostrarEstado('#respuestaRegistro',
        `201 Creado — usuario id=${r.datos.usuario.id}\nLocation: ${r.location || '(ver consola)'}`, true);
      toast('Cuenta registrada', 'exito');
    } catch (e) {
      const det = e.detalles ? '\n' + e.detalles.map(d => `${d.campo}: ${d.mensaje}`).join('\n') : '';
      mostrarEstado('#respuestaRegistro', `${e.status} ${e.codigo}: ${e.mensaje}${det}`, false);
    }
  });

  // ---------- Ofertas (consulta pública) ----------
  function tarjetaOferta(o) {
    const cls = o.estado === 'ACTIVA' ? '' : o.estado.toLowerCase();
    return `<article class="tarjeta" data-id="${o.id}">
      <h3>${escapar(o.titulo)}</h3>
      <div class="meta">
        <span>${escapar(o.empresaNombre || '—')}</span>
        <span>${escapar(o.ubicacion)} · ${escapar(o.tipoContrato)}</span>
        <span>${fmtSalario(o.salario)}</span>
      </div>
      <span class="badge ${cls}">${escapar(o.estado)}</span>
    </article>`;
  }

  async function cargarOfertas(params) {
    const cont = $('#listaOfertas');
    cont.innerHTML = '<p>Cargando…</p>';
    try {
      const r = await ApiCliente.get('/ofertas', params);
      cont.innerHTML = r.datos.length
        ? r.datos.map(tarjetaOferta).join('')
        : '<p>No se encontraron ofertas con esos filtros.</p>';
      cont.querySelectorAll('.tarjeta').forEach(t =>
        t.addEventListener('click', () => verDetalleOferta(t.dataset.id)));
    } catch (e) {
      cont.innerHTML = `<p class="mal">Error ${e.status} — ${escapar(e.codigo)}: ${escapar(e.mensaje)}</p>`;
    }
  }

  async function verDetalleOferta(id) {
    const panel = $('#panelDetalleOferta');
    panel.hidden = false;
    $('#detalleOferta').innerHTML = '<p>Cargando…</p>';
    try {
      const r = await ApiCliente.get('/ofertas/' + id);
      const o = r.datos;
      $('#detalleOferta').innerHTML = `<dl class="detalle">
        <dt>ID / Estado</dt><dd>${o.id} · ${escapar(o.estado)}</dd>
        <dt>Título</dt><dd>${escapar(o.titulo)}</dd>
        <dt>Empresa</dt><dd>${escapar(o.empresaNombre)} (id ${o.empresaId})</dd>
        <dt>Descripción</dt><dd>${escapar(o.descripcion)}</dd>
        <dt>Requisitos</dt><dd>${escapar(o.requisitos || '—')}</dd>
        <dt>Ubicación</dt><dd>${escapar(o.ubicacion)}</dd>
        <dt>Salario</dt><dd>${fmtSalario(o.salario)}</dd>
        <dt>Contrato / Horario</dt><dd>${escapar(o.tipoContrato)} · ${escapar(o.horario || '—')}</dd>
        <dt>Publicada</dt><dd>${escapar(o.fechaPublicacion)}</dd>
      </dl>`;
      panel.scrollIntoView({ behavior: 'smooth' });
    } catch (e) {
      $('#detalleOferta').innerHTML = `<p class="mal">Error ${e.status} — ${escapar(e.codigo)}: ${escapar(e.mensaje)}</p>`;
    }
  }

  $('#formFiltros').addEventListener('submit', (ev) => {
    ev.preventDefault();
    cargarOfertas({
      titulo: $('#fTitulo').value.trim(),
      ubicacion: $('#fUbicacion').value.trim(),
      empresaId: $('#fEmpresaId').value || null,
    });
  });
  $('#btnTodas').addEventListener('click', () => {
    $('#fTitulo').value = ''; $('#fUbicacion').value = ''; $('#fEmpresaId').value = '';
    cargarOfertas({});
  });

  // ---------- Empresas ----------
  async function cargarEmpresas() {
    const cont = $('#listaEmpresas');
    cont.innerHTML = '<p>Cargando…</p>';
    try {
      const params = $('#fDestacadas').checked ? { destacadas: 'true' } : {};
      const r = await ApiCliente.get('/empresas', params);
      cont.innerHTML = r.datos.map(e => `<article class="tarjeta" data-id="${e.id}">
        <h3>${escapar(e.nombre)}</h3>
        <div class="meta">
          <span>${escapar(e.categoria || '—')}</span>
          <span>${escapar(e.email)}</span>
          <span>${e.cantidadOfertas != null ? e.cantidadOfertas + ' ofertas' : ''}</span>
        </div>
        <span class="badge">Ver ofertas →</span>
      </article>`).join('') || '<p>Sin empresas.</p>';
      cont.querySelectorAll('.tarjeta').forEach(t =>
        t.addEventListener('click', () => verDetalleEmpresa(t.dataset.id)));
    } catch (e) {
      cont.innerHTML = `<p class="mal">Error ${e.status} — ${escapar(e.codigo)}: ${escapar(e.mensaje)}</p>`;
    }
  }

  async function verDetalleEmpresa(id) {
    const panel = $('#panelDetalleEmpresa');
    panel.hidden = false;
    $('#detalleEmpresa').innerHTML = '<p>Cargando…</p>';
    try {
      const [emp, ofs] = await Promise.all([
        ApiCliente.get('/empresas/' + id),
        ApiCliente.get('/empresas/' + id + '/ofertas'),
      ]);
      const e = emp.datos;
      $('#detalleEmpresa').innerHTML = `<dl class="detalle">
        <dt>Nombre</dt><dd>${escapar(e.nombre)} (id ${e.id})</dd>
        <dt>Categoría</dt><dd>${escapar(e.categoria || '—')}</dd>
        <dt>Contacto</dt><dd>${escapar(e.email)} · ${escapar(e.telefono || '—')}</dd>
        <dt>Descripción</dt><dd>${escapar(e.descripcion || '—')}</dd>
      </dl>
      <h3>Ofertas ACTIVAS (${ofs.datos.length})</h3>
      <div class="rejilla">${ofs.datos.map(tarjetaOferta).join('') || '<p>Sin ofertas activas.</p>'}</div>`;
    } catch (e2) {
      $('#detalleEmpresa').innerHTML = `<p class="mal">Error ${e2.status} — ${escapar(e2.codigo)}: ${escapar(e2.mensaje)}</p>`;
    }
  }
  $('#btnEmpresas').addEventListener('click', cargarEmpresas);

  // ---------- Operaciones CRUD ----------
  async function cargarMisOfertas() {
    const cont = $('#listaMisOfertas');
    cont.innerHTML = '<p>Cargando…</p>';
    try {
      const r = await ApiCliente.get('/ofertas', { mias: 'true' });
      cont.innerHTML = r.datos.map(o => `<article class="tarjeta" data-id="${o.id}">
        <h3>#${o.id} ${escapar(o.titulo)}</h3>
        <div class="meta"><span>${escapar(o.ubicacion)}</span><span>${fmtSalario(o.salario)}</span></div>
        <span class="badge ${o.estado.toLowerCase()}">${escapar(o.estado)}</span>
      </article>`).join('') || '<p>Tu empresa aún no tiene ofertas.</p>';
      cont.querySelectorAll('.tarjeta').forEach(t =>
        t.addEventListener('click', () => { $('#gId').value = t.dataset.id; cargarOfertaGestion(); }));
    } catch (e) {
      cont.innerHTML = `<p class="mal">Error ${e.status} — ${escapar(e.codigo)}: ${escapar(e.mensaje)}</p>`;
    }
  }
  $('#btnMisOfertas').addEventListener('click', cargarMisOfertas);

  $('#formOferta').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    const body = {
      titulo: $('#oTitulo').value.trim(),
      descripcion: $('#oDescripcion').value.trim(),
      requisitos: $('#oRequisitos').value.trim() || null,
      ubicacion: $('#oUbicacion').value.trim(),
      salario: $('#oSalario').value ? Number($('#oSalario').value) : null,
      tipoContrato: $('#oTipoContrato').value,
      horario: $('#oHorario').value.trim() || null,
      estado: 'ACTIVA',
    };
    try {
      const r = await ApiCliente.post('/ofertas', body);
      mostrarEstado('#respuestaOferta',
        `201 Creada — id=${r.datos.id}\nLocation: ${r.location || ''}\n${JSON.stringify(r.datos, null, 2)}`, true);
      toast('Oferta creada', 'exito');
      ev.target.reset();
    } catch (e) {
      mostrarEstado('#respuestaOferta', fmtError(e), false);
    }
  });

  function fmtError(e) {
    let t = `${e.status} ${e.codigo}: ${e.mensaje}`;
    if (e.detalles) t += '\n' + e.detalles.map(d => `  · ${d.campo}: ${d.mensaje}`).join('\n');
    return t;
  }

  async function cargarOfertaGestion() {
    try {
      const r = await ApiCliente.get('/ofertas/' + $('#gId').value);
      const o = r.datos;
      $('#gTitulo').value = o.titulo;
      $('#gDescripcion').value = o.descripcion;
      $('#gUbicacion').value = o.ubicacion;
      $('#gSalario').value = o.salario ?? '';
      $('#gTipoContrato').value = o.tipoContrato;
      mostrarEstado('#respuestaGestion', `GET 200 — oferta #${o.id} (${o.estado}) cargada`, true);
    } catch (e) {
      mostrarEstado('#respuestaGestion', fmtError(e), false);
    }
  }
  $('#btnCargarOferta').addEventListener('click', cargarOfertaGestion);

  $('#formGestion').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    try {
      const r = await ApiCliente.put('/ofertas/' + $('#gId').value, {
        titulo: $('#gTitulo').value.trim(),
        descripcion: $('#gDescripcion').value.trim(),
        ubicacion: $('#gUbicacion').value.trim(),
        salario: $('#gSalario').value ? Number($('#gSalario').value) : null,
        tipoContrato: $('#gTipoContrato').value.trim(),
      });
      mostrarEstado('#respuestaGestion', `PUT 200 — actualizada\n${JSON.stringify(r.datos, null, 2)}`, true);
    } catch (e) { mostrarEstado('#respuestaGestion', fmtError(e), false); }
  });

  async function cambiarEstado(estado) {
    try {
      const r = await ApiCliente.patch(`/ofertas/${$('#gId').value}/estado`, { estado });
      mostrarEstado('#respuestaGestion', `PATCH 200 — estado ahora: ${r.datos.estado}`, true);
    } catch (e) { mostrarEstado('#respuestaGestion', fmtError(e), false); }
  }
  $('#btnPausar').addEventListener('click', () => cambiarEstado('PAUSADA'));
  $('#btnActivar').addEventListener('click', () => cambiarEstado('ACTIVA'));
  $('#btnCerrar').addEventListener('click', () => cambiarEstado('CERRADA'));

  $('#btnEliminar').addEventListener('click', async () => {
    if (!confirm('¿Eliminar la oferta #' + $('#gId').value + '?')) return;
    try {
      await ApiCliente.del('/ofertas/' + $('#gId').value);
      mostrarEstado('#respuestaGestion', 'DELETE 204 — oferta eliminada', true);
    } catch (e) { mostrarEstado('#respuestaGestion', fmtError(e), false); }
  });

  // ---------- Perfiles y administración ----------

  $('#btnPerfilCargar').addEventListener('click', async () => {
    try {
      const r = await ApiCliente.get('/perfiles/candidato');
      const p = r.datos;
      $('#pcNombre').value = p.usuario?.nombre ?? '';
      $('#pcEmail').value = p.usuario?.email ?? '';
      $('#pcTelefono').value = p.telefono ?? '';
      $('#pcDireccion').value = p.direccionResidencia ?? '';
      $('#pcMunicipio').value = p.municipio ?? '';
      $('#pcDepartamento').value = p.departamento ?? '';
      const cv = p.curriculumPrincipal;
      mostrarEstado('#estadoPerfilC',
        `GET 200 — perfil #${p.id}` + (cv ? `\nCV principal: ${cv.nombreOriginal} (${cv.urlDescarga})` : '\nSin CV principal'), true);
    } catch (e) { mostrarEstado('#estadoPerfilC', fmtError(e), false); }
  });

  $('#formPerfilC').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    try {
      const r = await ApiCliente.put('/perfiles/candidato', {
        nombre: $('#pcNombre').value.trim(),
        email: $('#pcEmail').value.trim(),
        telefono: $('#pcTelefono').value.trim(),
        direccion: $('#pcDireccion').value.trim(),
        municipio: $('#pcMunicipio').value.trim(),
        departamento: $('#pcDepartamento').value.trim(),
      });
      mostrarEstado('#estadoPerfilC', `PUT 200 — perfil actualizado\n${JSON.stringify(r.datos, null, 2)}`, true);
    } catch (e) { mostrarEstado('#estadoPerfilC', fmtError(e), false); }
  });

  $('#btnCvListar').addEventListener('click', async () => {
    try {
      const r = await ApiCliente.get('/perfiles/candidato/curriculums');
      mostrarEstado('#estadoCv', r.datos.length
        ? `GET 200 — ${r.datos.length} currículum(es):\n` +
          r.datos.map(a => `  · #${a.id} ${a.nombreOriginal} (${a.tamanoBytes} bytes) → ${a.urlDescarga}`).join('\n')
        : 'GET 200 — sin currículums', true);
    } catch (e) { mostrarEstado('#estadoCv', fmtError(e), false); }
  });

  $('#btnCvSubir').addEventListener('click', async () => {
    const archivo = $('#cvArchivo').files[0];
    if (!archivo) { mostrarEstado('#estadoCv', 'Selecciona un archivo PDF primero.', false); return; }
    const fd = new FormData();
    fd.append('curriculum', archivo);
    fd.append('principal', $('#cvPrincipal').checked ? 'true' : 'false');
    try {
      const r = await ApiCliente.postFormData('/perfiles/candidato/curriculums', fd);
      mostrarEstado('#estadoCv', `201 subido — id=${r.datos.id}\n${JSON.stringify(r.datos, null, 2)}`, true);
      toast('Currículum subido', 'exito');
    } catch (e) { mostrarEstado('#estadoCv', fmtError(e), false); }
  });

  $('#btnPerfilECargar').addEventListener('click', async () => {
    try {
      const r = await ApiCliente.get('/perfiles/empresa');
      const p = r.datos;
      $('#peResponsable').value = p.nombreResponsable ?? '';
      $('#peEmail').value = p.usuario?.email ?? '';
      $('#peNombreEmpresa').value = p.nombreEmpresa ?? '';
      $('#peNit').value = p.nit ?? '';
      $('#peTelefono').value = p.telefonoEmpresa ?? '';
      $('#peDireccion').value = p.direccionEmpresa ?? '';
      $('#peMunicipio').value = p.municipio ?? '';
      $('#peDepartamento').value = p.departamento ?? '';
      $('#peSector').value = p.sector ?? '';
      $('#peSitioWeb').value = p.sitioWeb ?? '';
      $('#peDescripcion').value = p.descripcion ?? '';
      mostrarEstado('#estadoPerfilE', `GET 200 — perfil #${p.id} · empresa vinculada: ${p.empresaId ?? 'ninguna'}`, true);
    } catch (e) { mostrarEstado('#estadoPerfilE', fmtError(e), false); }
  });

  $('#formPerfilE').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    try {
      const r = await ApiCliente.put('/perfiles/empresa', {
        nombreResponsable: $('#peResponsable').value.trim(),
        email: $('#peEmail').value.trim(),
        nombreEmpresa: $('#peNombreEmpresa').value.trim(),
        nit: $('#peNit').value.trim() || null,
        telefonoEmpresa: $('#peTelefono').value.trim(),
        direccionEmpresa: $('#peDireccion').value.trim(),
        municipio: $('#peMunicipio').value.trim(),
        departamento: $('#peDepartamento').value.trim(),
        sector: $('#peSector').value.trim(),
        sitioWeb: $('#peSitioWeb').value.trim() || null,
        descripcion: $('#peDescripcion').value.trim() || null,
      });
      mostrarEstado('#estadoPerfilE', `PUT 200 — perfil actualizado\n${JSON.stringify(r.datos, null, 2)}`, true);
    } catch (e) { mostrarEstado('#estadoPerfilE', fmtError(e), false); }
  });

  $('#btnDashboard').addEventListener('click', async () => {
    try {
      const r = await ApiCliente.get('/admin/dashboard');
      mostrarEstado('#estadoAdmin', `GET 200 — dashboard\n${JSON.stringify(r.datos, null, 2)}`, true);
    } catch (e) { mostrarEstado('#estadoAdmin', fmtError(e), false); }
  });

  $('#btnAuditoria').addEventListener('click', async () => {
    try {
      const r = await ApiCliente.get('/admin/auditoria', { limite: $('#audLimite').value || 10 });
      mostrarEstado('#estadoAdmin', r.datos.length
        ? `GET 200 — ${r.datos.length} registro(s) de auditoría:\n${JSON.stringify(r.datos, null, 2)}`
        : 'GET 200 — sin registros de auditoría', true);
    } catch (e) { mostrarEstado('#estadoAdmin', fmtError(e), false); }
  });

  // ---------- Inicio ----------
  refrescarSesion().then(() => cargarOfertas({}));
})();
