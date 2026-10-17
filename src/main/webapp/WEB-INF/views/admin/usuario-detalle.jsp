<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="../header.jsp">
    <jsp:param name="tituloPagina" value="Detalle de usuario"/>
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="container section-sm">
    <div class="page-header">
        <div>
            <h1><c:out value="${usuarioDetalle.nombre}"/></h1>
            <p class="text-muted" style="margin-top:0.35rem;">Cuenta #<c:out value="${usuarioDetalle.id}"/></p>
        </div>
        <a class="btn btn-outline" href="${ctx}/admin?seccion=usuarios">Volver a usuarios</a>
    </div>

    <jsp:include page="tabs.jsp" />

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success"><c:out value="${mensajeExito}"/></div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-error"><c:out value="${mensajeError}"/></div>
    </c:if>

    <div class="detail-card mt-3">
        <div class="detail-card-header">
            <h1>Datos de la cuenta</h1>
        </div>
        <div class="detail-card-body">
            <div class="detail-row">
                <span class="detail-label">Email</span>
                <span class="detail-value"><c:out value="${usuarioDetalle.email}"/></span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Rol</span>
                <span class="detail-value"><span class="badge badge-primary"><c:out value="${usuarioDetalle.rol}"/></span></span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Estado</span>
                <span class="detail-value">
                    <c:choose>
                        <c:when test="${usuarioDetalle.estado == 'ACTIVO'}">
                            <span class="badge badge-success">ACTIVO</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge badge-danger">INACTIVO</span>
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Fecha de registro</span>
                <span class="detail-value"><c:out value="${usuarioDetalle.fechaCreacion}"/></span>
            </div>

            <c:if test="${not empty perfilEmpresaDetalle}">
                <div class="detail-section">
                    <h3>Perfil empresarial</h3>
                    <div class="detail-row">
                        <span class="detail-label">Empresa (perfil)</span>
                        <span class="detail-value"><c:out value="${perfilEmpresaDetalle.nombreEmpresa}"/></span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Responsable</span>
                        <span class="detail-value"><c:out value="${perfilEmpresaDetalle.nombreResponsable}"/></span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">NIT</span>
                        <span class="detail-value"><c:out value="${empty perfilEmpresaDetalle.nit ? 'Sin registrar' : perfilEmpresaDetalle.nit}"/></span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Telefono</span>
                        <span class="detail-value"><c:out value="${empty perfilEmpresaDetalle.telefonoEmpresa ? 'Sin registrar' : perfilEmpresaDetalle.telefonoEmpresa}"/></span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Direccion</span>
                        <span class="detail-value"><c:out value="${empty perfilEmpresaDetalle.direccionEmpresa ? 'Sin registrar' : perfilEmpresaDetalle.direccionEmpresa}"/></span>
                    </div>
                    <c:if test="${not empty perfilEmpresaDetalle.empresa}">
                        <div class="detail-row">
                            <span class="detail-label">Empresa vinculada</span>
                            <span class="detail-value">
                                <c:out value="${perfilEmpresaDetalle.empresa.nombre}"/>
                                (ID <c:out value="${perfilEmpresaDetalle.empresa.id}"/>)
                            </span>
                        </div>
                    </c:if>
                </div>

                <c:if test="${not empty ofertasEmpresa}">
                    <div class="detail-section">
                        <h3>Ofertas de la empresa (<c:out value="${ofertasEmpresa.size()}"/>)</h3>
                        <c:forEach var="o" items="${ofertasEmpresa}">
                            <div class="detail-row">
                                <span class="detail-label"><c:out value="${o.titulo}"/></span>
                                <span class="detail-value"><span class="badge ${o.estado == 'ACTIVA' ? 'badge-success' : (o.estado == 'PAUSADA' ? 'badge-warning' : 'badge-danger')}"><c:out value="${o.estado}"/></span></span>
                            </div>
                        </c:forEach>
                    </div>
                </c:if>
            </c:if>
        </div>
    </div>

    <div class="detail-actions mt-3">
        <c:choose>
            <c:when test="${usuarioDetalle.id == usuarioId}">
                <div class="aviso aviso-aviso">
                    <div><strong>Tu propia cuenta</strong> No es posible desactivarte desde este panel.</div>
                </div>
            </c:when>
            <c:when test="${usuarioDetalle.estado == 'ACTIVO'}">
                <form method="post" action="${ctx}/admin"
                      onsubmit="return confirm('Desactivar la cuenta de ${usuarioDetalle.email}? El usuario no podra iniciar sesion.');">
                    <input type="hidden" name="accion" value="cambiar-estado-usuario"/>
                    <input type="hidden" name="seccion" value="usuarios"/>
                    <input type="hidden" name="id" value="${usuarioDetalle.id}"/>
                    <input type="hidden" name="estado" value="INACTIVO"/>
                    <button type="submit" class="btn btn-danger">Desactivar cuenta</button>
                </form>
            </c:when>
            <c:otherwise>
                <form method="post" action="${ctx}/admin"
                      onsubmit="return confirm('Reactivar la cuenta de ${usuarioDetalle.email}?');">
                    <input type="hidden" name="accion" value="cambiar-estado-usuario"/>
                    <input type="hidden" name="seccion" value="usuarios"/>
                    <input type="hidden" name="id" value="${usuarioDetalle.id}"/>
                    <input type="hidden" name="estado" value="ACTIVO"/>
                    <button type="submit" class="btn btn-success">Reactivar cuenta</button>
                </form>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="../footer.jsp" />
