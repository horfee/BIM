/*
 *
 * IBM Confidential
 *
 * (C) COPYRIGHT IBM CORPORATION 2001-2011,2018
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 */
package psdi.webclient.beans.bim.viewer;

import java.rmi.RemoteException;

import psdi.app.bim.BIMService;
import psdi.app.bim.viewer.BuildingModel;
import psdi.app.location.LocationRemote;
import psdi.mbo.MboRemote;
import psdi.server.MXServer;
import psdi.util.MXException;
import psdi.webclient.beans.bim.Constants;
import psdi.webclient.system.beans.AppBean;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.beans.WebClientBean;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.session.WebClientSession;

public class ModelAppBean
    extends AppBean
{
	static final String SUFIX_MAIN = "_main";
	
	private ControlInstance _visibleMainSection = null;
	
	@Override
    protected void initialize() 
	throws MXException, 
	       RemoteException
	{
		super.initialize();
			DataBean appBean = app.getDataBean();
		if( appBean.isAttribute( Constants.FIELD_SITEID ))
		{
			String siteId = appBean.getString( Constants.FIELD_SITEID );
			if( siteId != null && siteId.length() > 0 )
			{
				WebClientSession wcs = this.app.getWebClientSession();
				ControlInstance ctrl;
				ctrl = wcs.findControl( "managemodels_table" );
				if( ctrl != null )
				{
					DataBean ctrlBean = ctrl.getDataBean();
		    		ctrlBean.resetQbe();
		    		ctrlBean.setQbe( Constants.FIELD_SITEID, siteId );
				}
			}
		}
	}
	
	/**
	 * Handle the upload button on the dialog
	 * @return
	 * @throws MXException
	 * @throws RemoteException
	 */
	public int loadModelFile() 
		throws MXException, 
			   RemoteException
	{
		clientSession.loadDialog( Constants.DLG_IMPORT_MODEL);
		fireDataChangedEvent();
		return WebClientBean.EVENT_HANDLED;
	}

	@Override
	// Catch update to location and update site and org (Called from DrillDown bean)
	synchronized public void setValue(
	    String attribute,
	    MboRemote mboRemote 
    ) 
		throws MXException
	{
		if(    mboRemote != null && mboRemote instanceof LocationRemote 
			&& attribute.equalsIgnoreCase( Constants.FIELD_LOCATION))
		{
			try 
			{
				setValue( Constants.FIELD_ORGID, mboRemote.getString( Constants.FIELD_ORGID ));
				setValue( Constants.FIELD_SITEID, mboRemote.getString( Constants.FIELD_SITEID ));
			} 
			catch (RemoteException e) 
			{
				handleRemoteException(e);
			}
		}
		super.setValue(  attribute, mboRemote );
	}
	
	public int eventTabChanged()
	{
		showViewerTypeSpecificSections();
		return EVENT_HANDLED;
	}

	
	@Override
    public void dataChangedEvent( 
       DataBean speaker 
    ) {
		super.structureChangedEvent( speaker );
		showViewerTypeSpecificSections();
	}
	
	@Override
    public void structureChangedEvent( 
       DataBean speaker 
    ) {
		super.structureChangedEvent( speaker );
		showViewerTypeSpecificSections();
	}
	
	@Override
	public synchronized void insert() 
		throws MXException, 
		       RemoteException
	{
		super.insert();
		showViewerTypeSpecificSections();
	}
	
	private void showViewerTypeSpecificSections()
	{
		try
		{
			BuildingModel model = (BuildingModel)getMbo();
			String viewerType;
			if( model != null )
			{
				viewerType   = model.getViewerType();
			}
			else
			{
				viewerType = MXServer.getMXServer().getProperty( BIMService.PROP_NAME_ACTIVE_VIEWER );
			}
			String sectionId = viewerType + SUFIX_MAIN;
			ControlInstance section = clientSession.getControlInstance( sectionId );
			if( section != null && _visibleMainSection != section )
			{
				if( _visibleMainSection != null )
				{
					_visibleMainSection.setVisibility( false );
				}
				section.setVisibility( true );
				_visibleMainSection = section;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

}
