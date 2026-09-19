<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="header.jsp">
    <jsp:param name="tituloPagina" value="Ofertas de Empleo"/>
</jsp:include>

<div class="container section-sm">
    <div class="page-header">
        <h1>Ofertas de Empleo</h1>
        <a href="${pageContext.request.contextPath}/ofertas?format=jsp&accion=registrar" class="btn btn-primary">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
            Nueva oferta
        </a>
    </div>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success">${mensajeExito}</div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-error">${mensajeError}</div>
    </c:if>

    <!-- Search -->
    <div class="search-bar mb-3">
        <form action="${pageContext.request.contextPath}/ofertas" method="get" class="search-bar-inline">
            <input type="hidden" name="format" value="jsp"/>
            <input type="hidden" name="accion" value="buscar"/>
            <div class="form-group">
                <input type="text" name="titulo" class="form-control" value="${param.titulo}" placeholder="Buscar por titulo..."/>
            </div>
            <button type="submit" class="btn btn-primary">Buscar</button>
            <a href="${pageContext.request.contextPath}/ofertas?format=jsp&accion=filtrar" class="btn btn-secondary">Filtrar</a>
            <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-secondary">Ver todas</a>
        </form>
    </div>

    <c:choose>
        <c:when test="${empty ofertas}">
            <div class="empty-state">
                <div class="empty-state-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
                </div>
                <h3>No se encontraron ofertas</h3>
                <p>No hay ofertas de empleo disponibles en este momento.</p>
                <a href="${pageContext.request.contextPath}/ofertas?format=jsp&accion=registrar" class="btn btn-primary">Crear primera oferta</a>
            </div>
        </c:when>
        <c:otherwise>
            <p class="text-muted mb-2" style="font-size:0.9rem;">${ofertas.size()} oferta(s) encontrada(s)</p>
            <c:forEach var="o" items="${ofertas}">
                <div class="card mb-1">
                    <div class="card-header">
                        <div style="flex:1;">
                            <div class="card-title">
                                <a href="#" onclick="abrirDetalleOferta(${o.id}); return false;">${o.titulo}</a>
                            </div>
                            <div class="card-meta">
                                <span class="card-meta-item">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/></svg>
                                    ${o.ubicacion}
                                </span>
                                <span class="card-meta-item">${o.tipoContrato}</span>
                                <c:if test="${not empty o.salario}">
                                    <span class="card-meta-item">$<fmt:formatNumber value="${o.salario}" type="number" minFractionDigits="2"/></span>
                                </c:if>
                                <c:if test="${not empty o.estado}">
                                    <span class="badge
                                        <c:choose>
                                            <c:when test='${o.estado == "ACTIVA"}'>badge-success</c:when>
                                            <c:when test='${o.estado == "PAUSADA"}'>badge-warning</c:when>
                                            <c:otherwise>badge-danger</c:otherwise>
                                        </c:choose>
                                    ">${o.estado}</span>
                                </c:if>
                            </div>
                        </div>
                        <div class="card-actions" onclick="event.stopPropagation();">
                            <a href="${pageContext.request.contextPath}/ofertas?format=jsp&accion=editar&id=${o.id}" class="btn btn-secondary btn-sm">Editar</a>
                            <form action="${pageContext.request.contextPath}/ofertas" method="post" style="display:inline;" onsubmit="return confirm('Eliminar esta oferta?')">
                                <input type="hidden" name="accion" value="eliminar"/>
                                <input type="hidden" name="format" value="jsp"/>
                                <input type="hidden" name="id" value="${o.id}"/>
                                <button type="submit" class="btn btn-danger btn-sm">Eliminar</button>
                            </form>
                        </div>
                    </div>
                    <c:if test="${not empty o.descripcion}">
                        <div class="card-body">
                            <p class="card-text">${o.descripcion}</p>
                        </div>
                    </c:if>
                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</div>

<!-- Modal detalle de oferta -->
<div class="modal-overlay" id="modalDetalleOferta" onclick="cerrarModal(event)">
    <div class="modal" onclick="event.stopPropagation();">
        <div class="modal-header">
            <div class="modal-header-content">
                <h2 id="modalTitulo"></h2>
                <div class="card-meta">
                    <span class="card-meta-item" id="modalUbicacion"></span>
                    <span class="card-meta-item" id="modalTipoContrato"></span>
                    <span class="card-meta-item" id="modalHorario"></span>
                    <span id="modalEstadoBadge"></span>
                </div>
            </div>
            <button class="modal-close" onclick="cerrarModal()" aria-label="Cerrar">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
        </div>
        <div class="modal-body" id="modalBody">
            <div class="modal-loading">
                <div class="modal-loading-spinner"></div>
                <p>Cargando detalle...</p>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-primary btn-lg" disabled title="La funcion de postulacion estara disponible proximamente.">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
                Postularme
            </button>
        </div>
    </div>
</div>

<script>
    var contexto = '${pageContext.request.contextPath}';

    function abrirDetalleOferta(id) {
        var overlay = document.getElementById('modalDetalleOferta');
        var body = document.getElementById('modalBody');
        overlay.classList.add('active');
        document.body.style.overflow = 'hidden';
        body.innerHTML = '<div class="modal-loading"><div class="modal-loading-spinner"></div><p>Cargando detalle...</p></div>';

        fetch(contexto + '/ofertas?accion=detalle&id=' + id)
            .then(function(response) {
                if (!response.ok) throw new Error('Error al cargar');
                return response.json();
            })
            .then(function(data) {
                document.getElementById('modalTitulo').textContent = data.titulo || '';
                document.getElementById('modalUbicacion').innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/></svg> ' + (data.ubicacion || 'No especificada');
                document.getElementById('modalTipoContrato').textContent = data.tipoContrato || '';
                document.getElementById('modalHorario').textContent = data.horario || '';

                var estadoBadge = '';
                if (data.estado) {
                    var badgeClass = 'badge-success';
                    if (data.estado === 'PAUSADA') badgeClass = 'badge-warning';
                    else if (data.estado !== 'ACTIVA') badgeClass = 'badge-danger';
                    estadoBadge = '<span class="badge ' + badgeClass + '">' + data.estado + '</span>';
                }
                document.getElementById('modalEstadoBadge').innerHTML = estadoBadge;

                var html = '';
                html += '<div class="modal-detail-row"><span class="modal-detail-label">Empresa</span><span class="modal-detail-value">' + (data.empresaNombre || 'No especificada') + '</span></div>';
                html += '<div class="modal-detail-row"><span class="modal-detail-label">Ubicacion</span><span class="modal-detail-value">' + (data.ubicacion || 'No especificada') + '</span></div>';
                html += '<div class="modal-detail-row"><span class="modal-detail-label">Salario</span><span class="modal-detail-value">' + (data.salario != null ? '$' + Number(data.salario).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2}) : 'No especificado') + '</span></div>';
                html += '<div class="modal-detail-row"><span class="modal-detail-label">Tipo de contrato</span><span class="modal-detail-value">' + (data.tipoContrato || 'No especificado') + '</span></div>';
                html += '<div class="modal-detail-row"><span class="modal-detail-label">Horario</span><span class="modal-detail-value">' + (data.horario || 'No especificado') + '</span></div>';
                html += '<div class="modal-detail-row"><span class="modal-detail-label">Estado</span><span class="modal-detail-value">' + (data.estado || 'No especificado') + '</span></div>';
                html += '<div class="modal-detail-row"><span class="modal-detail-label">Fecha de publicacion</span><span class="modal-detail-value">' + (data.fechaPublicacion || 'No especificada') + '</span></div>';

                if (data.descripcion) {
                    html += '<div class="modal-section"><h3>Descripcion del puesto</h3><p>' + data.descripcion + '</p></div>';
                }
                if (data.requisitos) {
                    html += '<div class="modal-section"><h3>Requisitos</h3><p>' + data.requisitos + '</p></div>';
                }

                body.innerHTML = html;
            })
            .catch(function() {
                body.innerHTML = '<div class="modal-loading"><p>No se pudo cargar el detalle de la oferta.</p></div>';
            });
    }

    function cerrarModal(e) {
        if (e && e.target !== e.currentTarget) return;
        var overlay = document.getElementById('modalDetalleOferta');
        overlay.classList.remove('active');
        document.body.style.overflow = '';
    }

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            cerrarModal();
        }
    });
</script>

<%@ include file="footer.jsp" %>
