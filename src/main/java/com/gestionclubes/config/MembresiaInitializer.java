package com.gestionclubes.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.gestionclubes.service.MembresiaService;

@Component
public class MembresiaInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(MembresiaInitializer.class);
    private final MembresiaService membresiaService;

    public MembresiaInitializer(MembresiaService membresiaService) {
        this.membresiaService = membresiaService;
    }

    @Override
    public void run(String... args) {
        int pendientes = membresiaService.sincronizarAprobadas();
        if (pendientes > 0) {
            log.info("Hay {} solicitudes aprobadas pendientes de registrar carné para crear su membresía", pendientes);
        }
    }
}
