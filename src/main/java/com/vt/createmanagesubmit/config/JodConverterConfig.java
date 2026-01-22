package com.vt.createmanagesubmit.config;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JodConverterConfig {

    @Bean(destroyMethod = "stop")
    public OfficeManager officeManager() {
        LocalOfficeManager officeManager = LocalOfficeManager.builder()
                .portNumbers(2002)
                .maxTasksPerProcess(1)
                .taskExecutionTimeout(300000L)  // 5 minutos timeout
                .taskQueueTimeout(300000L)
                .install()
                .build();
        try {
            officeManager.start();  // Solo se inicia una vez al iniciar la aplicación
        } catch (OfficeException e) {
            e.printStackTrace();
        }
        return officeManager;
    }

}
