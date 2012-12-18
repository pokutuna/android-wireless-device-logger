# android-wireless-device-logger

http://pokutuna.github.com/android-wireless-device-logger/


## download

https://github.com/pokutuna/android-wireless-device-logger/tree/master/build

click `Raw` link



## build
__Don't mind this part if you download .apk file__


Building this app requires [simple-build-tool](http://www.scala-sbt.org/).  
Install and run it.

```
$ sbt
> android:start-device
```


'Send to Dropbox' feature of this app needs Dropbox application keys.  
Get app keys from https://www.dropbox.com/developers , and make a xml file including app keys like following and put it to `src/main/res/values/secrets.xml`.

```xml
<resources>
  <string name="DropboxAppKey">***************</string>
  <string name="DropboxAppSecret">***************</string>
</resources>
```



## using library
this software id depending following libs.

- [Dropbox SDK for Android](https://www.dropbox.com/developers/reference/sdk)
- [jgilfelt / android-viewbadger](https://github.com/jgilfelt/android-viewbadger)
