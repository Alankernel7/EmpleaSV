<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/header.jsp">
    <jsp:param name="tituloPagina" value="Mis Postulaciones"/>
</jsp:include>

<div class="container section-sm">
    <div class="page-header">
        <h1>Mis postulaciones</h1>
    </div>

    <div class="alert alert-info">
        Esta seccion es visual. La funcionalidad de postulaciones estara disponible proximamente.
    </div>

    <!-- Postulacion 1 -->
    <div class="postulacion-card">
        <div class="postulacion-info">
            <h4>Desarrollador Full Stack</h4>
            <p>TechCorp El Salvador | San Salvador | Postulada el 10/09/2026</p>
        </div>
        <span class="badge badge-success">Entrevista</span>
    </div>

    <!-- Postulacion 2 -->
    <div class="postulacion-card">
        <div class="postulacion-info">
            <h4>Analista de Datos</h4>
            <p>DataVision SA | La Libertad | Postulada el 08/09/2026</p>
        </div>
        <span class="badge badge-warning">En revision</span>
    </div>

    <!-- Postulacion 3 -->
    <div class="postulacion-card">
        <div class="postulacion-info">
            <h4>Ingeniero de Software</h4>
            <p>NetSolutions SV | San Salvador | Postulada el 05/09/2026</p>
        </div>
        <span class="badge badge-primary">Enviada</span>
    </div>

    <!-- Postulacion 4 -->
    <div class="postulacion-card">
        <div class="postulacion-info">
            <h4>Responsable de Soporte TI</h4>
            <p>Grupo Meridian | Santa Ana | Postulada el 01/09/2026</p>
        </div>
        <span class="badge badge-danger">Finalizada</span>
    </div>

    <!-- Postulacion 5 -->
    <div class="postulacion-card">
        <div class="postulacion-info">
            <h4>Desarrollador Frontend</h4>
            <p>Creative Studio | San Salvador | Postulada el 28/08/2026</p>
        </div>
        <span class="badge badge-success">Entrevista</span>
    </div>
</div>

<%@ include file="/WEB-INF/views/footer.jsp" %>
