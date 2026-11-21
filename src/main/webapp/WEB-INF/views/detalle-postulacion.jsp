<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<jsp:include page="header.jsp">
    <jsp:param name="tituloPagina" value="Detalle de Postulacion"/>
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="claseEstado"
       value="${postulacion.estado == 'ACEPTADA' ? 'badge-success' : (postulacion.estado == 'RECHAZADA' ? 'badge-danger' : (postulacion.estado == 'REVISADA' ? 'badge-info' : 'badge-warning'))}" />

<div class="container section-sm">
    <div class="page-header">
        <a href="${ctx}/postulaciones" class="btn btn-ghost btn-sm">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/></svg>
            Volver
        </a>
    </div>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success"><c:out value="${mensajeExito}"/></div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-error"><c:out value="${mensajeError}"/></div>
    </c:if>

    <div class="detail-card">
        <div class="detail-card-header">
            <div class="flex-between" style="flex-wrap:wrap; gap:0.75rem;">
                <div>
                    <h1><c:out value="${postulacion.candidato.nombre}"/></h1>
                    <div class="card-meta" style="margin-top:0.5rem;">
                        <span class="card-meta-item"><c:out value="${postulacion.candidato.email}"/></span>
                        <c:if test="${not empty perfilCandidato.telefono}">
                            <span class="card-meta-item"><c:out value="${perfilCandidato.telefono}"/></span>
                        </c:if>
                        <span class="card-meta-item">
                            Postulacion del ${fn:substring(postulacion.fechaPostulacion, 0, 10)}
                        </span>
                    </div>
                </div>
                <span class="badge ${claseEstado}" style="font-size:0.8rem; padding:0.3rem 0.75rem;">
                    <c:out value="${postulacion.estado}"/>
                </span>
            </div>
        </div>

        <div class="detail-card-body">
            <div class="detail-row">
                <span class="detail-label">Oferta</span>
                <span class="detail-value">
                    <a href="${ctx}/ofertas?format=jsp&accion=detalle&id=${postulacion.oferta.id}">
                        <c:out value="${postulacion.oferta.titulo}"/>
                    </a>
                </span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Empresa</span>
                <span class="detail-value"><c:out value="${postulacion.oferta.empresa.nombre}"/></span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Ubicacion</span>
                <span class="detail-value"><c:out value="${postulacion.oferta.ubicacion}"/></span>
            </div>
            <c:if test="${not empty perfilCandidato}">
                <div class="detail-row">
                    <span class="detail-label">Residencia</span>
                    <span class="detail-value">
                        <c:out value="${perfilCandidato.municipio}"/>
                        <c:if test="${not empty perfilCandidato.departamento}">
                            , <c:out value="${perfilCandidato.departamento}"/>
                        </c:if>
                    </span>
                </div>
                <c:if test="${not empty perfilCandidato.direccionResidencia}">
                    <div class="detail-row">
                        <span class="detail-label">Direccion</span>
                        <span class="detail-value"><c:out value="${perfilCandidato.direccionResidencia}"/></span>
                    </div>
                </c:if>
            </c:if>

            <div class="detail-section">
                <h3>Curriculum enviado</h3>
                <c:choose>
                    <c:when test="${not empty postulacion.curriculum}">
                        <p class="card-text"><c:out value="${postulacion.curriculum.nombreOriginal}"/></p>
                        <div class="fila-acciones">
                            <a href="${ctx}/postulaciones?accion=cv&id=${postulacion.id}&inline=1" target="_blank"
                               rel="noopener" class="btn btn-outline btn-sm">Abrir CV</a>
                            <a href="${ctx}/postulaciones?accion=cv&id=${postulacion.id}" class="btn btn-secondary btn-sm">Descargar CV</a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <p class="text-muted">Esta postulacion no tiene un curriculum asociado.</p>
                    </c:otherwise>
                </c:choose>
            </div>

            <c:if test="${not empty postulacion.mensajePresentacion}">
                <div class="detail-section">
                    <h3>Mensaje de presentacion</h3>
                    <p style="line-height:1.8; color:var(--gray-700); white-space:pre-line;"><c:out value="${postulacion.mensajePresentacion}"/></p>
                </div>
            </c:if>

            <c:if test="${puedeCambiarEstado}">
                <div class="detail-section">
                    <h3>Estado de la postulacion</h3>
                    <form action="${ctx}/postulaciones" method="post" class="fila-acciones" style="align-items:flex-end;">
                        <input type="hidden" name="accion" value="estado"/>
                        <input type="hidden" name="id" value="${postulacion.id}"/>
                        <div class="form-group" style="margin-bottom:0; min-width:180px;">
                            <label class="form-label" for="estadoPostulacion">Nuevo estado</label>
                            <select class="form-control" id="estadoPostulacion" name="estado">
                                <c:forEach var="estado" items="${estados}">
                                    <option value="${estado}" ${postulacion.estado == estado ? 'selected' : ''}>
                                        <c:out value="${estado}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <button type="submit" class="btn btn-primary">Guardar estado</button>
                    </form>
                </div>
            </c:if>
        </div>
    </div>
</div>

<%@ include file="footer.jsp" %>
