import { ThreeScene } from './scene';
import { IfcManager } from './ifc-manager';

const ifcModels = [];
const baseScene = new ThreeScene();
const loader = new IfcManager(baseScene.scene, ifcModels);

document.querySelector("#selectSite").addEventListener("click", (event) => {
    const model = 0;
    const manager = loader.ifcLoader.ifcManager;
    const config = {
            modelID: model,
            scene: baseScene.scene,
            ids: [161],
            removePrevious: true
    }
    manager.createSubset(config);
});