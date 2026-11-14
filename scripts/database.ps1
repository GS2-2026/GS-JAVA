param([switch]$Apply)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not $env:JAVA_HOME) { throw 'Configure JAVA_HOME apontando para um JDK 21+.' }
$driver = Get-ChildItem -LiteralPath '.tools\m2\com\oracle\database\jdbc\ojdbc11' -Recurse -Filter 'ojdbc11-*.jar' |
    Where-Object { $_.Name -notmatch 'sources|javadoc' } | Select-Object -First 1
if (-not $driver) { throw 'Execute primeiro scripts/run.ps1 -Test para baixar o driver JDBC.' }
if ($Apply) { & "$env:JAVA_HOME\bin\java.exe" -cp $driver.FullName scripts/DatabaseSetup.java --apply }
else { & "$env:JAVA_HOME\bin\java.exe" -cp $driver.FullName scripts/DatabaseSetup.java }
exit $LASTEXITCODE
