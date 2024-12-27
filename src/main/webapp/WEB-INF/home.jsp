<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home</title>
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <style>
        .btn-custom {
            width: 100%;
            max-width: 300px;
            margin: 10px 0;
        }
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
    <main class="d-flex flex-column align-items-center justify-content-center" style="height: 90vh;">
        <h1 class="text-center">Bienvenido a E-VOLUTION</h1>
        <a href="/dataBaseAlumno" class="btn btn-primary btn-custom">Base de datos alumno</a>
        <a href="/addAlumnoBase" class="btn btn-primary btn-custom">Agregar alumno base de datos</a>
        <a href="/dataBasePlantilla" class="btn btn-secondary btn-custom">Base de datos plantilla</a>
        <a href="/dataBaseAdmin" class="btn btn-secondary btn-custom">Administradores</a>
        <button class="btn btn-light btn-custom">Documentación</button>
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