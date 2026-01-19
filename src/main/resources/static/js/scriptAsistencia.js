document.addEventListener("DOMContentLoaded", () => {
    const revisionCheck = document.getElementById("checkboxConfirmacion");
    const boton = document.getElementById("botonEnviar");

    boton.style.display = "none";

    revisionCheck.addEventListener("change", () => {
        boton.style.display = revisionCheck.checked ? "block" : "none";
    });

    const input = document.getElementById("NombreTemp");

    input.addEventListener("input", () => {
        input.value = input.value.toUpperCase();
    });

    document.getElementById("formAsistencia").addEventListener("submit", async (e) => {
        e.preventDefault();

        const form = e.target;
        const formData = new FormData(form);

        try {
            const response = await fetch("/api/alumnoTemporalSubir", {
                method: "POST",
                body: formData
            });

            if (!response.ok) {
                throw new Error("Error en la API");
            }

            alert("Datos subidos correctamente");

            form.reset();

        } catch (error) {
            alert("Error al subir los datos, revisar RUT");
            console.error(error);
        }
    });

    function formatearRut() {
        var input = document.getElementById('rutTemp');
        var rut = input.value.replace(/\./g, '').replace('-', '');

        if (rut.length > 3) {
            var cuerpo = rut.slice(0, -1);
            var dv = rut.slice(-1);
            input.value = cuerpo + '-' + dv;
        } else {
            input.value = rut;
        }
    }

    document.getElementById('rutTemp').addEventListener('input', formatearRut);

});
