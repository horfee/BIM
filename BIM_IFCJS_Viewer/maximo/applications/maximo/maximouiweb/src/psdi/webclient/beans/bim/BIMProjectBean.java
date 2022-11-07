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

import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.security.UserInfo;
import psdi.util.MXException;
import psdi.webclient.system.beans.AppBean;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.session.WebClientSession;

public class   BIMProjectBean
       extends AppBean
{
	@Override
	public synchronized void save() throws MXException
	{
		super.save();
		MboRemote bim;
		try
		{
			bim = app.getAppBean().getMbo();
			if( bim != null )
			{
				MboSetRemote childBIMs = bim.getMboSet("BIMSESSION");
				if( childBIMs != null )
				{
					childBIMs.save();
				}
			}
		}
		catch( RemoteException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
    public synchronized void setValue(
	    int nRow,
	    String attribute,
	    String value,
	    long accessModifier
    ) 
    	throws MXException
	{
		try
		{
			MboRemote mbo = mboSetRemote.getMbo(nRow);
			if( mbo.getMboValueData(attribute).getData().compareTo(value) == 0 )
			{
				return;
			}
			// Call parent to continue with the setValue
			super.setValue(nRow, attribute, value, accessModifier);
		}
		catch( RemoteException e )
		{
			handleRemoteException(e);
		}
	}
    
	@Override
	public synchronized void insert() 
		throws MXException, 
		       RemoteException
	{
		super.insert();
	    try
	    {
			MboRemote mbo = getMbo( getEndRow() - 1 );
			mbo = getMbo();
			if( mbo == null ) return;  

			String siteId = getString( Constants.FIELD_SITEID );
			if( siteId != null && siteId.length() > 0 ) return;
			
			UserInfo ui = clientSession.getUserInfo();
			siteId = ui.getInsertSite();

			if( siteId != null )
			{
//				setValue( Constants.FIELD_SITEID, siteId );
			}
	    }
	    catch( RemoteException e )
	    {
	    	// Ignore
	    }
	}
	
	public void setControlsReadonly()
	{
		setControlReadonly(Constants.CTRL_PROJECT_NAME );
		setControlReadonly(Constants.CTRL_PROJECT_PARENT_LOC );
		setControlReadonly(Constants.CTRL_PROJECT__SITE_ID );
		setControlReadonly(Constants.CTRL_PROJECT_LOC_PREFIX );
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
}
