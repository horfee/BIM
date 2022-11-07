/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * (C) COPYRIGHT IBM CORP. 2011, 2012
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */
package psdi.webclient.beans.bim.viewer;

import java.rmi.RemoteException;
import java.util.Set;

import psdi.app.common.virtual.DrillDownSetRemote;
import psdi.app.location.LocationRemote;
import psdi.mbo.Mbo;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.security.UserInfo;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.webclient.beans.bim.Constants;
import psdi.webclient.components.BIMViewer;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.beans.QbeBean;
import psdi.webclient.system.controller.ControlInstance;

public class AssetLookup extends AssetLookupBase 
{
	private ControlInstance _sourceControl    = null;
	private String          _srcDataAttribute = "location";
	private String          _dataSrc          = null;
	private int             _pageType         = DrillDownSetRemote.DRILLDOWN_RECORD;

	@Override
    protected void initialize() 
		throws MXException, 
		       RemoteException
	{
		super.initialize();
		String inputmode = "";
		String lookuptype = "locations";
		
		_sourceControl = clientSession.getCurrentEvent().getSourceControlInstance();
		MboRemote mbo = getMbo();
		if( _sourceControl != null )
		{			
			_dataSrc = _sourceControl.getProperty( "datasrc" );
			DataBean originatingBean = clientSession.getDataBean( _dataSrc );
			_srcDataAttribute = _sourceControl.getProperty("dataattribute");
			if( _srcDataAttribute == null || _srcDataAttribute.equals("") )
			{
				_srcDataAttribute= "location";
			}
			
			lookuptype = _sourceControl.getProperty("lookup");
			if(lookuptype == null || lookuptype.equals(""))
			{
				lookuptype= "location";
			}
			
			inputmode = _sourceControl.getProperty("inputmode");
			if(inputmode == null || inputmode.equals(""))
			{
				inputmode= "location";
			}
			if( mbo == null ) mbo = _sourceControl.getDataBean().getMbo();
			if( mbo == null )
			{
				mbo = originatingBean.getMbo();
			}

			
			if(    originatingBean instanceof QbeBean 
				|| (inputmode != null && inputmode.equalsIgnoreCase("query")))
			{
				_pageType = DrillDownSetRemote.DRILLDOWN_QBE;
			}
			else if( inputmode != null && inputmode.equalsIgnoreCase("default") )
			{
				_pageType = DrillDownSetRemote.DRILLDOWN_DEFAULTTABLE;
			}
			else if( app.onListTab() )
			{
				_pageType=DrillDownSetRemote.DRILLDOWN_LISTTAB;
				if( app.getResultsBean() == null )			// Will be null for the simple power app
				{
					//For simple power app set pagetype to Drilldown_Record
					_pageType = DrillDownSetRemote.DRILLDOWN_RECORD;
				}
			}
		}
		
		// If there is no asset in the lookup field, then start with no location value so
		// The viewer doesn't automatically load a model that might no be needed and force 
		// the user to wait while it loads.
		MboRemote mboLocation = null;
		String location       = null;
		String siteId         = null;
		if( _dataSrc != null )
		{
			DataBean sourceDataBean = clientSession.getDataBean( _dataSrc );
			if( sourceDataBean.isAttribute( BIMViewer.FIELD_SITEID ))
			{
				siteId = sourceDataBean.getString( BIMViewer.FIELD_SITEID );
			}
			if( siteId == null || siteId.length() == 0 )
			{
				UserInfo userInfo = mbo.getUserInfo();
				siteId = userInfo.getInsertSite();
			}
			if( sourceDataBean.isAttribute( Constants.FIELD_LOCATION ))
			{
				location = sourceDataBean.getString( Constants.FIELD_LOCATION );
				if( location != null && location.length() > 0 && siteId != null )
				{
					mboLocation = BIMViewer.lookupLocation( mbo, location, siteId );
				}
			}
		}
		
		setValue(  BIMViewer.FIELD_SITEID, siteId, Mbo.NOACCESSCHECK );
		if( location == null || location.length() == 0 )
		{
			setValue(  BIMViewer.FIELD_LOCATION, "", Mbo.NOACCESSCHECK );
			location = "- NO LOCATION -";
		}
		if( mboLocation != null )
		{
        	long uid = mboLocation.getUniqueIDValue();    
			getMboForUniqueId( uid );
		}
	}
	
	@Override
    public int execute() throws MXException, RemoteException
	{
		BIMViewer viewer = getModel();
		Set<String> selection = viewer.getCurrentSelection();
		if( selection.size() > 1 )
		{
			 throw new MXApplicationException( Constants.BUNDLE_MSG, Messages.MSG_TOO_MANY_SELECTED );	
		}

		DataBean sourceDataBean = clientSession.getDataBean( _dataSrc );

		MboRemote locationMbo = getMbo();
		if( locationMbo == null )
		{
			 throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_NOT_IMPORTED );	
		}
		
		MboRemote selectedMbo = viewer.getDataBean().getMbo();
		if( selectedMbo == null )
		{
			 throw new MXApplicationException( Constants.BUNDLE_MSG, Messages.MSG_NO_SELECTION );	
		}
		if( selectedMbo instanceof LocationRemote )
		{
			MboSetRemote mboSet = BIMViewer.lookupAssetsAtLocation( selectedMbo );
			if( mboSet != null )
			{
				selectedMbo = mboSet.getMbo( 0 );
			    if( selectedMbo == null )
			    {
					 throw new MXApplicationException(  Constants.BUNDLE_MSG, Messages.MSG_NO_ASSET );	
			    }
			}
		}
		
		if( sourceDataBean.isAttribute( Constants.FIELD_ASSETNUM ))
		{ 
			String assetValue = selectedMbo.getString(  Constants.FIELD_ASSETNUM );
			setReturnValue( sourceDataBean, Constants.FIELD_ASSETNUM, assetValue );
		}
		return EVENT_HANDLED;
	}
	
	public void setReturnValue(
		DataBean sourceDataBean,
		String   field,
		String   value
	) 
		throws MXException 
	{
		if(	_pageType == DrillDownSetRemote.DRILLDOWN_QBE )
		{
			sourceDataBean.setQbe( field, value );
		}
		else if( _pageType == DrillDownSetRemote.DRILLDOWN_DEFAULTTABLE )
		{
			sourceDataBean.setDefaultValue( field, value );
		}
		else
		{
			sourceDataBean.setValue( field, value );
		}
	}
}