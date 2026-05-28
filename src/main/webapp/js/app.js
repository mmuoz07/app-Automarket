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
let dbChats = JSON.parse(localStorage.getItem('autoMarketChatsDb')) || [];

let usuarioActual = "Invitado";
let idCocheEditando = null;
let chatActualCon = null;

function guardarDatos() {
    localStorage.setItem('autoMarketDb', JSON.stringify(dbCoches));
    localStorage.setItem('bannedWordsDb', JSON.stringify(bannedWords));
    localStorage.setItem('autoMarketChatsDb', JSON.stringify(dbChats));
}

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
        const busquedaTexto = (c.marca + " " + c.modelo + " " + (c.ciudad || c.ubicacion || "")).toLowerCase();
        const matchQuery = busquedaTexto.includes(query);
        const matchFuel = fuel === "all" || c.motor === fuel || c.combustible === fuel;
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
                <img src="${(c.imgs && c.imgs.length > 0) ? c.imgs[0] : 'https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80'}" alt="${c.marca} ${c.modelo}">
                <span class="price-tag">€${c.precio}</span>
            </div>
            <div class="car-info">
                <h3>${c.marca} ${c.modelo}</h3>
                <div class="car-specs">
                    <span><i class="far fa-calendar-alt"></i> ${c.ano}</span>
                    <span><i class="fas fa-tachometer-alt"></i> ${c.km} km</span>
                    <span><i class="fas fa-gas-pump"></i> ${c.motor || c.combustible}</span>
                    <span><i class="fas fa-map-marker-alt"></i> ${c.ciudad || c.ubicacion || 'No especificada'}</span>
                </div>
                <div class="car-footer">Vendedor: ${c.vendedor}</div>
            </div>
        </div>
    `).join('');
}

document.addEventListener("DOMContentLoaded", () => {
    // CAMBIO: Al arrancar, comprobamos si ya había una sesión guardada del usuario en el navegador
    const sesionGuardada = sessionStorage.getItem('sesionUsuarioMarket');
    if (sesionGuardada) {
        loguearUsuarioEnCliente(sesionGuardada, sesionGuardada.toLowerCase() === "admin" ? "ADMIN" : "USER");
    } else {
        cargarCochesInicio();
    }

    document.getElementById("main-search").addEventListener("input", cargarCochesInicio);
    document.getElementById("filter-fuel").addEventListener("change", cargarCochesInicio);
    document.getElementById("filter-year").addEventListener("change", cargarCochesInicio);

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

    function loguearUsuarioEnCliente(username, rol) {
        usuarioActual = username;
        // CAMBIO: Guardamos el usuario en sessionStorage para que no se pierda al navegar
        sessionStorage.setItem('sesionUsuarioMarket', username);

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
        mostrarSeccion('inicio');
    }

    document.getElementById("btn-logout").onclick = () => {
        usuarioActual = "Invitado";
        // CAMBIO: Eliminamos al usuario de la memoria al cerrar sesión
        sessionStorage.removeItem('sesionUsuarioMarket');

        document.getElementById("nav-btn-login").classList.remove("hidden");
        document.getElementById("nav-user-profile").classList.add("hidden");
        document.getElementById("nav-mis-coches").classList.add("hidden");
        document.getElementById("nav-chats").classList.add("hidden");
        document.getElementById("nav-btn-publicar").classList.add("hidden");
        document.getElementById("nav-panel-admin").classList.add("hidden");
        mostrarSeccion('inicio');
    };

    document.getElementById("login-form").onsubmit = async (e) => {
        e.preventDefault();
        const userVal = document.getElementById("login-user").value;
        const passVal = document.getElementById("login-pass") ? document.getElementById("login-pass").value : "";

        try {
            // CAMBIO: Ruta de fetch relativa para compatibilidad Localhost/Railway
            const response = await fetch("api/login-usuario", {
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
            loguearUsuarioEnCliente(userVal, "USER");
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
            // CAMBIO: Ruta de fetch relativa sin la barra '/' inicial
            const response = await fetch("api/registrar-usuario", {
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
                loguearUsuarioEnCliente(usernameVal, data.user || "USER");
            } else {
                alert(data.mensaje || "El usuario ya existe.");
            }
        } catch (err) {
            console.error("Error en registro:", err);
            alert("No se pudo conectar con el servidor de base de datos.");
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

        const nuevoCoche = {
            estado: "pendiente",
            marca: document.getElementById("pub-marca").value,
            modelo: document.getElementById("pub-modelo").value,
            ubicacion: document.getElementById("pub-ciudad").value,
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
            nuevoCoche.id = idCocheEditando;
        }

        try {
            const endpoint = idCocheEditando !== null ? "api/modificar-coche" : "api/crear-coches";
            
            const response = await fetch(endpoint, {
                method: "POST",
                headers: { "Content-Type": "application/json; charset=UTF-8" },
                body: JSON.stringify(nuevoCoche)
            });
            const data = await response.json();

            if (response.ok && data.ok) {
                alert("Vehículo procesado correctamente en el servidor remoto.");
                const cocheProcesado = data.resultados ? data.resultados : nuevoCoche;
                
                if (idCocheEditando !== null) {
                    const index = dbCoches.findIndex(c => c.id === idCocheEditando);
                    if (index !== -1) dbCoches[index] = cocheProcesado;
                } else {
                    dbCoches.push(cocheProcesado);
                }
                guardarDatos();
                cancelarEdicion();
                mostrarSeccion('mis-coches');
            } else {
                alert(data.mensaje || "Error del servidor al procesar el coche.");
            }
        } catch (err) {
            console.error("Error al conectar con el Backend:", err);
            alert("No se pudo conectar con el servidor. Se guardará en modo local temporal.");
            
            if (idCocheEditando !== null) {
                const index = dbCoches.findIndex(c => c.id === idCocheEditando);
                if (index !== -1) dbCoches[index] = nuevoCoche;
            } else {
                nuevoCoche.id = Date.now();
                dbCoches.push(nuevoCoche);
            }
            guardarDatos();
            cancelarEdicion();
            mostrarSeccion('mis-coches');
        }
    };
});

window.cancelarEdicion = function() {
    idCocheEditando = null;
    document.getElementById("form-publicar").reset();
    mostrarSeccion('inicio');
}

window.abrirDetalle = function(idBuscado) {
    const c = dbCoches.find(item => item.id === idBuscado);
    if (!c) return;
    
    let mainImg = `<img src="${(c.imgs && c.imgs.length > 0) ? c.imgs[0] : ''}" style="width:100%; border-radius:1rem;">`;
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
                <p class="ubicacion"><i class="fas fa-map-marker-alt"></i> ${c.ciudad || c.ubicacion || 'No especificada'}</p>
                <div class="specs-grid">
                    <div class="spec-box"><span>Año</span><strong>${c.ano}</strong></div>
                    <div class="spec-box"><span>Kilómetros</span><strong>${c.km}</strong></div>
                    <div class="spec-box"><span>Motor</span><strong>${c.motor || c.combustible}</strong></div>
                    <div class="spec-box"><span>Cambio</span><strong>${c.transmision || 'No especificado'}</strong></div>
                </div>
                <div class="desc-box"><h3>Descripción</h3><p>${c.desc || c.descripcion || ''}</p></div>
                <div class="vendedor-box">
                    <div class="vendedor-nombre">${c.vendedor || 'Particular'}</div>
                    <button class="btn-dark-full" onclick="abrirChat('${c.vendedor || 'Particular'}')">Enviar Mensaje</button>
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
        fetch('api/eliminar-coche', {
            method: "POST",
            headers: { "Content-Type": "application/json; charset=UTF-8" },
            body: JSON.stringify({ id: id })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error("Fallo en la comunicación con el servlet.");
            }
            return response.json();
        })
        .then(data => {
            if (data.ok) {
                alert(data.mensaje || "Eliminado de la base de datos MySQL.");
                dbCoches = dbCoches.filter(c => c.id !== id);
                guardarDatos();
                cargarMisCoches();
            } else {
                alert("Error de validación: " + data.mensaje);
            }
        })
        .catch(err => {
            console.error("Fallo al eliminar:", err);
            alert("No se pudo conectar con el backend. Eliminando en modo local.");
            dbCoches = dbCoches.filter(c => c.id !== id);
            guardarDatos();
            cargarMisCoches();
        });
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

// NUEVA FUNCIÓN: Mapea los datos del vehículo y los carga en los inputs para la sección de edición (U)
window.abrirEditarCoche = function(idRealBBDD) {
    idCocheEditando = idRealBBDD;
    const coche = dbCoches.find(c => c.id === idRealBBDD);
    
    if (!coche) {
        alert("No se han podido mapear los datos del vehículo.");
        return;
    }
    
    document.getElementById("pub-marca").value = coche.marca;
    document.getElementById("pub-modelo").value = coche.modelo;
    document.getElementById("pub-ciudad").value = coche.ciudad || coche.ubicacion || "";
    document.getElementById("pub-ano").value = coche.ano;
    document.getElementById("pub-precio").value = coche.precio;
    document.getElementById("pub-km").value = coche.km;
    document.getElementById("pub-motor").value = coche.motor || coche.combustible || "";
    document.getElementById("pub-transmision").value = coche.transmision || "";
    document.getElementById("pub-desc").value = coche.desc || coche.descripcion || "";
    
    mostrarSeccion('publicar');
};

function renderCarHorizontal(c, isPropietario = false, isAdmin = false) {
    let actions = "";
    
    // Mapeo seguro de la imagen desde el array de la base de datos relacional
    const imagenUrl = (c.imgs && c.imgs.length > 0) ? c.imgs[0] : 'https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80';
    
    // CAMBIO: Agregado el botón "Editar" condicional visible únicamente para el propietario que publicó el coche
    if(isPropietario) {
        actions = `
            <button class="btn-action btn-edit" onclick="abrirEditarCoche(${c.id})" style="margin-right: 8px; cursor: pointer;">
                <i class="fas fa-edit"></i> Editar
            </button>
            <button class="btn-action btn-reject" onclick="eliminarCoche(${c.id})">Eliminar</button>
        `;
    }
    if(isAdmin) {
        if(c.estado !== 'aprobado') actions += `<button class="btn-action btn-approve" onclick="cambiarEstado(${c.id}, 'aprobado')">Aprobar</button>`;
        if(c.estado !== 'rechazado') actions += `<button class="btn-action btn-reject" onclick="cambiarEstado(${c.id}, 'rechazado')">Rechazar (Imágenes/Datos)</button>`;
    }

    // CAMBIO: Manejo seguro de atributos de texto con operadores lógicos de respaldo (Evita el 'undefined')
    const textoAno = c.ano || '2024';
    const textoKm = c.km || '0';
    const textoMotor = c.motor || c.combustible || 'Gasolina';
    const textoCambio = c.transmision || 'No especificado';

    return `
        <div class="car-card-horizontal">
            <img src="${imagenUrl}" alt="${c.marca || 'Vehículo'}">
            <div class="car-card-content">
                <div class="car-header-flex">
                    <h3>${c.marca || ''} ${c.modelo || ''}</h3>
                    <span class="car-price-admin">€${c.precio || '0'}</span>
                </div>
                <p style="color: #666; margin-bottom:10px; font-size: 0.9rem;">${textoAno} • ${textoKm} km • ${textoMotor} • Cambio: ${textoCambio}</p>
                ${isAdmin ? `<p style="color: #666; margin-bottom:10px; font-size: 0.9rem;">Vendedor: ${c.vendedor || 'Particular'}</p>` : ''}
                <span class="status-badge status-${c.estado || 'pendiente'}">${(c.estado || 'pendiente').toUpperCase()}</span>
                <div class="car-card-actions">${actions}</div>
            </div>
        </div>
    `;
}

window.cambiarEstado = (id, nuevoEstado) => {
    const coche = dbCoches.find(c => c.id === id);
    if(coche) {
        const cocheActualizado = { ...coche, estado: nuevoEstado, desc: coche.desc || coche.descripcion, motor: coche.motor || coche.combustible, ubicacion: coche.ciudad || coche.ubicacion };
        
        fetch('api/modificar-coche', {
            method: "POST",
            headers: { "Content-Type": "application/json; charset=UTF-8" },
            body: JSON.stringify(cocheActualizado)
        })
        .then(response => response.json())
        .then(data => {
            if(data.ok) {
                coche.estado = nuevoEstado;
                guardarDatos();
                cargarPanelAdmin();
            } else {
                alert("El servidor denegó la actualización: " + data.mensaje);
            }
        })
        .catch(err => {
            console.error("Error al actualizar estado en backend:", err);
            coche.estado = nuevoEstado;
            guardarDatos();
            cargarPanelAdmin();
        });
    }
};

window.guardarPalabrasProhibidas = function() {
    const textarea = document.getElementById("admin-banned-words").value;
    bannedWords = textarea.split(",").map(word => word.trim()).filter(w => w !== "");
    guardarDatos();
    alert("Lista de palabras bloqueadas actualizada correctamente.");
}