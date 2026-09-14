<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="header.jsp">
    <jsp:param name="tituloPagina" value="Editar Oferta"/>
</jsp:include>

<div class="container section-sm">
    <c:choose>
        <c:when test="${empty oferta}">
            <div class="empty-state">
                <div class="empty-state-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                </div>
                <h3>Oferta no encontrada</h3>
                <p>La oferta que deseas editar no existe.</p>
                <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-primary">Volver al listado</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="page-header">
                <h1>Editar oferta</h1>
                <a href="${pageContext.request.contextPath}/ofertas?format=jsp&accion=detalle&id=${oferta.id}" class="btn btn-ghost btn-sm">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/></svg>
                    Cancelar
                </a>
            </div>

            <c:if test="${not empty errores}">
                <div class="alert alert-error">
                    <c:forEach var="error" items="${errores}">
                        <div>${error}</div>
                    </c:forEach>
                </div>
            </c:if>

            <div class="card" style="max-width:750px;">
                <form action="${pageContext.request.contextPath}/ofertas" method="post">
                    <input type="hidden" name="accion" value="actualizar"/>
                    <input type="hidden" name="id" value="${oferta.id}"/>

                    <div class="form-group">
                        <label class="form-label" for="titulo">Titulo *</label>
                        <input type="text" id="titulo" name="titulo" class="form-control" required maxlength="200"
                               value="${oferta.titulo}"/>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="descripcion">Descripcion *</label>
                        <textarea id="descripcion" name="descripcion" class="form-control" required>${oferta.descripcion}</textarea>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="requisitos">Requisitos</label>
                        <textarea id="requisitos" name="requisitos" class="form-control">${oferta.requisitos}</textarea>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label" for="ubicacion">Ubicacion *</label>
                            <input type="text" id="ubicacion" name="ubicacion" class="form-control" required maxlength="150"
                                   value="${oferta.ubicacion}"/>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="salario">Salario</label>
                            <input type="number" id="salario" name="salario" class="form-control" step="0.01" min="0"
                                   value="${oferta.salario}"/>
                            <span class="form-hint">Formato numerico con decimales</span>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label" for="tipo_contrato">Tipo de contrato *</label>
                            <select id="tipo_contrato" name="tipo_contrato" class="form-control" required>
                                <option value="">Seleccione...</option>
                                <option value="Tiempo completo" ${oferta.tipoContrato == 'Tiempo completo' ? 'selected' : ''}>Tiempo completo</option>
                                <option value="Medio tiempo" ${oferta.tipoContrato == 'Medio tiempo' ? 'selected' : ''}>Medio tiempo</option>
                                <option value="Por proyecto" ${oferta.tipoContrato == 'Por proyecto' ? 'selected' : ''}>Por proyecto</option>
                                <option value="Temporal" ${oferta.tipoContrato == 'Temporal' ? 'selected' : ''}>Temporal</option>
                                <option value="Practica" ${oferta.tipoContrato == 'Practica' ? 'selected' : ''}>Practica</option>
                                <option value="Freelance" ${oferta.tipoContrato == 'Freelance' ? 'selected' : ''}>Freelance</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="horario">Horario *</label>
                            <input type="text" id="horario" name="horario" class="form-control" required maxlength="100"
                                   value="${oferta.horario}"/>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label" for="estado">Estado</label>
                            <select id="estado" name="estado" class="form-control">
                                <option value="ACTIVA" ${oferta.estado == 'ACTIVA' ? 'selected' : ''}>Activa</option>
                                <option value="PAUSADA" ${oferta.estado == 'PAUSADA' ? 'selected' : ''}>Pausada</option>
                                <option value="CERRADA" ${oferta.estado == 'CERRADA' ? 'selected' : ''}>Cerrada</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="empresa_id">Empresa ID *</label>
                            <input type="number" id="empresa_id" name="empresa_id" class="form-control" required min="1"
                                   value="${oferta.empresaId}"/>
                        </div>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary btn-lg">Guardar cambios</button>
                        <a href="${pageContext.request.contextPath}/ofertas?format=jsp&accion=detalle&id=${oferta.id}" class="btn btn-secondary">Cancelar</a>
                    </div>
                </form>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="footer.jsp" %>
