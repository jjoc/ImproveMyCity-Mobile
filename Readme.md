### Localize your app

Server: 
src.com.mk4droid.IMC_Store/Constants_AP.java -> ``ServerSTR = "myserver.com"``;

Deployment area: 
src.com.mk4droid.IMC_Store/Constants_AP.java -> AppGPSLimits =   {40.57,  40.41,    23.24,   22.93}; // (latitudeMax,latitudeMin,longitudeMax,longitudeMin)

### Place your keys 

Google map key:
res/values/strings.xml ->  
``<string name="google_map_api_key" translatable="false">000000000000000000000000</string>``

Joomla/ImproveMyCity encryption key: 
src.com.mk4droid.IMC_Store/Constants_AP.java -> ``EncKey = "adadasdasdasdasd"``

(Optional) Flurry analytics (www.Flurry.com): 
src.com.mk4droid.IMC_Store/Constants_AP.java -> ``Flurry_key = "zxzxczxczxczxcxzc"``