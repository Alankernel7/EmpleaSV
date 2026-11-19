<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<jsp:include page="header.jsp">
    <jsp:param name="tituloPagina" value="Mi perfil empresarial"/>
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="container section-sm">
    <div class="page-header">
        <div>
            <h1>Mi perfil empresarial</h1>
            <p class="text-muted" style="margin-top:0.35rem;">Datos de la organizacion que aparecen junto a tus ofertas de empleo.</p>
        </div>
        <a href="${ctx}/panel-empresa" class="btn btn-secondary">Panel de empresa</a>
    </div>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success"><c:out value="${mensajeExito}"/></div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-error"><c:out value="${mensajeError}"/></div>
    </c:if>

    <c:if test="${not empty errores}">
        <div class="alert alert-error">
            <c:forEach var="error" items="${errores}">
                <div><c:out value="${error}" /></div>
            </c:forEach>
        </div>
    </c:if>

    <c:if test="${perfilIncompleto}">
        <div class="aviso aviso-aviso">
            <div>
                <strong>Perfil incompleto</strong>
                Completa los datos obligatorios de la empresa antes de publicar ofertas de empleo.
            </div>
            <a href="#datos-empresa" class="btn btn-primary btn-sm" style="margin-left:auto;">Completar</a>
        </div>
    </c:if>

    <div class="grid-2">
        <!-- Logotipo -->
        <div class="card">
            <div class="card-header">
                <h2 class="card-title">Logotipo</h2>
            </div>
            <div class="card-body">
                <div class="flex-center" style="margin-bottom:1.25rem;">
                    <c:choose>
                        <c:when test="${not empty perfil.logo}">
                            <img src="${ctx}/archivo?id=${perfil.logo.id}&inline=1"
                                 alt="Logotipo de ${fn:escapeXml(perfil.nombreEmpresa)}"
                                 class="profile-avatar"
                                 style="width:120px;height:120px;object-fit:contain;">
                        </c:when>
                        <c:otherwise>
                            <div class="profile-avatar flex-center"
                                 style="width:120px;height:120px;background:var(--primary-light);color:var(--primary);font-size:2.5rem;font-weight:700;">
                                <c:out value="${empty perfil.nombreEmpresa ? 'E' : fn:toUpperCase(fn:substring(perfil.nombreEmpresa, 0, 1))}" />
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <form action="${ctx}/perfil-empresa" method="post" enctype="multipart/form-data">
                    <input type="hidden" name="accion" value="subir-logo">
                    <div class="form-group">
                        <label class="form-label" for="logo">Cambiar logotipo</label>
                        <div class="file-input" id="logo-file-input">
                            <input type="file" id="logo" name="logo" class="file-input-native"
                                   accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp">
                            <label for="logo" class="file-input-btn">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 10-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486"/></svg>
                                Seleccionar imagen
                            </label>
                            <span class="file-input-name" id="logo-file-name">Ningun archivo seleccionado</span>
                        </div>
                        <script>
                            (function () {
                                var input = document.getElementById('logo');
                                var cont = document.getElementById('logo-file-input');
                                var nombre = document.getElementById('logo-file-name');
                                if (!input || !cont || !nombre) return;
                                input.addEventListener('change', function () {
                                    var archivo = input.files && input.files.length ? input.files[0].name : '';
                                    nombre.textContent = archivo || 'Ningun archivo seleccionado';
                                    cont.classList.toggle('has-file', !!archivo);
                                });
                            })();
                        </script>
                        <span class="form-hint">JPG, PNG o WEBP. Maximo 2 MB.</span>
                    </div>
                    <div class="fila-acciones">
                        <button type="submit" class="btn btn-primary btn-sm">Subir logotipo</button>
                    </div>
                </form>

                <c:if test="${not empty perfil.logo}">
                    <form action="${ctx}/perfil-empresa" method="post" style="margin-top:0.75rem;"
                          onsubmit="return confirm('Eliminar el logotipo?');">
                        <input type="hidden" name="accion" value="eliminar-logo">
                        <button type="submit" class="btn btn-danger btn-sm">Eliminar logotipo</button>
                    </form>
                </c:if>
            </div>
        </div>

        <!-- Resumen -->
        <div class="card">
            <div class="card-header">
                <h2 class="card-title">Resumen</h2>
            </div>
            <div class="card-body">
                <div class="datos-lista" style="grid-template-columns:1fr;">
                    <dl class="datos-item">
                        <dt>Empresa</dt>
                        <dd><c:out value="${perfil.nombreEmpresa}" /></dd>
                    </dl>
                    <dl class="datos-item">
                        <dt>Sector</dt>
                        <dd><c:out value="${empty perfil.sector ? 'Sin registrar' : perfil.sector}" /></dd>
                    </dl>
                    <dl class="datos-item">
                        <dt>Responsable de la cuenta</dt>
                        <dd><c:out value="${perfil.nombreResponsable}" /></dd>
                    </dl>
                    <dl class="datos-item">
                        <dt>Correo de contacto</dt>
                        <dd><c:out value="${usuario.email}" /></dd>
                    </dl>
                </div>

                <div class="fila-acciones" style="margin-top:1rem;">
                    <a href="${ctx}/ofertas?format=jsp&accion=registrar" class="btn btn-primary btn-sm">Publicar oferta</a>
                    <a href="${ctx}/panel-empresa" class="btn btn-secondary btn-sm">Ver mis ofertas</a>
                </div>
            </div>
        </div>
    </div>

    <!-- Datos de la empresa -->
    <div class="card" id="datos-empresa" style="margin-top:1.5rem; max-width:860px;">
        <div class="card-header">
            <h2 class="card-title">Datos de la empresa</h2>
        </div>
        <div class="card-body">
            <form action="${ctx}/perfil-empresa" method="post">
                <input type="hidden" name="accion" value="${perfilIncompleto ? 'crear' : 'actualizar'}">

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="nombre_responsable">Nombre del responsable *</label>
                        <input type="text" id="nombre_responsable" name="nombre_responsable"
                               class="form-control" required maxlength="150"
                               value="${fn:escapeXml(formNombreResponsable)}">
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="email">Correo electronico *</label>
                        <input type="email" id="email" name="email" class="form-control" required
                               maxlength="150" value="${fn:escapeXml(formEmail)}">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="nombre_empresa">Nombre legal o comercial *</label>
                        <input type="text" id="nombre_empresa" name="nombre_empresa"
                               class="form-control" required maxlength="150"
                               value="${fn:escapeXml(formNombreEmpresa)}">
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="sector">Sector *</label>
                        <input type="text" id="sector" name="sector" class="form-control"
                               maxlength="100" value="${fn:escapeXml(formSector)}"
                               placeholder="Ej. Tecnologia, Salud, Comercio">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="nit">NIT</label>
                        <input type="text" id="nit" name="nit" class="form-control" maxlength="50"
                               value="${fn:escapeXml(formNit)}" placeholder="0614-120587-102-3">
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="telefono_empresa">Telefono *</label>
                        <input type="tel" id="telefono_empresa" name="telefono_empresa"
                               class="form-control" maxlength="30"
                               value="${fn:escapeXml(formTelefono)}" placeholder="7000-0000">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="direccion_empresa">Direccion *</label>
                        <input type="text" id="direccion_empresa" name="direccion_empresa"
                               class="form-control" required maxlength="255"
                               value="${fn:escapeXml(formDireccion)}">
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="municipio">Municipio</label>
                        <input type="text" id="municipio" name="municipio" class="form-control"
                               maxlength="100" value="${fn:escapeXml(formMunicipio)}">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="departamento">Departamento</label>
                        <input type="text" id="departamento" name="departamento"
                               class="form-control" maxlength="100"
                               value="${fn:escapeXml(formDepartamento)}">
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="sitio_web">Sitio web</label>
                        <input type="url" id="sitio_web" name="sitio_web" class="form-control"
                               maxlength="255" value="${fn:escapeXml(formSitioWeb)}"
                               placeholder="https://tuempresa.com">
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="descripcion">Descripcion de la empresa</label>
                    <textarea id="descripcion" name="descripcion" class="form-control" rows="5"
                              maxlength="5000"
                              placeholder="Que hace tu empresa, a que se dedica y que tipos de perfiles buscas">${fn:escapeXml(formDescripcion)}</textarea>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-primary btn-lg">Guardar cambios</button>
                    <a href="${ctx}/panel-empresa" class="btn btn-secondary">Cancelar</a>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="footer.jsp" %>
