package com.vt.createmanagesubmit.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.vt.createmanagesubmit.modelos.Alumno;

@Service
public class ServicioCola {

    private final QueueChannel queueChannel;
    private final ServicioArchivos servicioArchivo;

    @Autowired
    public ServicioCola(QueueChannel queueChannel, ServicioArchivos servicioArchivo) {
        this.queueChannel = queueChannel;
        this.servicioArchivo = servicioArchivo;
    }

    public boolean agregarATarea(Object payload, String tipoTarea) {
        return queueChannel.send(
            MessageBuilder.withPayload(payload) // Cualquier objeto
                          .setHeader("tipoTarea", tipoTarea)
                          .build(),
            5000 // Timeout de 5 segundos
        );
    }
    

    @ServiceActivator(inputChannel = "queueChannel")
    public void procesarTarea(Message<?> mensaje) {
        Object tarea = mensaje.getPayload();
        System.out.println("Procesando tarea: " + tarea);
        String tipoTarea = (String) mensaje.getHeaders().get("tipoTarea");

        if(tipoTarea.equals("generateForAlumno")){
            try {
                //servicioArchivo.generateCertificateForAlumno((Alumno) tarea);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

