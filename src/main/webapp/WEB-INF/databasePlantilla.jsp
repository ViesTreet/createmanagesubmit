<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Plantillas de diplomas</title>
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

        #alumnosNoEnviados {
            height: 50vh; /* Define una altura fija */
            overflow-y: auto; /* Permite el desplazamiento vertical cuando el contenido es grande */
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

            #tablaPlantilla{
                width: 250vw;
            }
        }

    </style>
</head>
<body>
    <header class="d-flex align-items-center justify-content-between p-3 bg-light" style="height: 10vh; z-index: 1;" >
        <a href="/home" class="logo"><img src="/images/Logobgremove.png" alt="[LOGO]"></a>
        <nav>
            <a href="/home" class="btn btn-primary mx-2">Regresar</a>
            <a href="/home" class="btn btn-primary mx-2">Inicio</a>
            <a href="/documentacion/databasePlantilla" class="btn btn-primary mx-2">Documentación</a>
        </nav>
    </header>
    <div id="contenderBase" class="container pt-2 pb-2" style="height: 90vh;">
        <h2 class="text-center">Plantillas de diplomas</h2>
        <div class="d-flex align-items-center justify-content-between pb-1" style="max-width: 95vw;">
            <div>
                <form id="plantillaBuscador" class="col-12">
                    <input id="busquedaPlantilla" type="search" onkeydown="submitOnEnter(event, 'buscarLink')" class ="col-7" placeholder="Buscar por nombre" name="busquedaPlantilla"/>
                    <a id="buscarLink" href="#" class="btn btn-outline-primary">Buscar</a>
                </form>
            </div>
            <div>
                <button class="btn btn-success" onclick="openForm()">Agregar nueva plantilla</button>
            </div>
        </div>
        <div id="contenedorTabla" style="overflow-y: auto; max-height: 70vh; max-width: 95vw;">
            <table class="table table-hover table-bordered mb-5" style="table-layout: fixed; height: 100%;" id="tablaPlantilla">
                <thead class="thead-dark">
                    <tr>
                        <th>Nombre Plantilla</th>
                        <th>Descripción</th>
                        <th>Asistencia mínima</th>
                        <th>Nota mínima</th>
                        <th>Lugar y fecha</th>
                        <th style="width: 25vw;">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <!-- Las filas se agregarán dinámicamente aquí -->
                </tbody>
            </table>
        </div>
    </div>
    <script>
        $(document).ready(function () {
            function cargarDatos() {
                $.ajax({
                    url: "/api/datosPlantilla",
                    method: "GET",
                    success: function (data) {
                        var tbody = $("#tablaPlantilla tbody");
    
                        tbody.empty(); // Limpiar la tabla antes de agregar nuevos datos
    
                        $.each(data, function (i, plantilla) {
                            // Verificar si el nombreCertificado es "Error en encontrar plantilla"
                            if (plantilla.nombreCertificado === "Error en encontrar plantilla") {
                                return; // Si es así, omitir esta fila
                            }
    
                            // Crear una fila de tabla para todos los alumnos
                            var fila = "<tr>" +
                                "<td>" + (plantilla.nombreCertificado != null ? plantilla.nombreCertificado : "") + "</td>" +
                                "<td>" + (plantilla.descripcion != null ? plantilla.descripcion : "") + "</td>" +
                                "<td>" + (plantilla.asistenciaMin != null ? plantilla.asistenciaMin : "") + "</td>" +
                                "<td>" + (plantilla.notaMin != null ? plantilla.notaMin : "") + "</td>" +
                                "<td>" + (plantilla.lugarYFecha != null ? plantilla.lugarYFecha : "") + "</td>" +
                                "<td class='d-flex justify-content-center flex-wrap' style='width: 25vw;'><a class='btn btn-primary' href='/dataBasePlantilla/plantilla/"+ plantilla.id +"/editar'>Editar</a><a href='/api/dataBasePlantilla/plantilla/"+ plantilla.id +"/descargar' class='btn btn-success'>Descargar</a><a href='/dataBasePlantilla/plantilla/"+ plantilla.id +"/probar' class='btn btn-secondary'>Probar</a><a href='/dataBasePlantilla/plantilla/"+ plantilla.id +"/borrar' class='btn btn-danger'>Borrar</a></td>" +
                                "</tr>";
                            tbody.append(fila);
                        });
                    },
                    error: function (error) {
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
    
    
    </script>
    <script>
        document.getElementById('buscarLink').addEventListener('click', function(event) {
        // Prevenir el comportamiento por defecto del enlace
            event.preventDefault();

            const busqueda = document.querySelector('[name="busquedaPlantilla"]').value;

            // Construye la URL dinámica usando concatenación de strings
            const url = 'dataBasePlantilla/buscarPlantilla?busqueda=' + encodeURIComponent(busqueda);

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
    <div class="overlay" id="overlay">
        <div class="popup col-7">
            <div class="d-flex justify-content-end">
                <button class="btn btn-danger" onclick="closeForm()">Cerrar</button>
            </div>
            <h2 class="text-center pb-2">Crear plantilla</h2>
            <form action="/dataBasePlantilla/nuevaPlantilla" method="post" enctype="multipart/form-data">
                <div class="d-flex justify-content-around pb-2">
                    <div style="max-width: 40%;">
                        <label for="nombreCertificado">Nombre Plantilla</label>
                        <input id="nombreCertificado" name="nombreCertificado" type="text" class="form-control" placeholder="Nombre de la plantilla">
                        <label for="descripcion">Descripcion Plantilla</label>
                        <input id="descripcion" type="text" name="descripcion" class="form-control" placeholder="Descripcion de la plantilla">
                        <label for="lugarYFecha">Lugar y fecha</label>
                        <input type="text" name="lugarYFecha" id="lugarYFecha" class="form-control" placeholder="ej:Santiago,2023">
                    </div>
                    <div style="max-width: 40%;">
                        <label for="asistenciaMin">Asistencia Mínima</label>
                        <input id="asistenciaMin" type="text" name="asistenciaMin" class="form-control" placeholder="ej:60">
                        <label for="notaMin">Nota Mínima</label>
                        <input id="notaMin" type="text" name="notaMin" class="form-control" placeholder="ej:5.5">
                        <input type="checkbox" id="clonarPlantilla" name="clonarPlantilla" value="true" onclick ="toggleInputs('plantilla')">
                        <label for="clonarPlantilla">Clonar Plantilla</label>
                        <!-- Input file para subir la nueva plantilla -->
                        <div id="inputPlantilla">
                            <label for="pathArchivo">Nueva Plantilla(.pptx)</label>
                            <input type="file" name="pathArchivo" id="pathArchivo" class="form-control" accept=".pptx">
                        </div>
                        <div id="selectPlantilla" style="display: none;">
                            <label for="pathArchivoS">Elegir Plantilla</label>
                            <select name="pathArchivoS" id="pathArchivoS" class="form-select">
                                <c:forEach items="${plantillas}" var="plantilla">
                                        <option value="${plantilla.pathArchivo}">${plantilla.nombreCertificado}</option>

                                    </c:forEach>
                            </select>
                        </div>
                    </div>
                </div>
    
                <div class="d-flex justify-content-center">
                    <input type="hidden" name="orden" value="true">
                    <input class="btn btn-success" type="submit" value="Enviar">
                </div>
            </form>
        </div>
    </div>
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
        function toggleInputs(type) {
            if (type === 'plantilla') {
                const checkbox = document.getElementById('clonarPlantilla');
                const inputFile = document.getElementById('inputPlantilla');
                const selectOptions = document.getElementById('selectPlantilla');
            
                if (checkbox.checked) {
                    inputFile.style.display = 'none';
                    selectOptions.style.display = 'block';
                } else {
                    inputFile.style.display = 'block';
                    selectOptions.style.display = 'none';
                }
            }
        }
    </script>
    <script>
        function showAlert(message) {
            if (message && message.trim() !== "") { 
                alert(message);
            }
        }
    </script>
    <c:if test="${not empty error}">
        <script>
            showAlert("${error}");
        </script>
    </c:if>
    <footer class="text-center p-3 bg-light d-flex justify-content-center align-items-center" style="height: 15vh;">
        <div>
            <div><i class="fa-solid fa-phone"></i><a href="tel:+56 41 3830944">+56 41 3830944</a></div>
            <div><i class="fa-solid fa-location-dot"></i><a href="https://www.google.com/maps/place/Consultores+Empresariales+E-Volution+Limitada/@-36.8252678,-73.050754,19z/data=!3m1!4b1!4m6!3m5!1s0x9669b5d0308198b5:0xbd67409566499fa!8m2!3d-36.8252678!4d-73.0501103!16s%2Fg%2F11fzwngw1q?entry=ttu&g_ep=EgoyMDI1MDEwNi4xIKXMDSoASAFQAw%3D%3D">Freire 728, Oficina 206</a></div>
            <div><i class="fa-solid fa-envelope"></i><a href="mailto:contacto@e-volution.cl">contacto@e-volution.cl</a></div>
        </div>
    </footer>
</body>
</html>