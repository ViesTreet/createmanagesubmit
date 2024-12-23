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
        <div class="logo"><img src="/images/Logobgremove.png" alt="[LOGO]"></div>
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
                <form action="/agregarAlumno" method="post">
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
                            <label style="font-size: 10px;">Guardar(curso/dias/horas/correlativo/cliente/obra/codigo/relator/estado/plantilla)</label>
                            <input type="checkbox" name="guardar" id="guardar">
                        </div>
                        <input class="btn btn-success" type="submit" value="Añadir">
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
        window.onload = function() {
            if (localStorage.getItem('curso')) {
                document.getElementById('curso').value = localStorage.getItem('curso');
            }
            if (localStorage.getItem('diasCursos')) {
                document.getElementById('diasCursos').value = localStorage.getItem('diasCursos');
            }
            if (localStorage.getItem('numeroHoras')) {
                document.getElementById('numeroHoras').value = localStorage.getItem('numeroHoras');
            }
            if (localStorage.getItem('numeroCorrelativoInterno')) {
                document.getElementById('numeroCorrelativoInterno').value = localStorage.getItem('numeroCorrelativoInterno');
            }
            if (localStorage.getItem('cliente')) {
                document.getElementById('cliente').value = localStorage.getItem('cliente');
            }
            if (localStorage.getItem('obra')) {
                document.getElementById('obra').value = localStorage.getItem('obra');
            }
            if (localStorage.getItem('codigo')) {
                document.getElementById('codigo').value = localStorage.getItem('codigo');
            }
            if (localStorage.getItem('relator')) {
                document.getElementById('relator').value = localStorage.getItem('relator');
            }
            if (localStorage.getItem('estado')) {
                document.getElementById('estado').value = localStorage.getItem('estado');
            }
            if (localStorage.getItem('diploma')) {
                document.getElementById('diploma').value = localStorage.getItem('diploma');
            }
            if (localStorage.getItem('plantilla')) {
                document.getElementById('plantilla').value = localStorage.getItem('plantilla');
            }
        };
    </script>
    
    <script>
        function guardarDatos() {
            var guardar = document.getElementById('guardar').checked;
            if (guardar) {
                localStorage.setItem('curso', document.getElementById('curso').value);
                localStorage.setItem('diasCursos', document.getElementById('diasCursos').value);
                localStorage.setItem('numeroHoras', document.getElementById('numeroHoras').value);
                localStorage.setItem('numeroCorrelativoInterno', document.getElementById('numeroCorrelativoInterno').value);
                localStorage.setItem('cliente', document.getElementById('cliente').value);
                localStorage.setItem('obra', document.getElementById('obra').value);
                localStorage.setItem('codigo', document.getElementById('codigo').value);
                localStorage.setItem('relator', document.getElementById('relator').value);
                localStorage.setItem('estado', document.getElementById('estado').value);
                localStorage.setItem('diploma', document.getElementById('diploma').value);
                localStorage.setItem('plantilla', document.getElementById('plantilla').value);
            } else {
                localStorage.removeItem('curso');
                localStorage.removeItem('diasCursos');
                localStorage.removeItem('numeroHoras');
                localStorage.removeItem('numeroCorrelativoInterno');
                localStorage.removeItem('cliente');
                localStorage.removeItem('obra');
                localStorage.removeItem('codigo');
                localStorage.removeItem('relator');
                localStorage.removeItem('asistencia');
                localStorage.removeItem('estado');
                localStorage.removeItem('diploma');
                localStorage.removeItem('plantilla');
            }
        }
    </script>
    
</body>
</html>