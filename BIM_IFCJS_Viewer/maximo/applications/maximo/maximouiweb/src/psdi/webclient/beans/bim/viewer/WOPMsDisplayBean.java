/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * (C) COPYRIGHT IBM CORP. 2011,2018
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */
package psdi.webclient.beans.bim.viewer;

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;

import psdi.app.ticket.WorkView;
import psdi.app.workorder.virtual.ViewWOPMsRemote;
import psdi.mbo.MboConstants;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.mbo.SqlFormat;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.webclient.beans.bim.Constants;
import psdi.webclient.beans.common.ViewWorkDetailsBean;
import psdi.webclient.components.BIMViewer;
import psdi.webclient.system.beans.DataBean;
import psdi.webclient.system.controller.ComponentInstance;
import psdi.webclient.system.controller.ControlInstance;
import psdi.webclient.system.controller.WebClientEvent;

public class WOPMsDisplayBean extends ViewWorkDetailsBean
{
	
	private final static String TYPE_WO          = "WORKORDER";
	private final static String TYPE_SR          = "SR";
	
	private final static String TABLE_WO  	       = "displayWOandPMs_viewWOandPMsLoc_ViewWO";
	private final static String TABLE_PM  	       = "displayWOandPMs_viewWOandPMsLoc_pmloc_ViewPM";
	private final static String BUTTOM_DISPLAY_SEL = "displayWOandPMs_DisplaySelected";

	private final static String MBO_MULTIASSETLOCCI = "MULTIASSETLOCCI";
	
	/**
	 * Reference to building model control
	 */
	private BIMViewer _model = null;
	/**
	 * The location associated with the model file for which this dialog
	 * was launched
	 */
	private String        _modelLocation = null;
	
	private boolean       _initialized = false;

	@Override
    public void initialize() throws MXException , RemoteException
	{
		super.initialize();
		if( _initialized ) return;
		
		setSiteValue();

		WebClientEvent event = clientSession.getCurrentEvent();
		ComponentInstance ci = event.getSourceComponentInstance();
		if( ci != null || ci instanceof BIMViewer  )
		{
			_model = (BIMViewer)ci;
			Object o = event.getValue();
			if( o != null && o instanceof String )
			{
				_modelLocation = (String)o;
			}
			else
			{
				throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
			}
		}
		else
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		
		// This method get called recusivly indirectly through the execute method
		// the event is not from the building model control, but the model location
		// is set from the initial initlization
		if( _modelLocation != null )
		{
			setValue("location", _modelLocation );
		}
		
		MboRemote parentMbo = parent.getMbo();
		MboRemote mbo = getMbo();
		ViewWOPMsRemote viewWOPMs = null;
		if(parentMbo!=null && parentMbo.isBasedOn("VIEWWOPMS"))
		{
			viewWOPMs=(ViewWOPMsRemote)parentMbo;
		}
		else if(mbo!=null && mbo.isBasedOn("VIEWWOPMS"))
		{
			viewWOPMs=(ViewWOPMsRemote)mbo;
		}
		
		boolean showChildren = viewWOPMs.getBoolean("SHOWCHILDREN");
		boolean showAncestors = viewWOPMs.getBoolean("SHOWPARENTS");
		
		boolean hasChildrenToShow = !viewWOPMs.getMboSet("LOCANCESTORCHILDREN").isEmpty();
		boolean hasAncestorsToShow = !viewWOPMs.getMboSet("LOCANCESTORANCESTORS").isEmpty();
		
		viewWOPMs.setValue("SHOWCHILDREN",showChildren && hasChildrenToShow,MboConstants.NOACCESSCHECK|MboConstants.NOVALIDATION);
		viewWOPMs.setFieldFlag("SHOWCHILDREN", MboConstants.READONLY, !hasChildrenToShow);
		
		viewWOPMs.setValue("SHOWPARENTS",showAncestors && hasAncestorsToShow,MboConstants.NOACCESSCHECK|MboConstants.NOVALIDATION);
		viewWOPMs.setFieldFlag("SHOWPARENTS", MboConstants.READONLY, !hasAncestorsToShow);
		
		_initialized = true;
	}
	
	@Override
    public synchronized int execute() 
		throws MXException, RemoteException
	{
		boolean  selectedOnly = false;
		if( _model == null )
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		
		WebClientEvent event = clientSession.getCurrentEvent();
		ControlInstance ci = event.getSourceControlInstance();
		if( ci == null )
		{
			throw new MXApplicationException( Constants.BUNDLE_MSG, Constants.MSG_INTERNAL_ERR );	
		}
		
		String modelSiteId = _model.getSiteId();
		if( modelSiteId == null || modelSiteId.length() == 0 )
		{
			return EVENT_HANDLED;
		}
		
		String button = ci.getId();
		if( button.equalsIgnoreCase( BUTTOM_DISPLAY_SEL ))
		{
			selectedOnly = true;
		}
		
		HashSet<String> locationSet  = new HashSet<String>();
		
        DataBean bean = app.getDataBean( TABLE_WO );

        Vector<?> selection = bean.getSelection();
        Enumeration<?> e = selection.elements();
		HashSet<Long>   selectionSet =  new HashSet<Long>();
        while( e.hasMoreElements() )
        {
        	WorkView wv = (WorkView)e.nextElement();
        	long uid = wv.getUniqueIDValue();
        	selectionSet.add( new Long( uid ) );
        }
        
        int i = 0;
        MboRemote mboItem = bean.getMbo( i );
        while( mboItem != null )
    	{
        	long uid = mboItem.getUniqueIDValue();
        	boolean selected = selectionSet.contains( new Long( uid) );
        	if( selectedOnly && !selected )
        	{
                mboItem = bean.getMbo( ++i );
        		continue;
        	}

        	String siteId = mboItem.getString( Constants.FIELD_SITEID );
    		if( siteId == null || siteId.length()  == 0 )
    		{
                mboItem = bean.getMbo( ++i );
    			continue;		// Should never happen
    		}
    		if( !modelSiteId.equals( siteId ))
    		{
                mboItem = bean.getMbo( ++i );
    			continue;    // Item is for a different site
    		}
    		String type  = mboItem.getString( Constants.FIELD_CLASS );
    		String key  = mboItem.getString( Constants.FIELD_RECORD_KEY );

    		if(    type.equalsIgnoreCase( TYPE_WO )
    			|| type.equalsIgnoreCase( TYPE_SR ))
    		{
        		MboSetRemote mlMboSet = lookupAllAssetLocationsCIforWO( key, siteId );
        		for( int j = 0; j < mlMboSet.count(); j++ )
        		{
        			MboRemote mlMbo = mlMboSet.getMbo( j );
            		key  = mlMbo.getString( Constants.FIELD_RECORD_KEY );
            		String recordClass = mlMbo.getString( Constants.FIELD_RECORDCLASS );
            		if( !recordClass.equalsIgnoreCase( type ))
            		{
            			continue;
            		}
            		String location = _model.lookupLocationModelId( mlMbo.getString( "LOCATION" ), modelSiteId );
        			if( location != null  && location.length() > 0 )
        			{
        				locationSet.add( location );
        			}
        		}
    		}
            mboItem = bean.getMbo( ++i );
    	}

        bean = app.getDataBean( TABLE_PM );

        selection = bean.getSelection();
        e = selection.elements();
		selectionSet =  new HashSet<Long>();
        while( e.hasMoreElements() )
        {
        	mboItem = (MboRemote)e.nextElement();
        	long uid = mboItem.getUniqueIDValue();
        	selectionSet.add( new Long( uid ) );
        }

        i = 0;
        mboItem = bean.getMbo( i );
        while( mboItem != null )
    	{
        	long uid = mboItem.getUniqueIDValue();
        	boolean selected = selectionSet.contains( new Long( uid) );
        	if( selectedOnly && !selected )
        	{
                mboItem = bean.getMbo( ++i );
        		continue;
        	}

        	String siteId = mboItem.getString( Constants.FIELD_SITEID );
    		if( siteId == null || siteId.length()  == 0 )
    		{
                mboItem = bean.getMbo( ++i );
    			continue;		// Should never happen
    		}
    		if( !modelSiteId.equals( siteId ))
    		{
                mboItem = bean.getMbo( ++i );
    			continue;    // Item is for a different site
    		}
    		String location  = mboItem.getString( Constants.FIELD_LOCATION );
    		if( location != null && location.length() > 0 )
    		{
        		location = _model.lookupLocationModelId( location, modelSiteId );
    			if( location != null  && location.length() > 0 )
    			{
    				locationSet.add( location );
    			}
    		}

    		mboItem = bean.getMbo( ++i );
    	}        
    	_model.setMultiSelect( _modelLocation, locationSet );
		 
		return super.execute();
	}
	
	public MboSetRemote lookupAllAssetLocationsCIforWO(
		String recordKey,
		String siteId
	) 
		throws RemoteException, 
		MXException 
	{
		try
		{
			DataBean dataBean = app.getAppBean();
			SqlFormat sqlf = new SqlFormat( dataBean.getMbo(), Constants.FIELD_RECORD_KEY + "=:1 and siteid=:2");
			sqlf.setObject(1, MBO_MULTIASSETLOCCI, Constants.FIELD_RECORD_KEY, recordKey );
			sqlf.setObject(2, MBO_MULTIASSETLOCCI, Constants.FIELD_SITEID, siteId ) ; 
			return dataBean.getMbo().getMboSet("$getMultiAssetLocCISet", MBO_MULTIASSETLOCCI, sqlf.format());
		}
		catch( Exception e )
		{
			return null;
		}
	}

	public MboSetRemote lookupAllAssetLocationsCIforTickets(
			String recordKey,
			String siteId
		) 
			throws RemoteException, 
			MXException 
		{
			try
			{
				DataBean dataBean = app.getAppBean();
				SqlFormat sqlf = new SqlFormat( dataBean.getMbo(), Constants.FIELD_RECORD_KEY + "=:1 and siteid=:2");
				sqlf.setObject(1, MBO_MULTIASSETLOCCI, Constants.FIELD_RECORD_KEY, recordKey );
				sqlf.setObject(2, MBO_MULTIASSETLOCCI, Constants.FIELD_SITEID, siteId ) ; 
				return dataBean.getMbo().getMboSet("$getMultiAssetLocCISet", MBO_MULTIASSETLOCCI, sqlf.format());
			}
			catch( Exception e )
			{
				return null;
			}
		}
}
