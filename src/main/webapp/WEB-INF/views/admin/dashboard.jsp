<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="../header.jsp">
    <jsp:param name="tituloPagina" value="Dashboard administrativo"/>
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="container section-sm">
    <div class="page-header">
        <div>
            <h1>Panel administrativo</h1>
            <p class="text-muted" style="margin-top:0.35rem;">Estado general de la plataforma EmpleaSV.</p>
        </div>
    </div>

    <jsp:include page="tabs.jsp" />

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success"><c:out value="${mensajeExito}"/></div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-error"><c:out value="${mensajeError}"/></div>
    </c:if>

    <div class="dashboard-stats mt-3">
        <div class="stat-card">
            <div class="stat-card-label">Cuentas candidato</div>
            <div class="stat-card-value"><c:out value="${candidatosActivos}"/></div>
            <div class="stat-card-change">activas de <c:out value="${totalUsuarios}"/> usuarios</div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Empresas</div>
            <div class="stat-card-value"><c:out value="${totalEmpresas}"/></div>
            <div class="stat-card-change"><c:out value="${empresasActivas}"/> cuentas activas</div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Ofertas activas</div>
            <div class="stat-card-value"><c:out value="${ofertasActivas}"/></div>
            <div class="stat-card-change">de <c:out value="${totalOfertas}"/> publicadas</div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Administradores</div>
            <div class="stat-card-value"><c:out value="${administradoresActivos}"/></div>
            <div class="stat-card-change">cuentas activas</div>
        </div>
    </div>

    <div class="dashboard-stats" style="grid-template-columns: repeat(3, 1fr);">
        <div class="stat-card">
            <div class="stat-card-label">Ofertas pausadas</div>
            <div class="stat-card-value"><c:out value="${ofertasPausadas}"/></div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Ofertas cerradas</div>
            <div class="stat-card-value"><c:out value="${ofertasCerradas}"/></div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Accesos rapidos</div>
            <div class="stat-card-change" style="margin-top:0.6rem;">
                <a href="${ctx}/admin?seccion=usuarios">Gestionar usuarios</a> &middot;
                <a href="${ctx}/admin?seccion=ofertas">Revisar ofertas</a>
            </div>
        </div>
    </div>

    <div class="admin-grid-2">
        <div>
            <div class="section-header">
                <h2>Usuarios recientes</h2>
                <a href="${ctx}/admin?seccion=usuarios">Ver todos</a>
            </div>
            <div class="table-container">
                <table class="table">
                    <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Rol</th>
                        <th>Estado</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="u" items="${usuariosRecientes}">
                        <tr>
                            <td>
                                <a href="${ctx}/admin?seccion=usuarios&detalle=${u.id}">
                                    <c:out value="${u.nombre}"/>
                                </a>
                                <div class="text-muted" style="font-size:0.78rem;"><c:out value="${u.email}"/></div>
                            </td>
                            <td>
                                <span class="badge badge-primary"><c:out value="${u.rol}"/></span>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${u.estado == 'ACTIVO'}">
                                        <span class="badge badge-success">ACTIVO</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-danger">INACTIVO</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty usuariosRecientes}">
                        <tr><td colspan="3" class="text-center text-muted">No hay usuarios registrados.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>

        <div>
            <div class="section-header">
                <h2>Ultimas ofertas</h2>
                <a href="${ctx}/admin?seccion=ofertas">Ver todas</a>
            </div>
            <div class="table-container">
                <table class="table">
                    <thead>
                    <tr>
                        <th>Titulo</th>
                        <th>Estado</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="o" items="${ofertasRecientes}">
                        <tr>
                            <td>
                                <a href="${ctx}/admin?seccion=ofertas&detalle=${o.id}">
                                    <c:out value="${o.titulo}"/>
                                </a>
                                <div class="text-muted" style="font-size:0.78rem;"><c:out value="${o.empresa.nombre}"/></div>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${o.estado == 'ACTIVA'}">
                                        <span class="badge badge-success">ACTIVA</span>
                                    </c:when>
                                    <c:when test="${o.estado == 'PAUSADA'}">
                                        <span class="badge badge-warning">PAUSADA</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-danger">CERRADA</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty ofertasRecientes}">
                        <tr><td colspan="2" class="text-center text-muted">No hay ofertas publicadas.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="section-header mt-3">
        <h2>Actividad reciente</h2>
        <p class="text-muted">Registro de acciones administrativas (no incluye datos sensibles).</p>
    </div>
    <div class="table-container">
        <table class="table">
            <thead>
            <tr>
                <th>Fecha</th>
                <th>Administrador</th>
                <th>Accion</th>
                <th>Detalle</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="a" items="${auditoriaReciente}">
                <tr>
                    <td><c:out value="${a.fecha}"/></td>
                    <td><c:out value="${a.admin.nombre}"/></td>
                    <td><span class="badge badge-info"><c:out value="${a.accion}"/></span></td>
                    <td><c:out value="${a.detalle}"/></td>
                </tr>
            </c:forEach>
            <c:if test="${empty auditoriaReciente}">
                <tr><td colspan="4" class="text-center text-muted">Todavia no hay acciones registradas.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="../footer.jsp" />
