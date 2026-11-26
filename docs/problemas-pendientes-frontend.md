# Problemas pendientes del frontend — revisión 24/11/2026

Une los resultados de dos revisiones del frontend (`frontend/src/`):

- **`/impeccable audit`**: auditoría técnica de todo el frontend (accesibilidad, rendimiento, theming,
  responsive, integridad del sistema de diseño). Puntaje **12/20, aceptable**.
- **`/improve-ui`**: revisión visual de la pantalla de detalle de carrera (`/carreras/:id`) contra el
  sistema de diseño que la gobierna.

**Nada está corregido todavía.** Las dos revisiones son de lectura de código: la app no se levantó
en el navegador (necesita backend y MySQL), así que los problemas de responsive no están
verificados en pantalla.

Los problemas del backend están en `docs/problemas-pendientes.md`. Su punto 8 (errores tragados
por `catchError(() => of([]))` en 10 componentes) también es del frontend y conviene resolverlo
junto con la sección A de este documento.

Rutas relativas a `frontend/src/` salvo que se indique.

---

## Resumen

| # | Problema | Prioridad | Origen | Comando sugerido |
|---|---|---|---|---|
| A1 | 68 de 82 `<label>` sin asociar a su campo | **P1** | impeccable | `/impeccable harden` |
| A2 | El contorno de foco rojo no llega a 3:1 sobre los paneles | **P1** | impeccable | `/impeccable harden` |
| A3 | Tarjeta de setup y zona de archivos no se usan con teclado | **P1** | impeccable | `/impeccable harden` |
| B1 | 3 tablas de Mis campeonatos sin scroll horizontal | **P1** | impeccable | `/impeccable adapt` |
| C1 | "Dif. líder" con formato distinto al resto de las diferencias | **P1** | improve-ui | edición directa |
| C2 | Elo vacío ("—") pintado de verde en Resultados | P2 | improve-ui | edición directa |
| C3 | Encabezado de carrera dice "· Ronda" sin número | P2 | improve-ui | edición directa |
| A4 | Jerarquía de títulos y `<main>` anidado | P2 | impeccable | `/impeccable layout` |
| A5 | Pestañas de admin sin estado accesible | P2 | impeccable | `/impeccable harden` |
| A6 | Placeholder casi invisible | P2 | impeccable | `/impeccable harden` |
| D1 | 287 estilos inline en las plantillas | P2 | impeccable | `/impeccable layout` |
| E1 | Fuente de íconos Material Symbols completa | P2 | impeccable | `/impeccable optimize` |
| E2 | Fondos animados infinitos en la home | P2 | impeccable | `/impeccable optimize` |
| F1 | `prefers-reduced-motion` apaga todo el feedback | P3 | impeccable | `/impeccable animate` |
| F2 | Nombre del producto inconsistente | P3 | impeccable | decisión |
| F3 | Grilla decorativa en el hero (advertencia del detector) | P3 | impeccable | decisión |

**Orden sugerido:**
1. Los tres de la sección C: son de una línea cada uno.
2. A1–A3 y A5–A6 en una sola pasada de `harden`, sumando el punto 8 del documento del backend.
3. B1.
4. D1 junto con A4: `layout`.
5. E1–E2: `optimize`.
6. `/impeccable polish` como cierre, y volver a correr `/impeccable audit` para medir.

Prioridades: **P1** = problema serio o incumplimiento de WCAG AA, arreglar antes de publicar;
**P2** = molesto pero hay forma de seguir; **P3** = pulido.

---

## Puntajes de la auditoría (`/impeccable audit`)

| Dimensión | Puntaje | Hallazgo clave |
|---|---|---|
| Accesibilidad | 2/4 | Labels sin asociar; foco sin contraste sobre paneles |
| Rendimiento | 3/4 | Fuente de íconos pesada; animaciones de fondo continuas |
| Theming | 3/4 | Buen sistema de tokens; solo 16 colores sueltos |
| Responsive | 2/4 | 3 tablas sin scroll; anchos fijos inline |
| Integridad | 2/4 | 287 estilos inline |
| **Total** | **12/20** | **Aceptable** |

---

## A. Accesibilidad

### A1. Labels sin asociar a su campo — P1

**Dónde:** se repite en `incidents.component.html:131-139`, `admin.component.html`,
`my-championships.component.html` y `setups.html`. Solo 14 de los 82 `<label>` tienen `for`.

```html
<div class="field"><label>Carrera *</label>
  <select class="select" ...>
```

**Impacto:** un lector de pantalla anuncia "combo box" sin decir de qué campo se trata. Tocar la
etiqueta no lleva el foco al campo. *WCAG 1.3.1 y 4.1.2.*

**Solución:** agregar `id` al control y `for` al label, o meter el control dentro del `<label>`.
Conviene definir un único patrón de campo (label + control + error) y usarlo en todos los
formularios: es la causa de que falte en casi todos.

### A2. Contorno de foco sin contraste — P1

**Dónde:** `styles/_base.scss:106` (`:focus-visible` con `outline: 2px solid var(--lbm-rojo)`) y
`styles/_components.scss:550-556` (los inputs usan `outline: none` y borde rojo).

**Medición:** `#c61a1a` da 3.18:1 sobre `--bg-principal`, **2.83:1** sobre `--bg-paneles` y
**2.44:1** sobre `--bg-superficie`. El mínimo es 3:1. *WCAG 1.4.11.*

**Impacto:** casi todos los controles están sobre paneles; quien navega con teclado pierde dónde está.

**Solución:** usar `--lbm-rojo-hi` (`#e63946`) para el contorno, o un anillo doble (uno claro y uno
rojo). Verificar el contraste sobre los tres fondos.

### A3. Elementos clickeables que no se usan con teclado — P1

**Dónde:**
- `setups.html:100` — la tarjeta de setup es un `<div>` con `(click)`, sin `tabindex` ni teclas.
- `setups.html:35` — la zona para soltar archivos tiene `role="button"` pero no se puede enfocar.

**Impacto:** sin mouse no se puede elegir un setup. *WCAG 2.1.1.*

**Solución:** usar `<button>`, o copiar el patrón que ya está bien en
`incidents.component.html:36` (`role="button"`, `tabindex="0"`, `aria-label`, `keydown.enter` y
`keydown.space`).

### A4. Jerarquía de títulos y `<main>` anidado — P2

- Solo 3 pantallas tienen `<h1>` (home, driver-profile y race-detail); las demás arrancan en `<h2>`.
- `features/races/races-list/races-list.html:1` usa `<main class="container-wide section">`
  dentro del `<main>` de `app/app.html:4`. Debería ser un `<section>`.

### A5. Pestañas de admin sin estado accesible — P2

**Dónde:** `admin.component.html:21-32`. Las pestañas marcan la activa solo con la clase
`.active`. En `race-detail.html:66-118` ya se hace bien con `[attr.aria-pressed]`: copiar ese
patrón.

### A6. Placeholder casi invisible — P2

**Dónde:** `styles/_components.scss:548` usa `--texto-disabled` (`#474d54`) para el placeholder.
Da 1.95:1 sobre los paneles. Usar `--texto-terciario` o un gris intermedio que supere 4.5:1.

---

## B. Responsive

### B1. Tablas sin scroll horizontal — P1

**Dónde:** `my-championships.component.html:158` (miembros), `:311` (carreras) y `:380` (tabla
de puntos). Son `<table class="table">` sin el contenedor `.table-wrap`.

**Impacto:** en el celular rompen el ancho y la página entera se desplaza hacia los costados. Las
otras 13 tablas del frontend sí lo tienen.

**Solución:** envolver cada una en `<div class="table-wrap">` (`styles/_components.scss:618`).

> Hay además anchos fijos en estilos inline (`width: 220px`, `180px`, `min-width: 220px`…) que
> conviene revisar en la pasada de D1.

---

## C. Pantalla de detalle de carrera (`/improve-ui`)

Pantalla auditada: `features/races/race-detail/`. Las tres correcciones reutilizan algo que el
componente ya tiene; no hace falta crear tokens ni componentes nuevos.

### C1. "Dif. líder" con formato distinto — P1

**Dónde:** `race-detail.html:568`

```html
<td class="num mono">+{{ (a.deltaLiderMs ?? 0) / 1000 }} s</td>
```

**Problema:** muestra una cantidad variable de decimales (`+1.2 s`, `+12.345 s`). Las demás
diferencias de tiempo de la misma pestaña (sectores, l.448-460) y de Clasificación (l.364) usan
la pipe `fmtDif`, que da `+01.234`. Cuando el dato falta muestra `+0 s`, como si el piloto fuera
el líder, en vez de `—`.

**Corrección:** `<td class="num mono">{{ a.deltaLiderMs | fmtDif }}</td>`. La pipe ya está
importada en el componente.

### C2. Elo vacío pintado de verde — P2

**Dónde:** `race-detail.html:338-342`

```html
[style.color]="(r.eloGanado ?? 0) >= 0 ? 'var(--status-positivo)' : 'var(--lbm-rojo-hi)'"
```

**Problema:** con `eloGanado = null` la celda muestra `—` en verde, como si fuera ganancia. El
helper del mismo componente, `eloColor()` (`race-detail.component.ts:247-250`), devuelve sin color
para `null`, y ya se usa en Información (l.216) y en Análisis (l.528).

**Corrección:** `[style.color]="eloColor(r.eloGanado)"`.

### C3. "· Ronda" sin número — P2

**Dónde:** `race-detail.html:44`

```html
<span class="eyebrow alerta">{{ carrera()?.categoriaNombre || 'CARRERA' }} · Ronda</span>
```

**Problema:** la interfaz `Carrera` (`core/models/models.ts:63-81`) no tiene campo de ronda, así que
el texto siempre queda incompleto. En la home la misma etiqueta va con número
(`home.html:8`, "Ronda 8 de 16").

**Corrección:** quitar el sufijo ` · Ronda`. Si más adelante se quiere mostrar el número de ronda,
hay que agregarlo primero al DTO del backend (`CarreraResponse`) y a `models.ts`.

---

## D. Integridad del sistema de diseño

### D1. 287 estilos inline — P2

Los patrones más repetidos:

| Veces | Estilo |
|---|---|
| 26 | `width: Npx` |
| 12 | `display: flex; justify-content: space-between` |
| 30 | `font-size: var(--fs-sm)` / `var(--fs-2xs)` |
| ~60 | márgenes con tokens (`margin-bottom: var(--sp-4)`, etc.) |

**Impacto:** el layout de cada pantalla vive en el HTML y no en clases. Cuesta mantenerlo
consistente, y los anchos fijos complican el responsive. La mayoría ya usa tokens, así que el
problema es de estructura, no de valores.

**Solución:** crear clases utilitarias o por feature para los patrones repetidos y migrar pantalla
por pantalla. Empezar por `race-detail.html` (bloques de Análisis) y `admin.component.html`, que son
los más grandes.

---

## E. Rendimiento

### E1. Fuente de íconos completa — P2

**Dónde:** `index.html`. Se pide Material Symbols Rounded con todos sus ejes variables
(`opsz,wght,FILL,GRAD@20..48,300..700,0..1,-50..200`) en una hoja de Google Fonts que bloquea el
render. Se usa para unos pocos íconos (`.ms-icon` en logros y en los botones de admin).

**Solución:** fijar los ejes a un solo valor y, si es posible, pedir solo los íconos usados
(`&icon_names=`), o reemplazarlos por SVG inline como ya se hace en `race-detail.html`.

### E2. Fondos animados infinitos en la home — P2

**Dónde:** `styles/_base.scss:342-348`. `.pattern-checker` y `.pattern-trackgrid` animan
`background-position` en bucle infinito (40s y 60s); los usa `home.html:2-3`. Además, el header
sticky tiene `backdrop-filter: blur(14px)` (`styles/_components.scss:132`).

**Impacto:** animar `background-position` repinta en cada frame; en equipos modestos gasta GPU
todo el tiempo que la home está abierta.

**Solución:** animar con `transform` sobre un pseudo-elemento (corre en el compositor), o pausar la
animación cuando el hero sale de pantalla.

---

## F. Pulido y decisiones

- **F1 — `prefers-reduced-motion`** (`styles/_base.scss:112`): lleva todas las animaciones y
  transiciones a 0.01ms, y eso también apaga el feedback útil (hover, cambio de estado). Conviene
  reemplazar el movimiento por fundidos cortos en vez de anularlo. `MotionService` ya respeta la
  preferencia correctamente.
- **F2 — Nombre del producto:** "Late Brake Motorsport" en `index.html` y en los tokens, "Low Fuel
  Motorsport" / "LFM Nacional" en la documentación y el repo. Hay que decidir cuál es el oficial.
- **F3 — Grilla decorativa** (`styles/_base.scss:334`, `.pattern-trackgrid`): el detector de
  impeccable la marca como un patrón típico de UI generada. Como el producto gira alrededor de la
  telemetría se puede justificar; queda a criterio.

---

## Lo que ya está bien (no tocar)

- **Modal** (`shared/components/modal/modal.ts`): `role="dialog"`, `aria-modal`, título asociado,
  foco atrapado y vuelta del foco al cerrar.
- **`MotionService`**: respeta `prefers-reduced-motion` y no oculta contenido si no corre.
- **Botones de solo ícono**: tienen `aria-label`, y los íconos, `aria-hidden`.
- **Tokens** (`styles/_tokens.scss`): escala tipográfica, espaciado de base 8 y paleta semántica.
  El texto secundario y el terciario superan 4.5:1 en todos los fondos.
- **Pestañas de race-detail**: exponen el estado con `aria-pressed` (modelo para A5).
- **`index.html`**: `lang="es"` y estructura limpia.

---

## Referencias

- Informe original de impeccable: `.impeccable/audit/2026-11-24T21-29-31Z__frontend-src-app.md`
- Problemas del backend: `docs/problemas-pendientes.md`
- Reglas del repo: `docs/arquitectura-y-patrones.md` (§4.4 sobre errores tragados)
