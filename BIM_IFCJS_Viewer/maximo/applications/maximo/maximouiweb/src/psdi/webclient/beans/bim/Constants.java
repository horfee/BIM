/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * 5724-U18
 *
 * Copyright IBM Corp. 2018
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 */

package psdi.webclient.beans.bim;


public class Constants
{
	public final static String DLG_ADD_MODEL             = "bim_addmod";
	public final static String DLG_MANAGE_MODELS         = "bim_mm";
	public final static String DLG_IMPORT_MODEL          = "import_building_model";
	public final static String DLG_COBIEUP_CVS           = "cobieup";
	public final static String DLG_SAVE_CANCEL           = "savecancel";
	
	public final static String CTRL_ASSET_DETAIL_TBL     = "assetdetails_table";
	public final static String CTRL_WO_ASSET_LOC_TABLE   = "work_multiassetlocci_table";
	public final static String CTRL_SR_ASSET_LOC_TABLE   = "main_multiassetlocci_table";
	
	public final static String CTRL_DISP_SYSTEMS_TREE    = "displaysystems_tree";
	public final static String CTRL_DISP_SYSTEMS_TBL     = "displaysystems_table";
	
	public final static String CTRL_UPDATE_SYSTEMS_TBL   = "systemupdate_table_1";
	public final static String CTRL_UPDATE_SYSTEMS_TREE  = "systemupdate_tree";
	
	public final static String CTRL_PRODUCT_TABLE        = "bim_mpt_table";
	
	// BIM Project applications controls
	public final static String CTRL_PROJECT_NAME         = "project_name";
	public final static String CTRL_PROJECT_PARENT_LOC   = "project_parent_loc";
	public final static String CTRL_PROJECT__SITE_ID     = "project_siteid";
	public final static String CTRL_PROJECT_LOC_PREFIX   = "project_loc_prefix";
	
	public final static String CTRL_BIM_SESSIONS_TABLE   = "sessions_table";
	
    public final static String TABLE_ASSET        = "ASSET";
    public final static String TABLE_LOCANCESTOR  = "LOCANCESTOR";
    public final static String TABLE_LOCHIERARCHY = "LOCHIERARCHY";
    public final static String TABLE_LOCATIONS    = "LOCATIONS";
    public final static String TABLE_LOCSYSTEM    = "LOCSYSTEM";
    public final static String TABLE_WORKORDER    = "WORKORDER";
    
    public final static String FIELD_ASSETID       = "ASSETID";
    public final static String FIELD_ASSETNUM      = "ASSETNUM";
	public final static String FIELD_CHILDREN      = "CHILDREN";
	public final static String FIELD_CLASS         = "CLASS";
	public final static String FIELD_DESCRIPTION   = "DESCRIPTION";
	public final static String FIELD_ITEMSETID     = "ITEMSETID";
	public final static String FIELD_LOCATION	   = "LOCATION";
	public final static String FIELD_LOCATIONSID   = "LOCATIONSID";
	public final static String FIELD_MODELID       = "MODELID";
	public final static String FIELD_ORGID         = "ORGID";
	public final static String FIELD_NETWORK       = "NETWORK";
	public final static String FIELD_PARENT        = "PARENT";
	public final static String FIELD_PRIMARYSYSTEM = "PRIMARYSYSTEM";
	public final static String FIELD_PROJECTNAME   = "PROJECTNAME";
	public final static String FIELD_RECORDCLASS   = "RECORDCLASS";
	public final static String FIELD_RECORD_KEY    = "RECORDKEY";
	public final static String FIELD_SITEID        = "SITEID";
	public final static String FIELD_SYSTEMID      = "SYSTEMID";
	public final static String FIELD_BIMIMPORTSRC  = "BIMIMPORTSRC";
//	public final static String FIELD_STATUS        = "STATUS";
	
	public final static String LABEL_CLOSE         = "Close";
	
	/**
	 * Message constants for DBCHange generated message file
	 */
	public final static String BUNDLE_MSG                 = "bimviewer";
	public final static String BUNDLE_IMPORT_MSG          = "bimimport";	

	// The primary system cannot be edited
	public final static String MSG_EDIT_PROMARY_SYSTEM    = "edit-primary-system";
	// Internal Error
	public final static String MSG_INTERNAL_ERR           = "Internal-error";
	// The working directory does not exist on the server.  Contact a system administrator.
	public final static String ERR_BIM_DIR_MISSING        = "bimdir-missing";          
	// There is not a valid place to add the selection to the system
	public final static String MSG_NO_ROOT                = "no-root";
	// The selected item in the model has not been imported to Maximo
	public final static String MSG_NOT_IMPORTED           = "not-imported";
	// Internal Error:  It is like that the presentation XML for the dialog has been changed
	public final static String MSG_PRESENTATION_CHANGED   = "presentation-xml-changed";

	public final static String BUNDLE_MSG_IMPORT          = "bimimport";
	// File not found.  Ensure that the file exists and has the proper permissions.
	public final static String MSG_BIM_FILE_MISSING       = "bim-file-missing";        
	// To run and import or updated, exactly one facility and at least one floor must be included in the import files and selected for import
	public final static String MSG_FLOOR_FACILITY_MISSING = "floor-facility-missing";    
	// You can not delete a session once an import is complete.
	public final static String MSG_SESSION_DELETE         = "session_delete";

	// Validate COBie Data
	public final static String MSG_TITLE_VALIDATE =  "title-validata";
	// Import COBie Data
	public final static String MSG_TITLE_IMPORT   =  "title-import";
	// Update COBie Data
	public final static String MSG_TITLE_UPDATE   =  "title-update";
	// Merge COBie Data
	public final static String MSG_TITLE_MERGE    =  "title-merge";
	// Export COBie Data
	public final static String MSG_TITLE_EXPORT   =  "title0export";

}
