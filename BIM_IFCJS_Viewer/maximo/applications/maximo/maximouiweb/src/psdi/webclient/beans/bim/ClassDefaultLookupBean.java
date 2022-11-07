/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * 5724-R46
 *
 * (C) COPYRIGHT IBM CORP. 2011,2018
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */


package psdi.webclient.beans.bim;

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Vector;

import psdi.app.assetcatalog.AssetCatalogServiceRemote;
import psdi.app.assetcatalog.ClassStructureRemote;
import psdi.app.assetcatalog.ClassStructureSetRemote;
import psdi.app.assetcatalog.FldClassStructureId;
import psdi.mbo.Mbo;
import psdi.mbo.MboConstants;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.mbo.MboValue;
import psdi.mbo.MboValueListener;
import psdi.util.MXException;
import psdi.util.MXSession;
import psdi.webclient.beans.common.TreeControlBean;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ComponentInstance;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.controller.WebClientEvent;



public class ClassDefaultLookupBean extends TreeControlBean
{
	private DataBean originalBean = null;
	/**
	 * initialize - get the bean where this tree control bean is launched
	 * TreeControlBean is ClassStructure object
	 */
	private ClassStructureSetRemote classStructSet = null;
	private boolean isfrominitialize               = false;

	public void initialize() throws MXException, RemoteException
	{
		isfrominitialize=true;
		super.initialize();
		ControlInstance originalControl = creatingEvent.getSourceControlInstance();
		originalBean = clientSession.getDataBean(originalControl.getProperty("datasrc"));
		MboRemote originalMbo = originalBean.getMbo();
		classStructSet=(ClassStructureSetRemote)getMboSet();
		
		ComponentInstance compInst=creatingEvent.getSourceComponentInstance();
		String originalAttr=compInst.getProperty("dataattribute");
		String classAttr;
		
		int index = originalAttr.indexOf(".");
		if( index < 0 )
		{
			classAttr = originalAttr;  
		}
		else
		{
			classAttr = originalAttr.substring(0, index)  + "IFICATIONID";  
		}
		if( originalMbo instanceof Mbo )
		{
			Mbo m = (Mbo)originalMbo;
			MboValue mv = m.getMboValue( classAttr );
			Vector<MboValueListener> mvls = mv.getListeners();
			Enumeration<MboValueListener> e = mvls.elements();
			while( e.hasMoreElements() )
			{
				MboValueListener mvl = e.nextElement();
				if( mvl instanceof  FldClassStructureId )
				{
					classStructSet.setOriginatingObject( ((FldClassStructureId)mvl).getObjectName() );
				}
			}
		}
	}

	/** 
	 *	Handles the selectnode event fired from the frontend or through TreeNode
	 *  It gets MBo for the uniqueid passed whena a node is selected
	 *  
	 *   @return EVENT_HANDLED;
	 */
	public int selectnode() throws MXException, RemoteException
	{
		super.selectnode();
		WebClientEvent event = clientSession.getCurrentEvent();
		try
		{
			updateOriginatingRecord();
		}
		catch(MXException e)
		{
			clientSession.queueEvent(new WebClientEvent("dialogclose", app.getCurrentPageId(), null, clientSession));
			clientSession.showMessageBox( event, e );
		}
		catch(RemoteException m)
		{
			clientSession.queueEvent(new WebClientEvent("dialogclose", app.getCurrentPageId(), null, clientSession));
			clientSession.showMessageBox( event, m );
		}
		return EVENT_HANDLED;
	}

	/** 
	 * select a classstructure node from the tree
	 * @throws RemoteException 
	 */
	public int selectrecord() 
		throws MXException, RemoteException
	{
		return super.selectrecord();
	}
	
	/**
	 * update the classStructureid of the originating object with the selected
	 * classstructure node and close the dialog
	 * 
	 * @throws MXException
	 * @throws RemoteException
	 */
	protected void updateOriginatingRecord() 
		throws MXException,
		       RemoteException
	{
		//this is the classstructure
		WebClientEvent event = clientSession.getCurrentEvent();
		String uniqueIdSelected = event.getValueString();
		
		String originalAttr=null;
		MboSetRemote originalSet=null;
		
		ComponentInstance compInst=creatingEvent.getSourceComponentInstance();
		originalAttr=compInst.getProperty("dataattribute");
		originalSet=originalBean.getMboSet();

		MboRemote selectedClassMbo = getMbo();
		if( selectedClassMbo == null )
		{
			MXSession mxs = this.getMXSession();
			AssetCatalogServiceRemote assetCatService = (AssetCatalogServiceRemote) mxs.lookup("ASSETCATALOG");
			selectedClassMbo = assetCatService.getClassStructure(mxs.getUserInfo(), uniqueIdSelected);
		}

		if( selectedClassMbo.getThisMboSet().count() == 1 && classStructSet.hasAFakeTreeNode() )
		{
			String objectName = originalSet.getName().toUpperCase();
			MboRemote useWith = ((ClassStructureRemote)selectedClassMbo).getUseWith(objectName);
			if( useWith == null )
			{
				clientSession.queueEvent(new WebClientEvent("dialogclose", app.getCurrentPageId(), null, clientSession));
				return;
			}
		}

		MboRemote originalRecord=originalSet.getMbo();
		if (originalRecord == null)
		{
			originalRecord=originalSet.getMbo(0);
		}
		
		//String item=mainRecord.getString("itemnum");
		if( originalRecord != null && selectedClassMbo != null )
		{
			String classAttr = null;
			String hierarchyAttr = null;

			int index = originalAttr.indexOf(".");
			if( index < 0 )
			{
				classAttr = originalAttr;  
			}
			else
			{
				classAttr = originalAttr.substring(0, index)  + "IFICATIONID";  
			}
			hierarchyAttr = originalAttr;

			String value = selectedClassMbo.getString("classstructureid");
			originalBean.setValue(classAttr, value );
			String hierarchypath = originalRecord.getString(hierarchyAttr);
			originalBean.setValue(hierarchyAttr, hierarchypath, MboConstants.NOACCESSCHECK);

			clientSession.queueEvent(new WebClientEvent("dialogclose", app.getCurrentPageId(), null, clientSession));
		}
	}
	
	synchronized protected boolean moveTo(int row) throws MXException, RemoteException
	{
		if (row==0 && isfrominitialize)
		{
			isfrominitialize=false;
			return true;
		}
		return super.moveTo(row);
	}
}
