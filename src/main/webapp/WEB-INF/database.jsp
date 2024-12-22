<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Lista de Alumnos</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- Importar CSS de Bootstrap -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <!-- Importar jQuery -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>  
    <!-- Importar JS de Bootstrap -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>

    <style>
        th{
            font-size: 13px;
        }

        td{
            font-size: 11px;
        }

        @media (min-width: 1024px){
            #contenderBase{
                max-width: 95vw;
            }
        }
        @media (max-width: 768px) {
            #contenderBase{
                max-width: 100vw !important;
                padding-left: 0px !important;
                padding-right: 0px !important;
            }
            #contenedorTabla {
                width: 100vw;
                overflow-y: auto;
                overflow-x: auto;
            }

            th{
                font-size: 15px;
            }

            td{
                font-size: 12px;
            }

            #tablaAlumnos{
                width: 250vw;
            }
        }

    </style>
</head>
<body>
    <header class="d-flex align-items-center justify-content-between p-3 bg-light" style="height: 10vh; z-index: 1;" >
        <a href="/home" class="logo"><img src="/images/Logobgremove.png" alt="[LOGO]"></a>
        <nav>
            <a href="/home" class="mx-2">Inicio</a>
            <a href="#" class="mx-2">Funciones</a>
            <a href="#" class="mx-2">Contacto</a>
        </nav>
    </header>
    <div id="contenderBase" class="container pt-2 pb-2" style="height: 90vh;">
        <h2 class="text-center">Base de datos Alumnos</h2>
        <div id="contenedorTabla" style="overflow-y: auto; max-height: 70vh;">
            <table class="table table-hover table-sm table-bordered mb-5" style="table-layout: fixed; height: 100%;" id="tablaAlumnos">
                <thead class="thead-dark">
                    <tr>
                        <th>Nombre Asistente</th>
                        <th>Nombre Curso</th>
                        <th>Cliente</th>
                        <th>Obra</th>
                        <th>Relator</th>
                        <th>Estado</th>
                        <th>Rut</th>
                        <th>Correo</th>
                        <th>Plantilla</th>
                    </tr>
                </thead>
                <tbody>
                    <!-- Las filas se agregarán dinámicamente aquí -->
                </tbody>
            </table>
        </div>
    </div>

    <script>
        $(document).ready(function(){
            function cargarDatos(){
                $.ajax({
                    url: "/api/datos",
                    method: "GET",
                    success: function(data){
                        var tbody = $("#tablaAlumnos tbody");
                        tbody.empty(); // Limpiar la tabla antes de agregar nuevos datos

                        $.each(data, function(i, alumno){
                            var fila = "<tr>"+
                                "<td>"+ (alumno.nombreAsistente != null ? alumno.nombreAsistente : "") +"</td>"+
                                "<td>"+ (alumno.nombreCurso != null ? alumno.nombreCurso : "") +"</td>"+
                                "<td>"+ (alumno.cliente != null ? alumno.cliente : "") +"</td>"+
                                "<td>"+ (alumno.obra != null ? alumno.obra : "") +"</td>"+
                                "<td>"+ (alumno.relator != null ? alumno.relator : "") +"</td>"+
                                "<td>"+ (alumno.estado != null ? alumno.estado : "") +"</td>"+
                                "<td>"+ (alumno.rut != null ? alumno.rut : "") +"</td>"+
                                "<td>"+ (alumno.correo != null ? alumno.correo : "") +"</td>"+
                                "<td>"+ (alumno.plantilla != null ? alumno.plantilla : "") +"</td>"+
                                "</tr>";
                            tbody.append(fila);
                        });
                    },
                    error: function(error){
                        console.log("Error al obtener los datos", error);
                    }
                });
            }

            // Cargar datos inicialmente
            cargarDatos();

            // Actualizar la tabla cada 30 segundos (30000 milisegundos)
            setInterval(cargarDatos, 30000);
        });
    </script>
    <footer class="text-center p-3 bg-light" style="height: 15vh; z-index: 1;">
        <p>Contacto: [Dirección, Teléfono, Correo]</p>
        <div>
            <a href="#">Facebook</a> | <a href="#">Twitter</a> | <a href="#">LinkedIn</a>
        </div>
    </footer>
</body>
</html>