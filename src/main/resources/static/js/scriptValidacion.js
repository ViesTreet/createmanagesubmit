document.addEventListener("DOMContentLoaded", () => {
  const idEnc = document.body.dataset.idEnc;
  const validez = document.body.dataset.validez;
  let botonDescarga = document.getElementById("botonDescargar");
  

  const maxRequests = 5; // Número máximo de descargas permitidas
  const timeFrame = 60 * 60 * 1000; // Tiempo en milisegundos (1 hora)

  async function getIP() {
    // Llamada al backend para obtener la IP
    const response = await fetch("/api/getIP");
    const data = await response.text(); // El backend devuelve texto plano con la IP
    return data;
  }

  async function checkDownloadLimit(ip) {
    // Llamada al backend para verificar el límite de descargas por IP
    const response = await fetch("/api/checkIP", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
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
      document.querySelector(".download-message").textContent =
        "Se ha alcanzado el límite temporal de descargas. Intente más tarde.";
      return;
    }

    // Iniciar la descarga
    const id = idEnc; // Inyecta el id dinámicamente
    fetch("/api/dataBaseAlumno/downloadForQr", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ id }),
    })
      .then((response) => response.blob())
      .then((blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "certificado.pdf";
        document.body.appendChild(a);
        a.click();
        a.remove();

        document.querySelector(".download-message").textContent =
          "Gracias por preferirnos. Descarga completada.";
      })
      .catch((error) => console.error("Error en la descarga:", error));
  }

  if (validez == "Válido") {
    botonDescarga.style.display = "block";
    botonDescarga.addEventListener("click", iniciarDescarga);
  }
});