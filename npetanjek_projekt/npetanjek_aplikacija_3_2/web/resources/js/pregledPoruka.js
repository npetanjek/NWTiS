var wsocket;

function connect() {
    var aplikacija = "/" + document.location.pathname.split("/")[1];
    var wsUri = "ws://" + document.location.host + aplikacija +
            "/infoPoruka";
    wsocket = new WebSocket(wsUri);
    wsocket.onmessage = onMessage;
}

function onMessage(evt) {
    var arraypv = evt.data;
    if (arraypv[0] === "K") {        
        document.getElementById("formPregledJmsPoruka:preuzmiJmsPoruke").click();
    } else if (arraypv[0] === "M") {
        document.getElementById("formPregledMqttPoruka:preuzmiMqttPoruke").click();
    }
}

window.addEventListener("load", connect, false);


