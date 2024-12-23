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
    <style>

    </style>
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
    <main class="container d-flex flex-column align-items-center justify-content-center" style="height: 100vh;">
        <div class="card" style="width: 30vw;">
            <div class="card-body">
                <h4 class="card-title text-center">Agregar nuevo excel</h4>
                <form action="/api/uploadAlumnoExcel" method="POST" enctype="multipart/form-data">
                    <div class="pb-2 pt-2">
                        <label for="file">Selecciona un archivo Excel:</label>
                        <input class="form-control" type="file" id="file" name="file" accept=".xls,.xlsx" required>
                    </div>
                    <div class="pb-2 pt-2">
                        <label for="procesar">Enviar y Generar Certificados</label>
                        <input class="form-check-input" type="checkbox" id="procesar" name="procesar" value="true">
                        
                    </div>
                    <div class="pb-2 pt-2">
                        <select name="plantillaNombre" id="plantillaNombre">
                            <option value="excel">Usar plantillas del excel</option>
                            <c:forEach items="${plantillas}" var="plantilla">
                                    <option value="${plantilla.nombreCertificado}">${plantilla.nombreCertificado}</option>
                                </c:forEach>
                        </select>
                    </div>
                    <div class="d-flex align-items-end justify-content-between pb-2 pt-2">
                        <input class="btn btn-success" type="submit" value="Añadir">
                        <a href="addAlumnoBase" class="btn btn-danger">Volver</a>
                    </div>
                    
                </form>
            </div>
        </div>
    </main>
    <footer class="text-center p-3 bg-light" style="height: 15vh;">
        <p>Contacto: [Dirección, Teléfono, Correo]</p>
        <div>
            <a href="#">Facebook</a> | <a href="#">Twitter</a> | <a href="#">LinkedIn</a>
        </div>
    </footer>   
</body>
</html>