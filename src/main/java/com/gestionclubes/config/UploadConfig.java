package com.gestionclubes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadConfig implements WebMvcConfigurer {
    private final String directorioFotografias;
    private final String directorioPerfiles;

    public UploadConfig(@Value("${app.upload-dir:uploads/fotografias}") String directorioFotografias,
                        @Value("${app.profile-upload-dir:uploads/perfiles}") String directorioPerfiles) {
        String ubicacion = java.nio.file.Paths.get(directorioFotografias)
                .toAbsolutePath().normalize().toUri().toString();
        this.directorioFotografias = ubicacion.endsWith("/") ? ubicacion : ubicacion + "/";
        String ubicacionPerfiles = java.nio.file.Paths.get(directorioPerfiles)
                .toAbsolutePath().normalize().toUri().toString();
        this.directorioPerfiles = ubicacionPerfiles.endsWith("/") ? ubicacionPerfiles : ubicacionPerfiles + "/";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/fotografias/**")
                .addResourceLocations(directorioFotografias);
        registry.addResourceHandler("/uploads/perfiles/**")
                .addResourceLocations(directorioPerfiles);
    }
}
