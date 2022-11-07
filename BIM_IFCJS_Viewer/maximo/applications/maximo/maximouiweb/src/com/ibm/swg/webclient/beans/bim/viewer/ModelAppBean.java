package com.ibm.swg.webclient.beans.bim.viewer;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import psdi.app.bim.BIMService;
import psdi.app.bim.viewer.BuildingModel;
import psdi.app.location.LocationRemote;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.server.MXServer;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.util.MXObjectNotFoundException;
import psdi.webclient.beans.bim.Constants;
import psdi.webclient.beans.bim.viewer.Messages;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.beans.WebClientBean;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.controller.EventQueue;
import psdi.webclient.system.controller.MPFormData;
import psdi.webclient.system.controller.PageInstance;
import psdi.webclient.system.controller.UploadFile;
import psdi.webclient.system.controller.WebClientEvent;
import psdi.webclient.system.session.WebClientSession;

public class ModelAppBean extends psdi.webclient.beans.bim.viewer.ModelAppBean {

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
	
	
	public int uploadifc() throws RemoteException, MXException {
//		HttpServletRequest request = clientSession.getRequest();
//
//		WebClientEvent wce = clientSession.getCurrentEvent();
//		ControlInstance uploadfileControl = wce.getSourceControlInstance();
//		String maxfilesize = uploadfileControl.getProperty("maxfilesize");
//		try {
//			MPFormData mpData = new MPFormData(request, Integer.parseInt(maxfilesize));
//			String fName = mpData.getFileName();
//		} catch (NumberFormatException | MXException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		WebClientEvent wce = clientSession.getCurrentEvent();
		HttpServletRequest request = clientSession.getRequest();
		MPFormData mpData = null;
		if (MPFormData.isRequestMultipart(request))
		{
			try
			{
				ControlInstance uploadfileControl = wce.getSourceControlInstance();
				String maxfilesize = uploadfileControl.getProperty("maxfilesize");
				mpData = new MPFormData(request, Integer.parseInt(maxfilesize));
			}
			catch(psdi.util.MXException mxe)
			{
				clientSession.addWarning(mxe);
			}
			if(mpData!=null)
			{
				String fName = "";
				fName = mpData.getFullFileName();
				if (fName.equals(""))
				{
					clientSession.showMessageBox(wce, "jspmessages", "nouploadfile", null);
					return EventQueue.CANCEL_ALL;
				}
				UploadFile uf = new UploadFile(mpData.getFileName(), mpData.getFullFileName(), mpData.getFileContentType(), mpData.getFileOutputStream());
				String fileName = uf.getFullFileName();
				String docType = "CAD";
				
				
//				MboSetRemote docTypes = MXServer.getMXServer().getMboSet("DOCTYPES", clientSession.getUserInfo());
//				docTypes.setQbe("DOCTYPE", docType);
//				
//				if ( docTypes.getMbo(0) == null ) {
//					throw new MXApplicationException("bim", "folderMissing");
//				}
				
				String rootDir = "";
				try {
					rootDir = MXServer.getMXServer().getProperty(BIMService.PROP_NAME_BIM_MODEL_DIR);
					// Check root dir to be sure it exists.
					File file = new File(rootDir);
					boolean exists = file.exists();
					if (!exists) {
						throw new MXObjectNotFoundException(Constants.BUNDLE_MSG, Messages.ERR_MODEL_DIR_MISSING);
					}
					
					uf.setDirectoryName(rootDir);
					uf.writeToDisk();
					
					getMbo(0).setValue("URL", uf.getAbsoluteFileName());
				} catch (Exception e) {
					throw new MXObjectNotFoundException("system", "objectnotfound", e);
				}
//				
//				try {
//					uf.setDirectoryName(docTypes.getMbo(0).getString("DEFAULTFILEPATH"));
//					uf.writeToDisk();
//				} catch (MXApplicationException | IOException e) {
//					e.printStackTrace();
//				}
//				
//				
//				MboSetRemote doclinksMboSet = getMbo(0).getMboSet("DOCLINKS");
//				MboRemote doclinksMbo = doclinksMboSet.add();
//
//				doclinksMbo.setValue("URLTYPE", "FILE");
//				doclinksMbo.setValue("URLNAME", uf.getFileName());
//				doclinksMbo.setValue("NEWURLNAME", docTypes.getMbo(0).getString("DEFAULTFILEPATH") + File.separator + uf.getFileName());
//				doclinksMbo.setValue("DOCTYPE", docType);
//				doclinksMbo.setValue("ADDINFO", true);
//				doclinksMbo.setValue("DESCRIPTION", "IFC Model");
//				
//				getMbo(0).setValue("URL", "DOCLINKS:" + doclinksMbo.getString("NEWURLNAME"));
			}

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
			if ( clientSession.getDesignmode() ) return;
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
			
			Vector<String[]> values =(Vector<String[]>) MXServer.getMXServer().getMaximoDD().getTranslator().getValuesVector("BIMVIEWERTYPE",null, null);
			for(String[] value : values ) {
				String viewerSectionID = value[0].toString() + SUFIX_MAIN;
				ControlInstance section = clientSession.getControlInstance( viewerSectionID );
				if ( section != null ) {
					section.setVisibility( viewerType.equalsIgnoreCase(value[0].toString()) );
				}
			}
			/*String sectionId = viewerType + SUFIX_MAIN;
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
			*/
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
