/**
* Copyright IBM Corporation 2009-2017
*
* Licensed under the Eclipse Public License - v 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.eclipse.org/legal/epl-v10.html
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* 
* @Author Doug Wood
**/
package psdi.webclient.beans.bim.viewer.lmv;

import java.rmi.RemoteException;

import psdi.mbo.MboSetRemote;
import psdi.util.MXException;
import psdi.webclient.system.beans.DataBean;



/**
 * Allows the user to monitor progress on uploads to the AUtodesk cloud
 */
public class ModelUploadStatussBean extends DataBean
{
	public int refreshStatus() 
		throws MXException, 
		       RemoteException
	{
		MboSetRemote statusSet = getMboSet();
		statusSet.reset();
		tableStateFlags.setFlag(TABLE_DETAILS_EXPANDED, true);
		refreshTable();
		return EVENT_HANDLED;
	}
}