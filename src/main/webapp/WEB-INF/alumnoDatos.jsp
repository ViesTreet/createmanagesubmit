<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Datos Alumno</title>
    <link rel="stylesheet" href="/css/bootstrap.min.css">
</head>
<body>
    <header class="d-flex align-items-center justify-content-between p-3 bg-light" style="height: 10vh;">
        <div class="logo"><img src="/images/Logobgremove.png" alt="[LOGO]"></div>
        <nav>
            <a href="#" class="mx-2">Inicio</a>
            <a href="#" class="mx-2">Funciones</a>
            <a href="#" class="mx-2">Contacto</a>
        </nav>
    </header>
    <main class="d-flex flex-column align-items-center justify-content-center" style="height: 90vh;">
        <div class="d-flex justify-content-end" style="width: 70vw;"><a class="btn btn-warning" href="/dataBaseAlumno">Regresar</a></div>
        <div class="card" style="width: 70vw;">
            <div class="card-body">
                <div class="card-title text-center"><h3>Datos del alumno ${alumno.nombreAsistente}</h3></div>
            </div>
            <div class="d-flex justify-content-between">
                <div style="max-width: 50%;">
                    <p><strong>Nombre del alumno:</strong> ${alumno.nombreAsistente}</p>
                    <p><strong>Nombre del curso:</strong> ${alumno.nombreCurso}</p>
                    <p><strong>Días del curso:</strong> ${alumno.diasCursos}</p>
                    <p><strong>Número de horas:</strong> ${alumno.numeroHoras}</p>
                    <p><strong>Número correlativo interno:</strong> ${alumno.numeroCorrelativoInterno}</p>
                    <p><strong>Cliente:</strong> ${alumno.cliente}</p>
                    <p><strong>Obra:</strong> ${alumno.obra}</p>
                    <p><strong>Código:</strong> ${alumno.codigo}</p>
                </div>
                <div style="max-width: 50%;">
                    <p><strong>Nota de aprobación:</strong> ${alumno.notaAprovacion}</p>
                    <p><strong>Relator:</strong> ${alumno.relator}</p>
                    <p><strong>Asistencia:</strong> ${alumno.asistencia}</p>
                    <p><strong>Estado:</strong> ${alumno.estado}</p>
                    <p><strong>Diploma:</strong> ${alumno.diploma}</p>
                    <p><strong>RUT:</strong> ${alumno.rut}</p>
                    <p><strong>Correo electrónico:</strong> ${alumno.correo}</p>
                    <p><strong>Plantilla:</strong> ${alumno.plantilla != null ? alumno.plantilla.nombreCertificado : 'N/A'}</p>
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
    <footer class="text-center p-3 bg-light" style="height: 15vh;">
        <p>Contacto: [Dirección, Teléfono, Correo]</p>
        <div>
            <a href="#">Facebook</a> | <a href="#">Twitter</a> | <a href="#">LinkedIn</a>
        </div>
    </footer>
    <script src="assets/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>