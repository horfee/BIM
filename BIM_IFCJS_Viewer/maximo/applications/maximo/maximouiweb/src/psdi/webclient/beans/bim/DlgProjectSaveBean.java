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

import psdi.util.MXException;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.WebClientEvent;

/**
 * @author Esther Burwell	
 * This bean is used to display the save/cancel dialog for the project when the upload dialog is loaded.
 */
public class DlgProjectSaveBean extends DataBean
{

	public synchronized int execute() 
		throws MXException, RemoteException
	{	
		clientSession.queueEvent(new WebClientEvent( "loadCobieUpload", Constants.CTRL_BIM_SESSIONS_TABLE,
		                                             "", clientSession));

		return EVENT_HANDLED;
	}
}
