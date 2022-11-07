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


package psdi.webclient.beans.bim;

import java.rmi.RemoteException;

import psdi.app.assetcatalog.ClassStructureSetRemote;
import psdi.app.bim.product.BIMProduct;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.util.MXException;
import psdi.webclient.beans.assetcat.SearchClassificationBean;


public class ProductSearchClassificationBean extends SearchClassificationBean
{
	@Override
	public void initialize() throws MXException, RemoteException
	{
		super.initialize();
		ClassStructureSetRemote classStructSet=(ClassStructureSetRemote)getMboSet();
		classStructSet.setOriginatingObject( BIMProduct.TABLE_NAME );
	}

	@Override
	public MboSetRemote getResultSetForReturn() throws MXException,RemoteException
	{
		MboRemote selectedClassStructure=this.getSelectedClassStructure();
		MboSetRemote locSet=selectedClassStructure.getMboSet("PRODUCTS");	
		return locSet;
	}
	
	@Override
	public String getAttributeForReturn() throws MXException, RemoteException
	{
		return BIMProduct.FIELD_BIMPRODUCTBASEID;
	}
	
	@Override
	public String getResultObjectName() throws MXException, RemoteException
	{
		return BIMProduct.TABLE_NAME;
	}		
}
