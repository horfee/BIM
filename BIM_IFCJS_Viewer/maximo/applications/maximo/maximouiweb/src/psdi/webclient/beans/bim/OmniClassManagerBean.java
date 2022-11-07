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

import psdi.util.MXException;
import psdi.webclient.system.beans.DataBean;

public class OmniClassManagerBean
    extends
        DataBean
{
	public void initialize() throws MXException, RemoteException
	{
		super.initialize();
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
		int row = this.getCurrentRow();
		this.reset();
		this.moveTo( row );
		
		return EVENT_HANDLED;
	}
}
