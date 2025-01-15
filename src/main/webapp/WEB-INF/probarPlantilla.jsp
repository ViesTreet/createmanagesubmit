<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %> 
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Probar plantilla</title>
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <link rel="stylesheet" href="/css/styleCustom.css">
    <link rel="icon" href="/images/Logobgremove.png" type="image/x-icon">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css">
    <style>

    </style>
</head>
<body>
    <header class="d-flex align-items-center justify-content-between p-3 bg-light" style="height: 10vh;">
        <a href="/home" class="logo"><img src="/images/Logobgremove.png" alt="[LOGO]"></a>
        <nav>
            <a href="/dataBasePlantilla" class="btn btn-primary mx-2">Regresar</a>
            <a href="/home" class="btn btn-primary mx-2">Inicio</a>
            <a href="/documentacion/probarPlantilla" class="btn btn-primary mx-2">Documentación</a>
        </nav>
    </header>
    <main class="d-flex flex-column align-items-center justify-content-center" style="height: 90vh;">
        <div class="card" style="width: 70vw;">
            <div class="card-body" style="width: 100%;">
                <h4 class="card-title text-center">Probar plantilla: ${plantilla.nombreCertificado}</h4>
                <form id="formProbarPlantilla" action="/api/dataBasePlantilla/probarPlantilla" method="post">
                    <div class="d-flex justify-content-between">
                        <div style="max-width: 33%;">
                            <div>
                                <label for="nombreAsistente">Nombre Asistente</label>
                                <input type="text" name="nombreAsistente" id="nombreAsistente" class="form-control" placeholder="Nombres Apellidos Asistente">
                            </div>
                            <div>
                                <label for="nombreCurso">Cursos</label>
                                <input type="text" name="nombreCurso" id="nombreCurso" class="form-control" placeholder="curso1|curso2|etc...">
                            </div>
                            <div>
                                <label for="diasCursos">Dias del curso</label>
                                <input type="text" name="diasCursos" id="diasCursos" class="form-control" placeholder="dd/m/aa-dd/m/aa">
                            </div>
                        </div>
                        <div style="max-width: 33%;">
                            <div>
                                <label for="duracion">Duracion</label>
                                <input type="text" name="duracion" id="duracion" class="form-control" placeholder="3 horas">
                            </div>
                            <div>
                                <label for="cliente">Cliente</label>
                                <input type="text" name="cliente" id="cliente" class="form-control" placeholder="Cliente">
                            </div>
                            <div>
                                <label for="obra">Obra</label>
                                <input type="text" name="obra" id="obra" class="form-control" placeholder="Obra">
                            </div>
                        </div>
                        <div style="max-width: 33%;">
                            <div>
                                <label for="notaAprobacion">Nota aprovacion</label>
                                <input type="text" name="notaAprobacion" id="notaAprobacion" class="form-control" placeholder="Ej: 7.0">
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
                    </div>
                    <div class="d-flex align-items-center justify-content-center pt-1">
                        <input type="hidden" value="${plantilla.id}" name="idPlantilla">
                        <input class="btn btn-success" type="submit" value="Probar">
                    </div>
                </form>
            </div>
        </div>
    </main>
    <footer class="text-center p-3 bg-light d-flex justify-content-center align-items-center" style="height: 15vh;">
        <div>
            <div><i class="fa-solid fa-phone"></i><a href="tel:+56 41 3830944">+56 41 3830944</a></div>
            <div><i class="fa-solid fa-location-dot"></i><a href="https://www.google.com/maps/place/Consultores+Empresariales+E-Volution+Limitada/@-36.8252678,-73.050754,19z/data=!3m1!4b1!4m6!3m5!1s0x9669b5d0308198b5:0xbd67409566499fa!8m2!3d-36.8252678!4d-73.0501103!16s%2Fg%2F11fzwngw1q?entry=ttu&g_ep=EgoyMDI1MDEwNi4xIKXMDSoASAFQAw%3D%3D">Freire 728, Oficina 206</a></div>
            <div><i class="fa-solid fa-envelope"></i><a href="mailto:contacto@e-volution.cl">contacto@e-volution.cl</a></div>
        </div>
    </footer>    
    <script>
        document.getElementById('formProbarPlantilla').addEventListener('submit', function(event) {
            event.preventDefault(); // Evita que el formulario se envíe de forma tradicional
        
            // Recopila los datos del formulario
            const formData = new FormData(this);
        
            // Realiza una solicitud fetch para enviar los datos al servidor
            fetch('/api/probarPlantilla', {
                method: 'POST',
                body: formData,
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error en la respuesta de la red');
                }
                return response.blob(); // Obtiene el archivo como Blob
            })
            .then(blob => {
                // Crea un enlace temporal para iniciar la descarga
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = 'certificado.pdf'; // Nombre del archivo a descargar
                document.body.appendChild(a);
                a.click();
                a.remove();
                window.URL.revokeObjectURL(url);

                window.location.href = '/dataBasePlantilla';
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Ocurrió un error al generar el certificado.');
            });
        });
        </script>
    <c:if test="${not empty error}">
        <script>
            showAlert("${error}");
        </script>
    </c:if>
    
</body>
</html>