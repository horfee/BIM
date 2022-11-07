/*
 *
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * 5724-R46
 *
 * (C) COPYRIGHT IBM CORP. 2006,2007
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 */
/*
 * Created on Mar 25, 2004
 *
 * To change the template for this generated file go to
 * 
 */
package psdi.webclient.beans.bim;

import java.rmi.RemoteException;

import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.mbo.SqlFormat;
import psdi.security.UserInfo;
import psdi.server.MXServer;
import psdi.util.MXException;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ControlInstance;

/**
 * @author Esther Burwell	
 * Handles the configuration per site for BIM
 * 
 */
public class BIMConfigurationBean extends DataBean
{
	public void initialize() 
		throws MXException, RemoteException
	{
		super.initialize();
		
		MboRemote mbo = getMbo();
		if( mbo == null )
		{
			mbo = getMbo( 0 );
			if( mbo == null )
			{
				String siteId = getSiteid();
				insert();
				mbo = getMbo( 0 );
				if( mbo != null )
				{
					mbo.setValue( Constants.FIELD_SITEID, siteId );
				}
				select( 0 );
				moveTo( 0 );
			}
		}
	}
	
	public void clearClassification() 
		throws MXException
	{
		ControlInstance compInst = clientSession.getCurrentEvent().getSourceControlInstance();
		String originalAttr=compInst.getProperty("dataattribute");
		String classAttr = null;
	
		int index = originalAttr.indexOf(".");
		if( index > 0 )
		{
			classAttr = originalAttr.substring(0, index)  + "IFICATIONID";  
			setValue(classAttr, "" );
		}
	}
	
	@Override
	synchronized public int execute()
	    throws MXException,
	        RemoteException 
    {
		save();
		return EVENT_HANDLED;
	}
	
	@Override
	protected MboSetRemote getMboSetRemote()
	    throws MXException,
	        RemoteException
	{
		MboSetRemote setReturn = null;
		DataBean appBean = app.getDataBean();
		MboRemote appMbo = appBean.getMbo();
		String siteId = getSiteid();
		UserInfo userInfo = appMbo.getUserInfo();

		SqlFormat sqlf = new SqlFormat( appMbo, "siteid=:1" );
		sqlf.setObject( 1, "BIMCONFIGURATION", Constants.FIELD_SITEID, siteId );
		
        setReturn = MXServer.getMXServer().getMboSet( "BIMCONFIGURATION", userInfo );
        setReturn.setWhere( sqlf.format() );
        setReturn.reset();
		return setReturn;
	}
	
	private String getSiteid() 
		throws RemoteException, 
		       MXException
	{
		DataBean appBean = app.getDataBean();
		MboRemote appMbo = appBean.getMbo();
		UserInfo userInfo = appMbo.getUserInfo();
		if( appBean.isAttribute( Constants.FIELD_SITEID ) )
		{
			return appBean.getString( Constants.FIELD_SITEID );
		}
		else
		{
			return userInfo.getInsertSite();
		}
	}
}
