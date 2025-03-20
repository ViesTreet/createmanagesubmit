<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home</title>
    <link rel="stylesheet" th:href="@{/css/bootstrap.min.css}">
    <link rel="stylesheet" th:href="@{/css/styleCustom.css}">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css">
    <link rel="icon" th:href="@{/images/Logobgremove.png}" type="image/x-icon">
    <style>
        .btn-custom {
            width: 100%;
            max-width: 300px;
            margin: 10px 0;
        }
    </style>
</head>
<body>
    <header class="d-flex align-items-center justify-content-between p-3 bg-light" style="height: 10vh;">
        <a class="logo"><img th:src="@{/images/Logobgremove.png}" alt="[LOGO]"></a>
        <nav class="d-flex justify-content-center align-items-center flex-nowrap">
            <a th:href="@{/documentacion}" target="_blank" class="btn btn-primary mx-2">Documentación</a>
            <div class="d-flex flex-column justify-content-center align-items-center">
                <i class="fa-solid fa-user"></i>
                <p class="p-0 m-0" style="font-size: normal;" th:text="${admin.nombre}"></p>
            </div>
        </nav>
    </header>
    <main class="pb-5 d-flex flex-column align-items-center justify-content-center" style="height: 90vh;">
        <h1 class="text-center pb-1">Bienvenido a <b style="color: #0266ac;">E</b>-VOLUTION</h1>
        <h3 class="text-center pb-3">Por favor elija una ubicación</h3>
        <form class="d-flex flex-wrap justify-content-center" th:action="@{/actualizarUbicacion}" method="post">
            <select class="form-select" name="ubi" id="ubi">
                <option value="arica">Arica y Parinacota</option>
                <option value="tarapaca">Tarapacá</option>
                <option value="antofagasta">Antofagasta</option>
                <option value="atacama">Atacama</option>
                <option value="coquimbo">Coquimbo</option>
                <option value="valparaiso">Valparaíso</option>
                <option value="metropolitana">Metropolitana de Santiago</option>
                <option value="ohiggins">Libertador General Bernardo O'Higgins</option>
                <option value="maule">Maule</option>
                <option value="nuble">Ñuble</option>
                <option value="biobio">Biobío</option>
                <option value="araucania">La Araucanía</option>
                <option value="rios">Los Ríos</option>
                <option value="lagos">Los Lagos</option>
                <option value="aysen">Aysén del General Carlos Ibáñez del Campo</option>
                <option value="magallanes">Magallanes y de la Antártica Chilena</option>
            </select>
            <input type="submit" class="mt-2 btn btn-success" value="Seleccionar">
        </form>
    </main>
    <footer class="text-center p-3 bg-light d-flex justify-content-center align-items-center" style="height: 15vh;">
        <div>
            <div><i class="fa-solid fa-phone"></i><a href="tel:+56 41 3830944">+56 41 3830944</a></div>
            <div><i class="fa-solid fa-location-dot"></i><a href="https://www.google.com/maps/place/Consultores+Empresariales+E-Volution+Limitada/@-36.8252678,-73.050754,19z/data=!3m1!4b1!4m6!3m5!1s0x9669b5d0308198b5:0xbd67409566499fa!8m2!3d-36.8252678!4d-73.0501103!16s%2Fg%2F11fzwngw1q?entry=ttu&g_ep=EgoyMDI1MDEwNi4xIKXMDSoASAFQAw%3D%3D">Freire 728, Oficina 206</a></div>
            <div><i class="fa-solid fa-envelope"></i><a href="mailto:contacto@e-volution.cl">contacto@e-volution.cl</a></div>
        </div>
    </footer>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
</body>
</html>
