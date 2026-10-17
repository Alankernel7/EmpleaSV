<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%-- ============================================================
     Menu de navegacion compartido por todas las vistas.
     - Enlaces agrupados por rol (candidato / empresa / admin).
     - Estado activo calculado a partir de la ruta original de la
       peticion: request.getServletPath() conserva la ruta del
       servlet aunque se haya hecho forward al JSP.
     - Saludo compacto en la barra; el nombre completo, el correo y
       el rol viven solo dentro del menu de cuenta.
     ============================================================ --%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="rutaActual" value="${pageContext.request.servletPath}" />
<c:set var="qsActual" value="${pageContext.request.queryString}" />

<c:set var="esInicio" value="${rutaActual == '/inicio'}" />
<c:set var="esEmpresas" value="${rutaActual == '/empresas'}" />
<c:set var="esAyuda" value="${rutaActual == '/ayuda.jsp'}" />
<c:set var="esPerfil" value="${rutaActual == '/perfil' && not fn:contains(qsActual, 'accion=curriculum')}" />
<c:set var="esCurriculum" value="${rutaActual == '/perfil' && fn:contains(qsActual, 'accion=curriculum')}" />
<c:set var="esOfertas" value="${rutaActual == '/ofertas' && not fn:contains(qsActual, 'mias=1') && not fn:contains(qsActual, 'accion=registrar')}" />
<c:set var="esMisOfertas" value="${rutaActual == '/ofertas' && fn:contains(qsActual, 'mias=1')}" />
<c:set var="esNuevaOferta" value="${rutaActual == '/ofertas' && fn:contains(qsActual, 'accion=registrar')}" />
<c:set var="esPanelEmpresa" value="${rutaActual == '/panel-empresa'}" />
<c:set var="esPerfilEmpresa" value="${rutaActual == '/perfil-empresa'}" />

<c:set var="esAdminDashboard" value="${rutaActual == '/admin'}" />
<c:set var="esAdminUsuarios" value="${rutaActual == '/admin' && fn:contains(qsActual, 'seccion=usuarios')}" />
<c:set var="esAdminEmpresas" value="${rutaActual == '/admin' && fn:contains(qsActual, 'seccion=empresas')}" />
<c:set var="esAdminOfertas" value="${rutaActual == '/admin' && fn:contains(qsActual, 'seccion=ofertas')}" />

<c:set var="esCandidato" value="${rolUsuario == 'USUARIO'}" />
<c:set var="esEmpresaRol" value="${rolUsuario == 'EMPRESA'}" />
<c:set var="esAdmin" value="${rolUsuario == 'ADMINISTRADOR'}" />

<c:choose>
    <c:when test="${esEmpresaRol}"><c:set var="etiquetaRol" value="Empresa" /></c:when>
    <c:when test="${esAdmin}"><c:set var="etiquetaRol" value="Administrador" /></c:when>
    <c:otherwise><c:set var="etiquetaRol" value="Candidato" /></c:otherwise>
</c:choose>

<c:set var="nombreBarra" value="${empty nombreUsuario ? '' : (fn:contains(nombreUsuario, ' ') ? fn:substringBefore(nombreUsuario, ' ') : nombreUsuario)}" />

<nav class="navbar" id="navbar">
    <div class="nav-container">
        <a class="nav-brand" href="${ctx}/inicio">
            <span class="nav-brand-icon" aria-hidden="true">E</span>
            <span class="nav-brand-texto">EmpleaSV</span>
        </a>

        <button class="nav-toggle" type="button"
                onclick="document.getElementById('navCenter').classList.toggle('active')"
                aria-label="Abrir menu" aria-controls="navCenter" aria-expanded="false">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/></svg>
        </button>

        <div class="nav-center" id="navCenter">
            <c:if test="${esAdmin}">
                <div class="nav-group">
                    <a class="nav-link nav-link-tarea${esAdminDashboard && not esAdminUsuarios && not esAdminEmpresas && not esAdminOfertas ? ' active' : ''}" href="${ctx}/admin"
                       ${esAdminDashboard && not esAdminUsuarios && not esAdminEmpresas && not esAdminOfertas ? 'aria-current="page"' : ''}>Dashboard</a>
                    <a class="nav-link nav-link-tarea${esAdminUsuarios ? ' active' : ''}" href="${ctx}/admin?seccion=usuarios"
                       ${esAdminUsuarios ? 'aria-current="page"' : ''}>Usuarios</a>
                    <a class="nav-link nav-link-tarea${esAdminEmpresas ? ' active' : ''}" href="${ctx}/admin?seccion=empresas"
                       ${esAdminEmpresas ? 'aria-current="page"' : ''}>Empresas</a>
                    <a class="nav-link nav-link-tarea${esAdminOfertas ? ' active' : ''}" href="${ctx}/admin?seccion=ofertas"
                       ${esAdminOfertas ? 'aria-current="page"' : ''}>Ofertas</a>
                </div>
                <span class="nav-sep" aria-hidden="true"></span>
            </c:if>

            <div class="nav-group">
                <a class="nav-link${esInicio ? ' active' : ''}" href="${ctx}/inicio"
                   ${esInicio ? 'aria-current="page"' : ''}>Inicio</a>

                <c:if test="${not esAdmin}">
                    <a class="nav-link${esOfertas || esNuevaOferta ? ' active' : ''}" href="${ctx}/ofertas?format=jsp"
                       ${esOfertas ? 'aria-current="page"' : ''}>Ofertas</a>

                    <a class="nav-link${esEmpresas ? ' active' : ''}" href="${ctx}/empresas"
                       ${esEmpresas ? 'aria-current="page"' : ''}>Empresas</a>
                </c:if>

                <a class="nav-link${esAyuda ? ' active' : ''}" href="${ctx}/ayuda.jsp"
                   ${esAyuda ? 'aria-current="page"' : ''}>Ayuda</a>
            </div>

            <c:if test="${esCandidato or esEmpresaRol}">
                <span class="nav-sep" aria-hidden="true"></span>

                <div class="nav-group">
                    <c:if test="${esCandidato}">
                        <a class="nav-link nav-link-tarea${esCurriculum ? ' active' : ''}" href="${ctx}/perfil?accion=curriculum"
                           ${esCurriculum ? 'aria-current="page"' : ''}>Mi curriculum</a>
                    </c:if>
                    <c:if test="${esEmpresaRol}">
                        <a class="nav-link nav-link-tarea${esMisOfertas ? ' active' : ''}" href="${ctx}/ofertas?format=jsp&mias=1"
                           ${esMisOfertas ? 'aria-current="page"' : ''}>Mis ofertas</a>
                        <a class="nav-link nav-link-tarea${esNuevaOferta ? ' active' : ''}" href="${ctx}/ofertas?format=jsp&accion=registrar"
                           ${esNuevaOferta ? 'aria-current="page"' : ''}>Nueva oferta</a>
                    </c:if>
                </div>
            </c:if>
        </div>

        <div class="nav-actions">
            <c:choose>
                <c:when test="${not empty usuarioLogueado}">
                    <div class="nav-account" id="navAccount">
                        <button type="button" class="nav-account-btn" id="btn-cuenta"
                                aria-haspopup="true" aria-expanded="false" aria-controls="menu-cuenta"
                                onclick="toggleMenuCuenta()">
                            <span class="nav-user-avatar"><c:out value="${empty nombreUsuario ? 'U' : fn:toUpperCase(fn:substring(nombreUsuario, 0, 1))}" /></span>
                            <span class="nav-user-text">Hola, <strong><c:out value="${nombreBarra}" /></strong></span>
                            <svg class="nav-caret" xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none"
                                 viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/></svg>
                        </button>

                        <div class="nav-menu" id="menu-cuenta" hidden="hidden">
                            <div class="nav-menu-head">
                                <span class="nav-user-avatar nav-user-avatar-lg"><c:out value="${empty nombreUsuario ? 'U' : fn:toUpperCase(fn:substring(nombreUsuario, 0, 1))}" /></span>
                                <span class="nav-menu-cuenta">
                                    <span class="nav-menu-nombre"><c:out value="${nombreUsuario}" /></span>
                                    <span class="nav-menu-email"><c:out value="${usuarioLogueado.email}" /></span>
                                    <span class="nav-rol-badge nav-rol-${fn:toLowerCase(etiquetaRol)}"><c:out value="${etiquetaRol}" /></span>
                                </span>
                            </div>

                            <div class="nav-menu-titulo">Mi cuenta</div>
                            <div class="nav-menu-group">
                                <c:if test="${esAdmin}">
                                    <a class="nav-menu-link${esAdminDashboard && not esAdminUsuarios && not esAdminEmpresas && not esAdminOfertas ? ' active' : ''}" href="${ctx}/admin">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M4 6a8 3 0 1016 0A8 3 0 004 6zm0 6c0 1.66 3.58 3 8 3s8-1.34 8-3M4 18c0 1.66 3.58 3 8 3s8-1.34 8-3"/></svg>
                                        Panel administrador
                                    </a>
                                    <a class="nav-menu-link${esAdminUsuarios ? ' active' : ''}" href="${ctx}/admin?seccion=usuarios">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M17 20h5v-1a4 4 0 00-4-4h-1M9 20H4v-1a4 4 0 014-4h1m6-4a4 4 0 11-8 0 4 4 0 018 0zm6 0a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                                        Usuarios
                                    </a>
                                    <a class="nav-menu-link${esAdminOfertas ? ' active' : ''}" href="${ctx}/admin?seccion=ofertas">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M21 13V5a2 2 0 00-2-2H5a2 2 0 00-2 2v14a2 2 0 002 2h8M7 10h4m-4 4h2m7-1v6m3-3h-6"/></svg>
                                        Gestionar ofertas
                                    </a>
                                </c:if>
                                <c:if test="${esCandidato}">
                                    <a class="nav-menu-link${esPerfil ? ' active' : ''}" href="${ctx}/perfil"
                                       ${esPerfil ? 'aria-current="page"' : ''}>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
                                        Mi perfil
                                    </a>
                                    <a class="nav-menu-link${esCurriculum ? ' active' : ''}" href="${ctx}/perfil?accion=curriculum"
                                       ${esCurriculum ? 'aria-current="page"' : ''}>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
                                        Mi curriculum
                                    </a>
                                    <a class="nav-menu-link" href="${ctx}/mis-postulaciones.jsp">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/></svg>
                                        Mis postulaciones
                                    </a>
                                    <a class="nav-menu-link" href="${ctx}/ofertas-guardadas.jsp">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"/></svg>
                                        Ofertas guardadas
                                    </a>
                                </c:if>
                                <c:if test="${esEmpresaRol}">
                                    <a class="nav-menu-link${esPanelEmpresa ? ' active' : ''}" href="${ctx}/panel-empresa"
                                       ${esPanelEmpresa ? 'aria-current="page"' : ''}>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-4 0a1 1 0 01-1-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 01-1 1h-2z"/></svg>
                                        Panel empresa
                                    </a>
                                    <a class="nav-menu-link${esPerfilEmpresa ? ' active' : ''}" href="${ctx}/perfil-empresa"
                                       ${esPerfilEmpresa ? 'aria-current="page"' : ''}>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/></svg>
                                        Perfil empresa
                                    </a>
                                </c:if>
                            </div>

                            <a class="nav-menu-link nav-menu-salir" href="${ctx}/auth?accion=logout">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/></svg>
                                Cerrar sesion
                            </a>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <a href="${ctx}/auth?accion=login" class="btn btn-ghost btn-sm">Iniciar sesion</a>
                    <a href="${ctx}/auth?accion=registro" class="btn btn-primary btn-sm">Crear cuenta</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</nav>

<script>
    (function () {
        'use strict';
        var cuenta = document.getElementById('navAccount');
        var btn = document.getElementById('btn-cuenta');
        var menu = document.getElementById('menu-cuenta');
        if (!cuenta || !btn || !menu) {
            return;
        }

        function cerrar() {
            menu.hidden = true;
            btn.setAttribute('aria-expanded', 'false');
        }

        function alternar() {
            var abierto = !menu.hidden;
            menu.hidden = abierto;
            btn.setAttribute('aria-expanded', String(!abierto));
        }

        window.toggleMenuCuenta = alternar;

        document.addEventListener('click', function (e) {
            if (!menu.hidden && !cuenta.contains(e.target)) {
                cerrar();
            }
        });

        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') {
                cerrar();
            }
        });

        menu.addEventListener('click', function (e) {
            if (e.target && e.target.closest && e.target.closest('a')) {
                cerrar();
            }
        });

        var toggle = document.querySelector('.nav-toggle');
        var centro = document.getElementById('navCenter');
        if (toggle && centro) {
            toggle.addEventListener('click', function () {
                toggle.setAttribute('aria-expanded', String(centro.classList.contains('active')));
            });
            centro.addEventListener('click', function (e) {
                if (e.target && e.target.closest && e.target.closest('a')) {
                    centro.classList.remove('active');
                    toggle.setAttribute('aria-expanded', 'false');
                }
            });
        }
    })();
</script>
