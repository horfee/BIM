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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import psdi.app.bim.BIMService;
import psdi.app.bim.BIMServiceRemote;
import psdi.app.bim.project.BIMOmniClassImport;
import psdi.app.bim.project.BIMOmniClassImportRemote;
import psdi.app.bim.project.BIMOmniClassImportSet;
import psdi.app.bim.project.ImportBase;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.mbo.SqlFormat;
import psdi.mbo.Translate;
import psdi.server.MXServer;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.webclient.controls.Pushbutton;
import psdi.webclient.controls.TabGroup;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.beans.WebClientBean;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.session.WebClientSession;

public class      OmniClassImportBean
extends    DataBean
implements FileUpload
{
	public final static String CTRL_OMNI_IMPORT_BTN      = "omniclass_import";
	public final static String CTRL_OMNI_CANCEL_BTN      = "omniclass_cancel";
	public final static String CTRL_OMNI_BTN_GROUP       = "omniclass_bg";
	public final static String CTRL_OMNI_CLOSE_BTN       = "omniclass_close";
	public final static String CTRL_OMNICLASS_TAB_GROUP  = "omniclass_tabs";
	public final static String CTRL_OMNICLASS_LOG_TAB    = "omniclass_log_tab";

	public final static String CTRL_UNI_IMPORT_BTN      = "uniformat_import";
	public final static String CTRL_UNI_CANCEL_BTN      = "uniformat_cancel";
	public final static String CTRL_UNI_CLOSE_BTN       = "uniformat_close";
	public final static String CTRL_UNI_BTN_GROUP       = "uniformat_bg";
	public final static String CTRL_UNIFORMAT_TAB_GROUP = "uniformat_tabs";
	public final static String CTRL_UNIFORMAT_LOG_TAB   = "uniformat_log_tab";

	private Pushbutton      _cancelButton;
	private Pushbutton      _importButton;
	private Pushbutton      _omniclassCloseButton;
	private Pushbutton      _uniformatCloseButton;
	private ControlInstance _omniclassButtonGroup;
	private ControlInstance _uniformatButtonGroup;
	private long            _uid;
	private MboRemote       _mbo = null;
	private boolean         _initialize = false;

	public void initialize() throws MXException, RemoteException
	{
		if( _initialize ) return;
		super.initialize();
		BIMOmniClassImportSet mboSet = (BIMOmniClassImportSet)getMboSet();
		Translate translator = mboSet.getTranslator();
		String siteId = getString( Constants.FIELD_SITEID );
		String orgId  = getString( Constants.FIELD_ORGID );
		String value[] = translator.getExternalValues( ImportBase.DOMAIN_BIMIMPORTSTATUS, "NEW", siteId, orgId );

		SqlFormat sqlf = new SqlFormat( mboSet.getUserInfo(), ImportBase.FIELD_STATUS + " =:1" );
		sqlf.setObject( 1, BIMOmniClassImport.TABLE_NAME, ImportBase.FIELD_STATUS, value[0] );
		mboSet.setWhere( sqlf.format() );
		mboSet.reset();

		_mbo = getMbo();
		if( _mbo == null )
		{
			insert();
			select( 0 );
			moveTo( 0 );

			_mbo = getMbo();
			MboSetRemote useWithSet = _mbo.getMboSet( "USEWITHLIST" );
			useWithSet.addAtEnd();
		}
		_uid = _mbo.getUniqueIDValue();	
		WebClientSession wcs = app.getWebClientSession();
		_omniclassCloseButton      = (Pushbutton)wcs.findControl("omniclass_close");
		_uniformatCloseButton      = (Pushbutton)wcs.findControl("uniformat_close");
		_omniclassButtonGroup      = wcs.findControl( CTRL_OMNI_BTN_GROUP );
		_uniformatButtonGroup      = wcs.findControl( CTRL_UNI_BTN_GROUP );

		if(_omniclassCloseButton != null)
		{			
			_omniclassCloseButton.setVisibility(false);
		}
		if(_uniformatCloseButton != null)
		{
			_uniformatCloseButton.setVisibility(false);
		}

		_initialize = true;
	}

	public int omniclassImport() 
			throws MXException, 
			RemoteException
			{
		BIMOmniClassImport.setLoaderName("OmniClass");
		WebClientSession wcs = this.app.getWebClientSession();
		_cancelButton = (Pushbutton)wcs.findControl( CTRL_OMNI_CANCEL_BTN);
		_importButton = (Pushbutton)wcs.findControl( CTRL_OMNI_IMPORT_BTN);

		_uid = getUniqueIdValue();

		save();

		BIMOmniClassImportRemote importMbo = (BIMOmniClassImportRemote)getMbo();
		if( importMbo == null )
		{
			importMbo = (BIMOmniClassImportRemote)getMbo( 0 );
		}

		MboSetRemote mboSet = getMboSetRemote();
		importMbo.runImport( "OmniClass" );

		SqlFormat sqlf = new SqlFormat( mboSet.getUserInfo(), BIMOmniClassImport.FIELD_BIMOMNICLASSIMPORTID + " =:1" );
		sqlf.setLong( 1, _uid );
		getMboSet().setWhere( sqlf.format() );
		getMboSet().reset();
		refreshLogs();

		if( _omniclassButtonGroup != null ) _omniclassButtonGroup.setVisibility( false );
		if( _cancelButton != null )         _cancelButton.setVisibility( false );
		if( _importButton != null )         _importButton.setVisibility( false );
		if( _omniclassCloseButton  != null ) _omniclassCloseButton.setVisibility( true );
		selectTab( CTRL_OMNICLASS_TAB_GROUP , "Log" );

		return WebClientBean.EVENT_HANDLED;
			}

	public int uniformatImport() 
			throws MXException, 
			RemoteException
			{		
		BIMOmniClassImport.setLoaderName("Uniformat");		
		WebClientSession wcs = this.app.getWebClientSession();
		_cancelButton = (Pushbutton)wcs.findControl( CTRL_UNI_CANCEL_BTN);
		_importButton = (Pushbutton)wcs.findControl( CTRL_UNI_IMPORT_BTN);

		_uid = getUniqueIdValue();

		save();

		BIMOmniClassImportRemote importMbo = (BIMOmniClassImportRemote)getMbo();
		if( importMbo == null )
		{
			importMbo = (BIMOmniClassImportRemote)getMbo( 0 );
		}

		MboSetRemote mboSet = getMboSetRemote();
		importMbo.runImport( "Uniformat" );

		SqlFormat sqlf = new SqlFormat( mboSet.getUserInfo(), BIMOmniClassImport.FIELD_BIMOMNICLASSIMPORTID + " =:1" );
		sqlf.setLong( 1, _uid );
		getMboSet().setWhere( sqlf.format() );
		getMboSet().reset();
		refreshLogs();

		if( _uniformatButtonGroup != null ) _uniformatButtonGroup.setVisibility( false );
		if( _cancelButton != null )         _cancelButton.setVisibility( false );
		if( _importButton != null )         _importButton.setVisibility( false );
		if( _uniformatCloseButton  != null ) _uniformatCloseButton.setVisibility( true );
		selectTab( CTRL_UNIFORMAT_TAB_GROUP ,  "Log" );		

		return WebClientBean.EVENT_HANDLED;
			}


	public void clearClassification() 
			throws MXException
			{
		ControlInstance compInst = clientSession.getCurrentEvent().getSourceControlInstance();
		String originalAttr=compInst.getProperty("dataattribute");
		String classAttr = null;

		int index = originalAttr.indexOf(".");
		if( index > 0 )
		{
			classAttr = originalAttr.substring(0, index)  + "ID";  
			setValue(classAttr, "" );
		}
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
			String tab_grp , String tab) {
		WebClientSession wcs = this.app.getWebClientSession();
		ControlInstance ctrl;
		ctrl = wcs.findControl( tab_grp );
		if( ctrl == null || !(ctrl instanceof TabGroup )) return;
		TabGroup tabCtrl = (TabGroup)ctrl;
		tabCtrl.setCurrentTab( tab );
	}

	synchronized public int cancelDialog() 
			throws MXException, RemoteException
			{
		super.cancelDialog();

		try
		{
			String workingDirRoot = MXServer.getMXServer().getProperty( BIMService.PROP_NAME_BIM_WORKING_DIR );
			File workingDir = new File( workingDirRoot );

			File file = new File( getString( BIMOmniClassImport.FIELD_FILENAME ) );
			File currentDirectory = file.getParentFile();
			file.delete();

			while (!currentDirectory.equals(workingDir))
			{
				File deleteDirectory = currentDirectory;
				currentDirectory = deleteDirectory.getParentFile();

				if (!deleteDirectory.delete())
				{
					break;
				} 
			}
		}
		catch( Exception e )
		{ /* Ignore. Really couldn't have gotten her if this is bad */ }

		return EVENT_HANDLED;
			}

	public void setServerFileName(
			MboRemote targetMbo,
			String enentSource,
			String fileName 
			) 
					throws MXException, 
					RemoteException
					{
		targetMbo.setValue( BIMOmniClassImport.FIELD_FILENAME, fileName );
					}

	public void setClientFileName(
			MboRemote targetMbo,
			String enentSource,
			String fileName 
			) 
					throws MXException, 
					RemoteException
					{
		targetMbo.setValue( BIMOmniClassImport.FIELD_SOURCEFILENAME, fileName );
					}

	public String getDirectoryPath(
			String enentSource 
			) 
					throws IOException, MXException
					{
		String rootDir = MXServer.getMXServer().getProperty( BIMService.PROP_NAME_BIM_WORKING_DIR );

		// Check to make sure directory exists
		File dir = new File(rootDir);
		if (!dir.exists())
		{
			throw new MXApplicationException( Constants.BUNDLE_IMPORT_MSG, 
					Constants.ERR_BIM_DIR_MISSING );	
		}

		MboRemote mbo = getMbo();

		String importId = mbo.getString( BIMOmniClassImport.FIELD_BIMOMNICLASSIMPORTID );
		String siteId   = mbo.getString( BIMOmniClassImport.FIELD_SITEID );
		if( siteId != null && siteId.length() > 0 )
		{
			rootDir = rootDir + File.separator + siteId;
		}
		rootDir = rootDir + File.separator + importId;

		File file = new File(rootDir);
		BIMServiceRemote bsr = (BIMServiceRemote) MXServer.getMXServer().lookup( "BIM" );
		bsr.makeDir(file);

		return rootDir;
					}
}
