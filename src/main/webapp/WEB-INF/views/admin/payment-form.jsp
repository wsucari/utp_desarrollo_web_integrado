<%@ page pageEncoding="UTF-8" %>
<%@ include file="../fragments/start.jspf" %><%@ include file="../fragments/header.jspf" %>
<div data-editor-content><h1><c:out value="${pageTitle}"/></h1>
<section class="panel form-panel"><form:form modelAttribute="form" method="post" cssClass="stack-form"><form:errors path="*" cssClass="form-errors" element="div"/>
<c:if test="${id != null}"><form:hidden path="version"/><form:hidden path="studentId"/><form:hidden path="courseId"/></c:if>
<div class="form-grid"><label>Estudiante<form:select path="studentId" disabled="${id != null}" required="required" items="${students}" itemValue="id" itemLabel="fullName"/></label><label>Curso<form:select path="courseId" disabled="${id != null}" required="required" items="${courses}" itemValue="id" itemLabel="title"/></label>
<label>Importe en soles<form:input path="amount" type="number" min="0" max="999999.99" step="0.01" required="required"/></label><label>Referencia o sustento<form:input path="reference" maxlength="120" required="required"/></label>
<label>Estado<form:select path="status"><form:option value="CONFIRMADO">Confirmado</form:option><form:option value="PENDIENTE">Pendiente</form:option><form:option value="ANULADO">Anulado</form:option></form:select></label><label>Acceso hasta<form:input path="expiration" type="date" required="required"/></label></div>
<p class="muted small">El registro confirmado habilita la matrícula hasta la fecha indicada. No se procesa ningún cobro externo. El estudiante y el curso se conservan al editar.</p>
<div class="actions"><button class="button">${id == null ? 'Registrar pago' : 'Guardar cambios'}</button><button type="button" class="button button-ghost" data-modal-close>Cancelar</button></div></form:form></section></div>
<%@ include file="../fragments/footer.jspf" %>
