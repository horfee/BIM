package psdi.webclient.beans.bim.viewer;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.server.MXServer;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.WebClientEvent;

public class WOModelLocBean extends AssetLookupBase
{
  private WOTreeBaseBean _mainTree = null;
  private WOTreeBaseBean _multiTree = null;
	
	protected void initialize() throws MXException, RemoteException
	{
		super.initialize();
	}

  public void eventAddSelection()
    throws RemoteException, MXException
  {
    //System.out.println(">>> WOModelLocBean bean " + java.lang.Thread.currentThread().getStackTrace()[3].toString() + " ==> " + new Object() {  }.getClass().getEnclosingMethod().getName());
    DataBean appBean = this.app.getDataBean();
    MboRemote appMbo = appBean.getMbo();
    String siteId = getString("SITEID");

    DataBean multiLocDataBean = getMultiLocCITable(appMbo);
    Set selectedAssets = new HashSet();
    Set selectedLocations = new HashSet();

    if (!lookupSelectedAssetsAndLocations(siteId, selectedAssets, selectedLocations))
    {
      return;
    }

    filterAndUndelete(multiLocDataBean, siteId, selectedAssets, selectedLocations);

    insertAssets(appMbo, selectedAssets, siteId);
    insertLocations(appMbo, selectedLocations, siteId);

    if (multiLocDataBean != null)
    {
      multiLocDataBean.fireStructureChangedEvent();
      multiLocDataBean.refreshTable();
    }
  }

  public void eventRemoveSelection()
    throws RemoteException, MXException
  {
    //System.out.println(">>> WOModelLocBean bean " + java.lang.Thread.currentThread().getStackTrace()[3].toString() + " ==> " + new Object() {  }.getClass().getEnclosingMethod().getName());
    DataBean appBean = this.app.getDataBean();
    MboRemote appMbo = appBean.getMbo();
    String siteId = getString("SITEID");

    DataBean multiLocDataBean = getMultiLocCITable(appMbo);
    Set selectedAssets = new HashSet();
    Set selectedLocations = new HashSet();
    if (!lookupSelectedAssetsAndLocations(siteId, selectedAssets, selectedLocations))
    {
      return;
    }

    for (int i = 0; i < multiLocDataBean.count(); i++)
    {
      MboRemote multiLocMbo = multiLocDataBean.getMbo(i);
      if (multiLocMbo.toBeDeleted())
      {
        continue;
      }
      String itemSiteId = multiLocMbo.getString("SITEID");
      if ((itemSiteId == null) || (itemSiteId.length() == 0))
      {
        continue;
      }
      if (!siteId.equals(itemSiteId))
      {
        continue;
      }
      String assetnum = multiLocMbo.getString("ASSETNUM");
      if ((assetnum != null) && (assetnum.length() > 0))
      {
        if (!selectedAssets.contains(assetnum))
          continue;
        multiLocDataBean.delete(i);
      }
      else
      {
        String location = multiLocMbo.getString("LOCATION");
        if ((location == null) || (location.length() <= 0))
          continue;
        if (!selectedLocations.contains(location))
          continue;
        multiLocDataBean.delete(i);
      }
    }
  }

  boolean lookupSelectedAssetsAndLocations(String siteId, Set<String> selectedAssets, Set<String> selectedLocatios)
    throws RemoteException, MXException
  {
    //System.out.println(">>> WOModelLocBean bean " + java.lang.Thread.currentThread().getStackTrace()[3].toString() + " ==> " + new Object() {  }.getClass().getEnclosingMethod().getName());
    selectedAssets.clear();
    selectedLocatios.clear();

    Set values = getValueList();
    MboSetRemote mboSet = lookupLocationListFromModelId(values, siteId);

    if (mboSet.isEmpty())
    {
      return false;
    }
    for (int i = 0; i < mboSet.count(); i++)
    {
      MboRemote locMbo = mboSet.getMbo(i);
      String location = locMbo.getString("LOCATION");
      selectedLocatios.add(location);
    }
    mboSet.cleanup();

    mboSet = lookupAssetsAtLocations(selectedLocatios, siteId);
    for (int i = 0; i < mboSet.count(); i++)
    {
      MboRemote locMbo = mboSet.getMbo(i);
      String location = locMbo.getString("LOCATION");
      selectedLocatios.remove(location);
      String assetnum = locMbo.getString("ASSETNUM");
      selectedAssets.add(assetnum);
    }
    return true;
  }

  MboSetRemote lookupLocationListFromModelId(Set<String> values, String siteId)
    throws RemoteException, MXException
  {
    //System.out.println(">>> WOModelLocBean bean " + java.lang.Thread.currentThread().getStackTrace()[3].toString() + " ==> " + new Object() {  }.getClass().getEnclosingMethod().getName());
    StringBuffer query = new StringBuffer();
    String inClause = formatInClause(values.iterator());
    if ((inClause == null) || (inClause.length() == 0))
    {
      return null;
    }

    query.append("SITEID");
    query.append(" = '");
    query.append(siteId);
    query.append("' AND ");

    query.append(getBinding());
    query.append(inClause);

    MXServer server = MXServer.getMXServer();
    MboSetRemote locationSet = server.getMboSet("LOCATIONS", getMbo(0).getUserInfo());
    if (locationSet == null)
    {
      throw new MXApplicationException("bimviewer", "Internal-error");
    }
    locationSet.setWhere(query.toString());
    locationSet.reset();
    return locationSet;
  }

  MboSetRemote lookupAssetsAtLocations(Set<String> values, String siteId)
    throws RemoteException, MXException
  {
    //System.out.println(">>> WOModelLocBean bean " + java.lang.Thread.currentThread().getStackTrace()[3].toString() + " ==> " + new Object() {  }.getClass().getEnclosingMethod().getName());
    StringBuffer query = new StringBuffer();
    String inClause = formatInClause(values.iterator());
    if ((inClause == null) || (inClause.length() == 0))
    {
      return null;
    }

    query.append("SITEID");
    query.append(" = '");
    query.append(siteId);
    query.append("' AND ");

    query.append("LOCATION");
    query.append(inClause);

    MXServer server = MXServer.getMXServer();
    MboSetRemote locationSet = server.getMboSet("ASSET", getMbo(0).getUserInfo());
    if (locationSet == null)
    {
      throw new MXApplicationException("bimviewer", "Internal-error");
    }
    locationSet.setWhere(query.toString());
    locationSet.reset();
    return locationSet;
  }

  void filterItems(String siteId, Set<String> existing, Set<String> selected)
  {
    //System.out.println(">>> WOModelLocBean bean " + java.lang.Thread.currentThread().getStackTrace()[3].toString() + " ==> " + new Object() {  }.getClass().getEnclosingMethod().getName());
    Iterator itr = selected.iterator();
    while (itr.hasNext())
    {
      String item = (String)itr.next();
      if (!existing.contains(item + siteId))
        continue;
      itr.remove();
    }
  }

  public WOTreeBaseBean getMainTree()
  {
    //System.out.println(">>> WOModelLocBean bean " + java.lang.Thread.currentThread().getStackTrace()[3].toString() + " ==> " + new Object() {  }.getClass().getEnclosingMethod().getName());
    return this._mainTree;
  }

  public void setMainTree(WOTreeBaseBean mainTree)
  {
    //System.out.println(">>> WOModelLocBean bean " + java.lang.Thread.currentThread().getStackTrace()[3].toString() + " ==> " + new Object() {  }.getClass().getEnclosingMethod().getName());
    this._mainTree = mainTree;
  }

  public WOTreeBaseBean getMultiTree()
  {
    //System.out.println(">>> WOModelLocBean bean " + java.lang.Thread.currentThread().getStackTrace()[3].toString() + " ==> " + new Object() {  }.getClass().getEnclosingMethod().getName());
    return this._multiTree;
  }

  public void setMultiTree(WOTreeBaseBean multiTree)
  {
    //System.out.println(">>> WOModelLocBean bean " + java.lang.Thread.currentThread().getStackTrace()[3].toString() + " ==> " + new Object() {  }.getClass().getEnclosingMethod().getName());

    this._multiTree = multiTree;
  }
}