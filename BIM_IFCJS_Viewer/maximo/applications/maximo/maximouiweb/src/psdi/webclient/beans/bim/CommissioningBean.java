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
package psdi.webclient.beans.bim;

import java.rmi.RemoteException;

import psdi.app.bim.project.BIMCommission;
import psdi.app.bim.project.BIMCommissionRemote;
import psdi.app.bim.project.BIMCommissionSet;
import psdi.app.bim.project.BIMProject;
import psdi.app.bim.project.ImportBase;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.mbo.SqlFormat;
import psdi.mbo.Translate;
import psdi.util.MXException;
import psdi.webclient.controls.Pushbutton;
import psdi.webclient.controls.TabGroup;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.beans.WebClientBean;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.session.WebClientSession;

public class      CommissioningBean
       extends    DataBean
{
	public final static String CTRL_COMM_OK_BTN     = "commission_ok";
	public final static String CTRL_COMM_BTN_GROUP  = "commission_bg";
	public final static String CTRL_COMM_CANCEL_BTN = "commission_cancel";
	public final static String CTRL_COMM_CLOSE_BTN  = "commission_close";

	public final static String CTRL_COMM_TAB_GROUP  = "commission_tabs";
	public final static String CTRL_COMM_LOG_TAB    = "commission_log_tab";

	private ControlInstance _btnGroup;
	private Pushbutton _cancelButton;
	private Pushbutton _closeButton;
	private Pushbutton _commButton;
	private long       _uid;
	private MboRemote  _mbo = null;
	private boolean    _initialize = false;

	@Override
    public void initialize() throws MXException, RemoteException
	{
		if( _initialize ) return;
		super.initialize();
		BIMCommissionSet mboSet = (BIMCommissionSet)getMboSet();
		Translate translator = mboSet.getTranslator();
		String siteId = getString( Constants.FIELD_SITEID );
		String orgId  = getString( Constants.FIELD_ORGID );
		String value[] = translator.getExternalValues( ImportBase.DOMAIN_BIMIMPORTSTATUS, "NEW", siteId, orgId );
		
		MboRemote appMbo = app.getAppBean().getMbo();
		
		SqlFormat sqlf = new SqlFormat( mboSet.getUserInfo(), BIMCommission.FIELD_BIMPROJECTID + "=:1 AND " + ImportBase.FIELD_STATUS + " =:2" );
		sqlf.setObject( 1, BIMCommission.TABLE_NAME, BIMCommission.FIELD_BIMPROJECTID, "" + appMbo.getLong( BIMProject.FIELD_BIMPROJECTID ) );
		sqlf.setObject( 2, BIMCommission.TABLE_NAME, ImportBase.FIELD_STATUS, value[0] );
		mboSet.setWhere( sqlf.format() );
		mboSet.reset();
		
		_mbo = getMbo();
		if( _mbo == null )
		{
			insert();
			select( 0 );
			moveTo( 0 );
			
			_mbo = getMbo();
		}
		_uid = _mbo.getUniqueIDValue();

		WebClientSession wcs = this.app.getWebClientSession();
		_btnGroup     = wcs.findControl( CTRL_COMM_BTN_GROUP );
		_commButton   = (Pushbutton)wcs.findControl( CTRL_COMM_OK_BTN );
		_cancelButton = (Pushbutton)wcs.findControl( CTRL_COMM_CANCEL_BTN );
		_closeButton  = (Pushbutton)wcs.findControl( CTRL_COMM_CLOSE_BTN );
		if( _closeButton  != null ) _closeButton.setVisibility( false);
		
		_initialize = true;
	}
	
	public int commission() 
		throws MXException, 
		       RemoteException
	{
		_uid = getUniqueIdValue();

		save();

		BIMCommissionRemote importMbo = (BIMCommissionRemote)getMbo();
		if( importMbo == null )
		{
			importMbo = (BIMCommissionRemote)getMbo( 0 );
		}

		MboSetRemote mboSet = getMboSetRemote();
		importMbo.commision();
		
		SqlFormat sqlf = new SqlFormat( mboSet.getUserInfo(), BIMCommission.FIELD_BIMCOMMISSIONID + " =:1" );
		sqlf.setLong( 1, _uid );
		getMboSet().setWhere( sqlf.format() );
		getMboSet().reset();
		refreshLogs();
		
		if( _btnGroup != null )
		{
			_btnGroup.setVisibility( false );
		}
		if( _cancelButton != null ) _cancelButton.setVisibility( false );
		if( _commButton != null )   _commButton.setVisibility( false );
		if( _closeButton  != null ) _closeButton.setVisibility( true );
		selectTab( "Log" );

		return WebClientBean.EVENT_HANDLED;
	}
	
	/**
	 * The model loader runs as a background thread. and write status back to
	 * the session.  This method forces reload from the database to display
	 * the current load status
	 * @return
	 * @throws MXException
	 * @throws RemoteException
	 */
	public int refreshLogs() 
		throws MXException, RemoteException
	{
		getMboSet().reset();
		getMboForUniqueId( _uid );
		
		return EVENT_HANDLED;
	}

	private void selectTab(
		String tab
	) {
		WebClientSession wcs = this.app.getWebClientSession();
		ControlInstance ctrl;
		ctrl = wcs.findControl( CTRL_COMM_TAB_GROUP );
		if( ctrl == null || !(ctrl instanceof TabGroup )) return;
		TabGroup tabCtrl = (TabGroup)ctrl;
		tabCtrl.setCurrentTab( tab );
	}
	
}
