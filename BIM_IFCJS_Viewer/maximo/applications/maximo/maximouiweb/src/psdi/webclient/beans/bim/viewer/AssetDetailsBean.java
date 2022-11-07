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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;

import psdi.mbo.MboRemote;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.webclient.beans.bim.Constants;
import psdi.webclient.components.BIMViewer;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ComponentInstance;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.controller.WebClientEvent;
import psdi.webclient.system.session.WebClientSession;



/**
 * @author Doug Wood	
 * This bean is used to display the details of an asset selected in the building view.
 */
public class AssetDetailsBean extends DataBean
{
	private BIMViewer _viewer = null;
	private String    _modelLocation;
	
	private final static String BUTTOM_DISPLAY_SEL           = "assetdetails_display_sel";
	private final static String BUTTOM_ASSET_DETAIL_DISPLAY = "assetdetails_display";


	public void initialize() throws MXException, RemoteException
	{
		
		WebClientEvent event = clientSession.getCurrentEvent();
		ComponentInstance ci = event.getSourceComponentInstance();
		MboRemote locMbo;
		long uid;
		
		// Called from the viewer.  Use location in event
		if( ci != null )
		{
			DataBean dataBean = ci.getDataBean();
			uid = dataBean.getUniqueIdValue();
			WebClientSession wcs = app.getWebClientSession();
			ControlInstance ctrl;
			if( ci instanceof BIMViewer )
			{
				_viewer = (BIMViewer)ci;
				ctrl = wcs.findControl(Constants.CTRL_ASSET_DETAIL_TBL);
				if( ctrl != null )
				{
					ctrl.setProperty( "selectmode", "multiple" );
				}
				
				Object o = event.getValue();
				if( o == null || !(o instanceof String ))  
				{
					// Should never happen unless the .jsp is altered
					throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
				}
				_modelLocation = (String)o;
			}
			else
			{
				ctrl = wcs.findControl( BUTTOM_DISPLAY_SEL );
				if( ctrl != null )
				{
					ctrl.setVisibility( false );
				}
				ctrl = wcs.findControl( BUTTOM_ASSET_DETAIL_DISPLAY );
				if( ctrl != null )
				{
					ctrl.setVisibility( false );
				}
			}
		}
		// Called from the select action menu.  Use app location
		else
		{
			locMbo = app.getDataBean().getMbo();
			uid = locMbo.getUniqueIDValue();    
		}
		getMboForUniqueId( uid );	

		super.initialize();
	}
	
	public synchronized int execute() 
		throws MXException, RemoteException
	{
		DataBean tableBean = app.getDataBean( Constants.CTRL_ASSET_DETAIL_TBL );
		if( tableBean == null )
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_PRESENTATION_CHANGED );	
		}
		if( _viewer == null )
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		
		WebClientEvent event = clientSession.getCurrentEvent();
		ControlInstance ci = event.getSourceControlInstance();
		if( ci == null )
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		String button = ci.getId();
		HashSet<String> selectionSet  = new HashSet<String>();
		if( button.equalsIgnoreCase( BUTTOM_DISPLAY_SEL ))
		{
			Vector<?> selectedMbos = tableBean.getSelection();
			Enumeration<?> mbos = selectedMbos.elements();
			while( mbos.hasMoreElements() )
			{
				MboRemote mbo = (MboRemote)mbos.nextElement();
				selectionSet.add( mbo.getString( Constants.FIELD_MODELID ) );
			}
		}
		else
		{
			for( int i = 0; i < tableBean.count(); i++ )
			{
				MboRemote mbo = tableBean.getMbo( i );
				selectionSet.add( mbo.getString( Constants.FIELD_MODELID ) );
			}
		}

		MboRemote selection = tableBean.getMbo();
		if( selection == null )
		{
			return EVENT_HANDLED;
		}
		
		_viewer.setMultiSelect( _modelLocation, selectionSet );
		
		return EVENT_HANDLED;
	}

}
