[CmdletBinding()]
param(
    [string]$RegistryBaseUrl = "http://localhost:18081/api/v4",
    [string]$SuppliesBaseUrl = "http://localhost:18082/api/v4"
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Net.Http

$httpClient = [System.Net.Http.HttpClient]::new()
$seedPecaAtiva = "55555555-5555-5555-5555-555555555555"
$seedPecaParaReativar = "66666666-6666-6666-6666-666666666666"
$requisicaoUri = "$SuppliesBaseUrl/requisicoes-compra"
$deactivated = $false

function Invoke-JsonRequest {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Uri,
        [object]$Body
    )

    $request = [System.Net.Http.HttpRequestMessage]::new(
        [System.Net.Http.HttpMethod]::new($Method),
        $Uri
    )
    $response = $null

    if ($null -ne $Body) {
        $json = ConvertTo-Json -InputObject $Body -Depth 8 -Compress
        $request.Content = [System.Net.Http.StringContent]::new(
            $json,
            [System.Text.Encoding]::UTF8,
            "application/json"
        )
    }

    try {
        $response = $httpClient.SendAsync($request).GetAwaiter().GetResult()
        $content = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
        return [pscustomobject]@{
            StatusCode = [int]$response.StatusCode
            Content = $content
        }
    }
    finally {
        $request.Dispose()
        if ($null -ne $response) {
            $response.Dispose()
        }
    }
}

function Assert-Status {
    param(
        [Parameter(Mandatory = $true)]$Response,
        [Parameter(Mandatory = $true)][int]$Expected,
        [Parameter(Mandatory = $true)][string]$Scenario
    )

    if ($Response.StatusCode -ne $Expected) {
        throw "$Scenario expected HTTP $Expected but got $($Response.StatusCode): $($Response.Content)"
    }
}

function New-RequisitionBody {
    param([Parameter(Mandatory = $true)][string]$PecaUuid)

    return @{
        motivo = "ESTOQUE_MINIMO"
        observacao = "E2E Registry-Supplies"
        itens = @(
            @{
                pecaUuid = $PecaUuid
                quantidade = 1
            }
        )
    }
}

try {
    $seedResponse = Invoke-JsonRequest -Method "GET" -Uri "$RegistryBaseUrl/pecas/$seedPecaAtiva"
    Assert-Status -Response $seedResponse -Expected 200 -Scenario "Registry seed lookup"
    $seedPeca = $seedResponse.Content | ConvertFrom-Json
    if ($seedPeca.uuid -ne $seedPecaAtiva -or -not $seedPeca.isActive) {
        throw "The active Registry seed piece is missing or inactive."
    }

    $created = Invoke-JsonRequest -Method "POST" -Uri $requisicaoUri -Body (New-RequisitionBody $seedPecaAtiva)
    Assert-Status -Response $created -Expected 201 -Scenario "Create requisition using Registry seed"
    $requisicao = $created.Content | ConvertFrom-Json
    if ($requisicao.status -ne "ABERTA" -or $requisicao.itens.Count -ne 1) {
        throw "The created requisition did not contain the expected status and item."
    }
    $item = $requisicao.itens[0]
    if ($item.pecaUuid -ne $seedPecaAtiva -or $item.pecaCodigo -ne "PEA-001" -or
        $item.valorUnitario -ne 45.90) {
        throw "The requisition does not contain the Registry catalog snapshot."
    }
    Write-Output "PASS: Supplies created a requisition using the live Registry catalog."

    $missingUuid = [guid]::NewGuid().ToString()
    $missing = Invoke-JsonRequest -Method "POST" -Uri $requisicaoUri -Body (New-RequisitionBody $missingUuid)
    Assert-Status -Response $missing -Expected 404 -Scenario "Reject missing Registry piece"
    Write-Output "PASS: Supplies rejects a piece UUID missing from Registry."

    $inactiveSeedResponse = Invoke-JsonRequest -Method "GET" -Uri "$RegistryBaseUrl/pecas/$seedPecaParaReativar"
    Assert-Status -Response $inactiveSeedResponse -Expected 200 -Scenario "Registry inactive-test seed lookup"
    $inactiveSeed = $inactiveSeedResponse.Content | ConvertFrom-Json
    if (-not $inactiveSeed.isActive) {
        $reactivated = Invoke-JsonRequest -Method "PATCH" -Uri "$RegistryBaseUrl/pecas/$seedPecaParaReativar/reativar"
        Assert-Status -Response $reactivated -Expected 204 -Scenario "Prepare active seed piece"
    }

    $deactivation = Invoke-JsonRequest -Method "DELETE" -Uri "$RegistryBaseUrl/pecas/$seedPecaParaReativar"
    Assert-Status -Response $deactivation -Expected 204 -Scenario "Deactivate Registry seed piece"
    $deactivated = $true

    $inactive = Invoke-JsonRequest -Method "POST" -Uri $requisicaoUri -Body (New-RequisitionBody $seedPecaParaReativar)
    Assert-Status -Response $inactive -Expected 409 -Scenario "Reject inactive Registry piece"
    Write-Output "PASS: Supplies rejects a piece marked inactive by Registry."
}
finally {
    if ($deactivated) {
        $restored = Invoke-JsonRequest -Method "PATCH" -Uri "$RegistryBaseUrl/pecas/$seedPecaParaReativar/reativar"
        Assert-Status -Response $restored -Expected 204 -Scenario "Restore Registry seed piece"
    }
    $httpClient.Dispose()
}
