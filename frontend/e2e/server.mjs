import { spawn } from "node:child_process";
import { existsSync } from "node:fs";
import { fileURLToPath } from "node:url";
import path from "node:path";
const root = fileURLToPath(new URL("../..", import.meta.url));
const windows = process.platform === "win32";
const candidates = [
  process.env.JAVA_HOME &&
    path.join(process.env.JAVA_HOME, "bin", windows ? "java.exe" : "java"),
  ...(windows ? ["C:/Program Files/Java/jdk-21/bin/java.exe"] : []),
].filter(Boolean);
const executable = candidates.find((p) => existsSync(p)) || "java";
if (!existsSync(path.join(root, "target/inudi-academico.war")))
  throw new Error("Compila primero el WAR con npm run build:ui y mvn package.");
const child = spawn(executable, ["-jar", "target/inudi-academico.war"], {
  cwd: root,
  windowsHide: true,
  stdio: "inherit",
  env: {
    ...process.env,
    SERVER_PORT: "8093",
    SERVER_ADDRESS: "127.0.0.1",
    DB_URL: "jdbc:h2:mem:angular-e2e;DB_CLOSE_DELAY=-1",
    DEMO_ENABLED: "true",
    FRONTEND_ENABLED: "true",
    UPLOAD_DIRECTORY: "target/e2e-uploads",
    PUBLIC_URL: "http://127.0.0.1:8093",
  },
});
for (const event of ["SIGINT", "SIGTERM"])
  process.on(event, () => {
    child.kill();
    process.exit(0);
  });
process.on("exit", () => child.kill());
child.on("error", (e) => {
  console.error(e);
  process.exit(1);
});
child.on("exit", (code) => process.exit(code || 0));
