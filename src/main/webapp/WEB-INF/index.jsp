<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %> 
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inicio</title>
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <link rel="stylesheet" href="/css/styleCustom.css">
</head>
<body>
    <div class="container d-flex justify-content-center align-items-center" style="height: 100vh;">
        <div class="card p-4" style="width: 100%; max-width: 400px;">
            <div class="card-body">
                <h5 class="card-title text-center">Iniciar Sesión</h5>
                <form action="/login" method="post">
                    <div class="mb-3">
                        <label for="correo" class="form-label">Correo Electrónico</label>
                        <input type="email" class="form-control" id="correo" name="correo" placeholder="nombre@ejemplo.com">
                    </div>
                    <div class="mb-3">
                        <label for="contrasena" class="form-label">Contraseña</label>
                        <input type="password" class="form-control" name="contrasena" id="contrasena" placeholder="Contraseña">
                    </div>
                    <input type="submit" class="btn btn-primary w-100" value="Ingresar"> 
                </form>
            </div>
        </div>
    </div>
</body>
    <script src="assets/bootstrap/js/bootstrap.bundle.min.js"></script>
</html>