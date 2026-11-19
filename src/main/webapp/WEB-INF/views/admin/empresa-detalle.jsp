<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="../header.jsp">
    <jsp:param name="tituloPagina" value="Detalle de empresa"/>
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="container section-sm">
    <div class="page-header">
        <div>
            <h1><c:out value="${empty perfilEmpresaDetalle ? cuentaDetalle.nombre : perfilEmpresaDetalle.nombreEmpresa}"/></h1>
            <p class="text-muted" style="margin-top:0.35rem;">
                Cuenta responsable: <c:out value="${cuentaDetalle.email}"/>
            </p>
        </div>
        <a class="btn btn-outline" href="${ctx}/admin?seccion=empresas">Volver a empresas</a>
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
            <h1>Cuenta y perfil</h1>
        </div>
        <div class="detail-card-body">
            <div class="detail-row">
                <span class="detail-label">Estado de la cuenta</span>
                <span class="detail-value">
                    <c:choose>
                        <c:when test="${cuentaDetalle.estado == 'ACTIVO'}">
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
                <span class="detail-value"><c:out value="${cuentaDetalle.fechaCreacion}"/></span>
            </div>

            <c:choose>
                <c:when test="${not empty perfilEmpresaDetalle}">
                    <div class="detail-section">
                        <h3>Perfil empresarial</h3>
                        <div class="detail-row">
                            <span class="detail-label">Nombre del perfil</span>
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
                        <div class="detail-row">
                            <span class="detail-label">Municipio / Departamento</span>
                            <span class="detail-value">
                                <c:out value="${empty perfilEmpresaDetalle.municipio ? '-' : perfilEmpresaDetalle.municipio}"/>
                                /
                                <c:out value="${empty perfilEmpresaDetalle.departamento ? '-' : perfilEmpresaDetalle.departamento}"/>
                            </span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Sector</span>
                            <span class="detail-value"><c:out value="${empty perfilEmpresaDetalle.sector ? 'Sin registrar' : perfilEmpresaDetalle.sector}"/></span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Sitio web</span>
                            <span class="detail-value"><c:out value="${empty perfilEmpresaDetalle.sitioWeb ? 'Sin registrar' : perfilEmpresaDetalle.sitioWeb}"/></span>
                        </div>
                        <c:if test="${not empty perfilEmpresaDetalle.descripcion}">
                            <div class="detail-row">
                                <span class="detail-label">Descripcion</span>
                                <span class="detail-value"><c:out value="${perfilEmpresaDetalle.descripcion}"/></span>
                            </div>
                        </c:if>
                    </div>

                    <c:if test="${not empty empresaDetalle}">
                        <div class="detail-section">
                            <h3>Empresa vinculada</h3>
                            <div class="detail-row">
                                <span class="detail-label">Nombre</span>
                                <span class="detail-value"><c:out value="${empresaDetalle.nombre}"/> (ID <c:out value="${empresaDetalle.id}"/>)</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Categoria</span>
                                <span class="detail-value"><c:out value="${empresaDetalle.categoria}"/></span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Email publico</span>
                                <span class="detail-value"><c:out value="${empresaDetalle.email}"/></span>
                            </div>
                        </div>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <div class="aviso aviso-aviso mt-3">
                        <div><strong>Perfil incompleto</strong> Esta cuenta aun no ha completado su perfil empresarial.</div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="section-header mt-3">
        <h2>Ofertas publicadas</h2>
        <p class="text-muted"><c:out value="${empty ofertasEmpresa ? 0 : ofertasEmpresa.size()}"/> oferta(s) en total.</p>
    </div>
    <div class="table-container">
        <table class="table">
            <thead>
            <tr>
                <th>Titulo</th>
                <th>Ubicacion</th>
                <th>Publicada</th>
                <th>Estado</th>
                <th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="o" items="${ofertasEmpresa}">
                <tr>
                    <td><c:out value="${o.titulo}"/></td>
                    <td><c:out value="${o.ubicacion}"/></td>
                    <td><c:out value="${o.fechaPublicacion}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${o.estado == 'ACTIVA'}"><span class="badge badge-success">ACTIVA</span></c:when>
                            <c:when test="${o.estado == 'PAUSADA'}"><span class="badge badge-warning">PAUSADA</span></c:when>
                            <c:otherwise><span class="badge badge-danger">CERRADA</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <a class="btn btn-outline btn-sm"
                           href="${ctx}/admin?seccion=ofertas&detalle=${o.id}">Ver oferta</a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty ofertasEmpresa}">
                <tr><td colspan="5" class="text-center text-muted">Esta empresa no tiene ofertas publicadas.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>

    <div class="detail-actions mt-3">
        <c:choose>
            <c:when test="${cuentaDetalle.estado == 'ACTIVO'}">
                <form method="post" action="${ctx}/admin"
                      onsubmit="return confirm('Desactivar la cuenta de ${cuentaDetalle.email}? La empresa no podra iniciar sesion; sus ofertas seguiran visibles hasta que un administrador las revise.');">
                    <input type="hidden" name="accion" value="cambiar-estado-usuario"/>
                    <input type="hidden" name="seccion" value="empresas"/>
                    <input type="hidden" name="id" value="${cuentaDetalle.id}"/>
                    <input type="hidden" name="estado" value="INACTIVO"/>
                    <button type="submit" class="btn btn-danger">Desactivar cuenta empresa</button>
                </form>
            </c:when>
            <c:otherwise>
                <form method="post" action="${ctx}/admin"
                      onsubmit="return confirm('Reactivar la cuenta de ${cuentaDetalle.email}?');">
                    <input type="hidden" name="accion" value="cambiar-estado-usuario"/>
                    <input type="hidden" name="seccion" value="empresas"/>
                    <input type="hidden" name="id" value="${cuentaDetalle.id}"/>
                    <input type="hidden" name="estado" value="ACTIVO"/>
                    <button type="submit" class="btn btn-success">Reactivar cuenta empresa</button>
                </form>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="../footer.jsp" />
