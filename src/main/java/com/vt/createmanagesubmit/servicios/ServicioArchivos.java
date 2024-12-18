package com.vt.createmanagesubmit.servicios;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vt.createmanagesubmit.modelos.Alumno;
import com.vt.createmanagesubmit.repositorios.RepositorioAlumnos;

@Service
public class ServicioArchivos {

    @Autowired
    private RepositorioAlumnos alumnoRepository;

    public void leerExcelYGuardarEnBD(String rutaArchivo) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(rutaArchivo));

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
                        asignarValorAtributo(alumno, nombreColumna, celda);
                    }
                }

                System.out.println(alumno.getNombreAsistente()+" "+alumno.getRut());
                alumnoRepository.save(alumno);
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
}