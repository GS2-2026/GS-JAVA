param([switch]$Test, [switch]$Package)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not $env:JAVA_HOME -or -not (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
    $jdk = Get-ChildItem -LiteralPath "$env:USERPROFILE\.jdks" -Directory -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName 'bin\javac.exe') } | Sort-Object Name -Descending | Select-Object -First 1
    if (-not $jdk) { throw 'JDK 21+ nao encontrado. Configuracao necessaria: JAVA_HOME.' }
    $env:JAVA_HOME = $jdk.FullName
}
$maven = Join-Path (Get-Location) '.tools\apache-maven-3.9.11\bin\mvn.cmd'
if (-not (Test-Path $maven)) {
    if (Get-Command mvn -ErrorAction SilentlyContinue) { $maven = 'mvn' }
    else { throw 'Maven 3.9+ nao encontrado no PATH ou em .tools.' }
}
if ($Test) { & $maven '-Dmaven.repo.local=.tools/m2' test }
elseif ($Package) { & $maven '-Dmaven.repo.local=.tools/m2' clean package }
else { & $maven '-Dmaven.repo.local=.tools/m2' spring-boot:run }
exit $LASTEXITCODE
