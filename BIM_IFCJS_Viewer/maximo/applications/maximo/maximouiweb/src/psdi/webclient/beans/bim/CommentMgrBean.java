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

import psdi.app.bim.product.BIMComment;
import psdi.app.bim.product.BIMCommentSetRemote;
import psdi.mbo.MboRemote;
import psdi.util.MXException;
import psdi.webclient.controls.Pushbutton;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.session.WebClientSession;

/**
 * @author Doug Wood	
 * Handles the configuration per site for BIM
 * 
 */
public class CommentMgrBean extends DataBean
{
	private static final String MGR_SUFFIX        = "commentmgr";
	private static final String REPLY_SUFFIX      = "reply";
	
	private CommentTreeBean _treeBean    = null;
	private String          _baseId      = null;
	private Pushbutton      _replyBtn    = null;
	
	public CommentMgrBean()
	{
		super();
	}
	
	@Override
	public void setupBean(
	    WebClientSession wcs 
    ) {
    	super.setupBean( wcs );
    	_baseId = getId();
    	if( _baseId != null )
    	{
    		if( _baseId.endsWith( MGR_SUFFIX ) )
    		{
    			_baseId = _baseId.substring( 0, _baseId.length() - MGR_SUFFIX.length() );
    			ControlInstance ctrl = wcs.findControl( _baseId + REPLY_SUFFIX );
    			if( ctrl != null && ctrl instanceof Pushbutton )
    			{
    				_replyBtn = (Pushbutton)ctrl;
    			}
    		}
    	}
    	try
    	{
    		setOrderBy( BIMComment.FIELD_CHANGEDATE + " DESC" );
    	}
    	catch( Exception e )
    	{
    		e.printStackTrace();
    	}
	}
	
	@Override
	public void initialize() 
		throws RemoteException, 
		       MXException
	{
		super.initialize();
        MboRemote mbo = getMbo();
		if( mbo != null )
		{
			if( _treeBean != null )
			{
				long uid = mbo.getUniqueIDValue();
				_treeBean.setuniqueidvalue( "" + uid );
			}
			setupButtons( mbo );
		}
		setupButtons( mbo );
	}

	public void selectNode(
	    long uid 
    ) 
		throws RemoteException, 
		       MXException
	{
		MboRemote mbo = getMboForUniqueId( uid );
		setupButtons( mbo );
	}
	
	private void setupButtons(
		MboRemote currentMbo
	) 
		throws RemoteException 
	{
		if( currentMbo == null || _replyBtn == null )
		{
			_replyBtn.setDisabled( true );
			_replyBtn.setVisibility( false );
			return;
		}
		
		if( currentMbo.isNew() )
		{
			_replyBtn.setDisabled( true );
			_replyBtn.setVisibility( false );
		}
		else
		{
			_replyBtn.setDisabled( false );
			_replyBtn.setVisibility( true );
		}
	}
	
	public int newComment() 
		throws MXException, RemoteException
	{
		if( _treeBean == null ) return EVENT_STOP_ALL;
		insertAtEnd();
		long uid = getMbo().getUniqueIDValue();
		MboRemote mbo = getMboForUniqueId( uid );
		_treeBean.setuniqueidvalue( "" + uid );
		setupButtons( mbo );
		return EVENT_HANDLED;
	}
	
	public int reply() 
		throws RemoteException, 
		       MXException
	{
		if( _treeBean == null ) return EVENT_STOP_ALL;
		int currentRow = getCurrentRow();
		MboRemote mboParent = getMbo( currentRow );
		currentRow = _treeBean.getCurrentRow();
		if( mboParent == null ) return EVENT_STOP_ALL;
		
		BIMCommentSetRemote mboSet = (BIMCommentSetRemote)getMboSetRemote();
		mboSet.setParentForAdd( mboParent );
		insertAtEnd();
		MboRemote mbo = mboSet.getLastInsert();
		long uid = mbo.getUniqueIDValue();
		getMboForUniqueId( uid );
		_treeBean.setuniqueidvalue( "" + uid );
		setupButtons( mbo );

		return EVENT_HANDLED;
	}
	
	void setTreeBean(
		CommentTreeBean treeBean
	) {
		_treeBean = treeBean;
	}
}
