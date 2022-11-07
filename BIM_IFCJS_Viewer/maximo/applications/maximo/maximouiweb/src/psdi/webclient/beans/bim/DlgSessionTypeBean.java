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

import psdi.app.bim.project.BIMProjectRemote;
import psdi.app.bim.project.BIMSession;
import psdi.mbo.MboRemote;
import psdi.util.MXException;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.controller.WebClientEvent;
import psdi.webclient.system.session.WebClientSession;

public class DlgSessionTypeBean
    extends DataBean
{
	public static final String CTRL_OK_BUTTON     = "session_selection_type_ok";
	public static final String CTRL_SESSION_TABLE = "sessions_table";
	public static final String CTRL_MERGE_RB     = "session_type_selection_rb_merge";
	public static final String CTRL_UPDATE_RB     = "session_type_selection_rb_update";
	public static final String CTRL_EXPORT_RB     = "session_type_selection_rb_export";
	
	@Override
	public void initialize() 
		throws MXException, RemoteException
	{
		insert();
		select( 0 );
		moveTo( 0 );
		
		MboRemote mbo = getMbo();
		mbo = getMbo( 0 );

		super.initialize();
		
		WebClientSession wcs = this.app.getWebClientSession();
		ControlInstance ctrlMerge = wcs.findControl( CTRL_MERGE_RB );
		ControlInstance ctrlUpdate = wcs.findControl( CTRL_UPDATE_RB );
		ControlInstance ctrlExport = wcs.findControl( CTRL_EXPORT_RB );
		mbo = app.getDataBean().getMbo();
		if( mbo != null && mbo instanceof BIMProjectRemote )
		{
			BIMProjectRemote project = (BIMProjectRemote)mbo;
			if( !project.hasValidImport() )
			{
				if( ctrlUpdate != null )
				{
					ctrlUpdate.setDisabled( true );
					ctrlUpdate.setVisibility( false );
				}
				if( ctrlExport != null )
				{
					ctrlExport.setDisabled( true );
					ctrlExport.setVisibility( false );
				}
			}
			else
			{
				if( ctrlMerge != null )
				{
					ctrlMerge.setDisabled( true );
					ctrlMerge.setVisibility( false );
				}
			}
		}
	}
	
	@Override
	synchronized public int execute() 
		throws MXException, 
		       RemoteException
	{
		DataBean sessionBean = app.getDataBean( CTRL_SESSION_TABLE );
		if( sessionBean == null || !(sessionBean instanceof BIMSessionBean ))
		{
			return EVENT_STOP_ALL;
		}
		String sessiontType = getMbo().getString( BIMSession.FIELD_SESSIONTYPE );
		clientSession.queueEvent(new WebClientEvent("addNewRow", CTRL_SESSION_TABLE, sessiontType, clientSession));
		clientSession.queueEvent(new WebClientEvent("uploadCOBieCVSFiles", CTRL_SESSION_TABLE, sessiontType, clientSession));
		return super.execute();
	}
}
