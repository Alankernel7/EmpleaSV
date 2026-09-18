<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="header.jsp">
    <jsp:param name="tituloPagina" value="Registrar Oferta"/>
</jsp:include>

<div class="container section-sm">
    <div class="page-header">
        <h1>Registrar nueva oferta</h1>
        <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-ghost btn-sm">
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
            <input type="hidden" name="accion" value="registrar"/>
            <input type="hidden" name="format" value="jsp" />
            <div class="form-group">
                <label class="form-label" for="titulo">Titulo *</label>
                <input type="text" id="titulo" name="titulo" class="form-control" required maxlength="200"
                       value="${param.titulo}" placeholder="Ej: Desarrollador Java Senior"/>
            </div>

            <div class="form-group">
                <label class="form-label" for="descripcion">Descripcion *</label>
                <textarea id="descripcion" name="descripcion" class="form-control" required
                          placeholder="Describe las responsabilidades y actividades del puesto...">${param.descripcion}</textarea>
            </div>

            <div class="form-group">
                <label class="form-label" for="requisitos">Requisitos</label>
                <textarea id="requisitos" name="requisitos" class="form-control"
                          placeholder="Experiencia, conocimientos, habilidades requeridas...">${param.requisitos}</textarea>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="ubicacion">Ubicacion *</label>
                    <input type="text" id="ubicacion" name="ubicacion" class="form-control" required maxlength="150"
                           value="${param.ubicacion}" placeholder="Ej: San Salvador"/>
                </div>
                <div class="form-group">
                    <label class="form-label" for="salario">Salario</label>
                    <input type="number" id="salario" name="salario" class="form-control" step="0.01" min="0"
                           value="${param.salario}" placeholder="Ej: 1500.00"/>
                    <span class="form-hint">Formato numerico con decimales</span>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="tipo_contrato">Tipo de contrato *</label>
                    <select id="tipo_contrato" name="tipo_contrato" class="form-control" required>
                        <option value="">Seleccione...</option>
                        <option value="Tiempo completo" ${param.tipo_contrato == 'Tiempo completo' ? 'selected' : ''}>Tiempo completo</option>
                        <option value="Medio tiempo" ${param.tipo_contrato == 'Medio tiempo' ? 'selected' : ''}>Medio tiempo</option>
                        <option value="Por proyecto" ${param.tipo_contrato == 'Por proyecto' ? 'selected' : ''}>Por proyecto</option>
                        <option value="Temporal" ${param.tipo_contrato == 'Temporal' ? 'selected' : ''}>Temporal</option>
                        <option value="Practica" ${param.tipo_contrato == 'Practica' ? 'selected' : ''}>Practica</option>
                        <option value="Freelance" ${param.tipo_contrato == 'Freelance' ? 'selected' : ''}>Freelance</option>
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label" for="horario">Horario *</label>
                    <input type="text" id="horario" name="horario" class="form-control" required maxlength="100"
                           value="${param.horario}" placeholder="Ej: 8:00 AM - 5:00 PM"/>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="estado">Estado</label>
                    <select id="estado" name="estado" class="form-control">
                        <option value="ACTIVA" ${param.estado == 'ACTIVA' ? 'selected' : ''}>Activa</option>
                        <option value="PAUSADA" ${param.estado == 'PAUSADA' ? 'selected' : ''}>Pausada</option>
                        <option value="CERRADA" ${param.estado == 'CERRADA' ? 'selected' : ''}>Cerrada</option>
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label" for="empresa_id">Empresa *</label>
                    <select id="empresa_id" name="empresa_id" class="form-control" required>

                        <option value="">
                           Seleccione una empresa...
                        </option>

                        <c:forEach var="empresa" items="${empresas}">

                            <option value="${empresa.id}"
                                ${param.empresa_id == empresa.id ? 'selected' : ''}>
                                    ${empresa.nombre}
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn btn-primary btn-lg">Registrar oferta</button>
                <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-secondary">Cancelar</a>
            </div>
        </form>
    </div>
</div>

<%@ include file="footer.jsp" %>
