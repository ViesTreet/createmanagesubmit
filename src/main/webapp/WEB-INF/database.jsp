<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Lista de Alumnos</title>
    <!-- Importar CSS de Bootstrap -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <!-- Importar jQuery -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>  
    <!-- Importar JS de Bootstrap -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
</head>
<body>

<div class="container mt-5">
    <h2>Lista de Alumnos</h2>
    <table class="table table-bordered" id="tablaAlumnos">
        <thead class="thead-dark">
            <tr>
                <th>ID</th>
                <th>Nombre Asistente</th>
                <th>Nombre Curso</th>
                <th>Días Cursos</th>
                <!-- Agrega más columnas según sea necesario -->
            </tr>
        </thead>
        <tbody>
            <!-- Las filas se agregarán dinámicamente aquí -->
        </tbody>
    </table>
</div>

<script>
    $(document).ready(function(){
        function cargarDatos(){
            $.ajax({
                url: "/api/datos",
                method: "GET",
                success: function(data){
                    var tbody = $("#tablaAlumnos tbody");
                    tbody.empty(); // Limpiar la tabla antes de agregar nuevos datos

                    $.each(data, function(i, alumno){
                        var fila = "<tr>"+
                            "<td>"+ (alumno.id != null ? alumno.id : "") +"</td>"+
                            "<td>"+ (alumno.nombreAsistente != null ? alumno.nombreAsistente : "") +"</td>"+
                            "<td>"+ (alumno.nombreCurso != null ? alumno.nombreCurso : "") +"</td>"+
                            "<td>"+ (alumno.diasCursos != null ? alumno.diasCursos : "") +"</td>"+
                            // Agrega más celdas según sea necesario
                            "</tr>";
                        tbody.append(fila);
                    });
                },
                error: function(error){
                    console.log("Error al obtener los datos", error);
                }
            });
        }

        // Cargar datos inicialmente
        cargarDatos();

        // Actualizar la tabla cada 30 segundos (30000 milisegundos)
        setInterval(cargarDatos, 30000);
    });
</script>

</body>
</html>