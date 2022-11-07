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
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Hashtable;

import psdi.app.bim.BIMService;
import psdi.app.bim.viewer.BuildingModel;
import psdi.app.bim.viewer.virtual.BIMWorkOrderTree;
import psdi.app.bim.viewer.virtual.BIMWorkOrderTreeRemote;
import psdi.app.bim.viewer.virtual.BIMWorkOrderTreeSetRemote;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.mbo.SqlFormat;
import psdi.security.UserInfo;
import psdi.server.MXServer;
import psdi.util.MXException;
import psdi.webclient.beans.bim.Constants;
import psdi.webclient.beans.common.TreeControlBean;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.runtime.WebClientRuntime;
import psdi.webclient.system.session.WebClientSession;

public class WOTreeBaseBeanToo
    extends TreeControlBean
{
	public static final String CTRL_VIEW_LOC = "model_viewer";
	public static final String CTRL_TREE_LOC = "view_tree_loc";

    private boolean                       _initizialzed      = false;
	private MXServer                      _server            = null;
    private SqlFormat                     _sqlLocation       = null;
    private SqlFormat                     _sqlAsset          = null;
	private BIMWorkOrderTreeSetRemote     _treeSet           = null;
    
	protected MboRemote                   _woMbo             = null;
	private Hashtable<String, CacheEntry> _cache;
    
	protected DataBean                    _viewTreeLocBean; 
	protected WOModelLocBean              _viewModelLocBean;
    
	@Override
    protected void initialize() throws MXException, RemoteException
	{	
		//System.out.println(">>> WOTreeBaseBeanToo initialize hit: " + getId());
		super.initialize();
		if( !_initizialzed )
		{
			_cache       = new Hashtable<String, CacheEntry>();
	        _server      = MXServer.getMXServer();
			_sqlLocation = new SqlFormat( Constants.FIELD_SITEID + "=:1" + " AND " + Constants.FIELD_LOCATION + "=:2" );
			_sqlAsset    = new SqlFormat( Constants.FIELD_SITEID + "=:1" + " AND " + Constants.FIELD_ASSETNUM + "=:2" );
			_initizialzed = true;
		}
		
		storeProperties();

		WebClientSession wcs = this.app.getWebClientSession();
		ControlInstance ctrl = wcs.findControl( CTRL_VIEW_LOC );
		if( ctrl != null )
		{
			DataBean db = ctrl.getDataBean();
			if( db instanceof WOModelLocBean )
			{
				_viewModelLocBean =  (WOModelLocBean)db;
			}
		}

		ctrl = wcs.findControl( CTRL_TREE_LOC );
		if( ctrl != null )
		{
			_viewTreeLocBean = ctrl.getDataBean();
		}
	}
	
	/** 
	 *	Handles the selectnode event fired from the frontend or through TreeNode
	 *  It gets MBo for the uniqueid passed whena a node is selected
	 *  
	 *   @return EVENT_HANDLED;
	 */
	@Override
    public int selectnode() throws MXException, RemoteException
	{	
		//System.out.println(">>> WOTreeBaseBeanToo selectnode hit");
		try
		{				
			if(!WebClientRuntime.isNull(uniqueidvalue))
			{
				long num = NumberFormat.getIntegerInstance(clientSession.getUserInfo().getLocale()).parse(this.uniqueidvalue).longValue(); 
				this.getMboForUniqueId(num);
				selectItemInModel( num );
			}
		}
		catch(ParseException e)
		{
			// Do Nothing
		}
		
		return EVENT_HANDLED;
	}
	
	protected void selectItemInModel(
		long num
	) 
		throws RemoteException, 
		       MXException 
	{
		//System.out.println(">>> WOTreeBaseBeanToo selectItemInModel hit");
		BIMWorkOrderTreeSetRemote treeSet = (BIMWorkOrderTreeSetRemote)getMboSetRemote();
		MboRemote treeMbo = treeSet.getMboForUniqueId( num );
		if( treeMbo == null )
		{
			return;
		}
		String assetnum  = treeMbo.getString( BIMWorkOrderTree.FIELD_ASSETNUM );
		String location = treeMbo.getString( BIMWorkOrderTree.FIELD_LOCATION );
		String siteId   = treeMbo.getString( BIMWorkOrderTree.FIELD_SITEID );
 		String key;
		if( assetnum != null  && assetnum.length() > 0 )
		{
	 		key = CacheEntry.ASSET + siteId + assetnum;
		}
		else
		{
	 		key = CacheEntry.LOCATION + siteId + location;
		}
 		CacheEntry locationEntry = _cache.get( key );
 		if( locationEntry == null || locationEntry._mbo == null )
 		{
			return;
 		}
 		_viewTreeLocBean.getMboForUniqueId( locationEntry._mbo.getUniqueIDValue() );
 		_viewModelLocBean.getMboForUniqueId( locationEntry._mbo.getUniqueIDValue() );
	}
	
	@Override
    public void structureChangedEvent(
	    DataBean speaker 
    ) {
		//System.out.println(">>> WOTreeBaseBeanToo structureChangedEvent hit");
		if( speaker == _viewModelLocBean )
		{
			return;
		}
		else if( speaker == _viewTreeLocBean )
		{
	        try
	        {
		 		MboRemote mbo = _viewTreeLocBean.getMbo();
		 		if( mbo != null )
		 		{
		 			_viewModelLocBean.getMboForUniqueId( mbo.getUniqueIDValue() );
		 		}
	        }
	        catch( Exception e )
	        {
		        e.printStackTrace();	// Ignore - nothing we can do
	        }
			return;
		}
		super.structureChangedEvent( speaker );
	}
	
	public void processLocationChange()
	{
		//System.out.println(">>> WOTreeBaseBeanToo processLocationChange hit");
        try
        {
    		//System.out.println(">>> WOTreeBaseBeanToo processLocationChange _viewModelLocBean.getMboSet().getCompleteWhere(): " + _viewModelLocBean.getMboSet().getCompleteWhere());
	 		MboRemote mbo = _viewModelLocBean.getMbo();
	 		if( mbo != null )
	 		{
	 			//System.out.println(">>> WOTreeBaseBeanToo processLocationChange mbo != null");
				 _viewTreeLocBean.getMboForUniqueId( mbo.getUniqueIDValue() );
				String location = mbo.getString( Constants.FIELD_LOCATION );
				String siteId   = mbo.getString( Constants.FIELD_SITEID );
				
				boolean match = false;
				int count = count();
				for( int i = 0; i < count; i++ )
				{
					MboRemote treeMbo = getMbo( i );
					if( treeMbo == null  ) continue;
					String newLoc = treeMbo.getString( BIMWorkOrderTree.FIELD_LOCATION );
					if( !newLoc.equals( location )) continue;
					String newSite = treeMbo.getString( BIMWorkOrderTree.FIELD_SITEID );
					if( !newSite.equals( siteId )) continue;
					long uid = treeMbo.getUniqueIDValue();
		 			//System.out.println(">>> WOTreeBaseBeanToo processLocationChange this.getMboForUniqueId(uid): " + uid);
					this.getMboForUniqueId(uid);
					setuniqueidvalue( "" + uid );

					WebClientSession wcs = this.app.getWebClientSession();
					ControlInstance ctrl = wcs.findControl( CTRL_VIEW_LOC );
					if( ctrl != null )
					{
						//System.out.println(">>> WOTreeBaseBeanToo processLocationChange firing alpha re-render start3");
//						ctrl.render();
//						ctrl.renderChildren();
						ctrl.setChangedFlag();
//						ctrl.getDataBean().sendRefreshTable();
//						ctrl.getDataBean().fireChildChangedEvent();
//						ctrl.getDataBean().fireDataChangedEvent();
						ctrl.getDataBean().fireStructureChangedEvent();
						//System.out.println(">>> WOTreeBaseBeanToo processLocationChange firing alpha re-render end");
					}
					
					match = true;
					break;					
				}
				if( !match )
				{
					setuniqueidvalue( "" );
				}
	 		}
        }
        catch( Exception e )
        {
    		//System.out.println(">>> WOTreeBaseBeanToo processLocationChange exception");
	        e.printStackTrace();	// Ignore - nothing we can do
        }
		//System.out.println(">>> WOTreeBaseBeanToo processLocationChange end");
	}

	
 	@Override
    protected MboSetRemote getMboSetRemote() 
		throws MXException, 
		       RemoteException
	{
		//System.out.println(">>> WOTreeBaseBeanToo getMboSetRemote hit");
		if( getobjectname() == null || getobjectname().equals("") )
		{
			storeProperties();
		}
		
		if( _treeSet != null )
		{
			//System.out.println(">>> WOTreeBaseBeanToo _treeSet != null");
			return _treeSet;
		}
	
		DataBean appBean = app.getDataBean();
		MboRemote appMbo = appBean.getMbo();
		if( appMbo == null )
		{
			//System.out.println(">>> WOTreeBaseBeanToo appMbo == null");
			return null;
		}
	    MXServer server = MXServer.getMXServer();
	    UserInfo userInfo = appMbo.getUserInfo();
		_treeSet = (BIMWorkOrderTreeSetRemote)server.getMboSet( BIMWorkOrderTree.TABLE_IMWOTREE, 
		                                                        userInfo );
		//System.out.println(">>> WOTreeBaseBeanToo end");
		return _treeSet;
	}


	protected void populateTreeNode(
		MboRemote treeMbo,
		CacheEntry node
	) 
		throws RemoteException, 
		       MXException 
	{
		//System.out.println(">>> WOTreeBaseBeanToo populateTreeNode hit");
		treeMbo.setValue( BIMWorkOrderTree.FIELD_ASSETNUM, node._assetId  );
		treeMbo.setValue( BIMWorkOrderTree.FIELD_SITEID, node._siteId );
		treeMbo.setValue( BIMWorkOrderTree.FIELD_LOCATION, node._locationId );

		if( node._assetId != null && node._assetId.length() > 0 )
		{
			treeMbo.setValue(  BIMWorkOrderTree.FIELD_LABEL, node._assetId );
		}
		else if( node._locationId != null && node._locationId.length() > 0 )
		{
			treeMbo.setValue(  BIMWorkOrderTree.FIELD_LABEL, node._locationId );
		}
		treeMbo.setValue( BIMWorkOrderTree.FIELD_DESCRIPTION, node._description );
		treeMbo.setValue( BIMWorkOrderTree.FIELD_MODELID, node._modelId );
		if( node._modelTitle != null )
		{
			treeMbo.setValue( BIMWorkOrderTree.FIELD_MODELTITLE, "<strong> " + node._modelTitle + " </strong>" );
		}
	}
	
	/*
	 * Find the path to the top for each entry merging paths where they intersect and skipping
	 * locations tagged as operating (imported from component)
	 */
	protected void populatePathToTop(
		MboSetRemote                             treeSet,
		BIMWorkOrderTreeRemote                   treeMbo,
		Hashtable<String,BIMWorkOrderTreeRemote> mboCache,
		CacheEntry                               node
	) 
		throws RemoteException, MXException 
	{
		//System.out.println(">>> WOTreeBaseBeanToo populatePathToTop hit");
		String parent =  node._parent; 
		while( parent != null && parent.length() > 0 )
		{
			String current = parent;
			CacheEntry nodePrev = node;
			// See if a node has already been created for this.  If so then we can just use it
			// and we are done because the rest of the path has already been created
			BIMWorkOrderTreeRemote parentMbo = mboCache.get( CacheEntry.LOCATION + node._siteId + current );
			if( parentMbo != null )
			{
				treeMbo.setValue( BIMWorkOrderTree.FIELD_PARENT , parentMbo.getUniqueIDValue() );
				break;
			}

			// See if the parent is in the cache
			node = lookupLocation( node._siteId, current );
			if( node == null )
			{
				break;			// Underlying data structure error - just skip this node
			}

			parent = node._parent;

			// Is the parent a location to be skipped?
			String tag = treeMbo.translateImportDomainValue( node._spaceType );
			if(    tag != null && tag.equals( BIMService.IMPORT_SCR_COMPONENT ))
			{
				if( nodePrev._mbo == null )
				{
					nodePrev._mbo = node._mbo;
				}
				String loc = treeMbo.getString( BIMWorkOrderTree.FIELD_LOCATION );
				if( loc == null || loc.length() == 0 )
				{
					treeMbo.setValue( BIMWorkOrderTree.FIELD_LOCATION, node._locationId );
				}
				continue;
			}

			parentMbo     = (BIMWorkOrderTreeRemote)treeSet.add(); 
			populateTreeNode( parentMbo, node );

			mboCache.put( CacheEntry.LOCATION + node._siteId + current , parentMbo );
			
			treeMbo.setValue( BIMWorkOrderTree.FIELD_PARENT , parentMbo.getUniqueIDValue() );
			treeMbo = parentMbo; 
		}
	}
	
 	protected CacheEntry lookupLocation(
 		String siteId,
 		String location
	) 
 		throws RemoteException, 
 		       MXException 
 	{
		//System.out.println(">>> WOTreeBaseBeanToo lookupLocation hit");
 		String key = CacheEntry.LOCATION + siteId + location;
 		CacheEntry locationEntry = _cache.get( key );
 		if( locationEntry != null )
 		{
 			return locationEntry;
 		}
 		
		MboSetRemote locationSet = null;
		locationSet = _server.getMboSet( Constants.TABLE_LOCATIONS, _woMbo.getUserInfo() );
		_sqlLocation.setObject( 1, Constants.TABLE_LOCATIONS, Constants.FIELD_SITEID, siteId ); 
		_sqlLocation.setObject( 2, Constants.TABLE_LOCATIONS, Constants.FIELD_LOCATION, location ); 
		locationSet.setWhere( _sqlLocation.format() );
		locationSet.reset();
 		
 		if( locationSet.isEmpty() )
 		{
 			return null;		// Only happen of the asset is deleted while we are processing this
 		}
 		locationEntry = new CacheEntry( CacheEntry.LOCATION, siteId, location );
 		MboRemote locationMbo = locationSet.getMbo( 0 );
 		locationEntry._description = locationMbo.getString( Constants.FIELD_DESCRIPTION );
 		locationEntry._modelId     = locationMbo.getString( Constants.FIELD_MODELID );
 		locationEntry._parent      = locationMbo.getString( Constants.FIELD_PARENT );
 		locationEntry._spaceType   = locationMbo.getString( Constants.FIELD_BIMIMPORTSRC );
 		locationEntry._mbo         = locationMbo;
 		
 		MboSetRemote modelSet = locationMbo.getMboSet( "LOCMODEL" );
 		if( !modelSet.isEmpty() )
 		{
 			MboRemote modelMbo = modelSet.getMbo( 0 );
 			locationEntry._modelTitle  = modelMbo.getString( BuildingModel.FIELD_TITLE );
 		}
 		
 		return locationEntry;
 	}

 	protected CacheEntry lookupAsset(
 		String siteId,
 		String assetNum
	) 
 		throws RemoteException, 
 		       MXException 
 	{
		//System.out.println(">>> WOTreeBaseBeanToo lookupAsset hit");
 		String key = CacheEntry.ASSET + siteId + assetNum;
 		CacheEntry asset = _cache.get( key );
 		if( asset != null )
 		{
 			return asset;
 		}
 		
		MboSetRemote assetSet = null;
		assetSet = _server.getMboSet( Constants.TABLE_ASSET, _woMbo.getUserInfo() );
		_sqlAsset.setObject( 1, Constants.TABLE_ASSET, Constants.FIELD_SITEID, siteId ); 
		_sqlAsset.setObject( 2, Constants.TABLE_ASSET, Constants.FIELD_ASSETNUM, assetNum ); 
		assetSet.setWhere( _sqlAsset.format() );
		assetSet.reset();
 		
 		if( assetSet.isEmpty() )
 		{
 			return null;		// Only happen of the asset is delted while we are processing this
 		}
 		asset = new CacheEntry( CacheEntry.ASSET, siteId, assetNum );
 		MboRemote assetMbo = assetSet.getMbo( 0 );
 		asset._description = assetMbo.getString( Constants.FIELD_DESCRIPTION );
 		asset._modelId     = assetMbo.getString( Constants.FIELD_MODELID );
 		asset._parent      = assetMbo.getString( Constants.FIELD_LOCATION );
 		
 		return asset;
 	}


 	protected class CacheEntry
 	{
 		static final String SITE     = "SITE";
 		static final String LOCATION = "LOCATION";
 		static final String ASSET    = "ASSET";
 		
 		CacheEntry(
 		    String objectName,
 		    String siteId,
 		    String objectId
	 	) {
 			//System.out.println(">>> WOTreeBaseBeanToo CacheEntry hit");
	 		_objectName = objectName;
	 		_siteId     = siteId;
	 		if( objectName.equals( LOCATION ))
	 		{
	 			_locationId = objectId;
	 		}
	 		else if( objectName.equals( ASSET ))
	 		{
	 			_assetId = objectId;
	 		}
	 		_cache.put( getKey(), this );
	 	}
 		
 		String getKey()
 		{
 			//System.out.println(">>> WOTreeBaseBeanToo CacheEntry.getKey hit");
 	 		if( _objectName.equals( SITE ) )
			{
 	 			return SITE + _siteId;
			}
 	 		if( _objectName.equals( LOCATION ) )
			{
 	 			return LOCATION + _siteId + _locationId;
			}
 	 		if( _objectName.equals( ASSET ) )
			{
 	 			return ASSET + _siteId + _assetId;
			}
 	 		return null;
 		}
 		
 		String    _objectName;
 		String    _siteId;
 		String    _locationId;
 		String    _assetId;
 		String    _description;
 		String    _modelId;
 		String    _modelTitle;
 		String    _parent;
 		String    _spaceType;
 		MboRemote _mbo;
 	}
}