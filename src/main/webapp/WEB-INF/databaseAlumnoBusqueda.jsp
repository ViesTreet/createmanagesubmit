<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Lista de alumnos busqueda</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- Importar CSS de Bootstrap -->
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <link rel="stylesheet" href="/css/styleCustom.css">
    <link rel="icon" href="/images/Logobgremove.png" type="image/x-icon">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css">
    <!-- Importar jQuery -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>  
    <!-- Importar JS de Bootstrap -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>

    <style>
        th{
            font-size: 13px;
        }

        td{
            overflow-x: auto;
            font-size: 11px;
        }

        #alumnoBuscador{
                padding-right: 0px;
                padding-left: 0px;
            }
        
        .overlay {
            display: none; /* Oculto por defecto */
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.7); /* Fondo oscuro */
            z-index: 999; /* Por encima de otros elementos */
        }

        /* Contenedor del formulario emergente */
        .popup {
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background-color: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
            z-index: 1000; /* Por encima del overlay */
            width: 90%;
            max-width: 400px;
        }

        /* Botón cerrar */
        .close-btn {
            background-color: red;
            color: white;
            border: none;
            padding: 5px 10px;
            border-radius: 5px;
            cursor: pointer;
        }

        .close-btn:hover {
            background-color: darkred;
        }

        /* Botón principal */
        .open-btn {
            margin: 20px;
            padding: 10px 20px;
            font-size: 16px;
            cursor: pointer;
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
                max-width: 100vw !important;
                overflow-y: auto;
                overflow-x: auto;
            }

            #alumnoBuscador{
                max-width: 100vw !important;
            }

            th{
                font-size: 15px;
            }

            td{
                overflow-x: auto;
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
            <a href="/dataBaseAlumno" class="btn btn-primary mx-2">Regresar</a>
            <a href="/home" class="btn btn-primary mx-2">Inicio</a>
            <a href="/documentacion/databaseAlumnoBusqueda" class="btn btn-primary mx-2">Documentación</a>
        </nav>
    </header>
    <div id="contenderBase" class="container pt-2 pb-2" style="height: 90vh;">
        <h2 class="text-center">Base de datos Alumnos</h2>
        <div class="d-flex align-items-center justify-content-between pb-1" style="max-width: 95vw;">
            <div>
                <form id="alumnoBuscador" class="d-flex col-12">
                    <select id="filtroBusquedaAlumno" class="form-select" name="filtroBusquedaAlumno">
                        <option value="rut">Rut</option>
                        <option value="nombreAsistente">Nombre Asistente</option>
                        <option value="estado">Estado</option>
                        <option value="diploma">Diploma</option>
                        <option value="nombreCurso">Nombre Curso</option>
                        <option value="cliente">Cliente</option>
                        <option value="obra">Obra</option>
                        <option value="relator">Relator</option>
                    </select>
                    <input id="busquedaAlumno" type="search"  class ="col-5" onkeydown="submitOnEnter(event, 'buscarLink')" placeholder="Buscar" name="busquedaAlumno"/>
                    <a id="buscarLink" href="#" class="btn btn-outline-primary">Buscar</a>
                </form>
            </div>
            <div>
                <a class="btn btn-warning" href="/dataBaseAlumno">Regresar</a>
                <a class="btn btn-success" href="/dataBaseAlumno/addAlumnoBase">+</a>
                <button id="downloadBtn" href="/dataBaseAlumno/download" class="btn btn-secondary">Descargar base de datos</button>
            </div>
        </div>
        <div id="contenedorTabla" style="overflow-y: auto; max-height: 70vh; max-width: 95vw;">
            <table class="table table-hover table-bordered mb-5" style="table-layout: fixed; height: 100%;" id="tablaAlumnos">
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
                        <th>Diploma</th>
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
            // Obtener los valores de filtro y búsqueda del modelo
            var filtro = '<%= request.getAttribute("filtro") %>';
            var busqueda = '<%= request.getAttribute("busqueda") %>';
        
            function cargarDatos(filtro, busqueda){
                $.ajax({
                    url: "/api/datosAlumno/busquedaAlumno",
                    method: "GET",
                    data: { filtro: filtro, busqueda: busqueda }, // Enviar los parámetros como datos
                    success: function(data){
                        var tbody = $("#tablaAlumnos tbody");
                        tbody.empty(); // Limpiar la tabla antes de agregar nuevos datos
                        $.each(data, function(i, alumno){
                            var correoText = "sin correo"; // Valor por defecto

                            if (alumno.correo) { // Verifica si el correo existe
                                if (alumno.correo === "javito12ulloa@gmail.com") {
                                    correoText = "correo empresa";
                                } else {
                                    correoText = "con correo";
                                }
                            }
                            var fila = "<tr>"+
                                "<td><a href='/dataBaseAlumno/alumno/"+alumno.id+"'>"+ (alumno.nombreAsistente != null ? alumno.nombreAsistente : "") +"</a></td>"+
                                "<td>"+ (alumno.nombreCurso != null ? alumno.nombreCurso : "") +"</td>"+
                                "<td>"+ (alumno.cliente != null ? alumno.cliente : "") +"</td>"+
                                "<td>"+ (alumno.obra != null ? alumno.obra : "") +"</td>"+
                                "<td>"+ (alumno.relator != null ? alumno.relator : "") +"</td>"+
                                "<td>"+ (alumno.estado != null ? alumno.estado : "") +"</td>"+
                                "<td>"+ (alumno.rut != null ? alumno.rut : "") +"</td>"+
                                "<td>" + correoText + "</td>" +
                                "<td>"+ (alumno.plantilla != null ? alumno.plantilla : "") +"</td>"+
                                "<td>"+ (alumno.diploma != null ? alumno.diploma : "") +"</td>"+
                            "</tr>";
                            tbody.append(fila);

                        });
                    },
                    error: function(error){
                        console.log("Error al obtener los datos", error);
                    }
                });
            }
        
            // Llamar a cargarDatos con los valores obtenidos
            cargarDatos(filtro, busqueda);
        
            // Actualizar la tabla cada 30 segundos con los mismos criterios
            setInterval(function() {
                cargarDatos(filtro, busqueda);
            }, 30000);
        });
    </script>
    <script>
        document.getElementById('buscarLink').addEventListener('click', function(event) {
        // Prevenir el comportamiento por defecto del enlace
            event.preventDefault();

            const filtro = document.querySelector('[name="filtroBusquedaAlumno"]').value;
            const busqueda = document.querySelector('[name="busquedaAlumno"]').value;

            // Construye la URL dinámica usando concatenación de strings
            const url = 'buscarAlumno?filtro=' + encodeURIComponent(filtro) + '&busqueda=' + encodeURIComponent(busqueda);

            // Redirige al enlace generado
            window.location.href = url;
        });
    </script>
    <script>
        // Función que detecta la tecla Enter
        function submitOnEnter(event, buttonId) {
            // Verifica si la tecla presionada es Enter (código 13)
            if (event.key === 'Enter') {
                event.preventDefault(); // Evita el comportamiento predeterminado
                document.getElementById(buttonId).click(); // Simula un clic en el botón
            }
        }
    </script>
    <script>
        // Función para mostrar el formulario
        function openForm() {
            document.getElementById('overlay').style.display = 'block';
        }

        // Función para ocultar el formulario
        function closeForm() {
            document.getElementById('overlay').style.display = 'none';
        }
    </script>
    <script>
        document.getElementById("downloadBtn").addEventListener("click", function(event) {
            // Evitar que el enlace siga su ruta y provoque una descarga adicional
            event.preventDefault();
        
            // Prevenir clics adicionales
            const button = event.target;
            button.disabled = true;
        
            // Crear un enlace temporal para la descarga
            const link = document.createElement("a");
            link.href = "/dataBaseAlumno/download"; // Endpoint de descarga
            // No es necesario establecer 'download' ya que el servidor ya envía el nombre del archivo
            // link.setAttribute("download", "alumnos.xlsx"); 
        
            // Asegurar que el enlace no sea visible
            link.style.display = "none";
            document.body.appendChild(link);
        
            // Simular clic en el enlace
            link.click();
        
            // Eliminar el enlace del DOM después de usarlo
            setTimeout(() => {
                document.body.removeChild(link);
                button.disabled = false; // Reactivar el botón
            }, 100);
        });
    </script>
    <footer class="text-center p-3 bg-light d-flex justify-content-center align-items-center" style="height: 15vh;">
        <div>
            <div><i class="fa-solid fa-phone"></i><a href="tel:+56 41 3830944">+56 41 3830944</a></div>
            <div><i class="fa-solid fa-location-dot"></i><a href="https://www.google.com/maps/place/Consultores+Empresariales+E-Volution+Limitada/@-36.8252678,-73.050754,19z/data=!3m1!4b1!4m6!3m5!1s0x9669b5d0308198b5:0xbd67409566499fa!8m2!3d-36.8252678!4d-73.0501103!16s%2Fg%2F11fzwngw1q?entry=ttu&g_ep=EgoyMDI1MDEwNi4xIKXMDSoASAFQAw%3D%3D">Freire 728, Oficina 206</a></div>
            <div><i class="fa-solid fa-envelope"></i><a href="mailto:contacto@e-volution.cl">contacto@e-volution.cl</a></div>
        </div>
    </footer>
</body>
</html>