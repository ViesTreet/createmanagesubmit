<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %> 
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inicio</title>
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <style>

    </style>
</head>
<body>
    <header class="d-flex align-items-center justify-content-between p-3 bg-light" style="height: 10vh;">
        <div class="logo"><a href="/home"><img src="/images/Logobgremove.png" alt="[LOGO]"></div></a>
        <nav>
            <a href="#" class="mx-2">Inicio</a>
            <a href="#" class="mx-2">Funciones</a>
            <a href="#" class="mx-2">Contacto</a>
        </nav>
    </header>
    <main class="d-flex flex-column align-items-center justify-content-center" style="height: 90vh;">
        <div class="card" style="width: 70vw;">
            <div class="card-body" style="width: 100%;">
                <h4 class="card-title text-center">Agregar nuevo alumno</h4>
                <form action="/dataBaseAlumno/agregarAlumno" method="post">
                    <div class="d-flex justify-content-between">
                        <div style="max-width: 33%;">
                            <div>
                                <label for="nombreAsistente">Nombre Asistente</label>
                                <input type="text" name="nombreAsistente" id="nombreAsistente" class="form-control" placeholder="Nombres Apellidos Asistente">
                            </div>
                            <div>
                                <label for="curso">Cursos</label>
                                <input type="text" name="curso" id="curso" placeholder="curso1,curso2,etc...">
                            </div>
                            <div>
                                <label for="diasCursos">Dias del curso</label>
                                <input type="text" name="diasCursos" id="diasCursos" class="form-control" placeholder="dd/m/aa-dd/m/aa">
                            </div>
                            <div>
                                <label for="numeroHoras">Numero de horas</label>
                                <input type="text" name="numeroHoras" id="numeroHoras" class="form-control" placeholder="N° horas">
                            </div>
                            <div>
                                <label for="numeroCorrelativoInterno">Numero correlativo interno</label>
                                <input type="text" name="numeroCorrelativoInterno" id="numeroCorrelativoInterno" class="form-control" placeholder="N° Correlativo interno">
                            </div>
                            <div>
                                <label for="cliente">Cliente</label>
                                <input type="text" name="cliente" id="cliente" class="form-control" placeholder="Cliente">
                            </div>
                        </div>
                        <div style="max-width: 33%;">
                            <div>
                                <label for="obra">Obra</label>
                                <input type="text" name="obra" id="obra" class="form-control" placeholder="Obra">
                            </div>
                            <div>
                                <label for="codigo">Codigo</label>
                                <input type="text" name="codigo" id="codigo" class="form-control" placeholder="Codigo">
                            </div>
                            <div>
                                <label for="notaAprovacion">Nota aprovacion</label>
                                <input type="text" name="notaAprovacion" id="notaAprovacion" class="form-control" placeholder="Ej: 7.0">
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
                                    <option value="enviar">Enviar</option>
                                    <option value="noEnviar">No enviar</option>
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
                    <div class="d-flex align-items-center justify-content-between pt-1">
                        <div>
                            <label style="font-size: 10px;">Guardar datos comunes</label>
                            <input type="checkbox" name="guardar" id="guardar" onchange="guardarDatos()">
                        </div>
                        <input class="btn btn-success" type="submit" value="Añadir">
                        <a href="/dataBaseAlumno/addAlumnoBase/excel" class="btn btn-primary">Agregar usando excel</a>
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
    <script>
        function setCookie(name, value, days) {
            var expires = "";
            if (days) {
                var date = new Date();
                date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
                expires = "; expires=" + date.toUTCString();
            }
            document.cookie = name + "=" + (value || "") + expires + "; path=/";  // No uses "Secure" aquí

        }

        function getCookie(name) {
            var nameEQ = name + "=";
            var ca = document.cookie.split(';');
            for (var i = 0; i < ca.length; i++) {
                var c = ca[i];
                while (c.charAt(0) == ' ') c = c.substring(1, c.length);
                if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
            }
            return null;
        }

        function eraseCookie(name) {   
            document.cookie = name + '=; Max-Age=-99999999;';  
        }

        window.onload = function() {
            var campos = ['curso', 'diasCursos', 'numeroHoras', 'numeroCorrelativoInterno', 'cliente', 'obra', 'codigo', 'asistencia', 'estado', 'diploma', 'plantilla', 'relator'];
        
            campos.forEach(function(campo) {
                var valor = getCookie(campo);
                if (valor) {
                    var elemento = document.getElementById(campo);
                    if (elemento) {
                        elemento.value = valor;
                    }
                }
            });
        
            if (getCookie('datosGuardados') === 'true') {
                document.getElementById('guardar').checked = true;
            }
        };

        function guardarDatos() {
            var guardar = document.getElementById('guardar').checked;
            var campos = ['curso', 'diasCursos', 'numeroHoras', 'numeroCorrelativoInterno', 'cliente', 'obra', 'codigo', 'asistencia', 'estado', 'diploma', 'plantilla', 'relator'];
        
            if (guardar) {
                campos.forEach(function(campo) {
                    var elemento = document.getElementById(campo);
                    if (elemento) {
                        setCookie(campo, elemento.value, 7); // Guardar por 7 días
                    }
                });
                setCookie('datosGuardados', true, 7);
            } else {
                campos.forEach(function(campo) {
                    eraseCookie(campo);
                });
                eraseCookie('datosGuardados');
            }
        }
    </script>
    
</body>
</html>