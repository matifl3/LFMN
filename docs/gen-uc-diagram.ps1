# Generador de Diagrama de Casos de UML - Low Fuel Motorsport
$script:uid = 2
$script:xml = [System.Text.StringBuilder]::new()
function SB($t) { [void]$script:xml.AppendLine($t) }

function Add-Actor($name, $x, $y) {
    $id = $script:uid++
    # Stick figure: head (ellipse) + body (line) + arms (line) + legs (line)
    SB "    <mxCell id=`"$id`" value=`"$name`" style={`"shape=umlActor`";`"verticalLabelPosition=bottom`";`"verticalAlign=top`";`"align=center`";`"html=1`";`"outline=none`";`"fillColor=#000000`";`"strokeColor=#000000`";} vertex=`"1`" parent=`"1`">"
    SB "      <mxGeometry x=`"$x`" y=`"$y`" width=`"30`" height=`"60`" as=`"geometry`" />"
    SB "    </mxCell>"
    return $id
}

function Add-UC($id_val, $label, $x, $y) {
    $id = $script:uid++
    SB "    <mxCell id=`"$id`" value=`"$label`" style=`"ellipse;whiteSpace=wrap;html=1;aspect=fixed;fontSize=11;fillColor=#d5e8d4;strokeColor=#82b366;`" vertex=`"1`" parent=`"1`">"
    SB "      <mxGeometry x=`"$x`" y=`"$y`" width=`"200`" height=`"50`" as=`"geometry`" />"
    SB "    </mxCell>"
    return $id
}

function Add-Line($src, $tgt) {
    $id = $script:uid++
    SB "    <mxCell id=`"$id`" value=`"`" style=`"endArrow=none;dashed=1;html=1;`" edge=`"1`" parent=`"1`" source=`"$src`" target=`"$tgt`">"
    SB "      <mxGeometry relative=`"1`" as=`"geometry`" />"
    SB "    </mxCell>"
}

function Add-Association($src, $tgt) {
    $id = $script:uid++
    SB "    <mxCell id=`"$id`" value=`"`" style=`"endArrow=open;startFill=0;endFill=0;html=1;`" edge=`"1`" parent=`"1`" source=`"$src`" target=`"$tgt`">"
    SB "      <mxGeometry relative=`"1`" as=`"geometry`" />"
    SB "    </mxCell>"
}

function Add-Include($src, $tgt) {
    $id = $script:uid++
    SB "    <mxCell id=`"$id`" value=`"<<include>>`" style=`"endArrow=open;endFill=0;dashed=1;html=1;fontSize=9;fontColor=#666666;`" edge=`"1`" parent=`"1`" source=`"$src`" target=`"$tgt`">"
    SB "      <mxGeometry relative=`"1`" as=`"geometry`" />"
    SB "    </mxCell>"
}

function Add-Extend($src, $tgt) {
    $id = $script:uid++
    SB "    <mxCell id=`"$id`" value=`"<<extend>>`" style=`"endArrow=open;endFill=0;dashed=1;html=1;fontSize=9;fontColor=#666666;`" edge=`"1`" parent=`"1`" source=`"$src`" target=`"$tgt`">"
    SB "      <mxGeometry relative=`"1`" as=`"geometry`" />"
    SB "    </mxCell>"
}

# ═══ HEADER ═══
SB '<?xml version="1.0" encoding="UTF-8"?>'
SB '<mxfile host="app.diagrams.net" modified="2026-08-26T14:00:00.000Z" agent="5.0" version="24.0.0" type="device">'
SB '  <diagram name="Casos de Uso" id="uc-diagram">'
SB '    <mxGraphModel dx="2400" dy="1800" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="2800" pageHeight="2000" math="0" shadow="0">'
SB '      <root>'
SB '        <mxCell id="0" />'
SB '        <mxCell id="1" parent="0" />'

# ═══ SYSTEM BOUNDARY ═══
SB '    <mxCell id="2" value="&lt;b&gt;Low Fuel Motorsport&lt;/b&gt;" style="points=[[0,0],[0.25,0],[0.5,0],[0.75,0],[1,0],[1,0.25],[1,0.5],[1,0.75],[1,1],[0.75,1],[0.5,1],[0.25,1],[0,1],[0,0.75],[0,0.5],[0,0.25]];outlineConnect=0;gradientColor=none;html=1;whiteSpace=wrap;fontSize=14;fontStyle=1;shape=mxgraph.basic.rect;fillColor=none;strokeColor=#6c8ebf;strokeWidth=2;align=center;verticalAlign=top;container=1;dropTarget=0;collapsible=0;recursiveResize=0;arcSize=3;" vertex="1" parent="1">'
SB '      <mxGeometry x="250" y="20" width="1800" height="1960" as="geometry" />'
SB '    </mxCell>'

# ═══ ACTORS ═══
$aUsuario = Add-Actor "USUARIO" 30 500
$aAdmin = Add-Actor "ADMIN" 1960 500
$aComisario = Add-Actor "COMISARIO" 1960 1300

# ═══ USE CASES - USUARIO ═══

# A. Cuenta y Sesión
$uc001 = Add-UC "UC" "RF-001: Registro de usuario" 350 40
$uc002 = Add-UC "UC" "RF-002: Inicio de sesión" 350 100
$uc003 = Add-UC "UC" "RF-003: Login con Steam" 350 160
$uc004 = Add-UC "UC" "RF-004: Cambiar contraseña" 350 220
$uc005 = Add-UC "UC" "RF-005: Vincular/Desvincular Steam" 350 280
$uc006 = Add-UC "UC" "RF-006: Editar perfil" 350 340
$uc007 = Add-UC "UC" "RF-007: Cambiar foto de perfil" 350 400

# B. Perfil y Estadísticas
$uc008 = Add-UC "UC" "RF-008: Ver perfil propio" 350 470
$uc009 = Add-UC "UC" "RF-009: Ver Elo actual" 350 530
$uc010 = Add-UC "UC" "RF-010: Ver Safety Rating" 350 590
$uc011 = Add-UC "UC" "RF-011: Ver estadísticas" 350 650
$uc018 = Add-UC "UC" "RF-018: Historial de Elo" 350 710
$uc019 = Add-UC "UC" "RF-019: Historial de SR" 350 770
$uc020 = Add-UC "UC" "RF-020: Ver penalizaciones" 350 830

# C. Categorías
$uc021 = Add-UC "UC" "RF-021: Ver categorías" 350 900
$uc022 = Add-UC "UC" "RF-022/023: Categorías por Elo" 350 960

# D. Carreras
$uc024 = Add-UC "UC" "RF-024: Ver próximas carreras" 350 1030
$uc025 = Add-UC "UC" "RF-025: Ver resultados carreras" 350 1090
$uc026 = Add-UC "UC" "RF-026: Ver inscriptos" 350 1150
$uc027 = Add-UC "UC" "RF-027/028: Ver servidor/contraseña" 350 1210
$uc029 = Add-UC "UC" "RF-029: Ver/descargar archivos" 350 1270
$uc030 = Add-UC "UC" "RF-030: Inscribirse a carrera" 350 1330
$uc031 = Add-UC "UC" "RF-031: Bajarse de carrera" 350 1390
$uc033 = Add-UC "UC" "RF-033: Lista de espera" 350 1450

# E. Campeonato
$uc035 = Add-UC "UC" "RF-035: Ver campeonato" 350 1520

# F. Elo y SR
$uc038 = Add-UC "UC" "RF-038/039: Elo estimado" 350 1590

# G. Setups
$uc043 = Add-UC "UC" "RF-043: Crear/publicar setup" 350 1660
$uc046 = Add-UC "UC" "RF-046: Descargar setups" 350 1720
$uc047 = Add-UC "UC" "RF-047: Calificar setup" 350 1780
$uc048 = Add-UC "UC" "RF-048: Comentar setup" 350 1840
$uc049 = Add-UC "UC" "RF-049: Buscar setups" 350 1900

# H. Incidentes (reporte)
$uc052 = Add-UC "UC" "RF-052: Reportar incidente" 700 40
$uc053 = Add-UC "UC" "RF-053: Subir evidencia" 700 100
$uc054 = Add-UC "UC" "RF-054: Consultar estado reporte" 700 160
$uc055 = Add-UC "UC" "RF-055: Ver resolución" 700 220

# I. Logros y Recompensas
$uc056 = Add-UC "UC" "RF-056: Ver logros" 700 290
$uc057 = Add-UC "UC" "RF-057: Ver progreso logros" 700 350
$uc059 = Add-UC "UC" "RF-059: Ver recompensas" 700 410
$uc060 = Add-UC "UC" "RF-060: Reclamar recompensa" 700 470

# J. Notificaciones y Anuncios
$uc061 = Add-UC "UC" "RF-061: Notif. carrera" 700 540
$uc062 = Add-UC "UC" "RF-062: Notif. penalización" 700 600
$uc063 = Add-UC "UC" "RF-063: Notif. logro" 700 660
$uc064 = Add-UC "UC" "RF-064: Ver anuncios" 700 720

# K. Apelaciones
$uc065 = Add-UC "UC" "RF-065: Apelar sanción" 700 790

# ═══ USE CASES - ADMIN ═══

# L. Usuarios
$uc066 = Add-UC "UC" "RF-066: ABM de usuarios" 1100 40

# M. Categorías
$uc068 = Add-UC "UC" "RF-068: Crear categorías" 1100 110
$uc069 = Add-UC "UC" "RF-069: Editar categorías" 1100 170

# N. Carreras
$uc070 = Add-UC "UC" "RF-070: ABM de carreras" 1100 240
$uc072 = Add-UC "UC" "RF-072: Asignar servidores" 1100 300
$uc074 = Add-UC "UC" "RF-074: Cargar resultados" 1100 360

# O. Campeonatos
$uc075 = Add-UC "UC" "RF-075: Crear campeonatos" 1100 430
$uc076 = Add-UC "UC" "RF-076: Editar/cerrar campeonatos" 1100 490

# P. Penalizaciones
$uc077 = Add-UC "UC" "RF-077/078: Penalizar (puestos/seg)" 1100 560
$uc079 = Add-UC "UC" "RF-079: Dar/quitar Elo" 1100 620

# Q. Apelaciones
$uc080 = Add-UC "UC" "RF-080: Resolver apelaciones" 1100 690

# R. Logros y Anuncios
$uc081 = Add-UC "UC" "RF-081: ABM de logros" 1100 760
$uc082 = Add-UC "UC" "RF-082: Agregar recompensas" 1100 820
$uc083 = Add-UC "UC" "RF-083: Publicar anuncios" 1100 880

# S. Incidentes y Stats
$uc084 = Add-UC "UC" "RF-084: Asignar incidente" 1100 950
$uc085 = Add-UC "UC" "RF-085: Ver estadísticas" 1100 1010
$uc086 = Add-UC "UC" "RF-086: Moderar setups" 1100 1070
$uc087 = Add-UC "UC" "RF-087: Cancelar/posponer carrera" 1100 1130

# ═══ USE CASES - COMISARIO ═══

$uc088 = Add-UC "UC" "RF-088: Ver incidentes" 1400 1200
$uc089 = Add-UC "UC" "RF-089: Ver video/evidencia" 1400 1260
$uc090 = Add-UC "UC" "RF-090: Ver info carrera" 1400 1320
$uc091 = Add-UC "UC" "RF-091: Ver pilotos involucrados" 1400 1380
$uc092 = Add-UC "UC" "RF-092: Analizar incidente" 1400 1440
$uc093 = Add-UC "UC" "RF-093: Asignar penalización" 1400 1500
$uc094 = Add-UC "UC" "RF-094: Ajustar Elo" 1400 1560
$uc095 = Add-UC "UC" "RF-095: Ajustar SR" 1400 1620
$uc096 = Add-UC "UC" "RF-096: Comentar sanción" 1400 1680
$uc097 = Add-UC "UC" "RF-097: Guardar resolución" 1400 1740
$uc098 = Add-UC "UC" "RF-098: Historial incidentes" 1400 1800
$uc099 = Add-UC "UC" "RF-099: Votación comisarios" 1400 1860
$uc100 = Add-UC "UC" "RF-100: Hist. decisiones" 1400 1920

# ═══ ASSOCIATIONS - USUARIO ═══
foreach ($uc in @($uc001,$uc002,$uc004,$uc006,$uc007,$uc008,$uc009,$uc010,$uc011,$uc018,$uc019,$uc020,$uc021,$uc022,$uc024,$uc025,$uc026,$uc027,$uc029,$uc030,$uc031,$uc033,$uc035,$uc038,$uc043,$uc046,$uc047,$uc048,$uc049,$uc052,$uc053,$uc054,$uc055,$uc056,$uc057,$uc059,$uc060,$uc061,$uc062,$uc063,$uc064,$uc065)) {
    Add-Association $aUsuario $uc
}

# Steam login incluye login normal
Add-Include $uc003 $uc002

# Asociar UC de Steam al usuario
Add-Association $aUsuario $uc003
# Vincular Steam al usuario
Add-Association $aUsuario $uc005

# ═══ ASSOCIATIONS - ADMIN ═══
foreach ($uc in @($uc066,$uc068,$uc069,$uc070,$uc072,$uc074,$uc075,$uc076,$uc077,$uc079,$uc080,$uc081,$uc082,$uc083,$uc084,$uc085,$uc086,$uc087)) {
    Add-Association $aAdmin $uc
}

# ═══ ASSOCIATIONS - COMISARIO ═══
foreach ($uc in @($uc088,$uc089,$uc090,$uc091,$uc092,$uc093,$uc094,$uc095,$uc096,$uc097,$uc098,$uc099,$uc100)) {
    Add-Association $aComisario $uc
}

# ═══ INCLUDE/EXTEND ═══
# RF-003 (login Steam) <<include>> RF-002 (login)
# Already done above

# RF-053 (evidencia) <<include>> RF-052 (reportar)
Add-Include $uc053 $uc052

# RF-091 (pilotos) <<include>> RF-090 (info carrera)
Add-Include $uc091 $uc090

# RF-094/095 (ajustar Elo/SR) <<include>> RF-093 (asignar penalización)
Add-Include $uc094 $uc093
Add-Include $uc095 $uc093

# RF-096 (comentar) <<include>> RF-093
Add-Include $uc096 $uc093

# RF-097 (guardar resolución) <<include>> RF-093
Add-Include $uc097 $uc093

# RF-057 (ver progreso) <<extend>> RF-056 (ver logros)
Add-Extend $uc057 $uc056

# RF-022/023 (categorías por Elo) <<extend>> RF-021 (ver categorías)
Add-Extend $uc022 $uc021

# ═══ FOOTER ═══
SB '      </root>'
SB '    </mxGraphModel>'
SB '  </diagram>'
SB '</mxfile>'

# ═══ WRITE ═══
$out = "C:\Facultad\ProyectoProgra\LowFuelMotorSport Local\docs\uml-casos-de-uso.drawio"
Set-Content -Path $out -Value $script:xml.ToString() -Encoding UTF8
$sz = (Get-Item $out).Length
Write-Host "Generado: $out ($([math]::Round($sz/1024,1)) KB)"
