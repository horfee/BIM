/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * (C) COPYRIGHT IBM CORP. 2011,2018
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */
package psdi.webclient.beans.bim.viewer;

import java.rmi.RemoteException;

import psdi.mbo.MboRemote;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.webclient.beans.bim.Constants;
import psdi.webclient.components.BIMViewer;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ComponentInstance;
import psdi.webclient.system.controller.WebClientEvent;



/**
 * @author Doug Wood	
 */
public class BaseViewerBean extends DataBean  
{
	private BIMViewer _model = null;
	
	public void initialize() throws MXException, RemoteException
	{
		WebClientEvent event = clientSession.getCurrentEvent();
		ComponentInstance ci = event.getSourceComponentInstance();
		MboRemote locMbo;
		
		// Called from the viewer.  Use location in event
		if( ci != null && ci instanceof BIMViewer )
		{
			_model = (BIMViewer)ci;
			Object o = event.getValue();
			if( o == null || !(o instanceof String ))
			{
				// Should never happen unless the .jsp is altered
				throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
			}
			
			// There may not be a siteId if the viewer is used for lookup on a filter
			String siteId     = _model.getSiteId();
			
			// query for mbo for the location 
			locMbo = _model.lookupLocationFromModelId( (String)o, siteId );
			if( locMbo == null  )
			{
				 throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_NOT_IMPORTED );	
			}
		}
		// Called from the select action menu.  Use app location
		else
		{
			locMbo = app.getDataBean().getMbo();
		}
		long uid = locMbo.getUniqueIDValue();    
		getMboForUniqueId( uid );	
	}
}
