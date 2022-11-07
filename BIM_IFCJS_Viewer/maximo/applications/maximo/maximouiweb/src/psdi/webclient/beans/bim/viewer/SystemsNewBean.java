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
package psdi.webclient.beans.bim.viewer;

import java.rmi.RemoteException;
import java.util.Set;

import psdi.app.location.LocationRemote;
import psdi.app.location.LocationSetRemote;
import psdi.mbo.MboConstants;
import psdi.mbo.MboRemote;
import psdi.mbo.SqlFormat;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.webclient.beans.bim.Constants;
import psdi.webclient.beans.bim.SystemsBaseBean;

/**
 * @author Doug Wood	
 * Create a new system from the model location and the selection in the model view control 
 * 
 * Note:  I've tried this with various relationships to limit what is fetched. 
 * 		a) There is not much to filter on since location is non-persistent
 * 		b) Even just filtering on site loses the Mbo when an attachment is added
 */
public class SystemsNewBean extends SystemsBaseBean
{
	private boolean _initialized = false;
	private final int     _row = -1;
	@Override
    public void initialize() 
		throws MXException, RemoteException
	{
		if( _initialized )
		{
			moveTo( _row );
			select( _row );
			return;
		}
		
		try
		{
			setSetStartingLoc( false );
			insert();
			MboRemote sysMbo = getMbo();
			
			
			// Avoid ugly exception in the log file
			sysMbo.getThisMboSet().setLogLargFetchResultDisabled( true );
			setupStartingLocation();

			String siteId     = app.getDataBean().getString( Constants.FIELD_SITEID );
			sysMbo.setValue( Constants.FIELD_SITEID, siteId, MboConstants.NOACCESSCHECK );
			sysMbo.setValue( Constants.FIELD_LOCATION, getModelLocation(), MboConstants.NOACCESSCHECK );
			sysMbo.setValue( Constants.FIELD_NETWORK, false, MboConstants.NOACCESSCHECK );
		}
		catch( Throwable t )
		{
			t.printStackTrace();
		}

		_initialized = true;
	}
	
	@Override
	protected String getTableId()
	{
		return NOT_SUPPORTED;
	}

	@Override
	protected String getTreeId()
	{
		return NOT_SUPPORTED;
	}

	/* (non-Javadoc)
	 * @see psdi.webclient.system.beans.DataBean#execute()
	 */
	@Override
	public synchronized int execute() 
		throws MXException, RemoteException
	{
		MboRemote locSysMbo = getMbo();
		String siteId   = getString( Constants.FIELD_SITEID );
		String systemId = getString( Constants.FIELD_SYSTEMID );
		String parent   = getString( Constants.FIELD_LOCATION );
		save();
		
		MboRemote locMbo;
		try
		{
			SqlFormat sqlf = new SqlFormat( getMbo(), Constants.FIELD_LOCATION + "=:1 and siteid=:2");
			sqlf.setObject(1, Constants.TABLE_LOCATIONS, Constants.FIELD_LOCATION, parent );
			sqlf.setObject(2, Constants.TABLE_LOCATIONS, Constants.FIELD_SITEID, siteId ) ; 
			LocationSetRemote locSet = (LocationSetRemote)locSysMbo.getMboSet( "$getLocations", Constants.TABLE_LOCATIONS, sqlf.format());
			locSet.setHierarchy( "LOCATIONS", "", systemId );
			locMbo = locSet.getMbo(0);
			if( locMbo == null || !(locMbo instanceof LocationRemote ))
			{
				throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
			}
		}
		catch( Throwable t )
		{
			t.printStackTrace();
			return EVENT_HANDLED;
		}

		
		Set<String> selection = getViewer().getCurrentSelection();
		
		populateSystem( locSysMbo, (LocationRemote)locMbo, systemId, siteId, parent, selection );
		app.getDataBean().fireDataChangedEvent();
		app.getDataBean().fireChildChangedEvent();
		app.getDataBean().fireStructureChangedEvent();

		return EVENT_HANDLED;
	}
}
