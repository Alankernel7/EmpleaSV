<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="/WEB-INF/views/header.jsp">
    <jsp:param name="tituloPagina" value="Detalle de Empresa"/>
</jsp:include>

<div class="container section-sm">

    <%-- Volver --%>
    <div class="page-header">

        <h1>Perfil de empresa</h1>

        <a href="${pageContext.request.contextPath}/empresas"
           class="btn btn-ghost btn-sm">

            <svg xmlns="http://www.w3.org/2000/svg"
                 width="16"
                 height="16"
                 fill="none"
                 viewBox="0 0 24 24"
                 stroke="currentColor">

                <path stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M10 19l-7-7m0 0l7-7m-7 7h18"/>
            </svg>
            Volver a empresas
        </a>
    </div>

    <%-- Información de la empresa --%>
    <div class="detail-card">
        <div class="detail-card-header" style="text-align:center;">
            <div class="company-card-logo">
                ${empresa.iniciales}
            </div>

            <h1>
                ${empresa.nombre}
            </h1>

            <span class="badge badge-primary">${empresa.categoria}</span>
        </div>

        <div class="detail-card-body">
            <div class="detail-section">
                <h3>Sobre la empresa</h3>

                <p class="card-text">
                    ${empresa.descripcion}</p>
            </div>

            <div class="detail-section">
                <h3>Información de contacto</h3>

                <div class="detail-row">
                    <div class="detail-label">Correo</div>

                    <div class="detail-value">${empresa.email}</div>
                </div>

                <div class="detail-row">
                    <div class="detail-label">Telefono</div>

                    <div class="detail-value">${empresa.telefono}</div>
                </div>

                <div class="detail-row">
                    <div class="detail-label">Categoria</div>

                    <div class="detail-value">${empresa.categoria}</div>
                </div>
            </div>
        </div>
    </div>

    <%-- Ofertas de la empresa --%>
    <section class="section">
        <div class="section-header">
            <h2>Ofertas de ${empresa.nombre}</h2>
            <p>${fn:length(ofertas)}
                <c:choose>
                    <c:when test="${fn:length(ofertas) == 1}">
                        oferta publicada
                    </c:when>

                    <c:otherwise>
                        ofertas publicadas
                    </c:otherwise>
                </c:choose>
            </p>
        </div>

        <%-- Si existen ofertas --%>
        <c:if test="${not empty ofertas}">
            <div class="grid-3">
                <c:forEach var="oferta" items="${ofertas}">
                    <%-- Iniciales del título --%>
                    <c:set var="palabrasTitulo"
                           value="${fn:split(oferta.titulo, ' ')}"/>

                    <c:set var="inicial1"
                           value="${fn:toUpperCase(
                               fn:substring(palabrasTitulo[0],0,1)
                           )}"/>

                    <c:set var="inicial2" value=""/>

                    <c:if test="${fn:length(palabrasTitulo) > 1}">
                        <c:set var="inicial2"
                               value="${fn:toUpperCase(fn:substring(palabrasTitulo[1],0,1)
                               )}"/>
                    </c:if>

                    <div class="featured-card">
                        <%-- Estado --%>
                        <div class="featured-card-badge">
                            <c:choose>
                                <c:when test="${oferta.estado == 'ACTIVA'}">
                                    <span class="badge badge-success">
                                        Activa
                                    </span>
                                </c:when>

                                <c:when test="${oferta.estado == 'PAUSADA'}">
                                    <span class="badge badge-warning">
                                        Pausada
                                    </span>
                                </c:when>

                                <c:otherwise>
                                    <span class="badge badge-danger">
                                        Cerrada
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <%-- Iniciales --%>
                        <div class="featured-card-icon">
                            ${inicial1}${inicial2}
                        </div>

                        <%-- Título --%>
                        <h4>
                            <a href="${pageContext.request.contextPath}/ofertas?format=jsp&accion=detalle&id=${oferta.id}">
                                ${oferta.titulo}
                            </a>
                        </h4>

                        <%-- Metadata --%>
                        <div class="card-meta">
                            <span class="card-meta-item">
                                ${oferta.ubicacion}
                            </span>

                            <span class="card-meta-item">
                                <c:choose>
                                    <c:when test="${not empty oferta.salario}">
                                        $<fmt:formatNumber
                                            value="${oferta.salario}"
                                            minFractionDigits="2"
                                            maxFractionDigits="2"/>
                                    </c:when>

                                    <c:otherwise>
                                        Salario no especificado
                                    </c:otherwise>
                                </c:choose>
                            </span>

                            <span class="card-meta-item">
                                ${oferta.tipoContrato}
                            </span>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:if>

        <%-- Empresa sin ofertas --%>
        <c:if test="${empty ofertas}">
            <div class="empty-state">
                <div class="empty-state-icon">
                    <svg xmlns="http://www.w3.org/2000/svg"
                         width="28"
                         height="28"
                         fill="none"
                         viewBox="0 0 24 24"
                         stroke="currentColor">

                        <path stroke-linecap="round"
                              stroke-linejoin="round"
                              stroke-width="2"
                              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                    </svg>
                </div>
                <h3>No hay ofertas disponibles</h3>

                <p>Esta empresa todavía no ha publicado ofertas de empleo.</p>
            </div>
        </c:if>
    </section>
</div>

<%@ include file="/WEB-INF/views/footer.jsp" %>