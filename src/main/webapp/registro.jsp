<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Crear cuenta - EmpleaSV</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css?v=3">
</head>
<body>

<c:set var="esEmpresa" value="${rol == 'EMPRESA'}" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="WEB-INF/views/nav.jsp" />

<main>
<div class="auth-page">
    <div class="auth-card auth-card-wide">
        <div class="auth-head">
            <span class="auth-eyebrow">Paso 1 de 1</span>
            <h1>Crea tu cuenta</h1>
            <p class="subtitle">Elige el tipo de cuenta y completa solo los datos que corresponden.</p>
        </div>

        <c:if test="${not empty errores}">
            <div class="alert alert-error" role="alert" aria-live="polite">
                <div class="alert-contenido">
                    <strong>Revisa los siguientes datos:</strong>
                    <ul class="alert-list">
                        <c:forEach var="error" items="${errores}">
                            <li><c:out value="${error}" /></li>
                        </c:forEach>
                    </ul>
                </div>
            </div>
        </c:if>

        <form id="form-registro" action="${ctx}/auth" method="post" enctype="multipart/form-data">
            <input type="hidden" name="accion" value="registro">
            <input type="hidden" id="tipo-cuenta" name="rol" value="${esEmpresa ? 'EMPRESA' : 'USUARIO'}">

            <!-- Selector visual de tipo de cuenta -->
            <div class="role-selector" role="group" aria-label="Tipo de cuenta">
                <button type="button" id="btn-candidato"
                        class="role-option ${esEmpresa ? '' : 'active'}"
                        aria-pressed="${esEmpresa ? 'false' : 'true'}"
                        onclick="seleccionarTipo('USUARIO')">
                    <span class="role-option-icon" aria-hidden="true">
                        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
                    </span>
                    <span class="role-option-check" aria-hidden="true"></span>
                    <strong class="role-option-title">Candidato</strong>
                    <span class="role-option-desc">Busco empleo y quiero crear mi perfil</span>
                </button>

                <button type="button" id="btn-empresa"
                        class="role-option ${esEmpresa ? 'active' : ''}"
                        aria-pressed="${esEmpresa ? 'true' : 'false'}"
                        onclick="seleccionarTipo('EMPRESA')">
                    <span class="role-option-icon" aria-hidden="true">
                        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/></svg>
                    </span>
                    <span class="role-option-check" aria-hidden="true"></span>
                    <strong class="role-option-title">Empresa</strong>
                    <span class="role-option-desc">Publico ofertas y contrato talento</span>
                </button>
            </div>

            <!-- ==================== CANDIDATO ==================== -->
            <fieldset id="panel-candidato" class="form-section ${esEmpresa ? '' : 'active'}"
                      ${esEmpresa ? 'disabled' : ''}>
                <legend class="form-legend">Datos del candidato</legend>

                <div class="form-row">
                    <div class="form-group form-group-full">
                        <label class="form-label" for="c-nombre">Nombre completo <span class="req">*</span></label>
                        <input type="text" id="c-nombre" name="nombre" class="form-control"
                               placeholder="Tu nombre completo" value="${fn:escapeXml(nombre)}" required
                               maxlength="150" autocomplete="name" aria-required="true"
                               ${not empty erroresCampo.nombre ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.nombre}">
                            <span class="field-error"><c:out value="${erroresCampo.nombre}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="c-email">Correo electronico <span class="req">*</span></label>
                        <input type="email" id="c-email" name="email" class="form-control"
                               placeholder="tu@correo.com" value="${fn:escapeXml(email)}" required
                               maxlength="150" autocomplete="email" aria-required="true"
                               ${not empty erroresCampo.email ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.email}">
                            <span class="field-error"><c:out value="${erroresCampo.email}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="c-telefono">Telefono <span class="req">*</span></label>
                        <input type="tel" id="c-telefono" name="telefono" class="form-control"
                               placeholder="7000-0000" value="${fn:escapeXml(telefono)}" required
                               maxlength="30" autocomplete="tel" aria-required="true"
                               ${not empty erroresCampo.telefono ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.telefono}">
                            <span class="field-error"><c:out value="${erroresCampo.telefono}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="c-pass">Contrasena <span class="req">*</span></label>
                        <input type="password" id="c-pass" name="password" class="form-control"
                               placeholder="Minimo 6 caracteres" required minlength="6"
                               autocomplete="new-password" aria-required="true"
                               ${not empty erroresCampo.password ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.password}">
                            <span class="field-error"><c:out value="${erroresCampo.password}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="c-pass2">Confirmar contrasena <span class="req">*</span></label>
                        <input type="password" id="c-pass2" name="password2" class="form-control"
                               placeholder="Repite tu contrasena" required minlength="6"
                               autocomplete="new-password" aria-required="true"
                               ${not empty erroresCampo.password2 ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.password2}">
                            <span class="field-error"><c:out value="${erroresCampo.password2}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group form-group-full">
                        <label class="form-label" for="c-direccion">Direccion de residencia <span class="req">*</span></label>
                        <input type="text" id="c-direccion" name="direccion" class="form-control"
                               placeholder="Colonia, calle y numero" value="${fn:escapeXml(direccion)}" required
                               maxlength="255" autocomplete="street-address" aria-required="true"
                               ${not empty erroresCampo.direccion ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.direccion}">
                            <span class="field-error"><c:out value="${erroresCampo.direccion}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="c-municipio">Municipio</label>
                        <input type="text" id="c-municipio" name="municipio" class="form-control"
                               placeholder="Ej. San Salvador" value="${fn:escapeXml(municipio)}"
                               maxlength="100"
                               ${not empty erroresCampo.municipio ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.municipio}">
                            <span class="field-error"><c:out value="${erroresCampo.municipio}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="c-departamento">Departamento</label>
                        <input type="text" id="c-departamento" name="departamento" class="form-control"
                               placeholder="Ej. San Salvador" value="${fn:escapeXml(departamento)}"
                               maxlength="100"
                               ${not empty erroresCampo.departamento ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.departamento}">
                            <span class="field-error"><c:out value="${erroresCampo.departamento}"/></span>
                        </c:if>
                    </div>
                </div>

                <p class="form-footnote">
                    La fotografia y el curriculum se cargan despues, desde
                    <strong>Mi perfil</strong>.
                </p>
            </fieldset>

            <!-- ==================== EMPRESA ==================== -->
            <fieldset id="panel-empresa" class="form-section ${esEmpresa ? 'active' : ''}"
                      ${esEmpresa ? '' : 'disabled'}>
                <legend class="form-legend">Datos de la empresa</legend>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="e-responsable">Nombre del responsable <span class="req">*</span></label>
                        <input type="text" id="e-responsable" name="nombre_responsable" class="form-control"
                               placeholder="Quien administra la cuenta" value="${fn:escapeXml(nombre_responsable)}" required
                               maxlength="150" autocomplete="name" aria-required="true"
                               ${not empty erroresCampo.nombre_responsable ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.nombre_responsable}">
                            <span class="field-error"><c:out value="${erroresCampo.nombre_responsable}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="e-email">Correo corporativo <span class="req">*</span></label>
                        <input type="email" id="e-email" name="email" class="form-control"
                               placeholder="empresa@correo.com" value="${fn:escapeXml(email)}" required
                               maxlength="150" autocomplete="email" aria-required="true"
                               ${not empty erroresCampo.email ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.email}">
                            <span class="field-error"><c:out value="${erroresCampo.email}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="e-pass">Contrasena <span class="req">*</span></label>
                        <input type="password" id="e-pass" name="password" class="form-control"
                               placeholder="Minimo 6 caracteres" required minlength="6"
                               autocomplete="new-password" aria-required="true"
                               ${not empty erroresCampo.password ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.password}">
                            <span class="field-error"><c:out value="${erroresCampo.password}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="e-pass2">Confirmar contrasena <span class="req">*</span></label>
                        <input type="password" id="e-pass2" name="password2" class="form-control"
                               placeholder="Repite tu contrasena" required minlength="6"
                               autocomplete="new-password" aria-required="true"
                               ${not empty erroresCampo.password2 ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.password2}">
                            <span class="field-error"><c:out value="${erroresCampo.password2}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group form-group-full">
                        <label class="form-label" for="e-empresa">Nombre legal o comercial <span class="req">*</span></label>
                        <input type="text" id="e-empresa" name="nombre_empresa" class="form-control"
                               placeholder="Nombre de tu empresa" value="${fn:escapeXml(nombre_empresa)}" required
                               maxlength="150" aria-required="true"
                               ${not empty erroresCampo.nombre_empresa ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.nombre_empresa}">
                            <span class="field-error"><c:out value="${erroresCampo.nombre_empresa}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="e-nit">NIT</label>
                        <input type="text" id="e-nit" name="nit" class="form-control"
                               placeholder="0614-120587-102-3" value="${fn:escapeXml(nit)}" maxlength="50"
                               ${not empty erroresCampo.nit ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.nit}">
                            <span class="field-error"><c:out value="${erroresCampo.nit}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="e-telefono">Telefono de contacto <span class="req">*</span></label>
                        <input type="tel" id="e-telefono" name="telefono_empresa" class="form-control"
                               placeholder="7000-0000" value="${fn:escapeXml(telefono_empresa)}" required
                               maxlength="30" autocomplete="tel" aria-required="true"
                               ${not empty erroresCampo.telefono_empresa ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.telefono_empresa}">
                            <span class="field-error"><c:out value="${erroresCampo.telefono_empresa}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group form-group-full">
                        <label class="form-label" for="e-direccion">Direccion empresarial <span class="req">*</span></label>
                        <input type="text" id="e-direccion" name="direccion_empresa" class="form-control"
                               placeholder="Colonia, calle y numero" value="${fn:escapeXml(direccion_empresa)}" required
                               maxlength="255" autocomplete="street-address" aria-required="true"
                               ${not empty erroresCampo.direccion_empresa ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.direccion_empresa}">
                            <span class="field-error"><c:out value="${erroresCampo.direccion_empresa}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="e-municipio">Municipio <span class="req">*</span></label>
                        <input type="text" id="e-municipio" name="municipio" class="form-control"
                               placeholder="Ej. Santa Ana" value="${fn:escapeXml(municipio)}" required
                               maxlength="100" aria-required="true"
                               ${not empty erroresCampo.municipio ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.municipio}">
                            <span class="field-error"><c:out value="${erroresCampo.municipio}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="e-departamento">Departamento <span class="req">*</span></label>
                        <input type="text" id="e-departamento" name="departamento" class="form-control"
                               placeholder="Ej. Santa Ana" value="${fn:escapeXml(departamento)}" required
                               maxlength="100" aria-required="true"
                               ${not empty erroresCampo.departamento ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.departamento}">
                            <span class="field-error"><c:out value="${erroresCampo.departamento}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="e-sector">Sector o categoria <span class="req">*</span></label>
                        <input type="text" id="e-sector" name="sector" class="form-control"
                               placeholder="Ej. Tecnologia, Salud, Comercio" value="${fn:escapeXml(sector)}" required
                               maxlength="100" aria-required="true"
                               ${not empty erroresCampo.sector ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.sector}">
                            <span class="field-error"><c:out value="${erroresCampo.sector}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="e-web">Sitio web</label>
                        <input type="url" id="e-web" name="sitio_web" class="form-control"
                               placeholder="https://tuempresa.com" value="${fn:escapeXml(sitio_web)}" maxlength="255"
                               ${not empty erroresCampo.sitio_web ? 'aria-invalid="true"' : ''}>
                        <c:if test="${not empty erroresCampo.sitio_web}">
                            <span class="field-error"><c:out value="${erroresCampo.sitio_web}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group form-group-full">
                        <label class="form-label" for="e-descripcion">Descripcion de la empresa</label>
                        <textarea id="e-descripcion" name="descripcion" class="form-control" rows="4"
                                  maxlength="5000"
                                  placeholder="Que hace tu empresa, a que se dedica y que perfiles buscas"
                                  ${not empty erroresCampo.descripcion ? 'aria-invalid="true"' : ''}>${fn:escapeXml(descripcion)}</textarea>
                        <span class="form-hint">Maximo 5000 caracteres.</span>
                        <c:if test="${not empty erroresCampo.descripcion}">
                            <span class="field-error"><c:out value="${erroresCampo.descripcion}"/></span>
                        </c:if>
                    </div>

                    <div class="form-group form-group-full">
                        <label class="form-label" for="e-logo">Logotipo</label>
                        <div class="file-input" id="e-logo-file-input">
                            <input type="file" id="e-logo" name="logo" class="file-input-native"
                                   accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp">
                            <label for="e-logo" class="file-input-btn">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 10-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486"/></svg>
                                Seleccionar imagen
                            </label>
                            <span class="file-input-name" id="e-logo-file-name">Ningun archivo seleccionado</span>
                        </div>
                        <script>
                            (function () {
                                var input = document.getElementById('e-logo');
                                var cont = document.getElementById('e-logo-file-input');
                                var nombre = document.getElementById('e-logo-file-name');
                                if (!input || !cont || !nombre) return;
                                input.addEventListener('change', function () {
                                    var archivo = input.files && input.files.length ? input.files[0].name : '';
                                    nombre.textContent = archivo || 'Ningun archivo seleccionado';
                                    cont.classList.toggle('has-file', !!archivo);
                                });
                            })();
                        </script>
                        <span class="form-hint">JPG, PNG o WEBP. Maximo 2 MB. Puedes agregarlo despues desde el perfil.</span>
                    </div>
                </div>

            </fieldset>

            <button type="submit" class="btn btn-primary btn-lg btn-block" id="btn-registro">
                <span id="btn-registro-texto">${esEmpresa ? 'Crear cuenta de empresa' : 'Crear cuenta de candidato'}</span>
            </button>
        </form>

        <div class="auth-footer">
            Ya tienes una cuenta? <a href="${ctx}/auth?accion=login">Inicia sesion</a>
        </div>
    </div>
</div>

<script>
    (function () {
        'use strict';

        var textos = {
            USUARIO: 'Crear cuenta de candidato',
            EMPRESA: 'Crear cuenta de empresa'
        };

        var oculto = document.getElementById('tipo-cuenta');

        function seleccionarTipo(tipo) {
            var esEmpresa = (tipo === 'EMPRESA');
            oculto.value = tipo;

            var btnCandidato = document.getElementById('btn-candidato');
            var btnEmpresa = document.getElementById('btn-empresa');
            btnCandidato.classList.toggle('active', !esEmpresa);
            btnEmpresa.classList.toggle('active', esEmpresa);
            btnCandidato.setAttribute('aria-pressed', String(!esEmpresa));
            btnEmpresa.setAttribute('aria-pressed', String(esEmpresa));

            var candidato = document.getElementById('panel-candidato');
            var empresa = document.getElementById('panel-empresa');

            candidato.classList.toggle('active', !esEmpresa);
            empresa.classList.toggle('active', esEmpresa);

            /* El panel inactivo queda deshabilitado: sus campos no se envian
               ni participan en la validacion del navegador. */
            candidato.disabled = esEmpresa;
            empresa.disabled = !esEmpresa;

            document.getElementById('btn-registro-texto').textContent =
                esEmpresa ? textos.EMPRESA : textos.USUARIO;
        }

        window.seleccionarTipo = seleccionarTipo;
    })();
</script>

<%@ include file="WEB-INF/views/footer.jsp" %>
