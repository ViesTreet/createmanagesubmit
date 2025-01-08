<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Lista de Administradores</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- Importar CSS de Bootstrap -->
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <link rel="stylesheet" href="/css/styleCustom.css">
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
            <a href="/home" class="btn btn-primary mx-2">Regresar</a>
            <a href="/home" class="btn btn-primary mx-2">Inicio</a>
            <a href="/documentacion/databaseAdmin" class="btn btn-primary mx-2">Documentación</a>
        </nav>
    </header>
    <div id="contenderBase" class="container pt-2 pb-2" style="height: 90vh;">
        <h2 class="text-center">Base de datos Administradores</h2>
        <div class="d-flex align-items-center justify-content-end pb-1" style="max-width: 95vw;">
            <div>
                <button class="btn btn-success" onclick="openForm()">Agregar nuevo administradores</button>
            </div>
        </div>
        <div id="contenedorTabla" style="overflow-y: auto; max-height: 70vh; max-width: 95vw;">
            <table class="table table-hover table-bordered mb-5" style="table-layout: fixed; height: 100%;" id="tablaAdmin">
                <thead class="thead-dark">
                    <tr>
                        <th>Correo</th>
                        <th>Nombre</th>
                        <th>Acciones</th>
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
                    url: "/api/datosAdmin",
                    method: "GET",
                    success: function (data) {
                        var tbody = $("#tablaAdmin tbody");
    
                        tbody.empty(); // Limpiar la tabla antes de agregar nuevos datos
    
                        $.each(data, function (i, admin) {
                            // Crear una fila de tabla
                            var fila = "<tr>" +
                                "<td>" + (admin.correo != null ? admin.correo : "") + "</td>" +
                                "<td>" + (admin.nombre != null ? admin.nombre : "") + "</td>";
    
                            // Agregar el botón borrar solo si admin.nombre no es "admin"
                            if (admin.correo !== "admin@admin.com") {
                                fila += "<td>" + "<a class='btn btn-danger' href='/dataBaseAdmin/" + admin.id + "/borrar'>Borrar</a>" + "</td>";
                            } else {
                                fila += "<td></td>"; // Dejar la celda vacía si es "admin"
                            }
    
                            fila += "</tr>";
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
        <div class="popup col-4">
            <div class="d-flex justify-content-end">
                <button class="btn btn-danger" onclick="closeForm()">Cerrar</button>
            </div>
            <h2 class="text-center pb-2">Crear Admin</h2>
            <form action="/dataBaseAdmin/nuevoAdmin" method="post">
                <div class="d-flex justify-content-center">
                    <div style="max-width: 100%;">
                        <div class="pb-2">
                        <label for="correo">Correo</label>
                        <input type="email" name="correo" id="correo" class="form-control">
                        </div>
                        <div class="pb-2">
                        <label for="nombre">Nombre</label>
                        <input type="text" name="nombre" id="nombre" class="form-control">
                        </div>
                        <label for="contrasena">Contraseña</label>
                        <input type="password" name="contrasena" id="contrasena" class="form-control">
                    </div>
                </div>
    
                <div class="pt-2 d-flex justify-content-center">
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
            if (type === 'logo') {
                const checkbox = document.getElementById('clonarLogo');
                const inputFile = document.getElementById('inputLogo');
                const selectOptions = document.getElementById('selectLogo');
            
                if (checkbox.checked) {
                    inputFile.style.display = 'none';
                    selectOptions.style.display = 'block';
                } else {
                    inputFile.style.display = 'block';
                    selectOptions.style.display = 'none';
                }
            } else if (type === 'plantilla') {
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
    <footer class="text-center p-3 bg-light" style="height: 15vh; z-index: 1;">
        <p>Contacto: [Dirección, Teléfono, Correo]</p>
        <div>
            <a href="#">Facebook</a> | <a href="#">Twitter</a> | <a href="#">LinkedIn</a>
        </div>
    </footer>
</body>
</html>