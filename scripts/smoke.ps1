param([string]$BaseUrl = 'http://localhost:8080/api')
$ErrorActionPreference = 'Stop'
$tag = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$residencia = $null
$medidor = $null
$leitura = $null
function Request([string]$Method, [string]$Route, $Body = $null) {
    $params = @{ Method = $Method; Uri = "$BaseUrl/$Route"; UseBasicParsing = $true }
    if ($null -ne $Body) { $params.ContentType = 'application/json'; $params.Body = $Body | ConvertTo-Json -Compress }
    $response = Invoke-WebRequest @params
    Write-Host "$Method $Route -> $($response.StatusCode)"
    if ($response.Content) { return $response.Content | ConvertFrom-Json }
}
try {
    foreach ($resource in @('residencias','medidores','leituras')) { $null = Request GET $resource }
    $casa = @{ idComunidade=1; codigoResidencia="T$tag"; endereco='Residencia temporaria de verificacao'; qtdMoradores=2 }
    $residencia = Request POST 'residencias' $casa
    $null = Request GET "residencias/$($residencia.id)"
    $casa.qtdMoradores = 3
    $null = Request PUT "residencias/$($residencia.id)" $casa
    $m = @{ idResidencia=$residencia.id; numeroSerie="T$tag"; dataInstalacao='2025-01-01'; status='ATIVO' }
    $medidor = Request POST "residencias/$($residencia.id)/medidores" $m
    $null = Request GET "residencias/$($residencia.id)/medidores"
    $null = Request GET "medidores/$($medidor.id)"
    $null = Request PUT "medidores/$($medidor.id)" $m
    $l = @{ idMedidor=$medidor.id; dataHoraLeitura='2026-01-01T06:00:00'; consumoLitros=123.45 }
    $leitura = Request POST "medidores/$($medidor.id)/leituras" $l
    $null = Request GET "medidores/$($medidor.id)/leituras"
    $null = Request GET "leituras/$($leitura.id)"
    $l.consumoLitros = 100.00
    $null = Request PUT "leituras/$($leitura.id)" $l
    Write-Host 'CRUD real Oracle: criacao, consulta e alteracao confirmadas.'
} finally {
    if ($leitura) { $null = Request DELETE "leituras/$($leitura.id)" }
    if ($medidor) { $null = Request DELETE "medidores/$($medidor.id)" }
    if ($residencia) { $null = Request DELETE "residencias/$($residencia.id)" }
}
