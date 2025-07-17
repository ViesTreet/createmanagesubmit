package com.vt.createmanagesubmit.servicios;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ServicioApi {

    @Value("255a3f0b97656fcec9552a98df8c13c3") // Token desde application.properties
    private String moodleToken;

    @Value("https://aulavirtual.e-volution.cl") // URL base de Moodle
    private String moodleBaseUrl;

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
        String nombreFormateado = nombre.trim().toUpperCase();

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

    public static class QuizResponse {
        private List<Quiz> quizzes;
        public List<Quiz> getQuizzes() { return quizzes; }
        public void setQuizzes(List<Quiz> quizzes) { this.quizzes = quizzes; }
    }

    public static class Quiz {
        private Long id;
        private String name;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
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

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Obtener cursos de Moodle (solo id y nombre)
   public List<Map<String, Object>> obtenerCursosMoodle() {
    // Construye la URL con parámetros usando UriComponentsBuilder
    String fullUrl = UriComponentsBuilder.fromUriString(moodleBaseUrl + "/webservice/rest/server.php")
        .queryParam("wstoken", moodleToken)
        .queryParam("wsfunction", "core_course_get_courses")
        .queryParam("moodlewsrestformat", "json")
        .encode() // Codifica caracteres especiales
        .toUriString();

    try {
        // Usa getForEntity para capturar posibles errores HTTP
        ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);
        
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Error en Moodle: " + response.getStatusCode());
        }

        String responseBody = response.getBody();

        JsonNode root = objectMapper.readTree(responseBody);

        // Maneja el caso donde Moodle devuelve un objeto de error (no un array)
        if (root.isObject() && root.has("exception")) {
            throw new RuntimeException("Error de Moodle: " + root.get("message").asText());
        }

        List<Map<String, Object>> cursos = new ArrayList<>();
        for (JsonNode cursoNode : root) {
            Map<String, Object> curso = new HashMap<>();
            curso.put("id", cursoNode.get("id").asLong());
            curso.put("nombre", cursoNode.get("fullname").asText());
            cursos.add(curso);
        }
        List<Map<String, Object>> cursosFiltrados = new ArrayList<>();
        for (int x=(cursos.size()-1);x>(cursos.size()-21);x--){
            cursosFiltrados.add(cursos.get(x));
        }
        return cursosFiltrados;

    } catch (HttpClientErrorException e) {
        System.err.println("Error HTTP: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        throw new RuntimeException("Error al llamar a Moodle");
    } catch (JsonProcessingException e) {
        throw new RuntimeException("Error al parsear JSON de Moodle");
    }
}

public Long obtenerQuizId(Long courseId) {
    // 1) Montar URL
    String url = UriComponentsBuilder
        .fromUriString(moodleBaseUrl + "/webservice/rest/server.php")
        .queryParam("wstoken", moodleToken)
        .queryParam("wsfunction", "mod_quiz_get_quizzes_by_courses")
        .queryParam("moodlewsrestformat", "json")
        .queryParam("courseids[0]", courseId)
        .toUriString();

    // 2) Llamada HTTP
    String body = restTemplate.getForObject(url, String.class);

    try {
        // 3) Parseo genérico
        JsonNode root = objectMapper.readTree(body);

        // 4) ¿Error de Moodle?
        if (root.isObject() && root.has("exception")) {
            String msg = root.path("message").asText("Error desconocido de Moodle");
            throw new RuntimeException("Moodle API: " + msg);
        }

        // 5) Obtener array de quizzes
        JsonNode quizzesNode = root.path("quizzes");
        if (!quizzesNode.isArray()) {
            throw new RuntimeException("Formato inesperado: 'quizzes' no es un array");
        }

        // 6) Buscar nombre "tarea final" o "prueba final" (case‑insensitive)
        for (JsonNode quizNode : quizzesNode) {
            String nombre = quizNode.path("name").asText("").trim().toLowerCase();
            if (nombre.equals("tarea final") || nombre.equals("prueba final")) {
                return quizNode.path("id").asLong();
            }
        }

        // 7) Si no lo encontramos
        return null;

    } catch (JsonProcessingException e) {
        throw new RuntimeException("Error al parsear JSON de quizzes", e);
    }
}



}
