package com.vt.createmanagesubmit.servicios;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.TareaProgramada;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnos;

@Service
public class TareaMoodleService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RepositorioAlumnos alumnoRepo;

    @Value("https://aulavirtual.e-volution.cl")
    private String moodleBaseUrl;
    @Value("255a3f0b97656fcec9552a98df8c13c3")
    private String moodleToken;

    private static final Pattern NOMBRE_FINAL = Pattern.compile(".*final.*", Pattern.CASE_INSENSITIVE);

    public TareaMoodleService(RestTemplate restTemplate,
                              ObjectMapper objectMapper,
                              RepositorioAlumnos alumnoRepo) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.alumnoRepo = alumnoRepo;
    }

    public void procesarTarea(TareaProgramada tarea) throws JsonProcessingException {
        Long courseId = tarea.getIDCurso();

        // 1) Obtener todo el contenido del curso
        String urlContents = UriComponentsBuilder
            .fromUriString(moodleBaseUrl + "/webservice/rest/server.php")
            .queryParam("wstoken", moodleToken)
            .queryParam("wsfunction", "core_course_get_contents")
            .queryParam("moodlewsrestformat", "json")
            .queryParam("courseid", courseId)
            .toUriString();

        String contentsJson = restTemplate.getForObject(urlContents, String.class);
        JsonNode sections = objectMapper.readTree(contentsJson);

        // 2) Encontrar todos los cmid que coincidan con “final”
        List<Integer> cmidsFinal = new ArrayList<>();
        for (JsonNode section : sections) {
            for (JsonNode module : section.get("modules")) {
                String name = module.get("name").asText();
                if (NOMBRE_FINAL.matcher(name).matches()) {
                    cmidsFinal.add(module.get("id").asInt());
                }
            }
        }
        if (cmidsFinal.isEmpty()) {
            // no hay actividad final → nada que hacer
            return;
        }

        // 3) Obtener lista de alumnos matriculados en el curso
        String urlEnrolled = UriComponentsBuilder
            .fromUriString(moodleBaseUrl + "/webservice/rest/server.php")
            .queryParam("wstoken", moodleToken)
            .queryParam("wsfunction", "core_enrol_get_enrolled_users")
            .queryParam("moodlewsrestformat", "json")
            .queryParam("courseid", courseId)
            .toUriString();
        String enrolledJson = restTemplate.getForObject(urlEnrolled, String.class);
        List<JsonNode> users = objectMapper.readTree(enrolledJson).findValues("id");

        // 4) Para cada usuario, comprobar finalización y (si quiz) nota
        List<Alumno> resultado = new ArrayList<>();
        for (JsonNode userNode : users) {
            long userId = userNode.asLong();

            // 4.a) obtener estados de finalización
            String urlStatus = UriComponentsBuilder
                .fromUriString(moodleBaseUrl + "/webservice/rest/server.php")
                .queryParam("wstoken", moodleToken)
                .queryParam("wsfunction", "core_completion_get_activities_completion_status")
                .queryParam("moodlewsrestformat", "json")
                .queryParam("courseid", courseId)
                .queryParam("userid", userId)
                .toUriString();
            JsonNode statuses = objectMapper.readTree(
                restTemplate.getForObject(urlStatus, String.class)
            ).get("statuses");

            boolean completado = false;
            for (JsonNode st : statuses) {
                int cmid = st.get("cmid").asInt();
                int state = st.get("state").asInt();
                if (cmidsFinal.contains(cmid) && state == 1) {
                    // si es quiz, también comprobar nota mínima aprobatoria
                    JsonNode module = findModuleByCmid(sections, cmid);
                    if ("quiz".equals(module.get("modname").asText())) {
                        // 4.b) comprobar nota
                        String urlGrade = UriComponentsBuilder
                            .fromUriString(moodleBaseUrl + "/webservice/rest/server.php")
                            .queryParam("wstoken", moodleToken)
                            .queryParam("wsfunction", "mod_quiz_get_user_best_grade")
                            .queryParam("moodlewsrestformat", "json")
                            .queryParam("quizid", module.get("instance").asInt())
                            .queryParam("userid", userId)
                            .toUriString();
                        JsonNode gradeNode = objectMapper.readTree(
                            restTemplate.getForObject(urlGrade, String.class)
                        );
                        double grade = gradeNode.get("grade").asDouble();
                        if (grade >= module.get("grade").asDouble()) {
                            completado = true;
                        }
                    } else {
                        completado = true;
                    }
                    if (completado) break;
                }
            }
            if (!completado) continue;

            // 5) Obtener datos de usuario (nombre y correo)
            String urlUser = UriComponentsBuilder
                .fromUriString(moodleBaseUrl + "/webservice/rest/server.php")
                .queryParam("wstoken", moodleToken)
                .queryParam("wsfunction", "core_user_get_users")
                .queryParam("moodlewsrestformat", "json")
                .queryParam("criteria[0][key]", "id")
                .queryParam("criteria[0][value]", userId)
                .toUriString();
            JsonNode userInfo = objectMapper.readTree(
                restTemplate.getForObject(urlUser, String.class)
            ).get("users").get(0);

            // 6) Mapear a Alumno
            Alumno a = new Alumno();
            a.setNombreAsistente(userInfo.get("fullname").asText());
            a.setCorreo(userInfo.get("email").asText());
            // campos comunes copia de TareaProgramada
            a.setNombreCurso(tarea.getNombreCurso());
            a.setDiasCursos(tarea.getDiasCursos());
            a.setDuracion(tarea.getDuracion());
            a.setModalidad(tarea.getModalidad());
            a.setCliente(tarea.getCliente());
            a.setRelator(tarea.getRelator());
            a.setLugarYfechaEmision(tarea.getLugarYfechaEmision());
            a.setPlantilla(tarea.getPlantilla());
            // valores fijos
            a.setDiploma("noEnviado");
            a.setEstado("Aprobado");
            a.setRut(null);
            a.setUbicacionSubida(tarea.getUbicacionSubida());
            resultado.add(a);
        }

        // 7) Ejecutar según “accion”
        if ("generar".equalsIgnoreCase(tarea.getAccion())) {
            // TODO: ejecutar la función de generación de certificados, p.ej.:
            // certificadoService.generarParaAlumnos(resultado, tarea.getPlantilla());
        } else if ("guardar".equalsIgnoreCase(tarea.getAccion())) {
            alumnoRepo.saveAll(resultado);
        }
    }

    /** Busca en los JSON sections el módulo con el cmid dado */
    private JsonNode findModuleByCmid(JsonNode sections, int cmid) {
        for (JsonNode sec : sections) {
            for (JsonNode mod : sec.get("modules")) {
                if (mod.get("id").asInt() == cmid) {
                    return mod;
                }
            }
        }
        return null;
    }
}

