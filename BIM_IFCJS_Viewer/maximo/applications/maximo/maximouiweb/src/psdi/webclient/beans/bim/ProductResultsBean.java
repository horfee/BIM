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
 *
 * MRO Software Confidential
 *
 * (C) COPYRIGHT MRO Software, Inc. 2001-2006
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 */


/*
 * Created on Jul 16, 2004
 *
*/
package psdi.webclient.beans.bim;

import java.rmi.RemoteException;

import psdi.app.bim.product.BIMProduct;
import psdi.mbo.MboSetRemote;
import psdi.util.MXException;
import psdi.webclient.system.beans.ResultsBean;

/**
 * Results Bean for Item List Table
 * 
 * @author Doug Wood
 *
 */
public class ProductResultsBean extends ResultsBean
{
	
	/**
	 * set the MboSet to include ONLY ItemTypes of "ITEM"
	 */
	protected MboSetRemote getMboSetRemote() throws MXException, RemoteException
	{
		MboSetRemote mboSetRemote = super.getMboSetRemote();
		mboSetRemote.setRelationship( BIMProduct.FIELD_DESIGNSPEC + " = 0" );
		mboSetRemote.reset();
		return mboSetRemote;
	}
}
