package org.example.lfmnacional.service.sancion;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.entity.ResultadoCarrera;
import org.example.lfmnacional.entity.Sancion;
import org.example.lfmnacional.enums.TipoSancion;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.springframework.stereotype.Component;

/** Penalizacion de tiempo: suma (o resta al revertir) segundos y reclasifica. */
@Component
@RequiredArgsConstructor
public class EfectoSegundos implements EfectoSancion {

    private final ReclasificacionCarrera reclasificacion;
    private final ResultadoCarreraRepository resultadoCarreraRepository;

    @Override
    public TipoSancion tipo() {
        return TipoSancion.SEGUNDOS;
    }

    @Override
    public void aplicar(Sancion sancion) {
        ajustarTiempo(sancion, true);
    }

    @Override
    public void revertir(Sancion sancion) {
        ajustarTiempo(sancion, false);
    }

    @Override
    public boolean reclasificaCarrera() {
        return true;
    }

    /**
     * Una penalizacion de tiempo tiene que <em>reclasificar</em>: el piloto cae
     * en la clasificacion segun su tiempo total nuevo, no se queda en el puesto
     * que tenia con un tiempo peor.
     *
     * <p>Sin tiempo total no hay contra que comparar, asi que en vez de un NPE
     * (sumarle a null) se corta con un mensaje claro.
     */
    private void ajustarTiempo(Sancion sancion, boolean sumar) {
        if (sancion.getCarrera() == null || sancion.getValor() == null) {
            return;
        }
        ResultadoCarrera resultado = reclasificacion.resultadoDe(sancion);
        if (resultado.getTiempoTotal() == null) {
            throw new BusinessException("El usuario no tiene tiempo total en la carrera indicada");
        }
        long delta = Math.abs((long) sancion.getValor()) * 1000L;
        resultado.setTiempoTotal(sumar ? resultado.getTiempoTotal() + delta : resultado.getTiempoTotal() - delta);
        resultadoCarreraRepository.save(resultado);
        if (!sumar) {
            // reclasificar() aplica las sanciones activas: la que se revierte ya no cuenta.
            sancion.setEfectosAplicados(false);
        }
        reclasificacion.reclasificar(sancion.getCarrera());
    }
}
