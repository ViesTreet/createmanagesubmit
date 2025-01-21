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
    <link rel="icon" href="/images/Logobgremove.png" type="image/x-icon">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css">
    <style>

    </style>
</head>
<body>
    <header class="d-flex align-items-center justify-content-between p-3 bg-light" style="height: 10vh;">
        <a href="/home" class="logo"><img src="/images/Logobgremove.png" alt="[LOGO]"></a>
        <nav class="d-flex justify-content-center align-items-center flex-nowrap">
            <a href="/dataBaseAlumno/alumno/${alumno.id}" class="btn btn-primary mx-2">Regresar</a>
            <a href="/home" class="btn btn-primary mx-2">Inicio</a>
            <a href="/documentacion" target="_blank" class="btn btn-primary mx-2">Documentación</a>
            <div class="d-flex flex-column justify-content-center align-items-center">
                <i class="fa-solid fa-user"></i>
                <p class="p-0 m-0" style="font-size: normal;">${admin.nombre}</p>
            </div>
        </nav>
    </header>
    <main class="d-flex flex-column align-items-center justify-content-center" style="height: 90vh;">
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
                                <input type="text" name="nombreCurso" id="nombreCurso" class="form-control" placeholder="curso1,curso2,etc...">
                            </div>
                            <div>
                                <label for="diasCursos">Días del curso</label>
                                <input type="text" name="diasCursos" id="diasCursos" class="form-control" placeholder="fecha">
                            </div>
                            <div>
                                <label for="numeroHoras">Duración del curso</label>
                                <input type="text" name="numeroHoras" id="numeroHoras" class="form-control" placeholder="Duración">
                            </div>
                            <div>
                                <label for="modalidad">Modalidad</label>
                                <input type="text" name="modalidad" id="modalidad" class="form-control" placeholder="virtual">
                            </div>
                        </div>
                        <div style="max-width: 33%;">
                            <div>
                                <label for="cliente">Cliente</label>
                                <input type="text" name="cliente" id="cliente" class="form-control" placeholder="Cliente">
                            </div>
                            <div>
                                <label for="obra">Obra</label>
                                <input type="text" name="obra" id="obra" class="form-control" placeholder="Obra">
                            </div>
                            <div>
                                <label for="codigo">Codigo</label>
                                <input type="text" name="codigo" id="codigo" class="form-control" placeholder="Codigo">
                            </div>
                            <div>
                                <label for="notaAprobacion">Nota aprobacion</label>
                                <input type="text" name="notaAprobacion" id="notaAprobacion" class="form-control" placeholder="Ej: 7.0">
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
            document.getElementById('numeroHoras').value = "${alumno.duracion}";
            document.getElementById('cliente').value = "${alumno.cliente}";
            document.getElementById('obra').value = "${alumno.obra}";
            document.getElementById('codigo').value = "${alumno.codigo}";
            document.getElementById('correo').value = "${alumno.correo}";
            document.getElementById('notaAprobacion').value = "${alumno.notaAprobacion}";
            document.getElementById('relator').value = "${alumno.relator}";
            document.getElementById('diploma').value = "${alumno.diploma}";
            document.getElementById('asistencia').value = "${alumno.asistencia}";
            document.getElementById('rut').value = "${alumno.rut}";
            document.getElementById('modalidad').value = "${alumno.modalidad}";
            document.getElementById('plantilla').value = "${alumno.plantilla.id}";
        
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
        function formatearRut() {
            var input = document.getElementById('rut');
            var rut = input.value.replace(/\./g, '').replace('-', '');
        
            if (rut.length > 3) {
                var cuerpo = rut.slice(0, -1);
                var dv = rut.slice(-1);
                input.value = cuerpo + '-' + dv;
            } else {
                input.value = rut;
            }
        }
        document.getElementById('rut').addEventListener('input', formatearRut);
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
    <footer class="text-center p-3 bg-light d-flex justify-content-center align-items-center" style="height: 15vh;">
        <div>
            <div><i class="fa-solid fa-phone"></i><a href="tel:+56 41 3830944">+56 41 3830944</a></div>
            <div><i class="fa-solid fa-location-dot"></i><a href="https://www.google.com/maps/place/Consultores+Empresariales+E-Volution+Limitada/@-36.8252678,-73.050754,19z/data=!3m1!4b1!4m6!3m5!1s0x9669b5d0308198b5:0xbd67409566499fa!8m2!3d-36.8252678!4d-73.0501103!16s%2Fg%2F11fzwngw1q?entry=ttu&g_ep=EgoyMDI1MDEwNi4xIKXMDSoASAFQAw%3D%3D">Freire 728, Oficina 206</a></div>
            <div><i class="fa-solid fa-envelope"></i><a href="mailto:contacto@e-volution.cl">contacto@e-volution.cl</a></div>
        </div>
    </footer>
</body>
</html>