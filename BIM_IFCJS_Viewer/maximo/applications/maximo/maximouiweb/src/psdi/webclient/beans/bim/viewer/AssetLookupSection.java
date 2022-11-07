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

import psdi.util.MXException;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.beans.WebClientBean;

public class   AssetLookupSection
       extends DataBean
{
	public int bimviewer() 
	{
		DataBean parentBean = getParent();
		if( parentBean instanceof AssetLookupBase )
		{
			return ((AssetLookupBase)parentBean).bimviewer();
		}
		return WebClientBean.EVENT_HANDLED;
	}
	
	public int bimModelListChanged()
		throws RemoteException, 
		       MXException
	{
		DataBean parentBean = getParent();
		if( parentBean instanceof AssetLookupBase )
		{
			return ((AssetLookupBase)parentBean).bimModelListChanged();
		}
		return WebClientBean.EVENT_HANDLED;
	}
	
	public int eventMultiSelect()
	{
		DataBean parentBean = getParent();
		if( parentBean instanceof AssetLookupBase )
		{
			return ((AssetLookupBase)parentBean).eventMultiSelect();
		}
		return WebClientBean.EVENT_HANDLED;
	}
	
	public int appendSelection()
	{
		DataBean parentBean = getParent();
		if( parentBean instanceof AssetLookupBase )
		{
			return ((AssetLookupBase)parentBean).appendSelection();
		}
		return WebClientBean.EVENT_HANDLED;
	}
}
