<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home</title>
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <link rel="stylesheet" href="/css/styleCustom.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css">
    <link rel="icon" href="/images/Logobgremove.png" type="image/x-icon">
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
        <a href="/home" class="logo"><img src="/images/Logobgremove.png" alt="[LOGO]"></a>
        <nav class="d-flex justify-content-center align-items-center flex-nowrap">
            <a href="/home" class="btn btn-primary mx-2">Inicio</a>
            <a href="/documentacion" target="_blank" class="btn btn-primary mx-2">Documentación</a>
            <div class="d-flex flex-column justify-content-center align-items-center">
                <i class="fa-solid fa-user"></i>
                <p class="p-0 m-0" style="font-size: normal;">${admin.nombre}</p>
            </div>
        </nav>
    </header>
    <main class="d-flex flex-column align-items-center justify-content-center" style="height: 90vh;">
        <h1 class="text-center">Bienvenido a E-VOLUTION</h1>
        <a href="/dataBaseAlumno" class="btn btn-primary btn-custom">Base de datos alumno</a>
        <a href="/dataBaseAlumno/addAlumnoBase" class="btn btn-primary btn-custom">Agregar alumno base de datos</a>
        <a href="/dataBasePlantilla" class="btn btn-secondary btn-custom">Plantillas de diplomas</a>
        <a href="/dataBaseAdmin" class="btn btn-secondary btn-custom">Administradores</a>
        <a href="/documentacion" target="_blank" class="btn btn-light btn-custom">Documentación</a>
        <a href="/logout" class="btn btn-danger btn-custom">Cerrar sesión</a>
    </main>
    <footer class="text-center p-3 bg-light d-flex justify-content-center align-items-center" style="height: 15vh;">
        <div>
            <div><i class="fa-solid fa-phone"></i><a href="tel:+56 41 3830944">+56 41 3830944</a></div>
            <div><i class="fa-solid fa-location-dot"></i><a href="https://www.google.com/maps/place/Consultores+Empresariales+E-Volution+Limitada/@-36.8252678,-73.050754,19z/data=!3m1!4b1!4m6!3m5!1s0x9669b5d0308198b5:0xbd67409566499fa!8m2!3d-36.8252678!4d-73.0501103!16s%2Fg%2F11fzwngw1q?entry=ttu&g_ep=EgoyMDI1MDEwNi4xIKXMDSoASAFQAw%3D%3D">Freire 728, Oficina 206</a></div>
            <div><i class="fa-solid fa-envelope"></i><a href="mailto:contacto@e-volution.cl">contacto@e-volution.cl</a></div>
        </div>
    </footer>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script></body>
</html>