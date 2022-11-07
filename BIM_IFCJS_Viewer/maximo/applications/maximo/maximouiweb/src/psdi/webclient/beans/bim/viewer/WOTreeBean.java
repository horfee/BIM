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
import java.util.List;

import psdi.app.bim.viewer.virtual.BIMWorkOrderTreeRemote;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.util.MXException;
import psdi.webclient.beans.bim.Constants;
import psdi.webclient.beans.common.AssetLocDrilldownBean;
import psdi.webclient.components.BIMViewer;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ComponentInstance;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.controller.WebClientEvent;
import psdi.webclient.system.session.WebClientSession;


/**
 * @author Doug Wood
  * Custom bean to handle tree control for building model control
 */
public class WOTreeBean extends WOTreeBaseBean
{
	private BIMViewer _model = null;
	private String _assetnum = "";
	private String _location = "";
	private String _siteId   = "";
	
	DataBean _locationBean;
	DataBean _assetBean;
	
	protected void initialize() throws MXException, RemoteException
	{	
		//System.out.println(">>> WOTreeBean initialize hit v4");
		super.initialize();
		
		// when this is initialized, it will likely mean the location on the workorder has changed
		if(_viewModelLocBean != null)  // _viewModelLocBean is set in WOTreeBaseBean
		{
			_viewModelLocBean.setCurrentRecordToAppLocation();
			WebClientSession wcs = this.app.getWebClientSession();
			ControlInstance ctrl = wcs.findControl( CTRL_TREE_LOC ); // have to correct the current asset for both sections 
			if( ctrl != null )
			{
				DataBean db = ctrl.getDataBean();
				db.getMboForUniqueId(_viewModelLocBean.getUniqueIdValue());
			}
		}
		
		////System.out.println(">>> WOTreeBean initialize getBoundTree: " + getBoundTree());
		ControlInstance bvCtrl = clientSession.getControlInstance("model_location_view");
		//System.out.println(">>> WOTreeBean initialize get BIMViewer ctrl: " + bvCtrl);
		if(bvCtrl != null)
		{
			ComponentInstance vert = bvCtrl.getComponents().get(0);
			if(vert != null)
			{
				//System.out.println(">>> WOTreeBean initialize get BIMViewer vert: " + vert);
				//ComponentInstance horiz = vert.getControl().getComponents().get(0);
				ComponentInstance horiz = (ComponentInstance) vert.getChildren().get(0);
				if(horiz != null)
				{
					//System.out.println(">>> WOTreeBean initialize get BIMViewer horiz: " + horiz);
					//ComponentInstance ci = horiz.getControl().getComponents().get(0);
					ComponentInstance ci = (ComponentInstance) horiz.getChildren().get(0);
					//System.out.println(">>> WOTreeBean initialize get BIMViewer ci: " + ci);
					if( ci != null && ci instanceof BIMViewer )
					{
						//System.out.println(">>> WOTreeBean initialize BIMViewer assign before");
						_model = (BIMViewer)ci;
						_model.forceUpdate(); // we had a problem where next/prev record wasn't initializing everything correctly
						//System.out.println(">>> WOTreeBean initialize BIMViewer assign after");
					}
				}
			}
		}

		DataBean appBean = app.getDataBean();
		
		MboRemote woNewMbo = appBean.getMbo();
		boolean newVO = false;
		if( woNewMbo != _woMbo )
		{
			_woMbo = woNewMbo;
			newVO = true;
		}
		
		_viewModelLocBean.addListener( this );
		_viewModelLocBean.setMainTree( this );
		_viewTreeLocBean.addListener( this );
		appBean.addListener( this );
		WebClientSession wcs = this.app.getWebClientSession();
		ControlInstance ctrl = wcs.findControl( "main_grid3_4" );
		if( ctrl != null )
		{
			_locationBean = ctrl.getDataBean();
			_locationBean.addListener( this );
		}

		ctrl = wcs.findControl( "main_grid3_10" );
		if( ctrl != null )
		{
			_assetBean = ctrl.getDataBean();
			_assetBean.addListener( this );
		}
		
		if( newVO )
		{
			MboSetRemote locMboSet = _woMbo.getMboSet( "LOCATION" );
			MboRemote locMbo      = locMboSet.getMbo( 0 );
			if( locMbo != null  )
			{
				_viewTreeLocBean.getMboForUniqueId( locMbo.getUniqueIDValue() );
			}
		}

		update();
	}
//	
//	public void forceUpdateToWOLoc() throws RemoteException, MXException
//	{
//		////System.out.println(">>> WOTreeBean forceUpdateToWOLoc hit");
//		_location = "";
//		////System.out.println(">>> WOTreeBean getBoundTree()1: " + getBoundTree());
//		if(getBoundTree() == null)
//		{
//			super.initialize();
//		}
//		////System.out.println(">>> WOTreeBean getBoundTree()2: " + getBoundTree());
//		update();
//	}
	
	private void update() 
		throws RemoteException, 
		       MXException
	{
		////System.out.println(">>> WOTreeBean update hit");
		boolean changed = false;
		// Get the leaf node that is specified in the work order.  It may be an asset 
		// or a location
		DataBean appBean = app.getAppBean();
		String assetnum = appBean.getString( Constants.FIELD_ASSETNUM );
		String location = appBean.getString(  Constants.FIELD_LOCATION );
		String siteId   = appBean.getString( Constants.FIELD_SITEID );
		if( !_assetnum.equals( assetnum  ))
		{
			changed = true;
			_assetnum = assetnum;
		}
		if( !_location.equals( location ))
		{
			changed = true;
			_location = location;
		}
		if( !_siteId.equals( siteId ))
		{
			changed = true;
			_siteId = siteId;
		}
		
		////System.out.println(">>> WOTreeBean changed: " + changed);
		if( changed )
		{
			populateTree();
			processLocationChange();

			CacheEntry node = lookupAsset( _siteId, _assetnum );
			if( node == null )
			{
				node = lookupLocation( _siteId, _location );
			}
			if( node != null && node._mbo != null )
			{
				setuniqueidname( "" + node._mbo.getUniqueIDName() );
			}
		}
	}
	
	@Override
	protected void selectItemInModel(
		long num
	) 
		throws RemoteException, 
		       MXException 
	{
		////System.out.println(">>> WOTreeBean selectItemInModel hit");
		super.selectItemInModel( num );
		if( _viewModelLocBean != null && _viewModelLocBean.getMultiTree() != null )
		{
			_viewModelLocBean.getMultiTree().processLocationChange();
		}
	}

	
	@Override
	public void dataChangedEvent(
	    DataBean speaker 
    ) {
		////System.out.println(">>> WOTreeBean dataChangedEvent hit");
		if( speaker instanceof AssetLocDrilldownBean ) return;

		if( speaker == app.getDataBean() || speaker == _locationBean || speaker == _assetBean )
		{
			try
            {
	            update();
//	            speaker.fireStructureChangedEvent();
	            fireStructureChangedEvent();
            }
	        catch( Exception e )
	        {
		        e.printStackTrace();	// Ignore - nothing we can do
	        }
			return;
		}
		if( speaker == _viewModelLocBean )
		{
			processLocationChange();
			return;
		}
		super.dataChangedEvent( speaker );
	}
	
	private void populateTree() 
		throws RemoteException, 
		       MXException
	{
		////System.out.println(">>> WOTreeBean populateTree hit");
		MboSetRemote treeSet = getMboSetRemote();
		treeSet.clear();
		getBoundTree().setChangedFlag( true );
		
		CacheEntry node;
		Hashtable<String,BIMWorkOrderTreeRemote> mboCache = new Hashtable<String,BIMWorkOrderTreeRemote>(); 
		

		////System.out.println(">>> WOTreeBean populateTree _assetnum: " + _assetnum);
		////System.out.println(">>> WOTreeBean populateTree _location: " + _location);
		if( _assetnum != null && _assetnum.length() > 0 )
		{
			node = lookupAsset( _siteId, _assetnum );
			// This is an error that should never happen. If it does, just quietly skip the entry
			// It just missing for the tree display
			////System.out.println(">>> WOTreeBean populateTree asset node: " + node);
			if( node == null )
			{
				return;
			}
		}
		else if( _location != null && _location.length() > 0 )
		{
			node = lookupLocation( _siteId, _location );
			// This is an error that should never happen. If it doesn't just quitely skip the entry
			// It just missing for the tree display
			////System.out.println(">>> WOTreeBean populateTree location node: " + node);
			if( node == null )
			{
				return;
			}
		}
		else
		{
			// No value set yet
			////System.out.println(">>> WOTreeBean populateTree no value set");
			return;
		}
			
		BIMWorkOrderTreeRemote treeMbo = (BIMWorkOrderTreeRemote)treeSet.add(); 
		populateTreeNode( treeMbo, node );
		
		populatePathToTop( treeSet, treeMbo, mboCache, node );
	}
	
//	@Override
//    public int selectnode() throws MXException, RemoteException
//	{	
//		////System.out.println(">>> WOTreeBean selectnode hit <<<");
//		try
//		{				
//			WebClientSession wcs = this.app.getWebClientSession();
//			ControlInstance ctrl = wcs.findControl( CTRL_VIEW_LOC );
//			String targetId = ctrl.getId();
//			WebClientEvent wce = new WebClientEvent("joshevent", targetId, "joshtest", wcs);
//			WebClientRuntime.sendEvent(wce);
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//		
//		return EVENT_HANDLED;
//	}

}