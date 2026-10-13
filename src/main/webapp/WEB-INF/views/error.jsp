<%@ page pageEncoding="UTF-8" %>
<%@ include file="fragments/start.jspf" %><%@ include file="fragments/header.jspf" %>
<section class="empty-state panel narrow"><span class="stat-icon sand"><svg><use href="#i-check"/></svg></span><h1><c:out value="${empty errorTitle ? 'No fue posible abrir esta página' : errorTitle}"/></h1><p><c:out value="${empty errorMessage ? 'La página no está disponible o la operación no pudo completarse.' : errorMessage}"/></p><div class="actions"><button type="button" class="button button-ghost go-back">Volver</button><a class="button" href="${ctx}${user == null ? '/' : '/panel'}">Ir al inicio →</a></div></section>
<%@ include file="fragments/footer.jspf" %>
