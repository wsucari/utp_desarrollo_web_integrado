<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="account" required="true" type="pe.edu.inudi.domain.Account" %>
<%@ attribute name="role" required="true" type="java.lang.String" %>
<%@ attribute name="idPrefix" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<div class="tw-relative account-menu">
<button id="${idPrefix}-account-button" type="button" data-toggle-target="${idPrefix}-account-dropdown" aria-controls="${idPrefix}-account-dropdown" aria-expanded="false" class="tw-flex tw-items-center tw-gap-2 tw-bg-transparent tw-border-0 tw-p-2 tw-text-current"><span class="tw-rounded-full tw-w-8 tw-h-8 tw-shrink-0 tw-flex tw-items-center tw-justify-center tw-bg-gray-200 tw-text-gray-700 tw-text-xs"><c:out value="${account.initials}"/></span><span class="tw-text-sm tw-font-medium tw-text-left tw-break-words"><c:out value="${account.fullName}"/></span><span aria-hidden="true">⌄</span></button>
<div id="${idPrefix}-account-dropdown" class="tw-hidden ${idPrefix == 'mobile' ? 'account-dropup tw-absolute tw-left-0 tw-w-full' : 'tw-absolute tw-right-0 tw-mt-2 tw-w-48'} tw-z-50 tw-rounded-md tw-bg-white tw-shadow-lg tw-text-gray-700">
<a href="${pageContext.request.contextPath}${role == 'DOCENTE' ? '/profesor/perfil' : role == 'ESTUDIANTE' ? '/users/panel/perfil' : '/perfil'}" class="tw-block tw-px-4 tw-py-2 tw-text-sm hover:tw-bg-gray-100">${role == 'DOCENTE' ? 'Mi perfil' : 'Perfil'}</a>
<form method="post" action="${pageContext.request.contextPath}/logout"><input type="hidden" name="${requestScope._csrf.parameterName}" value="${requestScope._csrf.token}"/><button type="submit" class="tw-w-full tw-text-left tw-border-0 tw-bg-transparent tw-px-4 tw-py-2 tw-text-sm hover:tw-bg-gray-100">Cerrar Sesión</button></form>
</div></div>
