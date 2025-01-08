<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home</title>
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <link rel="stylesheet" href="/css/styleCustom.css">
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
            <a href="/home" class="btn btn-primary mx-2">Inicio</a>
            <a href="/documentacion/home" class="btn btn-primary mx-2">Documentación</a>
        </nav>
    </header>
    <main class="d-flex flex-column align-items-center justify-content-center" style="height: 90vh;">
        <h1 class="text-center">Bienvenido a E-VOLUTION</h1>
        <a href="/dataBaseAlumno" class="btn btn-primary btn-custom">Base de datos alumno</a>
        <a href="/dataBaseAlumno/addAlumnoBase" class="btn btn-primary btn-custom">Agregar alumno base de datos</a>
        <a href="/dataBasePlantilla" class="btn btn-secondary btn-custom">Base de datos plantilla</a>
        <a href="/dataBaseAdmin" class="btn btn-secondary btn-custom">Administradores</a>
        <a href="/documentacion" class="btn btn-light btn-custom">Documentación</a>
        <a href="/logout" class="btn btn-danger btn-custom">Cerrar sesión</a>
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