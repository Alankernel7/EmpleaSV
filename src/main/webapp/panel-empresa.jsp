<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Pagina movida: el panel de la cuenta EMPRESA ahora se sirve desde
    // /panel-empresa (WEB-INF/views/panel-empresa.jsp). Se conserva esta
    // direccion para que los enlaces antiguos sigan funcionando.
    response.sendRedirect(request.getContextPath() + "/panel-empresa");
%>
