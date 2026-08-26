# Generador de Diagrama de Clases UML - Low Fuel Motorsport
# Genera un archivo .drawio con 28 entidades + 14 enums + relaciones

$script:entityId = 2
$script:edgeId = 1000
$script:enumId = 500
$script:xml = [System.Text.StringBuilder]::new()

function SB($text) {
    [void]$script:xml.AppendLine($text)
}

function Add-Entity($name, $x, $y, $fields) {
    $h = 80 + ($fields.Count * 22)
    $attrLines = @()
    foreach ($f in $fields) {
        $attrLines += "+ $($f.name): $($f.type)"
    }
    $attrText = $attrLines -join "&#xa;"
    $id = $script:entityId++
    
    SB "    <mxCell id=`"$id`" value=`"&lt;b&gt;$name&lt;/b&gt;`" style=`"swimlane;fontStyle=1;align=center;startSize=26;fillColor=#dae8fc;strokeColor=#6c8ebf;swimlaneLine=1;collapsible=0;container=1;dropTarget=0;childLayout=stackLayout;horizontal=1;resizeParent=1;resizeParentMax=0;`" vertex=`"1`" parent=`"1`">"
    SB "      <mxGeometry x=`"$x`" y=`"$y`" width=`"260`" height=`"$h`" as=`"geometry`" />"
    SB "    </mxCell>"
    SB "    <mxCell id=`"$($id+100)`" value=`"$attrText`" style=`"text;strokeColor=none;fillColor=none;align=left;verticalAlign=top;spacingLeft=4;spacingRight=4;overflow=hidden;rotatable=0;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;fontSize=12;fontFamily=Courier New;html=1;`" vertex=`"1`" parent=`"$id`">"
    SB "      <mxGeometry y=`"26`" width=`"260`" height=`"$($h - 26)`" as=`"geometry`" />"
    SB "    </mxCell>"
    return $id
}

function Add-Enum($name, $values, $x, $y) {
    $valLines = @()
    foreach ($v in $values) { $valLines += "+ $v" }
    $valText = $valLines -join "&#xa;"
    $h = 60 + ($values.Count * 20)
    $id = $script:enumId++
    
    SB "    <mxCell id=`"$id`" value=`"&lt;b&gt;&amp;lt;&amp;lt;enumeration&amp;gt;&amp;gt;&lt;br&gt;$name&lt;/b&gt;`" style=`"swimlane;fontStyle=1;align=center;startSize=40;fillColor=#fff2cc;strokeColor=#d6b656;swimlaneLine=1;collapsible=0;container=1;dropTarget=0;childLayout=stackLayout;horizontal=1;`" vertex=`"1`" parent=`"1`">"
    SB "      <mxGeometry x=`"$x`" y=`"$y`" width=`"220`" height=`"$h`" as=`"geometry`" />"
    SB "    </mxCell>"
    SB "    <mxCell id=`"$($id+500)`" value=`"$valText`" style=`"text;strokeColor=none;fillColor=none;align=left;verticalAlign=top;spacingLeft=4;spacingRight=4;overflow=hidden;rotatable=0;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;fontSize=11;fontFamily=Courier New;html=1;`" vertex=`"1`" parent=`"$id`">"
    SB "      <mxGeometry y=`"40`" width=`"220`" height=`"$($h - 40)`" as=`"geometry`" />"
    SB "    </mxCell>"
    return $id
}

function Add-OneToMany($src, $tgt, $label) {
    $id = $script:edgeId++
    $l = if ($label) { $label } else { "" }
    SB "    <mxCell id=`"$id`" value=`"$l`" style=`"edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;exitX=0.5;exitY=0.5;exitDx=0;exitDy=0;entryX=0.5;entryY=0.5;entryDx=0;entryDy=0;endArrow=open;endFill=0;startArrow=diamondThin;startFill=0;fontSize=11;`" edge=`"1`" parent=`"1`" source=`"$src`" target=`"$tgt`">"
    SB "      <mxGeometry relative=`"1`" as=`"geometry`" />"
    SB "    </mxCell>"
}

function Add-OneToOne($src, $tgt, $label) {
    $id = $script:edgeId++
    $l = if ($label) { $label } else { "" }
    SB "    <mxCell id=`"$id`" value=`"$l`" style=`"edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;exitX=0.5;exitY=0.5;exitDx=0;exitDy=0;entryX=0.5;entryY=0.5;entryDx=0;entryDy=0;endArrow=open;endFill=0;startArrow=open;startFill=0;fontSize=11;`" edge=`"1`" parent=`"1`" source=`"$src`" target=`"$tgt`">"
    SB "      <mxGeometry relative=`"1`" as=`"geometry`" />"
    SB "    </mxCell>"
}

function Add-ManyToOne($src, $tgt, $label) {
    $id = $script:edgeId++
    $l = if ($label) { $label } else { "" }
    SB "    <mxCell id=`"$id`" value=`"$l`" style=`"edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;exitX=0.5;exitY=0.5;exitDx=0;exitDy=0;entryX=0.5;entryY=0.5;entryDx=0;entryDy=0;endArrow=diamondThin;endFill=0;startArrow=open;startFill=0;fontSize=11;`" edge=`"1`" parent=`"1`" source=`"$src`" target=`"$tgt`">"
    SB "      <mxGeometry relative=`"1`" as=`"geometry`" />"
    SB "    </mxCell>"
}

# ═══════════════════════════════════════════════════════════════════════
# HEADER
# ═══════════════════════════════════════════════════════════════════════

SB '<?xml version="1.0" encoding="UTF-8"?>'
SB '<mxfile host="app.diagrams.net" modified="2026-08-26T14:00:00.000Z" agent="5.0" version="24.0.0" type="device">'
SB '  <diagram name="Diagrama de Clases" id="uml-class-diagram">'
SB '    <mxGraphModel dx="2400" dy="1800" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="3000" pageHeight="2200" math="0" shadow="0">'
SB '      <root>'
SB '        <mxCell id="0" />'
SB '        <mxCell id="1" parent="0" />'

# ═══════════════════════════════════════════════════════════════════════
# ENTIDADES - Fila 1: Categoria, Campeonato, Usuario, Carrera, Archivo, Vuelta
# ═══════════════════════════════════════════════════════════════════════

$idCategoria = Add-Entity "Categoria" 50 80 @(
    @{name="- id"; type="Long"},
    @{name="- nombre"; type="String"},
    @{name="- descripcion"; type="String"},
    @{name="- eloMinimo"; type="Integer"},
    @{name="- eloMaximo"; type="Integer"},
    @{name="- setupAbierto"; type="Boolean"},
    @{name="- setupFijo"; type="Boolean"}
)

$idCampeonato = Add-Entity "Campeonato" 380 80 @(
    @{name="- id"; type="Long"},
    @{name="- nombre"; type="String"},
    @{name="- temporada"; type="String"},
    @{name="- estado"; type="EstadoCampeonato"},
    @{name="- sistemaPuntos"; type="String"}
)

$idUsuario = Add-Entity "Usuario" 750 80 @(
    @{name="- id"; type="Long"},
    @{name="- email"; type="String"},
    @{name="- password"; type="String"},
    @{name="- nombrePiloto"; type="String"},
    @{name="- fotoPerfil"; type="String"},
    @{name="- guidSteam"; type="String"},
    @{name="- elo"; type="Integer"},
    @{name="- safetyRating"; type="Integer"},
    @{name="- version"; type="Long"},
    @{name="- rol"; type="Rol"},
    @{name="- fechaRegistro"; type="LocalDateTime"}
)

$idCarrera = Add-Entity "Carrera" 1120 80 @(
    @{name="- id"; type="Long"},
    @{name="- nombre"; type="String"},
    @{name="- fecha"; type="LocalDateTime"},
    @{name="- circuito"; type="String"},
    @{name="- estado"; type="EstadoCarrera"},
    @{name="- cupoMaximo"; type="Integer"},
    @{name="- servidor"; type="String"},
    @{name="- contrasenaServidor"; type="String"},
    @{name="- linkPista"; type="String"},
    @{name="- linkAuto"; type="String"}
)

$idArchivoCarrera = Add-Entity "ArchivoCarrera" 1480 80 @(
    @{name="- id"; type="Long"},
    @{name="- nombre"; type="String"},
    @{name="- ruta"; type="String"},
    @{name="- tipo"; type="TipoArchivo"}
)

$idVueltaCarrera = Add-Entity "VueltaCarrera" 1800 80 @(
    @{name="- id"; type="Long"},
    @{name="- numeroVuelta"; type="Integer"},
    @{name="- tiempoMs"; type="Long"},
    @{name="- sector1"; type="Long"},
    @{name="- sector2"; type="Long"},
    @{name="- sector3"; type="Long"},
    @{name="- cortes"; type="Integer"},
    @{name="- neumatico"; type="String"},
    @{name="- tipo"; type="String"}
)

# ═══════════════════════════════════════════════════════════════════════
# ENTIDADES - Fila 2: CampeonatoPos, ResultadoCarrera, Inscripcion, SesionClasif, SesionProc
# ═══════════════════════════════════════════════════════════════════════

$idCampeonatoPos = Add-Entity "CampeonatoPosicion" 380 400 @(
    @{name="- id"; type="Long"},
    @{name="- puntos"; type="Integer"},
    @{name="- posicion"; type="Integer"}
)

$idResultadoCarrera = Add-Entity "ResultadoCarrera" 720 400 @(
    @{name="- id"; type="Long"},
    @{name="- posicionFinal"; type="Integer"},
    @{name="- tiempoTotal"; type="Long"},
    @{name="- vueltaRapida"; type="Long"},
    @{name="- modeloAuto"; type="String"},
    @{name="- skinAuto"; type="String"},
    @{name="- poles"; type="boolean"},
    @{name="- finalizo"; type="boolean"},
    @{name="- eloGanado"; type="Integer"},
    @{name="- srGanado"; type="Integer"}
)

$idInscripcion = Add-Entity "Inscripcion" 1060 400 @(
    @{name="- id"; type="Long"},
    @{name="- estado"; type="EstadoInscripcion"},
    @{name="- fechaInscripcion"; type="LocalDateTime"}
)

$idSesionClasificacion = Add-Entity "SesionClasificacion" 1400 400 @(
    @{name="- id"; type="Long"},
    @{name="- fecha"; type="LocalDateTime"},
    @{name="- tiempo"; type="Long"},
    @{name="- diferenciaPole"; type="Long"},
    @{name="- modeloAuto"; type="String"},
    @{name="- skinAuto"; type="String"}
)

$idSesionProcesada = Add-Entity "SesionProcesada" 1740 400 @(
    @{name="- id"; type="Long"},
    @{name="- nombreArchivo"; type="String"},
    @{name="- tipo"; type="String"},
    @{name="- fechaProcesamiento"; type="LocalDateTime"}
)

# ═══════════════════════════════════════════════════════════════════════
# ENTIDADES - Fila 3: INCIDENTES (abajo-izquierda)
# ═══════════════════════════════════════════════════════════════════════

$idIncidente = Add-Entity "Incidente" 50 650 @(
    @{name="- id"; type="Long"},
    @{name="- vuelta"; type="Integer"},
    @{name="- descripcion"; type="String"},
    @{name="- videoUrl"; type="String"},
    @{name="- fecha"; type="LocalDateTime"},
    @{name="- estado"; type="EstadoIncidente"}
)

$idIncidentePiloto = Add-Entity "IncidentePiloto" 50 900 @(
    @{name="- id"; type="Long"},
    @{name="- rol"; type="RolPilotoIncidente"}
)

$idVotoComisario = Add-Entity "VotoComisario" 350 900 @(
    @{name="- id"; type="Long"},
    @{name="- decision"; type="DecisionComisario"},
    @{name="- comentario"; type="String"},
    @{name="- fecha"; type="LocalDateTime"}
)

$idResolucionIncidente = Add-Entity "ResolucionIncidente" 650 900 @(
    @{name="- id"; type="Long"},
    @{name="- explicacion"; type="String"},
    @{name="- fecha"; type="LocalDateTime"}
)

# ═══════════════════════════════════════════════════════════════════════
# ENTIDADES - Fila 4: SANCIONES (abajo-centro)
# ═══════════════════════════════════════════════════════════════════════

$idSancion = Add-Entity "Sancion" 50 1180 @(
    @{name="- id"; type="Long"},
    @{name="- tipo"; type="TipoSancion"},
    @{name="- valor"; type="Integer"},
    @{name="- motivo"; type="String"},
    @{name="- origen"; type="OrigenSancion"},
    @{name="- idExterno"; type="String"},
    @{name="- fecha"; type="LocalDateTime"},
    @{name="- efectosAplicados"; type="Boolean"}
)

$idApelacion = Add-Entity "Apelacion" 350 1180 @(
    @{name="- id"; type="Long"},
    @{name="- motivo"; type="String"},
    @{name="- estado"; type="EstadoApelacion"},
    @{name="- respuestaAdmin"; type="String"},
    @{name="- fecha"; type="LocalDateTime"}
)

$idEloSancion = Add-Entity "EloSancion" 650 1180 @(
    @{name="- id"; type="Long"},
    @{name="- cambio"; type="Integer"},
    @{name="- motivo"; type="String"},
    @{name="- fecha"; type="LocalDateTime"}
)

$idSrSancion = Add-Entity "SafetyRatingSancion" 950 1180 @(
    @{name="- id"; type="Long"},
    @{name="- cambio"; type="Integer"},
    @{name="- motivo"; type="String"},
    @{name="- fecha"; type="LocalDateTime"}
)

# ═══════════════════════════════════════════════════════════════════════
# ENTIDADES - Fila 5: SETUPS, LOGROS, RECOMPENSAS
# ═══════════════════════════════════════════════════════════════════════

$idSetup = Add-Entity "Setup" 50 1500 @(
    @{name="- id"; type="Long"},
    @{name="- titulo"; type="String"},
    @{name="- descripcion"; type="String"},
    @{name="- circuito"; type="String"},
    @{name="- vehiculo"; type="String"},
    @{name="- archivo"; type="String"},
    @{name="- fechaPublicacion"; type="LocalDateTime"},
    @{name="- promedioCalificacion"; type="Double"}
)

$idSetupCalificacion = Add-Entity "SetupCalificacion" 350 1500 @(
    @{name="- id"; type="Long"},
    @{name="- puntaje"; type="Integer"}
)

$idSetupComentario = Add-Entity "SetupComentario" 550 1500 @(
    @{name="- id"; type="Long"},
    @{name="- texto"; type="String"},
    @{name="- fecha"; type="LocalDateTime"}
)

$idLogro = Add-Entity "Logro" 830 1500 @(
    @{name="- id"; type="Long"},
    @{name="- nombre"; type="String"},
    @{name="- descripcion"; type="String"},
    @{name="- tipoCondicion"; type="TipoCondicionLogro"},
    @{name="- valorCondicion"; type="Integer"},
    @{name="- icono"; type="String"}
)

$idRecompensa = Add-Entity "Recompensa" 1130 1500 @(
    @{name="- id"; type="Long"},
    @{name="- descripcion"; type="String"},
    @{name="- tipo"; type="TipoRecompensa"}
)

$idUsuarioLogro = Add-Entity "UsuarioLogro" 830 1750 @(
    @{name="- id"; type="Long"},
    @{name="- progreso"; type="Integer"},
    @{name="- obtenido"; type="Boolean"},
    @{name="- fechaObtencion"; type="LocalDateTime"}
)

$idUsuarioRecompensa = Add-Entity "UsuarioRecompensa" 1130 1750 @(
    @{name="- id"; type="Long"},
    @{name="- reclamada"; type="Boolean"},
    @{name="- fecha"; type="LocalDateTime"}
)

# ═══════════════════════════════════════════════════════════════════════
# ENTIDADES - Fila 6: NOTIFICACION, ANUNCIO
# ═══════════════════════════════════════════════════════════════════════

$idNotificacion = Add-Entity "Notificacion" 1450 1500 @(
    @{name="- id"; type="Long"},
    @{name="- tipo"; type="TipoNotificacion"},
    @{name="- mensaje"; type="String"},
    @{name="- leida"; type="Boolean"},
    @{name="- fecha"; type="LocalDateTime"},
    @{name="- link"; type="String"}
)

$idAnuncio = Add-Entity "Anuncio" 1750 1500 @(
    @{name="- id"; type="Long"},
    @{name="- titulo"; type="String"},
    @{name="- contenido"; type="String"},
    @{name="- urlImagen"; type="String"},
    @{name="- fecha"; type="LocalDateTime"},
    @{name="- destacado"; type="Boolean"}
)

# ═══════════════════════════════════════════════════════════════════════
# ENUMS (columna derecha, a partir de y=650)
# ═══════════════════════════════════════════════════════════════════════

$idRol = Add-Enum "Rol" @("USUARIO","ADMIN","COMISARIO") 1500 650
$idEstadoCampeonato = Add-Enum "EstadoCampeonato" @("ACTIVO","CERRADO") 1740 650
$idEstadoCarrera = Add-Enum "EstadoCarrera" @("PROGRAMADA","INSCRIPCIONES_ABIERTAS","INSCRIPCIONES_CERRADAS","EN_CURSO","FINALIZADA","CANCELADA") 1500 760
$idEstadoInscripcion = Add-Enum "EstadoInscripcion" @("INSCRIPTO","CANCELADA","LISTA_ESPERA") 1740 920
$idEstadoIncidente = Add-Enum "EstadoIncidente" @("PENDIENTE","EN_ANALISIS","RESUELTO") 1500 940
$idEstadoApelacion = Add-Enum "EstadoApelacion" @("PENDIENTE","APROBADA","RECHAZADA") 1740 1040
$idTipoSancion = Add-Enum "TipoSancion" @("PUESTOS","SEGUNDOS","DRIVE_THROUGH","STOP_AND_GO","DESCALIFICACION","ELO","SAFETY_RATING") 1500 1060
$idOrigenSancion = Add-Enum "OrigenSancion" @("REAL_PENALTY","COMISARIO","ADMIN") 1740 1230
$idRolPilotoIncidente = Add-Enum "RolPilotoIncidente" @("CAUSANTE","AFECTADO") 1500 1260
$idDecisionComisario = Add-Enum "DecisionComisario" @("A_FAVOR","EN_CONTRA","ABSTENCION") 1740 1340
$idTipoNotificacion = Add-Enum "TipoNotificacion" @("CARRERA_INICIO","PENALIZACION","LOGRO","RECOMPENSA","ANUNCIO","INCIDENTE","APELACION") 1500 1380
$idTipoCondicionLogro = Add-Enum "TipoCondicionLogro" @("VICTORIAS","PODIOS","CARRERAS","POLES","VUELTAS_RAPIDAS","CARRERAS_COMPLETADAS","ELO") 1740 1520
$idTipoRecompensa = Add-Enum "TipoRecompensa" @("VIRTUAL","FISICA","DESCUENTO","OTRA") 1740 1730
$idTipoArchivo = Add-Enum "TipoArchivo" @("SETUP","PAQUETE","CARGA","OTRO") 1500 1630

# ═══════════════════════════════════════════════════════════════════════
# RELACIONES
# ═══════════════════════════════════════════════════════════════════════

# Categoria 1──N Campeonato
Add-OneToMany $idCategoria $idCampeonato ""
# Campeonato 1──N CampeonatoPosicion
Add-OneToMany $idCampeonato $idCampeonatoPos ""
# Campeonato 1──N Carrera
Add-OneToMany $idCampeonato $idCarrera ""
# Carrera 1──N Inscripcion
Add-OneToMany $idCarrera $idInscripcion ""
# Carrera 1──N ResultadoCarrera
Add-OneToMany $idCarrera $idResultadoCarrera ""
# Carrera 1──N Incidente
Add-OneToMany $idCarrera $idIncidente ""
# Carrera 1──N Sancion
Add-OneToMany $idCarrera $idSancion ""
# Carrera 1──N EloSancion
Add-OneToMany $idCarrera $idEloSancion ""
# Carrera 1──N SafetyRatingSancion
Add-OneToMany $idCarrera $idSrSancion ""
# Carrera 1──N VueltaCarrera
Add-OneToMany $idCarrera $idVueltaCarrera ""
# Carrera 1──N SesionClasificacion
Add-OneToMany $idCarrera $idSesionClasificacion ""
# Carrera 1──N SesionProcesada
Add-OneToMany $idCarrera $idSesionProcesada ""
# ArchivoCarrera 1──N Carrera
Add-OneToMany $idArchivoCarrera $idCarrera ""

# Usuario ──< (N) Inscripcion
Add-OneToMany $idUsuario $idInscripcion ""
# Usuario ──< (N) ResultadoCarrera
Add-OneToMany $idUsuario $idResultadoCarrera ""
# Usuario ──< (N) Sancion
Add-OneToMany $idUsuario $idSancion ""
# Usuario ──< (N) EloSancion
Add-OneToMany $idUsuario $idEloSancion ""
# Usuario ──< (N) SafetyRatingSancion
Add-OneToMany $idUsuario $idSrSancion ""
# Usuario ──< (N) Setup
Add-OneToMany $idUsuario $idSetup ""
# Usuario ──< (N) Notificacion
Add-OneToMany $idUsuario $idNotificacion ""

# Incidente.reportante -> Usuario
Add-ManyToOne $idIncidente $idUsuario ""
# Incidente 1──N IncidentePiloto
Add-OneToMany $idIncidente $idIncidentePiloto ""
# Incidente 1──N VotoComisario
Add-OneToMany $idIncidente $idVotoComisario ""
# Incidente 1──1 ResolucionIncidente
Add-OneToOne $idIncidente $idResolucionIncidente ""

# ResolucionIncidente.comisario -> Usuario
Add-ManyToOne $idResolucionIncidente $idUsuario ""
# IncidentePiloto.usuario -> Usuario
Add-ManyToOne $idIncidentePiloto $idUsuario ""
# VotoComisario.comisario -> Usuario
Add-ManyToOne $idVotoComisario $idUsuario ""

# Sancion.resolucion -> ResolucionIncidente
Add-ManyToOne $idSancion $idResolucionIncidente ""
# Sancion 1──N Apelacion
Add-OneToMany $idSancion $idApelacion ""
# Apelacion.usuario -> Usuario
Add-ManyToOne $idApelacion $idUsuario ""

# Setup.categoria -> Categoria
Add-ManyToOne $idSetup $idCategoria ""
# Setup 1──N SetupCalificacion
Add-OneToMany $idSetup $idSetupCalificacion ""
# Setup 1──N SetupComentario
Add-OneToMany $idSetup $idSetupComentario ""
# SetupCalificacion.usuario -> Usuario
Add-ManyToOne $idSetupCalificacion $idUsuario ""
# SetupComentario.usuario -> Usuario
Add-ManyToOne $idSetupComentario $idUsuario ""

# Logro 1──N Recompensa
Add-OneToMany $idLogro $idRecompensa ""
# Logro 1──N UsuarioLogro
Add-OneToMany $idLogro $idUsuarioLogro ""
# Recompensa 1──N UsuarioRecompensa
Add-OneToMany $idRecompensa $idUsuarioRecompensa ""
# UsuarioLogro.usuario -> Usuario
Add-ManyToOne $idUsuarioLogro $idUsuario ""
# UsuarioRecompensa.usuario -> Usuario
Add-ManyToOne $idUsuarioRecompensa $idUsuario ""
# CampeonatoPosicion.usuario -> Usuario
Add-ManyToOne $idCampeonatoPos $idUsuario ""

# ═══════════════════════════════════════════════════════════════════════
# FOOTER
# ═══════════════════════════════════════════════════════════════════════

SB '      </root>'
SB '    </mxGraphModel>'
SB '  </diagram>'
SB '</mxfile>'

# ═══════════════════════════════════════════════════════════════════════
# ESCRIBIR ARCHIVO
# ═══════════════════════════════════════════════════════════════════════

$outputPath = "C:\Facultad\ProyectoProgra\LowFuelMotorSport Local\docs\uml-diagrama-clases.drawio"
Set-Content -Path $outputPath -Value $script:xml.ToString() -Encoding UTF8
$size = (Get-Item $outputPath).Length
Write-Host "Generado: $outputPath"
Write-Host "Tamano: $([math]::Round($size/1024, 1)) KB"
Write-Host "Entidades: $($script:entityId - 1)"
Write-Host "Enums: $($script:enumId - 500)"
Write-Host "Relaciones: $($script:edgeId - 1000)"
