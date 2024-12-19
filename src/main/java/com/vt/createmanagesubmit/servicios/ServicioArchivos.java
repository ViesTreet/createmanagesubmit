package com.vt.createmanagesubmit.servicios;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.JodConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.modelos.Plantilla;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnos;
import com.vt.createmanagesubmit.repositorios.RepositorioPlantillas;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ServicioArchivos {

    @Autowired
    private RepositorioAlumnos alumnoRepo;

    @Autowired
    private RepositorioPlantillas plantillaRepo;

    public void leerExcelYGuardarEnBD(String rutaArchivo) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(rutaArchivo));

        Workbook workbook = WorkbookFactory.create(fileInputStream);

        Plantilla planti = new Plantilla();
        planti.setNombreCertificado("aaaaa");
        planti.setPathArchivo("src/main/resources/static/DIPLOMAS SETOP Matriz.pptx");
        plantillaRepo.save(planti);

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
                        asignarValorAtributo(alumno, nombreColumna, celda);
                    }
                }

                System.out.println(alumno.getNombreAsistente()+" "+alumno.getRut());
                if (alumno.getNombreAsistente() != null) {
                Optional<Plantilla> optionalPlantilla = plantillaRepo.findById(1L);
                if (optionalPlantilla.isPresent()) {
                    Plantilla plantilla = optionalPlantilla.get();
                    alumno.setPlantilla(plantilla);
                    alumnoRepo.save(alumno);
                } else {
        // Manejar el caso en que la plantilla no se encuentra
                    throw new EntityNotFoundException("Plantilla no encontrada");
                }
}
            }
        }

        workbook.close();
        fileInputStream.close();
    }

    private void asignarValorAtributo(Alumno alumno, String nombreColumna, Cell celda) {
        // Obtener el valor de la celda como String
        String valorCelda = obtenerValorCeldaComoString(celda);

        // Asignar el valor al atributo correspondiente
        switch (nombreColumna.toLowerCase()) {
            case "nº":
            case "número": // Si la columna se llama "Nº" o "Número"
                // Si quieres guardar este dato, puedes agregar un atributo en Alumno
                break;
            case "NOMBRE ASISTENTE":
            case "nombre asistente":
                alumno.setNombreAsistente(valorCelda);
                break;
            case "nombre curso":
            case "Nombre curso":
                alumno.setNombreCurso(valorCelda);
                break;
            case "dias curso":
            case "dias  curso":
                alumno.setDiasCursos(valorCelda);
                break;
            case "Nº de Horas":
            case "numero horas":
                alumno.setNumeroHoras(valorCelda);
                break;
            case "Nº Correlativo Interno":
            case "numero correlativo interno":
                alumno.setNumeroCorrelativoInterno(valorCelda);
                break;
            case "cliente":
            case "Cliente":
                alumno.setCliente(valorCelda);
                break;
            case "obra":
            case "Obra":
                alumno.setObra(valorCelda);
                break;
            case "codigo":
            case "Codigo":
                alumno.setCodigo(valorCelda);
                break;
            case "nota aprobación":
            case "Nota Aprobación":
                alumno.setNotaAprovacion(valorCelda);
                break;
            case "relator":
            case "Relator":
                alumno.setRelator(valorCelda);
                break;
            case "asistencia":
            case "Asistencia":
                alumno.setAsistencia(valorCelda);
                break;
            case "estado":
            case "Estado":
                alumno.setEstado(valorCelda);
                break;
            case "diploma":
            case "Diploma":
                alumno.setDiploma(valorCelda);
                break;
            case "rut":
            case "Rut":
                alumno.setRut(valorCelda);
                break;
            case "correo":
            case "Correo":
                alumno.setCorreo(valorCelda);
                break;
            default:
                // Si hay columnas que no corresponden a ningún atributo, puedes ignorarlas
                break;
        }
    }

    private String obtenerValorCeldaComoString(Cell celda) {
        switch (celda.getCellType()) {
            case STRING:
                return celda.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(celda)) {
                    // Si es una fecha
                    Date date = celda.getDateCellValue();
                    return new SimpleDateFormat("yyyy-MM-dd").format(date);
                } else {
                    // Si es un número
                    return String.valueOf(celda.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(celda.getBooleanCellValue());
            case FORMULA:
                // Evaluar la fórmula
                FormulaEvaluator evaluator = celda.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                CellValue cellValue = evaluator.evaluate(celda);
                return cellValue.formatAsString();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    public void generateCertificates() throws Exception {

        List<Alumno> alumnos = alumnoRepo.findAll();
        for (Alumno alumno : alumnos) {
            generateCertificateForAlumno(alumno);
        }
    }

    private void generateCertificateForAlumno(Alumno alumno) throws Exception {
        // Obtén la plantilla asociada al alumno
        Plantilla plantilla = alumno.getPlantilla();
        if (plantilla == null) {
            throw new Exception("No hay una plantilla asociada al Alumno con ID " + alumno.getId());
        }

        // Carga la plantilla PPTX desde pathArchivo
        String templatePath = plantilla.getPathArchivo();
        // Asegúrate de que plantilla.getPathArchivo() es la ruta correcta al archivo PPTX

        // Carga el archivo PPTX usando Apache POI
        XMLSlideShow ppt;
        try (FileInputStream inputStream = new FileInputStream(templatePath)) {
            ppt = new XMLSlideShow(inputStream);
        }

        // Modifica las shapes con nombre 'name' y 'contenido'
        for (XSLFSlide slide : ppt.getSlides()) {
            for (XSLFShape shape : slide.getShapes()) {
                if (shape instanceof XSLFTextShape) {
                    XSLFTextShape textShape = (XSLFTextShape) shape;
                    String shapeName = textShape.getShapeName();
                    if ("name".equals(shapeName)) {
                        textShape.setText(alumno.getNombreAsistente());
                    } else if ("contenido".equals(shapeName)) {
                        textShape.setText(alumno.getNombreCurso());
                    }
                }
            }
        }

        // Guarda el PPTX modificado en un archivo temporal
        String outputPptxPath = "temp/" + alumno.getId() + ".pptx";
        File tempDir = new File("temp");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(outputPptxPath)) {
            ppt.write(out);
        }

        // Convierte el PPTX modificado a PDF usando JODConverter
        String outputPdfPath = "output/pdf/" + alumno.getId() + ".pdf";
        File pdfDir = new File("output/pdf");
        if (!pdfDir.exists()) {
            pdfDir.mkdirs();
        }

        // Configura el OfficeManager con la ruta donde está instalado LibreOffice
        LocalOfficeManager officeManager = LocalOfficeManager.builder()
            .install()
            .build();

        try {
            officeManager.start();
            JodConverter.convert(new File(outputPptxPath))
                .as(DefaultDocumentFormatRegistry.PPTX)
                .to(new File(outputPdfPath))
                .as(DefaultDocumentFormatRegistry.PDF)
                .execute();
        } catch (OfficeException e) {
            e.printStackTrace();
        } finally {
            OfficeUtils.stopQuietly(officeManager);
        }

        new File(outputPptxPath).delete();
    }
    
}