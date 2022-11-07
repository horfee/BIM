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
package psdi.webclient.beans.bim.viewer;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.mbo.SqlFormat;
import psdi.security.UserInfo;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.webclient.beans.bim.Constants;
import psdi.webclient.components.BIMViewer;
import psdi.webclient.system.beans.DataBean;

public class AssetLookupMulti  extends AssetLookupBase
{
	MboRemote _appMbo;

	public void initialize() throws MXException, RemoteException
	{
		super.initialize();
		
		DataBean appBean = app.getDataBean();
		_appMbo = appBean.getMbo();
		String siteId = null;
		if( appBean.isAttribute( Constants.FIELD_SITEID ))
		{
			siteId = appBean.getString( Constants.FIELD_SITEID );
		}
		else
		{
			UserInfo userInfo = _appMbo.getUserInfo();
			siteId = userInfo.getInsertSite();
		}
		setQbe( Constants.FIELD_SITEID, siteId );
		
		MboRemote mboLocation = null;
		if( appBean.isAttribute( Constants.FIELD_LOCATION ))
		{
			String location = appBean.getString( Constants.FIELD_LOCATION );
			if( location != null && location.length() > 0 && siteId != null )
			{
				mboLocation = BIMViewer.lookupLocation( _appMbo, location, siteId );
			}
		}
		if( mboLocation == null )
		{
			mboLocation = BIMViewer.lookupLocation( _appMbo, siteId, siteId );
		}
		if( mboLocation != null )
		{
        	long uid = mboLocation.getUniqueIDValue();    
			getMboForUniqueId( uid );
		}
	}
	

	public synchronized int execute() 
		throws MXException, RemoteException
	{
		
		Set<String> values = getValueList();
		BIMViewer   model  = getModel(); 

		super.execute();

		String siteId = getString( Constants.FIELD_SITEID );
		
		DataBean multiLocDataBean = getMultiLocCITable( _appMbo );
		if( multiLocDataBean == null )
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		
//    	Set<String> existinAssets = new HashSet<String>(); 
//    	lookupExistingAssetsAndLocatins( assetsDataBean, existinAssets, null );
		HashSet<String> selectedAssets = new HashSet<String>();
		
		if( _appMbo.isBasedOn("WORKORDER") || _appMbo.isBasedOn("TICKET") )
        {
			Iterator<String> itr = values.iterator();

			while( itr.hasNext() )
            {
				String value = itr.next();
				MboRemote locationMbo = model.lookupLocationFromModelId( value );
				
				// The correct way to configure the data import is with "Operating locations"
				// which means that every asset in the model also has a location 1 to 1.  Find the 
				// location then get the asset with that location as its parent.  This also supports
				// Moving asset better.  
				if( locationMbo != null )
				{
					MboSetRemote resultSet = BIMViewer.lookupAssetsAtLocation( locationMbo );
	        		if( resultSet == null || resultSet.isEmpty() )
	        		{
	        			continue;
	        		}

	        		addAssets( resultSet, selectedAssets );
				}
				// If the above fails, try and just find an asset that matches. this 
				// provide some functionality when "operating locations" are not used
				else
				{
					// If the locations data attribute is location then a typical
					// configuration would have the asset data attribute as assentnum.  This
					// may be the case if the AutoDesk provided data import is used
					// If not, assume that both location and asset uses a custom field
					// for the item's ID in the model The name should be the same for
					// both asset and locations.  This should be the case if the IBM 
					// provide data import is used
					String binding = "assetnum";
					if( !getBinding().equalsIgnoreCase( Constants.FIELD_LOCATION ))
					{
						binding = getBinding();
					}
					SqlFormat sqlf = new SqlFormat( _appMbo, binding + "=:1 and siteid=:2");
	        		sqlf.setObject(1, "ASSET", binding, value );
	        		sqlf.setObject(2, "ASSET", Constants.FIELD_SITEID, siteId ) ; 
	        		MboSetRemote resultSet;
	        		resultSet = _appMbo.getMboSet("$getAsset", "ASSET", sqlf.format());
	        		addAssets( resultSet, selectedAssets );
				}
            }
			
	    	filterAndUndelete( multiLocDataBean, siteId, selectedAssets, null );

    		insertAssets( _appMbo, selectedAssets, siteId );

        	multiLocDataBean.fireStructureChangedEvent();
        	multiLocDataBean.refreshTable();
        }
		 
		return EVENT_HANDLED;
	}
	
	protected void addAssets(
		MboSetRemote resultSet,
		Set<String>  selectedAssets
	) 
		throws RemoteException, 
		       MXException 
	{
		for( int i = 0; i < resultSet.count(); i++ )
		{
			MboRemote assetMbo = resultSet.getMbo( i );
			String assetnum    = assetMbo.getString( Constants.FIELD_ASSETNUM );
			if( assetnum == null || assetnum.length() == 0 )
			{
				continue;
			}
			selectedAssets.add( assetnum );
		}
	}
}
