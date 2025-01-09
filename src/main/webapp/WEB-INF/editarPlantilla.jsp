<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Editar Plantilla</title>
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
            <a href="/dataBasePlantilla" class="btn btn-primary mx-2">Regresar</a>
            <a href="/home" class="btn btn-primary mx-2">Inicio</a>
            <a href="/documentacion/editarPlantilla" class="btn btn-primary mx-2">Documentación</a>
        </nav>
    </header>
    <div id="contenderBase" class="d-flex justify-content-center align-items-center container pt-2 pb-2" style="height: 90vh;">
        <div class="card col-6">
            <div class="card-body">
                <div class="card-title"><h4 class="text-center">Editar plantilla: ${plantilla.nombreCertificado}</h4></div>
            </div>
            <form action="/dataBasePlantilla/editarPlantilla" method="post" enctype="multipart/form-data">
                <div class="d-flex justify-content-around">
                    <div style="max-width: 40%;">
                        <Label for="nombreCertificado">Nombre Plantilla</Label>
                        <input id="nombreCertificado" name="nombreCertificado" type="text" class="form-control" placeholder="Nombre de la plantilla">
                        <Label for="descripcion">Descripcion Plantilla</Label>
                        <input id="descripcion" type="text" name="descripcion" class="form-control" placeholder="Descripcion de la plantilla">
                        <input type="checkbox" id="cambiarLogo" name="cambiarLogo" value="true" onclick="toggleInputFile('inputLogo', this)">
                        <label for="cambiarLogo">Cambiar Logo</label>
                        <!-- Input file para subir el nuevo logo -->
                        <div id="inputLogo" style="display: none;">
                            <label for="pathLogo">Nuevo Logo</label>
                            <input type="file" name="pathLogo" id="pathLogo" class="form-control" >
                        </div>
                    </div>
                    <div style="max-width: 40%;">
                        <Label for="asistenciaMin">Asistencia Minima</Label>
                        <input id="asistenciaMin" type="text" name="asistenciaMin" class="form-control" placeholder="ej:60">
                        <Label for="notaMin">Nota Minima</Label>
                        <input id="notaMin" type="text" name="notaMin" class="form-control" placeholder="ej:5.5">
                        <input type="checkbox" id="cambiarPlantilla" name="cambiarPlantilla" value="true" onclick="toggleInputFile('inputPlantilla', this)">
                        <label for="cambiarPlantilla">Cambiar Plantilla</label>
                        <!-- Input file para subir la nueva plantilla -->
                        <div id="inputPlantilla" style="display: none;">
                            <label for="pathArchivo">Nueva Plantilla(.pptx)</label>
                            <input type="file" name="pathArchivo" id="pathArchivo" class="form-control" accept=".pptx">
                        </div>
                    </div>
                </div>
                <div class="d-flex justify-content-between pt-2">
                    <a href="/dataBasePlantilla" class="btn btn-warning">Cancelar</a>
                    <input type="hidden" name="id" id="id" value="${plantilla.id}">
                    <input type="submit" class="btn btn-success" value="Enviar">
                </div>
            </form>
        </div>
    </div>
    <script>
        // Función para mostrar u ocultar el input file
        function toggleInputFile(inputId, checkbox) {
            const inputDiv = document.getElementById(inputId);
            if (checkbox.checked) {
                inputDiv.style.display = 'block'; // Mostrar input file
            } else {
                inputDiv.style.display = 'none';  // Ocultar input file
            }
        }
    </script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            // Rellenar cada campo con su valor correspondiente
            document.getElementById('nombreCertificado').value = "${plantilla.nombreCertificado}";
            document.getElementById('descripcion').value = "${plantilla.descripcion}";
            document.getElementById('asistenciaMin').value = "${plantilla.asistenciaMin}";
            document.getElementById('notaMin').value = "${plantilla.notaMin}";
        });
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