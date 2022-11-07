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

public class Messages
{
	// The model directory does not exist on the server.  Contact a system administrator.
	public final static String ERR_MODEL_DIR_MISSING      = "model-dir-missing";       
	// There are no assets at the selected location
	public final static String MSG_NO_ASSET               = "no-asset";
	// You must select an item in the model before you can accept the dialog
	public final static String MSG_NO_SELECTION           = "no-item-selected";
	// The dialog can not be selected when more than one item is selected in the model
	public final static String MSG_TOO_MANY_SELECTED      = "too-many-selected";
	// This dialog is called from an unsupported application.  See your system administrator
	public final static String MSG_UNSUPPORTED_APP        = "unsupported-app";
}
