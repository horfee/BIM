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
import java.util.HashSet;

import psdi.app.location.LocationService;
import psdi.app.location.LocationServiceRemote;
import psdi.app.location.LocationSetRemote;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.mbo.SqlFormat;
import psdi.security.UserInfo;
import psdi.server.MXServer;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.webclient.beans.common.TreeControlBean;
import psdi.webclient.components.BIMViewer;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.WebClientEvent;
import psdi.webclient.system.runtime.WebClientRuntime;


/**
 * @author Doug Wood
  * Custom bean to handle tree control for building model control
 */
public class SystemsTreeBean extends TreeControlBean
{
	private SystemsBaseBean       _parentBean        = null; 
	private DataBean              _tableBean         = null;
	private MboRemote             _systemMbo         = null;
	private LocationServiceRemote _lsr               = null;
	private LocationSetRemote     _locationSet       = null;
	private LocationSetRemote     _root              = null;
	private UserInfo              _userInfo          = null;
	private String                _systemId          = null;
	private String                _siteId            = null;
	private boolean               _recordSelected    = false;
	private long                  _highlightedUID    = -1;

	protected void initialize() throws MXException, RemoteException
	{	
		super.initialize();
		storeProperties();
		if( _parentBean == null )
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_PRESENTATION_CHANGED );	
		}
		_tableBean = app.getDataBean( _parentBean.getTableId() );
		if( _tableBean == null )
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_PRESENTATION_CHANGED );	
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see psdi.webclient.system.beans.DataBean#selectrecord()
	 */
	public int selectrecord() throws MXException, RemoteException
	{
		if( _parentBean == null || !(_parentBean instanceof SystemsDisplayBean ) )
		{
			return EVENT_HANDLED;
		}
		BIMViewer model = _parentBean.getViewer();
		if( model == null )		// Should never happen
		{
			return EVENT_HANDLED;
		}

		WebClientEvent event = clientSession.getCurrentEvent();
		String newuniqueidvalue = event.getValueString();
		try
		{				
			if( WebClientRuntime.isNull( uniqueidvalue ) )
			{
				return EVENT_HANDLED;
			}
			long num = NumberFormat.getIntegerInstance(clientSession.getUserInfo().getLocale()).parse(newuniqueidvalue).longValue();
			LocationServiceRemote lsr;
			lsr = (LocationServiceRemote) MXServer.getMXServer().lookup( "LOCATION" );
			MboRemote selectedMbo = ((LocationService)lsr).getLocation( _userInfo,"locationsid", "" + num );

			if( selectedMbo != null )
			{
				String modelId = selectedMbo.getString( model.getBinding() );
				HashSet<String> selectionSet  = new HashSet<String>();
				selectionSet.add( modelId );
				model.setMultiSelect( _parentBean.getModelLocation(), selectionSet );
				model.forceUpdate();
			}
		}
		catch(ParseException e)
		{
			return EVENT_HANDLED;
		}
		_recordSelected = true;
		clientSession.queueEvent(new WebClientEvent("dialogcancel", "BIM_DS","", clientSession));
		app.getDataBean().fireDataChangedEvent();
		return EVENT_HANDLED;
	} // end selectrecord

	public boolean getRecordSelected()
	{
		return _recordSelected;
	}
	

	/** 
	 *	Handles the selectnode event fired from the frontend or through TreeNode
	 *  It gets MBo for the uniqueid passed whena a node is selected
	 *  
	 *   @return EVENT_HANDLED;
	 */
	public int selectnode() throws MXException, RemoteException
	{	
		try
		{				
			if(!WebClientRuntime.isNull(uniqueidvalue))
			{
				long num = NumberFormat.getIntegerInstance(clientSession.getUserInfo().getLocale()).parse(this.uniqueidvalue).longValue(); 
				this.getMboForUniqueId(num);
				_highlightedUID = num;
			}
		}
		catch(ParseException e)
		{
			// Do Nothing
		}
		
		return EVENT_HANDLED;
	}
	
	public long getHighlightedUID()
	{
		return _highlightedUID;
	}

	public void setHierarchy(
	    String objectname,
	    String uniqueid,
	    String hierarchy
    ) 
		throws MXException, 
		       RemoteException
	{

		if( getMboSet() == null )
			return;

		super.setHierarchy(objectname, uniqueid, _systemId);
	}

	
 	protected MboSetRemote getMboSetRemote() 
	throws MXException, 
	       RemoteException
	{
		if( getobjectname() == null || getobjectname().equals("") )
		{
			storeProperties();
		}
		
		if( _tableBean == null && _parentBean != null )
		{
			_tableBean = app.getDataBean( _parentBean.getTableId() );
		}
		if( _tableBean == null )
		{
			return null;
		}
	
		MboRemote currentMbo = _tableBean.getMbo();
		if( currentMbo == null )
		{
			return null;
		}
	
		if( _systemMbo == null || _systemMbo != currentMbo )
		{
			_userInfo       = currentMbo.getUserInfo();
			_lsr         = (LocationServiceRemote) MXServer.getMXServer().lookup( "LOCATION" );
			_locationSet = (LocationSetRemote)_lsr.getMboSet( "LOCATIONS", _userInfo );
			_systemMbo      = currentMbo;
			String location = _systemMbo.getString( Constants.FIELD_LOCATION );
			_siteId         = _systemMbo.getString( Constants.FIELD_SITEID );
			_systemId       = _systemMbo.getString( Constants.FIELD_SYSTEMID );
			_root           = lookupLocationSet( location, _systemId, _siteId );
			fireDataChangedEvent();
			fireStructureChangedEvent();
		}
		return _root;
	}
	
	protected LocationSetRemote lookupLocationSet(
		String location,
		String systemId,
		String siteId
	) 
		throws RemoteException, MXException 
	{
		_locationSet.setHierarchy(objectname, "", _systemId );
		MboRemote loc = _locationSet.getMbo( 0 );
	
		SqlFormat sqf = new SqlFormat( _userInfo, "siteid=:1 and location in (select location from lochierarchy where siteid=:1 and systemid=:2 and parent is null)");
		sqf.setObject(1, Constants.TABLE_LOCATIONS, Constants.FIELD_SITEID,   siteId );
		sqf.setObject(2, Constants.TABLE_LOCATIONS, Constants.FIELD_SYSTEMID, systemId );
		LocationSetRemote resultSet = (LocationSetRemote)loc.getMboSet("$getLocations", Constants.TABLE_LOCATIONS, sqf.format());
		MboRemote curLoc = resultSet.getMbo(0);
		curLoc.setValue( Constants.FIELD_SYSTEMID, systemId );
		setuniqueidvalue( "" + curLoc.getUniqueIDValue() );
		resultSet.setHierarchy(objectname, "" + curLoc.getUniqueIDValue(), _systemId );
		
		return (LocationSetRemote)resultSet;
	}
	
	void setParent(
		SystemsBaseBean parent	
	) {
		_parentBean = parent;
	}
}