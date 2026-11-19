<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/header.jsp">
    <jsp:param name="tituloPagina" value="Ayuda"/>
</jsp:include>

<div class="container section-sm">
    <div class="section-header" style="margin-bottom:2rem;">
        <h1>Ayuda</h1>
        <p>Encuentra respuestas a tus preguntas</p>
    </div>

    <!-- Que es EmpleaSV -->
    <div class="card mb-2" id="nosotros">
        <h3 style="margin-bottom:0.75rem;">Que es EmpleaSV?</h3>
        <p style="color:var(--gray-600); line-height:1.8;">
            EmpleaSV es una plataforma de busqueda de empleo disenada para conectar candidatos
            con oportunidades laborales en El Salvador. Permitimos a las empresas publicar ofertas
            y a los candidatos encontrar empleos que se ajusten a sus habilidades y experiencia.
        </p>
    </div>

    <!-- Como buscar empleo -->
    <div class="card mb-2">
        <h3 style="margin-bottom:0.75rem;">Como buscar empleo?</h3>
        <ol style="color:var(--gray-600); line-height:2; padding-left:1.25rem;">
            <li>Visita la seccion de <a href="${pageContext.request.contextPath}/ofertas?format=jsp">Ofertas de empleo</a>.</li>
            <li>Utiliza el buscador para filtrar por titulo o palabra clave.</li>
            <li>Filtra por ubicacion si buscas algo especifico.</li>
            <li>Haz clic en una oferta para ver los detalles completos.</li>
            <li>Completa tu <a href="${pageContext.request.contextPath}/perfil">perfil de candidato</a> y sube tu curriculum para que las empresas puedan conocerte.</li>
        </ol>
        <p style="color:var(--gray-500); font-size:0.85rem; margin-top:0.75rem;">
            La funcion de postulacion todavia no esta disponible en la plataforma.
        </p>
    </div>

    <!-- Como publicar una oferta -->
    <div class="card mb-2">
        <h3 style="margin-bottom:0.75rem;">Como publicar una oferta?</h3>
        <ol style="color:var(--gray-600); line-height:2; padding-left:1.25rem;">
            <li>Crea una cuenta de empresa en <a href="${pageContext.request.contextPath}/auth?accion=registro">Registro</a>.</li>
            <li>Inicia sesion con tus credenciales.</li>
            <li>Dirigete al <a href="${pageContext.request.contextPath}/panel-empresa">Panel de empresa</a>.</li>
            <li>Haz clic en "Nueva oferta" y completa el formulario.</li>
            <li>Publica tu oferta: aparecera en el listado publico de ofertas de empleo.</li>
        </ol>
    </div>

    <!-- Preguntas frecuentes -->
    <h2 style="margin: 2rem 0 1rem;">Preguntas frecuentes</h2>

    <div class="faq-item">
        <div class="faq-question">
            Es gratuito usar EmpleaSV?
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/></svg>
        </div>
        <div class="faq-answer">
            Si, crear una cuenta y buscar empleo es completamente gratuito para los candidatos.
            Las empresas pueden publicar ofertas de forma gratuita en esta etapa del proyecto.
        </div>
    </div>

    <div class="faq-item">
        <div class="faq-question">
            Como actualizo mi perfil?
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/></svg>
        </div>
        <div class="faq-answer">
            Dirigete a tu <a href="${pageContext.request.contextPath}/perfil">perfil de candidato</a>: ahi puedes
            actualizar tu nombre, correo, telefono, direccion, fotografia y curriculum.
        </div>
    </div>

    <div class="faq-item">
        <div class="faq-question">
            Cuando estara disponible postularme a las ofertas?
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/></svg>
        </div>
        <div class="faq-answer">
            Todavia no esta implementada la funcion de postulaciones ni la de guardar ofertas.
            Estas secciones existen en el menu, pero no registran informacion porque el modulo
            correspondiente no se ha desarrollado.
        </div>
    </div>

    <div class="faq-item">
        <div class="faq-question">
            Quien puede publicar ofertas de empleo?
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/></svg>
        </div>
        <div class="faq-answer">
            Solo las cuentas con rol de empresa. Al registrarte como empresa completas tu perfil
            empresarial y cada cuenta publica unicamente las ofertas de su propia organizacion.
        </div>
    </div>

    <div class="faq-item">
        <div class="faq-question">
            Como contacto al soporte?
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/></svg>
        </div>
        <div class="faq-answer">
            Puedes escribirnos a <strong>soporte@empleasv.com</strong> o al telefono
            <strong>2XXX-XXXX</strong>. Nuestro horario de atencion es de lunes a viernes de 8:00 AM a 5:00 PM.
        </div>
    </div>

    <!-- Contacto -->
    <div class="card mt-3" id="contacto" style="text-align:center; padding:2rem;">
        <h3 style="margin-bottom:0.5rem;">Contacto</h3>
        <p style="color:var(--gray-500); margin-bottom:1rem;">¿No encontraste lo que buscabas?</p>
        <p style="color:var(--gray-700); font-size:0.9rem;">
            Email: <strong>soporte@empleasv.com</strong><br>
            Telefono: <strong>2XXX-XXXX</strong><br>
            San Salvador, El Salvador
        </p>
    </div>
</div>

<%@ include file="/WEB-INF/views/footer.jsp" %>
