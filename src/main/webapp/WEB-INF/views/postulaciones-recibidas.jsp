<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<jsp:include page="header.jsp">
    <jsp:param name="tituloPagina" value="Postulaciones Recibidas"/>
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="container section-sm">
    <div class="page-header">
        <div>
            <h1>Postulaciones recibidas</h1>
            <p class="text-muted" style="margin-top:0.35rem;">
                Candidatos que se postularon a las ofertas de tu empresa.
            </p>
        </div>
        <a href="${ctx}/panel-empresa" class="btn btn-secondary">Volver al panel</a>
    </div>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success"><c:out value="${mensajeExito}"/></div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-error"><c:out value="${mensajeError}"/></div>
    </c:if>

    <div class="dashboard-stats mb-3">
        <div class="stat-card">
            <div class="stat-card-label">Total recibidas</div>
            <div class="stat-card-value">${totalPostulaciones}</div>
            <div class="stat-card-change">Postulaciones historicas</div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Pendientes</div>
            <div class="stat-card-value">${postulacionesPendientes}</div>
            <div class="stat-card-change">Aun sin revisar</div>
        </div>
    </div>

    <form action="${ctx}/postulaciones" method="get" class="card mb-3" style="padding:1rem;">
        <input type="hidden" name="accion" value="recibidas"/>
        <div class="fila-acciones" style="align-items:flex-end; flex-wrap:wrap;">
            <div class="form-group" style="flex:1 1 240px; margin:0;">
                <label class="form-label" for="buscarPostulacion">Buscar candidato u oferta</label>
                <input class="form-control" id="buscarPostulacion" name="buscar" type="search"
                       value="<c:out value='${busqueda}'/>"
                       placeholder="Nombre del candidato o título de la oferta"/>
            </div>
            <div class="form-group" style="flex:0 1 220px; margin:0;">
                <label class="form-label" for="filtroEstado">Estado</label>
                <select class="form-control" id="filtroEstado" name="estado">
                    <option value="">Todos los estados</option>
                    <c:forEach var="estado" items="${estados}">
                        <option value="${estado}" ${estadoSeleccionado == estado ? 'selected' : ''}>
                            <c:out value="${estado}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
            <button type="submit" class="btn btn-primary">Buscar</button>
            <a href="${ctx}/postulaciones?accion=recibidas" class="btn btn-ghost">Limpiar</a>
        </div>
    </form>

    <c:choose>
        <c:when test="${errorCarga}">
            <div class="empty-state">
                <h3>No se pudieron cargar las postulaciones</h3>
                <p>Revisa tu perfil empresarial o intenta nuevamente más tarde.</p>
            </div>
        </c:when>
        <c:when test="${empty postulaciones}">
            <div class="empty-state">
                <div class="empty-state-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 20h5v-1a4 4 0 00-4-4h-1M9 20H4v-1a4 4 0 014-4h1m6-4a4 4 0 11-8 0 4 4 0 018 0zm6 0a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                </div>
                <c:choose>
                    <c:when test="${not empty busqueda || not empty estadoSeleccionado}">
                        <h3>No hay resultados para estos filtros</h3>
                        <p>Prueba con otro nombre, oferta o estado.</p>
                    </c:when>
                    <c:otherwise>
                        <h3>Todavia no has recibido postulaciones</h3>
                        <p>Cuando un candidato se postule a una de tus ofertas, aparecera aqui con su CV.</p>
                        <a href="${ctx}/ofertas?format=jsp&mias=1" class="btn btn-primary">Ver mis ofertas</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:when>
        <c:otherwise>
            <c:forEach var="p" items="${postulaciones}">
                <c:set var="claseEstado"
                       value="${p.estado == 'ACEPTADA' ? 'badge-success' : (p.estado == 'RECHAZADA' ? 'badge-danger' : (p.estado == 'REVISADA' ? 'badge-info' : 'badge-warning'))}" />
                <div class="card mb-1">
                    <div class="card-header">
                        <div style="flex:1;">
                            <div class="card-title">
                                <a href="${ctx}/postulaciones?accion=detalle&id=${p.id}">
                                    <c:out value="${p.candidato.nombre}"/>
                                </a>
                                <span class="badge ${claseEstado}" style="margin-left:0.5rem;"><c:out value="${p.estado}"/></span>
                            </div>
                            <div class="card-meta">
                                <span class="card-meta-item"><c:out value="${p.candidato.email}"/></span>
                                <c:if test="${not empty perfilesCandidato[p.candidato.id].telefono}">
                                    <span class="card-meta-item"><c:out value="${perfilesCandidato[p.candidato.id].telefono}"/></span>
                                </c:if>
                                <span class="card-meta-item">
                                    Oferta: <c:out value="${p.oferta.titulo}"/>
                                </span>
                                <c:if test="${esAdministrador && not empty p.oferta.empresa}">
                                    <span class="card-meta-item"><c:out value="${p.oferta.empresa.nombre}"/></span>
                                </c:if>
                                <span class="card-meta-item">${fn:substring(p.fechaPostulacion, 0, 10)}</span>
                            </div>
                        </div>
                    </div>
                    <div class="card-body">
                        <c:if test="${not empty p.mensajePresentacion}">
                            <p class="card-text">
                                <strong>Mensaje:</strong>
                                <c:out value="${fn:substring(p.mensajePresentacion, 0, 220)}"/><c:if test="${fn:length(p.mensajePresentacion) > 220}">...</c:if>
                            </p>
                        </c:if>
                        <p class="card-text" style="font-size:0.85rem;">
                            <strong>CV:</strong>
                            <c:choose>
                                <c:when test="${not empty p.curriculum}">
                                    <span class="badge badge-success">Adjunto</span>
                                    <c:out value="${p.curriculum.nombreOriginal}"/>
                                </c:when>
                                <c:otherwise><span class="badge badge-warning">Sin CV</span></c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                    <div class="card-header" style="border-top:1px solid var(--gray-100); border-bottom:none;">
                        <div class="fila-acciones" style="width:100%; justify-content:flex-end; flex-wrap:wrap;">
                            <c:if test="${not empty p.curriculum}">
                                <a href="${ctx}/postulaciones?accion=cv&id=${p.id}&inline=1" target="_blank"
                                   rel="noopener" class="btn btn-outline btn-sm">Ver CV</a>
                                <a href="${ctx}/postulaciones?accion=cv&id=${p.id}" class="btn btn-ghost btn-sm">Descargar CV</a>
                            </c:if>
                            <a href="${ctx}/postulaciones?accion=detalle&id=${p.id}" class="btn btn-secondary btn-sm">Ver detalle</a>
                            <form action="${ctx}/postulaciones" method="post" class="fila-acciones" style="gap:0.35rem;">
                                <input type="hidden" name="accion" value="estado"/>
                                <input type="hidden" name="id" value="${p.id}"/>
                                <select name="estado" class="form-control" style="width:auto; padding:0.35rem 0.6rem; font-size:0.8rem;">
                                    <option value="PENDIENTE" ${p.estado == 'PENDIENTE' ? 'selected' : ''}>Pendiente</option>
                                    <option value="REVISADA" ${p.estado == 'REVISADA' ? 'selected' : ''}>Revisada</option>
                                    <option value="ACEPTADA" ${p.estado == 'ACEPTADA' ? 'selected' : ''}>Aceptada</option>
                                    <option value="RECHAZADA" ${p.estado == 'RECHAZADA' ? 'selected' : ''}>Rechazada</option>
                                </select>
                                <button type="submit" class="btn btn-primary btn-sm">Actualizar</button>
                            </form>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="footer.jsp" %>
