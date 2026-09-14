<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/header.jsp">
    <jsp:param name="tituloPagina" value="Mi Perfil"/>
</jsp:include>

<div class="container section-sm">
    <div class="page-header">
        <h1>Mi perfil</h1>
        <button class="btn btn-primary" onclick="alert('La edicion de perfil estara disponible proximamente.')">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
            Editar perfil
        </button>
    </div>

    <!-- Perfil header -->
    <div class="profile-header">
        <div class="profile-avatar">CM</div>
        <div class="profile-info" style="flex:1;">
            <h2>Carlos Martinez</h2>
            <p class="email">carlos.martinez@email.com</p>
            <p style="font-size:0.9rem; color:var(--gray-600); margin-bottom:0.5rem;">Desarrollador Full Stack | San Salvador, El Salvador</p>
            <div class="flex gap-1" style="flex-wrap:wrap;">
                <span class="badge badge-primary">Disponible</span>
                <span class="badge badge-info">5 anos exp.</span>
            </div>
        </div>
    </div>

    <div class="grid-2">
        <div>
            <!-- Info personal -->
            <div class="profile-section">
                <h3>Informacion personal</h3>
                <div class="detail-row">
                    <span class="detail-label">Nombre</span>
                    <span class="detail-value">Carlos Martinez</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Correo</span>
                    <span class="detail-value">carlos.martinez@email.com</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Telefono</span>
                    <span class="detail-value">7012-3456</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Ubicacion</span>
                    <span class="detail-value">San Salvador</span>
                </div>
            </div>

            <!-- Educacion -->
            <div class="profile-section">
                <h3>Educacion</h3>
                <div style="margin-bottom:1rem;">
                    <h4 style="font-size:0.9rem; margin-bottom:0.25rem;">Instituto Tecnico de El Salvador</h4>
                    <p style="font-size:0.8rem; color:var(--gray-500);">Ingenieria en Sistemas | 2018 - 2023</p>
                </div>
                <div>
                    <h4 style="font-size:0.9rem; margin-bottom:0.25rem;">Platzi</h4>
                    <p style="font-size:0.8rem; color:var(--gray-500);">Curso de Desarrollo Web Full Stack | 2022</p>
                </div>
            </div>
        </div>

        <div>
            <!-- Resumen -->
            <div class="profile-section">
                <h3>Resumen profesional</h3>
                <p style="font-size:0.9rem; color:var(--gray-600); line-height:1.7;">
                    Desarrollador Full Stack con 5 anos de experiencia en desarrollo web y movil.
                    Especializado en Java, JavaScript y bases de datos. Busco nuevas oportunidades
                    para crecer profesionalmente.
                </p>
            </div>

            <!-- Habilidades -->
            <div class="profile-section">
                <h3>Habilidades</h3>
                <div style="display:flex; flex-wrap:wrap; gap:0.5rem;">
                    <span class="skill-tag">Java</span>
                    <span class="skill-tag">JavaScript</span>
                    <span class="skill-tag">React</span>
                    <span class="skill-tag">Spring Boot</span>
                    <span class="skill-tag">MySQL</span>
                    <span class="skill-tag">HTML/CSS</span>
                    <span class="skill-tag">Git</span>
                    <span class="skill-tag">REST APIs</span>
                    <span class="skill-tag">Docker</span>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/footer.jsp" %>
