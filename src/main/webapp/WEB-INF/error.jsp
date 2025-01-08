<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error</title>
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <link rel="stylesheet" href="/css/styleCustom.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <style>
        .btn-custom {
            width: 100%;
            max-width: 300px;
            margin: 10px 0;
        }
        .error-icon {
            font-size: 5rem;
            color: #dc3545;
        }
    </style>
</head>
<body>
    <header class="d-flex align-items-center justify-content-between p-3 bg-light" style="height: 10vh;">
        <div class="logo"><img src="/images/Logobgremove.png" alt="[LOGO]"></div>
        <nav>
        </nav>
    </header>
    <main class="d-flex flex-column align-items-center justify-content-center" style="height: 90vh;">
        <i class="fas fa-exclamation-triangle error-icon"></i>
        <h1 class="text-center">Oops! Algo salió mal.</h1>
        <p class="text-center">Lo sentimos, pero la página que buscas no existe o ha ocurrido un error.</p>
        <a href="/" class="btn btn-primary btn-custom">Volver al Inicio</a>
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
