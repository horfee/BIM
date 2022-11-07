import { IFCJSBIMViewer } from './IFCJSBIMViewer.js';

const container = document.getElementById("viewer-container");
const panelsContainer = document.getElementById("rightPanels");

var viewer;
var options = {};

if ( true ) {
    viewer = new IFCJSBIMViewer( { 
        container, 
        toolbarDirection:"vertical", 
        //panelsContainer: panelsContainer, 
        imagePath: "./resources/", 
        javascriptPath: "./build/files/" ,
        wasmPath: "./",
        useWebWorker: true
    });
    viewer.openProperties();
    viewer.openStructure();
} else {
    viewer = new IFCJSBIMViewer( { container, toolbarDirection:"vertical", panelsContainer: null, imagePath: "./resources/", javascriptPath: "./build/files/"});
}
viewer.axes.setAxes();
viewer.grid.setGrid();
viewer.enableToolbar();
//window.myViewer = viewer;


viewer.addFileLoadListener((event) => {
        console.log(event); 
    });

viewer.addSelectionListener( (event) => {
    console.log(event);
});

viewer.enableSelection();

var file = document.getElementById("file-input");
file.addEventListener(
    "change",
    (changed) => {
        const file = changed.target.files[0];
        var ifcURL = URL.createObjectURL(file);
        viewer.loadFile(ifcURL);
    },
    false
);

window.viewer = viewer;