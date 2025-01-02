<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Lista de Plantilla</title>
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
            <a href="/home" class="mx-2">Inicio</a>
            <a href="/documentacion" class="mx-2">Funciones</a>
            <a href="#" class="mx-2">Contacto</a>
        </nav>
    </header>
    <div id="contenderBase" class="d-flex justify-content-center align-items-center container pt-2 pb-2" style="height: 90vh;">
        <div class="card col-5">
            <div class="card-body">
                <div class="card-title"><h4 class="text-center">¿Borrar plantilla: ${plantilla.nombreCertificado}?</h4></div>
                <p>Borrar una plantilla en uso puede ocacionar que todos los alumno relacionados con esta pierdan la referencia.</p>
            </div>
            <div class="d-flex justify-content-around ">
                <a href="/dataBasePlantilla" class="btn btn-primary">Cancelar</a>
                <a href="/dataBasePlantilla/Plantilla/${plantilla.id}/borrar" class="btn btn-danger">Borrar de todos modos</a>
            </div>
        </div>
    </div>
    <footer class="text-center p-3 bg-light" style="height: 15vh; z-index: 1;">
        <p>Contacto: [Dirección, Teléfono, Correo]</p>
        <div>
            <a href="#">Facebook</a> | <a href="#">Twitter</a> | <a href="#">LinkedIn</a>
        </div>
    </footer>
</body>
</html>