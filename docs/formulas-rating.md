# Fórmulas de rating — Elo, Safety Rating, puntos y quórum

> Documento técnico del backend (Spring Boot) del proyecto **Low Fuel Motorsport**.
> Documenta las fórmulas y valores por defecto aplicados en el código mientras
> los TBD del modelo de requisitos no sean definidos oficialmente.

---

## 1. Introducción

Estas fórmulas son la **interpretación por defecto** de las reglas pendientes
de validación:

| TBD | Tema | Valor por defecto usado |
|---|---|---|
| TBD-7 | Fórmula de Elo y Safety Rating | Elo K=32/dispersión=400, SR con bonificaciones por resultado |
| TBD-1 | Sistema de puntos de campeonato | Escala estilo F1 (25-18-15-12-10-8-6-4-2-1) |
| TBD-6 | Quórum de comisarios y penalidad RP | 2 votos A_FAVOR; RP −10 SR (−20 si DSQ) |

Todas las constantes están centralizadas como `private static final` en las
clases indicadas, para ajustarlas en un solo lugar cuando se confirmen las
reglas oficiales.

---

## 2. Elo

Implementación: `service/rating/EloCalculator.java`

Constantes:
- `K = 32`
- `DISPERSION = 400`

Cálculo por carrera, por cada participante:

```
esperado  = 1 / (1 + 10 ^ ((promedioRivales − eloPropio) / 400))
resultado = (participantes − posicion) / (participantes − 1)
cambio    = round(K × (resultado − esperado))

usuario.elo += cambio      (se guarda en resultado_carrera.elo_ganado)
```

Casos especiales:
- **Sin rivales** (participantes ≤ 1): `resultado = 0.5` (cambio nulo contra un
  promedio igual al propio, porque `esperado = 0.5`).
- **Sin datos previos** (usuario nuevo): `elo = 1500` (default en `Usuario`).
- El promedio de rivales se calcula sobre los `elosRivales` recibidos; si la
  lista está vacía se usa el Elo propio (`esperado = 0.5`).

### Ejemplo numérico

Participante con `elo = 1500`, termina 2º de 20, rivales con promedio `1600`:

```
esperado  = 1 / (1 + 10 ^ ((1600 − 1500)/400)) = 1 / (1 + 10^0.25)
          = 1 / (1 + 1.7783) = 0.3599
resultado = (20 − 2) / (20 − 1) = 18/19 = 0.9474
cambio    = round(32 × (0.9474 − 0.3599)) = round(18.8) = +19
```

Ganar contra rivales más fuertes da +Elo; perder (o terminar atrás del
promedio) lo resta.

---

## 3. Safety Rating (SR)

Implementación: `service/rating/SrCalculator.java`

Constantes:

| Concepto | Constante | Valor |
|---|---|---|
| Bonus por finalizar | `BONUS_FINALIZO` | +5 |
| Penalidad DNF | `PENALIDAD_DNF` | −5 |
| Bonus victoria | `BONUS_VICTORIA` | +10 |
| Bonus 2º lugar | `BONUS_PODIO` | +8 |
| Bonus 3º lugar | `BONUS_TERCERO` | +6 |

Regla:

```
cambio = finalizo ? +5 : −5
        + (finalizo && pos == 1) ? +10
        + (finalizo && pos == 2) ? +8
        + (finalizo && pos == 3) ? +6

usuario.safety_rating += cambio   (se guarda en resultado_carrera.sr_ganado)
```

Valores resultantes por posición (si finalizó):

| Posición | Cambio SR |
|---|---|
| 1º | +15 |
| 2º | +13 |
| 3º | +11 |
| 4º en adelante | +5 |
| DNF | −5 |

Default de usuario nuevo: `safety_rating = 100` (en `Usuario`).

---

## 4. Puntos de campeonato

Implementación: `service/CampeonatoService.java`

Constante:
```
PUNTOS_POR_POSICION = [25, 18, 15, 12, 10, 8, 6, 4, 2, 1]
```

| Posición | Puntos |
|---|---|
| 1º | 25 |
| 2º | 18 |
| 3º | 15 |
| 4º | 12 |
| 5º | 10 |
| 6º | 8 |
| 7º | 6 |
| 8º | 4 |
| 9º | 2 |
| 10º | 1 |
| 11º+ | 0 |

Reglas:
- Solo puntúa el **top 10**; posiciones fuera del rango o `null` → `0` puntos.
- Se acumula vía **upsert** en `campeonato_posicion`
  (UNIQUE(campeonato_id, usuario_id)): `posicion.puntos += puntos` por cada
  resultado cargado.
- Al recalcular, las filas se ordenan por puntos y se reasigna `posicion`
  (RF-035, tabla de campeonato).

---

## 5. Quórum de comisarios

Implementación: `service/IncidenteService.java`

Constante: `QUORUM_A_FAVOR = 2`

```
¿votos con decision == A_FAVOR >= 2?
  NO  → incidente sigue PENDIENTE
  SI  → se habilita cerrar con resolucion_incidente
         (estado → RESUELTO)
```

Reglas asociadas:
- **Un voto por comisario**: UNIQUE(incidente_id, comisario_id) en
  `voto_comisario`.
- La decisión que cuenta para el quórum es `DecisionComisario.A_FAVOR`
  (penalizar al perpetrador).
- La resolución la cierra un comisario y dispara la `sancion` (Flujo C de
  `docs/flujos.md`).

---

## 6. Penalidad Real Penalty → Safety Rating

Implementación: `service/SancionService.java`

Constante: `SR_PENALIDAD_RP = −10` (se duplica si es descalificación)

| Evento RP (`tipo`) | `TipoSancion` | Cambio SR |
|---|---|---|
| `dt` | DRIVE_THROUGH | −10 |
| `sg` | STOP_AND_GO | −10 |
| `dsq` | DESCALIFICACION | **−20** (`−10 × 2`) |
| otro / null | SEGUNDOS | −10 |

Reglas:
- Correlación del piloto por `guid_steam` → `usuario.guid_steam`.
- **Idempotencia**: si ya existe una `sancion` con `origen = REAL_PENALTY` y el
  mismo `id_externo` (eventoId), no se duplica ni se vuelve a aplicar el SR.
- La sanción guarda `valor` = segundos del evento y `motivo` de RP.
- Al aplicar se registra `safety_rating_sancion` (historial) y se notifica al
  piloto (TipoNotificacion.PENALIZACION).

---

## 7. Dónde ajustar cada constante

| Fórmula | Clase | Línea(s) clave |
|---|---|---|
| Elo | `service/rating/EloCalculator.java` | `K`, `DISPERSION` |
| SR de carrera | `service/rating/SrCalculator.java` | `BONUS_*`, `PENALIDAD_DNF` |
| Puntos campeonato | `service/CampeonatoService.java` | `PUNTOS_POR_POSICION`, `puntosPorPosicion()` |
| Quórum | `service/IncidenteService.java` | `QUORUM_A_FAVOR` |
| Penalidad RP | `service/SancionService.java` | `SR_PENALIDAD_RP`, `recibirEventoRealPenalty()` |
