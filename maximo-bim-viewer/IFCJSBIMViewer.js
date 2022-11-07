// Initial dependencies
//"web-ifc": "0.0.36",
    //"web-ifc-three": "^0.0.121",
    //"web-ifc-viewer": "1.0.213",

import { CameraProjections, IfcViewerAPI, NavigationModes } from 'web-ifc-viewer';
import { Box3, Sphere, Vector3,MeshBasicMaterial, LineBasicMaterial, Color, EdgesGeometry } from 'three';
//import * as THREE from 'three';
import {acceleratedRaycast, computeBoundsTree, disposeBoundsTree} from 'three-mesh-bvh';
import { Tree } from './tree';
import { createPanel } from './panel';
//import * as THREE from 'three';
import {
  IFCSPACE,
  IFCOPENINGELEMENT,
  IFCWALLSTANDARDCASE,
  IFCWALL,
  IFCSTAIR,
  IFCCOLUMN,
  IFCSLAB,
  IFCROOF,
  IFCCOVERING,

  // IFCFOOTING,
  // IFCFURNISHINGELEMENT
  // IFCSTAIRFLIGHT,
  // IFCRAILING
} from 'web-ifc';
import { jsPDF } from 'jspdf';
import { toIfcGuid, fromIfcGuid } from './IfcGuidConverter';
import { mapping as iconMapping } from './ifc-full-icons';
import { labels as textMapping } from './ifc-types-labels';
import { IFCTYPES_TO_STRING as IfcTypesMap } from './ifc-types-to-string';
//import { IfcTypesMap } from 'web-ifc/helpers';

import { exportPlanToDxf } from './exportPlan';

const lineMaterial = new LineBasicMaterial({ color: 0x555555 });
const baseMaterial = new MeshBasicMaterial({ color: 0xffffff, side: 2 });

export class IFCJSBIMViewer extends IfcViewerAPI {

  listeners = {};
  //ifcModels = [];
  _toolbarItems = [];
  _selectionProperties = [];
  _loadingMessage = "";
  fills = [];
  autoZoom = true;
  counter = 0;

  javascriptPath = "";
  imagePath = "";
  wasmPath = "";

  constructor( params ) {
    if  ( !params.backgroundColor ) params.backgroundColor = new Color(255, 255, 255);
    super(params);

    //const mapDiv = params.container;
    //map = new google.maps.Map(mapDiv, mapOptions);

    //const scene = this.IFC.getScene();
    // this.mpOverlay = new ThreeJSOverlayView({
    //   map,
    //   scene,
    //   anchor: { ...mapOptions.center, altitude: 100 },
    //   THREE,
    // });

    this.javascriptPath = params.javascriptPath || "./";
    this.imagePath = params.imagePath || "./";
    this.wasmPath = params.wasmPath || "./";

    if ( !this.imagePath.endsWith("/") ) this.imagePath = this.imagePath + "/";
    if ( !this.javascriptPath.endsWith("/") ) this.javascriptPath = this.javascriptPath + "/";
    if ( !this.wasmPath.endsWith("/") ) this.wasmPath = this.wasmPath + "/";

    this.useWebWorker = (params.useWebWorker === undefined ? true : params.useWebWorker);

    this.toIfcGuid = toIfcGuid;
    this.fromIfcGuid = fromIfcGuid;
    
    this.context.getContainerElement().classList.add("bimViewer");

    this.toolbarContainer = params.toolbarContainer;
    if ( this.toolbarContainer === undefined || this.toolbarContainer === null ) {
      this.toolbarContainer = createPanel("toolbar", "",[], false, false);
      this.context.getContainerElement().appendChild(this.toolbarContainer);
      this.toolbarContainer.removeAttribute("hidden");
      this.setToolbarDirection(params.toolbarDirection || "vertical");
    }

    this.panelsContainer = params.panelsContainer;
    if ( this.panelsContainer === undefined || this.panelsContainer === null ) {
      this.panelsContainer = this.context.getContainerElement();
    }

    this.listeners["selection"] = [];
    this.listeners["fileLoad"] = [];
    this.loadedFiles = [];

    this.IFC.loader.ifcManager.applyWebIfcConfig({
      COORDINATE_TO_ORIGIN: true,
      USE_FAST_BOOLS: true
    });

    this.IFC.loader.ifcManager.setupThreeMeshBVH(
      computeBoundsTree,
      disposeBoundsTree,
      acceleratedRaycast
  );

    // this.context.getContainerElement().style.position = "relative";
    this._prepareToolbar();
    
    
    this.IFC.loader.ifcManager.parser.setupOptionalCategories({
      [IFCSPACE]: false,
      [IFCOPENINGELEMENT]: false
    });
    
    
    //
    if ( this.useWebWorker ) {
      this.IFC.loader.ifcManager.useWebWorkers(true, this.javascriptPath + "IFCWorker.js");
    }
    this.IFC.setWasmPath(this.wasmPath);

    //this.IFC.loader.ifcManager.useFragments = true;
    //this.IFC.setWasmPath('/build/files/');
    //this.IFC.getIfcCamera().navMode[3] = new OrbitControls();
    // this.IFC.context.getScene().add( THREEViewCubeObject3D());
    this.addFileLoadListener( (event) => {
      const overlay = this._getLoadingOverlay();
      if ( event.event === "fetch" ) {
        if ( event.progress < 100 ) {
          this._loadingMessage = `Downloading in progress (${event.progress}%)`;
        } else {
          this._loadingMessage = "Parsing...";
        }
        overlay.removeAttribute("hidden");
      } else if ( event.event === "loading" ) {
          this._loadingMessage = `Loading model... (${event.progress}%)`;
      } else if ( event.event === "loaded" ) {
        overlay.setAttribute("hidden","");
      }
      this._refreshOverlayMessage();
    });

    window.addEventListener("keydown", async (event) => {
      
      /*************/
      if (event.key === 'f') {
        // viewer.plans.computeAllPlanViews(0);
        await this.plans.computeAllPlanViews(0);
        console.log("Compute all plan views");
    
      }
      if (event.key === 'r') {
        const planNames = Object.keys(this.plans.planLists[0]);
        if (!planNames[this.counter]) return;
        const current = planNames[this.counter];
        this.plans.goTo(0, current, true);
        this.edges.toggle('0');
        console.log("Displaying plane");
      }
      if (event.key === 'a') {
        // PDF export
    
        const currentPlans = this.plans.planLists[0];
        const planNames = Object.keys(currentPlans);
        const firstPlan = planNames[0];
        const currentPlan = this.plans.planLists[0][firstPlan];
    
        const documentName = 'test';
        const doc = new jsPDF('p', 'mm', [1000, 1000]);
        this.pdf.newDocument(documentName, doc, 20);
    
        this.pdf.setLineWidth(documentName, 0.2);
        this.pdf.drawNamedLayer(documentName, currentPlan, 'thick', 200, 200);
    
        this.pdf.setLineWidth(documentName, 0.1);
        this.pdf.setColor(documentName, new Color(100, 100, 100));
    
        const ids = await this.IFC.getAllItemsOfType(0, IFCWALLSTANDARDCASE, false);
        const subset = this.IFC.loader.ifcManager.createSubset({ modelID: 0, ids, removePrevious: true });
        const edgesGeometry = new EdgesGeometry(subset.geometry);
        const vertices = edgesGeometry.attributes.position.array;
        this.pdf.draw(documentName, vertices, 200, 200);
    
        this.pdf.drawNamedLayer(documentName, currentPlan, 'thin', 200, 200);
    
        this.pdf.exportPDF(documentName, 'test.pdf');
        console.log("Exported to pdf");
      }
      // if (event.key === 'b') {
      //   // DXF EXPORT
    
      //   const currentPlans = this.plans.planLists[0];
      //   const planNames = Object.keys(currentPlans);
      //   const firstPlan = planNames[0];
      //   const currentPlan = this.plans.planLists[0][firstPlan];
    
      //   const drawingName = "example";
    
      //   this.dxf.initializeJSPDF(Drawing);
    
      //   this.dxf.newDrawing(drawingName);
      //   this.dxf.drawNamedLayer(drawingName, currentPlan, 'thick', 'section', Drawing.ACI.RED);
      //   this.dxf.drawNamedLayer(drawingName, currentPlan, 'thin', 'projection', Drawing.ACI.GREEN);
    
      //   const ids = await this.IFC.getAllItemsOfType(0, IFCWALLSTANDARDCASE, false);
      //   const subset = this.IFC.loader.ifcManager.createSubset({ modelID: 0, ids, removePrevious: true });
      //   const edgesGeometry = new EdgesGeometry(subset.geometry);
      //   const vertices = edgesGeometry.attributes.position.array;
      //   this.dxf.draw(drawingName, vertices, 'other', Drawing.ACI.BLUE);
    
      //   this.dxf.exportDXF(drawingName);
      //   console.log("Exported to DXF");
      // }
      if (event.key === '+') {
        this.counter++;
        console.log("Next plan");
      }
      if (event.key === '-') {
        this.counter--;
        console.log("Previous plan");
      }
      // if (event.code === 'KeyC') {
      //   // viewer.context.ifcCamera.toggleProjection();
      //   viewer.shadowDropper.renderShadow(0);
      // }
      if (event.key === 'Escape') {
        this.plans.exitPlanView(true);
        this.edges.toggle('0');
        console.log("Escape");
        if ( document.fullscreenElement ) {
          this.exitFullScreen();
        }
      }
      if (event.key === 's') {
        const result = this.context.renderer.newScreenshot(false, 6000, 6000);
        const link = document.createElement('a');
        link.href = result;
        link.download = 'Download.jpg';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        console.log("Rendered as image");
      }
      if(event.key === 'd') {
        this.plans.currentPlan.plane.edges.visible = !this.plans.currentPlan.plane.edges.visible;
        console.log("Toggle edges");
        // const model =
        // viewer.context.ifcCamera.cameraControls.fitTo()
      }
      /*************/
    });

  }

  setToolbarDirection(direction) {
    if ( direction === "vertical" ) {
      this.toolbarContainer.setAttribute("vertical","");
    } else {
      this.toolbarContainer.setAttribute("horizontal","");
    }
  }

  getExpressIdFromGlobalId(gid) {
    const  map = this.IFC.loader.ifcManager.ifcAPI.ifcGuidMap;
    if ( map == undefined || map.size == 0 ) {
      this.IFC.loader.ifcManager.ifcAPI.CreateIfcGuidToExpressIdMapping(this.modelID);
    }
    return map.get(this.modelID).get(gid);
  }


  async createFill(modelID) {
    console.log("Creating fill");
    const wallsStandard = await this.IFC.loader.ifcManager.getAllItemsOfType(modelID, IFCWALLSTANDARDCASE, false);
    const walls = await this.IFC.loader.ifcManager.getAllItemsOfType(modelID, IFCWALL, false);
    const stairs = await this.IFC.loader.ifcManager.getAllItemsOfType(modelID, IFCSTAIR, false);
    const columns = await this.IFC.loader.ifcManager.getAllItemsOfType(modelID, IFCCOLUMN, false);
    const roofs = await this.IFC.loader.ifcManager.getAllItemsOfType(modelID, IFCROOF, false);
    const slabs = await this.IFC.loader.ifcManager.getAllItemsOfType(modelID, IFCSLAB, false);
    const ids = [...walls, ...wallsStandard, ...columns, ...stairs, ...slabs, ...roofs];
    const material = new MeshBasicMaterial({ color: 0x555555 });
    material.polygonOffset = true;
    material.polygonOffsetFactor = 10;
    material.polygonOffsetUnits = 1;
    this.fills.push(this.filler.create(`${modelID}`, modelID, ids, material));
    console.log("Fill created");
  }

  _prepareToolbar() {
    this.addToolbarItem(this._createToolbarItem({ "class": "settingsButton", title: "Settings", image: this.imagePath + "settings.svg", onClick: (event) => {
      this.openSettings();
    }}));
    this.addToolbarItem(this._createToolbarItem({ title: "Toggle selection", class: "toggleSelectionItem", image: this.imagePath + "selection.svg", onClick: (event) => {
      if ( this._selectionEnabled === undefined || !this._selectionEnabled ) {
        this.enableSelection();
      } else {
        this.disableSelection();
      }
    }}));
    this.addToolbarItem(this._createToolbarItem({ title: "Add Clipper", image: this.imagePath + "addClipper.svg", onClick: (event) => {
      event.stopPropagation();
      event.preventDefault();
      const container = this.context.getContainerElement();
      container.style.cursor = "pointer";
      const _handle = (event) => {
        this.clipper.active = true;
        this.clipper.createPlane();
        container.style.cursor = "default";
        container.removeEventListener("click", _handle);
      }
      container.addEventListener("click", _handle); 
    }}));

    this.addToolbarItem(this._createToolbarItem({ title: "Toggle Clipper", image: this.imagePath + "toggleClipper.svg", onClick: (event) => {
      this.clipper.toggle();
      event.stopPropagation();
      event.preventDefault();
    }}));

    this.addToolbarItem(this._createToolbarItem({ title: "Remove Clipper", image: this.imagePath + "removeClipper.svg", onClick: (event) => {
      event.stopPropagation();
      event.preventDefault();
      const container = this.context.getContainerElement();
      container.style.cursor = "pointer";
      const _handle = (event) => {
        this.clipper.deletePlane();
        this.clipper.active = this.clipper.planes.length !== 0;
        this.dimensions.delete();

        container.style.cursor = "default";
        container.removeEventListener("click", _handle);
      }
      container.addEventListener("click", _handle);
    }}));

    this.addToolbarItem(this._createToolbarItem({ title: "Properties", image: this.imagePath + "properties.svg", onClick: (event) => {
      event.stopPropagation();
      event.preventDefault();
      this.openProperties();
    }}));

    this.addToolbarItem(this._createToolbarItem({ title: "Structure", image: this.imagePath + "structure.svg", onClick: (event) => {
      event.stopPropagation();
      event.preventDefault();
      this.openStructure();
    }}));

    this.addToolbarItem(this._createToolbarItem({ title: "Home", image: this.imagePath + "home.svg", onClick: (event) => {
      event.stopPropagation();
      event.preventDefault();
      this.gotoHome();
    }}));

    this.addToolbarItem(this._createToolbarSeparator());

    this.addToolbarItem(this._createToolbarItem({ title: "Snapshot", image: this.imagePath + "snapshot.svg", onClick: (event) => {

    }}));

    this.addToolbarItem(this._createToolbarItem({ title: "Plan", image: this.imagePath + "plan.svg", onClick: async (event) => {
      const overlay = this._getLoadingOverlay();
      setTimeout( () => {
        this._loadingMessage = "";
        this._refreshOverlayMessage();
        overlay.removeAttribute("hidden");
      }, 0);
      
      setTimeout(async () => {
        if ( this.plans.active ) {
          this.closePlans();
          await this.edges.toggle(`${this.modelID}`, false);
          await this.shadowDropper.renderShadow(this.modelID);
          if ( this._gridEnabled) {
            this.grid.setGrid();
          }
          if ( this._axesEnabled ) {
            this.axes.setAxes();
          }
          await this.plans.exitPlanView(true);

          setTimeout( () => {
            overlay.setAttribute("hidden", "");
          }, 0);
        } else {
          await this.plans.computeAllPlanViews(this.modelID);
          const allPlans = this.plans.getAll(this.modelID);
          const firstPlan = allPlans[0];
          const plans = this._getPlansPanel();
          plans.querySelector(".container").innerHTML = "";
          allPlans.forEach( (plan) => {
            const btn = document.createElement("button");
            btn.innerHTML = this.plans.planLists[this.modelID][plan].name;
            btn.addEventListener("click", (event) => {
              this.plans.goTo(this.modelID, plan, true);
            });
            plans.querySelector(".container").appendChild(btn);
          });
          this.openPlans();
          setTimeout( () => {
            overlay.setAttribute("hidden", "");
          }, 0);
          //this.plans.planLists[this.modelID].forEach()
          await this.shadowDropper.deleteShadow(this.modelID);
          this._gridEnabled = this.grid.grid !== null && this.grid.grid !== undefined;
          this._axesEnabled = this.axes.axes !== null && this.axes.axes !== undefined;
          await this.grid.dispose();
          await this.axes.dispose();
          await this.plans.goTo(this.modelID, firstPlan, false);
          await this.edges.toggle(`${this.modelID}`, true);
        }
      }, 0);
      }}));
    /*
    this.addToolbarItem(this._createToolbarItem({ title: "Export COBie", image: this.imagePath + "cobie.svg", onClick: async (event) => {
      if ( this._tree == undefined ) await this._refreshStructurePanel();
      const blob = await modelToCobie(this.IFC, this.modelID, this._tree.data);
      var url = window.URL.createObjectURL(blob),
      anchor = document.createElement("a");
      anchor.href = url;
      anchor.download = "cobie.xlsx";
      anchor.click();
      window.URL.revokeObjectURL(url);
    }}));
    */
  }

  _getPlansPanel() {
    const container = this.panelsContainer;
    var panel = container.querySelector(".plans");
    if ( !panel ) {
      panel = createPanel("plans", "Plans", [
        {
          image: this.imagePath + "/exportDXF.svg",
          onClick: async (event) => {
            const ifcProject = await this.IFC.getSpatialStructure(this.modelID);
            const storeys = ifcProject.children[0].children[0].children;
            for (let storey of storeys) {
              for (let child of storey.children) {
                if (child.children.length) {
                  storey.children.push(...child.children);
                }
              }
            }
            const currentPlan = this.plans.currentPlan;
            const storey = storeys.find(storey => storey.expressID === currentPlan.expressID);
            exportPlanToDxf(this, storey, currentPlan, this.modelID);
          }
        },
        {
          image: this.imagePath + "/exportPDF.svg",
          onClick: async (event) => {
            const ifcProject = await this.IFC.getSpatialStructure(this.modelID);
            const storeys = ifcProject.children[0].children[0].children;
            for (let storey of storeys) {
              for (let child of storey.children) {
                if (child.children.length) {
                  storey.children.push(...child.children);
                }
              }
            }
            const currentPlan = this.plans.currentPlan;
            const storey = storeys.find(storey => storey.expressID === currentPlan.expressID);
            exportPlanToPdf(this, storey, currentPlan, this.modelID);
          }
        }], false, true);
      container.appendChild(panel);
    }
    return panel;
  }

  _getStructurePanel() {
    const container = this.panelsContainer;
    var panel = container.querySelector(".structure");
    if ( !panel ) {
      panel = createPanel("structure", "Structure", [], true, true);
      container.appendChild(panel);
      panel.querySelector(".container").classList.add("treeContainer");
    }
    return panel;
  }

  openPlans() {
    const panel = this._getPlansPanel();
    if ( panel.style.left === "" ) {
      panel.style.left = this._resolveLeftPosition() + "px";
    }
    if ( panel.style.top === "" ) {
      panel.style.top = this._resolveTopPosition() + "px";
    }
    //this._refreshStructurePanel();
    panel.removeAttribute("hidden");
  }

  closePlans() {
    const panel = this._getPlansPanel();
    panel.setAttribute("hidden", "");
  }


  exportCurrentPlan() {

  }

  closeStructure() {
    const panel = this._getStructurePanel();
    panel.setAttribute("hidden", "");
  }

  openStructure() {
    const panel = this._getStructurePanel();
    if ( panel.style.left === "" ) {
      panel.style.left = this._resolveLeftPosition() + "px";
    }
    if ( panel.style.top === "" ) {
      panel.style.top = this._resolveTopPosition() + "px";
    }
    //this._refreshStructurePanel();
    panel.removeAttribute("hidden");
    const res = panel.querySelector(".treeNode[selected]");
    if ( res ) {
      res.scrollIntoView({behavior: "smooth", block:"center"});
    }
  }

  async _refreshStructurePanel() {
    const container = this._getStructurePanel().querySelector(".container");
    try {
      const modelID = this.modelID;//this.IFC.getModelID();
      if ( modelID === null ) {
        container.innerHTML = "<p>No model pointed</p>";
        return;
      }
      const structure = await this.IFC.getSpatialStructure(modelID, false);
      
      const _f = async function(node) {
        if ( node === undefined || node.expressID === undefined ) return {};
        var ps = await this.IFC.getProperties(modelID, node.expressID, false, false);
        var res = { 
          [node.expressID] : ps.GlobalId.value,
          [ps.GlobalId.value] : node.expressID
            };
        for(const n of node.children || []) {
          Object.assign(res, await _f(n));
        }
        return res;
      }.bind(this);
      this.idsMap = await _f(structure);

      if ( this._tree ) delete this._tree;

      this._tree = new Tree({
        container: container, 
        imagePath: this.imagePath,
        getLabel: (data) => {
          var res = "";
          const img = iconMapping[data["type"]];
          if ( img !== '\ue047' ) res += `<i class=\"material-icons\">${img}</i>`;

          return this.IFC.getProperties(modelID, data["expressID"], false, false).then( (props) => {
            return res + `<span style="line-height: 24px">${textMapping[navigator.language][data["type"]]} - ${props["Name"]?.value}</span><span class=".name"/>`
          });
        },
        data: structure, 
        onClick: async (event) => {
          this._selectionProperties = await this.IFC.getProperties(modelID, event.expressID, false, true);
          this._refreshPropertiesTable();
        },
        onDoubleClick: async(event) => {
          this._selectionProperties = await this.IFC.getProperties(modelID, event.expressID, false, true);
          this._refreshPropertiesTable();
          if (  this._selectionProperties.Representation == undefined ) {
            this.IFC.selector.unpickIfcItems();
            
          } else {
            this.IFC.selector.pickIfcItemsByID(modelID, [event.expressID], true);
          }
          
        }
      });
      
    } catch(error) {
      container.innerHTML = `<p>No model selected</p>`;
      console.error(error);
    }
  }

  /*getAllItemsOfIfcGuid(modelID, guid, verbose) {
    return __async(this, null, function* () {
      let items = [];
      const lines = yield this.api.GetLineIDsWithType(modelID, type);
      for (let i = 0; i < lines.size(); i++)
        items.push(lines.get(i));
      if (!verbose)
        return items;
      const result = [];
      for (let i = 0; i < items.length; i++) {
        result.push(yield this.api.GetLine(modelID, items[i]));
      }
      return result;
    });
  }*/

  
  async selectItems(ids) {
    if  ( ids === undefined || ids === null ) {
      this.IFC.selector.unpickIfcItems();
      this._selectionProperties = [];
      if ( this.autoZoom ) {
        this.fitModelToFrame();
      }
    return;
    }
    if (  !(ids instanceof Array)) ids = [ids];

    ids = ids.map( (id) => {return this.idsMap[id]});

    const idsToSelect = [];
    for( const id of ids) {
      const props = await this.IFC.getProperties(this.modelID, id, false, true);
      if ( props.Representation !== null && props.Representation !== undefined ) {
        idsToSelect.push(id);
      }
    }

    if ( idsToSelect.length > 0 ) {
      await this.IFC.selector.pickIfcItemsByID(this.modelID, idsToSelect, this.autoZoom);
      this._selectionProperties = [];

      for(const id of idsToSelect) {
        this._selectionProperties = this._selectionProperties.concat(await this.IFC.getProperties(this.modelID, id,false, true));
        this._selectionProperties.push("<br/>");
      }
      this._refreshPropertiesTable(); 
    } else {
      await this.IFC.selector.unpickIfcItems();
      for(const id of ids) {
        this._selectionProperties = this._selectionProperties.concat(await this.IFC.getProperties(this.modelID, id,false, true));
        this._selectionProperties.push("<br/>");
      }
      this._refreshPropertiesTable(); 
      if ( this.autoZoom ) {
        this.fitModelToFrame();
      }
    } 
  }


  async exitFullScreen() {
    return document.exitFullscreen();
  }

  async toggleFullScreen() {
    if ( document.fullscreenElement === undefined || document.fullscreenElement === null ) {
      return this.context.getContainerElement().requestFullscreen();
    } else {
      return document.exitFullscreen();
    }
  }

  _getPropertiesPanel() {
    const container = this.panelsContainer;
    var panel = container.querySelector(".properties");
    if ( !panel ) {
      panel = createPanel("properties", "Properties", [], true, true);
      panel.querySelector(".container").classList.add("treeContainer");
      container.appendChild(panel);
      
    }
    return panel;
  }

  _refreshPropertiesTable() {
    const container = this._getPropertiesPanel().querySelector(".container");
    if (  this._selectionProperties === undefined ) {
      container.innerHTML = `<p>No object selected</p>`;
    } else if ( this._selectionProperties === "fetching") {
      container.innerHTML = '<img class="spinMe" src="' + this.imagePath + 'loader.svg"/>';
    } else {
      container.innerHTML = '<img class="spinMe" src="' + this.imagePath + 'loader.svg"/>';
      var data = Object.keys(this._selectionProperties).map( key => { return {propName: key, propValue: this._selectionProperties[key]}});
      const self = this;
      var tree = new Tree({
        container: container,
        imagePath: this.imagePath,
        getLabel: function(elt) { 
          if ( elt["propName"] === "type" ) {
            const ifcManager = self.IFC.loader.ifcManager;
            const stringType = ifcManager.typesMap[elt.propValue];//elt.propValue);// IfcTypesMap[elt.propValue];
            const text = textMapping[navigator.language][stringType];
            return elt["propName"] + ": " + (text || elt["propValue"] || "");
          }
          return elt["propName"] + ": " + (elt["value"] || elt["Description"] || (elt["propValue"] instanceof Object ? elt["propValue"].value : elt["propValue"]) || ""); 
        }, 
        getChildren: function(elt){
          if ( elt.propValue instanceof Object ) {
            if ( Object.keys(elt.propValue).length == 2 ) return [];
            return Object.keys(elt.propValue).map( key => { return {propName: key, propValue: elt.propValue[key]} });
          } else {
            return [];
          }
        }, 
        data: data
      });
    }
  }

  getAutoZoom() {
    return this.autoZoom;
  }

  setAutoZoom(autoZoom) {
    this.autoZoom = autoZoom;
  }


  openProperties() {
    const panel = this._getPropertiesPanel();
    if ( panel.style.left === "" ) {
      panel.style.left = this._resolveLeftPosition() + "px";
    }
    if ( panel.style.top === "" ) {
      panel.style.top = this._resolveTopPosition() + "px";
    }
    panel.removeAttribute("hidden");
  }

  closeProperties() {
    const panel = this._getPropertiesPanel();
    panel.setAttribute("hidden", "");
  }

  openSettings() {

  }

  closeSettings() {

  }

  _createToolbarSeparator() {
    const res = document.createElement("div");
    res.innerHTML = '<hr/>';
    return res;
  }

  _createToolbarItem(item) {
    const res = document.createElement("div");
    res.classList.add("item");
    if ( item.class ) {
      res.classList.add(item.class);
    }
    res.innerHTML = `<img src="${item.image}" alt="${item.title}"/>`;// onClick=${item.onClick}/>`;
    if ( item.onClick ) {
      res.children[0].addEventListener("click", item.onClick);
    }
    return res;
  }

  _getLoadingOverlay() {
    const container = this.context.getContainerElement();
    var overlay = container.querySelector(".loadingOverlay");
    if ( !overlay ) {
      overlay = document.createElement("div");
      overlay.classList.add("loadingOverlay");
      overlay.innerHTML = `<img class="spinMe" src="${this.imagePath}loader.svg"/><p>${this._loadingMessage}</p>`;
      overlay.setAttribute("hidden","");
      container.appendChild(overlay);
    }
    return overlay;
  }

  async gotoHome() {
    await this.fitModelToFrame();
    //camera.setPosition(0, true);
  }

  async fitModelToFrame() {
    const orbitControl = this.IFC.context.ifcCamera.navMode[NavigationModes.Orbit];

    if (!orbitControl.enabled)
        return;
    const scene = this.context.getScene();
    const ind = Math.max(0, Math.min(scene.children.findIndex( (elt) => elt.type === "Mesh"), scene.children.length - 1));
    const box = new Box3().setFromObject(scene.children[ind]);
    const sceneSize = new Vector3();
    box.getSize(sceneSize);
    const sceneCenter = new Vector3();
    box.getCenter(sceneCenter);
    const nearFactor = 0.5;
    const radius = Math.max(sceneSize.x, sceneSize.y, sceneSize.z) * nearFactor;
    const sphere = new Sphere(sceneCenter, radius);
    await this.IFC.context.ifcCamera.cameraControls.fitToSphere(sphere, true);
}

  _refreshOverlayMessage() {
    this._getLoadingOverlay().querySelector("p").innerHTML = this._loadingMessage;
  }


  enableToolbar() {
    this.toolbarContainer.removeAttribute("disabled");
  }

  addToolbarItem(item) {
    this._toolbarItems.push(item);
    this.toolbarContainer.appendChild(item);
  }

  removeToolbarItem(item) {
    const ind = this._toolbarItems.indexOf(item);
    if ( ind >= 0 ) {
      this._toolbarItems.splice(ind, 1);
      item.remove();
    }
  }

  disableToolbar() {
    this.toolbarContainer.setAttribute("disabled", "");
  }

  addSelectionListener(listener) {
    if ( this.listeners["selection"]?.indexOf(listener) == -1 ) {
        this.listeners["selection"]?.push(listener);
    }
  }

  removeSelectionListener(listener) {
      const ind = this.listeners["selection"]?.indexOf(listener) || -1;
      if ( ind > -1 ) {
          this.listeners["selection"]?.splice(ind,1);
      }
  }

  addFileLoadListener(listener) {
      if ( this.listeners["fileLoad"]?.indexOf(listener) == -1 ) {
          this.listeners["fileLoad"]?.push(listener);
          
      }
  }

  removeFileLoadListener(listener) {
      const ind = this.listeners["selection"]?.indexOf(listener) || -1;
      if ( ind > -1 ) {
          this.listeners["selection"]?.splice(ind,1);
      }
  }

  async reloadFiles() {
    this.context.items.ifcModels.forEach( (ifcMesh) => {
      var ind = this.context.items.pickableIfcModels.indexOf(ifcMesh);
      this.context.items.pickableIfcModels.splice(ind,1);
      this.context.getScene().remove(ifcMesh);
    });
    this.loadedFiles.forEach( (file) => this.loadFile(file, this.loadedFiles.length == 1));
  }

  async loadFile(file, replace = true) {
      this.listeners["fileLoad"]?.forEach( (listener) => listener({event: "fetch", file:file, progress:0}));
      // this.IFC.loader.ifcManager.setOnProgress((event) =>  {
      //   let pct = parseInt(event.loaded * 100 / event.total);
      //   this.listeners["fileLoad"]?.forEach( (listener) => listener({event: "loading", file:file, progress: pct}));
      // });
      var ifcModel = await this.IFC.loadIfcUrl(
          file, 
          true,
          (xmlHttpRequest) => {
               this.listeners["fileLoad"]?.forEach( (listener) => listener({event: "fetch", file:file, progress: parseInt(100 * xmlHttpRequest.loaded / xmlHttpRequest.total)}));
          });
        

      if ( replace ) {
        this.loadedFiles = [];
        this.context.items.ifcModels.forEach( (ifcMesh) => {
          if ( ifcModel == ifcMesh ) return;
          var ind = this.context.items.pickableIfcModels.indexOf(ifcMesh);
          this.context.items.pickableIfcModels.splice(ind,1);
          this.context.getScene().remove(ifcMesh);
        });
      }


      ifcModel.material.forEach(mat => mat.side = 2);

      this.modelID = ifcModel.modelID;
      //await this.createFill(ifcModel.modelID);
      
      this.edges.create(`${this.modelID}`, ifcModel.modelID, lineMaterial, baseMaterial);
      // viewer.edges.toggle(`${model.modelID}`);
    
      await this.shadowDropper.renderShadow(ifcModel.modelID);
      
      
      setTimeout( () => {
        this.loadedFiles.push(file);
        setTimeout( async () => {
          await this._refreshStructurePanel();
          this.listeners["fileLoad"]?.forEach( (listener) => listener({event: "loaded", file:file}));
        }, 0);
      }, 0);
          
  }

  _resolveLeftPosition() {
    var left = 0;
    if ( !this.toolbarContainer.hasAttribute("hidden") ) {
      left = this.toolbarContainer.getBoundingClientRect().right;
    }
    if ( !this._getStructurePanel().hasAttribute("hidden") ) {
      left = this._getStructurePanel().getBoundingClientRect().right;
    }
    if ( !this._getPropertiesPanel().hasAttribute("hidden") ) {
      left = this._getPropertiesPanel().getBoundingClientRect().right;
    }
    return left;

  }

  _resolveTopPosition() {
    return 0;
  }

  disableSelection() {
    if ( !this._selectionListener ) return;
    this._selectionEnabled = false;
    this.context.getDomElement().removeEventListener("dblclick", this._selectionListener);
    const container = this.context.getContainerElement();
    container.classList.remove("selection-enabled");
    delete this._selectionListener;
  }

  enableSelection() {
    if ( this._selectionListener ) return;
    if ( !this._selectionListener ) this._selectionListener = this._onSelect.bind(this);
    this._selectionEnabled = true;
    this.context.getDomElement().addEventListener("dblclick",  this._selectionListener);
    const container = this.context.getContainerElement();
    container.classList.add("selection-enabled");
  }

  async fetchPropertiesForItem(modelID, expressId) {
    console.log("Fetching properties...");
    this._selectionProperties = [];
    if ( modelID != null && expressId && expressId >= 0 ) {
      console.log("ongoing...");
      this._selectionProperties = "fetching";
      this._refreshPropertiesTable();
      this._selectionProperties = await this.IFC.getProperties(modelID, expressId, false, true  );
      console.log("Found");
    }
    this._refreshPropertiesTable();
    // console.log("Fetching related objects");
    // let lines = await this.IFC.loader.ifcManager.state.api.GetLineIDsWithType(modelID, IFCRELDEFINESBYPROPERTIES);

    // for (let i = 0; i < lines.size(); i++) {
    //   // Getting the ElementID from Lines
    //   let relatedID = lines.get(i);

    //   // Getting Element Data using the relatedID
    //   console.log("Fetching related object " + (i+1));
    //   let relDefProps = await this.IFC.loader.ifcManager.state.api.GetLine(modelID, relatedID);

    //   // Boolean for Getting the IDs if relevant IDs are present
    //   let foundElement = relDefProps.RelatedObjects.some( (relID) => relID.value === expressId);

    //   if (foundElement) {
    //     // Relevant IDs are found we then we go to RelatingPropertyDefinition
    //     // RelatingPropertyDefinition contain the IDs of Property Sets
    //     // But they should not be array, hence using (!Array.isArray())
    //     if (!Array.isArray(relDefProps.RelatingPropertyDefinition)) {
    //       let handle = relDefProps.RelatingPropertyDefinition;

    //       // Storing and pushing the IDs found in propSetIds Array
    //       console.log("Fetching properties for related object " + (i+1));
    //       Object.assign(this._selectionProperties, await this.IFC.loader.ifcManager.state.api.GetLine(modelID, handle.value, true));
    //     }
    //   }
    // }
  }
  
  async _onSelect(event) {
    console.log("Selecting geometry");
    //event.stopPropagation();
    //event.preventDefault();
    var res = await this.IFC.selector.pickIfcItem(this.autoZoom);
    console.log("Pickup returned");
    if ( res ) {
      console.log("Geometry found");
      if ( this._tree ) {
        this._tree.selectNodeById(res.id);
      }
      this.fetchPropertiesForItem(res.modelID, res.id).then( () => {
        this.listeners["selection"]?.forEach( (listener) => listener(this._selectionProperties));
      });
    } else {
      console.log("Unselecting geometry");
      this.fetchPropertiesForItem(null, null).then( () => {
        this.IFC.selector.unpickIfcItems();
        this.fitModelToFrame();
        this.listeners["selection"]?.forEach( (listener) => listener(this._selectionProperties));

      });
    }
  }

  resize(width, height) {
		this.IFC.context.ifcCamera.activeCamera.aspect = parseFloat(width) / parseFloat(height);
		this.IFC.context.ifcCamera.activeCamera.updateProjectionMatrix();
		this.IFC.context.renderer.renderer.setSize(width, height);
	  }


}