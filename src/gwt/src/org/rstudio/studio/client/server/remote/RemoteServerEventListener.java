/*
 * RemoteServerEventListener.java
 *
 * Copyright (C) 2009-11 by RStudio, Inc.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.server.remote;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import org.rstudio.core.client.files.FileSystemItem;
import org.rstudio.core.client.jsonrpc.RpcObjectList;
import org.rstudio.core.client.jsonrpc.RpcRequest;
import org.rstudio.core.client.jsonrpc.RpcRequestCallback;
import org.rstudio.core.client.jsonrpc.RpcResponse;
import org.rstudio.studio.client.application.events.*;
import org.rstudio.studio.client.application.model.SaveAction;
import org.rstudio.studio.client.application.model.SessionSerializationAction;
import org.rstudio.studio.client.server.Bool;
import org.rstudio.studio.client.server.ServerError;
import org.rstudio.studio.client.server.ServerRequestCallback;
import org.rstudio.studio.client.workbench.events.*;
import org.rstudio.studio.client.workbench.model.BrowseUrlInfo;
import org.rstudio.studio.client.workbench.model.ErrorMessage;
import org.rstudio.studio.client.workbench.model.OAuthApproval;
import org.rstudio.studio.client.workbench.model.QuotaStatus;
import org.rstudio.studio.client.workbench.views.choosefile.events.ChooseFileEvent;
import org.rstudio.studio.client.workbench.views.console.events.*;
import org.rstudio.studio.client.workbench.views.console.model.ConsolePrompt;
import org.rstudio.studio.client.workbench.views.console.model.ConsoleResetHistory;
import org.rstudio.studio.client.workbench.views.data.events.ViewDataEvent;
import org.rstudio.studio.client.workbench.views.data.model.DataView;
import org.rstudio.studio.client.workbench.views.edit.events.ShowEditorEvent;
import org.rstudio.studio.client.workbench.views.files.events.FileChangeEvent;
import org.rstudio.studio.client.workbench.views.files.model.FileChange;
import org.rstudio.studio.client.workbench.views.help.events.ShowHelpEvent;
import org.rstudio.studio.client.workbench.views.history.events.HistoryEntriesAddedEvent;
import org.rstudio.studio.client.workbench.views.history.model.HistoryEntry;
import org.rstudio.studio.client.workbench.views.packages.events.InstalledPackagesChangedEvent;
import org.rstudio.studio.client.workbench.views.packages.events.PackageStatusChangedEvent;
import org.rstudio.studio.client.workbench.views.packages.model.PackageStatus;
import org.rstudio.studio.client.workbench.views.plots.events.LocatorEvent;
import org.rstudio.studio.client.workbench.views.plots.events.PlotsChangedEvent;
import org.rstudio.studio.client.workbench.views.plots.model.PlotsState;
import org.rstudio.studio.client.workbench.views.source.editors.text.events.PublishPdfEvent;
import org.rstudio.studio.client.workbench.views.source.events.FileEditEvent;
import org.rstudio.studio.client.workbench.views.source.events.ShowContentEvent;
import org.rstudio.studio.client.workbench.views.source.events.ShowDataEvent;
import org.rstudio.studio.client.workbench.views.source.model.ContentItem;
import org.rstudio.studio.client.workbench.views.source.model.DataItem;
import org.rstudio.studio.client.workbench.views.workspace.events.WorkspaceObjectAssignedEvent;
import org.rstudio.studio.client.workbench.views.workspace.events.WorkspaceObjectRemovedEvent;
import org.rstudio.studio.client.workbench.views.workspace.events.WorkspaceRefreshEvent;
import org.rstudio.studio.client.workbench.views.workspace.model.WorkspaceObjectInfo;

import java.util.ArrayList;
import java.util.HashMap;


class RemoteServerEventListener 
{
   static class ClientEvent extends JavaScriptObject
   {   
      public static final String Busy = "busy";
      public static final String ConsolePrompt = "console_prompt";
      public static final String ConsoleOutput = "console_output" ;
      public static final String ConsoleError = "console_error";
      public static final String ConsoleWritePrompt = "console_write_prompt";
      public static final String ConsoleWriteInput = "console_write_input";
      public static final String ShowErrorMessage = "show_error_message";
      public static final String ShowHelp = "show_help" ;
      public static final String BrowseUrl = "browse_url";
      public static final String WorkspaceRefresh = "workspace_refresh";
      public static final String WorkspaceAssign = "workspace_assign";
      public static final String WorkspaceRemove = "workspace_remove";
      public static final String ShowEditor = "show_editor";
      public static final String ChooseFile = "choose_file";
      public static final String AbendWarning = "abend_warning";
      public static final String Quit = "quit";
      public static final String Suicide = "suicide";
      public static final String FileChanged = "file_changed";
      public static final String WorkingDirChanged = "working_dir_changed";
      public static final String PlotsStateChanged = "plots_state_changed";
      public static final String ViewData = "view_data";
      public static final String PackageStatusChanged = "package_status_changed";
      public static final String InstalledPackagesChanged = "installed_packages_changed";
      public static final String Locator = "locator";
      public static final String ConsoleResetHistory = "console_reset_history";
      public static final String SessionSerialization = "session_serialization";
      public static final String HistoryEntriesAdded = "history_entries_added";
      public static final String QuotaStatus = "quota_status";
      public static final String OAuthApproval = "oauth_approval";
      public static final String PublishPdf = "publish_pdf";
      public static final String FileEdit = "file_edit";
      public static final String ShowContent = "show_content";
      public static final String ShowData = "show_data";
      public static final String AsyncCompletion = "async_completion";
      public static final String SaveActionChanged = "save_action_changed";

      protected ClientEvent()
      {
      }
      
      public final native int getId() /*-{
         return this.id;
      }-*/;
      
      public final native String getType() /*-{
         return this.type;
      }-*/;
      
      public final native <T> T getData() /*-{
         return this.data;
      }-*/;
   }

   /**
    * Stores the context needed to complete an async request.
    */
   static class AsyncRequestInfo
   {
      AsyncRequestInfo(RpcRequest request, RpcRequestCallback callback)
      {
         this.request = request;
         this.callback = callback;
      }

      public final RpcRequest request;
      public final RpcRequestCallback callback;
   }

   public RemoteServerEventListener(RemoteServer server)
   {
      server_ = server;
      lastEventId_ = -1;
      listenCount_ = 0;
      listenErrorCount_ = 0;
      isListening_ = false;
      sessionWasQuit_ = false;
      
      // we take the liberty of stopping ourselves if the window is on 
      // the verge of being closed. this allows us to prevent the scenario:
      //
      //  - window closes and the browser terminates the listener connection
      //  - onError is called when the connection is terminated -- this results
      //    in another call to listen() which starts a new connection
      //  - now we have a "leftover" connection still active with the server
      //    even after the user has left the page
      //
      // we can't use Window CloseEvent because this occurs *after* the 
      // connection is terminated and restarted in onError. we currently
      // don't handle the ClosingEvent elsewhere in the application so calling
      // stop() here is as good as calling it in CloseEvent. however, even
      // if we did handle ClosingEvent and show a prompt which resulted in
      // the window NOT closing this would still be OK as the event listener
      // would still be restarted as necessary by the call to ensureEvents
      // 
      // note that in the future if we need to make sure event listening
      // is preserved even in the close cancelled case described above
      // (e.g. for multi-user cases) then we would need to make sure there
      // is another way to restart the listener (perhaps a global timer
      // that checks for isListening every few seconds, or perhaps some
      // abstraction over addWindowClosingHandler that allows "undo" of 
      // things which were closed or shutdown during closing
      Window.addWindowClosingHandler(new ClosingHandler() {
         public void onWindowClosing(ClosingEvent event)
         {
            stop();
         }
      });
   }
     
   public void start()
   {      
      // start should never be called on a running event listener!
      // (need to protect against extra requests going to the server
      // and starving the browser of its 2 connections)
      if (isListening_)
         stop();
      
      // maintain flag indicating that we *should* be listening (allows us to
      // know when to restart in the case that we are unexpectedly cutoff)
      isListening_ = true;
      
      // reset listen count. this will allow us to delay listening on the
      // second listen (to prevent the "perpetual loading" problem)
      listenCount_ = 0;
      
      // reset our lastEventId to make sure we get all events which are 
      // currently pending on the server. note in the case of "restarting"
      // the event listener setting this to -1 could in theory cause us to
      // receive an event twice (because the reset to -1 causes us to never
      // confirm receipt of the event with the server). in practice this
      // would a) be very unlikely; b) not be that big of a deal; and c) is
      // judged preferrable than doing something more complex in this code
      // which might avoid dupes but cause other bugs (such as missing events
      // from the server). note also that when we go multi-user we'll be 
      // revisiting this mechanism again so there will be an opportunity to 
      // eliminate this scenario then
      lastEventId_ = -1;
      
      // start listening
      listen();
   }
     
   public void stop()
   {        
      isListening_ = false;
      listenCount_ = 0;
      if (activeRequestCallback_ != null)
      {
         activeRequestCallback_.cancel();
         activeRequestCallback_ = null;
      }
      if (activeRequest_ != null)
      {
         activeRequest_.cancel();
         activeRequest_ = null;
      }
   }
   
   // ensure that we are actively listening for events (used to make 
   // sure that we restart listening when the session is about to resume
   // after a suspension)
   public void ensureListening(final int attempts)
   {
      // exit if we are now listening
      if (isListening_)
         return;
      
      // exit if we have already quit
      if (sessionWasQuit_)
         return;
      
      // attempt to start the service
      start();
      
      // if appropriate, schedule another attempt in 250ms 
      final int attemptsRemaining = attempts - 1;
      if (attemptsRemaining > 0)
      {
         new Timer() { 
            public void run()
            {
               ensureListening(attemptsRemaining);
            } 
         }.schedule(250);
      }
   }
   
   // ensure that events are received during the next short time interval.
   // this not only starts listening if we aren't currently listening but
   // also ensures (via a Watchdog) that events are received (and if they
   // are not received restarts the event listener)
   public void ensureEvents()
   {  
     // if we aren't listening then start us up
     if (!isListening_)
     {
         start();
     } 
     
     // if we are listening then use the Watchdog to still make sure we 
     // receive the events even if it requires restarting
     else
     {     
        // NOTE: Watchdog is required to work around pathological cases
        // where the browser has terminated our request for events but
        // we have not been notified nor can we programmatically detect it.
        // we need a way to recover and this is it. we have observed this
        // behavior in webkit if: 
        //
        //   1) we do not use DeferredCommand/doListen (see below); and
        //
        //   2) the user navigates Back within a Frame 
        //
        // can only imagine that it could happen in other scenarios!
   
        if (!watchdog_.isRunning())
          watchdog_.run(kWatchdogIntervalMs);
     }
   }
   
   private void restart()
   {
      stop();
      start();
   }
   
   private void listen()
   {
      // bounce listen to ensure it is never added to the browser's internal 
      // list of requests bound to the current page load. being on this list
      // (at least in webkit, perhaps in others) results in at least 2 and 
      // perhaps other problems:
      //
      //  1) perpetual "Loading..." indicator displayed to user (user can
      //     also then "cancel" the event request!); and
      //
      //  2) terimation of the request without warning by the browser when
      //     the user hits the Back button within a frame hosted on the page
      //     (note in this case we get no error so think the request is still
      //     running -- see Watchdog for workaround to this general class of 
      //     issues)
      
      // determine bounce ms (do a bigger bounce for the second listen
      // request as this is the one which gets us stuck in "perpetual loading")
      int bounceMs = 1;
      if (++listenCount_ == 2)
         bounceMs = kSecondListenBounceMs;
      
      Timer listenTimer = new Timer() {
         @Override
         public void run()
         {
            doListen();
         }
      };
      listenTimer.schedule(bounceMs);
   }
   
   private void doListen()
   {  
      // abort if we are no longer running
      if (!isListening_)
         return;
          
      // setup request callback (save reference for cancellation)
      activeRequestCallback_ = new ServerRequestCallback<JsArray<ClientEvent>>() 
      {
         @Override
         public void onResponseReceived(JsArray<ClientEvent> events)
         {
            // keep watchdog appraised of successful receipt of events
            watchdog_.notifyResponseReceived();
            
            try
            {
               // only processs events if we are still listening
               if (isListening_ && (events != null))
               {
                  for (int i=0; i<events.length(); i++)
                  {
                     // we can stop listening in the middle of dispatching
                     // events (e.g. if we dispatch a Suicide event) so we 
                     // need to check the listening_ flag before each event
                     // is dispatched
                     if (!isListening_)
                        return;
                     
                     // disppatch event
                     ClientEvent event = events.get(i);
                     enqueueEventForDispatch(event);
                     lastEventId_ = event.getId();
                  }   
               }
            }
            // catch all here to make sure that in all cases we call
            // listen() again after processing
            catch(Throwable e)
            {
               GWT.log("ERROR: Processing client events", e);
            }
            
            // listen for more events
            listen();
         }
         
         @Override
         public void onError(ServerError error)
         {           
            // stop listening for events
            stop();
            
            // if this was server unavailable then signal event and return
            if (error.getCode() == ServerError.UNAVAILABLE)
            {
               ServerUnavailableEvent event = new ServerUnavailableEvent();
               server_.getEventBus().fireEvent(event);   
               return;
            }
            
            // attempt to restart listening, but throttle restart attempts
            // in both timing (500ms delay) and quantity (no more than 5
            // attempts). We do this because unthrottled restart attempts could
            // result in our server getting hammered with requests)
            if (listenErrorCount_++ <= 5)
            {
               Timer startTimer = new Timer() {
                  @Override
                  public void run()
                  {
                     // only start again if we haven't been started 
                     // by some other means (e.g. ensureListening, etc)
                     if (!isListening_)
                        start();
                  }
               };
               startTimer.schedule(500);
            }
            // otherwise reset the listen error count and remain stopped
            else
            {
               listenErrorCount_ = 0;
            }
         }
      };
      
      // retry handler (restart listener)
      RetryHandler retryHandler = new RetryHandler() {

         public void onRetry()
         {
            // need to do a full restart to ensure that the existing
            // activeRequest_ and activeRequestCallback_ are cleaned up
            // and all state is reset correctly
            restart();
         }
         
         public void onError(ServerError error)
         {
            // error while attempting to recover, to be on the safe side
            // we simply stop listening for events. if rather than stopping 
            // we restarted we would open ourselves up to a situation
            // where we keep hitting the same error over and over again.
            stop();
         }
      };
      
      // send request
      activeRequest_ = server_.getEvents(lastEventId_, 
                                         activeRequestCallback_,
                                         retryHandler);                             
   }

   private void enqueueEventForDispatch(ClientEvent event)
   {
      pendingEvents_.add(event);
      if (pendingEvents_.size() == 1)
      {
         Scheduler.get().scheduleIncremental(new RepeatingCommand()
         {
            public boolean execute()
            {
               final int MAX_EVENTS_AT_ONCE = 200;
               for (int i = 0;
                    i < MAX_EVENTS_AT_ONCE && pendingEvents_.size() > 0;
                    i++)
               {
                  ClientEvent currentEvent = pendingEvents_.remove(0);
                  dispatchEvent(currentEvent);
               }
               return pendingEvents_.size() > 0;
            }
         });
      }
   }
   
   private void dispatchEvent(ClientEvent event) 
   { 
      String type = event.getType();
      EventBus eventBus = server_.getEventBus();
      try
      {
         if (type.equals(ClientEvent.Busy))
         {
            boolean busy = event.<Bool>getData().getValue();
            eventBus.fireEvent(new BusyEvent(busy));
         }
         else if (type.equals(ClientEvent.ConsoleOutput))
         {
            String output = event.getData();
            eventBus.fireEvent(new ConsoleWriteOutputEvent(output));
         }
         else if (type.equals(ClientEvent.ConsoleError))
         {
            String error = event.getData();
            eventBus.fireEvent(new ConsoleWriteErrorEvent(error));
         }
         else if (type.equals(ClientEvent.ConsoleWritePrompt))
         {
            String prompt = event.getData();
            eventBus.fireEvent(new ConsoleWritePromptEvent(prompt));
         }
         else if (type.equals(ClientEvent.ConsoleWriteInput))
         {
            String input = event.getData();
            eventBus.fireEvent(new ConsoleWriteInputEvent(input));
         }
         else if (type.equals(ClientEvent.ConsolePrompt))
         {
            ConsolePrompt prompt = event.getData();
            eventBus.fireEvent(new ConsolePromptEvent(prompt));
         }
         else if (type.equals(ClientEvent.ShowEditor))
         {
            String content = event.getData();
            eventBus.fireEvent(new ShowEditorEvent(content));
         }
         else if (type.equals(ClientEvent.FileChanged))
         {
            FileChange fileChange = event.getData();
            eventBus.fireEvent(new FileChangeEvent(fileChange));
         }
         else if (type.equals(ClientEvent.WorkingDirChanged))
         {
            String path = event.getData();
            eventBus.fireEvent(new WorkingDirChangedEvent(path));
         }
         else if (type.equals(ClientEvent.WorkspaceRefresh))
         {
            eventBus.fireEvent(new WorkspaceRefreshEvent());
         }
         else if (type.equals(ClientEvent.WorkspaceAssign))
         {
            WorkspaceObjectInfo objectInfo = event.getData();
            eventBus.fireEvent(new WorkspaceObjectAssignedEvent(objectInfo));
         }
         else if (type.equals(ClientEvent.WorkspaceRemove))
         {
            String objectName = event.getData();
            eventBus.fireEvent(new WorkspaceObjectRemovedEvent(objectName));
         }
         else if (type.equals(ClientEvent.ShowHelp))
         {
            String helpUrl = event.getData();
            eventBus.fireEvent(new ShowHelpEvent(helpUrl));
         }
         else if (type.equals(ClientEvent.ShowErrorMessage))
         {
            ErrorMessage errorMessage = event.getData();
            eventBus.fireEvent(new ShowErrorMessageEvent(errorMessage));
         }
         else if (type.equals(ClientEvent.ChooseFile))
         {
            boolean newFile = event.<Bool>getData().getValue();
            eventBus.fireEvent(new ChooseFileEvent(newFile));
         }
         else if (type.equals(ClientEvent.BrowseUrl))
         {
            BrowseUrlInfo urlInfo = event.getData();
            eventBus.fireEvent(new BrowseUrlEvent(urlInfo));
         }
         else if (type.equals(ClientEvent.PlotsStateChanged))
         {
            PlotsState plotsState = event.getData();
            eventBus.fireEvent(new PlotsChangedEvent(plotsState));
         }
         else if (type.equals(ClientEvent.ViewData))
         {
            DataView dataView = event.getData();
            eventBus.fireEvent(new ViewDataEvent(dataView));
         }
         else if (type.equals(ClientEvent.InstalledPackagesChanged))
         {
            eventBus.fireEvent(new InstalledPackagesChangedEvent());
         }
         else if (type.equals(ClientEvent.PackageStatusChanged))
         {
            PackageStatus status = event.getData();
            eventBus.fireEvent(new PackageStatusChangedEvent(status));
         }
         else if (type.equals(ClientEvent.Locator))
         {
            eventBus.fireEvent(new LocatorEvent());
         }
         else if (type.equals(ClientEvent.ConsoleResetHistory))
         {
            ConsoleResetHistory reset = event.getData();
            eventBus.fireEvent(new ConsoleResetHistoryEvent(reset));
         }
         else if (type.equals(ClientEvent.SessionSerialization))
         {
            SessionSerializationAction action = event.getData();
            eventBus.fireEvent(new SessionSerializationEvent(action));
         }
         else if (type.equals(ClientEvent.HistoryEntriesAdded))
         {
            RpcObjectList<HistoryEntry> entries = event.getData();
            eventBus.fireEvent(new HistoryEntriesAddedEvent(entries));
         }
         else if (type.equals(ClientEvent.QuotaStatus))
         {
            QuotaStatus quotaStatus = event.getData();
            eventBus.fireEvent(new QuotaStatusEvent(quotaStatus));
         }
         else if (type.equals(ClientEvent.OAuthApproval))
         {
            OAuthApproval oauthApproval = event.getData();
            eventBus.fireEvent(new OAuthApprovalEvent(oauthApproval));
         }
         else if (type.equals(ClientEvent.PublishPdf))
         {
            String path = event.getData();
            eventBus.fireEvent(new PublishPdfEvent(path));
         }
         else if (type.equals(ClientEvent.FileEdit))
         {
            FileSystemItem file = event.getData();
            eventBus.fireEvent(new FileEditEvent(file));
         }
         else if (type.equals(ClientEvent.ShowContent))
         {
            ContentItem content = event.getData();
            eventBus.fireEvent(new ShowContentEvent(content));
         }
         else if (type.equals(ClientEvent.ShowData))
         {
            DataItem data = event.getData();
            eventBus.fireEvent(new ShowDataEvent(data));
         }
         else if (type.equals(ClientEvent.AbendWarning))
         {            
            eventBus.fireEvent(new SessionAbendWarningEvent());
         }
         else if (type.equals(ClientEvent.Quit))
         {
            // NOTE: we don't explicily stop listending for events here 
            // for two reasons:
            //
            //   1) There could be additional console output events which
            //      occur after Quit
            //
            //   2) We will automatically stop listening as a result of 
            //      receiving ServiceUnavailable on the next listen()
            //
            
            // set flag to avoid ensureListening/ensureEvents calls trying
            // to spark the event stream back up after the user has quit
            sessionWasQuit_ = true;
        
            // fire event
            eventBus.fireEvent(new QuitEvent());
         }
         else if (type.equals(ClientEvent.Suicide))
         {
            // NOTE: we don't explicitly stop listening for events here
            // for the reasons cited above in ClientEvent.Quit
            
            // fire event
            String message = event.getData();
            eventBus.fireEvent(new SuicideEvent(message));
         }
         else if (type.equals(ClientEvent.AsyncCompletion))
         {
            AsyncCompletion completion = event.getData();
            String handle = completion.getHandle();
            AsyncRequestInfo req = asyncRequests_.remove(handle);
            if (req != null)
            {
               req.callback.onResponseReceived(req.request,
                                               completion.getResponse());
            }
            else
            {
               // We haven't seen this request yet. Store it for later,
               // maybe it's just taking a long time for the request
               // to complete.
               asyncResponses_.put(handle, completion.getResponse());
            }
         }
         else if (type.equals(ClientEvent.SaveActionChanged))
         {
            SaveAction action = event.getData();
            eventBus.fireEvent(new SaveActionChangedEvent(action));
         }
         else
         {
            GWT.log("WARNING: Server event not dispatched: " + type, null);
         }
      }
      catch(Throwable e)
      {
         GWT.log("WARNING: Exception occured dispatching event: " + type, e);
      }
   }
   
   // NOTE: the design of the Watchdog likely results in more restarts of
   // the event service than is optimal. when an rpc call reports that 
   // events are pending and the Watchdog is invoked it is very likely
   // that the events have already been delievered in response to the 
   // previous poll. In this case the Watchdog "misses" those events which
   // were already delivered and subsequently assumes that the service
   // needs to be restarted
   
   private class Watchdog
   {  
      public void run(int waitMs)
      {
         isRunning_ = true;
         responseReceived_ = false ;
         
         Timer timer = new Timer() {
            public void run()
            {
               try
               {
                  if (!responseReceived_)
                  {
                     // ensure that the workbench wasn't closed while we
                     // were waiting for the timer to run
                     if (!sessionWasQuit_) 
                        restart();
                  }
               }
               catch(Throwable e)
               {
                  GWT.log("Error restarting event source", e);
               }
               
               isRunning_ = false;
               responseReceived_ = false ;
            }
          
         };
         timer.schedule(waitMs);
      }
      
      public boolean isRunning()
      {
         return isRunning_ ;
      }
      
      public void notifyResponseReceived()
      {
         responseReceived_ = true;
      }
      
      private boolean isRunning_ = false;
      private boolean responseReceived_ = false;
   }

   public void registerAsyncHandle(String asyncHandle,
                                   RpcRequest request,
                                   RpcRequestCallback callback)
   {
      RpcResponse response = asyncResponses_.remove(asyncHandle);
      if (response == null)
      {
         // We don't have the response for this request--this is
         // the normal case.
         asyncRequests_.put(asyncHandle,
                            new AsyncRequestInfo(request, callback));
      }
      else
      {
         // We already have the response--the request must've taken
         // a long time to return.
         callback.onResponseReceived(request, response);
      }
   }

   private final RemoteServer server_;
   
   // note: kSecondListenDelayMs must be less than kWatchdogIntervalMs
   // (by a reasonable margin) to void the watchdog getting involved 
   // unnecessarily during a listen delay
   private final int kWatchdogIntervalMs = 1000;
   private final int kSecondListenBounceMs = 250;
       
   private boolean isListening_;
   private int lastEventId_ ;
   private int listenCount_ ;
   private int listenErrorCount_ ;
   private boolean sessionWasQuit_ ;
   
   private RpcRequest activeRequest_ ;
   private ServerRequestCallback<JsArray<ClientEvent>> activeRequestCallback_;

   private final ArrayList<ClientEvent> pendingEvents_ = new ArrayList<ClientEvent>();
   
   private Watchdog watchdog_ = new Watchdog();

   // Stores async requests that expect to be completed later.
   private final HashMap<String, AsyncRequestInfo> asyncRequests_
         = new HashMap<String, AsyncRequestInfo>();

   // Stores any async responses that didn't have matching requests at the
   // time they were received. This is to deal with any race conditions where
   // the completion occurs before we even finished making the request.
   private final HashMap<String, RpcResponse> asyncResponses_
         = new HashMap<String, RpcResponse>();
}
