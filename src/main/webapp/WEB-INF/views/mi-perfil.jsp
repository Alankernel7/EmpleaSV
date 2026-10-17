<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="header.jsp">
    <jsp:param name="tituloPagina" value="Mi perfil"/>
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="container section-sm">
    <div class="page-header">
        <div>
            <h1>Mi perfil</h1>
            <p class="text-muted" style="margin-top:0.35rem;">Informacion personal que comparten las empresas contigo.</p>
        </div>
        <a href="${ctx}/perfil?accion=curriculum" class="btn btn-secondary">Mi curriculum</a>
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

    <div class="grid-2">
        <!-- Fotografia -->
        <div class="card">
            <div class="card-header">
                <h2 class="card-title">Fotografia de perfil</h2>
            </div>
            <div class="card-body">
                <div class="flex-center" style="margin-bottom:1.25rem;">
                    <c:choose>
                        <c:when test="${not empty perfil.fotoPerfil}">
                            <img src="${ctx}/archivo?id=${perfil.fotoPerfil.id}&inline=1"
                                 alt="Fotografia de ${fn:escapeXml(usuario.nombre)}"
                                 class="profile-avatar"
                                 style="width:120px;height:120px;object-fit:cover;">
                        </c:when>
                        <c:otherwise>
                            <div class="profile-avatar flex-center"
                                 style="width:120px;height:120px;background:var(--primary-light);color:var(--primary);font-size:2.5rem;font-weight:700;">
                                <c:out value="${empty usuario.nombre ? 'U' : fn:substring(usuario.nombre, 0, 1)}" />
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <form action="${ctx}/perfil" method="post" enctype="multipart/form-data">
                    <input type="hidden" name="accion" value="subir-foto">
                    <div class="form-group">
                        <label class="form-label" for="foto">Cambiar fotografia</label>
                        <div class="file-input" id="foto-file-input">
                            <input type="file" id="foto" name="foto" class="file-input-native"
                                   accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp">
                            <label for="foto" class="file-input-btn">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 10-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486"/></svg>
                                Seleccionar imagen
                            </label>
                            <span class="file-input-name" id="foto-file-name">Ningun archivo seleccionado</span>
                        </div>
                        <script>
                            (function () {
                                var input = document.getElementById('foto');
                                var cont = document.getElementById('foto-file-input');
                                var nombre = document.getElementById('foto-file-name');
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
                        <button type="submit" class="btn btn-primary btn-sm">Subir fotografia</button>
                    </div>
                </form>

                <c:if test="${not empty perfil.fotoPerfil}">
                    <form action="${ctx}/perfil" method="post" style="margin-top:0.75rem;"
                          onsubmit="return confirm('Eliminar la fotografia de perfil?');">
                        <input type="hidden" name="accion" value="eliminar-foto">
                        <button type="submit" class="btn btn-danger btn-sm">Eliminar fotografia</button>
                    </form>
                </c:if>
            </div>
        </div>

        <!-- Curriculum -->
        <div class="card">
            <div class="card-header">
                <h2 class="card-title">Curriculum principal</h2>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${not empty perfil.curriculum}">
                        <div class="datos-lista" style="grid-template-columns:1fr;">
                            <dl class="datos-item">
                                <dt>Archivo</dt>
                                <dd><c:out value="${perfil.curriculum.nombreOriginal}"/></dd>
                            </dl>
                            <dl class="datos-item">
                                <dt>Formato y tamano</dt>
                                <dd>
                                    <c:out value="${perfil.curriculum.extension}" />
                                    -
                                    <fmt:formatNumber value="${perfil.curriculum.tamanoBytes / 1024}"
                                                      maxFractionDigits="1"/> KB
                                </dd>
                            </dl>
                        </div>
                        <div class="fila-acciones" style="margin-top:1rem;">
                            <a href="${ctx}/archivo?id=${perfil.curriculum.id}" class="btn btn-primary btn-sm">Descargar</a>
                            <a href="${ctx}/perfil?accion=curriculum" class="btn btn-secondary btn-sm">Gestionar curriculum</a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state" style="padding:1rem 0;">
                            <h3>Sin curriculum</h3>
                            <p>Aun no has adjuntado un archivo de curriculum.</p>
                            <a href="${ctx}/perfil?accion=curriculum" class="btn btn-primary">Subir curriculum</a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <!-- Datos personales -->
    <div class="card" style="margin-top:1.5rem; max-width:820px;">
        <div class="card-header">
            <h2 class="card-title">Datos personales</h2>
        </div>
        <div class="card-body">
            <form action="${ctx}/perfil" method="post">
                <input type="hidden" name="accion" value="actualizar">

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="nombre">Nombre completo *</label>
                        <input type="text" id="nombre" name="nombre" class="form-control" required
                               maxlength="150" value="${fn:escapeXml(formNombre)}">
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="email">Correo electronico *</label>
                        <input type="email" id="email" name="email" class="form-control" required
                               maxlength="150" value="${fn:escapeXml(formEmail)}">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="telefono">Telefono</label>
                        <input type="tel" id="telefono" name="telefono" class="form-control"
                               maxlength="30" value="${fn:escapeXml(formTelefono)}" placeholder="7000-0000">
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
                        <input type="text" id="departamento" name="departamento" class="form-control"
                               maxlength="100" value="${fn:escapeXml(formDepartamento)}">
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="direccion">Direccion de residencia</label>
                        <input type="text" id="direccion" name="direccion" class="form-control"
                               maxlength="255" value="${fn:escapeXml(formDireccion)}">
                    </div>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-primary btn-lg">Guardar cambios</button>
                    <a href="${ctx}/inicio" class="btn btn-secondary">Cancelar</a>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="footer.jsp" %>
