//import Drawing from 'dxf-writer';
import { MeshBasicMaterial } from 'three';

const dummySubsetMat = new MeshBasicMaterial({visible: false});

export async function exportPlanToDxf(viewer, storey, plan, modelID) {

    viewer.dxf.initializeJSDXF(Drawing);
	// Create a new drawing (if it doesn't exist)
	if (!viewer.dxf.drawings[plan.name]) viewer.dxf.newDrawing(plan.name);

	// Get the IDs of all the items to draw
	const ids = storey.children.map(item => item.expressID);

	// If no items to draw in this layer in this floor plan, let's continue
	if (!ids.length) return;

	// If there are items, extract its geometry
	const subset = viewer.IFC.loader.ifcManager.createSubset({
		modelID,
		ids,
		removePrevious: true,
		customID: 'floor_plan_generation',
		material: dummySubsetMat,
	});

	// Get the projection of the items in this floor plan
	const filteredPoints = [];
	const edges = await viewer.edgesProjector.projectEdges(subset);
	const positions = edges.geometry.attributes.position.array;

	// Lines shorter than this won't be rendered
	const tolerance = 0.01;
	for (let i = 0; i < positions.length - 5; i += 6) {

		const a = positions[i] - positions[i + 3];
		// Z coords are multiplied by -1 to match DXF Y coordinate
		const b = -positions[i + 2] + positions[i + 5];

		const distance = Math.sqrt(a * a + b * b);

		if (distance > tolerance) {
			filteredPoints.push([positions[i], -positions[i + 2], positions[i + 3], -positions[i + 5]]);
		}

	}

	// Draw the projection of the items
	viewer.dxf.drawEdges(plan.name, filteredPoints, 'Projection', Drawing.ACI.BLUE, 'CONTINUOUS');

	// Clean up
	edges.geometry.dispose();


	// Draw all sectioned items
    viewer.dxf.drawNamedLayer(plan.name, plan, 'thick', 'Section', Drawing.ACI.RED, 'CONTINUOUS');
    viewer.dxf.drawNamedLayer(plan.name, plan, 'thin', 'Section_Secondary', Drawing.ACI.CYAN, 'CONTINUOUS');

	const result = viewer.dxf.exportDXF(plan.name);
	const link = document.createElement('a');
	link.download = 'floorplan.dxf';
	link.href = URL.createObjectURL(result);
	document.body.appendChild(link);
	link.click();
	link.remove();
}

export async function exportPlanToPdf(viewer, storey, plan, modelID) {
        const currentPlans = this.plans.planLists[0];
        const planNames = Object.keys(currentPlans);
        const firstPlan = planNames[0];
        const currentPlan = this.plans.planLists[0][firstPlan];
    
        const documentName = 'plan-' + plan.name;
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
}