package com.gestionclubes.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.gestionclubes.models.Actividad;

@Service
public class CalendarioService {
    private final ActividadService actividadService;

    public CalendarioService(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    public CalendarioMensual mesActual() {
        YearMonth mes = YearMonth.now();
        List<Actividad> actividades = actividadService.listarTodas();
        List<DiaCalendario> dias = new ArrayList<>();
        int primerDia = mes.atDay(1).getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        for (int i = 0; i < primerDia; i++) {
            dias.add(new DiaCalendario(null, List.of()));
        }
        for (int dia = 1; dia <= mes.lengthOfMonth(); dia++) {
            LocalDate fecha = mes.atDay(dia);
            List<Actividad> delDia = actividades.stream()
                    .filter(actividad -> fecha.equals(actividad.getFecha()))
                    .toList();
            dias.add(new DiaCalendario(fecha, delDia));
        }
        String nombreMes = mes.format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "GT")));
        return new CalendarioMensual(nombreMes, dias);
    }

    public record CalendarioMensual(String nombreMes, List<DiaCalendario> dias) {
    }

    public record DiaCalendario(LocalDate fecha, List<Actividad> actividades) {
    }
}
