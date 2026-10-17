<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="header.jsp">
    <jsp:param name="tituloPagina" value="Mi curriculum"/>
</jsp:include>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="principalId" value="${perfil.curriculum.id}" />
<c:set var="totalCurriculums" value="${empty curriculums ? 0 : fn:length(curriculums)}" />

<div class="container section-sm">
    <div class="page-header">
        <div>
            <h1>Mi curriculum</h1>
            <p class="text-muted" style="margin-top:0.35rem;">Guarda varios curriculum, revisalos y marca cual quieres que vean las empresas.</p>
        </div>
        <a href="${ctx}/perfil" class="btn btn-ghost btn-sm">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/></svg>
            Volver al perfil
        </a>
    </div>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success"><c:out value="${mensajeExito}"/></div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-error"><c:out value="${mensajeError}"/></div>
    </c:if>

    <div class="card">
        <div class="card-header">
            <h2 class="card-title">Mis curriculum</h2>
            <span class="cv-contador">${totalCurriculums} ${totalCurriculums == 1 ? 'archivo' : 'archivos'}</span>
        </div>
        <div class="card-body">
            <c:choose>
                <c:when test="${empty curriculums}">
                    <div class="empty-state" style="padding:2.5rem 1rem;">
                        <div class="empty-state-icon">
                            <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
                        </div>
                        <h3>Aun no tienes curriculum</h3>
                        <p>Sube tu primer archivo para completar tu perfil de candidato.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="cv-grid">
                        <c:forEach var="cv" items="${curriculums}">
                            <article class="cv-card${cv.id == principalId ? ' cv-card-principal' : ''}">
                                <div class="cv-card-top">
                                    <span class="cv-icono" aria-hidden="true">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
                                    </span>
                                    <div class="cv-card-texto">
                                        <h3 class="cv-nombre" title="${fn:escapeXml(cv.nombreOriginal)}">
                                            <c:out value="${cv.nombreOriginal}"/>
                                        </h3>
                                        <p class="cv-meta">
                                            <span class="cv-ext"><c:out value="${cv.extension}"/></span>
                                            <span>&middot;</span>
                                            <span><fmt:formatNumber value="${cv.tamanoBytes / 1024}" maxFractionDigits="1"/> KB</span>
                                            <span>&middot;</span>
                                            <span><c:out value="${fn:substringBefore(cv.fechaSubida, 'T')}"/></span>
                                        </p>
                                    </div>
                                </div>

                                <c:choose>
                                    <c:when test="${cv.id == principalId}">
                                        <span class="cv-estado cv-estado-principal">
                                            <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"/></svg>
                                            Principal
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="cv-estado">Guardado</span>
                                    </c:otherwise>
                                </c:choose>

                                <div class="cv-acciones">
                                    <c:if test="${cv.extension == 'pdf'}">
                                        <button type="button" class="btn btn-secondary btn-sm"
                                                data-preview-id="${cv.id}"
                                                data-preview-nombre="${fn:escapeXml(cv.nombreOriginal)}"
                                                onclick="abrirPreview(this)">
                                            Vista previa
                                        </button>
                                    </c:if>
                                    <a class="btn btn-secondary btn-sm" href="${ctx}/archivo?id=${cv.id}">Descargar</a>

                                    <c:if test="${cv.id != principalId}">
                                        <form action="${ctx}/perfil" method="post" class="cv-form">
                                            <input type="hidden" name="accion" value="curriculum-principal">
                                            <input type="hidden" name="archivoId" value="${cv.id}">
                                            <button type="submit" class="btn btn-outline btn-sm">Hacer principal</button>
                                        </form>
                                    </c:if>

                                    <form action="${ctx}/perfil" method="post" class="cv-form"
                                          onsubmit="return confirm('Eliminar este curriculum? Esta accion no se puede deshacer.');">
                                        <input type="hidden" name="accion" value="eliminar-curriculum-archivo">
                                        <input type="hidden" name="archivoId" value="${cv.id}">
                                        <button type="submit" class="btn btn-danger btn-sm">Eliminar</button>
                                    </form>
                                </div>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="card" style="margin-top:1.5rem;">
        <div class="card-header">
            <h2 class="card-title">Subir curriculum</h2>
        </div>
        <div class="card-body">
            <form action="${ctx}/perfil" method="post" enctype="multipart/form-data">
                <input type="hidden" name="accion" value="agregar-curriculum">
                <div class="form-group">
                    <label class="form-label" for="curriculum">Selecciona el archivo</label>
                    <div class="file-input" id="curriculum-file-input">
                        <input type="file" id="curriculum" name="curriculum" class="file-input-native" required
                               accept=".pdf,.doc,.docx,.odt,.txt,application/pdf">
                        <label for="curriculum" class="file-input-btn">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 10-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486"/></svg>
                            Seleccionar archivo
                        </label>
                        <span class="file-input-name" id="curriculum-file-name">Ningun archivo seleccionado</span>
                    </div>
                    <script>
                        (function () {
                            var input = document.getElementById('curriculum');
                            var cont = document.getElementById('curriculum-file-input');
                            var nombre = document.getElementById('curriculum-file-name');
                            if (!input || !cont || !nombre) return;
                            input.addEventListener('change', function () {
                                var archivo = input.files && input.files.length ? input.files[0].name : '';
                                nombre.textContent = archivo || 'Ningun archivo seleccionado';
                                cont.classList.toggle('has-file', !!archivo);
                            });
                        })();
                    </script>
                    <span class="form-hint">Formatos admitidos: PDF, DOC, DOCX, ODT y TXT. Tamano maximo 5 MB. El archivo se agrega a tu lista y no reemplaza los demas.</span>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn-primary btn-lg">Subir curriculum</button>
                    <a href="${ctx}/perfil" class="btn btn-secondary">Cancelar</a>
                </div>
            </form>
        </div>
    </div>
</div>

<div class="modal-overlay" id="modal-curriculum" role="dialog" aria-modal="true"
     aria-labelledby="modal-curriculum-titulo">
    <div class="modal modal-preview">
        <div class="modal-header">
            <div class="modal-header-content">
                <h2 id="modal-curriculum-titulo">Vista previa del curriculum</h2>
                <p class="cv-preview-nombre" id="preview-nombre"></p>
            </div>
            <button type="button" class="modal-close" onclick="cerrarPreview()" aria-label="Cerrar vista previa">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
        </div>
        <div class="modal-body modal-body-preview">
            <iframe id="preview-frame" class="preview-frame" title="Vista previa del curriculum"></iframe>
            <p class="form-hint" style="margin-top:0.75rem;">
                Si el visor no carga en tu navegador, usa el boton <strong>Descargar</strong>.
            </p>
        </div>
    </div>
</div>

<script>
    (function () {
        'use strict';

        var CTX = '<c:out value="${ctx}"/>';
        var overlay = document.getElementById('modal-curriculum');
        var frame = document.getElementById('preview-frame');
        var nombre = document.getElementById('preview-nombre');

        window.abrirPreview = function (boton) {
            var id = boton.getAttribute('data-preview-id');
            if (!id) {
                return;
            }
            frame.src = CTX + '/archivo?id=' + encodeURIComponent(id) + '&inline=1';
            nombre.textContent = boton.getAttribute('data-preview-nombre') || 'Curriculum';
            overlay.classList.add('active');
        };

        window.cerrarPreview = function () {
            overlay.classList.remove('active');
            frame.src = '';
        };

        overlay.addEventListener('click', function (e) {
            if (e.target === overlay) {
                window.cerrarPreview();
            }
        });

        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && overlay.classList.contains('active')) {
                window.cerrarPreview();
            }
        });
    })();
</script>

<%@ include file="footer.jsp" %>
