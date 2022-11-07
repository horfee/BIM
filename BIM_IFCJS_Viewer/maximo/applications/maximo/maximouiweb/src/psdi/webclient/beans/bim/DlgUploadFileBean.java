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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;

import psdi.app.bim.BIMService;
import psdi.mbo.MboRemote;
import psdi.server.MXServer;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.util.MXSystemException;
import psdi.webclient.system.beans.AppBean;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ComponentInstance;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.controller.MPFormData;
import psdi.webclient.system.controller.UploadFile;
import psdi.webclient.system.controller.WebClientEvent;

public class DlgUploadFileBean extends AppBean {

	private DataBean         _originalBean = null;
	private String           _originalAttr = null;
	private MboRemote        _originalMbo  = null;
	private ControlInstance  _originalControl = null;
	
	public void initialize() 
		throws MXException, RemoteException
	{
		super.initialize();
		_originalControl = creatingEvent.getSourceControlInstance();
		_originalBean    = clientSession.getDataBean( _originalControl.getProperty("datasrc"));
		_originalMbo     = _originalBean.getMbo();
		
		ComponentInstance compInst=creatingEvent.getSourceComponentInstance();
		_originalAttr = compInst.getProperty("dataattribute");
	}
	
	  /**
     *  Method to import xml/Flat file. Both preview mode and writing into Queue are supported.
	 * @return
	 * @throws MXException
	 * @throws RemoteException 
	 */
	public int loadFile() 
		throws MXException, 
		       RemoteException
	{
		
		WebClientEvent wce = clientSession.getCurrentEvent();
		ControlInstance uploadfileControl = wce.getSourceControlInstance();

		if( uploadfileControl != null )
		{
			uploadfileControl.setDisabled( true );
			uploadfileControl.setProperty( "inputmode", "readonly" );
			uploadfileControl.setProperty( "lookup", "csv,xls,xlsx" );
		}
		
		ByteArrayOutputStream baOs = null;
		HttpServletRequest request = clientSession.getRequest();
	        
		if (MPFormData.isRequestMultipart(request))
		{
			try
			{
				FileUpload parent = null;
				String rootDir;
				if( _originalBean instanceof FileUpload )
				{
					parent = (FileUpload)_originalBean;
					rootDir = parent.getDirectoryPath( _originalAttr );
				}
				else
				{
					rootDir = getDirectoryPath();
				}

				String maxfilesize = uploadfileControl.getProperty("maxfilesize");
				MPFormData mpData = new MPFormData(request, Integer.parseInt(maxfilesize));
				UploadFile uf = new UploadFile( mpData.getFileName(), mpData.getFullFileName(), 
						                        mpData.getFileContentType(), mpData.getFileOutputStream());
				uf.setDirectoryName( rootDir );
				uf.writeToDisk();
		    	String serverFileName = rootDir + File.separator + uf.getFileName();
				String sourceFileName = mpData.getFullFileName();
				
				if( parent != null )
				{
					parent.setClientFileName( _originalMbo, _originalAttr, sourceFileName  );
					parent.setServerFileName( _originalMbo, _originalAttr, serverFileName  );
				}
				else
				{
					_originalMbo.setValue( _originalAttr, serverFileName );
				}
				
				_originalBean.fireDataChangedEvent();
			}
            catch( FileNotFoundException e )
            {
            	throw new MXApplicationException(Constants.BUNDLE_MSG_IMPORT, Constants.MSG_BIM_FILE_MISSING );
            }
            catch( IOException e )
            {
	            
            	throw new MXSystemException("system", "major", e);
            }
			finally
			{
				try
				{
					if(baOs != null)
						baOs.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}			
		}
		return EVENT_HANDLED;
	}	
	
	private String getDirectoryPath() throws IOException, MXException
	{
		String rootDir = MXServer.getMXServer().getProperty( BIMService.PROP_NAME_BIM_WORKING_DIR );
		
		// Check to make sure directory exists
		File dir = new File(rootDir);
		if (!dir.exists())
		{
			throw new IOException(Constants.ERR_BIM_DIR_MISSING);
		}
		return rootDir;
	}
}
