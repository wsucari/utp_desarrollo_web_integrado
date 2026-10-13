param([ValidateRange(1024,65535)][int]$Port=8080,[switch]$SkipBuild)
$ErrorActionPreference='Stop'
$taskProject=Split-Path -Parent $MyInvocation.MyCommand.Path
$taskJavaCandidates=@('C:\Program Files\Java\jdk-21',$env:JAVA_HOME)
$taskJavaDirectory=$taskJavaCandidates | Where-Object { $_ -and (Test-Path -LiteralPath (Join-Path $_ 'bin\java.exe')) } | Select-Object -First 1
if (-not $taskJavaDirectory) { throw 'Configura JAVA_HOME con un JDK 21 o posterior.' }
$taskPreviousJava=$env:JAVA_HOME
$taskPreviousPath=$env:PATH
$taskPreviousPort=$env:SERVER_PORT
$taskPreviousUrl=$env:PUBLIC_URL
Push-Location $taskProject
try {
 $env:JAVA_HOME=$taskJavaDirectory
 $env:PATH="$taskJavaDirectory\bin;$env:PATH"
 $env:SERVER_PORT="$Port"
 if(-not $env:PUBLIC_URL){$env:PUBLIC_URL="http://127.0.0.1:$Port"}
 $taskListener=Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue
 if($taskListener){throw "El puerto $Port está ocupado. Revisa http://127.0.0.1:$Port o selecciona otro puerto con -Port."}
 if(-not $SkipBuild) {
  if(-not (Get-Command npm.cmd -ErrorAction SilentlyContinue)){throw 'Node.js y npm son necesarios para compilar Tailwind.'}
  if(-not (Test-Path -LiteralPath 'node_modules\tailwindcss\package.json')) {
   & npm.cmd ci --no-audit --no-fund
   if($LASTEXITCODE -ne 0){throw 'No se pudieron preparar las dependencias de Tailwind.'}
  }
  & npm.cmd run build:css
  if($LASTEXITCODE -ne 0){throw 'La compilación de Tailwind falló; no se inició la aplicación.'}
  if(-not (Get-Command mvn.cmd -ErrorAction SilentlyContinue)){throw 'Maven no está disponible en PATH.'}
  & mvn.cmd -B package
  if($LASTEXITCODE -ne 0){throw 'La compilación o las pruebas fallaron; no se inició la aplicación.'}
 }
 if(-not (Test-Path -LiteralPath 'target\inudi-academico.war')){throw 'No existe el WAR. Ejecuta el script sin -SkipBuild.'}
 Write-Host "Campus de Inudi Perú: http://127.0.0.1:$Port"
 Write-Host 'Para detenerlo utiliza Ctrl+C en esta terminal.'
 & "$taskJavaDirectory\bin\java.exe" -jar 'target\inudi-academico.war'
} finally {
 $env:JAVA_HOME=$taskPreviousJava
 $env:PATH=$taskPreviousPath
 $env:SERVER_PORT=$taskPreviousPort
 $env:PUBLIC_URL=$taskPreviousUrl
 Pop-Location
}
