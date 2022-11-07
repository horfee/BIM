/*
 *
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * 5724-R46
 *
 * (C) COPYRIGHT IBM CORP. 2006-2013
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 */
package psdi.webclient.controls;

import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import psdi.mbo.MboValueData;
import psdi.util.MXException;
import psdi.webclient.controls.Tree;
import psdi.webclient.controls.TreeNode;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.controller.WebClientEvent;
import psdi.webclient.system.session.WebClientSession;

/**
 * Adds the ability to pragmatically highlight a node in the tree
 * @author Doug
 *
 */
public class BIMTree
    extends Tree
{
	private boolean _autoselect      = true;
	private boolean _selectFirstNode = false;
	private boolean _amIinitialize   = false;

	public void initialize()
	{
		if( _amIinitialize )
			return;
		_amIinitialize = true;

		_selectFirstNode = getProperty("selectfirstnode").equals("true");
		setProperty("selectfirstnode", "false" );

		super.initialize();
	}


	@Override
	public void createNodes()
	{
		String id = getTreeDataBean().getuniqueidvalue();
		super.createNodes();
		try
        {
			if( _autoselect )
			{
				id = getTreeDataBean().getuniqueidvalue();
		        setSelectedNode( id, false );
			}
        }
        catch( Exception e )
        {
	        e.printStackTrace();
        }
	}

	public void setSelectedNode(
		String  uid
	)
		throws RemoteException,
		       MXException
	{
		setSelectedNode( uid, true );
	}

	/**
	 * Recursively search all tree nodes for a not the matches the uid, and select that node
	 * @param uid
	 * @param sendEvent True, an event is sent to select the node, false, the node is just marked as the selected node.
	 * @throws RemoteException
	 * @throws MXException
	 */
	protected void setSelectedNode(
		String  id,
		boolean sendEvent
	)
		throws RemoteException,
		       MXException
	{
		WebClientSession wcs = getWebClientSession();

		Iterator<?> itr = getChildren().listIterator();
		long uid = -1;
		TreeNode firstNode = null;
		while (itr.hasNext())
		{
			ControlInstance ctrl = (ControlInstance) itr.next();
			if( ctrl instanceof TreeNode )
			{
				TreeNode treeNode = (TreeNode)ctrl;
				if( firstNode == null )
				{
					firstNode = treeNode;
				}
				// If nothing is selected, Select the first node
				if( id == null || id.length() == 0 )
				{
					if( _selectFirstNode )
					{
						treeNode.selectnode();
					}
					return;
				}
				if( uid < 0  )
				{
					try
					{
						uid = NumberFormat.getIntegerInstance( wcs.getUserInfo().getLocale()).parse( id ).longValue();
					}
					catch(ParseException e)
					{
						if( _selectFirstNode )
						{
							treeNode.selectnode();
						}
						return;
					}
				}

				if( selectNode( wcs, treeNode, uid, sendEvent ))
				{
					return;
				}
			}
		}

		// Got here because the target UID didn't match any nodes
		if( firstNode != null  && _selectFirstNode )
		{
			firstNode.selectnode();
		}
	}

	protected boolean selectNode(
		WebClientSession wcs,
		TreeNode         treeNode,
		long             targetUID,
		boolean          sendEvent
	)
		throws RemoteException,
		       MXException
	{
		long nodeUID = getNodeId( wcs, treeNode );
		if( nodeUID < 0 ) return false;
		if( targetUID == nodeUID  )
		{
			if( sendEvent  )
			{
				WebClientEvent wce = new WebClientEvent("selectnode", treeNode.getId(), null, wcs );
				getWebClientSession().queueEvent( wce );
			}
			else
			{
				setSelectedNode( treeNode );
			}
			return true;
		}
		if( selectChildNode( wcs, treeNode, targetUID, sendEvent ))
		{
			return true;
		}
		return false;
	}

	private long getNodeId(
		WebClientSession wcs,
		TreeNode         treeNode
	) {
		Object obj[] = treeNode.getNodeData();
		if( obj != null && obj.length > 1 && obj instanceof MboValueData[] )
		{
			MboValueData[] mvd = (MboValueData[])obj;
			if( mvd[1].getData() == null ) return -1;
			try
			{
				return NumberFormat.getIntegerInstance( wcs.getUserInfo().getLocale()).parse( mvd[1].getData() ).longValue();
			}
			catch(ParseException e)
			{
				return -1;
			}
		}
		return -1;
	}


	protected boolean selectChildNode(
		WebClientSession wcs,
		TreeNode         treeNode,
		long             targetUID,
		boolean          sendEvent
	)
		throws RemoteException,
		       MXException
	{
		if( !treeNode.hasChildNodes() ) return false;
		List<?> children = treeNode.getChildren();
		Iterator<?> itr = children.iterator();
		while( itr.hasNext() )
		{
			treeNode = (TreeNode)itr.next();
			if( selectNode( wcs, treeNode, targetUID, sendEvent ))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isAutoselect()
    {
    	return _autoselect;
    }

	public void setAutoselect(
        boolean autoselect
    ) {
    	_autoselect = autoselect;
    }
}
