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

<script type="text/javascript">
    const iframe = findElementThroughFrames("<%=id%>_frame");
    iframe.setAttribute("allowfullscreen", "");
    const toolbar = document.querySelector("#<%=toolbarId%> ul.toolbar");//("btnRestore");
    toolbar.style.flexDirection = "row";
    const fullScreenBtn = document.createElement("li");
    const fullScreenA = document.createElement("a");
    const fullScreenImage = document.createElement("img");

    
    fullScreenA.classList.add("on");
    fullScreenA.onblur = function() { removeClass(this, "onhover"); };

    fullScreenA.onclick = function() {
        const ctrl = document.getElementById("<%=ctrlId%>");
        ctrl.viewer.toggleFullScreen()
    }

    fullScreenImage.src = "<%=BIM_IMAGE_PATH%>/fullscreen.svg";
    fullScreenImage.setAttribute("draggable", "false");
    fullScreenImage.setAttribute("alt","<%=strings.resizeBtn%>");
    fullScreenImage.tabIndex = -1;


    fullScreenBtn.appendChild(fullScreenA);
    fullScreenA.appendChild(fullScreenImage);
    toolbar.insertBefore( fullScreenBtn, toolbar.firstChild);//(btnResize.parentNode, fullScreenBtn);
    console.log("here");
    
</script>