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

import psdi.app.bim.BIMService;
import psdi.app.bim.viewer.BuildingModel;
import psdi.server.MXServer;
import psdi.util.MXException;
import psdi.webclient.components.BIMViewer;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.beans.WebClientBean;
import psdi.webclient.system.controller.ComponentInstance;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.controller.WebClientEvent;
import psdi.webclient.system.session.WebClientSession;

public class ModelAddBean  extends DataBean
{
	private static final String DIALOG_PREFIX = "addmodel_";

	private BIMViewer _model = null;
	private boolean _initialized = false;
	private ControlInstance _visibleMainSection = null;

	@Override
    public void initialize() 
		throws MXException, 
		       RemoteException
	{
		if( !_initialized )
		{
			WebClientEvent event = clientSession.getCurrentEvent();
			ComponentInstance ci = event.getSourceComponentInstance();
			if( ci != null && ci instanceof BIMViewer )
			{
				_model = (BIMViewer)ci;
			}
			_initialized = true;
			showViewerTypeSpecificSections();
		}
	}

	/* (non-Javadoc)
	 * @see psdi.webclient.system.beans.DataBean#execute()
	 */
	@Override
	public synchronized int execute() 
		throws MXException, RemoteException
	{
	 super.execute();

		if( _model != null )
		{
			_model.forceUpdate();
			_model.setChangedFlag();
		}
		return EVENT_HANDLED;
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
		clientSession.loadDialog("import_building_model");
	
		if( _model != null )
		{
			_model.setModelListChanged( true );
		}

		return WebClientBean.EVENT_HANDLED;
	}
	
	@Override
	public int addrow() 
		throws MXException
	{
		int row = super.addrow();

		WebClientSession wcs = this.app.getWebClientSession();
		clientSession.queueEvent(new WebClientEvent("setupnewrow", this.getId(), "", wcs));
		
		return row;
	}
	
	@Override
	synchronized public boolean setCurrentRow(
	    int nRow 
    ) 
		throws MXException, 
		       RemoteException
	{
		if( super.setCurrentRow( nRow ) )
		{
			showViewerTypeSpecificSections();
			return true;
		}
		return false;
	}
	
	@Override
	synchronized public void dataChangedEvent(
	    DataBean speaker 
    ) {
		super.dataChangedEvent( speaker );
		showViewerTypeSpecificSections();
	}
	
	private void showViewerTypeSpecificSections()
	{
		try
		{
			BuildingModel model = null;
			if( currentRow >= 0 )
			{
				model = (BuildingModel)getMbo();
			}
			else
			{
				model = (BuildingModel)getMbo( 0 );
			}
			String viewerType;
			if( model != null )
			{
				viewerType   = model.getViewerType();
			}
			else
			{
				viewerType = MXServer.getMXServer().getProperty( BIMService.PROP_NAME_ACTIVE_VIEWER );
			}
			String sectionId = DIALOG_PREFIX + viewerType + ModelAppBean.SUFIX_MAIN;
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
