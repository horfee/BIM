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

import java.io.IOException;
import java.rmi.RemoteException;

import psdi.mbo.MboRemote;
import psdi.util.MXException;

/**
 * Used by DlgFileUploadBean to interact with launching bean for file uploads
 * @author Doug Wood
 */
public interface FileUpload
{
	void setServerFileName( MboRemote targetMbo, String enentSource, String fileName )  
		throws MXException, RemoteException;
	void setClientFileName( MboRemote targetMbo, String enentSource, String fileName  ) 
		throws MXException, RemoteException;
	String getDirectoryPath(  String enentSource ) throws IOException, MXException;
}
