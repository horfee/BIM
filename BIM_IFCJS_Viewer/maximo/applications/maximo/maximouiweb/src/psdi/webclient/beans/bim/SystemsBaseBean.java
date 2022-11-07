/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * (C) COPYRIGHT IBM CORP. 2011,2018
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */
package psdi.webclient.beans.bim;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Set;

import psdi.app.location.LocHierarchySetRemote;
import psdi.app.location.LocationRemote;
import psdi.app.location.LocationServiceRemote;
import psdi.app.location.LocationSetRemote;
import psdi.mbo.MboConstants;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.mbo.SqlFormat;
import psdi.security.UserInfo;
import psdi.server.MXServer;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.webclient.components.BIMViewer;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ComponentInstance;
import psdi.webclient.system.controller.WebClientEvent;



/**
 * @author Doug Wood	
 * This bean is sued by all of the dialogs that manipulate systems.
 * It assumes that all implementing dialogs are launged from the model view control UI
 * so the BIMViewer control class can be extracted from the initiating event
 */
public abstract class SystemsBaseBean extends DataBean
{
	private   SystemsTreeBean       _treeBean       = null;
	private   BIMViewer             _viewerCtrl     = null;
	protected String                _modelLocation  = null;
	protected LocationServiceRemote _lsr            = null;
	protected UserInfo              _userInfo       = null;
	protected boolean               _setStartingLoc = true;

	
	protected final String NOT_SUPPORTED = "-- NOT SUPPORTED --";
	
	@Override
    public void initialize() 
		throws MXException, 
		       RemoteException
	{
		String treeId = getTreeId();
		if( treeId != null && treeId.length() > 0 )
		{
			if( !treeId.equalsIgnoreCase( NOT_SUPPORTED  ))
			{
				DataBean treeBean = app.getDataBean( treeId );
				if( treeBean != null && treeBean instanceof SystemsTreeBean )
				{
					_treeBean = (SystemsTreeBean)treeBean;
					_treeBean.setParent( this );
				}
				else
				{
					throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_PRESENTATION_CHANGED );	
				}
			}
		}
		else
		{
			// Implementing superclass is badly behaved
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		_lsr = (LocationServiceRemote) MXServer.getMXServer().lookup( "LOCATION" );
		setupStartingLocation();
	}
	
	protected LocHierarchySetRemote getLocHierarchySet() 
		throws RemoteException, MXException
	{
		return (LocHierarchySetRemote)_lsr.getMboSet( "LOCHIERARCHY", _userInfo );
	}
	
	/**
	 * The starting location is passed from the .jsp on the event that initiatiates the dialog
	 * @throws MXException 
	 * @throws RemoteException 
	 */
	protected String setupStartingLocation() 
	    throws RemoteException, 
	           MXException 
	{
		WebClientEvent event = clientSession.getCurrentEvent();
		ComponentInstance ci = event.getSourceComponentInstance();
		if( ci != null && ci instanceof BIMViewer )
		{
			_viewerCtrl = (BIMViewer)ci;
		}
		else
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		
		Object o = event.getValue();
		if( o == null || !(o instanceof String ))  
		{
			// Should never happen unless the .jsp is altered
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		String result[] = ((String)o).split( ";" );
		if( result.length < 1 )
		{
			// Should never happen unless the .jsp is altered
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		_modelLocation = result[0];
		
		String location;
		// The length should always be 2, except for the test viewer that allows the system dialogs to be displayed
		// without a real viewer installed.
		if( result.length > 1 )
		{
			location = result[1];
		}
		else
		{
			location = result[1];
		}
		MboRemote locMbo = _viewerCtrl.lookupLocationFromModelId( _modelLocation, location  );
		if( locMbo == null  )
		{
			 throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_NOT_IMPORTED );	
		}
		
		if( _setStartingLoc )
		{
			long uid = locMbo.getUniqueIDValue();    
			getMboForUniqueId( uid );	
		}
		
		_userInfo = locMbo.getUserInfo();
		
		return location;
	}
	
	protected abstract String getTableId();
	protected abstract String getTreeId();
	public BIMViewer          getViewer() { return _viewerCtrl; }
	public String             getModelLocation() { return _modelLocation; }
	public SystemsTreeBean    getTree()   { return _treeBean; }
	

	/**
	 * Queries the LOCHIERARCHY table for all entries associated with a system
	 * @param systemId	System ID of target system
	 * @param siteId	Site ID of target system
	 * @return MboSetRemore with All entries associated with the system
	 * @throws RemoteException
	 * @throws MXException
	 */
	public MboSetRemote lookupAllLocationsForSystem(
		String systemId,
		String siteId
	) 
		throws RemoteException, 
		       MXException 
	{
		try
		{
			SqlFormat sqlf = new SqlFormat( getMbo(), Constants.FIELD_SYSTEMID + "=:1 and siteid=:2");
			sqlf.setObject(1, Constants.TABLE_LOCHIERARCHY, Constants.FIELD_SYSTEMID, systemId );
			sqlf.setObject(2, Constants.TABLE_LOCHIERARCHY, Constants.FIELD_SITEID, siteId ) ; 
			return getMbo().getMboSet("$getLocHierarchy", Constants.TABLE_LOCHIERARCHY, sqlf.format());
		}
		catch( Exception e )
		{
			return null;
		}
	}
	
	/**
	 * Populates a new (empty) system with a 2 level hierarchy.
	 * @param owningLocation
	 * @param systemId	System ID of target system
	 * @param siteId	Site ID of target system
	 * @param parent
	 * @param selection A set of items to add to the system.  It is assumed though not required
	 *                  that this set was geneated from a selection in the model viewer.  It is 
	 *                  possible that the sets contain items that have not been imported into
	 *                  Maximo
	 * @throws RemoteException
	 * @throws MXException
	 */
	protected void populateSystem(
		MboRemote      locSysMbo,
		LocationRemote owningLocation,
		String         systemId,
		String         siteId,
		String         parent,
		Set<String>    selection
	) 
		throws RemoteException, 
		       MXException 
	{
		LocHierarchySetRemote hierarchySet;
		SqlFormat sqlf = new SqlFormat( getMbo(), Constants.FIELD_LOCATION + "=:1 and siteid=:2");
		sqlf.setObject(1, Constants.TABLE_LOCATIONS, Constants.FIELD_LOCATION, parent );
		sqlf.setObject(2, Constants.TABLE_LOCATIONS, Constants.FIELD_SITEID, siteId ) ; 
		LocationSetRemote locSet = (LocationSetRemote)locSysMbo.getMboSet( "$getLocations", Constants.TABLE_LOCATIONS, sqlf.format());
		locSet.setHierarchy( "LOCATIONS", "", systemId );
		MboRemote locMbo = locSet.getMbo( 0 );
		
		if( locMbo == null )
		{
			// TODO Better message here?
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}

		locMbo.setValue( Constants.FIELD_SYSTEMID, systemId );
		hierarchySet = (LocHierarchySetRemote)locMbo.getMboSet( LocationSetRemote.LOCHIERARCHY );
		if( hierarchySet == null )
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		hierarchySet.add();
		hierarchySet.save();

		hierarchySet = (LocHierarchySetRemote)locMbo.getMboSet( LocationSetRemote.CHILDREN );
		int count = addSelectionToSystem( hierarchySet, systemId, siteId, parent, selection );
		if( count > 0 )
		{
			setChildrenFlag( locSysMbo, locMbo, systemId, siteId, parent );
		}
	}
	
	/**
	 * Add a set of locations specifed by the value bound to the control to the system with
	 * the specified parent.
	 * @param hierarchySet
	 * @param systemId	System ID of target system
	 * @param siteId	Site ID of target system
	 * @param rootLocation
	 * @param selection A set of items to add to the system.  It is assumed though not required
	 *                  that this set was geneated from a selection in the model viewer.  It is 
	 *                  possible that the sets contain items that have not been imported into
	 *                  Maximo
	 * @return The number of items added to the system.  
	 * @throws RemoteException
	 * @throws MXException
	 */
	protected int addSelectionToSystem(
		LocHierarchySetRemote hierarchySet,
		String                systemId,
		String                siteId,
		String                rootLocation,
		Set<String>           selection
	) 
		throws RemoteException, 
		       MXException
	{
		Iterator<String> itr = selection.iterator();
		
		int count = 0;
		while( itr.hasNext() )
        {
			String value = itr.next();
			MboRemote locationMbo = getViewer().lookupLocationFromModelId( _modelLocation, value );
			if( locationMbo == null )
			{
				continue;
			}
			hierarchySet.add();
			String location = locationMbo.getString( Constants.FIELD_LOCATION );
			MboRemote member = hierarchySet.getMbo();
			member.setValue( Constants.FIELD_LOCATION, location );
			hierarchySet.save();
			count++;
        }
		return count;
	}
	
	/**
	 * Add location as a member the the specified system with the specified parent
	 * @param hierarchySet
	 * @param systemId	System ID of target system
	 * @param siteId	Site ID of target system
	 * @param location	Location
	 * @param parent	Parent location (may be null)
	 * @return			Mbo for the new LocHierarchy record
	 * @throws RemoteException
	 * @throws MXException
	 */
	protected MboRemote addNodeToSystem(
		MboSetRemote hierarchySet,
		String       systemId,
		String       siteId,
		String       location,
		String       parent
	) 
		throws RemoteException, 
		       MXException 
	{
		hierarchySet.add();
		MboRemote member = hierarchySet.getMbo();
		member.setValue( Constants.FIELD_SITEID,   siteId );
		member.setValue( Constants.FIELD_SYSTEMID, systemId);
		member.setValue( Constants.FIELD_LOCATION, location,  MboConstants.NOACCESSCHECK );
		if( parent != null && parent.length() > 0 )
		{
			member.setValue( Constants.FIELD_PARENT, parent  );
		}
		hierarchySet.save();
		return member;
	}
	
	protected void deleteAllMember(
		MboRemote systemMbo
	) 
		throws RemoteException, 
		       MXException 
	{
		String siteId   = systemMbo.getString( Constants.FIELD_SITEID );
		String systemId = systemMbo.getString( Constants.FIELD_SYSTEMID );
		
		SqlFormat sqf = new SqlFormat( _userInfo, "siteid=:1 and systemid=:2");
		sqf.setObject(1, Constants.TABLE_LOCHIERARCHY, Constants.FIELD_SITEID,   siteId );
		sqf.setObject(2, Constants.TABLE_LOCHIERARCHY, Constants.FIELD_SYSTEMID, systemId );
		LocHierarchySetRemote hierarchySet; 
		hierarchySet = (LocHierarchySetRemote)systemMbo.getMboSet( "$getLocHierarchy", Constants.TABLE_LOCHIERARCHY, 
		                                                           sqf.format());
		hierarchySet.deleteAll( MboConstants.NOACCESSCHECK );
		hierarchySet.save();
		hierarchySet.clear();
	
		sqf = new SqlFormat( systemMbo, "siteid=:1 and systemid=:2");
		sqf.setObject(1, Constants.TABLE_LOCANCESTOR, Constants.FIELD_SITEID,   siteId );
		sqf.setObject(2, Constants.TABLE_LOCANCESTOR, Constants.FIELD_SYSTEMID, systemId );
		MboSetRemote resultSet = systemMbo.getMboSet("$getLocancestor", Constants.TABLE_LOCANCESTOR, sqf.format());
		
		resultSet.deleteAll( MboConstants.NOACCESSCHECK );
		resultSet.save();
		resultSet.clear();
	}
	
	public void setSetStartingLoc(
		boolean setStartingLoc
	) {
		_setStartingLoc = setStartingLoc;
	}
	
	/**
	 * Set the hasChildren flag to true on the location specified by parent
	 * @param locationMbo
	 * @param systemId	System ID of target system
	 * @param siteId	Site ID of target system
	 * @param parent
	 * @throws RemoteException
	 * @throws MXException
	 */
	protected void setChildrenFlag(
		MboRemote locSysMbo, 
		MboRemote locationMbo,
		String    systemId,
		String    siteId,
		String    parent
	) 
		throws RemoteException, 
		       MXException 
	{
		SqlFormat sqlf = new SqlFormat(locSysMbo, "systemid =:1 and siteid=:2 and location =:3");
		sqlf.setObject(1, Constants.TABLE_LOCHIERARCHY, Constants.FIELD_SYSTEMID, systemId ) ; 
		sqlf.setObject(2, Constants.TABLE_LOCHIERARCHY, Constants.FIELD_SITEID, siteId ) ; 
		sqlf.setObject(3, Constants.TABLE_LOCHIERARCHY, Constants.FIELD_LOCATION, parent );
		LocHierarchySetRemote hierarchySet;
		hierarchySet = (LocHierarchySetRemote)locationMbo.getMboSet( "$getLocHierarchy", 
		                                                             Constants.TABLE_LOCHIERARCHY, 
		                                                             sqlf.format());
		MboRemote root = hierarchySet.getMbo( 0 );
		if( root != null )
		{
			root.setValue( Constants.FIELD_CHILDREN, true, MboConstants.NOACCESSCHECK );
			hierarchySet.save();
		}
	}
}
