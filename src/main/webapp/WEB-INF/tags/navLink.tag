<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="href" required="true" type="java.lang.String" %>
<%@ attribute name="label" required="true" type="java.lang.String" %>
<%@ attribute name="mode" required="true" type="java.lang.String" %>
<%@ attribute name="active" required="true" type="java.lang.Boolean" %>
<%@ attribute name="icon" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<a href="<c:out value='${href}'/>" ${active ? 'aria-current="page"' : ''} class="${mode == 'sidebar' ? 'sidebar-link tw-flex tw-items-center tw-gap-3 tw-p-3' : 'tw-inline-flex tw-items-center tw-py-5 tw-border-b-2 tw-font-medium tw-text-sm'} ${active ? 'is-current' : ''}">
<c:if test="${not empty icon}"><svg aria-hidden="true"><use href="#i-${icon}"/></svg></c:if><span><c:out value="${label}"/></span></a>

