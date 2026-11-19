<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="../header.jsp">
    <jsp:param name="tituloPagina" value="Gestion de ofertas"/>
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="container section-sm">
    <div class="page-header">
        <div>
            <h1>Ofertas</h1>
            <p class="text-muted" style="margin-top:0.35rem;">Busca, filtra y cambia el estado de las ofertas publicadas.</p>
        </div>
        <a class="btn btn-outline" href="${ctx}/ofertas?format=jsp">CRUD completo de ofertas</a>
    </div>

    <jsp:include page="tabs.jsp" />

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success"><c:out value="${mensajeExito}"/></div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-error"><c:out value="${mensajeError}"/></div>
    </c:if>

    <form class="search-bar-inline mt-3" method="get" action="${ctx}/admin">
        <input type="hidden" name="seccion" value="ofertas"/>
        <div class="form-group">
            <label class="form-label" for="q">Buscar</label>
            <input class="form-control" type="text" id="q" name="q"
                   value="${filtroQ}" placeholder="Titulo o empresa"/>
        </div>
        <div class="form-group">
            <label class="form-label" for="estado">Estado</label>
            <select class="form-control" id="estado" name="estado">
                <option value="">Todos</option>
                <option value="ACTIVA" ${filtroEstado == 'ACTIVA' ? 'selected' : ''}>Activa</option>
                <option value="PAUSADA" ${filtroEstado == 'PAUSADA' ? 'selected' : ''}>Pausada</option>
                <option value="CERRADA" ${filtroEstado == 'CERRADA' ? 'selected' : ''}>Cerrada</option>
            </select>
        </div>
        <div class="form-group" style="align-self:flex-end;">
            <button type="submit" class="btn btn-primary">Filtrar</button>
        </div>
    </form>

    <div class="table-container mt-3">
        <table class="table">
            <thead>
            <tr>
                <th>ID</th>
                <th>Titulo</th>
                <th>Empresa</th>
                <th>Ubicacion</th>
                <th>Publicada</th>
                <th>Estado</th>
                <th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="o" items="${ofertas}">
                <tr>
                    <td><c:out value="${o.id}"/></td>
                    <td>
                        <a href="${ctx}/admin?seccion=ofertas&detalle=${o.id}">
                            <c:out value="${o.titulo}"/>
                        </a>
                        <c:if test="${not empty empresasCuentaInactiva && empresasCuentaInactiva.contains(o.empresa.id)}">
                            <span class="badge badge-danger" title="La cuenta de esta empresa esta desactivada">Empresa desactivada</span>
                        </c:if>
                    </td>
                    <td><c:out value="${o.empresa.nombre}"/></td>
                    <td><c:out value="${o.ubicacion}"/></td>
                    <td><c:out value="${o.fechaPublicacion}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${o.estado == 'ACTIVA'}"><span class="badge badge-success">ACTIVA</span></c:when>
                            <c:when test="${o.estado == 'PAUSADA'}"><span class="badge badge-warning">PAUSADA</span></c:when>
                            <c:otherwise><span class="badge badge-danger">CERRADA</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="fila-acciones">
                        <a class="btn btn-outline btn-sm"
                           href="${ctx}/admin?seccion=ofertas&detalle=${o.id}">Ver</a>
                        <c:choose>
                            <c:when test="${o.estado != 'PAUSADA'}">
                                <form method="post" action="${ctx}/admin"
                                      onsubmit="return confirm('Pausar la oferta \"${o.titulo}\"? Dejara de mostrarse en busquedas.');">
                                    <input type="hidden" name="accion" value="cambiar-estado-oferta"/>
                                    <input type="hidden" name="seccion" value="ofertas"/>
                                    <input type="hidden" name="id" value="${o.id}"/>
                                    <input type="hidden" name="estado" value="PAUSADA"/>
                                    <button type="submit" class="btn btn-warning btn-sm">Pausar</button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <form method="post" action="${ctx}/admin"
                                      onsubmit="return confirm('Reactivar la oferta \"${o.titulo}\"?');">
                                    <input type="hidden" name="accion" value="cambiar-estado-oferta"/>
                                    <input type="hidden" name="seccion" value="ofertas"/>
                                    <input type="hidden" name="id" value="${o.id}"/>
                                    <input type="hidden" name="estado" value="ACTIVA"/>
                                    <button type="submit" class="btn btn-success btn-sm">Activar</button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty ofertas}">
                <tr><td colspan="7" class="text-center text-muted">No se encontraron ofertas con esos filtros.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="../footer.jsp" />
