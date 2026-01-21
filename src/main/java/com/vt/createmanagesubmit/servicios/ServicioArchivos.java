package com.vt.createmanagesubmit.servicios;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vt.createmanagesubmit.dto.AlumnoDTO;
import com.vt.createmanagesubmit.exceptions.CertificateGenerationException;
import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnos;
import com.vt.createmanagesubmit.repositorios.RepositorioPlantillas;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class ServicioArchivos {

    @Autowired
    private RepositorioAlumnos alumnoRepo;

    @Autowired
    private RepositorioPlantillas plantillaRepo;

    @Autowired
    @Lazy
    private Servicio servicio;

    @Autowired
    @Lazy
    private ServicioApi servicioApi;

    @Autowired
    @Lazy
    private ServicioGenerarCertificado servicioGenerarCertificado;

    @Value("${DIP_MAIL}")
    String correoEmpresa;

    @Async
    public CompletableFuture<Void> leerExcelYGuardarEnBD(byte[] fileBytes, String estadoDiplomaExcel, String plantilla, String estadoExcel, String rutificador, String ubicacion) throws IOException {
        try (InputStream fileInputStream = new ByteArrayInputStream(fileBytes)){
            Workbook workbook = WorkbookFactory.create(fileInputStream);

        // Iterar sobre las hojas del libro
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet hoja = workbook.getSheetAt(i);

                // Obtener el encabezado (asumiendo que está en la primera fila)
                Row encabezado = hoja.getRow(0);
                Map<Integer, String> mapaColumnas = new HashMap<>();

                // Mapear los nombres de las columnas a sus índices
                for (Cell celda : encabezado) {
                    int indiceColumna = celda.getColumnIndex();
                    String nombreColumna = celda.getStringCellValue();
                    mapaColumnas.put(indiceColumna, nombreColumna.trim());
                }

                // Iterar sobre las filas de datos (empezando desde la segunda fila)
                for (int filaIndex = 1; filaIndex <= hoja.getLastRowNum(); filaIndex++) {
                    Row fila = hoja.getRow(filaIndex);
                    if (fila == null) {
                        continue; // Saltar filas vacías
                    }

                    Alumno alumno = new Alumno();

                    // Iterar sobre las celdas de la fila
                    for (Cell celda : fila) {
                        int indiceColumna = celda.getColumnIndex();
                        String nombreColumna = mapaColumnas.get(indiceColumna);
                        if (nombreColumna != null) {
                            asignarValorAtributo(alumno, nombreColumna, celda, rutificador);
                        }
                    }

                    if (alumno.getNombreAsistente() != null) {
                        if (!plantilla.trim().equals("excel")) {
                            Optional<Plantilla> plantillaOp = servicio.plantillaPorNombre(plantilla);
                            if (plantillaOp.isPresent()) {
                                Plantilla plantillaAlumnoEstablecido = plantillaOp.get();
                                alumno.setPlantilla(plantillaAlumnoEstablecido);
                            }
                        }

                        if (alumno.getCorreo() == null || alumno.getCorreo().trim().isEmpty() || alumno.getCorreo().trim().isBlank()) {
                            alumno.setCorreo(correoEmpresa);
                        }

                        if (alumno.getEstado() == null || alumno.getEstado().trim().isEmpty()) {
                            alumno.setEstado("revisionManual");
                        }

                        if (!estadoExcel.equals("Eexcel")) {
                            if (estadoExcel.equals("Eauto")) {
                                alumno = servicio.funcionEstadoManual(alumno);
                            } else {
                                alumno.setEstado(estadoExcel);
                            }
                        }

                        if (alumno.getEstado() == null || alumno.getEstado().trim().isEmpty() || alumno.getEstado().trim().equals("Eexcel")) {
                            alumno.setEstado("revisionManual");
                        }

                        if (!estadoDiplomaExcel.equals("diploExcel")) {
                            if (estadoDiplomaExcel.equals("noEnviar")) {
                                alumno.setDiploma("noEnviado");
                            } else if (estadoDiplomaExcel.equals("enviarApro")) {
                                if (alumno.getEstado().equals("aprobado")) {
                                    alumno.setDiploma("enviado");
                                }
                            } else if (estadoDiplomaExcel.equals("enviarTodos")) {
                                alumno.setDiploma("enviado");
                            }
                        }
                        if(rutificador.trim().equals("rutiTodos")){
                            if(!alumno.getRut().trim().isEmpty() && alumno.getRut() != null){
                                String rutFormateado = formatearRut(alumno.getRut());
                                String nombreRutificado = servicioApi.obtenerNombrePorRut(rutFormateado);
                                if(!nombreRutificado.trim().equals("nombreNoEncontrado")){
                                    alumno.setNombreAsistente(nombreRutificado);
                                }
        
                            }
                        }

                        if(alumno.getPlantilla() == null){
                            Optional<Plantilla> optPlantilla = servicio.plantillaPorNombre("Error en encontrar plantilla");
                            if(optPlantilla.isPresent()){
                                Plantilla plantilla3 = optPlantilla.get();
                                alumno.setPlantilla(plantilla3);
                            }else{
                                Plantilla plantilla3 = servicio.plantillaPorId(1L);
                                alumno.setPlantilla(plantilla3);
                            }
                        }

                        if(alumno.getDiploma() == null || alumno.getDiploma().trim().isEmpty()){
                            alumno.setDiploma("noEnviado");
                        }
                        String nombre = alumno.getNombreAsistente().trim().toUpperCase();
                        alumno.setNombreAsistente(nombre);
                        if(!alumno.getNombreAsistente().isEmpty()){
                            alumno.setUbicacionSubida(ubicacion);
                            alumnoRepo.save(alumno);
                            servicio.numeroCorrelativoAuto(alumno);
                            if(estadoDiplomaExcel.equals("enviarApro")&&estadoExcel.equals("aprobado")){
                                estadoDiplomaExcel = "enviarTodos";
                            }
                            if (estadoDiplomaExcel.equals("enviarTodos")) {
                                try {
                                    servicioGenerarCertificado.generateCertificateForAlumno(alumno);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (estadoDiplomaExcel.equals("enviarApro")) {
                                if (alumno.getEstado().equals("aprobado")&&alumno.getDiploma().equals("noEnviado")) {
                                    try {
                                        servicioGenerarCertificado.generateCertificateForAlumno(alumno);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            workbook.close();
            fileInputStream.close();
        } catch (Exception ex) {
            throw new IOException("Error al procesar el excel, verifique los datos.",ex);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    private void asignarValorAtributo(Alumno alumno, String nombreColumna, Cell celda, String rutificar) {
        // Obtén el valor de la celda como String
        String valorCelda = obtenerValorCeldaComoString(celda);

        switch (nombreColumna.toLowerCase()) {
            case "nº":
            case "número":
                break;
            case "Nombre asistente":
            case "Nombre":
            case "nombre":
            case "nombre asistente":
                valorCelda = servicioApi.formatearNombre(valorCelda);
                alumno.setNombreAsistente(valorCelda);
                break;
            case "Nombre curso":
            case "curso":
            case "Curos":
            case "nombre curso":
                alumno.setNombreCurso(valorCelda);
                break;
            case "días curso":
            case "días del curso":
            case "dias curso":
            case "dias del curso":
            case "dias":
                alumno.setDiasCursos(valorCelda);
                break;
            case "nº de horas":
            case "numero horas":
            case "numero de horas":
            case "duracion del curso":
            case "duracion curso":
            case "duración del curso":
            case "duración curso":
            case "duracion":
                alumno.setDuracion(valorCelda);
                break;
            case "cliente":
            case "Cliente":
                alumno.setCliente(valorCelda);
                break;
            case "identificador":
            case "id":
                alumno.setIdentificador(valorCelda);
                break;
            case "nota aprobación":
            case "nota aprobacion":
            case "Nota aprobación":
            case "Nota aprobacion":
            case "nota":
                alumno.setNotaAprobacion(valorCelda);
                break;
            case "relator":
            case "profesor":
                alumno.setRelator(valorCelda);
                break;
            case "asistencia":
            case "Asistencia":
                alumno.setAsistencia(valorCelda);
                break;
            case "estado":
            case "Estado":
                if (valorCelda.trim().equalsIgnoreCase("aprobado")) {
                    alumno.setEstado("aprobado");
                } else {
                    alumno.setEstado("noAprobado");
                }
                break;
            case "diploma":
            case "Diploma":
                if (valorCelda.trim().equalsIgnoreCase("no enviado")) {
                    alumno.setDiploma("noEnviado");
                } else if (valorCelda.trim().equalsIgnoreCase("enviado")) {
                    alumno.setDiploma("enviado");
                } else {
                    alumno.setDiploma("revisionManual");
                }
                break;
            case "rut":
            case "RUT":
            case "Rut":
                String rutForma = formatearRut(valorCelda);
                alumno.setRut(rutForma);
                break;
            case "correo":
            case "Correo":
                alumno.setCorreo(valorCelda);
                break;
            case "modalidad":
            case "Modalidad":
                alumno.setModalidad(valorCelda);
                break;
            case "plantilla":
            case "Plantilla":
                Optional<Plantilla> plantillaAlumnoOptional = servicio.plantillaPorNombre(valorCelda);
                if (plantillaAlumnoOptional.isPresent()) {
                    Plantilla plantillaAlumno = plantillaAlumnoOptional.get();
                    alumno.setPlantilla(plantillaAlumno);
                } else {
                    Optional<Plantilla> plantillaErrorOptional = servicio.plantillaPorNombre("Error en encontrar plantilla");
                    if (plantillaErrorOptional.isPresent()) {
                        Plantilla plantillaAlumno = plantillaErrorOptional.get();
                        alumno.setPlantilla(plantillaAlumno);
                    } else {
                        Plantilla plantillaAlumno = new Plantilla();
                        plantillaAlumno.setNombreCertificado("Error en encontrar plantilla");
                        plantillaRepo.save(plantillaAlumno);
                        alumno.setPlantilla(plantillaAlumno);
                    }
                }
                break;
            case "rutificador":
            case "Rutificador":
                if(rutificar.trim().equals("rutiExcel")){
                    if((valorCelda.trim().equalsIgnoreCase("si")) && (!alumno.getRut().trim().isEmpty() && alumno.getRut() != null)){
                        String rutFormateado = formatearRut(alumno.getRut());
                        String nombreRutificado = servicioApi.obtenerNombrePorRut(rutFormateado);
                        if(!nombreRutificado.trim().equals("nombreNoEncontrado")){
                            alumno.setNombreAsistente(nombreRutificado);
                        }

                    }
                }
                break;
            case "emision":
            case "emisión":
            case "lugar y fecha emision":
            case "lugar y fecha de emision":
                alumno.setLugarYfechaEmision(valorCelda);
                break;
            default:
                // Ignorar columnas no reconocidas
                break;
        }
    }

    private String obtenerValorCeldaComoString(Cell celda) {
        switch (celda.getCellType()) {
            case STRING:
                return celda.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(celda)) {
                    Date date = celda.getDateCellValue();
                    return new SimpleDateFormat("yyyy-MM-dd").format(date);
                } else {
                    double valorNumerico = celda.getNumericCellValue();
                    if (valorNumerico == Math.floor(valorNumerico)) {
                    // Si el número no tiene decimales (entero), lo convertimos sin decimales
                        return String.format("%.0f", valorNumerico);
                    } else {
                    // Si tiene decimales, se conserva el formato decimal
                        return String.valueOf(valorNumerico);
                }
                }
            case BOOLEAN:
                return String.valueOf(celda.getBooleanCellValue());
            case FORMULA:
                FormulaEvaluator evaluator = celda.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                CellValue cellValue = evaluator.evaluate(celda);
                return cellValue.formatAsString();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    public String formatearRut(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return rut;  
        }

        
        String rutSinPuntos = rut.replaceAll(".", "");

        
        if (!rutSinPuntos.contains("-")) {
            int largoRut = rutSinPuntos.length();
            if (largoRut > 1) {
                rutSinPuntos = rutSinPuntos.substring(0, largoRut - 1) + "-" + rutSinPuntos.charAt(largoRut - 1);
            }
        }

        return rutSinPuntos;
    }

    @Transactional
    public void generateCertificatesAll() throws Exception {
        List<Alumno> alumnos = alumnoRepo.findAllByDiplomaAndEstado("noEnviado", "aprobado");
        Optional<Plantilla> optPlantilla = servicio.plantillaPorNombre("Error en encontrar plantilla");
        Plantilla plantillaError = new Plantilla();
        if(optPlantilla.isPresent()){
            plantillaError = optPlantilla.get();
        }
        for (Alumno alumno : alumnos) {
            if(alumno.getPlantilla() != plantillaError){
                servicioGenerarCertificado.generateCertificateForAlumno(alumno);
                alumno.setDiploma("enviado");
                alumnoRepo.save(alumno);
            }
        }
    }

    @Transactional
    public void generateCertificatesById(Long id) throws Exception {
        Alumno alumno = alumnoRepo.findById(id).orElseThrow(() -> new Exception("Alumno no encontrado con ID: " + id));

        if (alumno != null) {
            Plantilla plantilla = alumno.getPlantilla();
            if (plantilla != null) {
                Hibernate.initialize(plantilla);
            }
            try {
                servicioGenerarCertificado.generateCertificateForAlumno(alumno);
                alumno.setDiploma("enviado");
                alumnoRepo.save(alumno);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CertificateGenerationException("Ocurrió un error al generar el certificado.");
            }
            
        }
    }

    public void exportToExcel(HttpServletResponse response) throws IOException {
        // Obtener la lista de alumnos
        List<Alumno> alumnos = alumnoRepo.findAll();

        // Crear un nuevo libro de Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Alumnos");

        // Crear la fila de cabecera
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Nombre Asistente", "Nombre Curso", "Días Curso", "Número Horas", 
                            "Cliente", "Identificador", "Código", "Nota Aprobación", "Relator", "Asistencia", "Estado", 
                            "Diploma", "RUT", "Correo", "Plantilla", "Ubiación", "Emision"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Llenar los datos de los alumnos
        int rowNum = 1;
        for (Alumno alumno : alumnos) {
            AlumnoDTO alumnoDTO = new AlumnoDTO(alumno);
            Row row = sheet.createRow(rowNum++);

            // Usar una función auxiliar para manejar los nulls
            row.createCell(0).setCellValue(safeGet(alumnoDTO.getNombreAsistente()));
            row.createCell(1).setCellValue(safeGet(alumnoDTO.getNombreCurso()));
            row.createCell(2).setCellValue(safeGet(alumnoDTO.getDiasCursos()));
            row.createCell(3).setCellValue(safeGet(alumnoDTO.getNumeroHoras()));
            row.createCell(5).setCellValue(safeGet(alumnoDTO.getCliente()));
            row.createCell(6).setCellValue(safeGet(alumnoDTO.getIdentificador()));
            row.createCell(8).setCellValue(safeGet(alumnoDTO.getNotaAprovacion()));
            row.createCell(9).setCellValue(safeGet(alumnoDTO.getRelator()));
            row.createCell(10).setCellValue(safeGet(alumnoDTO.getAsistencia()));
            row.createCell(11).setCellValue(safeGet(alumnoDTO.getEstado()));
            row.createCell(12).setCellValue(safeGet(alumnoDTO.getDiploma()));
            row.createCell(13).setCellValue(safeGet(alumnoDTO.getRut()));
            row.createCell(14).setCellValue(safeGet(alumnoDTO.getCorreo()));
            row.createCell(15).setCellValue(safeGet(alumnoDTO.getPlantilla()));
            row.createCell(16).setCellValue(safeGet(alumnoDTO.getUbicacionSubida()));
            row.createCell(17).setCellValue(safeGet(alumnoDTO.getLugarYfechaEmision()));
        }

        // Configuración de la respuesta HTTP para descargar el archivo Excel
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=alumnos.xlsx");

        // Escribir el archivo Excel en la respuesta HTTP
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // Función auxiliar para manejar nulls y devolver un espacio vacío
    private String safeGet(String value) {
        return value == null ? "" : value;
    }
}

    
