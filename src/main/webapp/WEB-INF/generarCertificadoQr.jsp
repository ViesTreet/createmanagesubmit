<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Descarga de Certificado</title>
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css">    
    <link rel="icon" href="/images/Logobgremove.png" type="image/x-icon">
    <style>
        .container {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            height: 100vh;
            text-align: center;
        }
        .dot {
            height: 15px;
            width: 15px;
            margin: 0 5px;
            background-color: #3498db; /* Color del logo */
            border-radius: 50%;
            display: inline-block;
            animation: blink 1s infinite both;
        }
        @keyframes blink {
            50% { opacity: 0; }
        }
        .dot:nth-child(2) {
            animation-delay: 0.2s;
        }
        .dot:nth-child(3) {
            animation-delay: 0.4s;
        }
        .dot:nth-child(4) {
            animation-delay: 0.6s;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1 style="color: #3498db;">E-VOLUTION</h1>
        <h2>Gracias por preferirnos</h2>
        <p>Su descarga comenzará en breve...</p>
        <div>
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
        </div>
        <p class="download-message mt-3"></p>
    </div>
    <footer class="text-center p-3 bg-light d-flex justify-content-center align-items-center" style="height: 15vh;">
        <div>
            <div><i class="fa-solid fa-phone"></i><a href="tel:+56 41 3830944">+56 41 3830944</a></div>
            <div><i class="fa-solid fa-location-dot"></i><a href="https://www.google.com/maps/place/Consultores+Empresariales+E-Volution+Limitada/@-36.8252678,-73.050754,19z/data=!3m1!4b1!4m6!3m5!1s0x9669b5d0308198b5:0xbd67409566499fa!8m2!3d-36.8252678!4d-73.0501103!16s%2Fg%2F11fzwngw1q?entry=ttu&g_ep=EgoyMDI1MDEwNi4xIKXMDSoASAFQAw%3D%3D">Freire 728, Oficina 206</a></div>
            <div><i class="fa-solid fa-envelope"></i><a href="mailto:contacto@e-volution.cl">contacto@e-volution.cl</a></div>
        </div>
    </footer>
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            const maxRequests = 5; // Número máximo de descargas permitidas
            const timeFrame = 60 * 60 * 1000; // Tiempo en milisegundos (1 hora)
                
            async function getIP() {
                // Llamada al backend para obtener la IP
                const response = await fetch('/api/getIP');
                const data = await response.text(); // El backend devuelve texto plano con la IP
                return data;
            }
        
            async function checkDownloadLimit(ip) {
                // Llamada al backend para verificar el límite de descargas por IP
                const response = await fetch('/api/checkIP', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ ip }), // Enviamos la IP al backend
                });
            
                if (response.status === 429) {
                    // Si el backend devuelve el código HTTP 429 (Too Many Requests)
                    return false;
                }
            
                return true;
            }
        
            async function iniciarDescarga() {
                const ip = await getIP(); // Obtén la IP del cliente
                const isAllowed = await checkDownloadLimit(ip); // Verifica el límite de descargas
            
                if (!isAllowed) {
                    document.querySelector('.download-message').textContent =
                        'Se ha alcanzado el límite temporal de descargas. Intente más tarde.';
                    return;
                }
            
                // Iniciar la descarga
                const id = '${id}'; // Reemplaza con el ID real
                fetch('/api/dataBaseAlumno/downloadForQr', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ id }),
                })
                    .then((response) => response.blob())
                    .then((blob) => {
                        const url = window.URL.createObjectURL(blob);
                        const a = document.createElement('a');
                        a.href = url;
                        a.download = 'certificado.pdf';
                        document.body.appendChild(a);
                        a.click();
                        a.remove();
                    
                        document.querySelector('.download-message').textContent =
                            'Gracias por preferirnos. Descarga completada.';
                    })
                    .catch((error) =>
                        console.error('Error en la descarga:', error)
                    );
            }
        
            iniciarDescarga();
        });
    </script>
</body>
</html>
