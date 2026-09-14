<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/header.jsp">
    <jsp:param name="tituloPagina" value="Panel de Empresa"/>
</jsp:include>

<div class="container section-sm">
    <div class="page-header">
        <h1>Panel de empresa</h1>
        <a href="${pageContext.request.contextPath}/ofertas?format=jsp&accion=registrar" class="btn btn-primary">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
            Nueva oferta
        </a>
    </div>

    <div class="alert alert-info">
        Panel visual. Las estadisticas mostradas son de ejemplo.
    </div>

    <!-- Stats -->
    <div class="dashboard-stats">
        <div class="stat-card">
            <div class="stat-card-label">Ofertas activas</div>
            <div class="stat-card-value">12</div>
            <div class="stat-card-change">+2 esta semana</div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Total ofertas</div>
            <div class="stat-card-value">24</div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Candidatos</div>
            <div class="stat-card-value">86</div>
            <div class="stat-card-change">+15 este mes</div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Ofertas cerradas</div>
            <div class="stat-card-value">12</div>
        </div>
    </div>

    <!-- Tabla de ofertas -->
    <div class="table-container">
        <div style="padding:1rem 1.25rem; border-bottom:1px solid var(--gray-200); display:flex; justify-content:space-between; align-items:center;">
            <h3 style="font-size:1rem;">Mis ofertas</h3>
            <a href="${pageContext.request.contextPath}/ofertas?format=jsp" class="btn btn-secondary btn-sm">Ver todas</a>
        </div>
        <table class="table">
            <thead>
                <tr>
                    <th>Oferta</th>
                    <th>Ubicacion</th>
                    <th>Estado</th>
                    <th>Postulantes</th>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td style="font-weight:600;">Desarrollador Full Stack</td>
                    <td>San Salvador</td>
                    <td><span class="badge badge-success">Activa</span></td>
                    <td>18</td>
                    <td>
                        <a href="#" class="btn btn-secondary btn-sm">Editar</a>
                    </td>
                </tr>
                <tr>
                    <td style="font-weight:600;">Disenador UI/UX</td>
                    <td>Santa Ana</td>
                    <td><span class="badge badge-success">Activa</span></td>
                    <td>12</td>
                    <td>
                        <a href="#" class="btn btn-secondary btn-sm">Editar</a>
                    </td>
                </tr>
                <tr>
                    <td style="font-weight:600;">Analista de Datos</td>
                    <td>La Libertad</td>
                    <td><span class="badge badge-success">Activa</span></td>
                    <td>8</td>
                    <td>
                        <a href="#" class="btn btn-secondary btn-sm">Editar</a>
                    </td>
                </tr>
                <tr>
                    <td style="font-weight:600;">Gerente de Marketing</td>
                    <td>San Salvador</td>
                    <td><span class="badge badge-warning">Pausada</span></td>
                    <td>5</td>
                    <td>
                        <a href="#" class="btn btn-secondary btn-sm">Editar</a>
                    </td>
                </tr>
                <tr>
                    <td style="font-weight:600;">Contador Fiscal</td>
                    <td>San Miguel</td>
                    <td><span class="badge badge-danger">Cerrada</span></td>
                    <td>22</td>
                    <td>
                        <a href="#" class="btn btn-secondary btn-sm">Editar</a>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="/WEB-INF/views/footer.jsp" %>
