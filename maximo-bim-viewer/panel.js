export function createPanel(className, title, footerActions, closeable = false, moveable = false) {
    const panel = document.createElement("div");
    panel.classList.add("panel", className);
    panel.setAttribute("hidden", "");

    panel.addEventListener("mousedown", (event) => {
        panel.parentElement.querySelectorAll(".panel").forEach( panel => panel.style.removeProperty("z-index"));
        panel.style.zIndex = 100;
    });

    panel.innerHTML = `
        <div class="header">${title}</div>
        <div class="container"></div>
        <div class="footer"></div>`;
    
    const footer = panel.querySelector(".footer");
    (footerActions || []).forEach( (action) => {
        const act = document.createElement("button");
        if ( action.className ) act.classList.add(action.className);
        if ( action.title ) act.innerText = action.title;
        if ( action.image ) {
            const img = document.createElement("img");
            img.src = action.image;
            act.appendChild(img);
        }
        if ( action.onClick ) act.addEventListener("click", action.onClick);
        footer.appendChild(act);
    });

    if ( closeable ) {
        const spacer = document.createElement("div");
        const closeButton = document.createElement("button");
        spacer.classList.add("spacer");
        closeButton.classList.add("closeButton");
        closeButton.innerText = "Close";

        footer.appendChild(spacer);
        footer.appendChild(closeButton);

        closeButton.addEventListener("click", (event) => {
            panel.setAttribute("hidden","");
        });
    }

    if ( moveable ) {
        makeMeMovable(panel, panel.querySelector(".header"));
    }

    return panel;
    
}

export function makeMeMovable(container, anchor) {

    var diffX;
    var diffY;

    var listener = (event) => {
        event.preventDefault();
        container.style.left = (event.clientX - diffX) + "px";
        container.style.top = (event.clientY - diffY) + "px";
    };

    var listener2 = (event) => {
        window.removeEventListener("mousemove", listener);
        window.removeEventListener("mouseup", listener2);
    }

    anchor.classList.add("movableAnchor");
    anchor.addEventListener("mousedown", (event) => {
        event.preventDefault();

        var marginLeft = parseInt(window.getComputedStyle(container).marginLeft);
        var marginTop = parseInt(window.getComputedStyle(container).marginTop);
        

        diffX = event.clientX - container.offsetLeft + marginLeft;
        diffY = event.clientY - container.offsetTop + marginTop;

        
        window.addEventListener("mousemove", listener);
        window.addEventListener("mouseup", listener2);
    });

    container._movableListeners = {"mousemove": listener, "mouseup": listener2};
   
}

export function makeMeUnmovable(container) {
    if ( container._movableListeners === undefined || !(container._movableListeners instanceof Array)) return;
    Object.keys(container._movableListeners).forEach( (event) => window.removeEventListener(event, container._movableListeners[event]));
}