<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="../header.jsp">
    <jsp:param name="tituloPagina" value="Gestion de usuarios"/>
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="container section-sm">
    <div class="page-header">
        <div>
            <h1>Usuarios</h1>
            <p class="text-muted" style="margin-top:0.35rem;">Busca, filtra y activa o desactiva cuentas de la plataforma.</p>
        </div>
    </div>

    <jsp:include page="tabs.jsp" />

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success"><c:out value="${mensajeExito}"/></div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-error"><c:out value="${mensajeError}"/></div>
    </c:if>

    <form class="search-bar-inline mt-3" method="get" action="${ctx}/admin">
        <input type="hidden" name="seccion" value="usuarios"/>
        <div class="form-group">
            <label class="form-label" for="q">Buscar</label>
            <input class="form-control" type="text" id="q" name="q"
                   value="${filtroQ}" placeholder="Nombre o email"/>
        </div>
        <div class="form-group">
            <label class="form-label" for="rol">Rol</label>
            <select class="form-control" id="rol" name="rol">
                <option value="">Todos</option>
                <option value="USUARIO" ${filtroRol == 'USUARIO' ? 'selected' : ''}>Candidato</option>
                <option value="EMPRESA" ${filtroRol == 'EMPRESA' ? 'selected' : ''}>Empresa</option>
                <option value="ADMINISTRADOR" ${filtroRol == 'ADMINISTRADOR' ? 'selected' : ''}>Administrador</option>
            </select>
        </div>
        <div class="form-group">
            <label class="form-label" for="estado">Estado</label>
            <select class="form-control" id="estado" name="estado">
                <option value="">Todos</option>
                <option value="ACTIVO" ${filtroEstado == 'ACTIVO' ? 'selected' : ''}>Activo</option>
                <option value="INACTIVO" ${filtroEstado == 'INACTIVO' ? 'selected' : ''}>Inactivo</option>
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
                <th>Nombre</th>
                <th>Email</th>
                <th>Rol</th>
                <th>Estado</th>
                <th>Registro</th>
                <th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="u" items="${usuarios}">
                <tr>
                    <td><c:out value="${u.id}"/></td>
                    <td>
                        <a href="${ctx}/admin?seccion=usuarios&detalle=${u.id}">
                            <c:out value="${u.nombre}"/>
                        </a>
                    </td>
                    <td><c:out value="${u.email}"/></td>
                    <td><span class="badge badge-primary"><c:out value="${u.rol}"/></span></td>
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
                    <td><c:out value="${u.fechaCreacion}"/></td>
                    <td class="fila-acciones">
                        <a class="btn btn-outline btn-sm"
                           href="${ctx}/admin?seccion=usuarios&detalle=${u.id}">Ver</a>
                        <c:choose>
                            <c:when test="${u.id == usuarioId}">
                                <span class="badge badge-info" title="No puedes desactivar tu propia cuenta">Yo</span>
                            </c:when>
                            <c:when test="${u.estado == 'ACTIVO'}">
                                <form method="post" action="${ctx}/admin"
                                      onsubmit="return confirm('Desactivar la cuenta de ${u.email}? El usuario no podra iniciar sesion.');">
                                    <input type="hidden" name="accion" value="cambiar-estado-usuario"/>
                                    <input type="hidden" name="seccion" value="usuarios"/>
                                    <input type="hidden" name="id" value="${u.id}"/>
                                    <input type="hidden" name="estado" value="INACTIVO"/>
                                    <button type="submit" class="btn btn-danger btn-sm">Desactivar</button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <form method="post" action="${ctx}/admin"
                                      onsubmit="return confirm('Reactivar la cuenta de ${u.email}?');">
                                    <input type="hidden" name="accion" value="cambiar-estado-usuario"/>
                                    <input type="hidden" name="seccion" value="usuarios"/>
                                    <input type="hidden" name="id" value="${u.id}"/>
                                    <input type="hidden" name="estado" value="ACTIVO"/>
                                    <button type="submit" class="btn btn-success btn-sm">Activar</button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty usuarios}">
                <tr><td colspan="7" class="text-center text-muted">No se encontraron usuarios con esos filtros.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="../footer.jsp" />
