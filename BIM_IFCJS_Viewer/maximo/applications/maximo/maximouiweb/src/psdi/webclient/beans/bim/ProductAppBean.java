/*
 *
 * IBM Confidential
 *
 * (C) COPYRIGHT IBM CORPORATION 2001-2011,2018
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 */
package psdi.webclient.beans.bim;

import java.rmi.RemoteException;

import psdi.app.bim.product.BIMProduct;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.util.MXException;
import psdi.webclient.system.beans.AppBean;

public class ProductAppBean
    extends
        AppBean
{
	public ProductAppBean()
	{
		super();
	}
	
    public synchronized void setValue(
	    int nRow,
	    String attribute,
	    String value,
	    long accessModifier
    ) 
    	throws MXException
	{
		try
		{
			MboRemote mbo = mboSetRemote.getMbo(nRow);
			if( mbo.getMboValueData(attribute).getData().compareTo(value) == 0 )
			{
				return;
			}
			// Call parent to continue with the setValue
			super.setValue(nRow, attribute, value, accessModifier);
		}
		catch( RemoteException e )
		{
			handleRemoteException(e);
		}
	}
    
	@Override
	public synchronized void insert() 
		throws MXException, 
		       RemoteException
	{
		super.insert();
	}
	
	public void clearClassification() 
		throws MXException
	{
		setValue( BIMProduct.FIELD_CLASSSTRUCTUREID, "" );
	}
	
	/**
	 * set the MboSet to include ONLY ItemTypes of "ITEM"
	 */
	@Override
	protected MboSetRemote getMboSetRemote() throws MXException, RemoteException
	{
		MboSetRemote mboSetRemote = super.getMboSetRemote();
		mboSetRemote.setRelationship( BIMProduct.FIELD_DESIGNSPEC + " = 0" );
		mboSetRemote.reset();
		return mboSetRemote;
	}

}
