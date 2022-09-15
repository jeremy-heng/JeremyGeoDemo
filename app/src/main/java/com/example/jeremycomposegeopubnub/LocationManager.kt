package com.example.jeremycomposegeopubnub

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.gson.JsonObject


class LocationManager (
    val appContext: MainActivity) {
    var myLocation: MutableState<String> = mutableStateOf("Waiting...")
    val locationRequest = createLocationRequest()
    var fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
    var locationCallback = object: LocationCallback () {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            for (location in locationResult.locations) {
                myLocation.value = "{\"lat\":${location?.latitude.toString()}, \"lng\":${location?.longitude.toString()}}"
                println(myLocation.value)
            }
            if(appContext.isMessage.value == true) {
                appContext.pubnub.publish(
                    channel = "geodata",
                    message = JsonObject().apply {
                        addProperty("lat", locationResult.locations[0].latitude.toString())
                        addProperty("lng", locationResult.locations[0].longitude.toString())
                    })
                    .async { result, status ->
                        // the result is always of a nullable type
                        // it's null if there were errors (status.error)
                        // otherwise it's usable

                        // handle publish result
                        if (!status.error) {
                            println("Message timetoken: ${result!!.timetoken}")
                        } else {
                            // handle error
                            status.exception?.printStackTrace()
                        }
                    }

                /*appContext.pubnub.sendFile(
                    channel = "geodata",
                    fileName = "anyfile",
                    inputStream = "HELLOWORLD".byteInputStream(),
                    message = HelloWorld())
                    .async { result, status ->
                        // the result is always of a nullable type
                        // it's null if there were errors (status.error)
                        // otherwise it's usable

                        // handle publish result
                        if (!status.error) {
                            println("Message timetoken: ${result!!.timetoken}")
                        } else {
                            // handle error
                            status.exception?.printStackTrace()
                        }
                    }*/


            } else {


                appContext.pubnub.signal(
                    channel = "geodata",
                    message = JsonObject().apply {
                        addProperty("lat", locationResult.locations[0].latitude.toString())
                        addProperty("lng", locationResult.locations[0].longitude.toString())
                    })
                    .async { result, status ->
                        if (!status.error) {
                            println("Message timetoken: ${result!!.timetoken}")

                        } else {
                            // handle error
                            status.exception?.printStackTrace()
                        }
                    }
            }
        }
    }


    fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        return locationRequest
    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())

    }



}