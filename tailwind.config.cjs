/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/main/webapp/WEB-INF/**/*.{jsp,jspf,tag}',
    './src/main/resources/static/assets/app.js'
  ],
  // Evita colisiones mientras se adaptan las pantallas a Tailwind.
  prefix: 'tw-',
  important: '.replica-ui',
  corePlugins: { preflight: false },
  theme: { extend: {} },
  plugins: []
};
