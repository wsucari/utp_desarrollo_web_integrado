<%@ page pageEncoding="UTF-8" %>
<%@ include file="fragments/start.jspf" %><%@ include file="fragments/header.jspf" %>
<div class="tw-min-h-[calc(100vh-96px)] tw-flex tw-flex-col tw-items-center tw-justify-center tw-bg-gray-200 tw-px-4 tw-py-6">
  <section aria-labelledby="login-title" class="tw-w-full tw-max-w-md tw-bg-white tw-rounded-lg tw-shadow-xl tw-px-6 tw-py-8">
    <div class="tw-flex tw-justify-center tw-mb-6">
      <a href="${ctx}/login" aria-label="Inudi Perú"><img src="${ctx}/images/logos/inudiwhite.png" alt="Inudi Perú" class="tw-h-20 tw-w-auto"/></a>
    </div>
    <h1 id="login-title" class="tw-text-3xl tw-font-bold tw-text-gray-700 tw-text-center tw-mb-8 tw-tracking-normal">Plataforma Inudi</h1>
    <c:if test="${not empty success}"><p role="status" class="tw-rounded-md tw-bg-green-50 tw-p-3 tw-mb-4 tw-text-green-800"><c:out value="${success}"/></p></c:if>
    <c:if test="${param.error != null}"><p role="alert" class="tw-rounded-md tw-bg-red-50 tw-p-3 tw-mb-4 tw-text-red-700">El correo o la contraseña no son correctos, o la cuenta está inactiva.</p></c:if>
    <c:if test="${param.salida != null}"><p role="status" class="tw-rounded-md tw-bg-green-50 tw-p-3 tw-mb-4 tw-text-green-800">Sesión cerrada correctamente.</p></c:if>
    <c:if test="${param.sesion != null}"><p role="alert" class="tw-rounded-md tw-bg-red-50 tw-p-3 tw-mb-4 tw-text-red-700">La cuenta o sus permisos cambiaron. Vuelve a iniciar sesión.</p></c:if>
    <form method="post" action="${ctx}/login">
      <%@ include file="fragments/csrf.jspf" %>
      <label for="login-email" class="tw-block tw-text-sm tw-font-medium tw-text-gray-700">Correo electrónico</label>
      <input id="login-email" type="email" name="username" autocomplete="username" placeholder="Usuario" required autofocus class="tw-block tw-mt-1 tw-w-full tw-rounded-lg tw-border tw-border-solid tw-border-gray-300 tw-px-4 tw-py-2 tw-text-sm focus:tw-outline-none focus:tw-ring-2 focus:tw-ring-blue-500"/>
      <label for="login-password" class="tw-block tw-mt-4 tw-text-sm tw-font-medium tw-text-gray-700">Contraseña</label>
      <div class="tw-relative tw-mt-1">
        <input id="login-password" type="password" name="password" autocomplete="current-password" placeholder="Contraseña" required class="tw-w-full tw-rounded-lg tw-border tw-border-solid tw-border-gray-300 tw-px-4 tw-py-2 tw-pr-12 tw-text-sm focus:tw-outline-none focus:tw-ring-2 focus:tw-ring-blue-500"/>
        <button type="button" data-password-toggle="login-password" aria-label="Mostrar contraseña" aria-pressed="false" class="tw-absolute tw-right-2 tw-top-1/2 -tw-translate-y-1/2 tw-border-0 tw-bg-transparent tw-text-gray-500 tw-p-1 hover:tw-text-blue-600">
          <svg aria-hidden="true" viewBox="0 0 24 24"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg>
        </button>
      </div>
      <div class="tw-flex tw-justify-center tw-mt-6">
        <button type="submit" class="tw-rounded-md tw-border-0 tw-bg-gray-800 tw-px-5 tw-py-2.5 tw-text-sm tw-font-semibold tw-text-white hover:tw-bg-gray-700 focus:tw-outline-none focus:tw-ring-2 focus:tw-ring-blue-500 focus:tw-ring-offset-2">Iniciar Sesión</button>
      </div>
    </form>
    <c:if test="${demo}">
      <details class="tw-mt-6 tw-text-xs tw-text-gray-600">
        <summary class="tw-cursor-pointer">Accesos de demostración local</summary>
        <p class="tw-mt-3 tw-mb-2">Administración: <code>admin@inudi.local</code><br/>Docente: <code>docente@inudi.local</code><br/>Estudiante: <code>estudiante@inudi.local</code></p>
        <p class="tw-mb-2">Contraseña: <code>Inudi2026!Demo</code></p>
        <p class="tw-mb-0">Datos ficticios. No utilizar estas cuentas en producción.</p>
      </details>
    </c:if>
  </section>
</div>
<%@ include file="fragments/footer.jspf" %>
