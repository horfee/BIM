import { IfcAPI } from "web-ifc";
import {exportCobieFile} from './ifc-to-cobie.js';
import * as fs from 'fs/promises';


function readFileAsync(file) {
    return new Promise((resolve, reject) => {
      let reader = new FileReader();
  
      reader.onload = () => {
        resolve(reader.result);
      };
  
      reader.onerror = reject;
      reader.readAsArrayBuffer(file);
    })
  }
  
  export async function exportCobieFile(inputFile) {
    const ifcApi = new IfcAPI();
    // initialize the library
    ifcApi.SetWasmPath("./");
    await ifcApi.Init();
  
    // open a model from data
    let modelID = ifcApi.OpenModel(await readFileAsync(inputFile)); 
    const structure = ifcApi.ifcManager.getSpatialStructure(modelID, true);
    const cobieFile = modelToCobie(ifcApi, modelID, structure);
    // the model is now loaded! use modelID to fetch geometry or properties
    // checkout examples/usage for some details on how to read/write IFC
    // close the model, all memory is freed
    ifcApi.CloseModel(modelID);
    return cobieFile;
    
  
  
  }

const inputFile = process.argv[2];
const outputFile = process.argv[3];

const cobieFile = exportCobieFile(inputFile);
fs.writeFile(outputFile, cobieFile);





