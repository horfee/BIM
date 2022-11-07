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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;

import psdi.app.bim.BIMService;
import psdi.mbo.MboConstants;
import psdi.mbo.MboRemote;
import psdi.server.MXServer;
import psdi.util.MXException;
import psdi.util.MXObjectNotFoundException;
import psdi.webclient.beans.bim.Constants;
import psdi.webclient.components.BIMViewer;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.controller.MPFormData;
import psdi.webclient.system.controller.WebClientEvent;

/**
 * @author Esther Burwell	
 * This bean is used to select and upload model files from the browser to the Maximo server
 * 
 */
public class ModelImportBean extends DataBean
{
	
	/**
	 * The model URL support paramter subsitution of the hostname.  The subsistution
	 * value is specified as the Maximo property bim.model.hostname.  This property
	 * holds the value of that property and is set the first time it is needed
	 */
	private static String _modelHostName = null;

   /**
     *  Method to import xml/Flat file. Both preview mode and writing into Queue are supported.
	 * @return
	 * @throws MXException
	 * @throws RemoteException 
	 */
	public int loadModelFile() 
		throws MXException, 
		       RemoteException
	{
		ByteArrayOutputStream baOs = null;
		HttpServletRequest request = clientSession.getRequest();
		
		String rootDir = "";
		try
        {
	    	rootDir = MXServer.getMXServer().getProperty( BIMService.PROP_NAME_BIM_MODEL_DIR );
			// Check root dir to be sure it exists.
			File file=new File(rootDir);
			 boolean exists = file.exists();
			 if (!exists) {
				 throw new MXObjectNotFoundException(Constants.BUNDLE_MSG, Messages.ERR_MODEL_DIR_MISSING );
			 }
        }
        catch( Exception e )
        {
            throw new MXObjectNotFoundException("system", "objectnotfound",e);
        }
        
		WebClientEvent wce = clientSession.getCurrentEvent();
		ControlInstance uploadfileControl = wce.getSourceControlInstance();
		String maxfilesize = uploadfileControl.getProperty("maxfilesize");
		MPFormData mpData = new MPFormData(request, Integer.parseInt(maxfilesize));
		String fName = mpData.getFileName();

		if (MPFormData.isRequestMultipart(request))
		{
			try
			{
			    baOs =  mpData.getFileOutputStream();
			    String name = rootDir + File.separatorChar + fName;
			    OutputStream outputStream = new FileOutputStream( name );
				baOs.writeTo(outputStream);
			}
			catch(Exception e)
			{
				e.printStackTrace();
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

			String servletBase = clientSession.getMaximoRequestContextURL();
			String modelURL;
			String modelHost = getModelHostname();
			
			// Check to see if a value is set for the system property bim.model.hostname
			// If so, the n add the substitution parameter marker into the URL
			if( modelHost.length() == 0 )
			{
				modelURL = servletBase + "/models/" + fName;
			}
			else
			{
				try 
				{
					URL url = new URL( servletBase );
					String protocol = url.getProtocol();
					String context  = url.getPath();
					modelURL = protocol + "://" + BIMViewer.HOST_PARAM_MARKER + context + "/models/" + fName;
				} 
				catch (MalformedURLException e) 
				{
					// Don't do any substitution
					modelURL = servletBase + "/models/" + fName;
				}
			}
			
			// Could be called from either the add or manage models dialogs
	        DataBean bean = app.getDataBean( Constants.DLG_ADD_MODEL );
	        if( bean == null )
	        {
		        bean = app.getDataBean( Constants.DLG_MANAGE_MODELS );
	        }
	        if( bean == null )
	        {
	        	if( app.getDataBean() instanceof ModelAppBean )
	        	{
	        		bean = app.getDataBean();
	        	}
	        }
	        if( bean == null )
	        {
	        	bean = getParent();
	        }
	        if( bean != null )
	        {
				MboRemote mbo = bean.getMbo();
				mbo.setValue("URL", modelURL, MboConstants.NOACCESSCHECK);
				app.getAppBean().fireDataChangedEvent();
	        }
		}
		return EVENT_HANDLED;
	}
	
	public String getModelHostname()
	{
		if( ModelImportBean._modelHostName != null )
		{
			return ModelImportBean._modelHostName;
		}
		try 
		{
//			ModelImportBean._modelHostName = BIMService.getMaximoPropertyValue( BIMService.PROP_NAME_BIM_MODEL_HOST );
			ModelImportBean._modelHostName = MXServer.getMXServer().getProperty( BIMService.PROP_NAME_BIM_MODEL_HOST );
			if( ModelImportBean._modelHostName == null ) ModelImportBean._modelHostName = "";
		} 
		catch (Exception e)
		{
			ModelImportBean._modelHostName = "";
		}
		return ModelImportBean._modelHostName;
	}
}
