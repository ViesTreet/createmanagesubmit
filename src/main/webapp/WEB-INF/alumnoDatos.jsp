<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Datos Alumno</title>
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <link rel="stylesheet" href="/css/styleCustom.css">
    <link rel="icon" href="/images/Logobgremove.png" type="image/x-icon">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css">
</head>
<body>
    <header class="d-flex align-items-center justify-content-between p-3 bg-light" style="height: 10vh;">
        <a href="/home" class="logo"><img src="/images/Logobgremove.png" alt="[LOGO]"></a>
        <nav>
            <a href="/dataBaseAlumno" class="btn btn-primary mx-2">Regresar</a>
            <a href="/home" class="btn btn-primary mx-2">Inicio</a>
            <a href="/documentacion/alumnoDatos" class="btn btn-primary mx-2">Documentación</a>
        </nav>
    </header>
    <main class="d-flex flex-column align-items-center justify-content-center" style="height: 90vh;">
        <div class="card" style="width: 70vw;">
            <div class="card-body">
                <div class="card-title text-center"><h3>Datos del alumno ${alumno.nombreAsistente}</h3></div>
            </div>
            <div class="d-flex justify-content-between">
                <div style="max-width: 50%;">
                    <p><strong>Nombre del alumno:</strong> ${alumno.nombreAsistente}</p>
                    <p><strong>Nombre del curso:</strong> ${alumno.nombreCurso}</p>
                    <p><strong>Días del curso:</strong> ${alumno.diasCursos}</p>
                    <p><strong>Duración del curso</strong> ${alumno.duracion}</p>
                    <p><strong>Número correlativo interno:</strong> ${alumno.numeroCorrelativoInterno}</p>
                    <p><strong>Cliente:</strong> ${alumno.cliente}</p>
                    <p><strong>Obra:</strong> ${alumno.obra}</p>
                    <p><strong>Código:</strong> ${alumno.codigo}</p>
                    <p><strong>Modalidad:</strong> ${alumno.modalidad}</p>
                    <p><strong>Nota de aprobación:</strong> ${alumno.notaAprobacion}</p>
                </div>
                <div style="max-width: 50%;">
                    <p><strong>Relator:</strong> ${alumno.relator}</p>
                    <p><strong>Asistencia:</strong> ${alumno.asistencia}</p>
                    <p><strong>Estado:</strong> ${alumno.estado}</p>
                    <p><strong>Diploma:</strong> ${alumno.diploma}</p>
                    <p><strong>RUT:</strong> ${alumno.rut}</p>
                    <p><strong>Correo electrónico:</strong> ${alumno.correo}</p>
                    <p><strong>Plantilla:</strong> ${alumno.plantilla != null ? alumno.plantilla.nombreCertificado : 'N/A'}</p>
                    <p><strong>Plantilla(lugar y fecha):</strong> ${alumno.plantilla != null ? alumno.plantilla.lugarYFecha : 'N/A'}</p>
                    <p><strong>Fecha de creación:</strong> ${alumno.createdAt}</p>
                    <p><strong>Fecha de actualización:</strong> ${alumno.updatedAt}</p>
                </div>
            </div>
            <div class="d-flex justify-content-between">
                <a class="btn btn-primary" href="/dataBaseAlumno/alumno/${alumno.id}/editar">Editar</a>
                <a class="btn btn-success" href="/dataBaseAlumno/generateCertificado/${alumno.id}">Enviar</a>
                <a class="btn btn-danger" href="/dataBaseAlumno/alumno/${alumno.id}/borrar">Borrar</a>
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
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
</body>
</html>