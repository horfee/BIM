package psdi.webclient.beans.bim.viewer;

import java.rmi.RemoteException;

import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.util.MXException;
import psdi.webclient.components.BIMViewer;
import psdi.webclient.system.beans.AppBean;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.beans.ResultsBean;
import psdi.webclient.system.beans.WebClientBean;
import psdi.webclient.system.controller.AppInstance;
import psdi.webclient.system.controller.ComponentInstance;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.controller.WebClientEvent;
import psdi.webclient.system.erm.EntityRelationshipModel;
import psdi.webclient.system.runtime.LoggingUtils;
import psdi.webclient.system.runtime.WebClientRuntime;
import psdi.webclient.system.session.WebClientSession;

public class WOBIMViewerEventBean extends BIMViewerEventBean
{

	protected void initialize() throws MXException, RemoteException
	{
		super.initialize();
	}
	
	public int eventSelect() throws MXException, RemoteException
	{
		WebClientEvent event = clientSession.getCurrentEvent();
		Object o = event.getValue();
		if( o == null || !(o instanceof String ))
		{
			// Should never happen unless the .jsp is altered
			return WebClientBean.EVENT_HANDLED;
		}
		String result[] = ((String)o).split( ";" );
		if(result.length > 1)
		{
			changeAppBeanRecordSet(app, (AppBean) app.getAppBean(), result[1]);
		}
		return EVENT_HANDLED;
	}

	
	private void changeAppBeanRecordSet(AppInstance app, AppBean appBean, String modelId)	throws RemoteException, MXException
	{
		ResultsBean resultsBean = app.getResultsBean();
		int existsIndex = 0;
		boolean exists = false;
		MboSetRemote resultsList = resultsBean.getMboSet();
		MboRemote mbo = resultsList.getMbo(existsIndex);
		while (mbo != null)
		{
			if (mbo.getString("location.modelid").equals(modelId))
			{
				exists = true;
				break;
			}
			mbo = resultsList.getMbo(++existsIndex);
		}
		
		if (!exists) // Add to results list if the selected record is not present
		{
			String userWhere = resultsBean.getUserWhere();
			userWhere = userWhere.isEmpty() ? userWhere : userWhere + " or ";
			userWhere = userWhere + "( exists (select 1 from locations where workorder.location = location and modelid = '" + modelId + "') )";
			appBean.initializeApp();
			resultsBean.resetQbe();
			resultsBean.setUserWhere(userWhere);
			resultsBean.reset();
		}
		
		resultsList = resultsBean.getMboSet();
		int recordToSelect = 0;
		mbo = resultsList.getMbo(recordToSelect);
		while (mbo != null)
		{
			if (mbo.getString("location.modelid").equals(modelId))
			{
				resultsBean.highlightrow(recordToSelect);
				appBean.setCurrentRow(recordToSelect);
				//appBean.fireStructureChangedEvent();
				break;
			}
			mbo = resultsList.getMbo(++recordToSelect);
		}
	}	
	
}