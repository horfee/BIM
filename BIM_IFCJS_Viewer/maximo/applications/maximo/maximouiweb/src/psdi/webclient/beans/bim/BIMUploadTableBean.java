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

import psdi.app.bim.BIMServiceRemote;
import psdi.app.bim.project.BIMSession;
import psdi.app.bim.project.BIMSessionRemote;
import psdi.app.bim.project.BIMUpload;
import psdi.app.bim.project.BIMUploadRemote;
import psdi.mbo.MboRemote;
import psdi.server.MXServer;
import psdi.util.MXException;
import psdi.util.MXSystemException;
import psdi.webclient.controls.Table;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.controller.WebClientEvent;
import psdi.webclient.system.session.WebClientSession;

public class   BIMUploadTableBean
    extends    DataBean
    implements FileUpload
{

	private ControlInstance  _ctrlTable = null;
	private ControlInstance  _ctrlToggle = null;

	@Override
	protected void initialize() throws MXException, RemoteException
	{
		super.initialize();
		WebClientSession wcs = this.app.getWebClientSession();
		_ctrlTable = wcs.findControl( "cobieup_upload_tbl_details" );
		_ctrlToggle = wcs.findControl( "cobieup_upload_tbl_5" );
	}
	
	synchronized public int execute() throws MXException, RemoteException
	{
		return super.execute();
	}

	synchronized public int cancelDialog() 
		throws MXException, RemoteException
	{
		return super.cancelDialog();
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
			setDetailVisibility();
			return true;
		}
		return false;
	}
	
	@Override
	synchronized public void dataChangedEvent(
	    DataBean speaker 
    ) {
		super.dataChangedEvent( speaker );
		setDetailVisibility();
	}
	
	/**
	 * New rows start with the table details collapsed because they are
	 * seldom used
	 * @return
	 */
	public int setupnewrow()
	{
		WebClientSession wcs = this.app.getWebClientSession();
		ControlInstance ctrl = wcs.findControl( "cobieup_upload_tbl" );
		if( ctrl == null  || !(ctrl instanceof Table) ) 
		{
			return EVENT_HANDLED;
		}
		Table table = (Table)ctrl;
		table.toggledetails( false );
		return EVENT_HANDLED;
	}
	
	/**
	 * The table details only apply to excel files.  Hide the whole details
	 * section if the sheet type is not excel
	 */
	protected void setDetailVisibility()
	{
		String sheet;
        try
        {
    		MboRemote mbo = getMbo();
    		if( !(mbo instanceof BIMUploadRemote ))
    		{
    			return;
    		}
			BIMUploadRemote uploadMbo = (BIMUploadRemote)mbo; 
			sheet = uploadMbo.getSheetName();
        }
        catch( Exception e )
        {
        	return;
        }

        if( _ctrlTable != null )
		{
			if( sheet.equals( "EXCEL" ))
			{
				_ctrlTable.setVisibility( true );
			}
			else
			{
				_ctrlTable.setVisibility( false );
			}
		}

		if( _ctrlToggle != null ) 
		{
			if( sheet.equals( "EXCEL" ))
			{
				_ctrlToggle.setVisibility( true );
			}
			else
			{
				_ctrlToggle.setVisibility( false );
			}
		}
	}
	
	public void setServerFileName(
		MboRemote targetMbo,
		String enentSource,
        String fileName 
    ) 
		throws MXException, 
	           RemoteException
	{
		targetMbo.setValue( BIMUpload.FIELD_FILENAME, fileName );
    }

	public void setClientFileName(
		MboRemote targetMbo,
		String enentSource,
        String fileName 
    ) 
		throws MXException, 
		      RemoteException
    {
		targetMbo.setValue( BIMUpload.FIELD_SOURCEFILENAME, fileName );
    }

	public String getDirectoryPath(
        String enentSource 
    ) 
		throws MXException, 
		       IOException
    {
    	MboRemote mbo = getMbo();
    	MboRemote mboSession = mbo.getOwner();
    	if( mboSession == null || !(mboSession instanceof BIMSessionRemote ))	// Should never happen
    	{
        	throw new MXSystemException("system", "major" );
    	}
    	String rootDir = ((BIMSession)mboSession).getUploadDirectory();
    	
    	File file = new File(rootDir);
    	BIMServiceRemote bsr = (BIMServiceRemote) MXServer.getMXServer().lookup( "BIM" );
    	bsr.makeDir(file);
//    	FileUtils.forceMkdir(file);
    	
    	return rootDir;
    }
}
