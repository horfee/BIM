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
package psdi.webclient.beans.bim;

import psdi.app.bim.product.BIMProduct;
import psdi.util.MXException;
import psdi.webclient.system.beans.DataBean;

/**
 * @author Doug Wood	
 * Handles the configuration per site for BIM
 * 
 */
public class ProductBean extends DataBean
{
	public void clearClassification() 
		throws MXException
	{
		setValue( BIMProduct.FIELD_CLASSSTRUCTUREID, "" );
	}
}
