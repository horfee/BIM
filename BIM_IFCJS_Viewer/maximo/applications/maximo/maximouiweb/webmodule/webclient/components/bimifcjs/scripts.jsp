<%--
* Licensed Materials - Property of IBM
* Restricted Materials of IBM
* 
* 5724-U18, 5737-M66
*
* (C) COPYRIGHT IBM CORP. 2010,2021 All Rights Reserved.
* US Government Users Restricted Rights - Use, duplication or
* disclosure restricted by GSA ADP Schedule Contract with
* IBM Corp.
--%>
<%@page import="com.ibm.tivoli.maximo.oslc.OslcUtils"%>
<%@page import="java.lang.Exception"%>
<%@page import="java.net.URL"%>
<%

String oslcUrl = "";
try {
	oslcUrl = OslcUtils.getOslcWebAppURL() + "ifc";
	URL url = new URL(oslcUrl);
	oslcUrl = url.getPath();
} catch(Exception e) {
	oslcUrl = "error";
}

%>

<script id=<%=id%>_container type="text/javascript" >
"use strict";


/**********************************************************************/
// Loads the NavisWorks control tryng different versions then sets the
// lable next to the AD logo to the version loaded
/**********************************************************************/
function loadControl(
	parentCtrl,
	versionLableCtrl
) {
	var parentCell        = document.getElementById( parentCtrl );
	var versionCell       = document.getElementById( versionLableCtrl );

	var container = document.createElement("DIV");
	container.id = "<%=ctrlId%>";
	parentCell.appendChild(container);

	
	vfToMaximoMessage({"funcCall": "viewerframeLoaded", "passVar": null});
}

/**********************************************************************/
// This class contains all the functions needed to interact with the 
// native viewer 
/**********************************************************************/
function ViewerWrapper(
	ctrl	// The control handle
) {
	this.modelMgr  = null;
	this.selMgr    = null;
	this.model;
	this.ctrl      = ctrl;
	this.selection = [];
	
    // Return the version of this interface.  Implementations written to
    // this spec should always return "1.0"
    this.getInterfaceVersion = function()
    {
      return "1.1";
    };
	
    // Called once after all objects are created, but before any methods
    // are called with the exception of setCurrentModel.
    // modelMgr       The ModelManager instance
    // selectionMgr   The SelectionManager instance
    // Return:        Nothing
	this.initialize = function(
		modelMgr,
		selectionMgr
	) {
		this.modelMgr  = modelMgr;
		this.selMgr    = selectionMgr;

		this.viewer = new window.ifcjs.IFCJSBIMViewer({ 
			container: this.ctrl,
			imagePath: '<%=BIM_IMAGE_PATH%>',
			javascriptPath: '<%=servletBase%>/javascript/bimifcjs',
			wasmPath: './',
			useWebWorker: true
		});

		this.ctrl.viewer = this.viewer;
		this.viewer.axes.setAxes();
		this.viewer.grid.setGrid();
		this.viewer.enableToolbar();
		this.viewer.enableSelection();
		this.viewer.addSelectionListener( event => {
			this.onSelect(event.GlobalId.value);
		});
	};
	
    // Request that the viewer load the specified file.  Errors should
    // be reported on the viewer status line by calling 
    // setStatus( status )
    // file:          The URL attribute from the Maximo 
    //                BuildingModel table
    // Return:        Nothing
	this.loadFile = function(  
		file
	) {
		if ( file === undefined || file === undefined || file === "" ) return;
		console.log(file);
		this._loadingPromise = new Promise( (resolve, reject) => {
			this.viewer.addFileLoadListener( (event) => {
				if ( event.event === "loaded" ) {
					resolve();
				}
			});
		});
		this.viewer.loadFile("<%=oslcUrl%>?file=" + file);

	};
	
	// Requests the viewer plugin to select a single item clearing
	// any previous selection
	// Value          Id of item to select
    // zoomToContext: Flag indicating if the viewer should zoom in on 
    //                the resulting selection set.
    // Return:        Nothing
	this.selectValue = function( 
		value, 
		zoomToContext 
	) {
		console.log("IFCJSBIMViewer > selectValue (%s) (%d)", value, zoomToContext );
		this._loadingPromise.then( () => {
			this.viewer.selectItems([value]);
		});
	};

    // Request the view plugin to select a list of items clearing
    // any previous selection
    // valueList:     Array of ids that is the desired selection set.
    // zoomToContext: Flag indicating if the viewer should zoom in on 
    //                the resulting selection set.
    // Return:        Number of items found and selected
	this.selectValueList = function(
		valueList,		// Array of itds to select
		zoomToContext
	) {
		console.log("IFCJSBIMViewer > selectValueList (%s) (%d)", JSON.stringify(valueList), zoomToContext );
		this._loadingPromise.then( () => {
			this.viewer.selectItems(valueList);
		});
	};

	// Called when the current model is changed. This method does not need to load
	// the new model.  loadFile is called for that
    // Return:        Nothing
	this.setCurrentModel = function(
		currentModel
	) {
		/*this._loadingPromise.then( () => {
			this.viewer.loadFile("<%=oslcUrl%>?file=" + file);
		});*/
		console.log("IFCJSBIMViewer > setCurrentModel (%s)", JSON.stringify(currentModel) );
	};
	
    // Requests the value (id) of the selected item with a specified
    // index in the selection list.  
    // index   The index of the active selection item in the current
    //         selection list 
    // return  The id of the item in the selection list referenced by
    //         index. If the index is out of bounds then an empty string
    //         is returned
	this.getSelection = function( index ) {
		console.log("IFCJSBIMViewer > getSelection (%d)", index );
		return this.selection[index];
	};
	
	// An array of the currently selected items ids
    this.getSelectionList = function() {
		console.log("IFCJSBIMViewer > getSelectionList");
		return this.selection;
    };
    
    // Return:  THe number of items currently selected
    this.getSelectionCount = function() {
		console.log("IFCJSBIMViewer > getSelectionCount");
		return this.selection.length;
    };
    
    // Search the selection list for selected item and return the imdex
    // Return the index of the selected item in the selection list.
    this.getItemIndex = function(
    	selectedItem	// Any item in the list of selected items
 	) {
		console.log("IFCJSBIMViewer > getItemIndex (%s)", JSON.stringify(selectedItem));
		return this.selection.findIndex( (elt) => selectedItem == elt);
    };
    
    // Clear all selected item and set the selection list to zero length
    this.clearSelection = function()
    {
		console.log("IFCJSBIMViewer > clearSelection");
		this.selection = [];
    };
    
    // Soom the current view to focus on the item in the selection set indicated by index
    this.focusOnSelectedItem = function(
    	itemIndex	// The idex of the item in the selection list
	) {
		console.log("IFCJSBIMViewer > focusOnSelectedItem (%d)", itemIndex );
    };
    
    this.enableMultiSelect = function (
   		enable
	) {
		console.log("IFCJSBIMViewer > enableMultiSelect (%d)", enableMultiSelect );
    };
	
	// Called to notify the viewer of changes to the auto soom state
	// The viewer need implement this only if it supports auto zoom
	// beyond what is controled by the flags in setValye and 
	// setValueList
	this.setAutoZoom = function(
		enable
	) {
		console.log("IFCJSBIMViewer > setAutoZoom (%d)", enable );
		this.viewer.setAutoZoom(enable);
	};
	
	// Called when the controling applcation has resized the viewer container
	// The viewer must adjust to the new container size
	this.reziseViewer = function(
		height,
		width
	) {
		this.viewer.resize(width,height);
		var ctrl = document.getElementById( "<%=ctrlId%>" );
		if(ctrl) {
			ctrl.style.height = "100%";
			ctrl.style.width = "100%";
		}
		console.log("IFCJSBIMViewer > resizeViewer (%s) (%s) (<%=ctrlId%>)", width, height );
	};
    
    // Called when the selection changes after the common processing
    // ctrl           HTML id of the viewer control
    // selectionList  Array of Ids that are currently selected. this
    //                is the list returned from calling  
    //                this.getSelectionList
    // selection,     The active item in the selection set.
    // count,         The number of items in the selection set.  This is
    //                the value returned from calling
    //                this.getSelectionCount
    // index          The index in the selection set of the active item
    //                This is the result of calling this.getItemIndex
    // Return:        Nothing
    this.onSelectionChange = function(
		ctrl,
		selectionList,
		selection,
		count,
		index 
	) {
	};
	
	this.onSelect = function(
		selection
	) {
		this.selection = [selection];
		selMgr.updateSelectionSet( this.ctrl );
	};
	
}

function findElementThroughFrames(id, w) {
	if ( w === undefined ) w = window.top;
	
	var el = w.document.getElementById(id);
	if(el) return el;
	for(var i = 0; i < w.frames.length; i++) {
		el = findElementThroughFrames(id, w.frames[i].window);
		if ( el ) return el;
	}

	return null;
}

</script>
