// ==========================================
// 1. BASE DE DATOS Y PERSISTENCIA
// ==========================================
let dbCoches = [];
let bannedWords = JSON.parse(localStorage.getItem('bannedWordsDb')) || ["estafa", "tonto", "idiota"];
let dbChats = JSON.parse(localStorage.getItem('autoMarketChatsDb')) || []; 

let usuarioActual = localStorage.getItem('usuarioActual') || "Invitado";
let rolActual = localStorage.getItem('rolActual') || "USER";
let idCocheEditando = null;
let chatActualCon = null; 

function guardarDatos() {
    localStorage.setItem('bannedWordsDb', JSON.stringify(bannedWords));
    localStorage.setItem('autoMarketChatsDb', JSON.stringify(dbChats)); 
}

// ==========================================
// 2. NAVEGACIÓN Y RENDERIZADO BÁSICO
// ==========================================
function mostrarSeccion(target) {
    const sections = ['sec-inicio', 'sec-publicar', 'sec-mis-coches', 'sec-admin', 'sec-detalle', 'sec-chat', 'sec-lista-chats'];
    sections.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.classList.add("hidden");
    });
    
    const targetEl = document.getElementById('sec-' + target);
    if (targetEl) targetEl.classList.remove("hidden");

    if (target === 'inicio') cargarCochesInicio();
    if (target === 'mis-coches') cargarMisCoches();
    if (target === 'admin') cargarPanelAdmin();
    if (target === 'lista-chats') cargarListaChats();
}

async function cargarCochesInicio() {
    const grid = document.getElementById("grid-cars");
    const countText = document.getElementById("inicio-resultados-count");
    if (!grid) return;

    const mainSearch = document.getElementById("main-search");
    const query = mainSearch ? mainSearch.value.trim() : "";

    try {
        const response = await fetch("/api/buscar-coches", {
            method: "POST",
            headers: { "Content-Type": "application/json; charset=UTF-8" },
            body: JSON.stringify({ texto: query })
        });
        const data = await response.json();
        if (response.ok && data.ok) {
            dbCoches = data.resultados;
        }
    } catch (err) {
        console.error("Error al traer coches del servidor:", err);
    }

    const filterFuel = document.getElementById("filter-fuel");
    const filterYear = document.getElementById("filter-year");
    const fuel = filterFuel ? filterFuel.value : "all";
    const yearMin = filterYear ? parseInt(filterYear.value) : 0;

    const filtrados = dbCoches.filter(c => {
        const matchFuel = fuel === "all" || c.motor === fuel;
        const matchYear = c.ano >= yearMin;
        return matchFuel && matchYear;
    });

    if (countText) countText.innerText = `${filtrados.length} coches encontrados`;

    if (filtrados.length === 0) {
        grid.innerHTML = "<p style='grid-column: 1/-1; text-align: center; padding:2rem; color: #666;'>No se han encontrado coches homologados o aprobados.</p>";
        return;
    }

    grid.innerHTML = filtrados.map(c => {
        const imagenCoche = (c.imgs && c.imgs.length > 0 && c.imgs[0] !== "") ? c.imgs[0] : "https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80";
        return `
        <div class="car-card car-card-clickable" onclick="abrirDetalle(${c.id})">
            <div class="car-image-container">
                <img src="${imagenCoche}" alt="${c.marca} ${c.modelo}">
                <span class="price-tag">€${c.precio}</span>
            </div>
            <div class="car-info">
                <h3>${c.marca} ${c.modelo}</h3>
                <div class="car-specs">
                    <span><i class="far fa-calendar-alt"></i> ${c.ano}</span>
                    <span><i class="fas fa-tachometer-alt"></i> ${c.km} km</span>
                    <span><i class="fas fa-gas-pump"></i> ${c.motor}</span>
                    <span><i class="fas fa-map-marker-alt"></i> ${c.ciudad}</span>
                </div>
                <div class="car-footer">Vendedor: ${c.vendedor}</div>
            </div>
        </div>
    `}).join('');
}

// ==========================================
// 3. INICIALIZACIÓN Y AUTH (CORREGIDO)
// ==========================================
document.addEventListener("DOMContentLoaded", () => {
    // Verificar si ya había una sesión activa al recargar la página
    if (usuarioActual !== "Invitado") {
        aplicarInterfazLogueado(usuarioActual, rolActual);
    }

    cargarCochesInicio();

    document.getElementById("main-search").addEventListener("input", cargarCochesInicio);
    document.getElementById("filter-fuel").addEventListener("change", cargarCochesInicio);
    document.getElementById("filter-year").addEventListener("change", cargarCochesInicio);

    // Modals de Auth
    document.getElementById("nav-btn-login").onclick = () => document.getElementById("auth-modal").classList.remove("hidden");
    document.getElementById("modal-close").onclick = () => document.getElementById("auth-modal").classList.add("hidden");

    document.getElementById("login-tab").onclick = () => {
        document.getElementById("login-form").classList.remove("hidden");
        document.getElementById("register-form").classList.add("hidden");
        document.getElementById("login-tab").classList.add("active");
        document.getElementById("register-tab").classList.remove("active");
    };
    document.getElementById("register-tab").onclick = () => {
        document.getElementById("register-form").classList.remove("hidden");
        document.getElementById("login-form").classList.add("hidden");
        document.getElementById("register-tab").classList.add("active");
        document.getElementById("login-tab").classList.remove("active");
    };

    function aplicarInterfazLogueado(username, rol) {
        usuarioActual = username;
        rolActual = rol;
        localStorage.setItem('usuarioActual', username);
        localStorage.setItem('rolActual', rol);

        document.getElementById("auth-modal").classList.add("hidden");
        document.getElementById("nav-btn-login").classList.add("hidden");
        document.getElementById("nav-user-profile").classList.remove("hidden");
        document.getElementById("nav-mis-coches").classList.remove("hidden");
        document.getElementById("nav-chats").classList.remove("hidden");
        document.getElementById("nav-btn-publicar").classList.remove("hidden");
        document.getElementById("user-name-display").innerText = usuarioActual;

        if (rol === "ADMIN" || username.toLowerCase() === "admin") {
            document.getElementById("nav-panel-admin").classList.remove("hidden");
        }
    }

    // Botón salir purga localStorage y sesión del Servidor
    document.getElementById("btn-logout").onclick = async () => {
        try {
            const response = await fetch("/api/buscar-coches", { 
                method: "POST",
                headers: { "Content-Type": "application/json; charset=UTF-8" },
                body: JSON.stringify({ accion: "logout" })
            });
            const data = await response.json();
            if (response.ok && data.ok) {
                usuarioActual = "Invitado";
                rolActual = "USER";
                localStorage.removeItem('usuarioActual');
                localStorage.removeItem('rolActual');

                document.getElementById("nav-btn-login").classList.remove("hidden");
                document.getElementById("nav-user-profile").classList.add("hidden");
                document.getElementById("nav-mis-coches").classList.add("hidden");
                document.getElementById("nav-chats").classList.add("hidden");
                document.getElementById("nav-btn-publicar").classList.add("hidden");
                document.getElementById("nav-panel-admin").classList.add("hidden");
                mostrarSeccion('inicio');
                alert("Sesión cerrada correctamente.");
            }
        } catch (err) {
            console.error("Error al cerrar sesión:", err);
        }
    };

    document.getElementById("login-form").onsubmit = async (e) => {
        e.preventDefault();
        const userVal = document.getElementById("login-user").value;
        const passVal = document.getElementById("login-pass") ? document.getElementById("login-pass").value : "";

        try {
            const response = await fetch("/api/login-usuario", {
                method: "POST",
                headers: { "Content-Type": "application/json; charset=UTF-8" },
                body: JSON.stringify({ username: userVal, password: passVal })
            });
            const data = await response.json();
            if (response.ok && data.ok) {
                aplicarInterfazLogueado(userVal, data.rol || "USER");
                mostrarSeccion('inicio');
            } else {
                alert(data.mensaje || "Error al iniciar sesión.");
            }
        } catch (err) {
            console.error("Error en login:", err);
        }
    };

    document.getElementById("register-form").onsubmit = async (e) => {
        e.preventDefault();
        const usernameVal = document.getElementById("register-username").value;
        const nombreVal = document.getElementById("register-nombre").value;
        const apellidosVal = document.getElementById("register-apellidos").value;
        const emailVal = document.getElementById("register-email") ? document.getElementById("register-email").value : "";
        const passVal = document.getElementById("register-pass") ? document.getElementById("register-pass").value : "";

        try {
            const response = await fetch("/api/registrar-usuario", {
                method: "POST",
                headers: { "Content-Type": "application/json; charset=UTF-8" },
                body: JSON.stringify({ 
                    username: usernameVal, 
                    nombre: nombreVal,
                    apellidos: apellidosVal,
                    email: emailVal, 
                    password: passVal 
                })
            });
            const data = await response.json();
            if (response.ok && data.ok) {
                alert(data.mensaje); 
                aplicarInterfazLogueado(usernameVal, "USER");
                mostrarSeccion('inicio');
            } else {
                alert(data.mensaje || "El usuario ya existe.");
            }
        } catch (err) {
            console.error("Error en registro:", err);
        }
    };

    document.getElementById("form-publicar").onsubmit = async function(e) {
        e.preventDefault();
        const filesInput = document.getElementById("pub-img").files;
        if(filesInput.length < 1 || filesInput.length > 4) {
            alert("Debes seleccionar mínimo 1 y máximo 4 fotos.");
            return;
        }

        const promesasImagenes = Array.from(filesInput).map(file => {
            return new Promise((resolve) => {
                const reader = new FileReader();
                reader.onload = ev => resolve(ev.target.result);
                reader.readAsDataURL(file);
            });
        });

        const imagenesCargadas = await Promise.all(promesasImagenes);

        const cocheJSON = {
            marca: document.getElementById("pub-marca").value,
            modelo: document.getElementById("pub-modelo").value,
            ciudad: document.getElementById("pub-ciudad").value,
            ano: parseInt(document.getElementById("pub-ano").value),
            precio: document.getElementById("pub-precio").value,
            km: document.getElementById("pub-km").value,
            motor: document.getElementById("pub-motor").value,
            transmision: document.getElementById("pub-transmision").value,
            imgs: imagenesCargadas,
            desc: document.getElementById("pub-desc").value
        };

        try {
            const response = await fetch("/api/buscar-coches", {
                method: "POST",
                headers: { "Content-Type": "application/json; charset=UTF-8" },
                body: JSON.stringify(cocheJSON)
            });
            const data = await response.json();
            if (response.ok && data.ok) {
                alert(data.mensaje);
                document.getElementById("form-publicar").reset();
                mostrarSeccion('inicio');
            } else {
                alert(data.mensaje || "Error al subir el coche.");
            }
        } catch (err) {
            console.error("Error al conectar con el servidor:", err);
            alert("Fallo de comunicación con la base de datos.");
        }
    };
});

// ==========================================
// 4. DETALLES Y CHATS GLOBALES
// ==========================================
window.cancelarEdicion = function() {
    idCocheEditando = null;
    document.getElementById("form-publicar").reset();
    mostrarSeccion('inicio');
}

window.abrirDetalle = function(idBuscado) {
    const c = dbCoches.find(item => item.id === idBuscado);
    if (!c) return;
    
    const fotoPrincipal = (c.imgs && c.imgs.length > 0 && c.imgs[0] !== "") ? c.imgs[0] : "https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80";
    let mainImg = `<img src="${fotoPrincipal}" style="width:100%; border-radius:1rem;">`;
    let restImgs = (c.imgs && c.imgs.length > 1) ? `<div class="multi-img-grid">` + c.imgs.slice(1).map(i => `<img src="${i}">`).join('') + `</div>` : '';

    document.getElementById("detalle-coche-content").innerHTML = `
        <div class="detalle-grid">
            <div class="detalle-img-container">
                ${mainImg}
                <span class="detalle-precio">€${c.precio}</span>
                ${restImgs}
            </div>
            <div class="detalle-info">
                <h2>${c.marca} ${c.modelo}</h2>
                <p class="ubicacion"><i class="fas fa-map-marker-alt"></i> ${c.ciudad}</p>
                <div class="specs-grid">
                    <div class="spec-box"><span>Año</span><strong>${c.ano}</strong></div>
                    <div class="spec-box"><span>Kilómetros</span><strong>${c.km}</strong></div>
                    <div class="spec-box"><span>Motor</span><strong>${c.motor}</strong></div>
                    <div class="spec-box"><span>Cambio</span><strong>${c.transmision}</strong></div>
                </div>
                <div class="desc-box"><h3>Descripción</h3><p>${c.desc || 'Sin descripción'}</p></div>
                <div class="vendedor-box">
                    <div class="vendedor-nombre">${c.vendedor}</div>
                    <button class="btn-dark-full" onclick="abrirChat('${c.vendedor}')">Enviar Mensaje</button>
                </div>
            </div>
        </div>
    `;
    mostrarSeccion('detalle');
    window.scrollTo(0, 0);
}

window.cargarListaChats = function() {
    const container = document.getElementById("lista-chats-container");
    const contactosUnicos = new Set();
    dbChats.forEach(m => {
        if (m.sender === usuarioActual) contactosUnicos.add(m.receiver);
        if (m.receiver === usuarioActual) contactosUnicos.add(m.sender);
    });

    if (contactosUnicos.size === 0) {
        container.innerHTML = "<p>No tienes mensajes todavía.</p>";
        return;
    }

    let htmlChats = "";
    contactosUnicos.forEach(contacto => {
        const mensajesConEsteContacto = dbChats.filter(m =>
            (m.sender === usuarioActual && m.receiver === contacto) ||
            (m.sender === contacto && m.receiver === usuarioActual)
        );
        const ultimoMsj = mensajesConEsteContacto[mensajesConEsteContacto.length - 1];
        const remitenteText = ultimoMsj.sender === usuarioActual ? "Tú" : contacto;

        htmlChats += `
            <div class="car-card-horizontal" style="cursor:pointer;" onclick="abrirChat('${contacto}')">
                <div class="chat-avatar" style="color:white; margin-right:15px; width:50px; height:50px; font-size:1.5rem;"><i class="fas fa-user"></i></div>
                <div class="car-card-content">
                    <h3 style="margin-bottom:5px;">${contacto}</h3>
                    <p style="color:#666; font-size:0.9rem;">${remitenteText}: ${ultimoMsj.text}</p>
                </div>
            </div>
        `;
    });
    container.innerHTML = htmlChats;
}

window.abrirChat = function(nombre) {
    chatActualCon = nombre; 
    document.getElementById('chat-seller-name').innerText = nombre;
    const chatMsg = document.getElementById('chat-messages');
    chatMsg.innerHTML = ``; 

    const historial = dbChats.filter(m =>
        (m.sender === usuarioActual && m.receiver === nombre) ||
        (m.sender === nombre && m.receiver === usuarioActual)
    );

    historial.forEach(m => {
        const esMio = m.sender === usuarioActual;
        const claseMsj = esMio ? 'sent' : 'received';
        chatMsg.innerHTML += `<div class="message ${claseMsj}"><p>${m.text}</p></div>`;
    });

    mostrarSeccion('chat');
    chatMsg.scrollTop = chatMsg.scrollHeight;
}

window.enviarMensaje = function() {
    const input = document.getElementById('chat-input');
    const chatMsg = document.getElementById('chat-messages');
    let msj = input.value.trim();
    if(!msj || !chatActualCon) return;
    
    bannedWords.forEach(word => {
        const regex = new RegExp(`\\b${word.trim()}\\b`, 'gi');
        msj = msj.replace(regex, '***');
    });

    const nuevoMensaje = {
        sender: usuarioActual,
        receiver: chatActualCon,
        text: msj,
        timestamp: Date.now()
    };
    dbChats.push(nuevoMensaje);
    guardarDatos(); 

    chatMsg.innerHTML += `<div class="message sent"><div class="message-text">${msj}</div></div>`;
    input.value = "";
    chatMsg.scrollTop = chatMsg.scrollHeight;
}

// ==========================================
// 5. MIS COCHES Y PANEL ADMIN
// ==========================================
window.cargarMisCoches = function() {
    const container = document.getElementById("list-mis-coches");
    const misCoches = dbCoches.filter(c => c.vendedor === usuarioActual);
    
    if (misCoches.length === 0) {
        container.innerHTML = "<p>No has publicado ningún coche.</p>";
        return;
    }
    container.innerHTML = misCoches.map(c => renderCarHorizontal(c, true)).join('');
}

window.eliminarCoche = function(id) {
    if(confirm("¿Seguro que quieres eliminar esta publicación?")) {
        dbCoches = dbCoches.filter(c => c.id !== id);
        cargarMisCoches();
    }
}

window.cargarPanelAdmin = function() {
    document.getElementById("admin-banned-words").value = bannedWords.join(", ");
    const pendientes = dbCoches.filter(c => c.estado === "pendiente");
    const aprobados = dbCoches.filter(c => c.estado === "aprobado");
    const rechazados = dbCoches.filter(c => c.estado === "rechazado");
    
    document.getElementById("count-total").innerText = dbCoches.length;
    document.getElementById("count-pendientes").innerText = pendientes.length;
    document.getElementById("count-aprobados").innerText = aprobados.length;
    document.getElementById("count-rechazados").innerText = rechazados.length;

    document.getElementById("list-admin-pendientes").innerHTML = pendientes.length ? pendientes.map(c => renderCarHorizontal(c, false, true)).join('') : "<p style='color:#666;'>No hay coches pendientes.</p>";
    document.getElementById("list-admin-aprobados").innerHTML = aprobados.length ? aprobados.map(c => renderCarHorizontal(c, false, true)).join('') : "<p style='color:#666;'>No hay coches aprobados.</p>";
    document.getElementById("list-admin-rechazados").innerHTML = rechazados.length ? rechazados.map(c => renderCarHorizontal(c, false, true)).join('') : "<p style='color:#666;'>No hay coches rechazados.</p>";
}

function renderCarHorizontal(c, isPropietario = false, isAdmin = false) {
    let actions = "";
    if(isPropietario) {
        actions = `<button class="btn-action btn-reject" onclick="eliminarCoche(${c.id})">Eliminar</button>`;
    }
    if(isAdmin) {
        if(c.estado !== 'aprobado') actions += `<button class="btn-action btn-approve" onclick="cambiarEstado(${c.id}, 'aprobado')">Aprobar</button>`;
        if(c.estado !== 'rechazado') actions += `<button class="btn-action btn-reject" onclick="cambiarEstado(${c.id}, 'rechazado')">Rechazar (Imágenes/Datos)</button>`;
    }

    const primeraImg = (c.imgs && c.imgs.length > 0 && c.imgs[0] !== "") ? c.imgs[0] : "https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80";

    return `
        <div class="car-card-horizontal">
            <img src="${primeraImg}">
            <div class="car-card-content">
                <div class="car-header-flex">
                    <h3>${c.marca} ${c.modelo}</h3>
                    <span class="car-price-admin">€${c.precio}</span>
                </div>
                ${isAdmin ? `<p style="color: #666; margin-bottom:10px; font-size: 0.9rem;">Vendedor: ${c.vendedor}</p>` : ''}
                <span class="status-badge status-${c.estado}">${c.estado.toUpperCase()}</span>
                <div class="car-card-actions">${actions}</div>
            </div>
        </div>
    `;
}

window.cambiarEstado = (id, nuevoEstado) => {
    const coche = dbCoches.find(c => c.id === id);
    if(coche) {
        coche.estado = nuevoEstado;
        cargarPanelAdmin();
    }
};

window.guardarPalabrasProhibidas = function() {
    const textarea = document.getElementById("admin-banned-words").value;
    bannedWords = textarea.split(",").map(word => word.trim()).filter(w => w !== "");
    guardarDatos();
    alert("Lista de palabras bloqueadas actualizada correctamente.");
}