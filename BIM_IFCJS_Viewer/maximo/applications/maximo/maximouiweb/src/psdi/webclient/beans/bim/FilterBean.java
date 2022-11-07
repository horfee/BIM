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
import java.util.Hashtable;

import psdi.util.MXException;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.session.WebClientSession;

public class   FilterBean
       extends DataBean
{
	public static final String CTRL_FILTER_TABLE = "filter_tbl";
	
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
				ctrl = wcs.findControl( CTRL_FILTER_TABLE );
				if( ctrl != null )
				{
					Hashtable<String, String> qbe = new Hashtable<String, String>();
					qbe.put( Constants.FIELD_SITEID, siteId  );
					String orgid = appBean.getString( Constants.FIELD_ORGID );
					if( orgid != null )
					{
						qbe.put(  Constants.FIELD_ORGID, orgid );
					}
					DataBean ctrlBean = ctrl.getDataBean();
		    		ctrlBean.resetQbe();
		    		ctrlBean.setQbe( qbe );
				}
			}
		}
	}
}
