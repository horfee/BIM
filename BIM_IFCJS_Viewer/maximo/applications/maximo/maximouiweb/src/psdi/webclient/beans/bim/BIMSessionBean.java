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

import psdi.app.bim.project.BIMSession;
import psdi.app.bim.project.BIMSessionRemote;
import psdi.app.bim.project.BIMUpload;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.util.MXObjectNotFoundException;
import psdi.webclient.system.beans.AppBean;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.beans.WebClientBean;
import psdi.webclient.system.controller.WebClientEvent;

public class BIMSessionBean
    extends
        DataBean
{

	private long	sessionId	= -1;

	@Override
	protected void initialize() 
		throws MXException, RemoteException
	{
		super.initialize();
	}

	@Override
	public synchronized void delete(
	    int nRow
    ) 
		throws MXException, RemoteException
	{
		BIMSessionRemote mbo = (BIMSessionRemote)getMbo(nRow);

		if( mbo.isStatusNew() )
		{
			super.delete(nRow);
		}
		else
		{
			throw new MXObjectNotFoundException(Constants.BUNDLE_MSG_IMPORT, Constants.MSG_SESSION_DELETE);
		}
	}
	
	public int addNewRow() 
		throws MXException
	{
		WebClientEvent event = clientSession.getCurrentEvent();
		Object o = event.getValue();
		if( o == null || !(o instanceof String ))  
		{
			// Should never happen unless the .jsp is altered
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		String sessionType = (String)o;

		int result = super.addrow();
		if( result != EVENT_HANDLED ) return result;
		
	    try
	    {
			MboRemote mbo = getMbo();
			if( mbo == null || !( mbo instanceof BIMSessionRemote ))
			{
				return result;
			}
			BIMSessionRemote mboSession = (BIMSessionRemote)mbo;
			mboSession.setValue( BIMSession.FIELD_SESSIONTYPE, sessionType );

			if( mboSession.getSessionType() != BIMSession.SESSION_TYPE_EXPORT )  
			{
				MboSetRemote fileSet = mbo.getMboSet( "BIMFILE" );
				mbo = fileSet.addAtEnd();
				mbo.setValue( BIMUpload.FIELD_COBIESHEET, "!EXCEL!" );
			}
	    }
	    catch( RemoteException e )
	    {
	    	// Ignore
	    }
		return result;
	}

	/**
	 * Handle the upload button on the dialog
	 * 
	 * @return
	 * @throws MXException
	 * @throws RemoteException
	 */
	public int uploadCOBieCVSFiles() throws MXException, RemoteException
	{
		MboRemote mbo = getMbo();
		long id = mbo.getLong(BIMSession.FIELD_BIMSESSIONID);
		
		AppBean ab = (AppBean) this.app.getAppBean();
		if( ab.toBeSaved() )
		{
			ab.save();
		}
		
		clientSession.queueEvent( new WebClientEvent( Constants.DLG_COBIEUP_CVS, Constants.DLG_COBIEUP_CVS,
			                                          "" + id, clientSession));
		return WebClientBean.EVENT_HANDLED;
	}

	public long getSessionId()
    {
    	return sessionId;
    }

	public void setSessionId(
        long sessionId)
    {
    	this.sessionId = sessionId;
    }
}
