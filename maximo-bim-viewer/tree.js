
class TreeNode {

    paddingSize= 5;

    constructor(data, imagePath, getLabel, getChildren, onClick, onDoubleClick, lazyLoading, indent) {
        this._data = data;
        this._imagePath = imagePath;
        this._indent = indent;
        this.getLabel = getLabel;
        this._onClick = onClick;
        this._onDoubleClick = onDoubleClick;
        this._lazyLoading = lazyLoading;
        this._getChildren = getChildren;

        this.children = [];

        this._children = this._getChildren(this._data);
        this._childCount = this._children.length;
        this._loaded = false;

        if ( this._data && getChildren ) {
            if ( !lazyLoading ) {        
                this._children.forEach( (elt) => this.children.push(new TreeNode(elt, imagePath, getLabel, getChildren, onClick, onDoubleClick, lazyLoading, indent + 1)));
                delete this._children;
                this._loaded = true;
            }
        } else {
            this._childCount = 0;
        }
    }

    render() {
        var node = document.createElement("div");
        node.classList.add("treeNode");
        if ( this._indent > 0 )
            node.setAttribute("indent", "");
        //node.style.paddingLeft = (this.paddingSize * this._indent) + "px";
        if ( this._data["expressID"] ) {
            node.setAttribute("data-id", this._data["expressID"]);
        }
        //node.setAttribute("expanded", "");
        const lbl = this.getLabel(this._data);

        node.innerHTML = `
                <div class="treeNodeHeader">
                    <div class="toggletreeNode" data-nb-child="${this._childCount}">
                        <img style="width:12px" src="${this._imagePath}/node.svg"/>
                    </div>
                    <div class="label"></div>
                </div>
                <div class="children"></div>`;//${this.children.map( elt => { return elt.render()})}</div>`;
        if ( lbl instanceof Element ) {
            node.querySelector(".label").appendChild(lbl);
        } else if ( lbl instanceof Promise) {
            lbl.then( (res) => node.querySelector(".label").innerHTML = res);
        
        } else {
            node.querySelector(".label").innerHTML = lbl;
        }

        node.querySelector(".toggletreeNode").addEventListener("click", (event) => this._toggleExpand(node));
        if (  this._onClick ) {
            node.querySelector(".label").addEventListener( "click", (event) => { 
                node.dispatchEvent(new CustomEvent("selectionChanged", { bubbles: true, detail: node }));
                this._onClick(this._data);
            });
            //node.querySelector(".label").addEventListener( "click", (event) => { });
        }
        if ( this._onDoubleClick ) {
            node.querySelector(".label").addEventListener( "dblclick", (event) => { 
                node.dispatchEvent(new CustomEvent("selectionChanged", { bubbles: true, detail: node }));
                this._onDoubleClick(this._data);
            });
            //node.querySelector(".label").addEventListener( "dblclick", (event) => { });
        }

        if ( !this._lazyLoading ) {
            const childrenContainer = node.querySelector(".children");
            this.children.forEach( elt => childrenContainer.appendChild(elt.render()));
        }
        return node;
    }

    _toggleExpand(node) {
        node.toggleAttribute("expanded");
        if ( this._loaded ) return;
        const workingIndicator = document.createElement("img");
        workingIndicator.classList.add("spinMe");
        workingIndicator.src = this._imagePath + "/loader.svg";
        node.querySelector(".children").appendChild(workingIndicator);
        this._children.forEach( (elt) => this.children.push(new TreeNode(elt, this._imagePath, this.getLabel, this._getChildren, this._onClick, this._onDoubleClick, this._lazyLoading, this._indent + 1)));
        delete this._children;
        workingIndicator.remove();
        const childrenContainer = node.querySelector(".children");
        this.children.forEach( elt => {
            childrenContainer.appendChild(elt.render());
        });
        this._loaded = true;

    }

}

export class Tree {

    constructor(options) {

        if ( options.attributeName ) {
            this._getLabel = (elt) => elt[options.attributeName];
        } else {
           this._getLabel = options.getLabel; 
        }
        if ( options.getChildren ) {
            this._getChildren = options.getChildren;
        } else {
            this._getChildren = (elt) => elt.children;
        }
        this._imagePath = options.imagePath || "./";
        this._onClick = options.onClick;
        this._onDoubleClick = options.onDoubleClick;
        this.container = options.container;
        this._data = options.data || null;
        this._lazyLoading = options.lazyLoading || true;
        this._treeNodes = [];
        this._refreshTree();
        this.container.addEventListener("selectionChanged", (event) => {
            event.stopPropagation();
            this.container.querySelectorAll(".treeNode[selected]").forEach( (node) => node.removeAttribute("selected"));
            event.detail.setAttribute("selected","");
        });
    }


    get data() {
        return this._data;
    }

    set data(data) {
        this._data = data;
        this._treeNodes = [];
        this._refreshTree();
    }

    selectNodeById(id) {
        const res = this.container.querySelector(".treeNode[data-id=\"" + id + "\"] .label");
        if ( res ) {
            res.click();
            let tmp = res;
            let path = [tmp];
            while( tmp != this.container) {
                path.push(tmp.parentNode);
                tmp = tmp.parentNode;
            }
            path = path.filter( (elt) => elt.classList.contains("treeNode") && !elt.hasAttribute("expanded"));
            path.forEach( treeNode => treeNode.querySelector(".toggletreeNode").click());
            res.parentNode.parentNode.scrollIntoView({behavior: "smooth", block:"center"});
        } else {
            const buildPathFor = function(data, getChildren, id) {
                if ( data.expressID == id ) return [data.expressID];
                else {
                    for(const children of getChildren(data)||[]) {
                        const r = buildPathFor(children,getChildren, id);
                        if ( r.length > 0 ) {
                            return [data.expressID, ...r];
                        }
                    }
                    return [];
                }
                    
            }
            
            const path = buildPathFor(this._data[0], this._getChildren, id);
            let node;
            while(path.length > 0) {
                node = this.container.querySelector(".treeNode[data-id=\"" + path[0] + "\"] .toggletreeNode");
                console.log("Looking for node " + path[0] + " (found : " + node + ")");
                if ( !node.parentNode.parentNode.hasAttribute("expanded") ) {
                    node.click();
                }
                path.shift();
            }

            const treeNode = node.parentNode.parentNode
            treeNode.scrollIntoView({behavior: "smooth", block:"center"});
            treeNode.querySelector(".label").click();
        }
    }

    _refreshTree() {
        this.container.innerHTML = "";
        this.container.classList.add("treeContainer");

        if (  this._data ) {
            if ( !(this._data instanceof Array) ) this._data = [this._data];
            this._data.forEach( (elt) => {
                this._treeNodes.push(new TreeNode(elt, this._imagePath, this._getLabel, this._getChildren, this._onClick, this._onDoubleClick, this._lazyLoading, 0));
            }); 
        }

        this.container.innerHTML = "";
        // const res = this._treeNodes.map( elt => {return elt.render()}).join("");
        // this.container.innerHTML = res;
        this._treeNodes.forEach( elt => this.container.appendChild(elt.render()));

    }
}