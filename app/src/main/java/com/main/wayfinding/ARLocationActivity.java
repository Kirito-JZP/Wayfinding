package com.main.wayfinding;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.main.wayfinding.databinding.ActivityArnavigationBinding;
import com.main.wayfinding.dto.LocationDto;
import com.main.wayfinding.logic.TrackerLogic;
import com.main.wayfinding.utility.ArLocationUtils;
import com.main.wayfinding.utility.PlaceManagerUtils;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;
import uk.co.appoly.arcorelocation.rendering.LocationNode;
import uk.co.appoly.arcorelocation.rendering.LocationNodeRender;
import uk.co.appoly.arcorelocation.sensor.DeviceLocationChanged;
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper;

public class ARLocationActivity extends AppCompatActivity {
    private boolean installRequested;
    private boolean hasFinishedLoading = false;
    private Snackbar loadingMessageSnackbar = null;
    private ArSceneView arSceneView;
    private ModelRenderable modelRenderable;
    private ViewRenderable layoutRenderable;
    private LocationScene locationScene;
    private ActivityArnavigationBinding binding;
    ArrayList<LocationDto> list;
    private Location lastPosition;
    private boolean needUpdate;
    private ImageView arReturnBtn;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArnavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        arSceneView = findViewById(R.id.ar_scene_view);
        arReturnBtn = findViewById(R.id.arReturnBtn);

        CompletableFuture<ViewRenderable> layout =
                ViewRenderable.builder()
                        .setView(this, R.layout.layout_artext)
                        .build();

        CompletableFuture<ModelRenderable> model = ModelRenderable.builder()
                .setSource(this, R.raw.andy)
                .build();


        CompletableFuture.allOf(layout, model).handle(
                (notUsed, throwable) -> {
                    // When you build a Renderable, Sceneform loads its resources in the background while
                    // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                    // before calling get().

                    if (throwable != null) {
                        ArLocationUtils.displayError(this, "Unable to load renderables", throwable);
                        return null;
                    }

                    try {
                        layoutRenderable = layout.get();
                        modelRenderable = model.get();
                        hasFinishedLoading = true;

                    } catch (InterruptedException | ExecutionException ex) {
                        ArLocationUtils.displayError(this, "Unable to load renderables", ex);
                    }

                    return null;
                });

        // Set an update listener on the Scene that will hide the loading message once a Plane is
        // detected.
        arSceneView
                .getScene()
                .addOnUpdateListener(
                        frameTime -> {
                            if (!hasFinishedLoading) {
                                return;
                            }

                            if (locationScene == null) {
                                // If our locationScene object hasn't been setup yet, this is a good time to do it
                                // We know that here, the AR components have been initiated.
                                locationScene = new LocationScene(this, this, arSceneView);
                                ARLocationActivity thisActivity = this;
                                locationScene.setLocationChangedEvent(new DeviceLocationChanged() {
                                    @Override
                                    public void onChange(Location ArLocation) {
                                        TrackerLogic trackerLogic = TrackerLogic.createInstance(thisActivity);
                                        trackerLogic.requestLastLocation(new TrackerLogic.RequestLocationCompleteCallback() {
                                            @Override
                                            public void onRequestLocationComplete(Location location) {
                                                locationScene.deviceLocation.currentBestLocation.setLatitude(location.getLatitude());
                                                locationScene.deviceLocation.currentBestLocation.setLongitude(location.getLongitude());
                                                if (lastPosition == null) {
                                                    lastPosition = location;
                                                    list = PlaceManagerUtils.getNearby(location);
                                                    needUpdate = true;
                                                }
                                                if (needUpdate) {
                                                    for (LocationDto locationDto : list) {
                                                        //------------------------
                                                        CompletableFuture<ViewRenderable> layout =
                                                                ViewRenderable.builder()
                                                                        .setView(thisActivity, R.layout.layout_artext)
                                                                        .build();

                                                        CompletableFuture<ModelRenderable> model = ModelRenderable.builder()
                                                                .setSource(thisActivity, R.raw.andy)
                                                                .build();
                                                        CompletableFuture.allOf(layout, model).handle(
                                                                (notUsed, throwable) -> {
                                                                    // When you build a Renderable, Sceneform loads its resources in the background while
                                                                    // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                                                                    // before calling get().

                                                                    if (throwable != null) {
                                                                        ArLocationUtils.displayError(thisActivity, "Unable to load renderables", throwable);
                                                                        return null;
                                                                    }
                                                                    try {
                                                                        layoutRenderable = layout.get();
                                                                        modelRenderable = model.get();
                                                                        hasFinishedLoading = true;
                                                                    } catch (InterruptedException | ExecutionException ex) {
                                                                        ArLocationUtils.displayError(thisActivity, "Unable to load renderables", ex);
                                                                    }
                                                                    //-----------------------------
                                                                    // Now lets create our location markers.
                                                                    // First, a layout

                                                                    LocationMarker layoutLocationMarker = new LocationMarker(
                                                                            locationDto.getLongitude(),
                                                                            locationDto.getLatitude(),
                                                                            createViewNode()
                                                                    );

                                                                    // Updates the layout with the markers distance
                                                                    ViewRenderable copyOfRenderable = layoutRenderable.makeCopy();
                                                                    String name = locationDto.getName();
                                                                    layoutLocationMarker.setRenderEvent(new LocationNodeRender() {
                                                                        @Override
                                                                        public void render(LocationNode node) {
                                                                            View eView = copyOfRenderable.getView();
                                                                            TextView distanceTextView = eView.findViewById(R.id.loc_distance);
                                                                            TextView nameTextView = eView.findViewById(R.id.loc_name);
                                                                            nameTextView.setText(name);
                                                                            distanceTextView.setText(node.getDistance() + "M");

                                                                        }
                                                                    });
                                                                    // Adding the marker
                                                                    locationScene.mLocationMarkers.add(layoutLocationMarker);

                                                                    // Adding a simple location marker of a 3D model
                                                                    locationScene.mLocationMarkers.add(
                                                                            new LocationMarker(
                                                                                    locationDto.getLongitude(),
                                                                                    locationDto.getLatitude(),
                                                                                    createModelNode()));
                                                                    return null;
                                                                });
                                                        //-------------------------
                                                    }
                                                    needUpdate = false;
                                                }
                                                if (location.distanceTo(lastPosition) >= 15) {
                                                    lastPosition = location;
                                                    list = PlaceManagerUtils.getNearby(location);
                                                    locationScene.clearMarkers();
                                                    needUpdate = true;
                                                }
                                            }
                                        });
                                    }
                                });
                            }

                            Frame frame = arSceneView.getArFrame();
                            if (frame == null) {
                                return;
                            }

                            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                                return;
                            }

                            if (locationScene != null) {
                                locationScene.processFrame(frame);
                            }

                            if (loadingMessageSnackbar != null) {
                                for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                                    if (plane.getTrackingState() == TrackingState.TRACKING) {
                                        hideLoadingMessage();
                                    }
                                }
                            }
                        });


        // Lastly request CAMERA & fine location permission which is required by ARCore-Location.
        ARLocationPermissionHelper.requestPermission(this);


        arReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ARLocationActivity.this, MainActivity.class);
                intent.putExtra("id", 1);
                startActivity(intent);
            }
        });
    }

    private Node createViewNode() {
        Node base = new Node();
        base.setRenderable(layoutRenderable);
        Context c = this;
        // Add  listeners etc here
        View eView = layoutRenderable.getView();
        eView.setOnTouchListener((v, event) -> {
            Toast.makeText(
                    c, "Location marker touched.", Toast.LENGTH_LONG)
                    .show();
            return false;
        });

        return base;
    }

    private Node createModelNode() {
        Node base = new Node();
        base.setRenderable(modelRenderable);
        Context c = this;
        base.setOnTapListener((v, event) -> {
            Toast.makeText(
                    c, "Andy touched.", Toast.LENGTH_LONG)
                    .show();
        });
        return base;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (locationScene != null) {
            locationScene.resume();
        }

        if (arSceneView.getSession() == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                Session session = ArLocationUtils.createArSession(this, installRequested);
                if (session == null) {
                    installRequested = ARLocationPermissionHelper.hasPermission(this);
                    return;
                } else {
                    arSceneView.setupSession(session);
                }
            } catch (UnavailableException e) {
                ArLocationUtils.handleSessionException(this, e);
            }
        }

        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            ArLocationUtils.displayError(this, "Unable to get camera", ex);
            finish();
            return;
        }

        if (arSceneView.getSession() != null) {
            showLoadingMessage();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (locationScene != null) {
            locationScene.pause();
        }

        arSceneView.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        arSceneView.destroy();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (!ARLocationPermissionHelper.hasPermission(this)) {
            if (!ARLocationPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                ARLocationPermissionHelper.launchPermissionSettings(this);
            } else {
                Toast.makeText(
                        this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                        .show();
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void showLoadingMessage() {
        if (loadingMessageSnackbar != null && loadingMessageSnackbar.isShownOrQueued()) {
            return;
        }

        loadingMessageSnackbar =
                Snackbar.make(
                        ARLocationActivity.this.findViewById(android.R.id.content),
                        R.string.plane_finding,
                        Snackbar.LENGTH_INDEFINITE);
        loadingMessageSnackbar.getView().setBackgroundColor(0xbf323232);
        loadingMessageSnackbar.show();
    }

    private void hideLoadingMessage() {
        if (loadingMessageSnackbar == null) {
            return;
        }

        loadingMessageSnackbar.dismiss();
        loadingMessageSnackbar = null;
    }
}