/*
 *
 * IBM Confidential
 *
 * 5724-U18, 5737-M66
 * 
 * (C) COPYRIGHT IBM CORPORATION 2001,2021
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 */
package psdi.app.bim.viewer;

import java.rmi.RemoteException;
import java.util.Vector;

import psdi.mbo.Mbo;
import psdi.mbo.MboServerInterface;
import psdi.mbo.MboSet;
import psdi.util.MXException;

public class BuildingModelSet extends MboSet implements BuildingModelSetRemote
{

    /**
     * Construct the set
     */
    public BuildingModelSet(
    	MboServerInterface ms
	) 
    	throws MXException, RemoteException
    {
        super(ms);
    }


	@Override
    /**
     * Generate a new operating location object
     *
     * @param        ms  mboset
     * @return       Mbo object
     */
    protected Mbo getMboInstance(
    	MboSet ms
	) 
    	throws MXException, RemoteException
    {
        return new BuildingModel(ms);
    }

	@Override
    public String getVieweerTypeList(
		String siteId,
		String orgId
	) 
		throws RemoteException, 
		       MXException
	{
		Vector values = getTranslator().getValuesVector( BuildingModel.DOMAIN_BIMVIEWERTYPE, siteId, orgId);
//		String viewer;
//		if ( getMbo().isNull(BuildingModel.FIELD_VIEWERTYPE)) {
//			MXServer server = MXServer.getMXServer();
//			viewer = server.getProperty( BIMService.PROP_NAME_ACTIVE_VIEWER );
//		} else {
//			viewer = ((BuildingModelRemote)getMbo()).getViewerType();
//		}
		//values = getTranslator().getExternalValues( BuildingModel.DOMAIN_BIMVIEWERTYPE, viewer, siteId, orgId );	
		
		
		StringBuffer list = new StringBuffer();
		for(Object value: values) {
			list.append("'");
			list.append(((String[])value)[1]);
			list.append("',");
		}
		return list.substring(0, list.length() - 1);
//		for( int i = 0; values != null && i < values.length; i++ )
//		{
//			list.append( "'" );
//			list.append( values[i] );
//			list.append( "'" );
//			if( i + 1 < values.length )
//			{
//				list.append(  "," );
//			}
//		}
//		
//		return list.toString();
	}
}
