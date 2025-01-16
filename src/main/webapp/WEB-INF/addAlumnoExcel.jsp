<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %> 
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Agregar a la base de datos con excel</title>
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
            <a href="/dataBaseAlumno/addAlumnoBase" class="btn btn-primary mx-2">Regresar</a>
            <a href="/home" class="btn btn-primary mx-2">Inicio</a>
            <a href="/documentacion/addAlumnoExcel" class="btn btn-primary mx-2">Documentación</a>
            <div class="d-flex flex-column justify-content-center align-items-center">
                <i class="fa-solid fa-user"></i>
                <p class="p-0 m-0" style="font-size: normal;">${admin.nombre}</p>
            </div>
        </nav>
    </header>
    <main class="container d-flex flex-column align-items-center justify-content-center" style="height: 100vh;">
        <div class="card" style="width: 30vw;">
            <div class="card-body">
                <h4 class="card-title text-center">Agregar nuevo excel</h4>
                <form action="/dataBaseAlumno/uploadAlumnoExcel" method="POST" enctype="multipart/form-data">
                    <div class="pb-2 pt-2">
                        <label for="file">Selecciona un archivo Excel:</label>
                        <input class="form-control" type="file" id="file" name="file" accept=".xls,.xlsx" required>
                    </div>
                    <div class="pb-2 pt-2">
                        <label for="plantillaNombre">Elegir la plantilla a usar</label>
                        <select name="plantillaNombre" id="plantillaNombre" class="form-select">
                            <option value="excel">Usar plantillas del excel</option>
                            <c:forEach items="${plantillas}" var="plantilla">
                                <option value="${plantilla.nombreCertificado}">${plantilla.nombreCertificado}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="pb-2 pt-2">
                        <label for="estadoExcel">Elegir el estado de los alumnos(aprobado/no aprobado):</label>
                        <select name="estadoExcel" id="estadoExcel" class="form-select">
                            <option value="Eexcel">Usar estado del excel</option>
                            <option value="Eauto">Automatico</option>
                            <option value="aprobado">Aprobado</option>
                            <option value="noAprobado">No aprobado</option>
                        </select>
                    </div>
                    <div class="pb-2 pt-2">
                        <label for="estadoDiplomaExcel">Acciones a realizar(enviar/no enviar):</label>
                        <select name="estadoDiplomaExcel" id="estadoDiplomaExcel" class="form-select">
                            <option value="diploExcel">Realizar segun excel</option>
                            <option value="enviarApro">Enviar(APROBADOS)</option>
                            <option value="enviarTodos">Enviar(TODOS)</option>
                            <option value="noEnviar">No enviar</option>
                        </select>
                    </div>
                    <div class="pb-2 pt-2">
                        <label for="rutificador">Rutificador</label>
                        <select name="rutificador" id="rutificador" class="form-select">
                            <option value="rutiExcel">Realizar segun excel</option>
                            <option value="rutiTodos">Rutificar Todos</option>
                            <option value="rutiNinguno">No rutificar</option>
                        </select>
                    </div>
                    <div class="d-flex align-items-end justify-content-between pb-2 pt-2">
                        <input class="btn btn-success" type="submit" value="Añadir">
                        <a href="/dataBaseAlumno/addAlumnoBase" class="btn btn-danger">Volver</a>
                    </div>
                    
                </form>
            </div>
        </div>
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
    </main>
    <footer class="text-center p-3 bg-light d-flex justify-content-center align-items-center" style="height: 15vh;">
        <div>
            <div><i class="fa-solid fa-phone"></i><a href="tel:+56 41 3830944">+56 41 3830944</a></div>
            <div><i class="fa-solid fa-location-dot"></i><a href="https://www.google.com/maps/place/Consultores+Empresariales+E-Volution+Limitada/@-36.8252678,-73.050754,19z/data=!3m1!4b1!4m6!3m5!1s0x9669b5d0308198b5:0xbd67409566499fa!8m2!3d-36.8252678!4d-73.0501103!16s%2Fg%2F11fzwngw1q?entry=ttu&g_ep=EgoyMDI1MDEwNi4xIKXMDSoASAFQAw%3D%3D">Freire 728, Oficina 206</a></div>
            <div><i class="fa-solid fa-envelope"></i><a href="mailto:contacto@e-volution.cl">contacto@e-volution.cl</a></div>
        </div>
    </footer>
</body>
</html>