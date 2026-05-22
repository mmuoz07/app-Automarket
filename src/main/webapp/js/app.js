//==========================================
// 1. BASE DE DATOS Y PERSISTENCIA
// ==========================================
const cochesPorDefecto = [
    {
        id: 0, estado: "aprobado", marca: "BMW", modelo: "Serie 3", precio: "28500", km: "45000", ano: 2020,
        motor: "Diésel", transmision: "Automática", ciudad: "Madrid",
        imgs: ["https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80"],
        desc: "BMW Serie 3 en excelente estado.", vendedor: "Usuario Demo"
    },
    {
        id: 1, estado: "aprobado", marca: "Mercedes", modelo: "Clase A", precio: "32000", km: "28000", ano: 2021,
        motor: "Gasolina", transmision: "Automática", ciudad: "Barcelona",
        imgs: ["https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?auto=format&fit=crop&w=800&q=80"],
        desc: "Vehículo impecable.", vendedor: "Carlos Ruiz"
    },
    {
        id: 2, estado: "pendiente", marca: "Audi", modelo: "A4", precio: "26000", km: "62000", ano: 2019,
        motor: "Diésel", transmision: "Manual", ciudad: "Valencia",
        imgs: ["https://images.unsplash.com/photo-1606152421802-db97b9c7a11b?auto=format&fit=crop&w=800&q=80"],
        desc: "Mantenimiento al día.", vendedor: "Ana Martínez"
    }
];

let dbCoches = JSON.parse(localStorage.getItem('autoMarketDb')) || cochesPorDefecto;
let bannedWords = JSON.parse(localStorage.getItem('bannedWordsDb')) || ["estafa", "tonto", "idiota"];
let dbChats = JSON.parse(localStorage.getItem('autoMarketChatsDb')) || []; // NUEVA: Base de datos de chats

let usuarioActual = "Invitado";
let idCocheEditando = null;
let chatActualCon = null; // Variable para saber con quién estamos chateando actualmente

function guardarDatos() {
    localStorage.setItem('autoMarketDb', JSON.stringify(dbCoches));
    localStorage.setItem('bannedWordsDb', JSON.stringify(bannedWords));
    localStorage.setItem('autoMarketChatsDb', JSON.stringify(dbChats)); // Guardar los chats
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

function cargarCochesInicio() {
    const grid = document.getElementById("grid-cars");
    const countText = document.getElementById("inicio-resultados-count");
    if (!grid) return;

    const mainSearch = document.getElementById("main-search");
    const filterFuel = document.getElementById("filter-fuel");
    const filterYear = document.getElementById("filter-year");

    const query = mainSearch ? mainSearch.value.toLowerCase() : "";
    const fuel = filterFuel ? filterFuel.value : "all";
    const yearMin = filterYear ? parseInt(filterYear.value) : 0;

    const filtrados = dbCoches.filter(c => {
        const matchEstado = c.estado === "aprobado";
        const busquedaTexto = (c.marca + " " + c.modelo + " " + c.ciudad).toLowerCase();
        const matchQuery = busquedaTexto.includes(query);
        const matchFuel = fuel === "all" || c.motor === fuel;
        const matchYear = c.ano >= yearMin;
        return matchEstado && matchQuery && matchFuel && matchYear;
    });

    if (countText) countText.innerText = `${filtrados.length} coches encontrados`;

    if (filtrados.length === 0) {
        grid.innerHTML = "<p style='grid-column: 1/-1; text-align: center; padding:2rem; color: #666;'>No se han encontrado coches.</p>";
        return;
    }

    grid.innerHTML = filtrados.map(c => `
        <div class="car-card car-card-clickable" onclick="abrirDetalle(${c.id})">
            <div class="car-image-container">
                <img src="${c.imgs[0]}" alt="${c.marca} ${c.modelo}">
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
    `).join('');
}

// ==========================================
// 3. INICIALIZACIÓN Y AUTH (CONECTADO A BASE DE DATOS Y AUTOMÁTICO)
// ==========================================
document.addEventListener("DOMContentLoaded", () => {
    cargarCochesInicio();

    document.getElementById("main-search").addEventListener("input", cargarCochesInicio);
    document.getElementById("filter-fuel").addEventListener("change", cargarCochesInicio);
    document.getElementById("filter-year").addEventListener("change", cargarCochesInicio);

    // Modal
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

    // Función auxiliar para aplicar el inicio de sesión visualmente en la interfaz
    function loguearUsuarioEnCliente(username, rol) {
        usuarioActual = username;
        document.getElementById("auth-modal").classList.add("hidden");

        document.getElementById("nav-btn-login").classList.add("hidden");
        document.getElementById("nav-user-profile").classList.remove("hidden");
        document.getElementById("nav-mis-coches").classList.remove("hidden");
        document.getElementById("nav-chats").classList.remove("hidden");
        document.getElementById("nav-btn-publicar").classList.remove("hidden");
        document.getElementById("user-name-display").innerText = usuarioActual;

        // Mostrar Panel Admin a admin según el rol que devuelve la base de datos
        if (rol === "ADMIN" || username.toLowerCase() === "admin") {
            document.getElementById("nav-panel-admin").classList.remove("hidden");
        }
        mostrarSeccion('inicio');
    }

    // Login conectado a Base de Datos
    document.getElementById("login-form").onsubmit = async (e) => {
        e.preventDefault();
        const userVal = document.getElementById("login-user").value;
        const passVal = document.getElementById("login-pass") ? document.getElementById("login-pass").value : "";

        try {
            // Reemplaza con tu URL real de Railway si pruebas directamente en producción
            const response = await fetch("/api/login-usuario", {
                method: "POST",
                headers: { "Content-Type": "application/json; charset=UTF-8" },
                body: JSON.stringify({ username: userVal, password: passVal })
            });

            const data = await response.json();

            if (response.ok && data.ok) {
                loguearUsuarioEnCliente(userVal, data.rol);
            } else {
                alert(data.mensaje || "Error al iniciar sesión.");
            }
        } catch (err) {
            console.error("Error en login:", err);
            // Fallback por si la API aún no está disponible en entorno local
            loguearUsuarioEnCliente(userVal, "USER");
        }
    };

    // NUEVO: Formulario de Registro Conectado a Base de Datos con Login Automático
    document.getElementById("register-form").onsubmit = async (e) => {
        e.preventDefault();
        const userVal = document.getElementById("register-user").value;
        const emailVal = document.getElementById("register-email") ? document.getElementById("register-email").value : "";
        const passVal = document.getElementById("register-pass") ? document.getElementById("register-pass").value : "";

        try {
            // Endpoint de tu Servlet en Railway
            const response = await fetch("/api/registrar-usuario", {
                method: "POST",
                headers: { "Content-Type": "application/json; charset=UTF-8" },
                body: JSON.stringify({ username: userVal, email: emailVal, password: passVal })
            });

            const data = await response.json();

            if (response.ok && data.ok) {
                alert(data.mensaje); // "¡Cuenta creada y sesión iniciada!"
                loguearUsuarioEnCliente(userVal, data.rol || "USER");
            } else {
                alert(data.mensaje || "El usuario ya existe.");
            }
        } catch (err) {
            console.error("Error en registro:", err);
            alert("No se pudo conectar con el servidor de base de datos.");
        }
    };

    document.getElementById("btn-logout").onclick = () => {
        usuarioActual = "Invitado";
        location.reload();
    };

    // PUBLICAR COCHE
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

        const nuevoCoche = {
            id: idCocheEditando || Date.now(),
            estado: "pendiente",
            marca: document.getElementById("pub-marca").value,
            modelo: document.getElementById("pub-modelo").value,
            ciudad: document.getElementById("pub-ciudad").value,
            ano: parseInt(document.getElementById("pub-ano").value),
            precio: document.getElementById("pub-precio").value,
            km: document.getElementById("pub-km").value,
            motor: document.getElementById("pub-motor").value,
            transmision: document.getElementById("pub-transmision").value,
            imgs: imagenesCargadas,
            desc: document.getElementById("pub-desc").value,
            vendedor: usuarioActual
        };

        if (idCocheEditando !== null) {
            const index = dbCoches.findIndex(c => c.id === idCocheEditando);
            if (index !== -1) dbCoches[index] = nuevoCoche;
        } else {
            dbCoches.push(nuevoCoche);
        }
        
        guardarDatos();
        cancelarEdicion();
        mostrarSeccion('mis-coches');
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
    
    let mainImg = `<img src="${c.imgs[0]}" style="width:100%; border-radius:1rem;">`;
    let restImgs = c.imgs.length > 1 ? `<div class="multi-img-grid">` + c.imgs.slice(1).map(i => `<img src="${i}">`).join('') + `</div>` : '';

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
                <div class="desc-box"><h3>Descripción</h3><p>${c.desc}</p></div>
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

// Cargar Lista de Chats Reales
window.cargarListaChats = function() {
    const container = document.getElementById("lista-chats-container");
    
    // Buscar con quién hemos chateado
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
        // Encontrar el último mensaje para mostrarlo en la previsualización
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
    chatActualCon = nombre; // Establecemos con quién estamos hablando
    document.getElementById('chat-seller-name').innerText = nombre;
    
    const chatMsg = document.getElementById('chat-messages');
    chatMsg.innerHTML = ``; // Limpiamos la caja

    // Cargar historial de base de datos
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
    
    // Filtro de Palabras Prohibidas
    bannedWords.forEach(word => {
        const regex = new RegExp(`\\b${word.trim()}\\b`, 'gi');
        msj = msj.replace(regex, '***');
    });

    // Guardar en la Base de Datos
    const nuevoMensaje = {
        sender: usuarioActual,
        receiver: chatActualCon,
        text: msj,
        timestamp: Date.now()
    };
    dbChats.push(nuevoMensaje);
    guardarDatos(); // Guardamos los cambios

    // Mostrar en pantalla
    chatMsg.innerHTML += `<div class="message sent"><p>${msj}</p></div>`;
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
        guardarDatos();
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

    return `
        <div class="car-card-horizontal">
            <img src="${c.imgs[0]}">
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
        guardarDatos();
        cargarPanelAdmin();
    }
};

window.guardarPalabrasProhibidas = function() {
    const textarea = document.getElementById("admin-banned-words").value;
    bannedWords = textarea.split(",").map(word => word.trim()).filter(w => w !== "");
    guardarDatos();
    alert("Lista de palabras bloqueadas actualizada correctamente.");
}