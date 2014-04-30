package roboguice.roboguice.event.eventListener.factory;

import roboguice.roboguice.event.EventListener;
import roboguice.roboguice.event.EventThread;
import roboguice.roboguice.event.eventListener.AsynchronousEventListenerDecorator;
import roboguice.roboguice.event.eventListener.UIThreadEventListenerDecorator;

import com.google.inject.Inject;
import com.google.inject.Provider;

import android.os.Handler;

/**
 * @author John Ericksen
 */
public class EventListenerThreadingDecorator {

    @Inject protected Provider<Handler> handlerProvider;

    public <T> EventListener<T> decorate(EventThread threadType, EventListener<T> eventListener){
        switch (threadType){
            case UI:
                return new UIThreadEventListenerDecorator<T>(eventListener, handlerProvider.get() );
            case BACKGROUND:
                return new AsynchronousEventListenerDecorator<T>(eventListener);
            default:
                return eventListener;
        }
    }
}
