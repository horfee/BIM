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

import psdi.app.bim.project.BIMProjectRemote;
import psdi.app.bim.project.BIMSession;
import psdi.app.bim.project.BIMSessionRemote;
import psdi.app.bim.project.BIMUploadSet;
import psdi.app.bim.project.BIMUploadSetRemote;
import psdi.mbo.MboRemote;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.webclient.controls.Pushbutton;
import psdi.webclient.controls.Tab;
import psdi.webclient.controls.TabGroup;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.controller.WebClientEvent;
import psdi.webclient.system.session.WebClientSession;

public class      BIMUploadBean
       extends    DataBean
{
	public final static String SIGOPT_ADVANCED                  = "BIM_LOCAL_OPT";
	
	public final static String CTRL_COBIE_UPLOAD_TABLE          = "cobieup_upload_tbl";
	public final static String CTRL_COBIE_TAB_GROUP             = "cobieup_tabs";
	public final static String CTRL_COBIE_UPLOAD_TAB            = "cobieup_upload_tab";
	public final static String CTRL_COBIE_FACILITIES_TAB        = "cobieup_facility_tab";
	public final static String CTRL_COBIE_UPDATE_TAB            = "cobieup_options_tab_update";
	public final static String CTRL_COBIE_OP_TAB                = "cobieup_options_tab";
	public final static String CTRL_COBIE_ATTRIB_TAB            = "cobieup_attrib_tab";
	public final static String CTRL_COBIE_DEFAULT_TAB           = "cobieup_default_tab";
	public final static String CTRL_COBIE_FILTER_TAB            = "cobieup_options_tab_filter";
	public final static String CTRL_COBIE_ADVANCED_TAB          = "cobieup_advanced_tab";
	public final static String CTRL_COBIE_EXPORT_TAB            = "cobie_export_tab";
	public final static String CTRL_COBIE_LOG_TAB               = "cobieup_log_tab";
	
	public final static String CTRL_COBIE_DEFAULTS              = "cobieup_default_tab_opt_defaults";
	
	public final static String CTRL_COBIE_TARGET_FACILITY       = "cobie_target_facility";			// Merge with previous import
	public final static String CTRL_COBIE_MERGE_FACILITY        = "cobie_merge_facility";			// Merge with existing data
	public final static String CTRL_COBIE_UPDATE_MODE           = "cobie_update_mode";
	public final static String CTRL_COBIE_UPDATE_CAT            = "cobieup_update_cat";
	public final static String CTRL_COBIE_UPDATE_SPEC           = "cobieup_update_spec";
	
	public final static String CTRL_COBIEUP_BTN_SAVE            = "cobieup_btn_save";
	public final static String CTRL_COBIEUP_BTN_VALIDATE        = "cobieup_btn_validate";
	public final static String CTRL_COBIEUP_BTN_IMPORT          = "cobieup_btn_import";
	public final static String CTRL_COBIEUP_BTN_MERGE           = "cobieup_btn_merge";
	public final static String CTRL_COBIEUP_BTN_UPDATE          = "cobieup_btn_update";
	public final static String CTRL_COBIEUP_BTN_EXPORT          = "cobieup_btn_export";
	public final static String CTRL_COBIEUP_BTN_CANCEL          = "cobieup_btn_cancel";
	public final static String CTRL_COBIEUP_BTN_GROUP_CLOSE     = "cobieup_btn_1";
	public final static String CTRL_COBIEUP_BTN_CLOSE           = "cobieup_btn_close";
	public final static String CTRL_COBIEUP_BTN_NEWROW          = "cobieup_btn_newrow";
	
	public final static String CTRL_COBIEUP_TRASH               = "cobieup_upload_tbl_4";
	
	
	public final static String CTRL_COBIEUP_FACILITY_CLASS           = "cobieup_facility_class";

	public final static String CTRL_COBIEUP_OP_ATTRIB_FILETER_NAME   = "cobieup_op_attrib_filter_name";
	public final static String CTRL_COBIEUP_OP_ATTRIB_MAP_NAME       = "cobieup_op_attrib_map_name";
	public final static String CTRL_COBIEUP_OP_ATTRIB_TYPE_MAP_NAME  = "cobieup_op_spec_map_name";
	public final static String CTRL_COBIEUP_OP_AREA_ATTRIB           = "cobieup_op_area_attrib";
	public final static String CTRL_COBIEUP_OP_ASSET_ATTRIB_LOC      = "cobieup_op_asset_attrib_loc";
	public final static String CTRL_COBIEUP_OP_ATTRIB_AT_SITE        = "cobieup_op_attrib_at_site";
	public final static String CTRL_COBIEUP_OP_BARCODE               = "cobieup_op_barcode";
	public final static String CTRL_COBIEUP_OP_CONTACT_TREATMENT     = "cobieup_op_contact_treatment";
	public final static String CTRL_COBIEUP_OP_CONVERT_GUIDS         = "cobieup_op_convert_guids";
	public final static String CTRL_COBIEUP_OP_COPY_TYPE_ATTRIBS     = "cobieup_op_copy_type_attribs";
	public final static String CTRL_COBIEUP_OP_CREATE_CLASS          = "cobieup_op_create_class";
	public final static String CTRL_COBIEUP_OP_DELETE_FILES          = "cobieup_op_deletefiles";
	public final static String CTRL_COBIEUP_OP_LOG_LEVEL             = "cobieup_op_loglevel";
	public final static String CTRL_COBIEUP_OP_INIT_ASSET_STATUS     = "cobieup_op_init_asset_status";
	public final static String CTRL_COBIEUP_OP_INIT_ASSET_TYPE       = "cobieup_op_init_asset_type";
	public final static String CTRL_COBIEUP_OP_INIT_JP_STATUS        = "cobieup_op_init_jp_status";
	public final static String CTRL_COBIEUP_OP_INIT_LOC_STATUS       = "cobieup_op_init_loc_status";
	public final static String CTRL_COBIEUP_OP_INIT_PROD_STATUS      = "cobieup_op_init_product_status";
	public final static String CTRL_COBIEUP_OP_INFER_AREA            = "cobieup_op_infer_area";
	public final static String CTRL_COBIEUP_OP_INFER_LEVELS          = "cobieup_op_infer_levels";
	public final static String CTRL_COBIEUP_OP_INFER_SPACES          = "bim_config_mdl_op_infer_spacess";
	public final static String CTRL_COBIEUP_OP_INFER_SYSTEMS         = "cobieup_op_infer_systems";
	public final static String CTRL_COBIEUP_OP_INFER_OMNICLASS       = "cobieup_op_infer_omniclass";
	public final static String CTRL_COBIEUP_OP_INFER_PERIMETER       = "cobieup_op_infer_perimeter";
	public final static String CTRL_COBIEUP_OP_ITEM_MASTER           = "cobieup_op_item_mast";
	public final static String CTRL_COBIEUP_OP_JP_AT_SITE            = "cobieup_op_jp_at_site";
	public final static String CTRL_COBIEUP_OP_LEVEL_ATTRIB          = "cobieup_op_level_attrib";
	public final static String CTRL_COBIEUP_OP_MAP_EXT_COLS          = "cobieup_op_map_ext_cols";
	public final static String CTRL_COBIEUP_OP_OMNICLASS_ATTRIB      = "cobieup_op_omniclass_attrib";
	public final static String CTRL_COBIEUP_OP_PERIMETER_ATTRIB      = "cobieup_op_perimeter_attrib";
	public final static String CTRL_COBIEUP_OP_PERSON_EMAIL          = "cobieup_op_person_email";
	public final static String CTRL_COBIEUP_OP_POPULATE_SYSTEM_MAP   = "cobieup_op_populate_system_map";
	public final static String CTRL_COBIEUP_OP_PROJECT_ADDRESS       = "cobieup_op_project_address";
	public final static String CTRL_COBIEUP_OP_PROMOTE_COMPONETS     = "cobieup_op_promote_componets";
	public final static String CTRL_COBIEUP_OP_PROMOTE_SPACES        = "cobieup_op_promote_spaces";
	public final static String CTRL_COBIEUP_OP_SKIP_ON_NULL          = "cobieup_op_skip_null";
	public final static String CTRL_COBIEUP_OP_SKIP_OM_VALUE_IS_NAME = "cobieup_op_skip_value_is_name";
	public final static String CTRL_COBIEUP_OP_SPACE_ID              = "cobieup_op_space_id";
	public final static String CTRL_COBIEUP_OP_SPACE_ATTRIB          = "bim_config_mdl_op_space_attrib";
	public final static String CTRL_COBIEUP_OP_SYSTEM_ATTRIB         = "cobieup_op_system_attrib";
	public final static String CTRL_COBIEUP_OP_TYPES_ARE_SPECS       = "cobieup_op_types_are_spec";
	public final static String CTRL_COBIEUP_OP_UNIT_TREATEMENT       = "cobieup_op_unit_treatment";
	public final static String CTRL_COBIEUP_OP_VENDOR                = "cobieup_op_vendor_attrib";
	public final static String CTRL_COBIEUP_OP_WARRANTY_CALC         = "cobieup_op_warranty_calc";
	public final static String CTRL_COBIEUP_OP_BARCODE_ATTRIB        = "cobieup_op_barcode_attrib";
	public final static String CTRL_COBIEUP_OP_AUTONUMBER            = "cobieup_op_autonumber";
	public final static String CTRL_COBIEUP_OP_COPY_TYPE_ITEM        = "cobieup_op_copy_type_item";
	
	public final static String CTRL_COBIEUP_SERVICE_ADDRESS          = "cobieup_service_address";
	public final static String CTRL_COBIEUP_BILL_ADDRESS             = "cobieup_bill_address";
	public final static String CTRL_COBIEUP_SHIP_ADDRESS             = "cobieup_ship_address";
	public final static String CTRL_COBIEUP_GLACCOUNT                = "cobieup_glaccount";
	
	// Export options
	public final static String CTRL_COBIE_EXPORT_FACILITY = "cobie_export_facility";
	public final static String CTRL_COBIE_EXPORT_EXPORTID = "cobie_export_exportid";
	public final static String CTRL_COBIE_EXPORT_LOGLEVEL = "cobie_export_loglevel";
	public final static String CTRL_COBIE_EXPORT_FLOOR = "cobie_export__floor";
	public final static String CTRL_COBIE_EXPORT_SPACE = "cobie_export__space";
	public final static String CTRL_COBIE_EXPORT_ZONE = "cobie_export__zone";
	public final static String CTRL_COBIE_EXPORT_COMPONENT = "cobie_export__component";
	public final static String CTRL_COBIE_EXPORT_TYPE = "cobie_export__type";
	public final static String CTRL_COBIE_EXPORT_SYSTEM = "cobie_export__system";
	public final static String CTRL_COBIE_EXPORT_JOB = "cobie_export__job";
	public final static String CTRL_COBIE_EXPORT_CONTACT = "cobie_export__contact";
	public final static String CTRL_COBIE_EXPORT_DOC = "cobie_export__document";
	public final static String CTRL_COBIE_EXPORT_ATTRIB = "cobie_export__attribute";

	
	private int _sessionType = BIMSession.SESSION_TYPE_UNKNOWN;

	private static final String readonlyWhenNotNew[] = 
	{
		CTRL_COBIEUP_OP_ATTRIB_FILETER_NAME,
		CTRL_COBIEUP_OP_ATTRIB_MAP_NAME,
		CTRL_COBIEUP_OP_ATTRIB_TYPE_MAP_NAME,
		CTRL_COBIEUP_FACILITY_CLASS,
		CTRL_COBIE_UPLOAD_TABLE,
		CTRL_COBIEUP_OP_AREA_ATTRIB,
		CTRL_COBIEUP_OP_ASSET_ATTRIB_LOC,
		CTRL_COBIEUP_OP_ATTRIB_AT_SITE,
		CTRL_COBIEUP_OP_AUTONUMBER,
		CTRL_COBIEUP_OP_BARCODE,
		CTRL_COBIEUP_OP_BARCODE_ATTRIB,
		CTRL_COBIEUP_OP_CONTACT_TREATMENT,
		CTRL_COBIEUP_OP_CONVERT_GUIDS,
		CTRL_COBIEUP_OP_COPY_TYPE_ATTRIBS,
		CTRL_COBIEUP_OP_COPY_TYPE_ITEM,
		CTRL_COBIEUP_OP_CREATE_CLASS,
		CTRL_COBIEUP_OP_LOG_LEVEL,
		CTRL_COBIEUP_OP_DELETE_FILES,
		CTRL_COBIEUP_OP_INIT_ASSET_STATUS,
		CTRL_COBIEUP_OP_INIT_ASSET_TYPE,
		CTRL_COBIEUP_OP_INIT_LOC_STATUS,
		CTRL_COBIEUP_OP_INIT_JP_STATUS,
		CTRL_COBIEUP_OP_INIT_PROD_STATUS,
		CTRL_COBIEUP_OP_JP_AT_SITE,
		CTRL_COBIEUP_OP_INFER_AREA,
		CTRL_COBIEUP_OP_INFER_LEVELS,
		CTRL_COBIEUP_OP_INFER_OMNICLASS,
		CTRL_COBIEUP_OP_INFER_PERIMETER,
		CTRL_COBIEUP_OP_INFER_SPACES,
		CTRL_COBIEUP_OP_ITEM_MASTER,
		CTRL_COBIEUP_OP_LEVEL_ATTRIB,
		CTRL_COBIEUP_OP_MAP_EXT_COLS,
		CTRL_COBIEUP_OP_INFER_SYSTEMS,
		CTRL_COBIEUP_OP_OMNICLASS_ATTRIB,
		CTRL_COBIEUP_OP_PERIMETER_ATTRIB,
		CTRL_COBIEUP_OP_PERSON_EMAIL, 
		CTRL_COBIEUP_OP_POPULATE_SYSTEM_MAP,
		CTRL_COBIEUP_OP_PROJECT_ADDRESS,
		CTRL_COBIEUP_OP_PROMOTE_SPACES,
		CTRL_COBIEUP_OP_PROMOTE_COMPONETS,
		CTRL_COBIEUP_OP_SKIP_ON_NULL,
		CTRL_COBIEUP_OP_SKIP_OM_VALUE_IS_NAME,
		CTRL_COBIEUP_OP_SPACE_ID,
		CTRL_COBIEUP_OP_SPACE_ATTRIB,
		CTRL_COBIEUP_OP_SYSTEM_ATTRIB,
		CTRL_COBIEUP_OP_TYPES_ARE_SPECS,
		CTRL_COBIEUP_OP_UNIT_TREATEMENT,
		CTRL_COBIEUP_OP_VENDOR,
		CTRL_COBIEUP_OP_WARRANTY_CALC,
		
		CTRL_COBIEUP_SERVICE_ADDRESS,
		CTRL_COBIEUP_BILL_ADDRESS,
		CTRL_COBIEUP_SHIP_ADDRESS,
		CTRL_COBIEUP_GLACCOUNT,
		
		CTRL_COBIE_TARGET_FACILITY,
		CTRL_COBIE_MERGE_FACILITY,
		CTRL_COBIE_UPDATE_MODE,
		CTRL_COBIE_UPDATE_CAT,
		CTRL_COBIE_UPDATE_SPEC,
		
		// Export options
		CTRL_COBIE_EXPORT_FACILITY,
		CTRL_COBIE_EXPORT_EXPORTID,
		CTRL_COBIE_EXPORT_LOGLEVEL,
		CTRL_COBIE_EXPORT_FLOOR,
		CTRL_COBIE_EXPORT_SPACE,
		CTRL_COBIE_EXPORT_ZONE,
		CTRL_COBIE_EXPORT_COMPONENT,
		CTRL_COBIE_EXPORT_TYPE,
		CTRL_COBIE_EXPORT_SYSTEM,
		CTRL_COBIE_EXPORT_JOB,
		CTRL_COBIE_EXPORT_CONTACT,
		CTRL_COBIE_EXPORT_DOC,
		CTRL_COBIE_EXPORT_ATTRIB
	};
	
	@Override
	protected void initialize() throws MXException, RemoteException
	{
		super.initialize();
		
		WebClientEvent event = clientSession.getCurrentEvent();
		Object o = event.getValue();
		long id = Long.parseLong( (String)o );
		MboRemote mbo = getMboForUniqueId( id );

		if( mbo != null && mbo instanceof BIMSessionRemote )
		{
			BIMSessionRemote session = (BIMSessionRemote)mbo;
			_sessionType = session.getSessionType();
			if( session.isStatusNew() )
			{
				setupButtons( true );
			}
			else
			{
				setupButtons( false );
			}
			mbo.select();
			setupTabs();
			setTitle();
		}
		else
		{
			setupButtons( true );
		}
	}
	
	@Override
    synchronized public int execute() 
		throws MXException, RemoteException
	{
		refreshSessionTable();
		
		DataBean sessionBean = app.getDataBean( CTRL_COBIE_UPLOAD_TABLE );
		BIMUploadSetRemote bimer = (BIMUploadSetRemote) sessionBean.getMboSet();
		bimer.deleteFiles( BIMUploadSet.DETETE_FILES_MARKED_FOR_DETLETE );
		
		return super.execute();
	}

	@Override
    synchronized public int cancelDialog() 
		throws MXException, RemoteException
	{
		super.cancelDialog();
		
		DataBean sessionBean = app.getDataBean( CTRL_COBIE_UPLOAD_TABLE );
		BIMUploadSetRemote bimer = (BIMUploadSetRemote) sessionBean.getMboSet();
		bimer.deleteFiles( BIMUploadSet.DETETE_FILES_MARKED_FOR_DETLETE | BIMUploadSet.DETETE_FILES_MODIFIED );
		
		refreshSessionTable();
		return EVENT_HANDLED;
	}
	
	
	public int validateBuilding() 
		throws MXException, 
		       RemoteException
	{
			return processAction( BIMSession.UPDATE_VALIDATE_ONLY );
	}
	
	public int importBuilding() 
		throws MXException, 
		       RemoteException
	{
		return processAction( BIMSession.UPDATE_INCREMENTAL );
	}
	
	public int updateBuilding() 
		throws MXException, 
		       RemoteException
	{
		MboRemote mbo = getMbo();
		if( !( mbo instanceof BIMSessionRemote ))
		{
			throw new MXApplicationException(Constants.BUNDLE_MSG_IMPORT, Constants.MSG_INTERNAL_ERR );
		}
		BIMSessionRemote sessionMbo = (BIMSessionRemote)mbo;
		return processAction( sessionMbo.getUpdateBehavior() );
	}

	public int exportBuilding() 
		throws MXException, 
		       RemoteException
	{
		save();

		MboRemote mbo = getMbo();
		if( !( mbo instanceof BIMSessionRemote ))
		{
			throw new MXApplicationException(Constants.BUNDLE_MSG_IMPORT, Constants.MSG_INTERNAL_ERR );
		}
		BIMSessionRemote sessionMbo = (BIMSessionRemote)mbo;
		
		
		TabGroup tg = (TabGroup) this.app.getWebClientSession().findControl( CTRL_COBIE_TAB_GROUP );
		Tab tab = (Tab) this.app.getWebClientSession().findControl( CTRL_COBIE_LOG_TAB );

		sessionMbo.exportToCOBie();

		if( tg != null && tab != null )
		{
			tg.setCurrentTab(tab, true);

		}

		setupButtons( false );
		selectTab( CTRL_COBIE_LOG_TAB );

		this.app.getDataBean().fireDataChangedEvent();
		refreshSessionTable();
		refreshLogs();
		return EVENT_HANDLED;
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
			classAttr = originalAttr.substring(0, index)  + "IFICATIONID";  
			setValue(classAttr, "" );
		}
	}

	
	public void setupButtons(
		boolean isNew
	) 
		throws RemoteException, 
		       MXException 
	{
		WebClientSession wcs = this.app.getWebClientSession();
		Pushbutton saveButton     = (Pushbutton)wcs.findControl( CTRL_COBIEUP_BTN_SAVE );
		Pushbutton validateButton = (Pushbutton)wcs.findControl( CTRL_COBIEUP_BTN_VALIDATE );
		Pushbutton importButton   = (Pushbutton)wcs.findControl( CTRL_COBIEUP_BTN_IMPORT );
		Pushbutton mergeButton    = (Pushbutton)wcs.findControl( CTRL_COBIEUP_BTN_MERGE );
		Pushbutton updateButton   = (Pushbutton)wcs.findControl( CTRL_COBIEUP_BTN_UPDATE );
		Pushbutton exportButton   = (Pushbutton)wcs.findControl( CTRL_COBIEUP_BTN_EXPORT );
		Pushbutton cancelButton   = (Pushbutton)wcs.findControl( CTRL_COBIEUP_BTN_CANCEL );
		Pushbutton closeButton    = (Pushbutton)wcs.findControl( CTRL_COBIEUP_BTN_CLOSE );
		Pushbutton newrowButton   = (Pushbutton)wcs.findControl( CTRL_COBIEUP_BTN_NEWROW );
		ControlInstance popSysButton = wcs.findControl( CTRL_COBIEUP_OP_POPULATE_SYSTEM_MAP );
		
		ControlInstance targetText = wcs.findControl( CTRL_COBIE_TARGET_FACILITY );
		ControlInstance mergeText  = wcs.findControl( CTRL_COBIE_MERGE_FACILITY );
		ControlInstance exportText = wcs.findControl( CTRL_COBIE_EXPORT_FACILITY );
		
		boolean hasValidImport = false;
		MboRemote mbo = app.getDataBean().getMbo();
		BIMProjectRemote project = (BIMProjectRemote)mbo;
		if( mbo != null && mbo instanceof BIMProjectRemote )
		{
			hasValidImport = project.hasValidImport();
		}

		if( mergeText != null ) 
		{
			switch( _sessionType )
			{
			case BIMSession.SESSION_TYPE_VALIDATE:
				if( hasValidImport )
				{
					if( mergeText != null ) mergeText.setVisibility( false );
				}
				else
				{
					if( targetText != null ) targetText.setVisibility( false );
				}
				break;
			case BIMSession.SESSION_TYPE_IMPORT:
				mergeText.setVisibility( false );
				if( !hasValidImport )
				{
					if( targetText != null ) targetText.setVisibility( false );
				}
				popSysButton.setVisibility( false );
				break;
			case BIMSession.SESSION_TYPE_MERGE:
				if( mergeText != null ) mergeText.setProperty( "inputmode", "required" );
				if( targetText != null ) targetText.setVisibility( false );
				popSysButton.setVisibility( false );
				break;
			case BIMSession.SESSION_TYPE_UPDATE:
				mergeText.setVisibility( false );
				if( targetText != null ) targetText.setProperty( "inputmode", "required" );
				popSysButton.setVisibility( false );
				break;
			case BIMSession.SESSION_TYPE_EXPORT:
				mergeText.setVisibility( false );
				popSysButton.setVisibility( false );
				break;
			}
		}
		
		if( exportText != null && _sessionType == BIMSession.SESSION_TYPE_EXPORT )
		{
			exportText.setProperty( "inputmode", "required" );
		}

		if( isNew )
		{
			switch( _sessionType )
			{
			case BIMSession.SESSION_TYPE_VALIDATE:
				if( importButton   != null ) importButton.setVisibility( false );
				if( mergeButton    != null ) mergeButton.setVisibility( false );
				if( updateButton   != null ) updateButton.setVisibility( false );
				if( exportButton   != null ) exportButton.setVisibility( false );
				if( validateButton != null ) validateButton.setVisibility( true );
				break;
			case BIMSession.SESSION_TYPE_IMPORT:
				if( mergeButton    != null ) mergeButton.setVisibility( false );
				if( updateButton   != null ) updateButton.setVisibility( false );
				if( exportButton   != null ) exportButton.setVisibility( false );
				if( validateButton != null ) validateButton.setVisibility( false );
				if( importButton   != null ) importButton.setVisibility( true );
				break;
			case BIMSession.SESSION_TYPE_MERGE:
				if( importButton   != null ) importButton.setVisibility( false );
				if( exportButton   != null ) exportButton.setVisibility( false );
				if( validateButton != null ) validateButton.setVisibility( false );
				if( updateButton   != null ) updateButton.setVisibility( false );
				if( mergeButton    != null ) mergeButton.setVisibility( true );
				break;
			case BIMSession.SESSION_TYPE_UPDATE:
				if( importButton   != null ) importButton.setVisibility( false );
				if( exportButton   != null ) exportButton.setVisibility( false );
				if( validateButton != null ) validateButton.setVisibility( false );
				if( updateButton   != null ) updateButton.setVisibility( true );
				if( mergeButton    != null ) mergeButton.setVisibility( false );
				break;
			case BIMSession.SESSION_TYPE_EXPORT:
				if( importButton   != null ) importButton.setVisibility( false );
				if( mergeButton    != null ) mergeButton.setVisibility( false );
				if( updateButton   != null ) updateButton.setVisibility( false );
				if( validateButton != null ) validateButton.setVisibility( false );
				if( exportButton   != null ) exportButton.setVisibility( true );
				break;
			}
			if( saveButton     != null ) saveButton.setVisibility( true );
			if( cancelButton   != null ) cancelButton.setVisibility( true );
			if( closeButton    != null ) closeButton.setVisibility( false );
			if( newrowButton   != null ) newrowButton.setVisibility( true );
		}
		else
		{ 
			ControlInstance btnGroup   = wcs.findControl( CTRL_COBIEUP_BTN_GROUP_CLOSE );
			if( btnGroup != null )
			{
				btnGroup.setVisibility( false );
			}
			if( saveButton     != null ) 
			{
				saveButton.setVisibility( false);
				saveButton.setDisabled( true );
			}
			if( mergeButton    != null )
			{
				mergeButton.setVisibility( false );
				mergeButton.setDisabled( true );
			}
			if( updateButton   != null )
			{
				updateButton.setVisibility( false );
				updateButton.setDisabled( true );
			}
			if( validateButton != null )
			{
				validateButton.setVisibility( false );
				validateButton.setDisabled( true );
			}
			if( importButton   != null )
			{
				importButton.setVisibility( false );
				importButton.setDisabled( true );
			}
			if( exportButton   != null )
			{
				exportButton.setVisibility( false );
				exportButton.setDisabled( true );
			}
			if( cancelButton   != null )
			{
				cancelButton.setVisibility( false );
				cancelButton.setDisabled( true );
			}
			if( closeButton    != null )
			{
				closeButton.setVisibility( true );
			}
			if( newrowButton   != null )
			{
				newrowButton.setVisibility( false );
				newrowButton.setDisabled( true );
			}
			
			
			ControlInstance ctrl = wcs.findControl( CTRL_COBIEUP_TRASH );
			if( ctrl != null )
			{
				ctrl.setVisibility( false );
				ctrl.setDisabled( true );
			}
			
			for( int i = 0; i < readonlyWhenNotNew.length; i++ )
			{
				setControlReadonly( readonlyWhenNotNew[i] );
			}
		}
	}
	
	private void setupTabs() 
		throws RemoteException, 
		       MXException
	{
		WebClientSession wcs = this.app.getWebClientSession();
		ControlInstance tabUpload   = wcs.findControl( CTRL_COBIE_UPLOAD_TAB );
		ControlInstance tabUpdate   = wcs.findControl( CTRL_COBIE_UPDATE_TAB );
		ControlInstance tabFacility = wcs.findControl( CTRL_COBIE_FACILITIES_TAB );
		ControlInstance tabOp       = wcs.findControl( CTRL_COBIE_OP_TAB );
		ControlInstance tabFilter   = wcs.findControl( CTRL_COBIE_FILTER_TAB );
		ControlInstance tabAdvanced = wcs.findControl( CTRL_COBIE_ADVANCED_TAB );
		ControlInstance tabAttrib   = wcs.findControl( CTRL_COBIE_ATTRIB_TAB );
		ControlInstance tabDefault  = wcs.findControl( CTRL_COBIE_DEFAULT_TAB );
		ControlInstance tabExport   = wcs.findControl( CTRL_COBIE_EXPORT_TAB );
		ControlInstance secDefaults = wcs.findControl( CTRL_COBIE_DEFAULTS );

		switch( _sessionType )
		{
		case BIMSession.SESSION_TYPE_VALIDATE:
			if( tabUpdate != null )  tabUpdate.setVisibility( false );
			if( tabExport != null )  tabExport.setVisibility( false );
			if( tabDefault != null ) tabDefault.setVisibility( false );
			if( tabFacility != null ) tabFacility.setVisibility( false );
			if( secDefaults != null ) secDefaults.setVisibility( false );
			break;
		case BIMSession.SESSION_TYPE_IMPORT:
			if( tabUpdate != null ) tabUpdate.setVisibility( false );
			if( tabExport != null ) tabExport.setVisibility( false );
			if( tabFacility != null ) tabFacility.setVisibility( false );
			break;
		case BIMSession.SESSION_TYPE_MERGE:
		case BIMSession.SESSION_TYPE_UPDATE:
			if( tabExport != null ) tabExport.setVisibility( false );
			if( tabFacility != null ) tabFacility.setVisibility( false );
			break;
		case BIMSession.SESSION_TYPE_EXPORT:
			if( tabUpload != null )   tabUpload.setVisibility( false );
			if( tabUpdate != null )   tabUpdate.setVisibility( false );
			if( tabOp != null )       tabOp.setVisibility( false );
			if( tabFilter != null )   tabFilter.setVisibility( false );
			if( tabAdvanced != null ) tabAdvanced.setVisibility( false );
			if( tabAttrib != null )   tabAttrib.setVisibility( false );
			if( tabDefault != null )  tabDefault.setVisibility( false );
			break;
		}
	}
	
	private void setTitle() throws RemoteException, MXException
	{
		String titleKey = "COBie Data";
		
		switch( _sessionType )
		{
		case BIMSession.SESSION_TYPE_VALIDATE:
			titleKey = Constants.MSG_TITLE_VALIDATE;
			break;
		case BIMSession.SESSION_TYPE_IMPORT:
			titleKey = Constants.MSG_TITLE_IMPORT;
			break;
		case BIMSession.SESSION_TYPE_UPDATE:
			titleKey = Constants.MSG_TITLE_UPDATE;
			break;
		case BIMSession.SESSION_TYPE_MERGE:
			titleKey = Constants.MSG_TITLE_MERGE;
			break;
		case BIMSession.SESSION_TYPE_EXPORT:
			titleKey = Constants.MSG_TITLE_EXPORT;
			break;
		}
		MboRemote mbo = app.getDataBean().getMbo();
		String title = mbo.getMessage( Constants.BUNDLE_IMPORT_MSG, titleKey );

		WebClientSession wcs = this.app.getWebClientSession();
		ControlInstance dlgControl = wcs.findControl( "cobieup" );
		dlgControl.setProperty( "label", title );
	}
	
	private void setControlReadonly(
		String ctrlId
	) {
		WebClientSession wcs = this.app.getWebClientSession();
		ControlInstance ctrl;
		ctrl = wcs.findControl( ctrlId );
		if( ctrl != null )
		{
			ctrl.setDisabled( true );
			ctrl.setProperty( "inputmode", "readonly" );
			ctrl.setProperty( "lookup", "" );
		}
	}
	
	
	private void selectTab(
		String tab
	) {
		WebClientSession wcs = this.app.getWebClientSession();
		ControlInstance ctrl;
		ctrl = wcs.findControl( CTRL_COBIE_TAB_GROUP );
		if( ctrl == null || !(ctrl instanceof TabGroup )) return;
		TabGroup tabCtrl = (TabGroup)ctrl;
		tabCtrl.setCurrentTab( tab );
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
		MboRemote mbo = getMbo();
		long uid = mbo.getUniqueIDValue();
		this.reset();
		getMboForUniqueId( uid );
		
		return EVENT_HANDLED;
	}
	
	public void refreshSessionTable() 
		throws MXException, RemoteException
	{
		MboRemote mbo = app.getDataBean().getMbo();
		if( mbo == null ) return;
		long uid = mbo.getUniqueIDValue();
		app.getDataBean().reset();
		app.getDataBean().getMboForUniqueId( uid );
	}
	
	private int processAction(
		int updateMode
	) 
		throws MXException, 
		       RemoteException 
   {
		save();
		DataBean sessionBean = app.getDataBean( CTRL_COBIE_UPLOAD_TABLE );
		BIMUploadSetRemote bimer = (BIMUploadSetRemote) sessionBean.getMboSet();

		if( bimer.readyForImport() )
		{
			WebClientSession wcs = this.app.getWebClientSession();
			ControlInstance tabFacility = wcs.findControl( CTRL_COBIE_FACILITIES_TAB );
			if( tabFacility != null ) tabFacility.setVisibility( true );

			TabGroup tg = (TabGroup) this.app.getWebClientSession().findControl( CTRL_COBIE_TAB_GROUP );
			Tab tab = (Tab) wcs.findControl( CTRL_COBIE_LOG_TAB );

			bimer.parseCollection( updateMode );

			if( tg != null && tab != null )
			{
				tg.setCurrentTab(tab, true);
			}

			setupButtons( false );
			selectTab( CTRL_COBIE_LOG_TAB );

			// Import executed. Make site, parentloc and prefix read-only
			this.app.getDataBean().fireDataChangedEvent();
			this.app.getDataBean().fireStructureChangedEvent();

		}
		else
		{
			throw new MXApplicationException(Constants.BUNDLE_MSG_IMPORT, Constants.MSG_FLOOR_FACILITY_MISSING);
		}
		save();

		bimer.deleteFiles( BIMUploadSet.DETETE_FILES_MARKED_FOR_DETLETE );
		
		refreshSessionTable();
		refreshLogs();
		return EVENT_HANDLED;
	}
	
}
