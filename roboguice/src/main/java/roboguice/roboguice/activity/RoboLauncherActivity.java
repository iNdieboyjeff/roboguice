/*
 * Copyright 2009 Michael Burton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package roboguice.roboguice.activity;

import java.util.HashMap;
import java.util.Map;

import roboguice.roboguice.RoboGuice;
import roboguice.roboguice.activity.event.OnActivityResultEvent;
import roboguice.roboguice.activity.event.OnContentChangedEvent;
import roboguice.roboguice.activity.event.OnNewIntentEvent;
import roboguice.roboguice.activity.event.OnPauseEvent;
import roboguice.roboguice.activity.event.OnRestartEvent;
import roboguice.roboguice.activity.event.OnResumeEvent;
import roboguice.roboguice.activity.event.OnSaveInstanceStateEvent;
import roboguice.roboguice.activity.event.OnStopEvent;
import roboguice.roboguice.context.event.OnConfigurationChangedEvent;
import roboguice.roboguice.context.event.OnCreateEvent;
import roboguice.roboguice.context.event.OnDestroyEvent;
import roboguice.roboguice.context.event.OnStartEvent;
import roboguice.roboguice.event.EventManager;
import roboguice.roboguice.inject.ContentViewListener;
import roboguice.roboguice.inject.RoboInjector;
import roboguice.roboguice.util.RoboContext;

import com.google.inject.Inject;
import com.google.inject.Key;

import android.app.Activity;
import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

/**
 * A {@link RoboLauncherActivity} extends from {@link LauncherActivity} to provide
 * dynamic injection of collaborators, using Google Guice.<br />
 * 
 * @see RoboActivity
 * 
 * @author Toly Pochkin
 */
public class RoboLauncherActivity extends LauncherActivity implements RoboContext {
    protected EventManager eventManager;
    protected HashMap<Key<?>,Object> scopedObjects = new HashMap<Key<?>, Object>();


    @Inject ContentViewListener ignored; // BUG find a better place to put this

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final RoboInjector injector = RoboGuice.getInjector(this);
        eventManager = injector.getInstance(EventManager.class);
        injector.injectMembersWithoutViews(this);
        super.onCreate(savedInstanceState);
        eventManager.fire(new OnCreateEvent<Activity>(this,savedInstanceState));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        eventManager.fire(new OnSaveInstanceStateEvent(this, outState));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        eventManager.fire(new OnRestartEvent(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        eventManager.fire(new OnStartEvent<Activity>(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventManager.fire(new OnResumeEvent(this));
    }

    @Override
    protected void onPause() {
        super.onPause();
        eventManager.fire(new OnPauseEvent(this));
    }

    @Override
    protected void onNewIntent( Intent intent ) {
        super.onNewIntent(intent);
        eventManager.fire(new OnNewIntentEvent(this));
    }

    @Override
    protected void onStop() {
        try {
            eventManager.fire(new OnStopEvent(this));
        } finally {
            super.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            eventManager.fire(new OnDestroyEvent<Activity>(this));
        } finally {
            try {
                RoboGuice.destroyInjector(this);
            } finally {
                super.onDestroy();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        final Configuration currentConfig = getResources().getConfiguration();
        super.onConfigurationChanged(newConfig);
        eventManager.fire(new OnConfigurationChangedEvent<Activity>(this,currentConfig, newConfig));
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        RoboGuice.getInjector(this).injectViewMembers(this);
        eventManager.fire(new OnContentChangedEvent(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        eventManager.fire(new OnActivityResultEvent(this, requestCode, resultCode, data));
    }

    @Override
    public Map<Key<?>, Object> getScopedObjectMap() {
        return scopedObjects;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        if (RoboActivity.shouldInjectOnCreateView(name))
            return RoboActivity.injectOnCreateView(name, context, attrs);

        return super.onCreateView(name, context, attrs);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        if (RoboActivity.shouldInjectOnCreateView(name))
            return RoboActivity.injectOnCreateView(name, context, attrs);

        return super.onCreateView(parent, name, context, attrs);
    }


}
