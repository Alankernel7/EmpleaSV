<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="../header.jsp">
    <jsp:param name="tituloPagina" value="Detalle de oferta"/>
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="container section-sm">
    <div class="page-header">
        <div>
            <h1><c:out value="${ofertaDetalle.titulo}"/></h1>
            <p class="text-muted" style="margin-top:0.35rem;">
                <c:out value="${ofertaDetalle.empresa.nombre}"/> &middot; <c:out value="${ofertaDetalle.ubicacion}"/>
            </p>
        </div>
        <a class="btn btn-outline" href="${ctx}/admin?seccion=ofertas">Volver a ofertas</a>
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
            <h1>Datos de la oferta</h1>
        </div>
        <div class="detail-card-body">
            <div class="detail-row">
                <span class="detail-label">Estado</span>
                <span class="detail-value">
                    <c:choose>
                        <c:when test="${ofertaDetalle.estado == 'ACTIVA'}"><span class="badge badge-success">ACTIVA</span></c:when>
                        <c:when test="${ofertaDetalle.estado == 'PAUSADA'}"><span class="badge badge-warning">PAUSADA</span></c:when>
                        <c:otherwise><span class="badge badge-danger">CERRADA</span></c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Empresa</span>
                <span class="detail-value">
                    <c:out value="${ofertaDetalle.empresa.nombre}"/>
                    (ID <c:out value="${ofertaDetalle.empresa.id}"/>)
                    <c:choose>
                        <c:when test="${empresaCuentaInactiva}">
                            <span class="badge badge-danger" title="La cuenta responsable de esta empresa esta desactivada">Cuenta desactivada</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge badge-success">Cuenta activa</span>
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Ubicacion</span>
                <span class="detail-value"><c:out value="${ofertaDetalle.ubicacion}"/></span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Tipo de contrato</span>
                <span class="detail-value"><c:out value="${ofertaDetalle.tipoContrato}"/></span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Horario</span>
                <span class="detail-value"><c:out value="${empty ofertaDetalle.horario ? 'No especificado' : ofertaDetalle.horario}"/></span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Salario</span>
                <span class="detail-value">
                    <c:choose>
                        <c:when test="${not empty ofertaDetalle.salario}">
                            $<c:out value="${ofertaDetalle.salario}"/>
                        </c:when>
                        <c:otherwise>No especificado</c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Publicada</span>
                <span class="detail-value"><c:out value="${ofertaDetalle.fechaPublicacion}"/></span>
            </div>

            <div class="detail-section">
                <h3>Descripcion</h3>
                <p style="white-space:pre-wrap;"><c:out value="${ofertaDetalle.descripcion}"/></p>
            </div>

            <c:if test="${not empty ofertaDetalle.requisitos}">
                <div class="detail-section">
                    <h3>Requisitos</h3>
                    <p style="white-space:pre-wrap;"><c:out value="${ofertaDetalle.requisitos}"/></p>
                </div>
            </c:if>
        </div>
    </div>

    <div class="detail-actions mt-3">
        <c:choose>
            <c:when test="${ofertaDetalle.estado != 'PAUSADA'}">
                <form method="post" action="${ctx}/admin"
                      onsubmit="return confirm('Pausar esta oferta? Dejara de mostrarse en busquedas.');">
                    <input type="hidden" name="accion" value="cambiar-estado-oferta"/>
                    <input type="hidden" name="seccion" value="ofertas"/>
                    <input type="hidden" name="id" value="${ofertaDetalle.id}"/>
                    <input type="hidden" name="estado" value="PAUSADA"/>
                    <button type="submit" class="btn btn-outline">Pausar oferta</button>
                </form>
            </c:when>
            <c:otherwise>
                <form method="post" action="${ctx}/admin"
                      onsubmit="return confirm('Reactivar esta oferta?');">
                    <input type="hidden" name="accion" value="cambiar-estado-oferta"/>
                    <input type="hidden" name="seccion" value="ofertas"/>
                    <input type="hidden" name="id" value="${ofertaDetalle.id}"/>
                    <input type="hidden" name="estado" value="ACTIVA"/>
                    <button type="submit" class="btn btn-success">Reactivar oferta</button>
                </form>
            </c:otherwise>
        </c:choose>
        <c:if test="${ofertaDetalle.estado != 'CERRADA'}">
            <form method="post" action="${ctx}/admin"
                  onsubmit="return confirm('Cerrar esta oferta? No se podra reactivar sin editarla desde el CRUD.');">
                <input type="hidden" name="accion" value="cambiar-estado-oferta"/>
                <input type="hidden" name="seccion" value="ofertas"/>
                <input type="hidden" name="id" value="${ofertaDetalle.id}"/>
                <input type="hidden" name="estado" value="CERRADA"/>
                <button type="submit" class="btn btn-danger">Cerrar oferta</button>
            </form>
        </c:if>
        <a class="btn btn-outline" href="${ctx}/ofertas?format=jsp&accion=detalle&id=${ofertaDetalle.id}">
            Ver ficha publica
        </a>
    </div>
</div>

<jsp:include page="../footer.jsp" />
