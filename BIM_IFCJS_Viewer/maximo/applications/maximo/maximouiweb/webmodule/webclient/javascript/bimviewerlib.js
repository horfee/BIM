//**********************************************************************
// Licensed Materials - Property of IBM
// Restricted Materials of IBM
// 
// (C) COPYRIGHT IBM CORP. 2018 All Rights Reserved.
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.
//**********************************************************************

//**********************************************************************
// Patches to library.js - These definitions override those in library.js
//**********************************************************************
function hideAllMenus(nf)
{
	if(showingPopup)
		return;
	hidePopup();
	dojohelper.closePickerPopup();
	currentMenu=null;
	var count = openMenus.length;
    for(var i = 0; i < openMenus.length; i++)
    {
        var om = getElement(openMenus[i]);
        if(om)
            om.style.display = "none";
    }
    openMenus = new Array();
    if( count > 0 ) showObjs();
	if(nf)
		delayedFocus(getFocusId(),true,FOCUSDELAY);
}

function hideObjs( isMenu )
{
	if( isMenu == undefined ) isMenu = false;
	
	hideObjsInFrame( document, isMenu, "OBJECT" );
    
	var frames = window.top.document.getElementsByTagName("IFRAME");
	var l = frames.length;
	for(var i = 0; i < l; i ++ )
	{
		var f = frames[i];
		var autoHide = f.getAttribute("autoHide"); 
		if( autoHide != undefined && (autoHide == true || autoHide == "true" ))
		{
	    	hideELement( f, isMenu );
		}
		else
		{
			try
			{
				var doc =  f.contentWindow.document;
				if( doc != undefined )
				{
					hideObjsInFrame( doc, isMenu, "OBJECT"  );
				}
			}
			catch( error )
			{
				continue;
			}
		}
	}
}

function hideObjsInFrame( doc, isMenu, type )
{
    var objs = doc.getElementsByTagName( type );
    var len = objs.length;
    for(var i = 0; i < len; i ++ )
    {
    	hideELement( objs[i], isMenu );
    }
}

function hideELement( obj, isMenu )
{
    var dc = dialogCount;
	if( !isMenu ) dc--;

    var hidden  = obj.getAttribute("hidden");
    if( hidden != undefined && ( hidden == true || hidden == "true" ))
   	{
    	return;
   	}

    var ch = obj.clientHeight;
    var cw = obj.clientWidth;
    var waiting = obj.getAttribute("wiating");
    if( waiting != undefined && waiting )
   	{
    	obj.setAttribute("wiating", false );
    	return;
   	}


    if(ch > 0)
    {
        if (obj.height.indexOf("%") > - 1)
        	obj.setAttribute("dimensionHeight",obj.height);
        else
        	obj.setAttribute("dimensionHeight",ch);

        if (obj.width.indexOf("%") > - 1)
        	obj.setAttribute("dimensionWidth",obj.width);
        else
        	obj.setAttribute("dimensionWidth",cw);

        obj.style.height = "1px";
        obj.style.width = "1px";
        var back = document.getElementById(obj.id + "_back");
        if(back)
        	back.style.display = "";

        obj.setAttribute("hidden", true );
        var hideLevel  = obj.getAttribute("hideLevel");
        if( hideLevel == undefined ) 
    	{
            obj.setAttribute("hideLevel", dc );
    	}
    }
}

function showObjs()
{
	showObjsInFrame( document, "OBJECT" );
	
 	var frames = window.top.document.getElementsByTagName("IFRAME");
	var l = frames.length;
	for(var i = 0; i < l; i ++ )
	{
		var f = frames[i];
		var doc;
		try
		{
			doc =  f.contentWindow.document;
		}
		catch( error )
		{
			continue;
		}

		var autoHide = f.getAttribute("autoHide"); 
		if( autoHide != undefined && (autoHide == true || autoHide == "true" ))
		{
			if( doc ) showElement( doc, f );
		}
		else
		{
			try
			{
				if( doc ) showObjsInFrame( doc, "OBJECT" );
			}
			catch( error )
			{
				continue;
			}
		}
	}
}

function showObjsInFrame( doc, type )
{
    var objs = doc.getElementsByTagName( type );
    var len = objs.length;
    for(var walkobjs = 0; walkobjs < len; walkobjs ++ )
    {
    	showElement( doc, objs[walkobjs] );
    }
}

function showElement( doc, obj )
{
    var dc = dialogCount;
    
    // Test if object is already hidden
    var hidden  = obj.getAttribute("hidden");
    if( hidden == undefined || hidden === false ) return;
    
    var hideLevel  = obj.getAttribute("hideLevel");
    if( hideLevel == undefined ) return;
    if( hideLevel < dialogCount ) return;
    
    var back = doc.getElementById(obj.id + "_back");
    if(back)
	{
    	back.style.display = "none";
	}

    // Its possible that the objects size has been changes since it was 
    // hidden, such as in response to a dialog or menu action.  So only
    // restore it if its the size hide set. 
    if( obj.clientHeight <=1 )
	{
        obj.style.height = obj.getAttribute("dimensionHeight");
        obj.height = obj.getAttribute("dimensionHeight");
	}
    if( obj.clientWidth <= 1 )
	{
    	obj.style.width = obj.getAttribute("dimensionWidth");
        obj.width = obj.getAttribute("dimensionWidth");
	}
    obj.setAttribute( "hidden", false );
	obj.hidden = false;
}

/*
 * Fixup when returning from a go to app link 
*/
function rehideObjs()
{
    if( dialogCount <= 0 )
	{
		return;
	}
    hideObjsInFrame( document, false, "OBJECT" );
 
	var frames = window.top.document.getElementsByTagName("IFRAME");
	var l = frames.length;
	for(var i = 0; i < l; i ++ )
	{
		var f = frames[i];
		var doc;
		try
		{
			doc =  f.contentWindow.document;
		}
		catch( error )
		{
			continue;
		}
		var autoHide = f.getAttribute("autoHide"); 
		if( autoHide != undefined && (autoHide == true || autoHide == "true" ))
		{
			hideObjsInFrame( doc, false, "IFRAME" );
	    	hideELement( f, false );
		}
		else
		{
			try
			{
				if( doc != undefined )
				{
					hideObjsInFrame( doc, false, "OBJECT" );
				}
			}
			catch( error )
			{
				continue;
			}
		}
	}
}


function findElementThroughFrames(id, w) {
	if ( w === undefined ) w = window.top;
	
	var el = w.document.getElementById(id);
	if(el) return el;
	for(var i = 0; i < w.frames.length; i++) {
		el = findElementThroughFrames(id, w.frames[i].window);
		if ( el ) return el;
	}

	return undefined;
}