<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="header.jsp">
    <jsp:param name="tituloPagina" value="Detalle de Oferta"/>
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="container section-sm">
    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success"><c:out value="${mensajeExito}"/></div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-error"><c:out value="${mensajeError}"/></div>
    </c:if>
    <c:choose>
        <c:when test="${empty oferta}">
            <div class="empty-state">
                <div class="empty-state-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                </div>
                <h3>Oferta no encontrada</h3>
                <p>La oferta que buscas no existe o fue eliminada.</p>
                <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-primary">Volver al listado</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="page-header">
                <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-ghost btn-sm">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/></svg>
                    Volver
                </a>
            </div>

            <div class="detail-card">
                <div class="detail-card-header">
                    <div class="flex-between" style="flex-wrap:wrap; gap:0.75rem;">
                        <div>
                            <h1><c:out value="${oferta.titulo}"/></h1>
                            <div class="card-meta" style="margin-top:0.5rem;">
                                <span class="card-meta-item">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/></svg>
                                    <c:out value="${oferta.ubicacion}"/>
                                </span>
                                <span class="card-meta-item"><c:out value="${oferta.tipoContrato}"/></span>
                                <c:if test="${not empty oferta.horario}">
                                    <span class="card-meta-item"><c:out value="${oferta.horario}"/></span>
                                </c:if>
                            </div>
                        </div>
                        <c:if test="${not empty oferta.estado}">
                            <span class="badge ${oferta.estado == 'ACTIVA' ? 'badge-success' : (oferta.estado == 'PAUSADA' ? 'badge-warning' : 'badge-danger')}"
                                  style="font-size:0.8rem; padding:0.3rem 0.75rem;"><c:out value="${oferta.estado}"/></span>
                        </c:if>
                    </div>
                </div>

                <div class="detail-card-body">
                    <div class="detail-row">
                        <span class="detail-label">Salario</span>
                        <span class="detail-value">
                            <c:choose>
                                <c:when test="${not empty oferta.salario}">
                                    $<fmt:formatNumber value="${oferta.salario}" type="number" minFractionDigits="2"/>
                                </c:when>
                                <c:otherwise>No especificado</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Tipo de contrato</span>
                        <span class="detail-value"><c:out value="${oferta.tipoContrato}"/></span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Horario</span>
                        <span class="detail-value">${not empty oferta.horario ? oferta.horario : 'No especificado'}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Empresa</span>
                        <span class="detail-value">
                            <c:choose>
                                <c:when test="${not empty oferta.empresa}">
                                    <a href="${pageContext.request.contextPath}/empresas?accion=detalle&id=${oferta.empresaId}"><c:out value="${oferta.empresa.nombre}"/></a>
                                </c:when>
                                <c:otherwise>No especificada</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Fecha de publicacion</span>
                        <span class="detail-value">
                            <c:choose>
                                <c:when test="${not empty oferta.fechaPublicacion}">${oferta.fechaPublicacion}</c:when>
                                <c:otherwise>No especificada</c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <c:if test="${not empty oferta.descripcion}">
                        <div class="detail-section">
                            <h3>Descripcion del puesto</h3>
                            <p style="line-height:1.8; color:var(--gray-700);"><c:out value="${oferta.descripcion}"/></p>
                        </div>
                    </c:if>

                    <c:if test="${not empty oferta.requisitos}">
                        <div class="detail-section">
                            <h3>Requisitos</h3>
                            <p style="line-height:1.8; color:var(--gray-700);"><c:out value="${oferta.requisitos}"/></p>
                        </div>
                    </c:if>
                </div>

                <div class="detail-actions">
                    <c:choose>
                        <c:when test="${empty usuarioLogueado}">
                            <a href="${ctx}/auth?accion=login&restringido=1" class="btn btn-primary btn-lg">
                                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"/></svg>
                                Inicia sesion para postularte
                            </a>
                        </c:when>
                        <c:when test="${rolUsuario == 'USUARIO'}">
                            <c:choose>
                                <c:when test="${yaPostulado}">
                                    <span class="badge badge-success" style="padding:0.65rem 1rem; font-size:0.9rem;">
                                        Ya te postulaste a esta oferta
                                    </span>
                                </c:when>
                                <c:when test="${oferta.estado != 'ACTIVA'}">
                                    <button type="button" class="btn btn-secondary btn-lg" disabled>
                                        Oferta no disponible
                                    </button>
                                </c:when>
                                <c:otherwise>
                                    <button type="button" class="btn btn-primary btn-lg" onclick="abrirModalPostulacion()">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
                                        Postularme
                                    </button>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <button type="button" class="btn btn-secondary btn-lg" disabled>
                                Solo los candidatos pueden postularse
                            </button>
                        </c:otherwise>
                    </c:choose>
                    <c:set var="esPropia"
                           value="${rolUsuario == 'ADMINISTRADOR' || (rolUsuario == 'EMPRESA' && not empty empresaIdSesion && empresaIdSesion == oferta.empresaId)}" />
                    <c:if test="${esPropia}">
                        <a href="${pageContext.request.contextPath}/ofertas?format=jsp&accion=editar&id=${oferta.id}" class="btn btn-secondary">Editar</a>
                        <form action="${pageContext.request.contextPath}/ofertas" method="post" style="display:inline;" onsubmit="return confirm('Eliminar esta oferta?')">
                            <input type="hidden" name="accion" value="eliminar"/>
                            <input type="hidden" name="id" value="${oferta.id}"/>
                            <button type="submit" class="btn btn-danger">Eliminar</button>
                        </form>
                    </c:if>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<c:if test="${rolUsuario == 'USUARIO' && !yaPostulado && oferta.estado == 'ACTIVA'}">
    <div class="modal-overlay" id="modalPostulacion" onclick="cerrarModalPostulacion(event)">
        <div class="modal" onclick="event.stopPropagation();">
            <div class="modal-header">
                <div class="modal-header-content">
                    <h2>Postularme</h2>
                    <p class="text-muted" style="font-size:0.85rem; margin-top:0.25rem;">
                        <c:out value="${oferta.titulo}"/>
                        <c:if test="${not empty oferta.empresa}"> &middot; <c:out value="${oferta.empresa.nombre}"/></c:if>
                    </p>
                </div>
                <button type="button" class="modal-close" onclick="cerrarModalPostulacion()" aria-label="Cerrar">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
                </button>
            </div>
            <div class="modal-body">
                <form id="formPostulacion" action="${ctx}/postulaciones" method="post" enctype="multipart/form-data">
                    <input type="hidden" name="accion" value="crear"/>
                    <input type="hidden" name="oferta_id" value="${oferta.id}"/>

                    <c:choose>
                        <c:when test="${not empty curriculums}">
                            <div class="cv-opciones">
                                <label class="cv-opcion">
                                    <input type="radio" name="cv_modo" value="existente" checked
                                           onchange="alternarCv(this.value)"/>
                                    <span>Usar un CV de mi perfil</span>
                                </label>
                                <select name="curriculum_id" id="cvSelect" class="form-control" required>
                                    <c:forEach var="cv" items="${curriculums}">
                                        <option value="${cv.id}" ${cv.id == curriculumPrincipalId ? 'selected' : ''}>
                                            <c:out value="${cv.nombreOriginal}"/>
                                        </option>
                                    </c:forEach>
                                </select>

                                <label class="cv-opcion" style="margin-top:1rem;">
                                    <input type="radio" name="cv_modo" value="nuevo"
                                           onchange="alternarCv(this.value)"/>
                                    <span>Subir otro CV (solo PDF)</span>
                                </label>
                                <input type="file" name="curriculum" id="cvArchivo" class="file-input"
                                       accept=".pdf,application/pdf" disabled/>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="aviso aviso-info" style="margin-bottom:1rem;">
                                <div>
                                    <strong>No tienes un CV en tu perfil</strong>
                                    Adjunta tu curriculum en PDF para completar la postulacion.
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="form-label" for="cvArchivo">Curriculum (PDF)</label>
                                <input type="file" name="curriculum" id="cvArchivo" class="file-input"
                                       accept=".pdf,application/pdf" required/>
                                <p class="form-hint">Solo PDF, maximo 5 MB.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div class="form-group" style="margin-top:1rem;">
                        <label class="form-label" for="mensajePostulacion">Mensaje de presentacion (opcional)</label>
                        <textarea class="form-control" id="mensajePostulacion" name="mensaje" rows="4"
                                  maxlength="2000"
                                  placeholder="Cuentale a la empresa por que te interesa esta oportunidad."></textarea>
                        <p class="form-hint">Maximo 2000 caracteres.</p>
                    </div>

                    <div class="form-actions">
                        <button type="button" class="btn btn-secondary" onclick="cerrarModalPostulacion()">Cancelar</button>
                        <button type="submit" class="btn btn-primary">Confirmar postulacion</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script>
        function abrirModalPostulacion() {
            var overlay = document.getElementById('modalPostulacion');
            if (!overlay) { return; }
            overlay.classList.add('active');
            document.body.style.overflow = 'hidden';
        }

        function cerrarModalPostulacion(e) {
            if (e && e.target !== e.currentTarget) { return; }
            var overlay = document.getElementById('modalPostulacion');
            if (!overlay) { return; }
            overlay.classList.remove('active');
            document.body.style.overflow = '';
        }

        function alternarCv(origen) {
            var select = document.getElementById('cvSelect');
            var archivo = document.getElementById('cvArchivo');
            if (!select || !archivo) { return; }
            var existente = origen === 'existente';
            select.disabled = !existente;
            select.required = existente;
            archivo.disabled = existente;
            archivo.required = !existente;
            if (existente) {
                archivo.value = '';
            }
        }

        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') { cerrarModalPostulacion(); }
        });

        (function () {
            var form = document.getElementById('formPostulacion');
            if (!form) { return; }
            form.addEventListener('submit', function (e) {
                var select = document.getElementById('cvSelect');
                var archivo = document.getElementById('cvArchivo');
                var usaExistente = select && !select.disabled && select.value;
                var subeNuevo = archivo && !archivo.disabled && archivo.files.length > 0;
                if (!usaExistente && !subeNuevo) {
                    e.preventDefault();
                    alert('Selecciona un CV de tu perfil o adjunta un PDF.');
                }
            });
        })();

        <c:if test="${postularSolicitado}">
        abrirModalPostulacion();
        </c:if>
    </script>
</c:if>

<%@ include file="footer.jsp" %>
