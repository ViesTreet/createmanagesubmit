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
            body {
                overflow-x: hidden;
                margin: 0;
                padding: 0;
                font-family: Arial, sans-serif;
                background-color: #f9f9f9;
            }
            .container {
                display: flex;
                height: 100vh;
                width: 100vw;
                justify-content: center;
                align-items: center;
                padding: 20px;
                text-align: center;
                background-color: #f9f9f9;

            }

            .contenido{
                box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                align-items: center;
                flex-direction: column;
                justify-content: center;
                display: flex;
                height: 100%;
                width: 100%;
                background-color: #fff;
                border-radius: 10px;
            }

            h1 {
                color: #3498db;
                font-size: 2rem;
                margin-bottom: 10px;
            }
            h2 {
                font-size: 1.5rem;
                margin-bottom: 20px;
                color: #555;
            }
            .dot {
                height: 12px;
                width: 12px;
                margin: 0 5px;
                background-color: #3498db;
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
            footer {
                background-color: #f0f0f0;
                padding: 15px;
                text-align: center;
                font-size: 0.9rem;
                margin-top: auto;
                width: 100vw;
            }
            footer a {
                color: #3498db;
                text-decoration: none;
            }
            footer a:hover {
                text-decoration: underline;
            }
            .contact-info div {
                margin-bottom: 8px;
            }
            .download-message {
                font-size: 1rem;
                margin-top: 15px;
                color: #666;
            }

            @media (max-width: 1024px) {
                .container {
                display: flex;
                height: 100vh;
                width: 100vw;
                justify-content: center;
                align-items: center;
                text-align: center;
                padding: 0;
                margin: 0;
                background-color: #f9f9f9;
                border-radius: 10px;
            }
                .contenido{
                align-items: center;
                flex-direction: column;
                justify-content: center;
                display: flex;
                height: 100%;
                width: 100%;
                background-color: #fff;
            }
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="contenido">
                <h1>E-VOLUTION</h1>
                <h2>Gracias por preferirnos</h2>
                <p>Su descarga comenzará en breve...</p>
                <div>
                    <span class="dot"></span>
                    <span class="dot"></span>
                    <span class="dot"></span>
                </div>
                <p class="download-message mt-3"></p>
            </div>
        </div>
        <footer>
            <div class="contact-info">
                <div><i class="fa-solid fa-phone"></i> <a href="tel:+56413830944">+56 41 3830944</a></div>
                <div><i class="fa-solid fa-location-dot"></i> <a href="https://goo.gl/maps/xQm5mBkxH4N2" target="_blank">Freire 728, Oficina 206</a></div>
                <div><i class="fa-solid fa-envelope"></i> <a href="mailto:contacto@e-volution.cl">contacto@e-volution.cl</a></div>
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
