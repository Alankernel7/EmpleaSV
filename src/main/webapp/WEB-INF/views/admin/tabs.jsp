<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%-- Barra de secciones del panel administrativo --%>
<div class="admin-tabs" role="navigation" aria-label="Secciones administrativas">
    <a class="admin-tab${seccionActual == 'dashboard' ? ' active' : ''}"
       href="${ctx}/admin">Dashboard</a>
    <a class="admin-tab${seccionActual == 'usuarios' ? ' active' : ''}"
       href="${ctx}/admin?seccion=usuarios">Usuarios</a>
    <a class="admin-tab${seccionActual == 'empresas' ? ' active' : ''}"
       href="${ctx}/admin?seccion=empresas">Empresas</a>
    <a class="admin-tab${seccionActual == 'ofertas' ? ' active' : ''}"
       href="${ctx}/admin?seccion=ofertas">Ofertas</a>
</div>
