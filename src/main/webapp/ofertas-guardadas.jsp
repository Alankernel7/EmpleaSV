<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/header.jsp">
    <jsp:param name="tituloPagina" value="Ofertas Guardadas"/>
</jsp:include>

<div class="container section-sm">
    <div class="page-header">
        <h1>Mis ofertas guardadas</h1>
    </div>

    <div class="alert alert-info">
        Esta seccion es visual. La funcionalidad de guardado estara disponible proximamente.
    </div>

    <!-- Oferta guardada ejemplo 1 -->
    <div class="card mb-1">
        <div class="card-header">
            <div style="flex:1;">
                <div class="card-title">
                    <a href="#">Desarrollador Java Senior</a>
                </div>
                <div class="card-meta">
                    <span class="card-meta-item">TechCorp El Salvador</span>
                    <span class="card-meta-item">San Salvador</span>
                    <span class="card-meta-item">$1,800</span>
                    <span class="badge badge-success">Activa</span>
                </div>
            </div>
            <div class="card-actions">
                <a href="#" class="btn btn-primary btn-sm" onclick="alert('La funcion de postulacion estara disponible proximamente.'); return false;">Postularme</a>
                <button class="btn btn-ghost btn-sm" onclick="alert('Removida de guardados (visual).')">Eliminar</button>
            </div>
        </div>
        <div class="card-body">
            <p class="card-text">Buscamos desarrollador Java con experiencia en Spring Boot y microservicios.</p>
        </div>
    </div>

    <!-- Oferta guardada ejemplo 2 -->
    <div class="card mb-1">
        <div class="card-header">
            <div style="flex:1;">
                <div class="card-title">
                    <a href="#">Ingeniero de Datos</a>
                </div>
                <div class="card-meta">
                    <span class="card-meta-item">DataVision SA</span>
                    <span class="card-meta-item">La Libertad</span>
                    <span class="card-meta-item">$1,500</span>
                    <span class="badge badge-success">Activa</span>
                </div>
            </div>
            <div class="card-actions">
                <a href="#" class="btn btn-primary btn-sm" onclick="alert('La funcion de postulacion estara disponible proximamente.'); return false;">Postularme</a>
                <button class="btn btn-ghost btn-sm" onclick="alert('Removida de guardados (visual).')">Eliminar</button>
            </div>
        </div>
        <div class="card-body">
            <p class="card-text">Requerimos ingeniero de datos con conocimientos en Python, SQL y herramientas ETL.</p>
        </div>
    </div>

    <!-- Oferta guardada ejemplo 3 -->
    <div class="card mb-1">
        <div class="card-header">
            <div style="flex:1;">
                <div class="card-title">
                    <a href="#">Disenador UX/UI</a>
                </div>
                <div class="card-meta">
                    <span class="card-meta-item">Creative Studio</span>
                    <span class="card-meta-item">Santa Ana</span>
                    <span class="card-meta-item">$1,200</span>
                    <span class="badge badge-warning">Pausada</span>
                </div>
            </div>
            <div class="card-actions">
                <a href="#" class="btn btn-primary btn-sm" onclick="alert('La funcion de postulacion estara disponible proximamente.'); return false;">Postularme</a>
                <button class="btn btn-ghost btn-sm" onclick="alert('Removida de guardados (visual).')">Eliminar</button>
            </div>
        </div>
        <div class="card-body">
            <p class="card-text">Estamos buscando disenador UX/UI con experiencia en Figma y prototipado.</p>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/footer.jsp" %>
