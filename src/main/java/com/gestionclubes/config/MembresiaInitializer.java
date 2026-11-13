package com.gestionclubes.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.gestionclubes.service.MembresiaService;

/**
 * Sincroniza al iniciar la aplicación las solicitudes aprobadas con sus membresías.
 */
@Component
public class MembresiaInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(MembresiaInitializer.class);
    private final MembresiaService membresiaService;

    public MembresiaInitializer(MembresiaService membresiaService) {
        this.membresiaService = membresiaService;
    }

    /** Registra las membresías posibles e informa si quedan solicitudes sin carné. */
    @Override
    public void run(String... args) {
        int pendientes = membresiaService.sincronizarAprobadas();
        if (pendientes > 0) {
            log.info("Hay {} solicitudes aprobadas pendientes de registrar carné para crear su membresía", pendientes);
        }
    }
}
