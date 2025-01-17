package com.vt.createmanagesubmit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;

@Configuration
public class IntegrationConfig {

    @Bean
    public QueueChannel queueChannel() {
        return new QueueChannel(30); // Aquí defines el tamaño de la cola (30 es solo un ejemplo)
    }

}
