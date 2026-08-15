# ============================================================
# LFM Nacional — Simulación de carrera terminada
#
# Crea un piloto de prueba, una carrera FINALIZADA con sus
# resultados (importando el JSON de sesión RACE de prueba) y
# la clasificación, para ver cómo se muestran en el frontend.
#
# Reutilizable: si la carrera ya existe, la reutiliza (no
# duplica resultados). Usa -CarreraNombre para generar una
# carrera nueva con otro nombre.
#
# Uso:
#   powershell -ExecutionPolicy Bypass -File scripts\simular-carrera.ps1
# ============================================================

[CmdletBinding()]
param(
    [string]$ApiBase = "http://localhost:8080",
    [string]$RaceJson = "",
    [string]$CarreraNombre = "Carrera Simulada",
    [string]$EmailPiloto = "simulacion.matifel@lfm.local",
    [string]$PasswordPiloto = "test1234",
    [string]$NombrePiloto = "Matifel",
    [string]$GuidSteam = "76561199066767489",
    [string]$Circuito = "Balcarce",
    [string]$FechaCarrera = "2026-08-07T16:31:00",
    [long]$TiempoClasificacionMs = 120654
)

$script:Token = $null

$ScriptDir = if ($PSScriptRoot) { $PSScriptRoot } else { Split-Path -Parent $MyInvocation.MyCommand.Path }
if ([string]::IsNullOrWhiteSpace($RaceJson)) {
    $RaceJson = Join-Path $ScriptDir "..\testJsonServidor\2026_8_7_16_31_RACE.json"
}

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Invoke-Api {
    param(
        [string]$Method = "GET",
        [string]$Path,
        [object]$Body = $null,
        [bool]$Auth = $true,
        [string]$ContentType = "application/json"
    )
    $params = @{
        Method    = $Method
        Uri       = "$ApiBase/api$Path"
        TimeoutSec = 30
    }
    $headers = @{}
    if ($Auth -and $script:Token) { $headers["Authorization"] = "Bearer $script:Token" }
    if ($Body -ne $null) {
        if ($Body -is [string]) { $params.Body = $Body }
        else { $params.Body = ($Body | ConvertTo-Json -Depth 8) }
        $params.ContentType = $ContentType
    }
    try {
        if ($headers.Count -gt 0) { $params.Headers = $headers }
        return Invoke-RestMethod @params
    }
    catch {
        $detalle = ""
        try {
            $resp = $_.Exception.Response
            if ($resp) {
                $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
                $detalle = $reader.ReadToEnd()
            }
        }
        catch { }
        throw "Fallo $Method $Path`: $($_.Exception.Message) $detalle"
    }
}

# ------------------------------------------------------------
# 0) Verificar backend
# ------------------------------------------------------------
Write-Step "Verificando backend en $ApiBase ..."
try {
    Invoke-RestMethod -Uri "$ApiBase/api/categorias" -TimeoutSec 5 | Out-Null
    Write-Host "  Backend OK."
}
catch {
    Write-Error "No se puede conectar con el backend en $ApiBase. Levantalo (mvnw spring-boot:run) y volvé a intentar."
    exit 1
}

# ------------------------------------------------------------
# 1) Login de admin (crea categorías/logros/administrador al arrancar)
# ------------------------------------------------------------
Write-Step "Iniciando sesión de administrador ..."
try {
    $admin = Invoke-Api -Method POST -Path "/usuarios/login" `
        -Body @{ email = "admin@lfm.local"; password = "admin123" } -Auth $false
    $script:Token = $admin.token
    Write-Host "  Admin OK ($($admin.usuario.nombrePiloto))."
}
catch {
    Write-Error "No se pudo iniciar sesión como admin@lfm.local. ¿Se levantó el backend con el DataSeeder? $($_.Exception.Message)"
    exit 1
}

# ------------------------------------------------------------
# 2) Asegurar el piloto de prueba (con el GUID del JSON)
# ------------------------------------------------------------
Write-Step "Asegurando piloto de prueba '$NombrePiloto' ..."
$usuarios = Invoke-Api -Path "/usuarios"
$piloto = $usuarios | Where-Object { $_.email -eq $EmailPiloto }
if (-not $piloto) {
    $piloto = Invoke-Api -Method POST -Path "/usuarios/registro" -Body @{
        email        = $EmailPiloto
        password     = $PasswordPiloto
        nombrePiloto = $NombrePiloto
        guidSteam    = $GuidSteam
    } -Auth $false
    Write-Host "  Piloto creado: $NombrePiloto (id $($piloto.id), guid $GuidSteam)."
}
else {
    Write-Host "  Piloto ya existente: $($piloto.nombrePiloto) (id $($piloto.id))."
}

# ------------------------------------------------------------
# 3) Asegurar la carrera FINALIZADA
# ------------------------------------------------------------
Write-Step "Buscando/creando carrera '$CarreraNombre' ..."
$carreras = Invoke-Api -Path "/carreras"
$carrera = $carreras | Where-Object { $_.nombre -eq $CarreraNombre } | Select-Object -First 1

if (-not $carrera) {
    $categorias = Invoke-Api -Path "/categorias"
    $categoria = $categorias | Where-Object { $_.nombre -eq "LFM Open" } | Select-Object -First 1
    if (-not $categoria) { $categoria = $categorias | Select-Object -First 1 }

    $carrera = Invoke-Api -Method POST -Path "/carreras" -Body @{
        nombre            = $CarreraNombre
        fecha             = $FechaCarrera
        circuito          = $Circuito
        categoriaId       = $categoria.id
        estado            = "FINALIZADA"
        cupoMaximo        = 24
        servidor          = "simulacion-servidor"
        contrasenaServidor = ""
    }
    Write-Host "  Carrera creada: '$($carrera.nombre)' (id $($carrera.id), estado $($carrera.estado), categoria '$($carrera.categoriaNombre)')."
}
else {
    Write-Host "  Carrera ya existente: '$($carrera.nombre)' (id $($carrera.id), estado $($carrera.estado))."
}

$carreraId = [long]$carrera.id

# ------------------------------------------------------------
# 4) Importar resultados desde el JSON de sesión RACE
# ------------------------------------------------------------
Write-Step "Cargando resultados desde '$RaceJson' ..."
$resultados = Invoke-Api -Path "/resultados/carrera/$carreraId"
if ($resultados.Count -gt 0) {
    Write-Host "  La carrera ya tiene $($resultados.Count) resultado(s); no se vuelven a importar."
}
else {
    if (-not (Test-Path -LiteralPath $RaceJson)) {
        Write-Error "No se encontró el JSON de sesión: $RaceJson"
        exit 1
    }
    $jsonRaw = Get-Content -LiteralPath $RaceJson -Raw
    $resp = Invoke-Api -Method POST -Path "/sesiones/importar?carreraId=$carreraId" -Body $jsonRaw
    Write-Host "  Sesión importada: tipo '$($resp.tipo)'."
}

# ------------------------------------------------------------
# 5) Asegurar la clasificación (el QUALIFY de prueba no tiene
#    tiempos válidos; se carga la vuelta real de la sesión)
# ------------------------------------------------------------
Write-Step "Asegurando clasificación ..."
$clasificaciones = Invoke-Api -Path "/clasificaciones/carrera/$carreraId"
if ($clasificaciones.Count -gt 0) {
    Write-Host "  La carrera ya tiene $($clasificaciones.Count) tiempo(s) de clasificación."
}
else {
    $auto = $null
    if (Test-Path -LiteralPath $RaceJson) {
        $jsonSesion = Get-Content -LiteralPath $RaceJson -Raw | ConvertFrom-Json
        $auto = $jsonSesion.Cars | Where-Object { $_.Driver.Guid -eq $GuidSteam } | Select-Object -First 1
    }
    Invoke-Api -Method POST -Path "/clasificaciones" -Body @{
        carreraId      = $carreraId
        usuarioId      = $piloto.id
        tiempo         = $TiempoClasificacionMs
        diferenciaPole = 0
        modeloAuto     = if ($auto) { $auto.Model } else { $null }
        skinAuto       = if ($auto) { $auto.Skin } else { $null }
    } | Out-Null
    Write-Host "  Clasificación creada para $NombrePiloto (tiempo $TiempoClasificacionMs ms)."
}

# ------------------------------------------------------------
# 6) Resumen
# ------------------------------------------------------------
Write-Step "Simulación lista"
Write-Host ""
Write-Host "  Carrera : $CarreraNombre (id $carreraId, estado FINALIZADA)"
Write-Host "  Piloto  : $NombrePiloto (id $($piloto.id))"
Write-Host ""
Write-Host "  Abrí el detalle de la carrera:"
Write-Host "    http://localhost:8080/04-race-detail.html?id=$carreraId"
Write-Host "    (o desde el repo: files\04-race-detail.html?id=$carreraId)"
Write-Host ""
Write-Host "  En la lista de carreras, pestaña 'Pasadas':"
Write-Host "    http://localhost:8080/03-races-list.html"
Write-Host ""
Write-Host "  Para una nueva simulación con otro nombre: -CarreraNombre 'Carrera Simulada 2'" -ForegroundColor DarkGray
