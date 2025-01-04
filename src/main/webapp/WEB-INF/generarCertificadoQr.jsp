<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Descarga de Certificado</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
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
    <footer class="text-center p-3 bg-light" style="height: 15vh;">
        <p>Contacto: [Dirección, Teléfono, Correo]</p>
        <div>
            <a href="#">Facebook</a> | <a href="#">Twitter</a> | <a href="#">LinkedIn</a>
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
