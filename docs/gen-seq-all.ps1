# Generador de 5 Diagramas de Secuencia UML (v2 - strings escapados)
$script:sid = 2
$script:xml = [System.Text.StringBuilder]::new()
function SB($t) { [void]$script:xml.AppendLine($t) }
function Seq-Header($title) {
    SB '<?xml version="1.0" encoding="UTF-8"?>'
    SB '<mxfile host="app.diagrams.net" modified="2026-08-26T14:00:00.000Z" agent="5.0" version="24.0.0" type="device">'
    SB "  <diagram name=`"$title`" id=`"seq-$([guid]::NewGuid().ToString('N').Substring(0,8))`">"
    SB '    <mxGraphModel dx="1600" dy="1200" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="1600" pageHeight="1200" math="0" shadow="0">'
    SB '      <root>'
    SB '        <mxCell id="0" />'
    SB '        <mxCell id="1" parent="0" />'
}
function Seq-Footer() {
    SB '      </root>'
    SB '    </mxGraphModel>'
    SB '  </diagram>'
    SB '</mxfile>'
}
function P($name, $x) {
    $id = $script:sid++
    $style = if ($name -match '^[A-Z]+$') {
        'shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;align=center;html=1;outline=none;fillColor=#000000;strokeColor=#000000;'
    } else {
        'shape=umlLifeline;perimeter=lifelinePerimeter;whiteSpace=wrap;html=1;container=1;dropTarget=0;collapsible=0;recursiveResize=0;outlineConnect=0;size=40;fillColor=#dae8fc;strokeColor=#6c8ebf;'
    }
    SB "    <mxCell id=`"$id`" value=`"$name`" style=`"$style`" vertex=`"1`" parent=`"1`">"
    SB "      <mxGeometry x=`"$x`" y=`"40`" width=`"100`" height=`"60`" as=`"geometry`" />"
    SB "    </mxCell>"
    return $id
}
function Msg($from, $to, $label, $y) {
    $id = $script:sid++
    SB "    <mxCell id=`"$id`" value=`"$label`" style=`"html=1;verticalAlign=bottom;endArrow=block;endFill=1;fontSize=11;`" edge=`"1`" parent=`"1`" source=`"$from`" target=`"$to`">"
    SB "      <mxGeometry relative=`"1`" y=`"$y`" as=`"geometry`" />"
    SB "    </mxCell>"
}
function Reply($from, $to, $label, $y) {
    $id = $script:sid++
    SB "    <mxCell id=`"$id`" value=`"$label`" style=`"html=1;verticalAlign=bottom;endArrow=block;endFill=0;dashed=1;fontSize=11;`" edge=`"1`" parent=`"1`" source=`"$from`" target=`"$to`">"
    SB "      <mxGeometry relative=`"1`" y=`"$y`" as=`"geometry`" />"
    SB "    </mxCell>"
}
function Note($label, $x, $y) {
    $id = $script:sid++
    SB "    <mxCell id=`"$id`" value=`"$label`" style=`"shape=note;whiteSpace=wrap;html=1;backgroundOutline=1;size=14;fillColor=#fff2cc;strokeColor=#d6b656;fontSize=10;`" vertex=`"1`" parent=`"1`">"
    SB "      <mxGeometry x=`"$x`" y=`"$y`" width=`"220`" height=`"50`" as=`"geometry`" />"
    SB "    </mxCell>"
}
function Box($label, $x, $y, $w, $h) {
    $id = $script:sid++
    SB "    <mxCell id=`"$id`" value=`"$label`" style=`"shape=frame;whiteSpace=wrap;html=1;boundedLbl=1;backgroundOutline=1;size=20;fillColor=#d5e8d4;strokeColor=#82b366;fontSize=10;fontStyle=1;`" vertex=`"1`" parent=`"1`">"
    SB "      <mxGeometry x=`"$x`" y=`"$y`" width=`"$w`" height=`"$h`" as=`"geometry`" />"
    SB "    </mxCell>"
}
$base = 'C:\Facultad\ProyectoProgra\LowFuelMotorSport Local\docs'

# ═══════════════════════════════════════════════════════════════════════
# 1. INSCRIPCIÓN A CARRERA
# ═══════════════════════════════════════════════════════════════════════
Seq-Header 'Secuencia: Inscripcion a Carrera'
$u = P 'USUARIO' 50; $s = P ':Sistema' 250; $c = P ':Carrera' 450; $i = P ':Inscripcion' 650; $n = P ':Notificacion' 850
$y=140
Msg $u $u $u 'inscribirse(carreraId)' $y; $y+=40  # placeholder, fix below

$script:sid = 2; $script:xml = [System.Text.StringBuilder]::new()
Seq-Header 'Secuencia: Inscripcion a Carrera'
$u = P 'USUARIO' 50
$s = P ':Sistema' 250
$c = P ':Carrera' 450
$i = P ':Inscripcion' 650
$n = P ':Notificacion' 850

$y=140
Msg $u $s 'inscribirse(carreraId)' $y; $y+=40
Msg $s $c 'buscarPorId(carreraId)' $y; $y+=40
Reply $c $s 'carrera' $y; $y+=40
Note 'RF-073: validar fecha < inicio-5min' 250 $y; $y+=10
Msg $s $s 'validarFecha(carrera.fecha)' $y; $y+=40
Box '[fecha valida] -> validar cupo' 200 $y 600 140; $y+=30
Msg $s $c 'getCupoMaximo()' $y; $y+=40
Reply $c $s 'cupoMaximo' $y; $y+=40
Box '[cupo > 0] -> INSCRIPTO' 220 $y 560 60; $y+=25
Msg $s $i 'crear(usuario, carrera, INSCRIPTO)' $y; $y+=30
Box '[cupo == 0] -> LISTA_ESPERA (RF-033)' 220 $y 560 60; $y+=25
Msg $s $i 'crear(usuario, carrera, LISTA_ESPERA)' $y; $y+=30
Msg $s $n 'notificar(usuario, INSCRIPCION)' $y; $y+=40
Reply $n $s 'ok' $y; $y+=40
Reply $s $u 'inscripcion creada' $y; $y+=40
Note 'RF-030 Inscribirse | RF-031 Bajarse | RF-033 Lista espera' 50 $y
Set-Content "$base\uml-seq-inscripcion.drawio" $script:xml.ToString() -Encoding UTF8
Write-Host '1. Inscripcion: OK'

# ═══════════════════════════════════════════════════════════════════════
# 2. RESULTADOS -> RATING -> CAMPEONATO
# ═══════════════════════════════════════════════════════════════════════
$script:sid = 2; $script:xml = [System.Text.StringBuilder]::new()
Seq-Header 'Secuencia: Resultados, Rating, Campeonato'
$adm = P 'ADMIN' 50
$s = P ':Sistema' 230
$rc = P ':ResultadoCarrera' 430
$ec = P 'EloCalculator' 630
$sr = P 'SrCalculator' 830
$es = P ':EloSancion' 1030
$cp = P ':CampeonatoPos' 1230
$y=140
Msg $adm $s 'cargarResultados(carreraId)' $y; $y+=40
Note 'RF-074: carga manual o desde JSON AC' 50 $y; $y+=10
Msg $s $s 'verificarIdempotencia(carreraId)' $y; $y+=40
Note 'Rechaza si ya tiene resultados' 250 $y; $y+=10
Box 'Para cada resultado:' 200 $y 1100 400; $y+=30
Msg $s $rc 'crear(carrera, usuario, pos, tiempo)' $y; $y+=40
Reply $rc $s 'resultadoCarrera' $y; $y+=40
Msg $s $ec 'calcularElo(usuario, pos, prom)' $y; $y+=40
Note 'RF-040: K=32, dispersión=400' 630 $y; $y+=10
Reply $ec $s 'eloGanado' $y; $y+=40
Msg $s $sr 'calcularSR(usuario, finalizo, pos)' $y; $y+=40
Note 'RF-041: +5 finalizo, -5 DNF' 830 $y; $y+=10
Reply $sr $s 'srGanado' $y; $y+=40
Msg $s $es 'registrar(usuario, eloGanado, carrera)' $y; $y+=40
Note 'RF-018: historial de cambios' 1030 $y; $y+=10
Reply $es $s 'ok' $y; $y+=40
Msg $s $cp 'upsert(campeonato, usuario, pts)' $y; $y+=40
Note 'RF-035: tabla F1 (25-18-15...)' 1230 $y; $y+=10
Reply $cp $s 'posicion' $y; $y+=40
Reply $s $adm 'resultados cargados + rating' $y; $y+=30
Note 'RF-035 campeonato | RF-040 Elo | RF-041 SR' 50 $y
Set-Content "$base\uml-seq-resultados-rating.drawio" $script:xml.ToString() -Encoding UTF8
Write-Host '2. Resultados-Rating: OK'

# ═══════════════════════════════════════════════════════════════════════
# 3. INCIDENTES -> RESOLUCION
# ═══════════════════════════════════════════════════════════════════════
$script:sid = 2; $script:xml = [System.Text.StringBuilder]::new()
Seq-Header 'Secuencia: Incidentes y Resolucion'
$u = P 'USUARIO' 50
$s = P ':Sistema' 230
$inc = P ':Incidente' 430
$ip = P ':IncidentePiloto' 630
$vc = P ':VotoComisario' 830
$res = P ':ResolucionIncidente' 1030
$san = P ':Sancion' 1230
$y=140
Note 'PASO 1: REPORTE' 50 $y; $y+=10
Msg $u $s 'reportarIncidente(carreraId, desc, video)' $y; $y+=40
Msg $s $inc 'crear(carrera, reportante, PENDIENTE)' $y; $y+=40
Reply $inc $s 'incidente' $y; $y+=40
Msg $s $ip 'agregar(incidente, causante, CAUSANTE)' $y; $y+=40
Msg $s $ip 'agregar(incidente, afectado, AFECTADO)' $y; $y+=40
Note 'RF-052 reportar | RF-053 evidencia' 50 $y; $y+=10
$y+=20
Note 'PASO 2: VOTACION (RF-099)' 50 $y; $y+=10
Msg $s $vc 'votar(incidente, comisario1, A_FAVOR)' $y; $y+=40
Msg $s $vc 'votar(incidente, comisario2, A_FAVOR)' $y; $y+=40
Note 'TBD-6: quorum minimo 2 votos' 250 $y; $y+=10
$y+=20
Note 'PASO 3: RESOLUCION' 50 $y; $y+=10
Box '[quorum alcanzado] -> RESUELTO' 200 $y 900 120; $y+=30
Msg $s $res 'crear(incidente, comisario, explicacion)' $y; $y+=40
Msg $s $inc 'setEstado(RESUELTO)' $y; $y+=40
$y+=20
Note 'PASO 4: SANCION (RF-093)' 50 $y; $y+=10
Msg $s $san 'crear(usuario, carrera, resolucion, tipo, valor)' $y; $y+=40
Note 'RF-094/095: ajustar Elo y SR' 250 $y; $y+=10
Msg $s $s 'notificar(usuario, PENALIZACION)' $y; $y+=40
Note 'RF-088-100: flujo completo comisarios' 50 $y
Set-Content "$base\uml-seq-incidentes.drawio" $script:xml.ToString() -Encoding UTF8
Write-Host '3. Incidentes: OK'

# ═══════════════════════════════════════════════════════════════════════
# 4. APELACION DE SANCION
# ═══════════════════════════════════════════════════════════════════════
$script:sid = 2; $script:xml = [System.Text.StringBuilder]::new()
Seq-Header 'Secuencia: Apelacion de Sancion'
$u = P 'USUARIO' 50
$s = P ':Sistema' 230
$ap = P ':Apelacion' 430
$san = P ':Sancion' 630
$adm = P 'ADMIN' 830
$y=140
Note 'PASO 1: PILOTO APELA' 50 $y; $y+=10
Msg $u $s 'apelar(sancionId, motivo)' $y; $y+=40
Msg $s $san 'buscarPorId(sancionId)' $y; $y+=40
Reply $san $s 'sancion' $y; $y+=40
Msg $s $ap 'crear(sancion, usuario, motivo, PENDIENTE)' $y; $y+=40
Reply $ap $s 'apelacion' $y; $y+=40
Note 'RF-065: apelar sancion' 50 $y; $y+=10
$y+=20
Note 'PASO 2: ADMIN RESUELVE (RF-080)' 50 $y; $y+=10
Msg $adm $s 'resolverApelacion(apelacionId, decision)' $y; $y+=40
Msg $s $ap 'buscarPorId(apelacionId)' $y; $y+=40
Reply $ap $s 'apelacion' $y; $y+=40
Box '[APROBADA] -> revertir sancion' 200 $y 700 100; $y+=30
Msg $s $san 'setEstado(REVERTIDA)' $y; $y+=40
Msg $s $s 'revertirEloSR(sancion)' $y; $y+=40
Note 'Compensar Elo/SR afectados' 250 $y; $y+=10
$y+=20
Box '[RECHAZADA] -> sancion firme' 200 $y 700 60; $y+=25
Msg $s $san 'setEfectosAplicados(true)' $y; $y+=30
Msg $s $ap 'setEstado(decision)' $y; $y+=40
Msg $s $u 'notificar(RESOLUCION_APELACION)' $y; $y+=40
Note 'RF-065 apelar | RF-080 resolver' 50 $y
Set-Content "$base\uml-seq-apelacion.drawio" $script:xml.ToString() -Encoding UTF8
Write-Host '4. Apelacion: OK'

# ═══════════════════════════════════════════════════════════════════════
# 5. INGESTION SESIONES ASSETTO CORSA
# ═══════════════════════════════════════════════════════════════════════
$script:sid = 2; $script:xml = [System.Text.StringBuilder]::new()
Seq-Header 'Secuencia: Ingestion Sesiones Assetto Corsa'
$w = P 'Watcher' 50
$s = P ':SesionServidorService' 230
$sc = P ':SesionClasificacion' 430
$rc = P ':ResultadoCarrera' 630
$ec = P 'EloCalculator' 830
$sr = P 'SrCalculator' 1030
$inc = P ':Incidente' 1230
$sp = P ':SesionProcesada' 1400
$y=140
Note 'PASO 1: WATCHER DETECTA ARCHIVO (cada 10s)' 50 $y; $y+=10
Msg $w $s 'detectar(archivo.json)' $y; $y+=40
Msg $s $sp 'verificarSiProcesado(nombreArchivo)' $y; $y+=40
Reply $sp $s 'noExiste' $y; $y+=40
Note 'RF: idempotencia via sesion_procesada' 250 $y; $y+=10
$y+=20
Note 'PASO 2: PARSEAR JSON' 50 $y; $y+=10
Msg $s $s 'parsearJSON(archivo)' $y; $y+=40
Msg $s $s 'correlacionar(driverGUID -> guidSteam)' $y; $y+=40
$y+=20
Box '[Type == QUALIFY]' 200 $y 1200 100; $y+=30
Msg $s $sc 'crear(carrera, usuario, tiempo, difPole)' $y; $y+=40
Note 'RF-015: detectar pole position' 430 $y; $y+=10
$y+=20
Box '[Type == RACE]' 200 $y 1200 200; $y+=30
Msg $s $rc 'crear(carrera, usuario, pos, tiempo, bestLap)' $y; $y+=40
Msg $s $ec 'calcularElo(...)' $y; $y+=40
Msg $s $sr 'calcularSR(...)' $y; $y+=40
Note '-> Flujo B: Rating + Campeonato' 630 $y; $y+=10
$y+=20
Box '[Events -> colisiones]' 200 $y 1200 80; $y+=25
Msg $s $inc 'autoGenerar(carrera, pilotos, colision)' $y; $y+=40
Note 'Autogenera incidentes por colision' 250 $y; $y+=10
$y+=20
Note 'PASO 3: REGISTRAR PROCESAMIENTO' 50 $y; $y+=10
Msg $s $sp 'crear(carrera, nombreArchivo, tipo)' $y; $y+=40
Msg $s $s 'moverArchivo(procesadas/)' $y; $y+=40
Note 'Flujo G: watcher -> AC JSON -> resultados + incidentes' 50 $y
Set-Content "$base\uml-seq-ingestion-ac.drawio" $script:xml.ToString() -Encoding UTF8
Write-Host '5. Ingestion AC: OK'
