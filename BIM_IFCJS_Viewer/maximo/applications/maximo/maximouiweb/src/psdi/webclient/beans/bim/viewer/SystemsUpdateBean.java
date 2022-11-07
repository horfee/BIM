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

import psdi.app.location.LocHierarchySetRemote;
import psdi.app.location.LocationRemote;
import psdi.app.location.LocationSetRemote;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.mbo.SqlFormat;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.webclient.beans.bim.Constants;
import psdi.webclient.beans.bim.SystemsBaseBean;
import psdi.webclient.components.BIMViewer;
import psdi.webclient.system.beans.DataBean;

public class   SystemsUpdateBean
       extends SystemsBaseBean
{
	private final static String QUERY_LOC_MODELID_LIST = 
		"location in ( select location from locancestor where ancestor =:1 and systemid " +
		"in (select systemid from locsystem where siteid =:2 and primarysystem = 1)) and ";

	private DataBean _tableBean = null;

	@Override
    public void initialize() 
	throws MXException, 
	       RemoteException
	{
		super.initialize();
		_tableBean = app.getDataBean( getTableId() );
		if( _tableBean == null )
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_PRESENTATION_CHANGED );	
		}
	}

	
	@Override
	protected String getTableId()
	{
		return Constants.CTRL_UPDATE_SYSTEMS_TBL;
	}

	@Override
	protected String getTreeId()
	{
		return Constants.CTRL_UPDATE_SYSTEMS_TREE;
	}
	
	/**
	 * Handles the replace button
	 * Deletes all node form the system and repopulates it with the model location as the
	 * root and items selected in the model as children
	 * @return
	 * @throws RemoteException
	 * @throws MXException
	 */
	public int replacesystem() 
		throws RemoteException, 
		       MXException
	{
		MboRemote systemMbo = _tableBean.getMbo();
		if( systemMbo == null )
		{
			return EVENT_HANDLED;
		} 
		boolean isPrimary = systemMbo.getBoolean( Constants.FIELD_PRIMARYSYSTEM );
		if( isPrimary )
		{
			throw new MXApplicationException(  Constants.BUNDLE_MSG, Constants.MSG_EDIT_PROMARY_SYSTEM );	
		}
		String systemId = systemMbo.getString( Constants.FIELD_SYSTEMID );
		String siteId   = systemMbo.getString( Constants.FIELD_SITEID );
		String parent   = systemMbo.getString( Constants.FIELD_LOCATION );
		
		MboRemote mbo = BIMViewer.lookupLocation( getMbo(), parent, siteId );
		if( mbo == null || !(mbo instanceof LocationRemote ))
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		
		deleteAllMember( systemMbo );
		
		mbo = BIMViewer.lookupLocation( getMbo(), parent, siteId );
		Set<String> selection = getViewer().getCurrentSelection();
		populateSystem( systemMbo, (LocationRemote)mbo, systemId, siteId, parent, selection );
		
		fireDataChangedEvent();
		getTree().fireDataChangedEvent();

		return EVENT_HANDLED;
	}

	/**
	 * Handles the append button.
	 * Adds all non duplicate items in the selection as children of the model location
	 * @return
	 * @throws RemoteException
	 * @throws MXException
	 */
	public int appendsystem() 
		throws RemoteException, 
		       MXException
	{
		MboRemote systemMbo = _tableBean.getMbo();
		if( systemMbo == null )
		{
			return EVENT_HANDLED;
		} 
		boolean isPrimary = systemMbo.getBoolean( Constants.FIELD_PRIMARYSYSTEM );
		if( isPrimary )
		{
			throw new MXApplicationException(  Constants.BUNDLE_MSG, Constants.MSG_EDIT_PROMARY_SYSTEM  );	
		}
		
		String systemId = systemMbo.getString( Constants.FIELD_SYSTEMID );
		String siteId   = systemMbo.getString( Constants.FIELD_SITEID );
		String parent   = getString( Constants.FIELD_LOCATION );
		
		HashSet<String> currentMembers = new HashSet<String>();
		MboSetRemote resultSet = lookupAllLocationsForSystem( systemId, siteId );
		for( int i = 0; resultSet != null && i < resultSet.count(); i++ )
		{
			String location = resultSet.getMbo(i).getString( Constants.FIELD_LOCATION );
			currentMembers.add( location );
		}
		
		Set<String> selection = getViewer().getCurrentSelection();
		LocationSetRemote selectedLocations = lookupLocationsFromSelection( getViewer(), selection );
		HashSet<String> updates = new HashSet<String>();
		
		// Find all locations in the selection set that are not currently in the system
		for( int i = 0; selectedLocations != null && i < selectedLocations.count(); i++ ) 
		{
			MboRemote locMbo = selectedLocations.getMbo( i );
			String location = locMbo.getString( Constants.FIELD_LOCATION );
			if( !currentMembers.contains( location ))
			{
				updates.add( location );
			}
		}
		
		// Try to use the selected item in the tree.  This allows a multi level tree to be built
		// If there is no selected item uses the location of the model if it is in the tree.
		MboRemote mbo = null;
		long uid = getTree().getHighlightedUID();
		if( uid >= 0 )
		{
			SqlFormat sqf = new SqlFormat( systemMbo, Constants.FIELD_LOCATIONSID + "=:1");
			sqf.setObject(1, Constants.TABLE_LOCATIONS, Constants.FIELD_LOCATIONSID, "" + uid );
			resultSet =  systemMbo.getMboSet("$getLocations", Constants.TABLE_LOCATIONS, sqf.format());
			mbo = resultSet.getMbo( 0 );
		}
		if( mbo == null || !(mbo instanceof LocationRemote ))
		{
			mbo = BIMViewer.lookupLocation( systemMbo, parent, siteId );
			if( mbo == null || !(mbo instanceof LocationRemote ))
			{
				throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
			}
			if( !currentMembers.contains( parent ))
			{
				throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_NO_ROOT );	
			}
		}
		else
		{
			parent = mbo.getString( Constants.FIELD_LOCATION );
		}
		
		LocHierarchySetRemote hierarchySet = (LocHierarchySetRemote)mbo.getMboSet( "LocHIerarchy" );
		if( hierarchySet == null )
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		
		int count = 0;
		Iterator<String> itr = updates.iterator();
		while( itr.hasNext() )
		{
			String location = itr.next();
			addNodeToSystem( hierarchySet, systemId, siteId, location, parent );
			count++;
		}
		
		if( count > 0 )
		{
			setChildrenFlag( systemMbo, mbo, systemId, siteId, parent );
		}
		
		fireDataChangedEvent();
		getTree().fireDataChangedEvent();

		return EVENT_HANDLED;
	}

	public int deletesystem()
		throws RemoteException, 
	           MXException
	{
		MboRemote systemMbo = _tableBean.getMbo();
		if( systemMbo == null )
		{
			return EVENT_HANDLED;
		} 
		boolean isPrimary = systemMbo.getBoolean( Constants.FIELD_PRIMARYSYSTEM );
		if( isPrimary )
		{
			throw new MXApplicationException(  Constants.BUNDLE_MSG, Constants.MSG_EDIT_PROMARY_SYSTEM  );	
		}
		String siteId   = systemMbo.getString( Constants.FIELD_SITEID );
		String systemId = systemMbo.getString( Constants.FIELD_SYSTEMID );
		MboSetRemote mboSet = _tableBean.getMboSet();
		mboSet.remove( systemMbo );
		
		deleteAllMember( systemMbo );
		
		SqlFormat sqf = new SqlFormat( systemMbo, "siteid=:1 and systemid=:2");
		sqf.setObject(1, Constants.TABLE_LOCSYSTEM, Constants.FIELD_SITEID,   siteId );
		sqf.setObject(2, Constants.TABLE_LOCSYSTEM, Constants.FIELD_SYSTEMID, systemId );
		MboSetRemote resultSet = systemMbo.getMboSet("$getLocsystem", Constants.TABLE_LOCSYSTEM, sqf.format());
		for( int i = 0; i < resultSet.count(); i++ )
		{
			String s = resultSet.getMbo(i).getString( Constants.FIELD_SYSTEMID );
			if( s.length() > 0 ) continue;
		}
		resultSet.deleteAll();
		resultSet.save();
		
		_tableBean.refreshTable();
		_tableBean.fireDataChangedEvent();
		_tableBean.fireStructureChangedEvent();
		return EVENT_HANDLED;
	}
	
	public LocationSetRemote lookupLocationsFromSelection(
		BIMViewer   model,
		Set<String> selection
	) 
		throws RemoteException, 
		       MXException 
	{
		MboRemote mbo = model.getDataBean().getMbo();
		String binding = model.getBinding();
		StringBuffer whereClause = new StringBuffer();
		whereClause.append( QUERY_LOC_MODELID_LIST );
		whereClause.append( binding );
		whereClause.append(" IN (" );
		for( int count = 1; count <= selection.size(); count++ )
		{
			whereClause.append( " :" );
			whereClause.append( count +2 );
			if( count < selection.size() )
			{
				whereClause.append( ", " );
			}
		}
		whereClause.append( " )" );

		SqlFormat sqf = new SqlFormat( mbo, whereClause.toString() );
		Iterator<String> itr = selection.iterator();
		int count = 3;
		sqf.setObject( 1, Constants.TABLE_LOCATIONS, Constants.FIELD_LOCATION, getModelLocation() );
		String siteId     = app.getDataBean().getString( Constants.FIELD_SITEID );
		sqf.setObject( 2, Constants.TABLE_LOCATIONS, Constants.FIELD_SITEID, siteId );
		while( itr.hasNext() )
		{
			sqf.setObject( count++, Constants.TABLE_LOCATIONS, binding, itr.next() );
		}
		MboSetRemote resultSet = mbo.getMboSet("$getLocations", Constants.TABLE_LOCATIONS, sqf.format());
		return (LocationSetRemote)resultSet;
	}
	
}
