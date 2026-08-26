# Diagrama de Secuencia: Inscripción a Carrera
. "C:\Facultad\ProyectoProgra\LowFuelMotorSport Local\docs\seq-helpers.ps1"

Seq-Header "Secuencia: Inscripción a Carrera" 1600 1400

$pUsuario = Seq-Participant "USUARIO" 50 40 $true
$pSistema = Seq-Participant ":Sistema" 250 40 $false
$pCarrera = Seq-Participant ":Carrera" 450 40 $false
$pInscrip = Seq-Participant ":Inscripcion" 650 40 $false
$pNotif   = Seq-Participant ":Notificacion" 850 40 $false

$y = 130

Seq-Message $pUsuario $pSistema "inscribirse(carreraId)" $y
$y += 40

Seq-Message $pSistema $pCarrera "buscar(carreraId)" $y
$y += 40
Seq-Message $pCarrera $pSistema "carrera" $y $true
$y += 40

# Alt: estado PROGRAMADA
Seq-Alt "alt [estado == PROGRAMADA]" 200 $y 700 300
$y += 30

Seq-Message $pSistema $pSistema "validarFecha(carrera.fecha)" $y
$y += 30
Seq-Note $pSistema "RF-073: cierre 5 min antes" 250 $y
$y += 50

# Alt: fecha válida
Seq-Alt "alt [fecha < inicio - 5min]" 200 $y 700 160
$y += 30

Seq-Message $pSistema $pSistema "validarCupo(carrera.cupoMaximo)" $y
$y += 30

# Alt: cupo disponible
Seq-Alt "alt [cupo > 0]" 200 $y 700 80
$y += 25
Seq-Message $pSistema $pInscrip "crear(usuario, carrera, INSCRIPTO)" $y
$y += 25
Seq-Message $pInscrip $pSistema "inscripcion" $y $true
$y += 30

# Else: lista de espera
Seq-Alt "else [cupo == 0]" 200 $y 700 60
$y += 20
Seq-Message $pSistema $pInscrip "crear(usuario, carrera, LISTA_ESPERA)" $y
$y += 20
Seq-Note $pInscrip "RF-033: lista de espera" 650 $y
$y += 30

Seq-Message $pSistema $pNotif "notificar(INSCRIPCION)" $y
$y += 30

# Else: cierres
Seq-Else $y
$y += 30
Seq-Message $pSistema $pUsuario "error: inscripciones cerradas" $y $true
$y += 40

# Footer
$y += 10
Seq-Note $pUsuario "RF-030: Inscribirse a carrera" 50 $y

Seq-Footer

$out = "C:\Facultad\ProyectoProgra\LowFuelMotorSport Local\docs\uml-seq-inscripcion.drawio"
Set-Content -Path $out -Value $script:xml.ToString() -Encoding UTF8
Write-Host "Generado: $out ($([math]::Round((Get-Item $out).Length/1024,1)) KB)"
