package psdi.webclient.beans.bim.viewer;

import java.rmi.RemoteException;

import com.ibm.json.java.JSONObject;

import psdi.app.asset.AssetRemote;
import psdi.mbo.MboConstants;
import psdi.mbo.MboRemote;
import psdi.util.MXException;
import psdi.util.MXFormat;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.WebClientEvent;
import psdi.webclient.system.session.WebClientSession;

public class BIMViewerEventBean extends DataBean {
    protected void initialize()
    throws MXException, RemoteException {
        super.initialize();
    }

    public void setupBean(WebClientSession wcs) {
        this.clientSession = wcs;
        this.sessionContext = wcs.getAdaptorInstance();

        this.app = this.clientSession.getCurrentApp();
    }

    public int eventSelect() throws MXException, RemoteException {
//        WebClientEvent event = this.clientSession.getCurrentEvent();
//        Object o = event.getValue();
//        if ((o == null) || (!(o instanceof String))) {
//            return EVENT_HANDLED;
//        }
//        String[] result = ((String) o).split(";");
//        if (result.length > 1) {
//            // do something like change the application 
//			  // record to match the selected item in the viewer	
//        }
//        return EVENT_HANDLED;
        return EVENT_CONTINUE;
    }

    public String getCurrentData(String attributesToPass) throws MXException, RemoteException {
		JSONObject jsonData = new JSONObject();
        String userName = this.app.getAppBean().getMboSet().getUserInfo().getLoginID();
        jsonData.put("whoami", userName);
        jsonData.put("currentApp", this.app.getId());
        
        //System.out.println(">>> BIMViewerEventBean getCurrentData attributesToPass: " + attributesToPass);
        if(attributesToPass != null && !attributesToPass.equals(""))
        {
        	MboRemote mbo = this.app.getAppBean().getMbo();
        	String attrs[] = attributesToPass.split(",");
            //System.out.println(">>> BIMViewerEventBean getCurrentData attrs.length: " + attrs.length);
        	for(int i=0;i<attrs.length;i++)
        	{
                //System.out.println(">>> BIMViewerEventBean getCurrentData attrs[i]: " + attrs[i]);
        		if(!attrs[i].equals(""))
        		{
        			String attribute = attrs[i].trim();
        			try
        			{
            			int attrType = mbo.getMboValueData(attribute).getTypeAsInt();
        				if(		attrType == MXFormat.BIGINT ||
        						attrType == MXFormat.INTEGER ||
        						attrType == MXFormat.SMALLINT
        						)
        				{
        	                //System.out.println(">>> BIMViewerEventBean getCurrentData long attribute: " + attribute);
                			jsonData.put(attribute, mbo.getLong(attribute));
        				}
        				else if(attrType == MXFormat.AMOUNT || 
        						attrType == MXFormat.DECIMAL ||
        						attrType == MXFormat.FLOAT
        						)
        				{
        	                //System.out.println(">>> BIMViewerEventBean getCurrentData float attribute: " + attribute);
                			jsonData.put(attribute, mbo.getFloat(attribute));
        				}
        				else
        				{
        	                //System.out.println(">>> BIMViewerEventBean getCurrentData string attribute: " + attribute);
                			jsonData.put(attribute, mbo.getString(attribute));
        				}
        			} catch (Exception e)
        			{
        				System.out.println(">>> getCurrentData exception for attribute: " + attribute);
        				e.printStackTrace();
        			}
        		}
        	}
        }
    	
        //System.out.println(">>> BIMViewerEventBean getCurrentData jsonData.toString(): " + jsonData.toString());
        return jsonData.toString();
    }
    
    // Event to allow viewer to set modelid
    public int assignModelId() throws MXException, RemoteException {
        WebClientEvent event = this.clientSession.getCurrentEvent();
        Object o = event.getValue();
        if ((o == null) || (!(o instanceof String))) {
            return EVENT_HANDLED;
        }
        String result = ((String) o);
        MboRemote asset = this.app.getAppBean().getMbo();
        if(!(asset instanceof AssetRemote)) {
        	asset = asset.getMboSet("asset").getMbo(0);
        }
        asset.setValue("modelid", result, MboConstants.NOACCESSCHECK | MboConstants.NOVALIDATION_AND_NOACTION);
        return EVENT_HANDLED;
    }
    
}