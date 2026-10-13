<%@ page pageEncoding="UTF-8" %>
<%@ include file="../fragments/start.jspf" %><%@ include file="../fragments/header.jspf" %>
<div class="page-heading"><h1>Registro de operaciones</h1><p>Últimas 50 operaciones administrativas y académicas registradas.</p></div><section class="panel"><div class="table-wrap"><table><thead><tr><th>Fecha</th><th>Responsable</th><th>Operación</th><th>Detalle</th></tr></thead><tbody><c:forEach items="${entries}" var="entry"><tr><td>${entry.createdAt}</td><td><c:out value="${entry.actor.fullName}"/></td><td>${entry.action}</td><td><c:out value="${entry.details}"/></td></tr></c:forEach></tbody></table></div><c:if test="${empty entries}"><p class="muted">Todavía no se han registrado operaciones.</p></c:if></section>
<%@ include file="../fragments/footer.jspf" %>
