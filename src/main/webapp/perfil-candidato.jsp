<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Pagina movida: el panel del candidato ahora se sirve desde /inicio
    // (WEB-INF/views/panel-candidato.jsp). Se conserva esta direccion para
    // que los enlaces antiguos sigan funcionando.
    response.sendRedirect(request.getContextPath() + "/inicio");
%>
