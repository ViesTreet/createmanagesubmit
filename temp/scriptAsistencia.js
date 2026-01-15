document.addEventListener("DOMContentLoaded", () => {
    const revisionCheck = document.getElementById("checkboxConfirmacion");
    const boton = document.getElementById("botonEnviar");

    boton.style.display = "none";

    revisionCheck.addEventListener("change", () => {
        boton.style.display = revisionCheck.checked ? "block" : "none";
    });

    const input = document.getElementById("nombre");

    input.addEventListener("input", () => {
        input.value = input.value.toUpperCase();
    });

});
