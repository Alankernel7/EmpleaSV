<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Crear cuenta - EmpleaSV</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        .form-section { display: none; }
        .form-section.active { display: block; }
    </style>
</head>
<body>
    <nav class="navbar">
        <div class="nav-container">
            <a class="nav-brand" href="${pageContext.request.contextPath}/inicio">
                <span class="nav-brand-icon">E</span>
                EmpleaSV
            </a>
            <div class="nav-center">
                <a class="nav-link" href="${pageContext.request.contextPath}/inicio">Inicio</a>
                <a class="nav-link" href="${pageContext.request.contextPath}/ofertas?format=jsp">Ofertas</a>
                <a class="nav-link" href="${pageContext.request.contextPath}/empresas">Empresas</a>
            </div>
            <div class="nav-actions">
                <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-ghost btn-sm">Iniciar sesion</a>
            </div>
        </div>
    </nav>

    <div class="auth-page">
        <div class="auth-card" style="max-width:500px;">
            <h1>Crear una cuenta</h1>
            <p class="subtitle">Selecciona el tipo de cuenta que deseas crear</p>

            <div class="alert alert-info" style="font-size:0.85rem;">
                El registro estara disponible proximamente.
            </div>

            <!-- Selector de rol -->
            <div class="role-selector">
                <div class="role-option active" onclick="showSection('candidato')">
                    <div class="role-option-icon">
                        <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
                    </div>
                    <h4>Candidato</h4>
                    <p>Busco empleo</p>
                </div>
                <div class="role-option" onclick="showSection('empresa')">
                    <div class="role-option-icon">
                        <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/></svg>
                    </div>
                    <h4>Empresa</h4>
                    <p>Recluto talento</p>
                </div>
            </div>

            <!-- Formulario Candidato -->
            <div id="section-candidato" class="form-section active">
                <form action="#" method="post" onsubmit="return false;">
                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label" for="c-nombre">Nombre</label>
                            <input type="text" id="c-nombre" name="nombre" class="form-control" placeholder="Tu nombre" disabled>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="c-apellido">Apellido</label>
                            <input type="text" id="c-apellido" name="apellido" class="form-control" placeholder="Tu apellido" disabled>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="c-email">Correo electronico</label>
                        <input type="email" id="c-email" name="email" class="form-control" placeholder="tu@email.com" disabled>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="c-pass">Contrasena</label>
                        <input type="password" id="c-pass" name="password" class="form-control" placeholder="Minimo 8 caracteres" disabled>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="c-pass2">Confirmar contrasena</label>
                        <input type="password" id="c-pass2" name="password2" class="form-control" placeholder="Repite tu contrasena" disabled>
                    </div>
                    <button type="submit" class="btn btn-primary btn-lg" style="width:100%;" disabled>Crear cuenta de candidato</button>
                </form>
            </div>

            <!-- Formulario Empresa -->
            <div id="section-empresa" class="form-section">
                <form action="#" method="post" onsubmit="return false;">
                    <div class="form-group">
                        <label class="form-label" for="e-nombre">Nombre de la empresa</label>
                        <input type="text" id="e-nombre" name="nombre" class="form-control" placeholder="Nombre de tu empresa" disabled>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="e-email">Correo electronico</label>
                        <input type="email" id="e-email" name="email" class="form-control" placeholder="empresa@email.com" disabled>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="e-tel">Telefono</label>
                        <input type="tel" id="e-tel" name="telefono" class="form-control" placeholder="2XXX-XXXX" disabled>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="e-desc">Descripcion</label>
                        <textarea id="e-desc" name="descripcion" class="form-control" placeholder="Breve descripcion de tu empresa" disabled></textarea>
                    </div>
                    <button type="submit" class="btn btn-primary btn-lg" style="width:100%;" disabled>Crear cuenta de empresa</button>
                </form>
            </div>

            <div class="auth-footer">
                Ya tienes una cuenta? <a href="${pageContext.request.contextPath}/login.jsp">Inicia sesion</a>
            </div>
        </div>
    </div>

    <script>
        function showSection(tipo) {
            document.querySelectorAll('.role-option').forEach(function(el) { el.classList.remove('active'); });
            document.querySelectorAll('.form-section').forEach(function(el) { el.classList.remove('active'); });
            if (tipo === 'candidato') {
                document.querySelector('.role-option:nth-child(1)').classList.add('active');
                document.getElementById('section-candidato').classList.add('active');
            } else {
                document.querySelector('.role-option:nth-child(2)').classList.add('active');
                document.getElementById('section-empresa').classList.add('active');
            }
        }
    </script>
</body>
</html>
