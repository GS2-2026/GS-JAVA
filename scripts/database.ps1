param([switch]$Apply)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not $env:JAVA_HOME) { throw 'JAVA_HOME nao configurado para um JDK 21+.' }
$driver = Get-ChildItem -LiteralPath '.tools\m2\com\oracle\database\jdbc\ojdbc11' -Recurse -Filter 'ojdbc11-*.jar' |
    Where-Object { $_.Name -notmatch 'sources|javadoc' } | Select-Object -First 1
if (-not $driver) { throw 'Driver JDBC ausente. Resolucao de dependencias: scripts/run.ps1 -Test.' }
if ($Apply) { & "$env:JAVA_HOME\bin\java.exe" -cp $driver.FullName scripts/DatabaseSetup.java --apply }
else { & "$env:JAVA_HOME\bin\java.exe" -cp $driver.FullName scripts/DatabaseSetup.java }
exit $LASTEXITCODE
