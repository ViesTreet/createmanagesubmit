<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %> 
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Alumno</title>
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <link rel="stylesheet" href="/css/styleCustom.css">
    <style>

    </style>
</head>
<body>
    <header class="d-flex align-items-center justify-content-between p-3 bg-light" style="height: 10vh;">
        <div class="logo"><a href="/home"><img src="/images/Logobgremove.png" alt="[LOGO]"></div></a>
        <nav>
            <a href="/dataBaseAlumno/alumno/${alumno.id}" class="btn btn-primary mx-2">Regresar</a>
            <a href="/home" class="btn btn-primary mx-2">Inicio</a>
            <a href="/documentacion/editarAlumno" class="btn btn-primary mx-2">Documentación</a>
        </nav>
    </header>
    <main class="d-flex flex-column align-items-center justify-content-center" style="height: 90vh;">
        <div class="d-flex justify-content-end" style="width: 70vw;"><a class="btn btn-warning" href="/dataBaseAlumno/alumno/${alumno.id}">Regresar</a></div>
        <div class="card" style="width: 70vw;">
            <div class="card-body" style="width: 100%;">
                <h4 class="card-title text-center">Editar Alumno ${alumnoJson.nombreAsistente}</h4>
                <form action="/dataBaseAlumno/editarAlumno" method="post" modelAttribute="alumno">
                    <div class="d-flex justify-content-between">
                        <div style="max-width: 33%;">
                            <div>
                                <label for="nombreAsistente">Nombre Asistente</label>
                                <input type="text" name="nombreAsistente" id="nombreAsistente" class="form-control" placeholder="Nombres Apellidos Asistente">
                            </div>
                            <div>
                                <label for="curso">Cursos</label>
                                <input type="text" name="nombreCurso" id="nombreCurso" placeholder="curso1,curso2,etc...">
                            </div>
                            <div>
                                <label for="diasCursos">Dias del curso</label>
                                <input type="text" name="diasCursos" id="diasCursos" class="form-control" placeholder="dd/m/aa-dd/m/aa">
                            </div>
                            <div>
                                <label for="numeroHoras">Numero de horas</label>
                                <input type="text" name="numeroHoras" id="numeroHoras" class="form-control" placeholder="N° horas">
                            </div>
                            <div>
                                <label for="numeroCorrelativoInterno">Numero correlativo interno</label>
                                <input type="text" name="numeroCorrelativoInterno" id="numeroCorrelativoInterno" class="form-control" placeholder="N° Correlativo interno">
                            </div>
                            <div>
                                <label for="cliente">Cliente</label>
                                <input type="text" name="cliente" id="cliente" class="form-control" placeholder="Cliente">
                            </div>
                        </div>
                        <div style="max-width: 33%;">
                            <div>
                                <label for="obra">Obra</label>
                                <input type="text" name="obra" id="obra" class="form-control" placeholder="Obra">
                            </div>
                            <div>
                                <label for="codigo">Codigo</label>
                                <input type="text" name="codigo" id="codigo" class="form-control" placeholder="Codigo">
                            </div>
                            <div>
                                <label for="notaAprovacion">Nota aprovacion</label>
                                <input type="text" name="notaAprovacion" id="notaAprovacion" class="form-control" placeholder="Ej: 7.0">
                            </div>
                            <div>
                                <label for="relator">Relator</label>
                                <input type="text" name="relator" id="relator" class="form-control" placeholder="Relator">
                            </div>
                            <div>
                                <label for="asistencia">Asistencia</label>
                                <input type="text" name="asistencia" id="asistencia" class="form-control" placeholder="Ej: 100">
                            </div>
                        </div>
                        <div style="max-width: 33%;">
                            <div>
                                <label for="estado">Estado</label>
                                <select class="form-select" name="estado" id="estado">
                                    <option value="auto">Automatico</option>
                                    <option value="aprobado">Aprobado</option>
                                    <option value="noAprobado">No Aprobado</option>
                                </select>
                            </div>
                            <div>
                                <label for="diploma">Diploma</label>
                                <select class="form-select" name="diploma" id="diploma">
                                    <option value="enviado">Enviado</option>
                                    <option value="noEnviado">No enviado</option>
                                </select>
                            </div>
                            <div>
                                <label for="rut">Rut</label>
                                <input type="text" name="rut" id="rut" class="form-control" placeholder="11.111.111-1">
                            </div>
                            <div>
                                <label for="correo">Correo</label>
                                <input type="email" name="correo" id="correo" class="form-control" placeholder="example@example.com">
                            </div>
                            <div>
                                <label for="plantilla">Plantilla</label>
                                <select name="plantilla" id="plantilla" class="form-select">
                                    <c:forEach items="${plantillas}" var="plantilla">
                                        <option value="${plantilla.id}">${plantilla.nombreCertificado}</option>

                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="d-flex align-items-center justify-content-center pt-1">
                        <input type="hidden" name="id" value="${alumno.id}">
                        <input class="btn btn-success" type="submit" value="Editar">

                    </div>
                    
                </form>
            </div>
        </div>
    </main>
    <script>
        // Script para rellenar el formulario con los datos actuales del alumno
        document.addEventListener('DOMContentLoaded', function() {
            // Rellenar cada campo con su valor correspondiente
            document.getElementById('nombreAsistente').value = "${alumno.nombreAsistente}";
            document.getElementById('nombreCurso').value = "${alumno.nombreCurso}";
            document.getElementById('diasCursos').value = "${alumno.diasCursos}";
            document.getElementById('numeroHoras').value = "${alumno.numeroHoras}";
            document.getElementById('numeroCorrelativoInterno').value = "${alumno.numeroCorrelativoInterno}";
            document.getElementById('cliente').value = "${alumno.cliente}";
            document.getElementById('obra').value = "${alumno.obra}";
            document.getElementById('codigo').value = "${alumno.codigo}";
            document.getElementById('notaAprovacion').value = "${alumno.notaAprovacion}";
            document.getElementById('relator').value = "${alumno.relator}";
            document.getElementById('diploma').value = "${alumno.diploma}";
            document.getElementById('asistencia').value = "${alumno.asistencia}";
        
            // Manejo especial para el campo 'estado'
            var estadoValue = "${alumno.estado}";
        
            if (estadoValue === "revisionManual") {
                estadoValue = "auto"; // Cambiar a 'auto' si es 'revisionManual'
            }
        
            document.getElementById('estado').value = estadoValue;
        
            // Continuar rellenando otros campos...
        });
    </script>
    <script>
        function showAlert(message) {
            if (message && message.trim() !== "") { 
                alert(message);
            }
        }
    </script>
    <c:if test="${not empty error}">
        <script>
            showAlert("${error}");
        </script>
    </c:if>
    <footer class="text-center p-3 bg-light" style="height: 15vh;">
        <p>Contacto: [Dirección, Teléfono, Correo]</p>
        <div>
            <a href="#">Facebook</a> | <a href="#">Twitter</a> | <a href="#">LinkedIn</a>
        </div>
    </footer>
</body>
</html>