/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * 5724-U18
 *
 * Copyright IBM Corp. 2018
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 */

package psdi.webclient.beans.bim.viewer;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import psdi.app.asset.AssetSetRemote;
import psdi.app.ticket.TicketRemote;
import psdi.app.workorder.WORemote;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.mbo.SqlFormat;
import psdi.server.MXServer;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.webclient.beans.bim.Constants;
import psdi.webclient.beans.workorder.WorkorderAppBean;
import psdi.webclient.components.BIMViewer;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.beans.WebClientBean;
import psdi.webclient.system.controller.ComponentInstance;
import psdi.webclient.system.controller.WebClientEvent;

public class AssetLookupBase
    extends DataBean
{
	/**
	 * When the control is used in selection/Lookup mode, this contains
	 * the list of currently selected items 
	 */
	private HashSet<String> _values = null;

	/**
	 * The Data attribute to which the building model attribute is bound
	 * Used for build queries to translate values from the building model
	 * into assets
	 */
	private String _binding = "modelid";
	
	private BIMViewer _model = null;
	
	public MboSetRemote getMboSet() throws MXException, RemoteException
	{
		return super.getMboSet();
	}
	
	public MboSetRemote getMboSetRemote() throws MXException, RemoteException
	{
		return super.getMboSetRemote();
	}
	
	protected void initialize() throws MXException, RemoteException
	{
		super.initialize();
		setCurrentRecordToAppLocation();
		app.getDataBean().addListener(this);
	}

	@Override
	public void structureChangedEvent(DataBean speaker)
	{
		if(speaker instanceof WorkorderAppBean)
		{
			System.out.println(">>> AssetLookupBase structureChangedEvent 2");
			try {
				setCurrentRecordToAppLocation();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.structureChangedEvent(speaker);
	}

	public void setCurrentRecordToAppLocation() throws MXException, RemoteException
	{
		
		MboRemote appMbo = app.getDataBean().getMbo();
		if(appMbo != null)
		{
			if(!appMbo.isNull("location") && !appMbo.getString("location").equals(""))
			{
				this.setCurrentBeanRecord(appMbo.getString("location"));
			}
		}
		
	}
	
	public int bimviewer() 
	{
        //System.out.println(">>> AssetLookupBase bean " + (Thread.currentThread().getStackTrace()[3].toString()) + " ==> " + ((new Object() {}).getClass().getEnclosingMethod().getName()) );
		WebClientEvent event = clientSession.getCurrentEvent();
		ComponentInstance ci = event.getSourceComponentInstance();
		if( ci != null && ci instanceof BIMViewer )
		{
			_model = (BIMViewer)ci;
			_binding = _model.getBinding();
		}
		return WebClientBean.EVENT_HANDLED;
	}
	
	public int bimModelListChanged() 
		throws RemoteException, 
		       MXException
	{
        //System.out.println(">>> AssetLookupBase bean " + (Thread.currentThread().getStackTrace()[3].toString()) + " ==> " + ((new Object() {}).getClass().getEnclosingMethod().getName()) );
		WebClientEvent event = clientSession.getCurrentEvent();
		Object o = event.getValue();
		if( o == null || !(o instanceof String ))  
		{
			//System.out.println(">>> AssetLookupBase o == null || !(o instanceof String");
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		String modelLocation = (String)o;
		if( modelLocation.length() == 0 )
		{
			//System.out.println(">>> AssetLookupBase modelLocation.length() == 0");
			return WebClientBean.EVENT_HANDLED;
		}
		String appLocation = app.getDataBean().getMbo().getString("location");
		if(appLocation != null && appLocation.equals(modelLocation))
		{
			// same location, nothing to change
			return WebClientBean.EVENT_HANDLED;
		}
		
		setCurrentBeanRecord(modelLocation);
		
		return WebClientBean.EVENT_CONTINUE;
	}
	
	public void setCurrentBeanRecord(String modelLocation) throws RemoteException, MXException
	{
        //System.out.println(">>> AssetLookupBase bean " + (Thread.currentThread().getStackTrace()[3].toString()) + " ==> " + ((new Object() {}).getClass().getEnclosingMethod().getName()) );
		MboRemote mbo = getMbo();
		String siteId = getString( Constants.FIELD_SITEID );
		SqlFormat sqlf = new SqlFormat( mbo, Constants.FIELD_LOCATION + "=:1 and siteid=:2");
		sqlf.setObject(1, Constants.TABLE_LOCATIONS, Constants.FIELD_LOCATION, modelLocation );
		sqlf.setObject(2, Constants.TABLE_LOCATIONS, Constants.FIELD_SITEID, siteId ) ; 
		MXServer server = MXServer.getMXServer();
		
		MboSetRemote locationSet = server.getMboSet( Constants.TABLE_LOCATIONS, getMboSet().getUserInfo() );
		locationSet.setWhere( sqlf.format() );
		locationSet.reset();
		//System.out.println(">>> AssetLookupBase select locationsid, location, siteid, orgid from locations where " + locationSet.getCompleteWhere());
		mbo = locationSet.getMbo( 0 );
		if(mbo != null)
		{
	    	long uid = mbo.getUniqueIDValue();    
	    	getMboSet().setWhere("");
	    	getMboSet().setRelationship("");
			getMboSet().getMboForUniqueId(uid);
		}
	}
	
	protected void setLocToModelLoc(
		WebClientEvent event
	) 
		throws RemoteException, 
		       MXException 
    {
        //System.out.println(">>> AssetLookupBase bean " + (Thread.currentThread().getStackTrace()[3].toString()) + " ==> " + ((new Object() {}).getClass().getEnclosingMethod().getName()) );
	}
	
	public int eventMultiSelect()
	{
        //System.out.println(">>> AssetLookupBase bean " + (Thread.currentThread().getStackTrace()[3].toString()) + " ==> " + ((new Object() {}).getClass().getEnclosingMethod().getName()) );
		_values = new HashSet<String>();
		WebClientEvent event = clientSession.getCurrentEvent();
		ComponentInstance ci = event.getSourceComponentInstance();
		if( ci != null && ci instanceof BIMViewer )
		{
			_model = (BIMViewer)ci;
			_binding = _model.getBinding();
		}
		return appendSelection();
	}
	
	public int appendSelection()
	{
        //System.out.println(">>> AssetLookupBase bean " + (Thread.currentThread().getStackTrace()[3].toString()) + " ==> " + ((new Object() {}).getClass().getEnclosingMethod().getName()) );
		WebClientEvent event = clientSession.getCurrentEvent();
		Object o = event.getValue();
		if( o == null || !(o instanceof String ))
		{
			// Should never happen unless the .jsp is altered
			return WebClientBean.EVENT_HANDLED;
		}
		String result[] = ((String)o).split( ";" );
		for( int i = 0; i < result.length; i++ )
		{
			_values.add( result[i] );
		}
		return WebClientBean.EVENT_HANDLED;
	}
	
	public String getBinding()
	{
        //System.out.println(">>> AssetLookupBase bean " + (Thread.currentThread().getStackTrace()[3].toString()) + " ==> " + ((new Object() {}).getClass().getEnclosingMethod().getName()) );
		return _binding;
	}
	
	public BIMViewer getModel() 
		throws MXApplicationException
	{
        //System.out.println(">>> AssetLookupBase bean " + (Thread.currentThread().getStackTrace()[3].toString()) + " ==> " + ((new Object() {}).getClass().getEnclosingMethod().getName()) );
		if( _model == null )
		{
			 throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		return _model;
	}
	
	DataBean getMultiLocCITable(
		MboRemote appMbo
	) 
		throws RemoteException, 
		       MXApplicationException
	{
        //System.out.println(">>> AssetLookupBase bean " + (Thread.currentThread().getStackTrace()[3].toString()) + " ==> " + ((new Object() {}).getClass().getEnclosingMethod().getName()) );
    	if ( appMbo.isBasedOn("WORKORDER"))
        {
			return app.getDataBean( Constants.CTRL_WO_ASSET_LOC_TABLE );
        }
    	else if ( appMbo.isBasedOn("TICKET"))
        {
			return app.getDataBean( Constants.CTRL_SR_ASSET_LOC_TABLE );
        }
    	else
    	{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Messages.MSG_UNSUPPORTED_APP );	
    	}
	}
	
	public Set<String> getValueList() 
		throws MXApplicationException 
	{ 
        //System.out.println(">>> AssetLookupBase bean " + (Thread.currentThread().getStackTrace()[3].toString()) + " ==> " + ((new Object() {}).getClass().getEnclosingMethod().getName()) );
		BIMViewer model = getModel();
		Set<String> sel = model.getCurrentSelection();
		if( sel == null || sel.size() == 0 )
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Messages.MSG_NO_SELECTION );	
		}
		return sel;
	}

	/**
     * Called by field lookup dialogs to set the current record in this bean
     */
	@Override
    synchronized public int returnLookupValue(
    	MboRemote lookupMbo
	) 
    	throws MXException
    {
        //System.out.println(">>> AssetLookupBase bean " + (Thread.currentThread().getStackTrace()[3].toString()) + " ==> " + ((new Object() {}).getClass().getEnclosingMethod().getName()) );
        if( mboSetRemote == null || lookupMbo == null )
        {
            return EVENT_HANDLED;
        }
        try
        {
        	if( lookupMbo.isBasedOn("LOCATIONS") )
        	{
            	long uid = lookupMbo.getUniqueIDValue();    
    			getMboForUniqueId( uid );
        	}
        	else if( lookupMbo.isBasedOn("SITE") )
        	{
        		String siteId = lookupMbo.getString( Constants.FIELD_SITEID );
        		resetQbe();
        		setQbe( Constants.FIELD_SITEID, siteId );
        		moveTo(0);
        		fireStructureChangedEvent();
        	}
        }
		catch( RemoteException e )
		{
			handleRemoteException( e );
		}
		return EVENT_HANDLED;
    }
	
	/**
	 * Check if the assets in the result set are already on the result set.
	 * If not add them
	 * @param assetSet
	 * @param resultSet
	 * @param existingAssets
	 * @throws RemoteException
	 * @throws MXException
	 */
	protected void testResultsForDuplicates(
		Set<String>  assetnumSet,
		MboSetRemote resultSet,
		Set<String>  existingAssets
	) 
		throws RemoteException, 
		       MXException 
	{
        //System.out.println(">>> AssetLookupBase bean " + (Thread.currentThread().getStackTrace()[3].toString()) + " ==> " + ((new Object() {}).getClass().getEnclosingMethod().getName()) );
		for( int i = 0; i < resultSet.count(); i++ )
		{
			MboRemote assetMbo = resultSet.getMbo( i );
			String assetnum    = assetMbo.getString( Constants.FIELD_ASSETNUM );
			if( assetnum == null || assetnum.length() == 0 )
			{
				continue;
			}
			String siteId = assetMbo.getString( Constants.FIELD_SITEID );
			if( siteId == null || siteId .length() == 0 )
			{
				continue;
			}
			if( existingAssets.contains( assetnum+siteId ))
			{
				continue;
			}
			assetnumSet.add( assetnum );
		}
	}
	
	/**
	 * Adds assets in the parent Asset and locations table
	 * @param assetnumSet	The set of attributes to add
	 * @param siteId	The siteId of the assets to be added. All assets in assetNumSet must have the same siteId  
	 * @throws MXException 
	 * @throws RemoteException 
	 */
	public void insertAssets(
		MboRemote    appMbo,
		Set<String>  newAssets,
		String       siteid
	) 
		throws RemoteException, 
		       MXException 
	{
        //System.out.println(">>> AssetLookupBase bean " + (Thread.currentThread().getStackTrace()[3].toString()) + " ==> " + ((new Object() {}).getClass().getEnclosingMethod().getName()) );
		StringBuffer query = new StringBuffer();
		String inClause = formatInClause( newAssets.iterator() );
		if( inClause == null || inClause.length() == 0 )
		{
			return;
		}
		
		query.append( Constants.FIELD_SITEID );
		query.append( " = '" );
		query.append( siteid );
		query.append( "' AND " );

		query.append( Constants.FIELD_ASSETNUM );
		query.append( inClause );
		
        MXServer server = MXServer.getMXServer();
		MboSetRemote assetSet = server.getMboSet( Constants.TABLE_ASSET, appMbo.getUserInfo() );
		if( assetSet == null )
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		assetSet.setWhere( query.toString() );
		assetSet.reset();
		
		for( int i = 0; i < assetSet.count(); i++ )
		{
			assetSet.select( i );
		}
		
    	if ( appMbo.isBasedOn("WORKORDER"))
        {
			WORemote workorder = (WORemote)appMbo;
			workorder.copyAssetsToMultiAsset((AssetSetRemote)assetSet);  
        }
        else if ( appMbo.isBasedOn("TICKET"))
        {
        	TicketRemote ticket = (TicketRemote)appMbo;
        	ticket.copyAssetsToMultiAsset((AssetSetRemote)assetSet); 
        }
    	
    	assetSet.cleanup();
	}

	/**
	 * Adds assets in the parent Asset and locations table
	 * @param assetnumSet	The set of attributes to add
	 * @param siteId	The siteId of the assets to be added. All assets in assetNumSet must have the same siteId  
	 * @throws MXException 
	 * @throws RemoteException 
	 */
	public void insertLocations(
		MboRemote    appMbo,
		Set<String>  newLocations,
		String       siteid
	) 
		throws RemoteException, 
		       MXException 
	{
        //System.out.println(">>> AssetLookupBase bean " + (Thread.currentThread().getStackTrace()[3].toString()) + " ==> " + ((new Object() {}).getClass().getEnclosingMethod().getName()) );
		StringBuffer query = new StringBuffer();
		String inClause = formatInClause( newLocations.iterator() );
		if( inClause == null || inClause.length() == 0 )
		{
			return;
		}
		
		query.append( Constants.FIELD_SITEID );
		query.append( " = '" );
		query.append( siteid );
		query.append( "' AND " );

		query.append( Constants.FIELD_LOCATION );
		query.append( inClause );
		
        MXServer server = MXServer.getMXServer();
		MboSetRemote locationSet = server.getMboSet( Constants.TABLE_LOCATIONS, appMbo.getUserInfo() );
		if( locationSet == null )
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		locationSet.setWhere( query.toString() );
		locationSet.reset();
		
		for( int i = 0; i < locationSet.count(); i++ )
		{
			locationSet.select( i );
		}
		
    	if ( appMbo.isBasedOn("WORKORDER"))
        {
			WORemote workorder = (WORemote)appMbo;
			workorder.copyLocationsToMultiAsset( locationSet );
        }
        else if ( appMbo.isBasedOn("TICKET"))
        {
        	TicketRemote ticket = (TicketRemote)appMbo;
        	ticket.copyLocationsToMultiAsset( locationSet );
        }
    	
    	locationSet.cleanup();
	}
	
	/**
	 * Compares the proposed insertions against the existing table.  Duplicates
	 * are remove and items in the table that are delted, but are in the insertion
	 * set are undeleted
	 * @param assetsDataBean
	 * @return	Set of assets with the ID being assetnum + siteId
	 * @throws RemoteException
	 * @throws MXException
	 */
	protected void filterAndUndelete(
	    DataBean    multiLocDataBean,
	    String      modelSiteId,
	    Set<String> selectedAssets,
	    Set<String> selectedLocations
	) 
		throws RemoteException, 
		       MXException 
	{
        //System.out.println(">>> AssetLookupBase bean " + (Thread.currentThread().getStackTrace()[3].toString()) + " ==> " + ((new Object() {}).getClass().getEnclosingMethod().getName()) );
		if( selectedAssets == null )
		{
			selectedAssets = new HashSet<String>();
		}
		if( selectedLocations == null )
		{
			selectedLocations = new HashSet<String>();
		}
    	if( multiLocDataBean == null )
    	{
        	return;
    	}
		for( int i = 0; i < multiLocDataBean.count(); i++ )
		{
			MboRemote multiLocMbo = multiLocDataBean.getMbo( i );
			String siteId      = multiLocMbo.getString( Constants.FIELD_SITEID );
			if( siteId == null || siteId .length() == 0 )
			{
				continue;
			}
			if( !siteId.equals( modelSiteId ))
			{
				continue;
			}

			String assetnum    = multiLocMbo.getString( Constants.FIELD_ASSETNUM );
			if( assetnum != null && assetnum.length() > 0 )
			{
				if( selectedAssets.contains( assetnum ))
				{
					selectedAssets.remove( assetnum );
					if( multiLocMbo.toBeDeleted() )
					{
						multiLocMbo.undelete();
					}
				}
				continue;
			}

			String location    = multiLocMbo.getString( Constants.FIELD_LOCATION );
			if( location != null && location.length() > 0 )
			{
				if( selectedLocations.contains( location ))
				{
					selectedLocations.remove( location );
					if( multiLocMbo.toBeDeleted() )
					{
						multiLocMbo.undelete();
					}
				}
			}
		}
	}

	protected String formatInClause(
		Iterator<String> itr
	) {
        //System.out.println(">>> AssetLookupBase bean " + (Thread.currentThread().getStackTrace()[3].toString()) + " ==> " + ((new Object() {}).getClass().getEnclosingMethod().getName()) );
		StringBuffer inCluase = new StringBuffer();

		inCluase.append( " IN ( " );
		
		boolean first = true;
		while( itr.hasNext() )
		{
			if( !first )
			{
				inCluase.append( ", " );
			}
			inCluase.append( "'" );
			inCluase.append( itr.next() );
			inCluase.append( "'" );
			first = false;
		}
		if( first )
		{
			return "";		// Nothing to insert
		}
		inCluase.append( ")" );
		
		return inCluase.toString();
	}

}
