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
package psdi.webclient.beans.bim;

import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.text.ParseException;

import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.util.MXException;
import psdi.webclient.beans.common.TreeControlBean;
import psdi.webclient.controls.Tree;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.runtime.WebClientRuntime;
import psdi.webclient.system.session.WebClientSession;


/**
 * @author Doug Wood
  * Custom bean to handle tree control for building model control
 */
public class CommentTreeBean extends TreeControlBean
{
	public final String QUERY_TOP     = "parentid is null and OWNERTABLE = :1 and OWNERID = :2";
	public final String QUERY_COMMENT = "ownertable = :1 and ownerid = :2";
	
	private CommentMgrBean _mgrBean    = null;
	private MboRemote      _currentMbo = null;
	
	@Override
	public void setupBean(
	    WebClientSession wcs 
    ) {
    	super.setupBean( wcs );
		DataBean db = getParent();
		while( db != null )
		{
			if( db instanceof CommentMgrBean )
			{
				_mgrBean = (CommentMgrBean)db;
				_mgrBean.setTreeBean( this );
				break;
			}
			db = db.getParent();
		}
    }
	
	@Override
	protected void initialize() 
		throws MXException, RemoteException
	{
		super.initialize();
	}

	public void setuniqueidvalue( 
		String newuniqueidvalue 
	) {
		super.setuniqueidvalue( newuniqueidvalue );
		getBoundTree().setUniqueIdValue( newuniqueidvalue );
	}

	/** 
	 *	Handles the selectnode event fired from the frontend or through TreeNode
	 *  It gets MBo for the uniqueid passed whena a node is selected
	 *  
	 *   @return EVENT_HANDLED;
	 */
	public int selectnode() 
		throws MXException, 
		       RemoteException
	{	
		if(!WebClientRuntime.isNull(uniqueidvalue))
		{
			Tree tree = getBoundTree();
			String id = null;
			if( tree != null )
			{
				id = tree.getUniqueIdValue();
			}
			id = uniqueidvalue;
			if( id != null && id.length() > 0 )
			{
				try
				{				
					long num = NumberFormat.getIntegerInstance(clientSession.getUserInfo().getLocale()).parse( id ).longValue(); 
					this.uniqueidvalue = id;
					this.getMboForUniqueId(num);
					if( _mgrBean != null )
					{
						_mgrBean.selectNode( num );
					}
				}
				catch(ParseException e)
				{ /* Ignore */	}
			}
		}
		
		return EVENT_HANDLED;
	}
	
	MboRemote getCurrentMbo()
	{
		return _currentMbo;
	}
	
	protected MboSetRemote getMboSetRemote() 
		throws MXException, 
		       RemoteException
	{
		if( getobjectname() == null || getobjectname().equals("") )
		{
			storeProperties();
		}
		if( _mgrBean != null )
		{
			return _mgrBean.getMboSet();
		}
		return null;
	}
}