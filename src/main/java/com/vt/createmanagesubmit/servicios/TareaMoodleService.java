package com.vt.createmanagesubmit.servicios;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.TareaProgramada;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnos;

@Service
public class TareaMoodleService {

    @Autowired
    private Servicio ser;

    @Autowired
    private ServicioArchivos serAr;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RepositorioAlumnos alumnoRepo;

    @Value("https://aulavirtual.e-volution.cl")
    private String moodleBaseUrl;
    @Value("255a3f0b97656fcec9552a98df8c13c3")
    private String moodleToken;

    public TareaMoodleService(RestTemplate restTemplate,
                              ObjectMapper objectMapper,
                              RepositorioAlumnos alumnoRepo) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.alumnoRepo = alumnoRepo;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void procesarTarea(TareaProgramada tarea) throws Exception {
        Long courseId = tarea.getIDCurso();
        List<Long> userIds = obtenerUsuariosMatriculados(courseId);

        Map<Long, Boolean> completadosMap =
            obtenerEstadosFinalizacionBatch(courseId, userIds);

        List<Long> aprobados = completadosMap.entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        if (aprobados.isEmpty()) return;

        Map<Long, JsonNode> infoUsuarios = obtenerUsuariosInfoBatch(aprobados);

        List<Alumno> resultado = new ArrayList<>();
        for (Long uid : aprobados) {
            JsonNode info = infoUsuarios.get(uid);
            Alumno a = new Alumno();
            String nombreAsistente;
            if (info.hasNonNull("fullname")) {
                nombreAsistente = info.get("fullname").asText();
            } else {
                String first = info.hasNonNull("firstname") ? info.get("firstname").asText() : "";
                String last  = info.hasNonNull("lastname")  ? info.get("lastname").asText()  : "";
                nombreAsistente = (first + " " + last).trim();
            }
            double promedioMoodle = obtenerPromedioNotas(courseId, uid);
            double notaF = Math.round(promedioMoodle * 10.0) / 10.0;
            a.setNotaAprobacion(String.valueOf(notaF));
            a.setNombreAsistente(nombreAsistente.toUpperCase());
            a.setCorreo(info.get("email").asText());
            // datos heredados de la tarea
            a.setNombreCurso(tarea.getNombreCurso());
            a.setDiasCursos(tarea.getDiasCursos());
            a.setDuracion(tarea.getDuracion());
            a.setModalidad(tarea.getModalidad());
            a.setCliente(tarea.getCliente());
            a.setRelator(tarea.getRelator());
            a.setLugarYfechaEmision(tarea.getLugarYfechaEmision());
            a.setPlantilla(tarea.getPlantilla());
            a.setDiploma("noEnviado");
            a.setEstado("Aprobado");
            a.setRut(null);
            a.setUbicacionSubida(tarea.getUbicacionSubida());
            a.setAsistencia("100");
            resultado.add(a);
        }

        for (Alumno alumno : resultado){
            ser.numeroCorrelativoAuto(alumno);
            if(tarea.getAccion().equals("generar")){
                serAr.generateCertificatesById(alumno.getId());
            }
        }
    }
    
    private List<Long> obtenerUsuariosMatriculados(Long courseId) throws Exception {
        String json = restTemplate.getForObject(
            UriComponentsBuilder
                .fromUriString(moodleBaseUrl + "/webservice/rest/server.php")
                .queryParam("wstoken", moodleToken)
                .queryParam("wsfunction", "core_enrol_get_enrolled_users")
                .queryParam("moodlewsrestformat", "json")
                .queryParam("courseid", courseId)
                .toUriString(),
            String.class
        );
        JsonNode arr = objectMapper.readTree(json);
        List<Long> ids = new ArrayList<>();
        for (JsonNode u : arr) {
            ids.add(u.get("id").asLong());
        }
        return ids;
    }

    private Map<Long, Boolean> obtenerEstadosFinalizacionBatch(
        Long courseId,
        List<Long> userIds
    ) throws Exception {
        Map<Long, Boolean> result = new HashMap<>();
        if (userIds.isEmpty()) return result;

        // Moodle permite un batch de userids[]
        UriComponentsBuilder b = UriComponentsBuilder
            .fromUriString(moodleBaseUrl + "/webservice/rest/server.php")
            .queryParam("wstoken", moodleToken)
            .queryParam("wsfunction", "core_completion_get_course_completion_status")
            .queryParam("moodlewsrestformat", "json")
            .queryParam("courseid", courseId);

        for (int i = 0; i < userIds.size(); i++) {
            b.queryParam("userids[" + i + "]", userIds.get(i));
        }
        String json = restTemplate.getForObject(b.toUriString(), String.class);
        JsonNode arr = objectMapper.readTree(json);

        // Cada elemento: {userid, timecompleted, ...}
        for (JsonNode e : arr) {
            long uid = e.get("userid").asLong();
            boolean done = e.hasNonNull("timecompleted") && e.get("timecompleted").asLong() > 0;
            result.put(uid, done);
        }
        return result;
    }
    private Map<Long, JsonNode> obtenerUsuariosInfoBatch(List<Long> userIds) throws Exception {
        Map<Long, JsonNode> map = new HashMap<>();
        if (userIds.isEmpty()) return map;

        UriComponentsBuilder b = UriComponentsBuilder
            .fromUriString(moodleBaseUrl + "/webservice/rest/server.php")
            .queryParam("wstoken", moodleToken)
            .queryParam("wsfunction", "core_user_get_users_by_field")
            .queryParam("moodlewsrestformat", "json")
            .queryParam("field", "id");

        for (int i = 0; i < userIds.size(); i++) {
            b.queryParam("values[" + i + "]", userIds.get(i));
        }
        String json = restTemplate.getForObject(b.toUriString(), String.class);
        JsonNode arr = objectMapper.readTree(json);
        for (JsonNode u : arr) {
            map.put(u.get("id").asLong(), u);
        }
        return map;
    }

    private double obtenerPromedioNotas(Long courseId, Long userId) throws Exception {
        UriComponentsBuilder b = UriComponentsBuilder
            .fromUriString(moodleBaseUrl + "/webservice/rest/server.php")
            .queryParam("wstoken", moodleToken)
            .queryParam("wsfunction", "gradereport_user_get_grade_items")
            .queryParam("moodlewsrestformat", "json")
            .queryParam("courseid", courseId)
            .queryParam("userid", userId);
        
        String json = restTemplate.getForObject(b.toUriString(), String.class);
        JsonNode root = objectMapper.readTree(json);
        JsonNode gradeItems = root.path("usergrades").get(0).path("gradeitems");
        
        double total = 0.0;
        double totalPeso = 0.0;
        
        for (JsonNode item : gradeItems) {
            if (item.hasNonNull("graderaw") && item.hasNonNull("weightraw")&& item.get("weightraw").asDouble() > 0) {
                double nota = item.get("graderaw").asDouble();
                double peso = item.get("weightraw").asDouble();
                total += nota * peso;
                totalPeso += peso;
            }
        }
    
        // Si no hay peso, devolvemos 0
        if (totalPeso == 0.0) return 0.0;
    
        return total / totalPeso;
    }


}
