<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="../header.jsp">
    <jsp:param name="tituloPagina" value="Gestion de empresas"/>
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="container section-sm">
    <div class="page-header">
        <div>
            <h1>Empresas</h1>
            <p class="text-muted" style="margin-top:0.35rem;">Cuentas empresa registradas y su actividad.</p>
        </div>
    </div>

    <jsp:include page="tabs.jsp" />

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success"><c:out value="${mensajeExito}"/></div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-error"><c:out value="${mensajeError}"/></div>
    </c:if>

    <div class="table-container mt-3">
        <table class="table">
            <thead>
            <tr>
                <th>Cuenta responsable</th>
                <th>Email</th>
                <th>Estado cuenta</th>
                <th>Empresa (perfil)</th>
                <th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="fila" items="${cuentasEmpresa}">
                <c:set var="cuenta" value="${fila.cuenta}" />
                <tr>
                    <td>
                        <a href="${ctx}/admin?seccion=empresas&detalle=${cuenta.id}">
                            <c:out value="${cuenta.nombre}"/>
                        </a>
                        <div class="text-muted" style="font-size:0.78rem;">ID cuenta: <c:out value="${cuenta.id}"/></div>
                    </td>
                    <td><c:out value="${cuenta.email}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${cuenta.estado == 'ACTIVO'}">
                                <span class="badge badge-success">ACTIVO</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge badge-danger">INACTIVO</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:out value="${empty fila.nombreEmpresa ? 'Pendiente de completar' : fila.nombreEmpresa}"/>
                        <div class="text-muted" style="font-size:0.78rem;">
                            <c:out value="${fila.ofertas}"/> oferta(s)
                        </div>
                    </td>
                    <td class="fila-acciones">
                        <a class="btn btn-outline btn-sm"
                           href="${ctx}/admin?seccion=empresas&detalle=${cuenta.id}">Ver</a>
                        <c:choose>
                            <c:when test="${cuenta.estado == 'ACTIVO'}">
                                <form method="post" action="${ctx}/admin"
                                      onsubmit="return confirm('Desactivar la cuenta de ${cuenta.email}? La empresa no podra iniciar sesion ni publicar ofertas.');">
                                    <input type="hidden" name="accion" value="cambiar-estado-usuario"/>
                                    <input type="hidden" name="seccion" value="empresas"/>
                                    <input type="hidden" name="id" value="${cuenta.id}"/>
                                    <input type="hidden" name="estado" value="INACTIVO"/>
                                    <button type="submit" class="btn btn-danger btn-sm">Desactivar</button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <form method="post" action="${ctx}/admin"
                                      onsubmit="return confirm('Reactivar la cuenta de ${cuenta.email}?');">
                                    <input type="hidden" name="accion" value="cambiar-estado-usuario"/>
                                    <input type="hidden" name="seccion" value="empresas"/>
                                    <input type="hidden" name="id" value="${cuenta.id}"/>
                                    <input type="hidden" name="estado" value="ACTIVO"/>
                                    <button type="submit" class="btn btn-success btn-sm">Activar</button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty cuentasEmpresa}">
                <tr><td colspan="5" class="text-center text-muted">No hay cuentas de empresa registradas.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="../footer.jsp" />
