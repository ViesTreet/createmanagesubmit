package com.vt.createmanagesubmit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.servicios.Servicio;

@Configuration
public class InitConfig {
    @Value("${AD_MAIL}")
    private String mail; 
    @Value("${AD_NAME}")
    private String name;
    @Value("${AD_PASSWORD}")
    private String contra;

    @Bean
    ApplicationRunner initRunner(Servicio servicio) {
        return args -> {
            System.out.println("Corre una vez");
            if(servicio.adminPorCorreo(mail)==null){
                servicio.registrarAdmin(mail, name, contra, "administrador");
            }
            if(!servicio.plantillaPorNombre("Error en encontrar plantilla").isPresent()){
                Plantilla nuevaPlantilla = new Plantilla();
                nuevaPlantilla.setNombreCertificado("Error en encontrar plantilla");
                servicio.guardarPlantilla(nuevaPlantilla);
            }
        };
    }
}
