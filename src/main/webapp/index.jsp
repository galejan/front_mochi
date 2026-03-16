<html>
<body>
<h2>El servidor de fábrica esta funcionando!</h2>
<p> Versión 2.1</p>
<p> Fecha última modificación: 10/01/2018 </p>

    <li> Se pone en marcha la función de agregar restos por pecición del QR con la actividad de código 10 al finalizar la actividad.</li>
    <li> El sistema envía email justo después de mochilificar. Se adjunta LOG.</li>
    <li> Se incluye indicación de barra agregada al stock para barras RESTO, 'AGREGADA'. </li>  
    <li> Edición de Log para mostrar correctamente en Excel utilizando separadores ':'. </li> 
    <li> BUG por el que las barras de 6300 aumentaban en el stock cuando se liberaban las barras de un proyecto.</li> 
    <li> Se incluye solicitud de inventario a través de URL. HOST/web-service-fabrica/inventario/stockbarras/ </li> 
    
    
<p> Versión 2.0</p>
<p> Fecha última modificación: 28/12/2017 </p>
    <li> Se pone en marcha el sistema de mochilificación. La petición es realizada por APPGLASS al finalizar el envío de barras.</li>
    <li> Recepción de barras de cada proyecto y crear restos. Operaciones de agregar restos lista para solicitud desde Android.</li>
    <li> Actualización de proyecto por nueva versión sincronizada.</li>
    <li> Sólo afecta a varios colores de stock.</li>
    
<p> Versión 1.2</p>
    <p> Fecha última modificación: 19/09/2016 </p>
    <li> Nueva sincronización. Actualizar el estado de un evento en concreto para cambiarlo de estado FINALIZADO a PAUSADO por.</li>
    `
<p> Versión 1.1</p>
<p> Fecha última modificación: 28/06/2016 </p>

    <li> Nueva sincronización de variables ML, Paneles, Tipo proyecto.</li>
    <li> La hora de registro de eventos ahora es sólo gestionada por el servidor en lugar de los dispositivos.</li>
    <li> Los proyectos serán actualizados según indicaciones en la sincronización (PUT o POST), variableSincronización y número de versión actuales en APPGLASS.</li>
    





</body>
</html>
