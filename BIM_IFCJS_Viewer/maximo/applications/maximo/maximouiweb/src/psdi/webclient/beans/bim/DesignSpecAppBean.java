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

import psdi.app.bim.product.BIMProduct;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.util.MXException;
import psdi.webclient.system.beans.AppBean;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ControlInstance;

public class DesignSpecAppBean
    extends AppBean
{
	CommentMgrBean _commentMgrBean = null;

	public DesignSpecAppBean()
	{
		super();
	}
	
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
 
    public void clearClassification() 
		throws MXException
	{
		setValue( BIMProduct.FIELD_CLASSSTRUCTUREID, "" );
	}
	
	/**
	 * set the MboSet to include Predoucts that are design specification"
	 */
	@Override
	protected MboSetRemote getMboSetRemote() throws MXException, RemoteException
	{
		MboSetRemote mboSetRemote = super.getMboSetRemote();
		mboSetRemote.setRelationship( BIMProduct.FIELD_DESIGNSPEC + " = 1" );
		mboSetRemote.reset();
		return mboSetRemote;
	}
	
	public int newComment() 
		throws MXException, 
		       RemoteException
	{
		CommentMgrBean commentMgrBean = getCommentBean();
		if( commentMgrBean != null )
		{
			return commentMgrBean.newComment();
		}
		return EVENT_STOP_ALL;
	}
	
	public int reply() 
		throws RemoteException, 
		       MXException
	{
		CommentMgrBean commentMgrBean = getCommentBean();
		if( commentMgrBean != null )
		{
			return commentMgrBean.reply();
		}
		return EVENT_STOP_ALL;
	}

	CommentMgrBean getCommentBean()
	{
		if( _commentMgrBean != null )
		{
			return _commentMgrBean;
		}
		ControlInstance ctrl = app.getWebClientSession().findControl( "designspec_tabs_review_commentmgr" );
		DataBean db = ctrl.getDataBean();
		if( db instanceof CommentMgrBean )
		{
			_commentMgrBean = (CommentMgrBean)db;
		}
		return _commentMgrBean;
	}
}
