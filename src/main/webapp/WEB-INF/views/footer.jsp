<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    </main>

    <footer class="footer">
        <div class="container">
            <div class="footer-grid">
                <div>
                    <div class="footer-brand">EmpleaSV</div>
                    <p class="footer-desc">Plataforma de oportunidades laborales para El Salvador. Conectamos talento con las mejores empresas del pais.</p>
                </div>
                <div>
                    <div class="footer-title">Plataforma</div>
                    <ul class="footer-links">
                        <li><a href="${pageContext.request.contextPath}/index.jsp">Inicio</a></li>
                        <li><a href="${pageContext.request.contextPath}/ofertas?format=jsp">Ofertas de empleo</a></li>
                        <li><a href="${pageContext.request.contextPath}/empresas.jsp">Empresas</a></li>
                    </ul>
                </div>
                <div>
                    <div class="footer-title">Candidatos</div>
                    <ul class="footer-links">
                        <li><a href="${pageContext.request.contextPath}/perfil-candidato.jsp">Mi perfil</a></li>
                        <li><a href="${pageContext.request.contextPath}/mis-postulaciones.jsp">Mis postulaciones</a></li>
                        <li><a href="${pageContext.request.contextPath}/ofertas-guardadas.jsp">Ofertas guardadas</a></li>
                    </ul>
                </div>
                <div>
                    <div class="footer-title">Soporte</div>
                    <ul class="footer-links">
                        <li><a href="${pageContext.request.contextPath}/ayuda.jsp">Ayuda</a></li>
                        <li><a href="${pageContext.request.contextPath}/ayuda.jsp#nosotros">Nosotros</a></li>
                        <li><a href="${pageContext.request.contextPath}/ayuda.jsp#contacto">Contacto</a></li>
                    </ul>
                </div>
            </div>
            <div class="footer-bottom">
                <span>&copy; 2026 EmpleaSV. Todos los derechos reservados.</span>
                <div class="footer-bottom-links">
                    <a href="#">Terminos</a>
                    <a href="#">Privacidad</a>
                </div>
            </div>
        </div>
    </footer>

    <script>
        window.addEventListener('scroll', function() {
            var navbar = document.getElementById('navbar');
            if (window.scrollY > 10) {
                navbar.classList.add('scrolled');
            } else {
                navbar.classList.remove('scrolled');
            }
        });
    </script>
</body>
</html>
