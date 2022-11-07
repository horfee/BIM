<%--
* Licensed Materials - Property of IBM
* Restricted Materials of IBM
* 
* (C) COPYRIGHT IBM CORP. 2010,2018 All Rights Reserved.
* US Government Users Restricted Rights - Use, duplication or
* disclosure restricted by GSA ADP Schedule Contract with
* IBM Corp.
--%>
<%@page import="psdi.webclient.components.*"%>
<%@page import="psdi.server.MXServer"%>
<%@ include file="../common/componentheader.jsp" %>
<%

// when in design mode return some stub html for App Designer
if(designmode) 
{
	IMAGE_PATH = servletBase + "/"+skin+"images/"+(rtl?"rtl/":"")+wcs.getImagePath();
	%>
    <div>
    <img src="<%=IMAGE_PATH%>bim//ViewerDesignerMode.png" alt="BIM 3D viewer" draggable="false"> 
    </div>
    <%
    return;
}

// Must be bound to an instance of NavisWorks to function
if( !(component instanceof BIMViewer ) )
{
	return;
}
String uiSessionId = wcs.getUISessionID();
BIMViewer bldgMdl = (BIMViewer)component;
IMAGE_PATH = servletBase + "/"+skin+"images/"+(rtl?"rtl/":"")+wcs.getImagePath();

// Designer mode may put "-" into the ID string which make them invalid for JavaScript 
// idenfiers - Get rid of them
id = id.replace( "-", "_" );

String containerTable   = id + "container";

String value   = null;

boolean _needsRendered = bldgMdl.needsRender();


if( _needsRendered )
{%>
	<script type="text/javascript" >
		var jsLibrary = document.createElement('SCRIPT' );
		jsLibrary.type = "text/javascript";
		jsLibrary.src  = "<%=servletBase%>/javascript/bimviewerlib.js";
		if( navigator.appName == "Microsoft Internet Explorer" )
		{
			try
			{
				window.top.document.appendChild( jsLibrary );			// 7.5 wants this
			}
			catch( e ) 
			{
				window.top.document.head.appendChild( jsLibrary );		// 7.6 Want this
			}
		}
		else
		{
			var headers = document.getElementsByTagName('head');
			var head = headers[0];
			head.appendChild( jsLibrary );
		}
		
		<%
		String version = MXServer.getMXServer().getMaxupgValue();
		if( version.startsWith( "V7503" ) )
		{%>
			jsLibrary = document.createElement('SCRIPT' );
			jsLibrary.type = "text/javascript";
			jsLibrary.src  = "<%=servletBase%>/javascript/menus.js";
			if( navigator.appName == "Microsoft Internet Explorer" )
			{
				window.top.document.appendChild( jsLibrary );			// 7.5 wants this
			}
			else
			{
				var headers = document.getElementsByTagName('head');
				var head = headers[0];
				head.appendChild( jsLibrary );
			}
		<%}%>
		
	<%if( bldgMdl.getAppType() ==  BIMViewer.TYPE_LOOKUP )
	{%>
		// Need to allow time for the NavisWorks control to initialize or it fails to bind
		// to its event handlers
		addLoadMethod( 'setTimeout( \'<%=bldgMdl.jspScript( id )%>\', 100 );' );
	<%}
	else
	{%>
		addLoadMethod( 'setTimeout( \'<%=bldgMdl.jspScript( id )%>\', 100 );' );
		//addLoadMethod( '<%=bldgMdl.jspScript( id )%>' );
	<%}%>
	
	</script>
	<%
} 
else
{
	if( bldgMdl.getMxVersion() >= BIMViewer.VERSION_75_OR_GREATER )
	{%>
		<component id="<%=id%>_holder"><%="<![CDATA["%>
			<script>
				setTimeout( '<%=bldgMdl.jspScript( id )%>', 100 );
			</script>
		<%="]]>"%></component>
		<%
	}
	else
	{%>
		<%=bldgMdl.jspScript( id )%>
	<%}
}  
  
if( _needsRendered )
{
	// Force a reload of the model file if the control is being redrawn
//	bldgMdl.setModelListChanged( true );
//	bldgMdl.setValueChanged( true );
	String controlTop = component.getProperty("controltop");
	controlTop = (controlTop == null || controlTop.equalsIgnoreCase("")) ? "250" : controlTop;
	String controlLeft     = component.getProperty("controlleft");
	controlLeft = (controlLeft == null || controlLeft.equalsIgnoreCase("")) ? "325" : controlLeft;
	String height     = component.getProperty("height");
	height = (height == null) ? "" : height;
	String width     = component.getProperty("width");
	width = (width == null) ? "" : width;
	%>

<div style="position: relative;height: 0;width: 0"> <!-- div so that the frameLoc div doesn't take space even though it's moved relatively -->
	<div id="<%=id%>_frameLoc" style="border: 0px none; position: relative; overflow: hidden; visibility: hidden; display: none; height: 1px; width: 1px;">
		<script>
			var eventMethod = window.addEventListener ? "addEventListener" : "attachEvent";
			var eventer = window[eventMethod];
			var messageEvent = eventMethod == "attachEvent" ? "onmessage" : "message";
			var isViewerFrameLoaded = false;
			var queuedMessages = [ ];

			// Listen to message from child window
			eventer(messageEvent, function(e) {
				var key = e.message ? "message" : "data";
				var data = e[key];

				if (bimviewerFunctions[data.funcCall]) {
					bimviewerFunctions[data.funcCall](data.passVar);
				}
			},false);
			
			// Send message to child window
			function maximoToViewerMessage(message) {
				var viewerIFrame = document.getElementById("<%=id%>_frame");
				if(viewerIFrame) {
					var viewerIFWin = viewerIFrame.contentWindow;
					if(viewerIFWin) {
						if(isViewerFrameLoaded) {
							viewerIFWin.postMessage(message,"*");
						} else {
							queuedMessages.push(message);
						}
					}
				}
			}
			
			function sendQueuedMessage() {
				isViewerFrameLoaded = true;
				queuedMessages.forEach(function(item, index, array) {
					maximoToViewerMessage(item);
				});
				queuedMessages = [ ];
			}
			
			function resizeTarget(hwArg) {
				if(targCtrl) {
					targCtrl.style.height = hwArg.height + "px";
					targCtrl.style.width = hwArg.width + "px";
					console.log(">>> target control resized", targCtrl.style.height, targCtrl.style.width);
				}
			}

			var bimviewerFunctions = {
				'viewerframeLoaded': function() {sendQueuedMessage();},
				'resizeTarget': function(passArg) {resizeTarget(passArg);}
			}
			
				
			function locRectToWindRect( elem ) {
				if(!elem) return null;
				var target = elem;
				var target_width = target.offsetWidth;
				var target_height = target.offsetHeight;
				var target_left = target.offsetLeft;
				var target_top = target.offsetTop;
				var gleft = 0;
				var gtop = 0;
				var rect = {};

				var getHigher = function( parentElem ) {
					if (!!parentElem) {
						gleft += parentElem.offsetLeft;
						gtop += parentElem.offsetTop;
						getHigher( parentElem.offsetParent );
					} else {
						return rect = {
							top: (target.offsetTop + gtop),
							left: (target.offsetLeft + gleft),
							bottom: ((target.offsetTop + gtop) + target_height),
							right: ((target.offsetLeft + gleft) + target_width)
						};
					}
				};
				getHigher( target.offsetParent );
				return rect;
			}
			
			function sameRect(rect1, rect2) {
				return((rect1 && rect2) &&
					rect1.top == rect2.top && 
					rect1.left == rect2.left &&
					rect1.bottom == rect2.bottom && 
					rect1.right == rect2.right);
			}
			
			var targCtrl;
			var targRect;
			var elemParentRect;
			var targetTrackingInterval;
			function moveToTarg(elemId, targId) {
				
				console.log(">>> moveToTarg hit");
				var ckTarg = document.getElementById(targId);
				var elem = document.getElementById(elemId);
				var elemParent = elem.parentElement;
				if(ckTarg == null) {
					elem.style.display = "none";
					elem.style.visibility = "hidden";
				} else {
					elem.style.display = "block";
					elem.style.visibility = "visible";
				}

				var targMoved = (targCtrl == undefined || !sameRect(targRect, locRectToWindRect(ckTarg)));
				var elemParentMoved = (elemParentRect == undefined || !sameRect(elemParentRect, locRectToWindRect(elemParent)));
				if(ckTarg && (targMoved || elemParentMoved)) {

					targCtrl = document.getElementById(targId);
					if(elem && !sameRect(locRectToWindRect(elem), locRectToWindRect(ckTarg))) {
						elemParentRect = locRectToWindRect(elemParent);
						targRect = locRectToWindRect(ckTarg);
						var newElemTop = targRect.top - elemParentRect.top;
						var newElemLeft = targRect.left - elemParentRect.left;
						elem.style.top = newElemTop + "px";
						elem.style.left = newElemLeft + "px";
						elem.style.height = targCtrl.style.height;
						elem.style.width = targCtrl.style.width;
						
						console.log(">>> elem.id, targ.id", elem.id, targCtrl.id);
						console.log(">>> elem.style.width, targ.style.width", elem.style.width, targCtrl.style.width);

						if(!targetTrackingInterval) {
							targetTrackingInterval = setInterval(moveToTarg, 100, elemId, targId);
						}
					}
				}
			}

		</script>
	</div>
</div>

<%
}  // Close else if !bldgMdl.needsRender() )
%>

<%@ include file="../common/componentfooter.jsp" %>
