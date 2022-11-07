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
package psdi.webclient.beans.bim.viewer;

import java.rmi.RemoteException;
import java.util.Hashtable;

import psdi.app.bim.viewer.virtual.BIMWorkOrderTree;
import psdi.app.bim.viewer.virtual.BIMWorkOrderTreeRemote;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.webclient.beans.bim.Constants;
import psdi.webclient.beans.common.AssetLocDrilldownBean;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.session.WebClientSession;


/**
 * @author Doug Wood
  * Custom bean to handle tree control for building model control
 */
public class WOMultiTreeBean extends WOTreeBaseBean
{
	public static final String FIELD_MULTIID = "MULTIID";
	
	private DataBean _multiLocBean;

	@Override	
	public void setupBean( 
		WebClientSession wcs 
	) {	
		//System.out.println(">>> WOMultiTreeBean setupBean hit");
		super.setupBean( wcs );
	}
	
	@Override
	protected void initialize() 
		throws RemoteException, 
		       MXException
	{
		//System.out.println(">>> WOMultiTreeBean initialize hit");
		super.initialize();
		
		DataBean appBean = app.getDataBean();
		
		MboRemote woNewMbo = appBean.getMbo();
		boolean newVO = false;
		if( woNewMbo != _woMbo )
		{
			_woMbo = woNewMbo;
			newVO = true;
		}
		
		if( newVO )
		{
			if ( _woMbo.isBasedOn("WORKORDER"))
		    {
				_multiLocBean = app.getDataBean( Constants.CTRL_WO_ASSET_LOC_TABLE );
		    }
			else if ( _woMbo.isBasedOn("TICKET"))
		    {
				_multiLocBean = app.getDataBean( Constants.CTRL_SR_ASSET_LOC_TABLE );
		    }
			else
			{
				throw new MXApplicationException( Constants.BUNDLE_MSG, Messages.MSG_UNSUPPORTED_APP );	
			}
		}
		populateTree( _multiLocBean );
		processLocationChange();
		fireStructureChangedEvent();
		
		_multiLocBean.addListener( this );
		_viewModelLocBean.addListener( this );
		_viewModelLocBean.setMultiTree( this );
		_viewTreeLocBean.addListener( this );
	}
	
	@Override
	public void dataChangedEvent(
	    DataBean speaker 
    ) {
		//System.out.println(">>> WOMultiTreeBean dataChangedEvent hit");
		if( speaker instanceof AssetLocDrilldownBean ) return;

		if( speaker == _viewModelLocBean )
		{
			processLocationChange();
			return;
		}
		super.dataChangedEvent( speaker );
	}
	
	
	@Override
	protected void selectItemInModel(
		long num
	) 
		throws RemoteException, 
		       MXException 
	{
		//System.out.println(">>> WOMultiTreeBean selectItemInModel hit");
		super.selectItemInModel( num );
		if( _viewModelLocBean != null && _viewModelLocBean.getMainTree() != null )
		{
			_viewModelLocBean.getMainTree().processLocationChange();
		}
	}

	private void populateTree(
	    DataBean multiLocBean 
    ) 
		throws RemoteException, 
		       MXException
	{
		//System.out.println(">>> WOMultiTreeBean populateTree hit");
		MboSetRemote treeSet = getMboSetRemote();
		treeSet.clear();
		getBoundTree().setChangedFlag( true );

		CacheEntry node;
		Hashtable<String,BIMWorkOrderTreeRemote> mboCache = new Hashtable<String,BIMWorkOrderTreeRemote>(); 
		
		for( int i = 0; i < multiLocBean.count(); i++ )
		{
			// Get the leaf node that is specified in the work order.  It may be an asset 
			// or a location
			MboRemote multiLocMbo = multiLocBean.getMbo(i);
			if( multiLocMbo.toBeDeleted() )
			{
				continue;
			}
			
			String siteId = multiLocMbo.getString( Constants.FIELD_SITEID );
			String assetnum = multiLocMbo.getString( Constants.FIELD_ASSETNUM );
			String location = multiLocMbo.getString( Constants.FIELD_LOCATION );
			String multiId  = multiLocMbo.getString( FIELD_MULTIID );
			if( assetnum != null && assetnum.length() > 0 )
			{
				node = lookupAsset( siteId, assetnum );
				// This is an error that should never happen. If it doesn, just quitely skip the entry
				// It just missing for the tree display
				if( node == null )
				{
					continue;
				}
			}
			else if( location != null && location.length() > 0 )
			{
				node = lookupLocation( siteId, location );
				// This is an error that should never happen. If it doesn't just quitely skip the entry
				// It just missing for the tree display
				if( node == null )
				{
					continue;
				}
			}
			else
			{
				// A CI or something else we can't display
				continue;
			}
				
			BIMWorkOrderTreeRemote treeMbo = (BIMWorkOrderTreeRemote)treeSet.add(); 
			populateTreeNode( treeMbo, node );
			treeMbo.setValue( BIMWorkOrderTree.FIELD_MULTIID, multiId );
			
			populatePathToTop( treeSet, treeMbo, mboCache, node );
		}
	}
}