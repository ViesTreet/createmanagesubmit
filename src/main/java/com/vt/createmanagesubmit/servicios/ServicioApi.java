package com.vt.createmanagesubmit.servicios;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ServicioApi {

    public String obtenerNombrePorRut(String rut) {
        // Construye la URL con el rut
        String url = UriComponentsBuilder.fromUriString("https://api.boostr.cl/rut/name/{rut}.json")
                .buildAndExpand(rut)
                .toUriString();

        // Crea una instancia de RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Realiza la petición GET y obtiene la respuesta como String
            ApiResponse response = restTemplate.getForObject(url, ApiResponse.class);

            if (response != null && response.getData() != null) {
                String name = response.getData().getName();
                if ("**".equals(name)) {
                    return "nombreNoEncontrado";
                } else {
                    // Formatea el nombre
                    return formatearNombre(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Manejar excepciones según sea necesario
        }

        return "nombreNoEncontrado";
    }

    public String formatearNombre(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return nombre;
        }
        // Divide el nombre en palabras y capitaliza cada una
        String[] palabras = nombre.toLowerCase().split(" ");
        StringBuilder nombreFormateado = new StringBuilder();

        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                nombreFormateado.append(Character.toUpperCase(palabra.charAt(0)))
                                .append(palabra.substring(1))
                                .append(" ");
            }
        }

        return nombreFormateado.toString().trim();
    }

    // Clase interna para mapear la respuesta de la API
    public static class ApiResponse {
        private Data data;

        // Getters y setters
        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }
    }

    public static class Data {
        private String document;
        private String dv;
        private String name;
        private Object[] activities;

        // Getters y setters
        public String getDocument() {
            return document;
        }

        public void setDocument(String document) {
            this.document = document;
        }

        public String getDv() {
            return dv;
        }

        public void setDv(String dv) {
            this.dv = dv;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object[] getActivities() {
            return activities;
        }

        public void setActivities(Object[] activities) {
            this.activities = activities;
        }
    }

}
