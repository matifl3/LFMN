# Helpers simplificados para diagramas de secuencia UML
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
    $style = if ($name -match "^[A-Z]+$") {
        "shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;align=center;html=1;outline=none;fillColor=#000000;strokeColor=#000000;"
    } else {
        "shape=umlLifeline;perimeter=lifelinePerimeter;whiteSpace=wrap;html=1;container=1;dropTarget=0;collapsible=0;recursiveResize=0;outlineConnect=0;size=40;fillColor=#dae8fc;strokeColor=#6c8ebf;"
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
    SB "      <mxGeometry x=`"$x`" y=`"$y`" width=`"200`" height=`"50`" as=`"geometry`" />"
    SB "    </mxCell>"
}

function Box($label, $x, $y, $w, $h) {
    $id = $script:sid++
    SB "    <mxCell id=`"$id`" value=`"$label`" style=`"shape=frame;whiteSpace=wrap;html=1;boundedLbl=1;backgroundOutline=1;size=20;fillColor=#d5e8d4;strokeColor=#82b366;fontSize=10;fontStyle=1;`" vertex=`"1`" parent=`"1`">"
    SB "      <mxGeometry x=`"$x`" y=`"$y`" width=`"$w`" height=`"$h`" as=`"geometry`" />"
    SB "    </mxCell>"
}
