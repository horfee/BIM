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
package psdi.webclient.beans.bim;

import java.rmi.RemoteException;
import java.util.HashSet;

import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.webclient.components.BIMViewer;
import psdi.webclient.controls.Pushbutton;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ComponentInstance;
import psdi.webclient.system.controller.WebClientEvent;
import psdi.webclient.system.session.WebClientSession;

/**
 * @author Doug Wood	
 * This bean is used to display the details of an asset selected in the building view.
 */
public class SystemsDisplayBean extends SystemsBaseBean
{
	public final static String CTRL_DISPLAY = "displaysystems_bg_display";
	public final static String CTRL_CLOSE   = "displaysystems_bg_close";
	public final static String CTRL_CANCEL  = "displaysystems_bg_cancel";

	
	public void initialize() 
		throws MXException, 
		       RemoteException
	{
		WebClientSession wcs = this.app.getWebClientSession();
		Pushbutton displayButton = (Pushbutton)wcs.findControl( CTRL_DISPLAY );
		Pushbutton closeButton   = (Pushbutton)wcs.findControl( CTRL_CLOSE );
		Pushbutton cancelButton  = (Pushbutton)wcs.findControl( CTRL_CANCEL );
		super.initialize();
		if( getViewer() != null )
		{
			if( closeButton   != null ) closeButton.setVisibility( false );
		}
		else
		{
			if( displayButton   != null ) displayButton.setVisibility( false );
			if( cancelButton   != null ) cancelButton.setVisibility( false );
		}
	}

	protected String getTableId()
	{
		return Constants.CTRL_DISP_SYSTEMS_TBL;
	}

	protected String getTreeId()
	{
		return Constants.CTRL_DISP_SYSTEMS_TREE;
	}
	
	
	/**
	 * The starting location is passed from the .jsp on the event that initiatiates the dialog
	 * @throws MXApplicationException 
	 * @throws MXException 
	 * @throws RemoteException 
	 */
	@Override
	protected String setupStartingLocation() 
	    throws RemoteException, 
	           MXException
    {
		WebClientEvent event = clientSession.getCurrentEvent();
		ComponentInstance ci = event.getSourceComponentInstance();
		if( ci != null && ci instanceof BIMViewer )
		{
			return super.setupStartingLocation();
		}
		DataBean appBean = app.getAppBean();
		getMboForUniqueId( app.getAppBean().getUniqueIdValue() );	

		_userInfo = getMbo( 0 ).getUserInfo();

		return appBean.getString( Constants.FIELD_MODELID );
	}

	public synchronized int execute() 
		throws MXException, RemoteException
	{
		// dialogok can be sent by selectrecord in SystemTreeBean.  In that case the
		// tree has already sent the selection to the control and nothing needs to be 
		// done here
		if( getTree() != null && getTree().getRecordSelected() )
		{
			return EVENT_HANDLED;
		}

		if( getViewer() == null )
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		
		String siteId = getViewer().getSiteId();
		DataBean tableBean = app.getDataBean( Constants.CTRL_DISP_SYSTEMS_TBL );
		if( tableBean == null )
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_PRESENTATION_CHANGED );	
		}
		MboRemote selection = tableBean.getMbo();
		if( selection == null )
		{
			return EVENT_HANDLED;
		}
		String systemId = selection.getString( BIMViewer.FIELD_SYSTEMID );
		String location = selection.getString( BIMViewer.FIELD_LOCATION );

		HashSet<String> selectionSet  = new HashSet<String>();
		MboSetRemote hierarchySet = lookupAllLocationsForSystem( systemId, siteId );
		for( int i = 0; hierarchySet != null && i < hierarchySet.count(); i++ )
		{
			MboRemote mbo = hierarchySet.getMbo( i );
			location = mbo.getString( BIMViewer.FIELD_LOCATION );
    		location = getViewer().lookupLocationModelId( location, siteId );
			if( location != null  && location.length() > 0 )
			{
				selectionSet.add( location );
			}
		}
		
		getViewer().setMultiSelect( getModelLocation(), selectionSet );
		
		return EVENT_HANDLED;
	}
}
